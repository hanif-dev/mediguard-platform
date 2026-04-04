package de.mediguard.record;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    List<MedicalRecord> findByPatientenIdAndGeloeschtFalseOrderByErstelltAmDesc(Long patientenId);
    List<MedicalRecord> findByGeloeschtFalseOrderByErstelltAmDesc();
    long countByGeloeschtFalse();
}
