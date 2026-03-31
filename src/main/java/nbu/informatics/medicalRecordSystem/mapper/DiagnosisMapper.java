package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DiagnosisMapper {
    DiagnosisResponseDTO toDto(Diagnosis diagnosis);
}
