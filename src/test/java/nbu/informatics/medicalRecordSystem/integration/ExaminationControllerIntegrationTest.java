package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class ExaminationControllerIntegrationTest extends BaseIntegrationTest {

    // --- Права на достъп ---

    @Test
    void listExaminations_asAdmin_seesAll() throws Exception {
        mockMvc.perform(get("/examinations")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("examinations/list"))
                .andExpect(model().attributeExists("examinations"));
    }

    @Test
    void listExaminations_asDoctor_seesOnlyOwn() throws Exception {
        mockMvc.perform(get("/examinations")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("examinations/list"));
    }

    @Test
    void createForm_asDoctor_isOk() throws Exception {
        mockMvc.perform(get("/examinations/new")
                        .with(user("doctor1").roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(view().name("examinations/form"));
    }

    @Test
    void createForm_asAdmin_isForbidden() throws Exception {
        mockMvc.perform(get("/examinations/new")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void createForm_asPatient_isForbidden() throws Exception {
        mockMvc.perform(get("/examinations/new")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    // --- Create ---

    @Test
    void create_validDTO_savesExamination() throws Exception {
        mockMvc.perform(post("/examinations/new")
                        .with(user(new UserDetailsImpl(doctorUser)))
                        .with(csrf())
                        .param("patientId", patient.getId().toString())
                        .param("diagnosisId", diagnosis.getId().toString())
                        .param("treatment", "Почивка")
                        .param("price", "50.00")
                        .param("issueSickLeave", "false"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examinations"));

        assertEquals(1, examinationRepository.findAll().size());
    }

    @Test
    void create_missingPrice_returnsForm() throws Exception {
        mockMvc.perform(post("/examinations/new")
                        .with(SecurityMockMvcRequestPostProcessors.user(
                                new UserDetailsImpl(doctorUser)))
                        .with(csrf())
                        .param("patientId", patient.getId().toString())
                        .param("diagnosisId", diagnosis.getId().toString())
                        .param("price", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("examinations/form"))
                .andExpect(model().hasErrors());
    }

    // --- Филтриране според роля ---

    @Test
    void listExaminations_asPatient_seesOnlyOwn() throws Exception {
        mockMvc.perform(get("/examinations")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isOk())
                .andExpect(model().attribute("examinations", hasSize(0)));
    }
}
