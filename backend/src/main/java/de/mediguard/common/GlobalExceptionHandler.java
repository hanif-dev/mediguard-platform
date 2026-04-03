package de.mediguard.common;

import de.mediguard.patient.*;
import de.mediguard.user.Role;
import de.mediguard.user.User;
import de.mediguard.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.stream.Collectors;

// ─── Global Exception Handler ──────────────────────────
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validierungsfehler: " + errors);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ProblemDetail handleForbidden(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Zugriff verweigert - unzureichende Berechtigungen");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                "Interner Serverfehler: " + ex.getMessage());
    }
}

// ─── Data Seeder ───────────────────────────────────────
@Configuration
@RequiredArgsConstructor
@Slf4j
class DataSeeder {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedData() {
        return args -> {
            if (userRepository.count() > 0) return;

            // Seed users
            userRepository.save(User.builder()
                    .username("admin").email("admin@mediguard.de").fullName("System Administrator")
                    .password(passwordEncoder.encode("MediGuard2024!"))
                    .role(Role.ADMIN).abteilung("IT-Administration").build());

            userRepository.save(User.builder()
                    .username("dr.mueller").email("mueller@mediguard.de").fullName("Dr. Hans Müller")
                    .password(passwordEncoder.encode("Arzt2024!"))
                    .role(Role.ARZT).abteilung("Innere Medizin").build());

            userRepository.save(User.builder()
                    .username("pfleger.schmidt").email("schmidt@mediguard.de").fullName("Anna Schmidt")
                    .password(passwordEncoder.encode("Pflege2024!"))
                    .role(Role.PFLEGER).abteilung("Station 3B").build());

            userRepository.save(User.builder()
                    .username("verwaltung").email("verwaltung@mediguard.de").fullName("Klaus Weber")
                    .password(passwordEncoder.encode("Verwalt2024!"))
                    .role(Role.VERWALTUNG).abteilung("Patientenaufnahme").build());

            // Seed demo patients
            patientRepository.save(Patient.builder()
                    .patientenNr("P-2024-0001").nachname("Bauer").vorname("Lieselotte")
                    .geburtsdatum(LocalDate.of(1948, 3, 15)).geschlecht(Geschlecht.WEIBLICH)
                    .adresse("Hauptstraße 12").plz("10115").stadt("Berlin")
                    .telefon("030-12345678").versicherungsart(Versicherungsart.GESETZLICH)
                    .krankenkasse("AOK Nordost").versicherungsnummer("A123456789")
                    .allergien("Penicillin").behandelnderArzt("Dr. Hans Müller").build());

            patientRepository.save(Patient.builder()
                    .patientenNr("P-2024-0002").nachname("Fischer").vorname("Thomas")
                    .geburtsdatum(LocalDate.of(1975, 7, 22)).geschlecht(Geschlecht.MAENNLICH)
                    .adresse("Kastanienallee 5").plz("20354").stadt("Hamburg")
                    .telefon("040-87654321").versicherungsart(Versicherungsart.PRIVAT)
                    .krankenkasse("DKV").versicherungsnummer("P987654321")
                    .vorerkrankungen("Diabetes Typ 2, Hypertonie").behandelnderArzt("Dr. Hans Müller").build());

            patientRepository.save(Patient.builder()
                    .patientenNr("P-2024-0003").nachname("Wagner").vorname("Maria")
                    .geburtsdatum(LocalDate.of(1990, 11, 5)).geschlecht(Geschlecht.WEIBLICH)
                    .adresse("Schillerstraße 88").plz("80336").stadt("München")
                    .telefon("089-11223344").versicherungsart(Versicherungsart.GESETZLICH)
                    .krankenkasse("TK").behandelnderArzt("Dr. Hans Müller").build());

            log.info("✅ Demo-Daten erfolgreich geladen");
            log.info("   admin / MediGuard2024!  |  dr.mueller / Arzt2024!  |  pfleger.schmidt / Pflege2024!");
        };
    }
}
