package de.mediguard.incident;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "sicherheitsvorfaelle")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SecurityIncident {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titel;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String beschreibung;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VorfallKategorie kategorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Schweregrad schweregrad;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VorfallStatus status = VorfallStatus.OFFEN;

    @Column(name = "betroffene_systeme", length = 500)
    private String betroffeneSysteme;

    @Column(name = "betroffene_patienten_anzahl")
    private Integer betroffenePatienten;

    @Column(name = "dsgvo_meldepflichtig")
    @Builder.Default
    private Boolean dsgvoMeldepflichtig = false;

    @Column(name = "bsi_gemeldet")
    @Builder.Default
    private Boolean bsiGemeldet = false;

    @Column(name = "melder_name", nullable = false, length = 100)
    private String melderName;

    @Column(name = "melder_abteilung", length = 100)
    private String melderAbteilung;

    @Column(name = "zugewiesen_an", length = 100)
    private String zugewiesenAn;

    @Column(columnDefinition = "TEXT")
    private String massnahmen;

    @Column(columnDefinition = "TEXT")
    private String loesung;

    @CreatedDate
    @Column(name = "gemeldet_am", updatable = false)
    private LocalDateTime gemeldetAm;

    @Column(name = "geloest_am")
    private LocalDateTime geloestAm;
}