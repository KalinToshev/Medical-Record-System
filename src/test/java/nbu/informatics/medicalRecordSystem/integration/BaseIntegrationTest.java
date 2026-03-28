package nbu.informatics.medicalRecordSystem.integration;

import jakarta.transaction.Transactional;
import nbu.informatics.medicalRecordSystem.model.entity.Diagnosis;
import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.repository.DiagnosisRepository;
import nbu.informatics.medicalRecordSystem.repository.DoctorRepository;
import nbu.informatics.medicalRecordSystem.repository.ExaminationRepository;
import nbu.informatics.medicalRecordSystem.repository.PatientRepository;
import nbu.informatics.medicalRecordSystem.repository.SpecialityRepository;
import nbu.informatics.medicalRecordSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected DoctorRepository doctorRepository;
    @Autowired
    protected PatientRepository patientRepository;
    @Autowired
    protected SpecialityRepository specialityRepository;
    @Autowired
    protected DiagnosisRepository diagnosisRepository;
    @Autowired
    protected ExaminationRepository examinationRepository;
    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected User adminUser;
    protected User doctorUser;
    protected User patientUser;
    protected Doctor doctor;
    protected Patient patient;
    protected Speciality speciality;
    protected Diagnosis diagnosis;

    @BeforeEach
    void setUpData() {
        adminUser = createUser("admin", Role.ADMIN);
        doctorUser = createUser("doctor1", Role.DOCTOR);
        patientUser = createUser("patient1", Role.PATIENT);

        speciality = new Speciality();
        speciality.setName("Кардиология");
        specialityRepository.save(speciality);

        diagnosis = new Diagnosis();
        diagnosis.setName("Грип");
        diagnosisRepository.save(diagnosis);

        doctor = new Doctor();
        doctor.setName("Д-р Иванов");
        doctor.setGp(true);
        doctor.setSpeciality(speciality);
        doctor.setUser(doctorUser);
        doctorRepository.save(doctor);

        patient = new Patient();
        patient.setName("Петър Петров");
        patient.setEgn("9001011234");
        patient.setGp(doctor);
        patient.setUser(patientUser);
        patientRepository.save(patient);
    }

    protected User createUser(String username, Role role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(role);
        return userRepository.save(user);
    }
}
