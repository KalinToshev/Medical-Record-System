package nbu.informatics.medicalRecordSystem.model.dto.healthInsurance;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HealthInsuranceResponseDTO {
    private Long id;
    private Integer year;
    private Integer month;
    private boolean paid;
}
