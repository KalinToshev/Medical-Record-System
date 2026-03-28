package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.diagnosis.DiagnosisResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import nbu.informatics.medicalRecordSystem.repository.DiagnosisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;
    @InjectMocks
    private DiagnosisService diagnosisService;

    private Diagnosis diagnosis;

    @BeforeEach
    void setUp() {
        diagnosis = new Diagnosis();
        diagnosis.setId(1L);
        diagnosis.setName("Грип");
    }

    @Test
    void findAll_returnsMappedDTOs() {
        when(diagnosisRepository.findAll()).thenReturn(List.of(diagnosis));
        List<DiagnosisResponseDTO> result = diagnosisService.findAll();
        assertEquals(1, result.size());
        assertEquals("Грип", result.getFirst().getName());
    }

    @Test
    void findById_existingId_returnsDTO() {
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));
        assertEquals("Грип", diagnosisService.findById(1L).getName());
    }

    @Test
    void findById_notFound_throwsException() {
        when(diagnosisRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> diagnosisService.findById(99L));
    }

    @Test
    void create_savesDiagnosis() {
        DiagnosisRequestDTO dto = new DiagnosisRequestDTO();
        dto.setName("Ангина");
        diagnosisService.create(dto);
        verify(diagnosisRepository).save(argThat(d -> d.getName().equals("Ангина")));
    }

    @Test
    void update_validDTO_updatesDiagnosis() {
        DiagnosisRequestDTO dto = new DiagnosisRequestDTO();
        dto.setName("Обновена");
        when(diagnosisRepository.findById(1L)).thenReturn(Optional.of(diagnosis));
        diagnosisService.update(1L, dto);
        verify(diagnosisRepository).save(diagnosis);
        assertEquals("Обновена", diagnosis.getName());
    }

    @Test
    void update_notFound_throwsException() {
        when(diagnosisRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> diagnosisService.update(99L, new DiagnosisRequestDTO()));
    }

    @Test
    void delete_callsRepository() {
        diagnosisService.delete(1L);
        verify(diagnosisRepository).deleteById(1L);
    }
}
