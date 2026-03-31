package nbu.informatics.medicalRecordSystem.mapper;

import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExaminationMapper {
    @Mapping(source = "doctor.name", target = "doctorName")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "diagnosis.name", target = "diagnosisName")
    @Mapping(target = "hasSickLeave", expression = "java(examination.getSickLeave() != null)")
    ExaminationResponseDTO toDto(Examination examination);
}
