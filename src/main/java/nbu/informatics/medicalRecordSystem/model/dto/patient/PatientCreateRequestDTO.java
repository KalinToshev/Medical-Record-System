package nbu.informatics.medicalRecordSystem.model.dto.patient;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatientCreateRequestDTO {
    @NotBlank(message = "Името е задължително")
    private String name;

    @NotBlank(message = "ЕГН е задължително")
    @Size(min = 10, max = 10, message = "ЕГН трябва да е 10 символа")
    private String egn;

    @NotNull(message = "Личният лекар е задължителен")
    private Long gpId;

    @NotNull(message = "Потребителят е задължителен")
    private Long userId;
}
