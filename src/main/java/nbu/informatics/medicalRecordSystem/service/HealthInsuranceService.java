package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.mapper.HealthInsuranceMapper;
import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.HealthInsurance;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.repository.HealthInsuranceRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HealthInsuranceService {

    private final HealthInsuranceRepository healthInsuranceRepository;
    private final PatientRepository patientRepository;
    private final HealthInsuranceMapper healthInsuranceMapper;

    public List<HealthInsuranceResponseDTO> findByPatient(Long patientId) {
        Patient patient = getPatientOrThrow(patientId);
        return healthInsuranceRepository.findByPatient(patient)
                .stream()
                .map(healthInsuranceMapper::toDto)
                .toList();
    }

    public List<HealthInsuranceResponseDTO> findLast6MonthsByPatient(Long patientId) {
        Patient patient = getPatientOrThrow(patientId);
        LocalDate cutoff = LocalDate.now().minusMonths(5).withDayOfMonth(1);

        return healthInsuranceRepository.findByPatient(patient).stream()
                .filter(hi -> {
                    LocalDate hiDate = LocalDate.of(hi.getYear(), hi.getMonth(), 1);
                    return !hiDate.isBefore(cutoff);
                })
                .sorted(Comparator
                        .comparing(HealthInsurance::getYear).reversed()
                        .thenComparing(Comparator.comparing(HealthInsurance::getMonth).reversed()))
                .map(healthInsuranceMapper::toDto)
                .toList();
    }

    @Transactional
    public void create(Long patientId, HealthInsuranceRequestDTO dto) {
        Patient patient = getPatientOrThrow(patientId);

        boolean alreadyExists = healthInsuranceRepository
                .existsByPatientAndYearAndMonth(patient, dto.getYear(), dto.getMonth());

        if (alreadyExists) {
            throw new IllegalStateException(
                    "Вече съществува запис за " + dto.getMonth() + "/" + dto.getYear()
            );
        }

        HealthInsurance insurance = new HealthInsurance();
        insurance.setPatient(patient);
        insurance.setYear(dto.getYear());
        insurance.setMonth(dto.getMonth());
        insurance.setPaid(dto.isPaid());

        try {
            healthInsuranceRepository.save(insurance);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "Вече съществува запис за " + dto.getMonth() + "/" + dto.getYear()
            );
        }
    }

    @Transactional
    public void delete(Long patientId, Long insuranceId) {
        HealthInsurance insurance = healthInsuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new EntityNotFoundException("Записът не е намерен"));

        if (!insurance.getPatient().getId().equals(patientId)) {
            throw new AccessDeniedException("Нямате право да изтриете този запис");
        }

        healthInsuranceRepository.deleteById(insuranceId);
    }

    private Patient getPatientOrThrow(Long patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Пациентът не е намерен"));
    }
}
