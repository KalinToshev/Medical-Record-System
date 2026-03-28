package nbu.informatics.medicalRecordSystem.model.dto.sickLeave;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SickLeaveResponseDTO {
    private Long id;
    private LocalDate startDate;
    private Integer days;
}
