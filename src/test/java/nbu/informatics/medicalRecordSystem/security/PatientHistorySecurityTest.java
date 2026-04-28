package nbu.informatics.medicalRecordSystem.security;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientHistorySecurityTest {

    @Mock
    private ExaminationRepository examinationRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientHistorySecurity patientHistorySecurity;

    private User adminUser;
    private User doctorUser;
    private User patientUser;
    private Doctor doctor;
    private Patient patient;
    private Patient otherPatient;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(Role.ADMIN);

        doctorUser = new User();
        doctorUser.setId(2L);
        doctorUser.setRole(Role.DOCTOR);

        patientUser = new User();
        patientUser.setId(3L);
        patientUser.setRole(Role.PATIENT);

        User otherPatientUser = new User();
        otherPatientUser.setId(4L);
        otherPatientUser.setRole(Role.PATIENT);

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setUser(doctorUser);

        patient = new Patient();
        patient.setId(10L);
        patient.setUser(patientUser);

        otherPatient = new Patient();
        otherPatient.setId(20L);
        otherPatient.setUser(otherPatientUser);
    }

    @Test
    void canViewHistory_admin_returnsTrueForAnyPatient() {
        assertTrue(patientHistorySecurity.canViewHistory(10L, new UserDetailsImpl(adminUser)));
    }

    @Test
    void canViewHistory_patient_returnsTrueForOwnRecord() {
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        assertTrue(patientHistorySecurity.canViewHistory(10L, new UserDetailsImpl(patientUser)));
    }

    @Test
    void canViewHistory_patient_returnsFalseForOtherPatient() {
        when(patientRepository.findById(20L)).thenReturn(Optional.of(otherPatient));
        assertFalse(patientHistorySecurity.canViewHistory(20L, new UserDetailsImpl(patientUser)));
    }

    @Test
    void canViewHistory_doctor_returnsTrueWhenExaminedPatient() {
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(examinationRepository.existsByDoctorAndPatient(doctor, patient)).thenReturn(true);

        assertTrue(patientHistorySecurity.canViewHistory(10L, new UserDetailsImpl(doctorUser)));
    }

    @Test
    void canViewHistory_doctor_returnsFalseWhenNotExaminedPatient() {
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByUser(doctorUser)).thenReturn(Optional.of(doctor));
        when(examinationRepository.existsByDoctorAndPatient(doctor, patient)).thenReturn(false);

        assertFalse(patientHistorySecurity.canViewHistory(10L, new UserDetailsImpl(doctorUser)));
    }

    @Test
    void canViewHistory_nullPrincipal_returnsFalse() {
        assertFalse(patientHistorySecurity.canViewHistory(10L, null));
    }

    @Test
    void canViewHistory_nullPatientId_returnsFalse() {
        assertFalse(patientHistorySecurity.canViewHistory(null, new UserDetailsImpl(adminUser)));
    }

    @Test
    void canViewHistory_pendingRole_returnsFalse() {
        User pendingUser = new User();
        pendingUser.setId(5L);
        pendingUser.setRole(Role.PENDING);

        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));

        assertFalse(patientHistorySecurity.canViewHistory(10L, new UserDetailsImpl(pendingUser)));
    }
}
