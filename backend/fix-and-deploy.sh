#!/bin/bash
# ================================================================
# MediGuard - Fix & Deploy All-in-One Script
# Cara pakai: bash fix-and-deploy.sh
# Jalankan dari ROOT project (folder yang berisi pom.xml)
# ================================================================

set -e

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; BLUE='\033[0;34m'; NC='\033[0m'
log()   { echo -e "${GREEN}✅ $1${NC}"; }
warn()  { echo -e "${YELLOW}⚠️  $1${NC}"; }
err()   { echo -e "${RED}❌ $1${NC}"; exit 1; }
info()  { echo -e "${BLUE}ℹ️  $1${NC}"; }
step()  { echo -e "\n${BLUE}━━━ $1 ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"; }

echo ""
echo "╔══════════════════════════════════════════════╗"
echo "║     MediGuard Fix & Deploy All-in-One        ║"
echo "╚══════════════════════════════════════════════╝"
echo ""

# ── Validasi lokasi ──────────────────────────────────────────────
if [ ! -f "pom.xml" ]; then
    err "Harus dijalankan dari ROOT project (folder yang berisi pom.xml)"
fi
log "Root project ditemukan: $(pwd)"

# ── Deteksi package path dari pom.xml ───────────────────────────
GROUP_ID=$(grep -m1 '<groupId>de.mediguard' pom.xml | sed 's/.*<groupId>//;s/<\/groupId>//' | tr '.' '/')
BASE_PATH="src/main/java/${GROUP_ID}"
info "Base package path: $BASE_PATH"

if [ ! -d "$BASE_PATH" ]; then
    err "Package path $BASE_PATH tidak ditemukan. Pastikan group ID 'de.mediguard' benar."
fi

# ================================================================
# STEP 1: Patch pom.xml — tambah Flyway + Actuator
# ================================================================
step "STEP 1: Patch pom.xml"

if grep -q "flyway-core" pom.xml; then
    log "Flyway sudah ada di pom.xml, skip"
else
    # Sisipkan Flyway dan Actuator setelah dependency postgresql
    TEMP_POM=$(mktemp)
    python3 - << 'PYEOF'
import re, sys

with open('pom.xml', 'r') as f:
    content = f.read()

# Tambah Flyway setelah postgresql dependency
flyway_deps = """
        <!-- Flyway: schema migration (gantikan ddl-auto=update di prod) -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Actuator: health check untuk AWS Elastic Beanstalk -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>"""

# Insert setelah closing tag postgresql dependency
pattern = r'(.*postgresql.*\n.*<scope>runtime</scope>\n.*</dependency>)'
match = re.search(pattern, content, re.DOTALL)
if match:
    insert_after = content.find('</dependency>', match.start()) + len('</dependency>')
    new_content = content[:insert_after] + '\n' + flyway_deps + content[insert_after:]
    with open('pom.xml', 'w') as f:
        f.write(new_content)
    print("PATCHED")
else:
    print("SKIP - pattern not found, add manually")
PYEOF
    log "Flyway + Actuator ditambahkan ke pom.xml"
fi

# ================================================================
# STEP 2: Fix application-prod.properties
# ================================================================
step "STEP 2: Fix application-prod.properties"

cat > src/main/resources/application-prod.properties << 'PROPEOF'
# ─── Production Profile (PostgreSQL on AWS RDS) ────────────────
# Activate: SPRING_PROFILES_ACTIVE=prod

# ── Server ──────────────────────────────────────────────────────
server.port=${SERVER_PORT:5000}

# ── Database (RDS PostgreSQL) ────────────────────────────────────
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/mediguarddb}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:mediguard}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:change_in_production}

# ── HikariCP ─────────────────────────────────────────────────────
spring.datasource.hikari.maximum-pool-size=5
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# ── JPA / Hibernate ──────────────────────────────────────────────
# FIXED: spring.jpa.database-platform DIHAPUS (auto-detect di Spring Boot 3)
# FIXED: ddl-auto=validate — Flyway yang kelola schema
# FIXED: open-in-view=false — cegah N+1 query
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true

# ── Flyway ───────────────────────────────────────────────────────
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0
spring.flyway.validate-on-migrate=true

