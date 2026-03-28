package nbu.informatics.medicalRecordSystem.model.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DoctorCreateRequestDTO {
    @NotBlank(message = "Името е задължително")
    private String name;

    private boolean isGp;

    @NotNull(message = "Специалността е задължителна")
    private Long specialityId;

    @NotNull(message = "Потребителят е задължителен")
    private Long userId;
}
