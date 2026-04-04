package de.mediguard.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordDto {
    @NotNull  private Long patientenId;
    @NotNull  private EintragsTyp eintragsTyp;
    @NotBlank private String titel;
    @NotBlank private String inhalt;
    private String icd10Code;
    @NotBlank private String behandelnderArzt;
    private String abteilung;
    private Boolean vertraulich;
}