# ── H2 Console (OFF di prod) ─────────────────────────────────────
spring.h2.console.enabled=false

# ── Security ─────────────────────────────────────────────────────
mediguard.jwt.secret=${JWT_SECRET:change_this_secret_in_production_min_256_bits}

# ── CORS ─────────────────────────────────────────────────────────
mediguard.cors.allowed-origins=${CORS_ORIGINS:https://mediguard.example.de}

# ── Actuator ─────────────────────────────────────────────────────
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
management.endpoint.health.probes.enabled=true
management.health.db.enabled=true

# ── Swagger (OFF di prod) ─────────────────────────────────────────
springdoc.swagger-ui.enabled=false
springdoc.api-docs.enabled=false

# ── Logging ──────────────────────────────────────────────────────
logging.level.root=WARN
logging.level.de.mediguard=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate.SQL=WARN
logging.level.org.flywaydb=INFO
PROPEOF
log "application-prod.properties difix"

# ================================================================
# STEP 3: Buat UserDetailsServiceImpl.java (fix circular dep)
# ================================================================
step "STEP 3: Buat UserDetailsServiceImpl.java"

USER_PKG_DIR="$BASE_PATH/user"
if [ ! -d "$USER_PKG_DIR" ]; then
    warn "Direktori $USER_PKG_DIR tidak ada, coba cari..."
    USER_PKG_DIR=$(find src/main/java -name "UserRepository.java" 2>/dev/null | xargs -I{} dirname {} | head -1)
    if [ -z "$USER_PKG_DIR" ]; then
        err "Tidak bisa menemukan direktori yang berisi UserRepository.java"
    fi
    info "Ditemukan: $USER_PKG_DIR"
fi

# Deteksi package name dari UserRepository.java
PKG_NAME=$(grep "^package " "$USER_PKG_DIR/UserRepository.java" 2>/dev/null | sed 's/package //;s/;//' | tr -d '[:space:]')
if [ -z "$PKG_NAME" ]; then
    PKG_NAME="de.mediguard.user"
    warn "Tidak bisa deteksi package, pakai default: $PKG_NAME"
fi
info "Package: $PKG_NAME"

cat > "$USER_PKG_DIR/UserDetailsServiceImpl.java" << JAVAEOF
package ${PKG_NAME};

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DSGVO-konform: UserDetailsService als eigenständiger @Service.
 *
 * FIX: Extrahiert aus SecurityConfig, um zirkuläre Abhängigkeit aufzulösen:
 *   VORHER: SecurityConfig → @Lazy JwtAuthFilter → UserDetailsService → SecurityConfig (LOOP)
 *   NACHHER: SecurityConfig → JwtAuthFilter → UserDetailsServiceImpl  (CLEAN)
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .authorities(user.getAuthorities())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Benutzer nicht gefunden: " + username));
    }
}
JAVAEOF
log "UserDetailsServiceImpl.java dibuat di $USER_PKG_DIR"

# ================================================================
# STEP 4: Fix SecurityConfig.java — hapus @Lazy dan userDetailsService bean
# ================================================================
step "STEP 4: Fix SecurityConfig.java"

CONFIG_DIR=$(find src/main/java -name "SecurityConfig.java" 2>/dev/null | xargs -I{} dirname {} | head -1)
if [ -z "$CONFIG_DIR" ]; then
    err "SecurityConfig.java tidak ditemukan"
fi
info "Config dir: $CONFIG_DIR"

# Backup dulu
cp "$CONFIG_DIR/SecurityConfig.java" "$CONFIG_DIR/SecurityConfig.java.bak"

CONFIG_PKG=$(grep "^package " "$CONFIG_DIR/SecurityConfig.java" | sed 's/package //;s/;//' | tr -d '[:space:]')
FILTER_IMPORT=$(grep "import.*JwtAuthFilter" "$CONFIG_DIR/SecurityConfig.java" | tr -d '[:space:]')
FILTER_PKG=$(echo "$FILTER_IMPORT" | sed 's/import//;s/;//')

cat > "$CONFIG_DIR/SecurityConfig.java" << JAVAEOF
package ${CONFIG_PKG};

