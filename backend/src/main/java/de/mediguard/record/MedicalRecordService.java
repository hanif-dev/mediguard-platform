package de.mediguard.record;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepository;

    public List<MedicalRecord> findByPatient(Long patientenId) {
        return recordRepository
                .findByPatientenIdAndGeloeschtFalseOrderByErstelltAmDesc(patientenId);
    }

    public MedicalRecord findById(Long id) {
        return recordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Akte nicht gefunden: " + id));
    }

    public MedicalRecord create(MedicalRecordDto dto, String erstelltVon) {
        MedicalRecord record = MedicalRecord.builder()
                .patientenId(dto.getPatientenId())
                .eintragsTyp(dto.getEintragsTyp())
                .titel(dto.getTitel())
                .inhalt(dto.getInhalt())
                .icd10Code(dto.getIcd10Code())
                .behandelnderArzt(dto.getBehandelnderArzt())
                .abteilung(dto.getAbteilung())
                .vertraulich(dto.getVertraulich() != null ? dto.getVertraulich() : false)
                .erstelltVon(erstelltVon)
                .build();
        return recordRepository.save(record);
    }

    // §630f BGB: no hard delete
    public void softDelete(Long id) {
        MedicalRecord record = findById(id);
        record.setGeloescht(true);
        recordRepository.save(record);
    }
}
