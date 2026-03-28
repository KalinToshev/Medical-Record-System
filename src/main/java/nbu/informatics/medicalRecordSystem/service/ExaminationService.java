package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.sickLeave.SickLeaveResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.SickLeave;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.DiagnosisRepository;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.HealthInsuranceRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExaminationService {

    private final ExaminationRepository examinationRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DiagnosisRepository diagnosisRepository;
    private final HealthInsuranceRepository healthInsuranceRepository;

    @Transactional(readOnly = true)
    public List<ExaminationResponseDTO> findAll() {
        return examinationRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExaminationResponseDTO findById(Long id) {
        return toResponseDTO(getExaminationOrThrow(id));
    }

    public ExaminationResponseDTO findByIdForDoctor(Long id, User currentUser) {
        Examination examination = getExaminationOrThrow(id);

        if (currentUser.getRole() != Role.ADMIN) {
            verifyDoctorOwnership(examination, currentUser);
        }

        return toResponseDTO(examination);
    }

    public Long getDiagnosisIdForDoctor(Long examinationId, User currentUser) {
        Examination examination = getExaminationOrThrow(examinationId);

        if (currentUser.getRole() != Role.ADMIN) {
            verifyDoctorOwnership(examination, currentUser);
        }

        return examination.getDiagnosis().getId();
    }

    public SickLeaveResponseDTO getSickLeaveForDoctor(Long examinationId, User currentUser) {
        Examination examination = getExaminationOrThrow(examinationId);

        if (currentUser.getRole() != Role.ADMIN) {
            verifyDoctorOwnership(examination, currentUser);
        }

        SickLeave sl = examination.getSickLeave();
        if (sl == null) return null;
        return new SickLeaveResponseDTO(sl.getId(), sl.getStartDate(), sl.getDays());
    }

    @Transactional(readOnly = true)
    public List<ExaminationResponseDTO> findAllForUser(User user) {
        return switch (user.getRole()) {
            case ADMIN -> examinationRepository.findAll()
                    .stream().map(this::toResponseDTO).toList();

            case DOCTOR -> {
                Doctor doctor = doctorRepository.findByUser(user)
                        .orElseThrow(() -> new EntityNotFoundException("Лекарят не е намерен"));
                yield examinationRepository.findByDoctor(doctor)
                        .stream().map(this::toResponseDTO).toList();
            }

            case PATIENT -> {
                Patient patient = patientRepository.findByUser(user)
                        .orElseThrow(() -> new EntityNotFoundException("Пациентът не е намерен"));
                yield examinationRepository.findByPatient(patient)
                        .stream().map(this::toResponseDTO).toList();
            }

            case PENDING -> List.of();
        };
    }

    @Transactional
    public void create(ExaminationCreateRequestDTO dto, User currentUser) {
        Doctor doctor = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Лекарят не е намерен"));

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Пациентът не е намерен"));

        Diagnosis diagnosis = diagnosisRepository.findById(dto.getDiagnosisId())
                .orElseThrow(() -> new EntityNotFoundException("Диагнозата не е намерена"));

        Examination examination = new Examination();
        examination.setDateTime(LocalDateTime.now());
        examination.setDoctor(doctor);
        examination.setPatient(patient);
        examination.setDiagnosis(diagnosis);
        examination.setTreatment(dto.getTreatment());
        examination.setPrice(dto.getPrice());
        examination.setPaidBy(determinePayee(patient));

        if (dto.isIssueSickLeave()) {
            SickLeave sickLeave = new SickLeave();
            sickLeave.setStartDate(dto.getSickLeaveStartDate());
            sickLeave.setDays(dto.getSickLeaveDays());
            sickLeave.setExamination(examination);
            examination.setSickLeave(sickLeave);
        }

        examinationRepository.save(examination);
    }

    @Transactional
    public void update(Long id, ExaminationUpdateRequestDTO dto, User currentUser) {
        Examination examination = getExaminationOrThrow(id);

        Doctor doctor = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Лекарят не е намерен"));

        if (!examination.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Нямате право да редактирате този преглед");
        }

        Diagnosis diagnosis = diagnosisRepository.findById(dto.getDiagnosisId())
                .orElseThrow(() -> new EntityNotFoundException("Диагнозата не е намерена"));

        examination.setDiagnosis(diagnosis);
        examination.setTreatment(dto.getTreatment());
        examination.setPrice(dto.getPrice());

        if (dto.isIssueSickLeave()) {
            SickLeave sickLeave = examination.getSickLeave() != null
                    ? examination.getSickLeave()
                    : new SickLeave();
            sickLeave.setStartDate(dto.getSickLeaveStartDate());
            sickLeave.setDays(dto.getSickLeaveDays());
            sickLeave.setExamination(examination);
            examination.setSickLeave(sickLeave);
        } else {
            examination.setSickLeave(null);
        }

        examinationRepository.save(examination);
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Examination examination = getExaminationOrThrow(id);

        if (currentUser.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Само администратори могат да изтриват прегледи");
        }

        examinationRepository.delete(examination);
    }

    public PaidBy determinePayee(Patient patient) {
        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();
        int currentMonth = now.getMonthValue();

        for (int i = 0; i < 6; i++) {
            int month = currentMonth - i;
            int year = currentYear;
            if (month <= 0) {
                month += 12;
                year -= 1;
            }

            boolean paid = healthInsuranceRepository
                    .existsByPatientAndYearAndMonthAndPaidTrue(patient, year, month);

            if (!paid) return PaidBy.PATIENT;
        }

        return PaidBy.NHIF;
    }

    private Examination getExaminationOrThrow(Long id) {
        return examinationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Преглед с id " + id + " не е намерен"));
    }

    private ExaminationResponseDTO toResponseDTO(Examination examination) {
        return new ExaminationResponseDTO(
                examination.getId(),
                examination.getDateTime(),
                examination.getDoctor().getName(),
                examination.getPatient().getName(),
                examination.getDiagnosis().getName(),
                examination.getTreatment(),
                examination.getPrice(),
                examination.getPaidBy(),
                examination.getSickLeave() != null
        );
    }

    private void verifyDoctorOwnership(Examination examination, User currentUser) {
        Doctor doctor = doctorRepository.findByUser(currentUser)
                .orElseThrow(() -> new EntityNotFoundException("Лекарят не е намерен"));

        if (!examination.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("Нямате право да достъпите този преглед");
        }
    }
}
