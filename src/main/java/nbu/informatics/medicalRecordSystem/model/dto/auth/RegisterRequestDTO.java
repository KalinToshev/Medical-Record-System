package nbu.informatics.medicalRecordSystem.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotBlank(message = "Потребителското име е задължително")
    private String username;

    @NotBlank(message = "Паролата е задължителна")
    @Size(min = 6, message = "Паролата трябва да е поне 6 символа")
    private String password;
}
