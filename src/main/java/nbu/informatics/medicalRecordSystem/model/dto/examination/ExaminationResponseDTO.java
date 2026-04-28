package nbu.informatics.medicalRecordSystem.model.dto.examination;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExaminationResponseDTO {
    private Long id;
    private Long patientId;
    private LocalDateTime dateTime;
    private String doctorName;
    private String patientName;
    private String diagnosisName;
    private String treatment;
    private BigDecimal price;
    private PaidBy paidBy;
    private boolean hasSickLeave;

    public String getPaidByDisplay() {
        if (paidBy == null) return "";
        return switch (paidBy) {
            case NHIF -> "НЗОК";
            case PATIENT -> "Пациент";
        };
    }
}
