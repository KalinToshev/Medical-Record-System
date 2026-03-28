package nbu.informatics.medicalRecordSystem.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nbu.informatics.medicalRecordSystem.model.role.Role;

@Getter
@Setter
@NoArgsConstructor
public class AssignRoleRequestDTO {
    @NotNull
    private Long userId;

    @NotNull(message = "Ролята е задължителна")
    private Role role;

    @NotBlank(message = "Името е задължително")
    private String name;

    @Size(min = 10, max = 10, message = "ЕГН трябва да е точно 10 символа")
    private String egn;

    private Long gpId;

    private Long specialityId;

    private boolean isGp;
}
