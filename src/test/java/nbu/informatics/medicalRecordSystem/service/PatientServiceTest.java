package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientCreateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientResponseDTO;
import nbu.informatics.medicalRecordSystem.model.dto.patient.PatientUpdateRequestDTO;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private Doctor gp;
    private User user;

    @BeforeEach
    void setUp() {
        Speciality speciality = new Speciality();
        speciality.setId(1L);
        speciality.setName("Обща медицина");

        user = new User();
        user.setId(1L);
        user.setUsername("patient1");
        user.setRole(Role.PATIENT);

        gp = new Doctor();
        gp.setId(1L);
        gp.setName("Д-р Иванов");
        gp.setGp(true);
        gp.setSpeciality(speciality);
        gp.setUser(new User());

        patient = new Patient();
        patient.setId(1L);
        patient.setName("Петър Петров");
        patient.setEgn("9001011234");
        patient.setGp(gp);
        patient.setUser(user);
    }

    // --- findAll ---

    @Test
    void findAll_returnsMappedDTOs() {
        when(patientRepository.findAll()).thenReturn(List.of(patient));

        List<PatientResponseDTO> result = patientService.findAll();

        assertEquals(1, result.size());
        assertEquals("Петър Петров", result.getFirst().getName());
        assertEquals("9001011234", result.getFirst().getEgn());
    }

    @Test
    void findAll_empty_returnsEmptyList() {
        when(patientRepository.findAll()).thenReturn(List.of());
        assertTrue(patientService.findAll().isEmpty());
    }

    // --- findById ---

    @Test
    void findById_existingId_returnsDTO() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        PatientResponseDTO result = patientService.findById(1L);

        assertEquals("Петър Петров", result.getName());
        assertEquals("Д-р Иванов", result.getGpName());
    }

    @Test
    void findById_nonExistingId_throwsException() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> patientService.findById(99L));
    }

    // --- create ---

    @Test
    void create_validDTO_savesPatient() {
        PatientCreateRequestDTO dto = new PatientCreateRequestDTO();
        dto.setName("Нов Пациент");
        dto.setEgn("9505051234");
        dto.setGpId(1L);
        dto.setUserId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(gp));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        patientService.create(dto);

        verify(patientRepository).save(argThat(p ->
                p.getName().equals("Нов Пациент") &&
                        p.getEgn().equals("9505051234") &&
                        p.getGp().equals(gp)
        ));
    }

    @Test
    void create_gpNotFound_throwsException() {
        PatientCreateRequestDTO dto = new PatientCreateRequestDTO();
        dto.setGpId(99L);
        dto.setUserId(1L);

        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.create(dto));
    }

    @Test
    void create_userNotFound_throwsException() {
        PatientCreateRequestDTO dto = new PatientCreateRequestDTO();
        dto.setGpId(1L);
        dto.setUserId(99L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(gp));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.create(dto));
    }

    // --- update ---

    @Test
    void update_validDTO_updatesPatient() {
        PatientUpdateRequestDTO dto = new PatientUpdateRequestDTO();
        dto.setName("Обновен Пациент");
        dto.setEgn("9001011234");
        dto.setGpId(1L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(gp));

        patientService.update(1L, dto);

        verify(patientRepository).save(patient);
        assertEquals("Обновен Пациент", patient.getName());
    }

    @Test
    void update_patientNotFound_throwsException() {
        PatientUpdateRequestDTO dto = new PatientUpdateRequestDTO();
        dto.setGpId(1L);

        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.update(99L, dto));
    }

    @Test
    void update_gpNotFound_throwsException() {
        PatientUpdateRequestDTO dto = new PatientUpdateRequestDTO();
        dto.setGpId(99L);

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> patientService.update(1L, dto));
    }

    // --- delete ---

    @Test
    void delete_callsRepository() {
        patientService.delete(1L);
        verify(patientRepository).deleteById(1L);
    }
}
