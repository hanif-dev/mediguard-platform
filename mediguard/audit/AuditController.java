package de.mediguard.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
@RequiredArgsConstructor
public class AuditController {

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
