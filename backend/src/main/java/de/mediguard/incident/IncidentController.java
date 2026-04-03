package de.mediguard.incident;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

// ─── Enums ─────────────────────────────────────────────
enum Schweregrad { NIEDRIG, MITTEL, HOCH, KRITISCH }
enum VorfallKategorie {
    DATENPANNE,         // Data breach - §33 DSGVO reporting required
    PHISHING,           // Phishing attack
    RANSOMWARE,         // Ransomware
    UNBEFUGTER_ZUGRIFF, // Unauthorized access
    GERAETEVERLUST,     // Device loss/theft
    SOCIAL_ENGINEERING, // Social engineering
    INSIDER_BEDROHUNG,  // Insider threat
    SONSTIGES           // Other
}
enum VorfallStatus { OFFEN, IN_BEARBEITUNG, GELOEST, GESCHLOSSEN }

// ─── Entity ────────────────────────────────────────────
@Entity
@Table(name = "sicherheitsvorfaelle")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class SecurityIncident {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String beschreibung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VorfallKategorie kategorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Schweregrad schweregrad;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VorfallStatus status = VorfallStatus.OFFEN;

    @Column(name = "betroffene_systeme", length = 500)
    private String betroffeneSysteme; // Affected systems

    @Column(name = "betroffene_patienten_anzahl")
    private Integer betroffenePatienten; // Number of affected patients

    // §33 DSGVO: Must report to BSI within 72h if personal data breached
    @Column(name = "dsgvo_meldepflichtig")
    @Builder.Default
    private Boolean dsgvoMeldepflichtig = false;

    @Column(name = "bsi_gemeldet")
    @Builder.Default
    private Boolean bsiGemeldet = false; // Reported to BSI (Bundesamt für Sicherheit)

    @Column(name = "melder_name", nullable = false, length = 100)
    private String melderName; // Reporter name

    @Column(name = "melder_abteilung", length = 100)
    private String melderAbteilung;

    @Column(name = "zugewiesen_an", length = 100)
    private String zugewiesenAn; // Assigned to

    @Column(columnDefinition = "TEXT")
    private String massnahmen; // Countermeasures taken

    @Column(columnDefinition = "TEXT")
    private String loesung; // Resolution

    @CreatedDate
    @Column(name = "gemeldet_am", updatable = false)
    private LocalDateTime gemeldetAm;

    @Column(name = "geloest_am")
    private LocalDateTime geloestAm;
}

// ─── DTO ───────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class IncidentDto {
    @NotBlank private String titel;
    @NotBlank private String beschreibung;
    @NotNull private VorfallKategorie kategorie;
    @NotNull private Schweregrad schweregrad;
    private String betroffeneSysteme;
    private Integer betroffenePatienten;
    private Boolean dsgvoMeldepflichtig;
    @NotBlank private String melderName;
    private String melderAbteilung;
}

@Data
class IncidentUpdateDto {
    private VorfallStatus status;
    private String zugewiesenAn;
    private String massnahmen;
    private String loesung;
    private Boolean bsiGemeldet;
}

// ─── Repository ────────────────────────────────────────
interface IncidentRepository extends JpaRepository<SecurityIncident, Long> {
    List<SecurityIncident> findAllByOrderByGemeldetAmDesc();
    List<SecurityIncident> findByStatusOrderByGemeldetAmDesc(VorfallStatus status);
    long countByStatus(VorfallStatus status);
    long countBySchweregrad(Schweregrad schweregrad);
    List<SecurityIncident> findByDsgvoMeldepflichtigTrueAndBsiGemeldetFalse();
}

// ─── Service ───────────────────────────────────────────
@Service
@RequiredArgsConstructor
class IncidentService {

    private final IncidentRepository incidentRepository;

    public List<SecurityIncident> findAll(String status) {
        if (status != null && !status.isBlank()) {
            return incidentRepository.findByStatusOrderByGemeldetAmDesc(VorfallStatus.valueOf(status.toUpperCase()));
        }
        return incidentRepository.findAllByOrderByGemeldetAmDesc();
    }

    public SecurityIncident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Vorfall nicht gefunden: " + id));
    }

    public SecurityIncident create(IncidentDto dto) {
        SecurityIncident incident = SecurityIncident.builder()
                .titel(dto.getTitel())
                .beschreibung(dto.getBeschreibung())
                .kategorie(dto.getKategorie())
                .schweregrad(dto.getSchweregrad())
                .betroffeneSysteme(dto.getBetroffeneSysteme())
                .betroffenePatienten(dto.getBetroffenePatienten())
                .dsgvoMeldepflichtig(dto.getDsgvoMeldepflichtig() != null ? dto.getDsgvoMeldepflichtig() : false)
                .melderName(dto.getMelderName())
                .melderAbteilung(dto.getMelderAbteilung())
                .build();
        return incidentRepository.save(incident);
    }

    public SecurityIncident update(Long id, IncidentUpdateDto dto) {
        SecurityIncident incident = findById(id);
        if (dto.getStatus() != null) {
            incident.setStatus(dto.getStatus());
            if (dto.getStatus() == VorfallStatus.GELOEST || dto.getStatus() == VorfallStatus.GESCHLOSSEN) {
                incident.setGeloestAm(LocalDateTime.now());
            }
        }
        if (dto.getZugewiesenAn() != null) incident.setZugewiesenAn(dto.getZugewiesenAn());
        if (dto.getMassnahmen() != null) incident.setMassnahmen(dto.getMassnahmen());
        if (dto.getLoesung() != null) incident.setLoesung(dto.getLoesung());
        if (dto.getBsiGemeldet() != null) incident.setBsiGemeldet(dto.getBsiGemeldet());
        return incidentRepository.save(incident);
    }

    public List<SecurityIncident> getDsgvoPending() {
        return incidentRepository.findByDsgvoMeldepflichtigTrueAndBsiGemeldetFalse();
    }
}

// ─── Controller ────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/vorfaelle")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER','VERWALTUNG')")
    public ResponseEntity<List<SecurityIncident>> getAll(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(incidentService.findAll(status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER','VERWALTUNG')")
    public ResponseEntity<SecurityIncident> getById(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.findById(id));
    }

    @GetMapping("/dsgvo-ausstehend")
    @PreAuthorize("hasAnyRole('ADMIN','VERWALTUNG')")
    public ResponseEntity<List<SecurityIncident>> getDsgvoPending() {
        return ResponseEntity.ok(incidentService.getDsgvoPending());
    }

    @PostMapping
    public ResponseEntity<SecurityIncident> create(
            @jakarta.validation.Valid @RequestBody IncidentDto dto) {
        return ResponseEntity.status(201).body(incidentService.create(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VERWALTUNG')")
    public ResponseEntity<SecurityIncident> update(@PathVariable Long id,
                                                   @RequestBody IncidentUpdateDto dto) {
        return ResponseEntity.ok(incidentService.update(id, dto));
    }
}
