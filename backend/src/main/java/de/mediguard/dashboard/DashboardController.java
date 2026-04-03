package de.mediguard.dashboard;

import de.mediguard.incident.IncidentRepository;
import de.mediguard.incident.Schweregrad;
import de.mediguard.incident.VorfallStatus;
import de.mediguard.patient.PatientRepository;
import de.mediguard.record.MedicalRecordRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PatientRepository patientRepository;
    private final MedicalRecordRepository recordRepository;
    private final IncidentRepository incidentRepository;

    @Data @Builder
    static class DashboardStats {
        private long totalPatienten;
        private long totalAkten;
        private long offeneVorfaelle;
        private long kritischeVorfaelle;
        private long dsgvoAusstehend;
        private long gesamtVorfaelle;
        private long inBearbeitungVorfaelle;
    }

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','ARZT','VERWALTUNG')")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = DashboardStats.builder()
                .totalPatienten(patientRepository.countActive())
                .totalAkten(recordRepository.countByGeloeschtFalse())
                .offeneVorfaelle(incidentRepository.countByStatus(VorfallStatus.OFFEN))
                .inBearbeitungVorfaelle(incidentRepository.countByStatus(VorfallStatus.IN_BEARBEITUNG))
                .kritischeVorfaelle(incidentRepository.countBySchweregrad(Schweregrad.KRITISCH))
                .dsgvoAusstehend(incidentRepository.findByDsgvoMeldepflichtigTrueAndBsiGemeldetFalse().size())
                .gesamtVorfaelle(incidentRepository.count())
                .build();
        return ResponseEntity.ok(stats);
    }
}
