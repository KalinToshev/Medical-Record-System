package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.healthInsurance.HealthInsuranceResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.HealthInsurance;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.repository.HealthInsuranceRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthInsuranceServiceTest {

    @Mock
    private HealthInsuranceRepository healthInsuranceRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private HealthInsuranceService healthInsuranceService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Петър Петров");
    }

    // --- findByPatient ---

    @Test
    void findByPatient_returnsInsurances() {
        HealthInsurance ins = new HealthInsurance();
        ins.setId(1L);
        ins.setYear(2026);
        ins.setMonth(3);
        ins.setPaid(true);
        ins.setPatient(patient);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.findByPatient(patient)).thenReturn(List.of(ins));

        List<HealthInsuranceResponseDTO> result =
                healthInsuranceService.findByPatient(1L);

        assertEquals(1, result.size());
        assertEquals(2026, result.getFirst().getYear());
        assertTrue(result.getFirst().isPaid());
    }

    @Test
    void findByPatient_patientNotFound_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> healthInsuranceService.findByPatient(99L));
    }

    // --- create ---

    @Test
    void create_validDTO_savesInsurance() {
        HealthInsuranceRequestDTO dto = new HealthInsuranceRequestDTO();
        dto.setYear(2026);
        dto.setMonth(3);
        dto.setPaid(true);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.existsByPatientAndYearAndMonth(
                patient, 2026, 3)).thenReturn(false);

        healthInsuranceService.create(1L, dto);

        verify(healthInsuranceRepository).save(argThat(ins ->
                ins.getYear() == 2026 &&
                        ins.getMonth() == 3 &&
                        ins.isPaid()
        ));
    }

    @Test
    void create_duplicateEntry_throwsIllegalStateException() {
        HealthInsuranceRequestDTO dto = new HealthInsuranceRequestDTO();
        dto.setYear(2026);
        dto.setMonth(3);
        dto.setPaid(true);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.existsByPatientAndYearAndMonth(
                patient, 2026, 3)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> healthInsuranceService.create(1L, dto));
    }

    @Test
    void create_patientNotFound_throwsException() {
        HealthInsuranceRequestDTO dto = new HealthInsuranceRequestDTO();
        dto.setYear(2026);
        dto.setMonth(3);

        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> healthInsuranceService.create(99L, dto));
    }

    // --- delete ---

    @Test
    void delete_validEntry_deletesSuccessfully() {
        HealthInsurance ins = new HealthInsurance();
        ins.setId(1L);
        ins.setPatient(patient);

        when(healthInsuranceRepository.findById(1L)).thenReturn(Optional.of(ins));

        healthInsuranceService.delete(1L, 1L);

        verify(healthInsuranceRepository).deleteById(1L);
    }

    @Test
    void delete_wrongPatient_throwsAccessDeniedException() {
        Patient otherPatient = new Patient();
        otherPatient.setId(99L);

        HealthInsurance ins = new HealthInsurance();
        ins.setId(1L);
        ins.setPatient(otherPatient);

        when(healthInsuranceRepository.findById(1L)).thenReturn(Optional.of(ins));

        assertThrows(AccessDeniedException.class,
                () -> healthInsuranceService.delete(1L, 1L));
    }

    @Test
    void delete_insuranceNotFound_throwsException() {
        when(healthInsuranceRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> healthInsuranceService.delete(1L, 99L));
    }
}
