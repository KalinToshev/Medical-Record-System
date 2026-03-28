package nbu.informatics.medicalRecordSystem.integration;

import nbu.informatics.medicalRecordSystem.model.security.UserDetailsImpl;
import org.junit.jupiter.api.Test;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void loginPage_isAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void login_withValidAdminCredentials_redirectsToDashboard() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void login_withInvalidCredentials_redirectsToLoginWithError() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "admin")
                        .param("password", "wrongpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void dashboard_adminUser_redirectsToDoctors() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .with(user(new UserDetailsImpl(adminUser))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/doctors"));
    }

    @Test
    void dashboard_doctorUser_redirectsToExaminations() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .with(user(new UserDetailsImpl(doctorUser))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examinations"));
    }

    @Test
    void dashboard_patientUser_redirectsToExaminations() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .with(user(new UserDetailsImpl(patientUser))))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examinations"));
    }

    @Test
    void logout_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    @Test
    void protectedPage_withoutAuth_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/doctors"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
