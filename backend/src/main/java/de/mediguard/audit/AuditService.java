package de.mediguard.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

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
