package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.entity.Speciality;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;

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

class SpecialityControllerIntegrationTest extends BaseIntegrationTest {
    @Test
    void list_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/specialities")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities/list"))
                .andExpect(model().attributeExists("specialities"));
    }

    @Test
    void list_asDoctor_isForbidden() throws Exception {
        mockMvc.perform(get("/specialities")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void list_asPatient_isForbidden() throws Exception {
        mockMvc.perform(get("/specialities")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void createForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/specialities/new")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities/form"))
                .andExpect(model().attributeExists("speciality"));
    }

    @Test
    void create_validDTO_redirectsToSpecialities() throws Exception {
        mockMvc.perform(post("/specialities/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "Неврология"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));

        assertTrue(specialityRepository.findAll().stream()
                .anyMatch(s -> s.getName().equals("Неврология")));
    }

    @Test
    void create_blankName_returnsForm() throws Exception {
        mockMvc.perform(post("/specialities/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void editForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/specialities/" + speciality.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities/form"))
                .andExpect(model().attributeExists("speciality", "specialityId"));
    }

    @Test
    void update_validDTO_redirectsToSpecialities() throws Exception {
        mockMvc.perform(post("/specialities/" + speciality.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", "Обновена"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));

        Speciality updated = specialityRepository.findById(speciality.getId()).orElseThrow();
        assertEquals("Обновена", updated.getName());
    }

    @Test
    void update_blankName_returnsForm() throws Exception {
        mockMvc.perform(post("/specialities/" + speciality.getId() + "/edit")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("specialities/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void delete_specialityWithoutDoctors_deletesSuccessfully() throws Exception {
        Speciality toDelete = new Speciality();
        toDelete.setName("За изтриване");
        specialityRepository.save(toDelete);

        mockMvc.perform(post("/specialities/" + toDelete.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"));

        assertFalse(specialityRepository.existsById(toDelete.getId()));
    }

    @Test
    void delete_specialityWithDoctors_redirectsWithError() throws Exception {
        mockMvc.perform(post("/specialities/" + speciality.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/specialities"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
