package nbu.informatics.medicalRecordSystem.service;

import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SickLeaveRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final SickLeaveRepository sickLeaveRepository;

    public List<PatientResponseDTO> findByDiagnosis(Long diagnosisId) {
        return examinationRepository.findByDiagnosisId(diagnosisId)
                .stream()
                .map(e -> toPatientDTO(e.getPatient()))
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
                .stream().map(this::toPatientDTO).toList();
    }

    public List<Object[]> countPatientsPerGp() {
        return patientRepository.countPerGp();
    }

    public List<ExaminationResponseDTO> findPatientHistory(Long patientId) {
        return examinationRepository.findByPatientId(patientId)
                .stream().map(this::toExaminationDTO).toList();
    }

    public List<ExaminationResponseDTO> findByDoctorAndPeriod(
            Long doctorId, LocalDateTime from, LocalDateTime to) {

        List<Examination> results;

        if (doctorId != null && from != null && to != null) {
            results = examinationRepository.findByDoctorIdAndDateTimeBetween(doctorId, from, to);
        } else if (doctorId != null && from != null) {
            results = examinationRepository.findByDoctorIdAndDateTimeAfter(doctorId, from);
        } else if (doctorId != null && to != null) {
            results = examinationRepository.findByDoctorIdAndDateTimeBefore(doctorId, to);
        } else if (doctorId != null) {
            results = examinationRepository.findByDoctorId(doctorId);
        } else if (from != null && to != null) {
            results = examinationRepository.findByDateTimeBetween(from, to);
        } else {
            results = examinationRepository.findAll();
        }

        return results.stream().map(this::toExaminationDTO).toList();
    }

    public List<Object[]> countExaminationsPerDoctor() {
        return examinationRepository.countPerDoctor();
    }

    public BigDecimal totalPaidByPatients() {
        BigDecimal result = examinationRepository.totalPaidByPatients();
        return result != null ? result : BigDecimal.ZERO;
    }

    public List<Object[]> totalPaidByPatientsPerDoctor() {
        return examinationRepository.totalPaidByPatientsPerDoctor();
    }

    public String mostCommonDiagnosis() {
        List<Object[]> results = examinationRepository.diagnosisFrequency();
        return results.isEmpty() ? "Няма данни" : (String) results.getFirst()[0];
    }

    public String monthWithMostSickLeaves() {
        List<Object[]> results = sickLeaveRepository.countPerMonth();
        if (results.isEmpty()) return "Няма данни";
        Object[] top = results.getFirst();
        int year = ((Number) top[0]).intValue();
        int month = ((Number) top[1]).intValue();
        return month + "/" + year;
    }

    public List<Object[]> doctorsWithMostSickLeaves() {
        List<Object[]> all = sickLeaveRepository.countPerDoctor();
        if (all.isEmpty()) return List.of();
        Long max = (Long) all.getFirst()[1];
        return all.stream().filter(r -> r[1].equals(max)).toList();
    }

    private ExaminationResponseDTO toExaminationDTO(Examination e) {
        return new ExaminationResponseDTO(
                e.getId(), e.getDateTime(),
                e.getDoctor().getName(), e.getPatient().getName(),
                e.getDiagnosis().getName(), e.getTreatment(),
                e.getPrice(), e.getPaidBy(),
                e.getSickLeave() != null
        );
    }

    private PatientResponseDTO toPatientDTO(Patient p) {
        return new PatientResponseDTO(
                p.getId(), p.getName(), p.getEgn(),
                p.getGp().getId(), p.getGp().getName(),
                p.getUser().getUsername()
        );
    }
}
