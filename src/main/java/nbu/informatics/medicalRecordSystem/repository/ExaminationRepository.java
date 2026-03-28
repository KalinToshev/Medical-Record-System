package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findByDoctor(Doctor doctor);

    List<Examination> findByPatient(Patient patient);

    @Query("SELECT e FROM Examination e WHERE e.diagnosis.id = :diagnosisId")
    List<Examination> findByDiagnosisId(@Param("diagnosisId") Long diagnosisId);

    List<Examination> findByDoctorId(Long doctorId);

    List<Examination> findByDoctorIdAndDateTimeBetween(Long doctorId, LocalDateTime from, LocalDateTime to);

    List<Examination> findByDoctorIdAndDateTimeAfter(Long doctorId, LocalDateTime from);

    List<Examination> findByDoctorIdAndDateTimeBefore(Long doctorId, LocalDateTime to);

    List<Examination> findByDateTimeBetween(LocalDateTime from, LocalDateTime to);

    @Query("SELECT SUM(e.price) FROM Examination e WHERE e.paidBy = 'PATIENT'")
    BigDecimal totalPaidByPatients();

    @Query("SELECT e.doctor.name, SUM(e.price) FROM Examination e " +
            "WHERE e.paidBy = 'PATIENT' GROUP BY e.doctor.name")
    List<Object[]> totalPaidByPatientsPerDoctor();

    @Query("SELECT e.doctor.name, COUNT(e) FROM Examination e GROUP BY e.doctor.name")
    List<Object[]> countPerDoctor();

    @Query("SELECT e.diagnosis.name, COUNT(e) FROM Examination e " +
            "GROUP BY e.diagnosis.name ORDER BY COUNT(e) DESC")
    List<Object[]> diagnosisFrequency();

    @Query("SELECT e FROM Examination e WHERE e.patient.id = :patientId")
    List<Examination> findByPatientId(@Param("patientId") Long patientId);

    long countByDiagnosisId(Long diagnosisId);

    long countByPatientId(Long patientId);
}
