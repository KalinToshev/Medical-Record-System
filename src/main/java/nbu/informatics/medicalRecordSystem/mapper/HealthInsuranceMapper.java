package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.HealthInsurance;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthInsuranceMapper {
    HealthInsuranceResponseDTO toDto(HealthInsurance insurance);
}
