package nbu.informatics.medicalRecordSystem.model.dto.examination;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ExaminationCreateRequestDTO {
    @NotNull(message = "Пациентът е задължителен")
    private Long patientId;

    @NotNull(message = "Диагнозата е задължителна")
    private Long diagnosisId;

    private String treatment;

    @NotNull(message = "Цената е задължителна")
    @DecimalMin(value = "0.0", inclusive = false, message = "Цената трябва да е положително число")
    private BigDecimal price;

    private boolean issueSickLeave;

    private LocalDate sickLeaveStartDate;

    @Min(value = 1, message = "Броят дни трябва да е поне 1")
    private Integer sickLeaveDays;
}
