package de.mediguard.incident;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;

    public List<SecurityIncident> findAll(String status) {
        if (status != null && !status.isBlank()) {
            return incidentRepository.findByStatusOrderByGemeldetAmDesc(
                    VorfallStatus.valueOf(status.toUpperCase()));
        }
        return incidentRepository.findAllByOrderByGemeldetAmDesc();
    }

    public SecurityIncident findById(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Vorfall nicht gefunden: " + id));
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