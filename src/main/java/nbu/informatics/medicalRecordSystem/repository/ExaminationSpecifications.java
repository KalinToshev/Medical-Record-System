package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class ExaminationSpecifications {

    private ExaminationSpecifications() {
    }

    public static Specification<Examination> hasDoctor(Long doctorId) {
        return (root, query, cb) -> cb.equal(root.get("doctor").get("id"), doctorId);
    }

    public static Specification<Examination> dateAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("dateTime"), from);
    }

    public static Specification<Examination> dateBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("dateTime"), to);
    }
}