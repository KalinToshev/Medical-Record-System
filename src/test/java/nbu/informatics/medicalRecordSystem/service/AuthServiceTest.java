package nbu.informatics.medicalRecordSystem.service;

import jakarta.persistence.EntityNotFoundException;
import nbu.informatics.medicalRecordSystem.model.dto.auth.AssignRoleRequestDTO;
import nbu.informatics.medicalRecordSystem.model.dto.auth.RegisterRequestDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private SpecialityRepository specialityRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User user;
    private Doctor doctor;
    private Speciality speciality;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(Role.PENDING);

        speciality = new Speciality();
        speciality.setId(1L);
        speciality.setName("Кардиология");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Д-р Иванов");
        doctor.setGp(true);
        doctor.setSpeciality(speciality);
    }

    @Test
    void register_newUsername_savesUser() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("newuser");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        authService.register(dto);

        verify(userRepository).save(argThat(u ->
                u.getUsername().equals("newuser") &&
                        u.getPassword().equals("encodedPassword") &&
                        u.getRole() == Role.PENDING
        ));
    }

    @Test
    void register_existingUsername_throwsIllegalArgument() {
        RegisterRequestDTO dto = new RegisterRequestDTO();
        dto.setUsername("existing");
        dto.setPassword("password123");

        when(userRepository.existsByUsername("existing")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void findPendingUsers_returnsPendingUsers() {
        User pending1 = new User();
        pending1.setId(2L);
        pending1.setRole(Role.PENDING);
        User pending2 = new User();
        pending2.setId(3L);
        pending2.setRole(Role.PENDING);

        when(userRepository.findByRole(Role.PENDING)).thenReturn(List.of(pending1, pending2));

        List<User> result = authService.findPendingUsers();

        assertEquals(2, result.size());
    }

    @Test
    void findPendingUsers_noPending_returnsEmpty() {
        when(userRepository.findByRole(Role.PENDING)).thenReturn(List.of());

        List<User> result = authService.findPendingUsers();

        assertEquals(0, result.size());
    }

    @Test
    void assignRole_asPatient_createsPatient() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(1L);
        dto.setRole(Role.PATIENT);
        dto.setName("Петър Петров");
        dto.setEgn("9001011234");
        dto.setGpId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        authService.assignRole(dto);

        verify(userRepository).save(argThat(u -> u.getRole() == Role.PATIENT));
        verify(patientRepository).save(argThat(p ->
                p.getName().equals("Петър Петров") &&
                        p.getEgn().equals("9001011234") &&
                        p.getGp().equals(doctor) &&
                        p.getUser().equals(user)
        ));
    }

    @Test
    void assignRole_asPatient_gpNotFound_throwsException() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(1L);
        dto.setRole(Role.PATIENT);
        dto.setName("Петър");
        dto.setGpId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.assignRole(dto));
    }

    @Test
    void assignRole_asDoctor_createsDoctor() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(1L);
        dto.setRole(Role.DOCTOR);
        dto.setName("Д-р Нов");
        dto.setGp(true);
        dto.setSpecialityId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(specialityRepository.findById(1L)).thenReturn(Optional.of(speciality));

        authService.assignRole(dto);

        verify(userRepository).save(argThat(u -> u.getRole() == Role.DOCTOR));
        verify(doctorRepository).save(argThat(d ->
                d.getName().equals("Д-р Нов") &&
                        d.isGp() &&
                        d.getSpeciality().equals(speciality) &&
                        d.getUser().equals(user)
        ));
    }

    @Test
    void assignRole_asDoctor_specialityNotFound_throwsException() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(1L);
        dto.setRole(Role.DOCTOR);
        dto.setName("Д-р Нов");
        dto.setSpecialityId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(specialityRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.assignRole(dto));
    }

    @Test
    void assignRole_userNotFound_throwsException() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(99L);
        dto.setRole(Role.PATIENT);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> authService.assignRole(dto));
    }

    @Test
    void assignRole_asAdmin_doesNotCreatePatientOrDoctor() {
        AssignRoleRequestDTO dto = new AssignRoleRequestDTO();
        dto.setUserId(1L);
        dto.setRole(Role.ADMIN);
        dto.setName("Админ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authService.assignRole(dto);

        verify(userRepository).save(argThat(u -> u.getRole() == Role.ADMIN));
        verify(patientRepository, never()).save(any());
        verify(doctorRepository, never()).save(any());
    }
}
