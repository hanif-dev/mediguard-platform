package de.mediguard.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private String patientenNr;
    @NotBlank private String nachname;
    @NotBlank private String vorname;
    @NotNull  private LocalDate geburtsdatum;
    private Geschlecht geschlecht;
    private String adresse;
    private String plz;
    private String stadt;
    private String telefon;
    private String email;
    private Versicherungsart versicherungsart;
    private String versicherungsnummer;
    private String krankenkasse;
    private String allergien;
    private String vorerkrankungen;
    private String behandelnderArzt;
    private Boolean istAktiv;
    private LocalDateTime angelegtAm;
}
