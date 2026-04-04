package de.mediguard.incident;

import lombok.Data;

@Data
public class IncidentUpdateDto {
    private VorfallStatus status;
    private String zugewiesenAn;
    private String massnahmen;
    private String loesung;
    private Boolean bsiGemeldet;
}