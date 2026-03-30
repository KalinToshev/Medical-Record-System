package nbu.informatics.medicalRecordSystem.model.dto.report;

public record MonthCountProjection(
        Integer year,
        Integer month,
        Long count
) {
}
