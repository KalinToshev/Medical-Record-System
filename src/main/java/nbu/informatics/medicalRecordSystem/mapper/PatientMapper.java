package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    @Mapping(source = "gp.id", target = "gpId")
    @Mapping(source = "gp.name", target = "gpName")
    @Mapping(source = "user.username", target = "username")
    PatientResponseDTO toDto(Patient patient);
}
