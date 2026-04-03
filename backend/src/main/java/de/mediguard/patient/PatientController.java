package de.mediguard.patient;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// ─── Enums ─────────────────────────────────────────────
enum Geschlecht { MAENNLICH, WEIBLICH, DIVERS }
enum Versicherungsart { GESETZLICH, PRIVAT, BERUFSGENOSSENSCHAFT }

// ─── Entity ────────────────────────────────────────────
@Entity
@Table(name = "patienten")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
class Patient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patienten_nr", unique = true, nullable = false, length = 20)
    private String patientenNr; // Patient number: P-2024-0001

    @Column(nullable = false, length = 100)
    private String nachname; // Last name

    @Column(nullable = false, length = 100)
    private String vorname;  // First name

    @Column(name = "geburtsdatum", nullable = false)
    private LocalDate geburtsdatum; // Date of birth

    @Enumerated(EnumType.STRING)
    private Geschlecht geschlecht;

    @Column(length = 200)
    private String adresse; // Address

    @Column(length = 10)
    private String plz;     // Postal code

    @Column(length = 100)
    private String stadt;   // City

    @Column(length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "versicherungsart")
    private Versicherungsart versicherungsart;

    @Column(name = "versicherungsnummer", length = 30)
    private String versicherungsnummer; // Insurance number

    @Column(name = "krankenkasse", length = 100)
    private String krankenkasse; // Health insurance company

    @Column(columnDefinition = "TEXT")
    private String allergien; // Allergies

    @Column(columnDefinition = "TEXT")
    private String vorerkrankungen; // Pre-existing conditions

    @Column(name = "behandelnder_arzt", length = 100)
    private String behandelnderArzt; // Attending physician

    @Builder.Default
    @Column(name = "ist_aktiv")
    private Boolean istAktiv = true;

    @CreatedDate @Column(name = "angelegt_am", updatable = false)
    private LocalDateTime angelegtAm;

    @LastModifiedDate @Column(name = "geaendert_am")
    private LocalDateTime geaendertAm;
}

// ─── DTO ───────────────────────────────────────────────
@Data @Builder @NoArgsConstructor @AllArgsConstructor
class PatientDto {
    private Long id;
    private String patientenNr;
    @NotBlank private String nachname;
    @NotBlank private String vorname;
    @NotNull private LocalDate geburtsdatum;
    private Geschlecht geschlecht;
    private String adresse;
    private String plz;
    private String stadt;
    private String telefon;
    private String email;
    private Versicherungsart versicherungsart;
    private String versicherungsnummer;
    private String krankenkasse;
    private String allergien;
    private String vorerkrankungen;
    private String behandelnderArzt;
    private Boolean istAktiv;
    private LocalDateTime angelegtAm;
}

// ─── Repository ────────────────────────────────────────
interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByIstAktivTrue();
    List<Patient> findByNachnameContainingIgnoreCaseOrVornameContainingIgnoreCase(String nachname, String vorname);
    boolean existsByPatientenNr(String patientenNr);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.istAktiv = true")
    long countActive();
}

// ─── Service ───────────────────────────────────────────
@Service
@RequiredArgsConstructor
class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> findAll() {
        return patientRepository.findByIstAktivTrue();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Patient nicht gefunden: " + id));
    }

    public Patient create(PatientDto dto) {
        String nr = generatePatientenNr();
        Patient patient = Patient.builder()
                .patientenNr(nr)
                .nachname(dto.getNachname())
                .vorname(dto.getVorname())
                .geburtsdatum(dto.getGeburtsdatum())
                .geschlecht(dto.getGeschlecht())
                .adresse(dto.getAdresse())
                .plz(dto.getPlz())
                .stadt(dto.getStadt())
                .telefon(dto.getTelefon())
                .email(dto.getEmail())
                .versicherungsart(dto.getVersicherungsart())
                .versicherungsnummer(dto.getVersicherungsnummer())
                .krankenkasse(dto.getKrankenkasse())
                .allergien(dto.getAllergien())
                .vorerkrankungen(dto.getVorerkrankungen())
                .behandelnderArzt(dto.getBehandelnderArzt())
                .build();
        return patientRepository.save(patient);
    }

    public Patient update(Long id, PatientDto dto) {
        Patient patient = findById(id);
        patient.setNachname(dto.getNachname());
        patient.setVorname(dto.getVorname());
        patient.setGeburtsdatum(dto.getGeburtsdatum());
        patient.setGeschlecht(dto.getGeschlecht());
        patient.setAdresse(dto.getAdresse());
        patient.setPlz(dto.getPlz());
        patient.setStadt(dto.getStadt());
        patient.setTelefon(dto.getTelefon());
        patient.setEmail(dto.getEmail());
        patient.setVersicherungsart(dto.getVersicherungsart());
        patient.setVersicherungsnummer(dto.getVersicherungsnummer());
        patient.setKrankenkasse(dto.getKrankenkasse());
        patient.setAllergien(dto.getAllergien());
        patient.setVorerkrankungen(dto.getVorerkrankungen());
        patient.setBehandelnderArzt(dto.getBehandelnderArzt());
        return patientRepository.save(patient);
    }

    public void deactivate(Long id) {
        Patient patient = findById(id);
        patient.setIstAktiv(false);
        patientRepository.save(patient);
    }

    public List<Patient> search(String query) {
        return patientRepository.findByNachnameContainingIgnoreCaseOrVornameContainingIgnoreCase(query, query);
    }

    private String generatePatientenNr() {
        int year = java.time.LocalDate.now().getYear();
        long count = patientRepository.count() + 1;
        return String.format("P-%d-%04d", year, count);
    }
}

// ─── Controller ────────────────────────────────────────
@RestController
@RequestMapping("/api/v1/patienten")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER','VERWALTUNG')")
    public ResponseEntity<List<Patient>> getAll() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER','VERWALTUNG')")
    public ResponseEntity<Patient> getById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.findById(id));
    }

    @GetMapping("/suche")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','PFLEGER','VERWALTUNG')")
    public ResponseEntity<List<Patient>> search(@RequestParam String q) {
        return ResponseEntity.ok(patientService.search(q));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','VERWALTUNG')")
    public ResponseEntity<Patient> create(@jakarta.validation.Valid @RequestBody PatientDto dto) {
        return ResponseEntity.status(201).body(patientService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','VERWALTUNG')")
    public ResponseEntity<Patient> update(@PathVariable Long id,
                                          @jakarta.validation.Valid @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        patientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
