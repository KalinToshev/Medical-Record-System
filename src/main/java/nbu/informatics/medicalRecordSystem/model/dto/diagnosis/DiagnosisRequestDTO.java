package nbu.informatics.medicalRecordSystem.model.dto.diagnosis;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DiagnosisRequestDTO {
    @NotBlank(message = "Името на диагнозата е задължително")
    private String name;
}
