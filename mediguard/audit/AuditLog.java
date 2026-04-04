package de.mediguard.audit;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_protokoll")
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "benutzer_name", nullable = false, length = 100)
    private String benutzerName;

    @Column(nullable = false, length = 50)
    private String aktion;

    @Column(name = "ressource_typ", length = 50)
    private String ressourceTyp;

    @Column(name = "ressource_id")
    private Long ressourceId;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_adresse", length = 45)
    private String ipAdresse;

    @Column(name = "war_erfolgreich")
    @Builder.Default
    private Boolean warErfolgreich = true;

    @CreatedDate
    @Column(name = "zeitstempel", updatable = false)
    private LocalDateTime zeitstempel;
}
