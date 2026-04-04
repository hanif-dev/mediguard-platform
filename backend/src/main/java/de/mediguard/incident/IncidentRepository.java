package de.mediguard.incident;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IncidentRepository extends JpaRepository<SecurityIncident, Long> {
    List<SecurityIncident> findAllByOrderByGemeldetAmDesc();
    List<SecurityIncident> findByStatusOrderByGemeldetAmDesc(VorfallStatus status);
    long countByStatus(VorfallStatus status);
    long countBySchweregrad(Schweregrad schweregrad);
    List<SecurityIncident> findByDsgvoMeldepflichtigTrueAndBsiGemeldetFalse();
}