package de.mediguard.user;

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
