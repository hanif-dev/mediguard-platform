package de.mediguard.patient;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public List<Patient> findAll() {
        return patientRepository.findByIstAktivTrue();
    }

    public Patient findById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Patient nicht gefunden: " + id));
    }

    public Patient create(PatientDto dto) {
        String nr = generatePatientenNr();
        Patient patient = Patient.builder()
                .patientenNr(nr)
                .nachname(dto.getNachname())
                .vorname(dto.getVorname())
                .geburtsdatum(dto.getGeburtsdatum())
                .geschlecht(dto.getGeschlecht())
                .adresse(dto.getAdresse())
                .plz(dto.getPlz())
                .stadt(dto.getStadt())
                .telefon(dto.getTelefon())
                .email(dto.getEmail())
                .versicherungsart(dto.getVersicherungsart())
                .versicherungsnummer(dto.getVersicherungsnummer())
                .krankenkasse(dto.getKrankenkasse())
                .allergien(dto.getAllergien())
                .vorerkrankungen(dto.getVorerkrankungen())
                .behandelnderArzt(dto.getBehandelnderArzt())
                .build();
        return patientRepository.save(patient);
    }

    public Patient update(Long id, PatientDto dto) {
        Patient patient = findById(id);
        patient.setNachname(dto.getNachname());
        patient.setVorname(dto.getVorname());
        patient.setGeburtsdatum(dto.getGeburtsdatum());
        patient.setGeschlecht(dto.getGeschlecht());
        patient.setAdresse(dto.getAdresse());
        patient.setPlz(dto.getPlz());
        patient.setStadt(dto.getStadt());
        patient.setTelefon(dto.getTelefon());
        patient.setEmail(dto.getEmail());
        patient.setVersicherungsart(dto.getVersicherungsart());
        patient.setVersicherungsnummer(dto.getVersicherungsnummer());
        patient.setKrankenkasse(dto.getKrankenkasse());
        patient.setAllergien(dto.getAllergien());
        patient.setVorerkrankungen(dto.getVorerkrankungen());
        patient.setBehandelnderArzt(dto.getBehandelnderArzt());
        return patientRepository.save(patient);
    }

    public void deactivate(Long id) {
        Patient patient = findById(id);
        patient.setIstAktiv(false);
        patientRepository.save(patient);
    }

    public List<Patient> search(String query) {
        return patientRepository
                .findByNachnameContainingIgnoreCaseOrVornameContainingIgnoreCase(query, query);
    }

    private String generatePatientenNr() {
        int year = java.time.LocalDate.now().getYear();
        long count = patientRepository.count() + 1;
        return String.format("P-%d-%04d", year, count);
    }
}
