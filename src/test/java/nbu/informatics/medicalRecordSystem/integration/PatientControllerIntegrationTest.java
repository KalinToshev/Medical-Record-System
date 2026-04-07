package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.entity.Examination;
import nbu.informatics.medicalRecordSystem.model.entity.Patient;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.PaidBy;
import nbu.informatics.medicalRecordSystem.model.role.Role;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class PatientControllerIntegrationTest extends BaseIntegrationTest {
    @Test
    void list_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/patients")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/list"))
                .andExpect(model().attributeExists("patients"));
    }

    @Test
    void list_asDoctor_isForbidden() throws Exception {
        mockMvc.perform(get("/patients")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void list_asPatient_isForbidden() throws Exception {
        mockMvc.perform(get("/patients")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void createForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/patients/new")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeExists("patient", "gps", "users"));
    }

    @Test
    void create_validDTO_redirectsToPatients() throws Exception {
        User newPatientUser = createUser("patient2", Role.PATIENT);

        mockMvc.perform(post("/patients/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "Иван Иванов")
                        .param("egn", "8501011234")
                        .param("gpId", doctor.getId().toString())
                        .param("userId", newPatientUser.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        assertTrue(patientRepository.findAll().stream()
                .anyMatch(p -> p.getName().equals("Иван Иванов")));
    }

    @Test
    void create_missingName_returnsForm() throws Exception {
        mockMvc.perform(post("/patients/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "")
                        .param("egn", "8501011234")
                        .param("gpId", doctor.getId().toString())
                        .param("userId", patientUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void create_invalidEgn_returnsForm() throws Exception {
        mockMvc.perform(post("/patients/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "Иван")
                        .param("egn", "123")
                        .param("gpId", doctor.getId().toString())
                        .param("userId", patientUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void editForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().attributeExists("patient", "patientId", "patientUsername", "gps"));
    }

    @Test
    void update_validDTO_redirectsToPatients() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "Обновено Име")
                        .param("egn", "9001011234")
                        .param("gpId", doctor.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        Patient updated = patientRepository.findById(patient.getId()).orElseThrow();
        assertEquals("Обновено Име", updated.getName());
    }

    @Test
    void update_blankName_returnsForm() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "")
                        .param("egn", "9001011234")
                        .param("gpId", doctor.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("patients/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void delete_patientWithoutExaminations_deletesSuccessfully() throws Exception {
        User tempUser = createUser("temppatient", Role.PATIENT);
        Patient toDelete = new Patient();
        toDelete.setName("За Изтриване");
        toDelete.setEgn("7701011234");
        toDelete.setGp(doctor);
        toDelete.setUser(tempUser);
        patientRepository.save(toDelete);

        mockMvc.perform(post("/patients/" + toDelete.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"));

        assertFalse(patientRepository.existsById(toDelete.getId()));
    }

    @Test
    void delete_patientWithExaminations_redirectsWithError() throws Exception {
        Examination exam = new Examination();
        exam.setDateTime(LocalDateTime.now());
        exam.setDoctor(doctor);
        exam.setPatient(patient);
        exam.setDiagnosis(diagnosis);
        exam.setTreatment("Тест");
        exam.setPrice(new BigDecimal("10.00"));
        exam.setPaidBy(PaidBy.PATIENT);
        examinationRepository.save(exam);

        mockMvc.perform(post("/patients/" + patient.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients"))
                .andExpect(flash().attributeExists("errorMessage"));

        assertTrue(patientRepository.existsById(patient.getId()));
    }
}
