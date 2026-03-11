package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
}
