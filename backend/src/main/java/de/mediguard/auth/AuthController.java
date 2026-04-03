package de.mediguard.auth;

import de.mediguard.user.Role;
import de.mediguard.user.User;
import de.mediguard.user.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.time.LocalDateTime;

// ─── DTOs ──────────────────────────────────────────────
class LoginRequest {
    @NotBlank public String username;
    @NotBlank public String password;
}

class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50) public String username;
    @NotBlank @Email public String email;
    @NotBlank public String fullName;
    @NotBlank @Size(min = 8) public String password;
    public String abteilung;
    public Role role = Role.VERWALTUNG;
}

@Data @Builder
class AuthResponse {
    private String token;
    private String tokenType;
    private UserInfo user;

    @Data @Builder
    static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String role;
        private String abteilung;
        private LocalDateTime createdAt;
    }
}

// ─── Service ───────────────────────────────────────────
@Service
@RequiredArgsConstructor
class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password));
        User user = userRepository.findByUsername(req.username)
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden"));
        return buildResponse(user);
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username))
            throw new IllegalArgumentException("Benutzername bereits vergeben");
        if (userRepository.existsByEmail(req.email))
            throw new IllegalArgumentException("E-Mail bereits registriert");

        User user = User.builder()
                .username(req.username)
                .email(req.email)
                .fullName(req.fullName)
                .password(passwordEncoder.encode(req.password))
                .abteilung(req.abteilung)
                .role(req.role)
                .build();
        userRepository.save(user);
        return buildResponse(user);
    }

    private AuthResponse buildResponse(User user) {
        String token = jwtUtil.generateToken(user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .abteilung(user.getAbteilung())
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}

// ─── Controller ────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.status(201).body(authService.register(req));
    }
}
