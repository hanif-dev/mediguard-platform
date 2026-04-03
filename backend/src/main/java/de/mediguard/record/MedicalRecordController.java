package de.mediguard.record;

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
enum EintragsTyp {
    ANAMNESE,       // Medical history
    DIAGNOSE,       // Diagnosis
    BEHANDLUNG,     // Treatment
    MEDIKATION,     // Medication
    LABOR,          // Lab results
    BILDGEBUNG,     // Imaging (X-ray, MRI, etc.)
    ENTLASSUNG,     // Discharge
    NOTIZ           // Note
}

// ─── Entity ────────────────────────────────────────────
@Entity
@Table(name = "krankenakten")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class MedicalRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patienten_id", nullable = false)
    private Long patientenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "eintrags_typ", nullable = false)
    private EintragsTyp eintragsTyp;

    @Column(nullable = false, length = 200)
    private String titel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inhalt; // Content

    @Column(name = "icd10_code", length = 20)
    private String icd10Code; // International Classification of Diseases

    @Column(name = "behandelnder_arzt", nullable = false, length = 100)
    private String behandelnderArzt;

    @Column(length = 100)
    private String abteilung;

    @Column(name = "vertraulich")
    @Builder.Default
    private Boolean vertraulich = false; // Confidential

    // §630f BGB: Records must NOT be deletable — only soft flags
    @Column(name = "geloescht")
    @Builder.Default
    private Boolean geloescht = false;

    @CreatedDate
    @Column(name = "erstellt_am", updatable = false)
    private LocalDateTime erstelltAm;

    // §630f BGB: Immutable audit - store who created this entry
    @Column(name = "erstellt_von", updatable = false, length = 100)
    private String erstelltVon;
}

// ─── DTO ───────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class MedicalRecordDto {
    @NotNull private Long patientenId;
    @NotNull private EintragsTyp eintragsTyp;
    @NotBlank private String titel;
    @NotBlank private String inhalt;
    private String icd10Code;
    @NotBlank private String behandelnderArzt;
    private String abteilung;
    private Boolean vertraulich;
}

// ─── Repository ────────────────────────────────────────
interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientenIdAndGeloeschtFalseOrderByErstelltAmDesc(Long patientenId);
    List<MedicalRecord> findByGeloeschtFalseOrderByErstelltAmDesc();
    long countByGeloeschtFalse();
}

// ─── Service ───────────────────────────────────────────
@Service
@RequiredArgsConstructor
class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;

    public List<MedicalRecord> findByPatient(Long patientenId) {
        return recordRepository.findByPatientenIdAndGeloeschtFalseOrderByErstelltAmDesc(patientenId);
    }

    public MedicalRecord findById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Akte nicht gefunden: " + id));
    }

    public MedicalRecord create(MedicalRecordDto dto, String erstelltVon) {
        MedicalRecord record = MedicalRecord.builder()
                .patientenId(dto.getPatientenId())
                .eintragsTyp(dto.getEintragsTyp())
                .titel(dto.getTitel())
                .inhalt(dto.getInhalt())
                .icd10Code(dto.getIcd10Code())
                .behandelnderArzt(dto.getBehandelnderArzt())
                .abteilung(dto.getAbteilung())
                .vertraulich(dto.getVertraulich() != null ? dto.getVertraulich() : false)
                .erstelltVon(erstelltVon)
                .build();
        return recordRepository.save(record);
    }

    // §630f BGB: No hard delete — only mark as deleted
    public void softDelete(Long id) {
        MedicalRecord record = findById(id);
        record.setGeloescht(true);
        recordRepository.save(record);
    }
}

// ─── Controller ────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/krankenakten")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService recordService;

    @GetMapping("/patient/{patientenId}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER')")
    public ResponseEntity<List<MedicalRecord>> getByPatient(@PathVariable Long patientenId) {
        return ResponseEntity.ok(recordService.findByPatient(patientenId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER')")
    public ResponseEntity<MedicalRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(recordService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ARZT')")
    public ResponseEntity<MedicalRecord> create(
            @jakarta.validation.Valid @RequestBody MedicalRecordDto dto,
            org.springframework.security.core.Authentication auth) {
        return ResponseEntity.status(201).body(recordService.create(dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        recordService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
