package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PatientHistoryControllerIntegrationTest extends BaseIntegrationTest {

    private User doctorUser2;
    private Patient patient2;

    @BeforeEach
    void setUpExtra() {
        doctorUser2 = createUser("doctor2", Role.DOCTOR);
        Doctor doctor2 = new Doctor();
        doctor2.setName("Д-р Петрова");
        doctor2.setGp(false);
        doctor2.setSpeciality(speciality);
        doctor2.setUser(doctorUser2);
        doctorRepository.save(doctor2);

        User patientUser2 = createUser("patient2", Role.PATIENT);
        patient2 = new Patient();
        patient2.setName("Мария Иванова");
        patient2.setEgn("8506152345");
        patient2.setGp(doctor);
        patient2.setUser(patientUser2);
        patientRepository.save(patient2);

        Examination exam = new Examination();
        exam.setDateTime(LocalDateTime.now());
        exam.setDoctor(doctor);
        exam.setPatient(patient);
        exam.setDiagnosis(diagnosis);
        exam.setTreatment("Почивка");
        exam.setPrice(new BigDecimal("50.00"));
        exam.setPaidBy(PaidBy.PATIENT);
        examinationRepository.save(exam);
    }

    @Test
    void history_admin_returns200() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/history")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/history"))
                .andExpect(model().attributeExists("patient", "examinations", "insurances"));
    }

    @Test
    void history_doctorWhoExaminedPatient_returns200() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/history")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/history"));
    }

    @Test
    void history_doctorWhoDidNotExaminePatient_returns403() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/history")
                        .with(user(new UserDetailsImpl(doctorUser2))))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void history_patientViewingOwnHistory_returns200() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/history")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/history"));
    }

    @Test
    void history_patientViewingOtherPatient_returns403() throws Exception {
        mockMvc.perform(get("/patients/" + patient2.getId() + "/history")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));
    }

    @Test
    void history_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/history"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