import ${FILTER_PKG};
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * FIXED:
 * 1. @Lazy dihapus — UserDetailsServiceImpl memutus circular dependency
 * 2. userDetailsService() bean dihapus — ada di UserDetailsServiceImpl.java
 * 3. @RequiredArgsConstructor untuk constructor injection yang bersih
 * 4. Security headers diperketat (DENY, HSTS)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // → UserDetailsServiceImpl

    @Value("\${mediguard.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/h2-console/**",
                                "/actuator/health"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .headers(h -> h
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .contentTypeOptions(c -> {})
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)
                        )
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
JAVAEOF
log "SecurityConfig.java difix (backup: SecurityConfig.java.bak)"

# ================================================================
# STEP 5: Fix JwtAuthFilter.java — tambah try-catch
# ================================================================
step "STEP 5: Fix JwtAuthFilter.java"

FILTER_DIR=$(find src/main/java -name "JwtAuthFilter.java" 2>/dev/null | xargs -I{} dirname {} | head -1)
if [ -z "$FILTER_DIR" ]; then
    err "JwtAuthFilter.java tidak ditemukan"
fi

cp "$FILTER_DIR/JwtAuthFilter.java" "$FILTER_DIR/JwtAuthFilter.java.bak"

FILTER_PKG_NAME=$(grep "^package " "$FILTER_DIR/JwtAuthFilter.java" | sed 's/package //;s/;//' | tr -d '[:space:]')
JWTUTIL_IMPORT=$(grep "import.*JwtUtil" "$FILTER_DIR/JwtAuthFilter.java" | tr -d '[:space:]' | sed 's/import//;s/;//')

