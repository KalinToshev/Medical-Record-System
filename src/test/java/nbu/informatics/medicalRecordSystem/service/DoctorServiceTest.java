package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.mapper.DoctorMapper;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.doctor.DoctorUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private SpecialityRepository specialityRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    private Doctor doctor;
    private Speciality speciality;
    private User user;
    private DoctorResponseDTO doctorResponseDTO;

    @BeforeEach
    void setUp() {
        speciality = new Speciality();
        speciality.setId(1L);
        speciality.setName("Кардиология");

        user = new User();
        user.setId(1L);
        user.setUsername("doctor1");
        user.setRole(Role.DOCTOR);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Д-р Иванов");
        doctor.setGp(true);
        doctor.setSpeciality(speciality);
        doctor.setUser(user);

        doctorResponseDTO = new DoctorResponseDTO();
        doctorResponseDTO.setId(1L);
        doctorResponseDTO.setName("Д-р Иванов");
        doctorResponseDTO.setGp(true);
        doctorResponseDTO.setSpecialityName("Кардиология");
    }

    // --- findAll ---

    @Test
    void findAll_returnsMappedDTOs() {
        when(doctorRepository.findAll()).thenReturn(List.of(doctor));
        when(doctorMapper.toDto(doctor)).thenReturn(doctorResponseDTO);

        List<DoctorResponseDTO> result = doctorService.findAll();

        assertEquals(1, result.size());
        assertEquals("Д-р Иванов", result.getFirst().getName());
        assertEquals("Кардиология", result.getFirst().getSpecialityName());
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(doctorRepository.findAll()).thenReturn(List.of());
        assertTrue(doctorService.findAll().isEmpty());
    }

    // --- findById ---

    @Test
    void findById_existingId_returnsDTO() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toDto(doctor)).thenReturn(doctorResponseDTO);

        DoctorResponseDTO result = doctorService.findById(1L);

        assertEquals("Д-р Иванов", result.getName());
        assertTrue(result.isGp());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> doctorService.findById(99L));
    }

    // --- create ---

    @Test
    void create_validDTO_savesDoctor() {
        DoctorCreateRequestDTO dto = new DoctorCreateRequestDTO();
        dto.setName("Д-р Петров");
        dto.setGp(false);
        dto.setSpecialityId(1L);
        dto.setUserId(1L);

        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doctorService.create(dto);

        verify(doctorRepository).save(argThat(d ->
                d.getName().equals("Д-р Петров") &&
                        d.getSpeciality().equals(speciality) &&
                        d.getUser().equals(user)
        ));
    }

    @Test
    void create_specialityNotFound_throwsException() {
        DoctorCreateRequestDTO dto = new DoctorCreateRequestDTO();
        dto.setSpecialityId(99L);
        dto.setUserId(1L);

        when(specialityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doctorService.create(dto));
    }

    @Test
    void create_userNotFound_throwsException() {
        DoctorCreateRequestDTO dto = new DoctorCreateRequestDTO();
        dto.setSpecialityId(1L);
        dto.setUserId(99L);

        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doctorService.create(dto));
    }

    // --- update ---

    @Test
    void update_validDTO_updatesDoctor() {
        DoctorUpdateRequestDTO dto = new DoctorUpdateRequestDTO();
        dto.setName("Д-р Обновен");
        dto.setGp(false);
        dto.setSpecialityId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));
        when(patientRepository.countByGpId(1L)).thenReturn(0L);

        doctorService.update(1L, dto);

        verify(doctorRepository).save(doctor);
        assertEquals("Д-р Обновен", doctor.getName());
        assertFalse(doctor.isGp());
    }

    @Test
    void update_doctorNotFound_throwsException() {
        DoctorUpdateRequestDTO dto = new DoctorUpdateRequestDTO();
        dto.setSpecialityId(1L);

        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doctorService.update(99L, dto));
    }

    @Test
    void update_specialityNotFound_throwsException() {
        DoctorUpdateRequestDTO dto = new DoctorUpdateRequestDTO();
        dto.setSpecialityId(99L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.countByGpId(1L)).thenReturn(0L);
        when(specialityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> doctorService.update(1L, dto));
    }

    @Test
    void update_removeGpStatus_whenHasPatients_throwsException() {
        DoctorUpdateRequestDTO dto = new DoctorUpdateRequestDTO();
        dto.setName("Д-р Иванов");
        dto.setGp(false); // опитваме да премахнем GP статуса
        dto.setSpecialityId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(patientRepository.countByGpId(1L)).thenReturn(3L); // има пациенти

        assertThrows(IllegalStateException.class, () -> doctorService.update(1L, dto));
    }

    // --- delete ---

    @Test
    void delete_callsRepository() {
        when(patientRepository.countByGpId(1L)).thenReturn(0L);
        doctorService.delete(1L);
        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void delete_doctorHasPatients_throwsException() {
        when(patientRepository.countByGpId(1L)).thenReturn(2L);
        assertThrows(IllegalStateException.class, () -> doctorService.delete(1L));
        verify(doctorRepository, never()).deleteById(any());
    }

    // --- findAllGps ---

    @Test
    void findAllGps_returnsOnlyGps() {
        when(doctorRepository.findByIsGpTrue()).thenReturn(List.of(doctor));
        when(doctorMapper.toDto(doctor)).thenReturn(doctorResponseDTO);

        List<DoctorResponseDTO> result = doctorService.findAllGps();

        assertEquals(1, result.size());
        assertTrue(result.getFirst().isGp());
    }

    @Test
    void findAllGps_noGps_returnsEmpty() {
        when(doctorRepository.findByIsGpTrue()).thenReturn(List.of());
        assertTrue(doctorService.findAllGps().isEmpty());
    }
}
