package nbu.informatics.medicalRecordSystem.service;

import nbu.informatics.medicalRecordSystem.mapper.ExaminationMapper;
import nbu.informatics.medicalRecordSystem.mapper.PatientMapper;
import nbu.informatics.medicalRecordSystem.model.dto.examination.ExaminationResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.report.MonthCountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameAmountProjection;
import nbu.informatics.medicalRecordSystem.model.dto.report.NameCountProjection;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SickLeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ExaminationRepository examinationRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private SickLeaveRepository sickLeaveRepository;
    @Mock
    private ExaminationMapper examinationMapper;
    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private ReportService reportService;

    private Patient patient;
    private Examination examination;
    private ExaminationResponseDTO examinationDTO;
    private PatientResponseDTO patientDTO;

    @BeforeEach
    void setUp() {
        User doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setUsername("doctor1");

        User patientUser = new User();
        patientUser.setId(2L);
        patientUser.setUsername("patient1");

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Д-р Иванов");
        doctor.setUser(doctorUser);

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Петър Петров");
        patient.setEgn("9001011234");
        patient.setGp(doctor);
        patient.setUser(patientUser);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1L);
        diagnosis.setName("Грип");

        examination = new Examination();
        examination.setId(1L);
        examination.setDateTime(LocalDateTime.of(2026, 3, 15, 10, 0));
        examination.setDoctor(doctor);
        examination.setPatient(patient);
        examination.setDiagnosis(diagnosis);
        examination.setTreatment("Почивка");
        examination.setPrice(new BigDecimal("50.00"));
        examination.setPaidBy(PaidBy.PATIENT);

        examinationDTO = new ExaminationResponseDTO();
        examinationDTO.setId(1L);
        examinationDTO.setDoctorName("Д-р Иванов");
        examinationDTO.setPatientName("Петър Петров");
        examinationDTO.setDiagnosisName("Грип");
        examinationDTO.setPrice(new BigDecimal("50.00"));

        patientDTO = new PatientResponseDTO();
        patientDTO.setId(1L);
        patientDTO.setName("Петър Петров");
        patientDTO.setEgn("9001011234");
        patientDTO.setGpId(1L);
        patientDTO.setGpName("Д-р Иванов");
    }

    @Test
    void findByDiagnosis_returnsDistinctPatients() {
        Examination exam2 = new Examination();
        exam2.setId(2L);
        exam2.setPatient(patient);

        when(examinationRepository.findByDiagnosisId(1L)).thenReturn(List.of(examination, exam2));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        List<PatientResponseDTO> result = reportService.findByDiagnosis(1L);

        assertEquals(1, result.size());
        assertEquals("Петър Петров", result.getFirst().getName());
    }

    @Test
    void findByDiagnosis_noExaminations_returnsEmpty() {
        when(examinationRepository.findByDiagnosisId(99L)).thenReturn(List.of());

        List<PatientResponseDTO> result = reportService.findByDiagnosis(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByGp_returnsPatients() {
        when(patientRepository.findByGpId(1L)).thenReturn(List.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDTO);

        List<PatientResponseDTO> result = reportService.findByGp(1L);

        assertEquals(1, result.size());
        assertEquals("Петър Петров", result.getFirst().getName());
    }

    @Test
    void countPatientsPerGp_returnsProjections() {
        List<NameCountProjection> expected = List.of(
                new NameCountProjection("Д-р Иванов", 5L)
        );
        when(patientRepository.countPerGp()).thenReturn(expected);

        List<NameCountProjection> result = reportService.countPatientsPerGp();

        assertEquals(1, result.size());
        assertEquals("Д-р Иванов", result.getFirst().name());
        assertEquals(5L, result.getFirst().count());
    }

    @Test
    void findPatientHistory_returnsExaminations() {
        when(examinationRepository.findByPatientId(1L)).thenReturn(List.of(examination));
        when(examinationMapper.toDto(examination)).thenReturn(examinationDTO);

        List<ExaminationResponseDTO> result = reportService.findPatientHistory(1L);

        assertEquals(1, result.size());
        assertEquals("Д-р Иванов", result.getFirst().getDoctorName());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByDoctorAndPeriod_allFilters_returnsFiltered() {
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 31, 23, 59);

        when(examinationRepository.findAll(any(Specification.class))).thenReturn(List.of(examination));
        when(examinationMapper.toDto(examination)).thenReturn(examinationDTO);

        List<ExaminationResponseDTO> result = reportService.findByDoctorAndPeriod(1L, from, to);

        assertEquals(1, result.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void findByDoctorAndPeriod_noFilters_returnsAll() {
        when(examinationRepository.findAll(any(Specification.class))).thenReturn(List.of(examination));
        when(examinationMapper.toDto(examination)).thenReturn(examinationDTO);

        List<ExaminationResponseDTO> result = reportService.findByDoctorAndPeriod(null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void countExaminationsPerDoctor_returnsProjections() {
        List<NameCountProjection> expected = List.of(
                new NameCountProjection("Д-р Иванов", 10L),
                new NameCountProjection("Д-р Петрова", 8L)
        );
        when(examinationRepository.countPerDoctor()).thenReturn(expected);

        List<NameCountProjection> result = reportService.countExaminationsPerDoctor();

        assertEquals(2, result.size());
    }

    @Test
    void totalPaidByPatients_withData_returnsTotal() {
        when(examinationRepository.totalPaidByPatients()).thenReturn(new BigDecimal("500.00"));

        BigDecimal result = reportService.totalPaidByPatients();

        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    void totalPaidByPatients_noData_returnsZero() {
        when(examinationRepository.totalPaidByPatients()).thenReturn(null);

        BigDecimal result = reportService.totalPaidByPatients();

        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void totalPaidByPatientsPerDoctor_returnsProjections() {
        List<NameAmountProjection> expected = List.of(
                new NameAmountProjection("Д-р Иванов", new BigDecimal("300.00"))
        );
        when(examinationRepository.totalPaidByPatientsPerDoctor()).thenReturn(expected);

        List<NameAmountProjection> result = reportService.totalPaidByPatientsPerDoctor();

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("300.00"), result.getFirst().amount());
    }

    @Test
    void mostCommonDiagnosis_withData_returnsTopDiagnosis() {
        when(examinationRepository.diagnosisFrequency()).thenReturn(List.of(
                new NameCountProjection("Грип", 15L),
                new NameCountProjection("Ангина", 10L)
        ));

        String result = reportService.mostCommonDiagnosis();

        assertEquals("Грип", result);
    }

    @Test
    void mostCommonDiagnosis_noData_returnsDefault() {
        when(examinationRepository.diagnosisFrequency()).thenReturn(List.of());

        String result = reportService.mostCommonDiagnosis();

        assertEquals("Няма данни", result);
    }

    @Test
    void monthWithMostSickLeaves_withData_returnsFormatted() {
        when(sickLeaveRepository.countPerMonth()).thenReturn(List.of(
                new MonthCountProjection(2026, 3, 12L)
        ));

        String result = reportService.monthWithMostSickLeaves();

        assertEquals("3/2026", result);
    }

    @Test
    void monthWithMostSickLeaves_noData_returnsDefault() {
        when(sickLeaveRepository.countPerMonth()).thenReturn(List.of());

        String result = reportService.monthWithMostSickLeaves();

        assertEquals("Няма данни", result);
    }

    @Test
    void doctorsWithMostSickLeaves_singleTop_returnsSingle() {
        when(sickLeaveRepository.countPerDoctor()).thenReturn(List.of(
                new NameCountProjection("Д-р Иванов", 10L),
                new NameCountProjection("Д-р Петрова", 5L)
        ));

        List<NameCountProjection> result = reportService.doctorsWithMostSickLeaves();

        assertEquals(1, result.size());
        assertEquals("Д-р Иванов", result.getFirst().name());
    }

    @Test
    void doctorsWithMostSickLeaves_tiedTop_returnsBoth() {
        when(sickLeaveRepository.countPerDoctor()).thenReturn(List.of(
                new NameCountProjection("Д-р Иванов", 10L),
                new NameCountProjection("Д-р Петрова", 10L),
                new NameCountProjection("Д-р Георгиев", 3L)
        ));

        List<NameCountProjection> result = reportService.doctorsWithMostSickLeaves();

        assertEquals(2, result.size());
    }

    @Test
    void doctorsWithMostSickLeaves_noData_returnsEmpty() {
        when(sickLeaveRepository.countPerDoctor()).thenReturn(List.of());

        List<NameCountProjection> result = reportService.doctorsWithMostSickLeaves();

        assertTrue(result.isEmpty());
    }
}
