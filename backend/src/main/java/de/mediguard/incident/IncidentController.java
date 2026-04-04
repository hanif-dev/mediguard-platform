package de.mediguard.incident;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @Valid @RequestBody IncidentDto dto) {
        return ResponseEntity.status(201).body(incidentService.create(dto));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VERWALTUNG')")
    public ResponseEntity<SecurityIncident> update(
            @PathVariable Long id,
            @RequestBody IncidentUpdateDto dto) {
        return ResponseEntity.ok(incidentService.update(id, dto));
    }
}