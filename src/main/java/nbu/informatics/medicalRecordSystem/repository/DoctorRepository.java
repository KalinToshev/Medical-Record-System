package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findByIsGpTrue();

    Optional<Doctor> findByUser(User user);

    long countBySpecialityId(Long specialityId);
}
