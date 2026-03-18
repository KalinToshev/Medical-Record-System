package nbu.informatics.medicalRecordSystem.model.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private Long id;
    private String name;
    private boolean isGp;
    private Long specialityId;
    private String specialityName;
    private String username;
}
