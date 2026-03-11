package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findByDoctor(Doctor doctor);

    List<Examination> findByPatient(Patient patient);
}
