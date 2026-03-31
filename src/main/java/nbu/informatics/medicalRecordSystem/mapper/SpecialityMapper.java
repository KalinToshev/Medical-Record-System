package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpecialityMapper {
    SpecialityResponseDTO toDto(Speciality speciality);
}
