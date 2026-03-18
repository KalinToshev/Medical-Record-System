package nbu.informatics.medicalRecordSystem.model.dto.speciality;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpecialityRequestDTO {
    @NotBlank(message = "Името на специалността е задължително")
    private String name;
}