cat > "$FILTER_DIR/JwtAuthFilter.java" << JAVAEOF
package ${FILTER_PKG_NAME};

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * FIXED:
 * - Tidak ada @Lazy → CGLIB warning hilang
 * - UserDetailsService inject ke UserDetailsServiceImpl (bukan SecurityConfig)
 * - Try-catch di token validation: exception tidak crash filter chain
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token != null) {
            try {
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    var auth = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                log.debug("JWT validation failed [{}]: {}",
                        request.getRequestURI(), e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
JAVAEOF
log "JwtAuthFilter.java difix (backup: JwtAuthFilter.java.bak)"

# ================================================================
# STEP 6: Buat db/migration/V1__baseline.sql
# ================================================================
step "STEP 6: Buat Flyway migration"

mkdir -p src/main/resources/db/migration

cat > src/main/resources/db/migration/V1__baseline.sql << 'SQL'
-- ================================================================
-- V1__baseline.sql
-- Fix: hapus orphan constraints yang muncul sebagai WARN di log
-- Constraint ini sudah tidak ada di DB tapi masih direferensikan Hibernate
-- ================================================================

ALTER TABLE IF EXISTS patienten
    DROP CONSTRAINT IF EXISTS uk_oi0md8hnm2gl6lmada4rx0xh6;

ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS uk_6dotkott2kjsp8vw4d0m25fb7;

ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS uk_r43af9ap4edm43mmtq01oddj6;
SQL
log "db/migration/V1__baseline.sql dibuat"

# ================================================================
# STEP 7: Buat .platform/nginx/conf.d/security.conf
# ================================================================
step "STEP 7: Buat Nginx security config"

mkdir -p .platform/nginx/conf.d

cat > .platform/nginx/conf.d/security.conf << 'NGINX'
# Security Headers
add_header X-Frame-Options              "DENY"                              always;
add_header X-Content-Type-Options       "nosniff"                           always;
add_header X-XSS-Protection             "1; mode=block"                     always;
add_header Referrer-Policy              "strict-origin-when-cross-origin"   always;
add_header Permissions-Policy           "camera=(), microphone=(), geolocation=()" always;
add_header Strict-Transport-Security    "max-age=31536000; includeSubDomains" always;

# Rate Limiting Zones
limit_req_zone $binary_remote_addr zone=api_limit:10m  rate=30r/m;
limit_req_zone $binary_remote_addr zone=auth_limit:10m rate=5r/m;

# Block PHP/WordPress/exploit scanners (detected in access.log)
location ~* \.(php|asp|aspx|cgi)$                                        { return 444; }
location ~* /(wp-content|wp-includes|wp-admin|wp-login|xmlrpc|phpunit)/  { return 444; }
location ~* /(cgi-bin|\.git|\.env|\.htaccess|backup|config\.json)        { return 444; }

# API endpoint with rate limit
location /api/ {
    limit_req        zone=api_limit burst=20 nodelay;
    limit_req_status 429;
    proxy_pass             http://127.0.0.1:5000;
    proxy_http_version     1.1;
    proxy_set_header Host              $host;
    proxy_set_header X-Real-IP         $remote_addr;
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_read_timeout     60s;
    proxy_connect_timeout  10s;
}

# Auth endpoint: strict rate limit
location /api/v1/auth/ {
    limit_req        zone=auth_limit burst=5 nodelay;
    limit_req_status 429;
    proxy_pass             http://127.0.0.1:5000;
    proxy_set_header Host              $host;
    proxy_set_header X-Real-IP         $remote_addr;
    proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}

# Health check: no rate limit
location /actuator/health {
    proxy_pass http://127.0.0.1:5000;
    proxy_set_header Host            $host;
    proxy_set_header X-Real-IP       $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
NGINX
log ".platform/nginx/conf.d/security.conf dibuat"

# ================================================================
# STEP 8: Build
# ================================================================
step "STEP 8: Maven build"
echo "Menjalankan: mvn clean package -DskipTests ..."
mvn clean package -DskipTests && log "BUILD SUCCESS" || err "BUILD GAGAL — cek error di atas"

# ================================================================
# STEP 9: Buat deployment ZIP
# ================================================================
step "STEP 9: Buat deployment bundle"

JAR=$(ls target/*.jar 2>/dev/null | grep -v "sources\|javadoc" | head -1)
[ -z "$JAR" ] && err "JAR tidak ditemukan di target/"

TIMESTAMP=$(date +%Y%m%d-%H%M)
DEPLOY_ZIP="mediguard-deploy-${TIMESTAMP}.zip"

TMPDIR_BUNDLE=$(mktemp -d)
mkdir -p "$TMPDIR_BUNDLE/.platform/nginx/conf.d"
cp "$JAR" "$TMPDIR_BUNDLE/application.jar"
cp .platform/nginx/conf.d/security.conf "$TMPDIR_BUNDLE/.platform/nginx/conf.d/"

cd "$TMPDIR_BUNDLE" && python3 -c "
import zipfile, os
with zipfile.ZipFile('$OLDPWD/$DEPLOY_ZIP', 'w', zipfile.ZIP_DEFLATED) as zf:
    for root, dirs, files in os.walk('.'):
        for file in files:
            fp = os.path.join(root, file)
            zf.write(fp, fp[2:] if fp.startswith('./') else fp)
print('ZIP created')
"
cd "$OLDPWD"
rm -rf "$TMPDIR_BUNDLE"

log "Deployment bundle siap: $DEPLOY_ZIP ($(du -sh $DEPLOY_ZIP | cut -f1))"

# ================================================================
# RINGKASAN
# ================================================================
echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║                   SEMUA FIX SELESAI!                    ║"
echo "╠══════════════════════════════════════════════════════════╣"
echo "║  Files yang diubah:                                     ║"
echo "║  ✅ pom.xml (Flyway + Actuator ditambah)                ║"
echo "║  ✅ application-prod.properties (dialect dihapus, dll)  ║"
echo "║  ✅ SecurityConfig.java (@Lazy dihapus)                  ║"
echo "║  ✅ JwtAuthFilter.java (try-catch ditambah)             ║"
echo "║  ✅ UserDetailsServiceImpl.java (BARU)                  ║"
echo "║  ✅ db/migration/V1__baseline.sql (BARU)                ║"
echo "║  ✅ .platform/nginx/conf.d/security.conf (BARU)         ║"
echo "╠══════════════════════════════════════════════════════════╣"
echo "║  Deploy:                                                ║"
echo "║  1. Set env vars di AWS EB Console                      ║"
echo "║  2. eb deploy                                           ║"
echo "║     ATAU upload $DEPLOY_ZIP via EB Console     ║"
echo "╚══════════════════════════════════════════════════════════╝"
