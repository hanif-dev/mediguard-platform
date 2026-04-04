package de.mediguard.patient;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseEntity<Patient> create(@Valid @RequestBody PatientDto dto) {
        return ResponseEntity.status(201).body(patientService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','VERWALTUNG')")
    public ResponseEntity<Patient> update(@PathVariable Long id,
                                          @Valid @RequestBody PatientDto dto) {
        return ResponseEntity.ok(patientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        patientService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
