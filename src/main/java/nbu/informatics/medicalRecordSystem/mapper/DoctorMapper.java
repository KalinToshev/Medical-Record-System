package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorMapper {
    @Mapping(source = "speciality.id", target = "specialityId")
    @Mapping(source = "speciality.name", target = "specialityName")
    @Mapping(source = "user.username", target = "username")
    DoctorResponseDTO toDto(Doctor doctor);
}
