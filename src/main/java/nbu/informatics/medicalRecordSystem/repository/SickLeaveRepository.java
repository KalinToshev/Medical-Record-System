package nbu.informatics.medicalRecordSystem.repository;

import nbu.informatics.medicalRecordSystem.model.dto.report.MonthCountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection;
import nbu.informatics.medicalRecordSystem.model.entity.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
    @Query("SELECT new nbu.informatics.medicalRecordSystem.model.dto.report.MonthCountProjection(" +
            "CAST(EXTRACT(YEAR FROM s.startDate) AS INTEGER), " +
            "CAST(EXTRACT(MONTH FROM s.startDate) AS INTEGER), COUNT(s)) " +
            "FROM SickLeave s GROUP BY EXTRACT(YEAR FROM s.startDate), " +
            "EXTRACT(MONTH FROM s.startDate) ORDER BY COUNT(s) DESC")
    List<MonthCountProjection> countPerMonth();

    @Query("SELECT new nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection(e.doctor.name, COUNT(s)) " +
            "FROM SickLeave s JOIN s.examination e GROUP BY e.doctor.name ORDER BY COUNT(s) DESC")
    List<NameCountProjection> countPerDoctor();
}
