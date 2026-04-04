package de.mediguard.patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByIstAktivTrue();
    List<Patient> findByNachnameContainingIgnoreCaseOrVornameContainingIgnoreCase(
            String nachname, String vorname);
    boolean existsByPatientenNr(String patientenNr);

    @Query("SELECT COUNT(p) FROM Patient p WHERE p.istAktiv = true")
    long countActive();
}
