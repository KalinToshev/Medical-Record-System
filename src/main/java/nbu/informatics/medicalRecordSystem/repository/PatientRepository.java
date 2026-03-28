package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    long countByGpId(Long gpId);

    Optional<Patient> findByUser(User user);

    @Query("SELECT p FROM Patient p WHERE p.gp.id = :gpId")
    List<Patient> findByGpId(@Param("gpId") Long gpId);

    @Query("SELECT p.gp.name, COUNT(p) FROM Patient p GROUP BY p.gp.name")
    List<Object[]> countPerGp();
}
