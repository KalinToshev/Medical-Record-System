package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.mapper.ExaminationMapper;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationUpdateRequestDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExaminationServiceTest {

    @Mock
    private ExaminationRepository examinationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private DiagnosisRepository diagnosisRepository;
    @Mock
    private HealthInsuranceRepository healthInsuranceRepository;
    @Mock
    private ExaminationMapper examinationMapper;

    @InjectMocks
    private ExaminationService examinationService;

    private Doctor doctor;
    private Patient patient;
    private Diagnosis diagnosis;
    private User doctorUser;
    private User adminUser;
    private ExaminationResponseDTO examinationResponseDTO;

    @BeforeEach
    void setUp() {
        doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setUsername("doctor1");
        doctorUser.setRole(Role.DOCTOR);

        adminUser = new User();
        adminUser.setId(2L);
        adminUser.setUsername("admin1");
        adminUser.setRole(Role.ADMIN);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Д-р Иванов");
        doctor.setUser(doctorUser);

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Петър Петров");
        patient.setEgn("9001011234");

        diagnosis = new Diagnosis();
        diagnosis.setId(1L);
        diagnosis.setName("Грип");

        examinationResponseDTO = new ExaminationResponseDTO();
        examinationResponseDTO.setId(1L);
        examinationResponseDTO.setDoctorName("Д-р Иванов");
        examinationResponseDTO.setPatientName("Петър Петров");
    }

    // --- findAll ---

    @Test
    void findAll_returnsAllExaminations() {
        Examination e = buildExamination();
        when(examinationRepository.findAll()).thenReturn(List.of(e));
        when(examinationMapper.toDto(e)).thenReturn(examinationResponseDTO);

        List<ExaminationResponseDTO> result = examinationService.findAll();

        assertEquals(1, result.size());
        assertEquals("Д-р Иванов", result.getFirst().getDoctorName());
    }

    @Test
    void findAll_emptyList_returnsEmpty() {
        when(examinationRepository.findAll()).thenReturn(List.of());
        assertTrue(examinationService.findAll().isEmpty());
    }

    // --- findById ---

    @Test
    void findById_existingId_returnsDTO() {
        Examination e = buildExamination();
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));
        when(examinationMapper.toDto(e)).thenReturn(examinationResponseDTO);

        ExaminationResponseDTO result = examinationService.findById(1L);

        assertEquals("Петър Петров", result.getPatientName());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(examinationRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> examinationService.findById(99L));
    }

    // --- determinePayee ---

    @Test
    void determinePayee_allSixMonthsPaid_returnsNHIF() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(true);

        PaidBy result = examinationService.determinePayee(patient);

        assertEquals(PaidBy.NHIF, result);
    }

    @Test
    void determinePayee_oneMonthNotPaid_returnsPatient() {
        // Първият месец не е платен, останалите са
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt()))
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true);

        PaidBy result = examinationService.determinePayee(patient);

        assertEquals(PaidBy.PATIENT, result);
    }

    @Test
    void determinePayee_noInsurances_returnsPatient() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(false);

        PaidBy result = examinationService.determinePayee(patient);

        assertEquals(PaidBy.PATIENT, result);
    }

    // --- create ---

    @Test
    void create_validDTO_savesExamination() {
        ExaminationCreateRequestDTO dto = new ExaminationCreateRequestDTO();
        dto.setPatientId(1L);
        dto.setDiagnosisId(1L);
        dto.setTreatment("Почивка");
        dto.setPrice(new BigDecimal("50.00"));
        dto.setIssueSickLeave(false);

        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(false);

        examinationService.create(dto, doctorUser);

        verify(examinationRepository).save(any(Examination.class));
    }

    @Test
    void create_withSickLeave_savesSickLeave() {
        ExaminationCreateRequestDTO dto = new ExaminationCreateRequestDTO();
        dto.setPatientId(1L);
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("50.00"));
        dto.setIssueSickLeave(true);
        dto.setSickLeaveStartDate(LocalDate.now());
        dto.setSickLeaveDays(3);

        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(false);

        examinationService.create(dto, doctorUser);

        verify(examinationRepository).save(argThat(e -> e.getSickLeave() != null));
    }

    @Test
    void create_doctorNotFound_throwsException() {
        ExaminationCreateRequestDTO dto = new ExaminationCreateRequestDTO();
        dto.setPatientId(1L);
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("50.00"));

        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> examinationService.create(dto, doctorUser));
    }

    @Test
    void create_patientNotFound_throwsException() {
        ExaminationCreateRequestDTO dto = new ExaminationCreateRequestDTO();
        dto.setPatientId(99L);
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("50.00"));

        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> examinationService.create(dto, doctorUser));
    }

    // --- update ---

    @Test
    void update_ownExamination_updatesSuccessfully() {
        Examination e = buildExamination();
        ExaminationUpdateRequestDTO dto = new ExaminationUpdateRequestDTO();
        dto.setDiagnosisId(1L);
        dto.setTreatment("Ново лечение");
        dto.setPrice(new BigDecimal("60.00"));
        dto.setIssueSickLeave(false);

        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));

        examinationService.update(1L, dto, doctorUser);

        verify(examinationRepository).save(e);
        assertEquals("Ново лечение", e.getTreatment());
    }

    @Test
    void update_notOwnExamination_throwsAccessDeniedException() {
        Doctor otherDoctor = new Doctor();
        otherDoctor.setId(99L);

        Examination e = buildExamination();
        e.setDoctor(otherDoctor);

        ExaminationUpdateRequestDTO dto = new ExaminationUpdateRequestDTO();
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("60.00"));

        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));

        assertThrows(AccessDeniedException.class,
                () -> examinationService.update(1L, dto, doctorUser));
    }

    @Test
    void update_addSickLeave_sickLeaveCreated() {
        Examination e = buildExamination();
        ExaminationUpdateRequestDTO dto = new ExaminationUpdateRequestDTO();
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("60.00"));
        dto.setIssueSickLeave(true);
        dto.setSickLeaveStartDate(LocalDate.now());
        dto.setSickLeaveDays(5);

        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));

        examinationService.update(1L, dto, doctorUser);

        assertNotNull(e.getSickLeave());
        assertEquals(5, e.getSickLeave().getDays());
    }

    @Test
    void update_removeSickLeave_sickLeaveNull() {
        Examination e = buildExamination();
        SickLeave sl = new SickLeave();
        sl.setDays(3);
        sl.setStartDate(LocalDate.now());
        e.setSickLeave(sl);

        ExaminationUpdateRequestDTO dto = new ExaminationUpdateRequestDTO();
        dto.setDiagnosisId(1L);
        dto.setPrice(new BigDecimal("60.00"));
        dto.setIssueSickLeave(false);

        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));

        examinationService.update(1L, dto, doctorUser);

        assertNull(e.getSickLeave());
    }

    // --- delete ---

    @Test
    void delete_existingId_deletesSuccessfully() {
        Examination e = buildExamination();
        when(examinationRepository.findById(1L)).thenReturn(Optional.of(e));

        examinationService.delete(1L, adminUser);

        verify(examinationRepository).delete(e);
    }

    // --- helper ---

    private Examination buildExamination() {
        Examination e = new Examination();
        e.setId(1L);
        e.setDateTime(LocalDateTime.now());
        e.setDoctor(doctor);
        e.setPatient(patient);
        e.setDiagnosis(diagnosis);
        e.setTreatment("Почивка");
        e.setPrice(new BigDecimal("50.00"));
        e.setPaidBy(PaidBy.PATIENT);
        return e;
    }
}
