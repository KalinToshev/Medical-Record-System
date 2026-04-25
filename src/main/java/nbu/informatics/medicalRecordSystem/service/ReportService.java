package nbu.informatics.medicalRecordSystem.service;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.mapper.ExaminationMapper;
import nbu.informatics.medicalRecordSystem.mapper.PatientMapper;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.report.MonthCountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameAmountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationSpecifications;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SickLeaveRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final SickLeaveRepository sickLeaveRepository;
    private final ExaminationMapper examinationMapper;
    private final PatientMapper patientMapper;

    public List<PatientResponseDTO> findByDiagnosis(Long diagnosisId) {
        return examinationRepository.findByDiagnosisId(diagnosisId)
                .stream()
                .map(e -> patientMapper.toDto(e.getPatient()))
                .collect(Collectors.toMap(
                        PatientResponseDTO::getId,
                        p -> p,
                        (existing, duplicate) -> existing
                ))
                .values()
                .stream()
                .toList();
    }

    public List<PatientResponseDTO> findByGp(Long gpId) {
        return patientRepository.findByGpId(gpId)
                .stream().map(patientMapper::toDto).toList();
    }

    public List<NameCountProjection> countPatientsPerGp() {
        return patientRepository.countPerGp();
    }

    public List<ExaminationResponseDTO> findPatientHistory(Long patientId) {
        return examinationRepository.findByPatientId(patientId)
                .stream().map(examinationMapper::toDto).toList();
    }

    public List<ExaminationResponseDTO> findByDoctorAndPeriod(
            Long doctorId, LocalDateTime from, LocalDateTime to) {

        Specification<Examination> spec = Specification.unrestricted();

        if (doctorId != null) {
            spec = spec.and(ExaminationSpecifications.hasDoctor(doctorId));
        }
        if (from != null) {
            spec = spec.and(ExaminationSpecifications.dateAfter(from));
        }
        if (to != null) {
            spec = spec.and(ExaminationSpecifications.dateBefore(to));
        }

        return examinationRepository.findAll(spec)
                .stream()
                .map(examinationMapper::toDto)
                .toList();
    }

    public List<NameCountProjection> countExaminationsPerDoctor() {
        return examinationRepository.countPerDoctor();
    }

    public BigDecimal totalPaidByPatients() {
        BigDecimal result = examinationRepository.totalPaidByPatients();
        return result != null ? result : BigDecimal.ZERO;
    }

    public List<NameAmountProjection> totalPaidByPatientsPerDoctor() {
        return examinationRepository.totalPaidByPatientsPerDoctor();
    }

    public String mostCommonDiagnosis() {
        List<NameCountProjection> results = examinationRepository.diagnosisFrequency();
        return results.isEmpty() ? "Няма данни" : results.getFirst().name();
    }

    public String monthWithMostSickLeaves() {
        List<MonthCountProjection> results = sickLeaveRepository.countPerMonth();
        if (results.isEmpty()) return "Няма данни";
        MonthCountProjection top = results.getFirst();
        return top.month() + "/" + top.year();
    }

    public List<NameCountProjection> doctorsWithMostSickLeaves() {
        List<NameCountProjection> all = sickLeaveRepository.countPerDoctor();
        if (all.isEmpty()) return List.of();
        Long max = all.getFirst().count();
        return all.stream().filter(r -> r.count().equals(max)).toList();
    }
}
