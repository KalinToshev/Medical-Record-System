package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.HealthInsurance;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HealthInsuranceRepository extends JpaRepository<HealthInsurance, Long> {
    List<HealthInsurance> findByPatient(Patient patient);

    boolean existsByPatientAndYearAndMonthAndPaidTrue(Patient patient, int year, int month);

    boolean existsByPatientAndYearAndMonth(Patient patient, int year, int month);
}
