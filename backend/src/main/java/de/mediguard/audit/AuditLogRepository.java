package de.mediguard.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByZeitstempelDesc();
    List<AuditLog> findByBenutzerNameOrderByZeitstempelDesc(String benutzerName);
    long countByWarErfolgreichFalseAndZeitstempelAfter(LocalDateTime since);
}
