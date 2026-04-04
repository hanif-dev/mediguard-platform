package de.mediguard.record;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "krankenakten")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patienten_id", nullable = false)
    private Long patientenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "eintrags_typ", nullable = false)
    private EintragsTyp eintragsTyp;

    @Column(nullable = false, length = 200)
    private String titel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String inhalt;

    @Column(name = "icd10_code", length = 20)
    private String icd10Code;

    @Column(name = "behandelnder_arzt", nullable = false, length = 100)
    private String behandelnderArzt;

    @Column(length = 100)
    private String abteilung;

    @Builder.Default
    @Column(name = "vertraulich")
    private Boolean vertraulich = false;

    // §630f BGB: No hard delete allowed
    @Builder.Default
    @Column(name = "geloescht")
    private Boolean geloescht = false;

    @CreatedDate
    @Column(name = "erstellt_am", updatable = false)
    private LocalDateTime erstelltAm;

    // §630f BGB: immutable audit trail
    @Column(name = "erstellt_von", updatable = false, length = 100)
    private String erstelltVon;
}
