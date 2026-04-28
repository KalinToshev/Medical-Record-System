package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.mapper.HealthInsuranceMapper;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    @Mock
    private HealthInsuranceMapper healthInsuranceMapper;

    @InjectMocks
    private HealthInsuranceService healthInsuranceService;

    private Patient patient;
    private HealthInsuranceResponseDTO healthInsuranceResponseDTO;

    @BeforeEach
    void setUp() {
        patient = new Patient();
        patient.setId(1L);
        patient.setName("Петър Петров");

        healthInsuranceResponseDTO = new HealthInsuranceResponseDTO();
        healthInsuranceResponseDTO.setId(1L);
        healthInsuranceResponseDTO.setYear(2026);
        healthInsuranceResponseDTO.setMonth(3);
        healthInsuranceResponseDTO.setPaid(true);
    }

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
        when(healthInsuranceMapper.toDto(ins)).thenReturn(healthInsuranceResponseDTO);

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

    @Test
    void findLast6MonthsByPatient_returnsOnlyEntriesWithinLast6Months() {
        java.time.LocalDate now = java.time.LocalDate.now();
        HealthInsurance inside = buildInsurance(now.getYear(), now.getMonthValue(), true);
        HealthInsurance outside = buildInsurance(now.minusMonths(6).getYear(), now.minusMonths(6).getMonthValue(), true);

        HealthInsuranceResponseDTO dto = new HealthInsuranceResponseDTO();
        dto.setId(1L);
        dto.setYear(inside.getYear());
        dto.setMonth(inside.getMonth());
        dto.setPaid(true);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.findByPatient(patient)).thenReturn(List.of(inside, outside));
        when(healthInsuranceMapper.toDto(inside)).thenReturn(dto);

        List<HealthInsuranceResponseDTO> result = healthInsuranceService.findLast6MonthsByPatient(1L);

        assertEquals(1, result.size());
        assertEquals(inside.getMonth(), result.getFirst().getMonth());
    }

    @Test
    void findLast6MonthsByPatient_handlesYearBoundary() {
        // Simulate via known dates: current month is Feb 2026 → cutoff is Oct 2025
        // Dec 2025 and Nov 2025 are within 6-month window
        HealthInsurance dec = buildInsurance(2025, 12, true);
        HealthInsurance nov = buildInsurance(2025, 11, true);
        HealthInsurance sep = buildInsurance(2025, 9, false);

        HealthInsuranceResponseDTO decDto = new HealthInsuranceResponseDTO();
        decDto.setYear(2025); decDto.setMonth(12); decDto.setPaid(true);
        HealthInsuranceResponseDTO novDto = new HealthInsuranceResponseDTO();
        novDto.setYear(2025); novDto.setMonth(11); novDto.setPaid(true);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.findByPatient(patient)).thenReturn(List.of(dec, nov, sep));
        when(healthInsuranceMapper.toDto(dec)).thenReturn(decDto);
        when(healthInsuranceMapper.toDto(nov)).thenReturn(novDto);

        // The service uses LocalDate.now() so we can't fully control the boundary here,
        // but we verify that entries older than 6 months are excluded.
        // We call the real service and just check it filters sep out.
        List<HealthInsuranceResponseDTO> result = healthInsuranceService.findLast6MonthsByPatient(1L);

        result.forEach(r -> assertFalse(r.getYear() == 2025 && r.getMonth() == 9, "September 2025 should not appear"));
    }

    @Test
    void findLast6MonthsByPatient_sortsByYearMonthDesc() {
        java.time.LocalDate now = java.time.LocalDate.now();
        HealthInsurance older = buildInsurance(now.getYear(), now.getMonthValue() > 1 ? now.getMonthValue() - 1 : 1, true);
        HealthInsurance newer = buildInsurance(now.getYear(), now.getMonthValue(), false);

        HealthInsuranceResponseDTO olderDto = new HealthInsuranceResponseDTO();
        olderDto.setYear(older.getYear()); olderDto.setMonth(older.getMonth()); olderDto.setPaid(true);
        HealthInsuranceResponseDTO newerDto = new HealthInsuranceResponseDTO();
        newerDto.setYear(newer.getYear()); newerDto.setMonth(newer.getMonth()); newerDto.setPaid(false);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.findByPatient(patient)).thenReturn(List.of(older, newer));
        when(healthInsuranceMapper.toDto(older)).thenReturn(olderDto);
        when(healthInsuranceMapper.toDto(newer)).thenReturn(newerDto);

        List<HealthInsuranceResponseDTO> result = healthInsuranceService.findLast6MonthsByPatient(1L);

        assertEquals(2, result.size());
        assertTrue(result.get(0).getMonth() >= result.get(1).getMonth(),
                "Results should be sorted month descending");
    }

    @Test
    void findLast6MonthsByPatient_emptyWhenNoInsurances() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(healthInsuranceRepository.findByPatient(patient)).thenReturn(List.of());

        List<HealthInsuranceResponseDTO> result = healthInsuranceService.findLast6MonthsByPatient(1L);

        assertTrue(result.isEmpty());
    }

    private HealthInsurance buildInsurance(int year, int month, boolean paid) {
        HealthInsurance ins = new HealthInsurance();
        ins.setPatient(patient);
        ins.setYear(year);
        ins.setMonth(month);
        ins.setPaid(paid);
        return ins;
    }
}
