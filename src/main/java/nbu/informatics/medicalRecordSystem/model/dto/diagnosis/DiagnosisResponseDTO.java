package nbu.informatics.medicalRecordSystem.model.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisResponseDTO {
    private Long id;

    private String name;
}
