package de.mediguard.audit;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

// ─── Entity ────────────────────────────────────────────
@Entity
@Table(name = "audit_protokoll")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benutzer_name", nullable = false, length = 100)
    private String benutzerName; // Who performed the action

    @Column(nullable = false, length = 50)
    private String aktion; // Action: LOGIN, PATIENT_CREATE, RECORD_VIEW, etc.

    @Column(name = "ressource_typ", length = 50)
    private String ressourceTyp; // Patient, MedicalRecord, Incident

    @Column(name = "ressource_id")
    private Long ressourceId;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_adresse", length = 45)
    private String ipAdresse;

    @Column(name = "war_erfolgreich")
    @Builder.Default
    private Boolean warErfolgreich = true;

    @CreatedDate
    @Column(name = "zeitstempel", updatable = false)
    private LocalDateTime zeitstempel;
}

// ─── Repository ────────────────────────────────────────
interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByZeitstempelDesc();
    List<AuditLog> findByBenutzerNameOrderByZeitstempelDesc(String benutzerName);
    long countByWarErfolgreichFalseAndZeitstempelAfter(LocalDateTime since);
}

// ─── Service ───────────────────────────────────────────
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String benutzer, String aktion, String ressourceTyp,
                    Long ressourceId, String details) {
        AuditLog log = AuditLog.builder()
                .benutzerName(benutzer)
                .aktion(aktion)
                .ressourceTyp(ressourceTyp)
                .ressourceId(ressourceId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> findRecent() {
        return auditLogRepository.findTop100ByOrderByZeitstempelDesc();
    }

    public List<AuditLog> findByUser(String username) {
        return auditLogRepository.findByBenutzerNameOrderByZeitstempelDesc(username);
    }
}

// ─── Controller ────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getRecent() {
        return ResponseEntity.ok(auditService.findRecent());
    }

    @GetMapping("/benutzer/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getByUser(@PathVariable String username) {
        return ResponseEntity.ok(auditService.findByUser(username));
    }
}
