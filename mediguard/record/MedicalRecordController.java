package de.mediguard.record;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
            @Valid @RequestBody MedicalRecordDto dto,
            Authentication auth) {
        return ResponseEntity.status(201).body(recordService.create(dto, auth.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDelete(@PathVariable Long id) {
        recordService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
