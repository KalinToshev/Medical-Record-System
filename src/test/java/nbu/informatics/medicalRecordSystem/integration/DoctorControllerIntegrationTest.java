package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.entity.Doctor;
import nbu.informatics.medicalRecordSystem.model.entity.User;
import nbu.informatics.medicalRecordSystem.model.role.Role;
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

class DoctorControllerIntegrationTest extends BaseIntegrationTest {
    @Test
    void listDoctors_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/doctors")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors/list"))
                .andExpect(model().attributeExists("doctors"));
    }

    @Test
    void listDoctors_asDoctor_isForbidden() throws Exception {
        mockMvc.perform(get("/doctors")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void listDoctors_asPatient_isForbidden() throws Exception {
        mockMvc.perform(get("/doctors")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().isForbidden())
                .andExpect(forwardedUrl("/error/403"));
    }

    @Test
    void createForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/doctors/new")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors/form"))
                .andExpect(model().attributeExists("doctor", "specialities", "users"));
    }

    @Test
    void create_validDTO_redirectsToDoctors() throws Exception {
        User newDoctorUser = createUser("doctor2", Role.DOCTOR);

        mockMvc.perform(post("/doctors/new")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Д-р Нов")
                        .param("gp", "true")
                        .param("specialityId", speciality.getId().toString())
                        .param("userId", newDoctorUser.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));

        assertTrue(doctorRepository.findAll().stream()
                .anyMatch(d -> d.getName().equals("Д-р Нов")));
    }

    @Test
    void create_missingName_returnsForm() throws Exception {
        mockMvc.perform(post("/doctors/new")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "")
                        .param("specialityId", speciality.getId().toString())
                        .param("userId", doctorUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    void editForm_asAdmin_isOk() throws Exception {
        mockMvc.perform(get("/doctors/" + doctor.getId() + "/edit")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(view().name("doctors/form"));
    }

    @Test
    void update_validDTO_redirectsToDoctors() throws Exception {
        mockMvc.perform(post("/doctors/" + doctor.getId() + "/edit")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .param("name", "Д-р Обновен")
                        .param("gp", "true")
                        .param("specialityId", speciality.getId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));

        Doctor updated = doctorRepository.findById(doctor.getId()).orElseThrow();
        assertEquals("Д-р Обновен", updated.getName());
    }

    @Test
    void delete_doctorWithoutPatients_redirectsToDoctors() throws Exception {
        User newDoctorUser = createUser("doctor3", Role.DOCTOR);
        Doctor newDoctor = new Doctor();
        newDoctor.setName("Д-р За Изтриване");
        newDoctor.setGp(false);
        newDoctor.setSpeciality(speciality);
        newDoctor.setUser(newDoctorUser);
        doctorRepository.save(newDoctor);

        mockMvc.perform(post("/doctors/" + newDoctor.getId() + "/delete")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));

        assertFalse(doctorRepository.existsById(newDoctor.getId()));
    }

    @Test
    void delete_doctorWithPatients_redirectsWithError() throws Exception {
        mockMvc.perform(post("/doctors/" + doctor.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
