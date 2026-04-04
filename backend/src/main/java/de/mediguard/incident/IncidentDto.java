package de.mediguard.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentDto {
    @NotBlank private String titel;
    @NotBlank private String beschreibung;
    @NotNull private VorfallKategorie kategorie;
    @NotNull private Schweregrad schweregrad;
    private String betroffeneSysteme;
    private Integer betroffenePatienten;
    private Boolean dsgvoMeldepflichtig;
    @NotBlank private String melderName;
    private String melderAbteilung;
}