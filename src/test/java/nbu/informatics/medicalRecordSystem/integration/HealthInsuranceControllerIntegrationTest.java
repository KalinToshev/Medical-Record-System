package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.entity.HealthInsurance;
import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import nbu.informatics.medicalRecordSystem.repository.HealthInsuranceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class HealthInsuranceControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private HealthInsuranceRepository healthInsuranceRepository;

    @Test
    void list_asAdmin_showsInsurancesForPatient() throws Exception {
        mockMvc.perform(get("/patients/" + patient.getId() + "/insurances")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("insurances/list"))
                .andExpect(model().attributeExists("patient", "insurances", "insurance"));
    }

    @Test
    void create_validDTO_redirectsToInsurances() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/insurances/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("year", "2026")
                        .param("month", "3")
                        .param("paid", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patient.getId() + "/insurances"));

        assertTrue(healthInsuranceRepository.existsByPatientAndYearAndMonth(patient, 2026, 3));
    }

    @Test
    void create_duplicateEntry_showsError() throws Exception {
        HealthInsurance existing = new HealthInsurance();
        existing.setPatient(patient);
        existing.setYear(2026);
        existing.setMonth(5);
        existing.setPaid(true);
        healthInsuranceRepository.save(existing);

        mockMvc.perform(post("/patients/" + patient.getId() + "/insurances/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("year", "2026")
                        .param("month", "5")
                        .param("paid", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("insurances/list"))
                .andExpect(model().attributeExists("duplicateError"));
    }

    @Test
    void create_invalidMonth_returnsForm() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/insurances/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("year", "2026")
                        .param("month", "13")
                        .param("paid", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("insurances/list"))
                .andExpect(model().hasErrors());
    }

    @Test
    void create_missingYear_returnsForm() throws Exception {
        mockMvc.perform(post("/patients/" + patient.getId() + "/insurances/new")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf())
                        .param("year", "")
                        .param("month", "3")
                        .param("paid", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("insurances/list"))
                .andExpect(model().hasErrors());
    }

    @Test
    void delete_validEntry_deletesSuccessfully() throws Exception {
        HealthInsurance insurance = new HealthInsurance();
        insurance.setPatient(patient);
        insurance.setYear(2026);
        insurance.setMonth(7);
        insurance.setPaid(true);
        healthInsuranceRepository.save(insurance);

        mockMvc.perform(post("/patients/" + patient.getId() + "/insurances/" + insurance.getId() + "/delete")
                        .with(user(new UserDetailsImpl(adminUser)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patients/" + patient.getId() + "/insurances"));

        assertFalse(healthInsuranceRepository.existsById(insurance.getId()));
    }

    @Test
    void list_withMultipleInsurances_showsAll() throws Exception {
        for (int m = 1; m <= 3; m++) {
            HealthInsurance ins = new HealthInsurance();
            ins.setPatient(patient);
            ins.setYear(2026);
            ins.setMonth(m);
            ins.setPaid(true);
            healthInsuranceRepository.save(ins);
        }

        mockMvc.perform(get("/patients/" + patient.getId() + "/insurances")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("insurances"));

        assertEquals(3, healthInsuranceRepository.findByPatient(patient).size());
    }
}
