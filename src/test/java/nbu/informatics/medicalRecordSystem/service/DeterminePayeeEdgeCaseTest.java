package nbu.informatics.medicalRecordSystem.service;

import nbu.informatics.medicalRecordSystem.mapper.ExaminationMapper;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;
import nbu.informatics.medicalRecordSystem.repository.DiagnosisRepository;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.HealthInsuranceRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeterminePayeeEdgeCaseTest {

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

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Тест Пациент");
    }

    @Test
    void determinePayee_allPaid_returnsNHIF() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(true);

        assertEquals(PaidBy.NHIF, examinationService.determinePayee(patient));
    }

    @Test
    void determinePayee_firstMonthUnpaid_returnsPatient() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(false);

        assertEquals(PaidBy.PATIENT, examinationService.determinePayee(patient));
    }

    @Test
    void determinePayee_lastOfSixMonthsUnpaid_returnsPatient() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        assertEquals(PaidBy.PATIENT, examinationService.determinePayee(patient));
    }

    @Test
    void determinePayee_checksExactlySixMonths() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt())).thenReturn(true);

        examinationService.determinePayee(patient);

        verify(healthInsuranceRepository, atLeastOnce())
                .existsByPatientAndYearAndMonthAndPaidTrue(any(), anyInt(), anyInt());
    }

    @Test
    void determinePayee_yearBoundary_checksCorrectYearAndMonth() {
        ArgumentCaptor<Integer> yearCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> monthCaptor = ArgumentCaptor.forClass(Integer.class);

        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                eq(patient), anyInt(), anyInt())).thenReturn(true);

        examinationService.determinePayee(patient);

        verify(healthInsuranceRepository, atLeastOnce())
                .existsByPatientAndYearAndMonthAndPaidTrue(
                        eq(patient), yearCaptor.capture(), monthCaptor.capture());

        List<Integer> years = yearCaptor.getAllValues();
        List<Integer> months = monthCaptor.getAllValues();

        assertEquals(6, years.size());
        assertEquals(6, months.size());

        for (int month : months) {
            assertTrue(month >= 1 && month <= 12,
                    "Month " + month + " is outside valid range 1-12");
        }

        List<int[]> pairs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            pairs.add(new int[]{years.get(i), months.get(i)});
        }

        for (int i = 0; i < pairs.size() - 1; i++) {
            int[] current = pairs.get(i);
            int[] prev = pairs.get(i + 1);

            if (current[1] == 1) {
                assertEquals(12, prev[1],
                        "After January, previous month should be December");
                assertEquals(current[0] - 1, prev[0],
                        "After January, previous year should be current year - 1");
            } else {
                assertEquals(current[1] - 1, prev[1],
                        "Previous month should be current month - 1");
                assertEquals(current[0], prev[0],
                        "Year should stay the same within the same year");
            }
        }
    }

    @Test
    void determinePayee_stopsAtFirstUnpaid() {
        when(healthInsuranceRepository.existsByPatientAndYearAndMonthAndPaidTrue(
                any(), anyInt(), anyInt()))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        PaidBy result = examinationService.determinePayee(patient);

        assertEquals(PaidBy.PATIENT, result);
        verify(healthInsuranceRepository, org.mockito.Mockito.times(3))
                .existsByPatientAndYearAndMonthAndPaidTrue(any(), anyInt(), anyInt());
    }
}
