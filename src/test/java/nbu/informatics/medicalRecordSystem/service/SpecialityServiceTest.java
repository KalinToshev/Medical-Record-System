package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.speciality.SpecialityResponseDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
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
class SpecialityServiceTest {

    @Mock
    private SpecialityRepository specialityRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @InjectMocks
    private SpecialityService specialityService;

    private Speciality speciality;

    @BeforeEach
    void setUp() {
        speciality = new Speciality();
        speciality.setId(1L);
        speciality.setName("Кардиология");
    }

    @Test
    void findAll_returnsMappedDTOs() {
        when(specialityRepository.findAll()).thenReturn(List.of(speciality));
        List<SpecialityResponseDTO> result = specialityService.findAll();
        assertEquals(1, result.size());
        assertEquals("Кардиология", result.getFirst().getName());
    }

    @Test
    void findById_existingId_returnsDTO() {
        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));
        assertEquals("Кардиология", specialityService.findById(1L).getName());
    }

    @Test
    void findById_notFound_throwsException() {
        when(specialityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> specialityService.findById(99L));
    }

    @Test
    void create_savesSpeciality() {
        SpecialityRequestDTO dto = new SpecialityRequestDTO();
        dto.setName("Неврология");
        specialityService.create(dto);
        verify(specialityRepository).save(argThat(s -> s.getName().equals("Неврология")));
    }

    @Test
    void update_validDTO_updatesSpeciality() {
        SpecialityRequestDTO dto = new SpecialityRequestDTO();
        dto.setName("Обновена");
        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));
        specialityService.update(1L, dto);
        verify(specialityRepository).save(speciality);
        assertEquals("Обновена", speciality.getName());
    }

    @Test
    void update_notFound_throwsException() {
        when(specialityRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> specialityService.update(99L, new SpecialityRequestDTO()));
    }

    @Test
    void delete_callsRepository() {
        when(doctorRepository.countBySpecialityId(1L)).thenReturn(0L);

        specialityService.delete(1L);

        verify(specialityRepository).deleteById(1L);
    }
}
