package nbu.informatics.medicalRecordSystem.model.dto.healthInsurance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HealthInsuranceRequestDTO {
    @NotNull(message = "Годината е задължителна")
    @Min(value = 2000, message = "Годината трябва да е след 2000")
    private Integer year;

    @NotNull(message = "Месецът е задължителен")
    @Min(value = 1, message = "Невалиден месец")
    @Max(value = 12, message = "Невалиден месец")
    private Integer month;

    private boolean paid;
}
