package de.mediguard.patient;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patienten")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patienten_nr", unique = true, nullable = false, length = 20)
    private String patientenNr;

    @Column(nullable = false, length = 100)
    private String nachname;

    @Column(nullable = false, length = 100)
    private String vorname;

    @Column(name = "geburtsdatum", nullable = false)
    private LocalDate geburtsdatum;

    @Enumerated(EnumType.STRING)
    private Geschlecht geschlecht;

    @Column(length = 200)
    private String adresse;

    @Column(length = 10)
    private String plz;

    @Column(length = 100)
    private String stadt;

    @Column(length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "versicherungsart")
    private Versicherungsart versicherungsart;

    @Column(name = "versicherungsnummer", length = 30)
    private String versicherungsnummer;

    @Column(name = "krankenkasse", length = 100)
    private String krankenkasse;

    @Column(columnDefinition = "TEXT")
    private String allergien;

    @Column(columnDefinition = "TEXT")
    private String vorerkrankungen;

    @Column(name = "behandelnder_arzt", length = 100)
    private String behandelnderArzt;

    @Builder.Default
    @Column(name = "ist_aktiv")
    private Boolean istAktiv = true;

    @CreatedDate
    @Column(name = "angelegt_am", updatable = false)
    private LocalDateTime angelegtAm;

    @LastModifiedDate
    @Column(name = "geaendert_am")
    private LocalDateTime geaendertAm;
}
