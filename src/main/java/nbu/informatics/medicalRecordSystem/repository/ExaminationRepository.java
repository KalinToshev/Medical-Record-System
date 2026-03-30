package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.dto.report.NameAmountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long>, JpaSpecificationExecutor<Examination> {
    List<Examination> findByDoctor(Doctor doctor);

    List<Examination> findByPatient(Patient patient);

    @Query("SELECT e FROM Examination e WHERE e.diagnosis.id = :diagnosisId")
    List<Examination> findByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    @Query("SELECT SUM(e.price) FROM Examination e WHERE e.paidBy = 'PATIENT'")
    BigDecimal totalPaidByPatients();

    @Query("SELECT new nbu.informatics.medicalRecordSystem.model.dto.report.NameAmountProjection(e.doctor.name, SUM(e.price)) " +
            "FROM Examination e WHERE e.paidBy = 'PATIENT' GROUP BY e.doctor.name")
    List<NameAmountProjection> totalPaidByPatientsPerDoctor();

    @Query("SELECT new nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection(e.doctor.name, COUNT(e)) " +
            "FROM Examination e GROUP BY e.doctor.name")
    List<NameCountProjection> countPerDoctor();

    @Query("SELECT new nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection(e.diagnosis.name, COUNT(e)) " +
            "FROM Examination e GROUP BY e.diagnosis.name ORDER BY COUNT(e) DESC")
    List<NameCountProjection> diagnosisFrequency();

    @Query("SELECT e FROM Examination e WHERE e.patient.id = :patientId")
    List<Examination> findByPatientId(@Param("patientId") Long patientId);

    long countByDiagnosisId(Long diagnosisId);

    long countByPatientId(Long patientId);
}
