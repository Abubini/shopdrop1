package com.shopdrop.integration;

import com.shopdrop.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Exercises registration and login together, through the real Spring MVC + Spring Security
 * stack and a real (in-memory) database — i.e. several components working together, not a
 * single class in isolation.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void registerThenLogIn_succeeds() throws Exception {
        String email = "integration.user@shopdrop.com";

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Integration User")
                        .param("email", email)
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        assertTrue(userRepository.findByEmail(email).isPresent());

        mockMvc.perform(formLogin("/login").user(email).password("password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account"));
    }

    @Test
    void registerWithMismatchedPasswords_reRendersFormWithoutCreatingAnAccount() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Bad Match")
                        .param("email", "mismatch@shopdrop.com")
                        .param("password", "password123")
                        .param("confirmPassword", "different123"))
                .andExpect(status().isOk());

        assertTrue(userRepository.findByEmail("mismatch@shopdrop.com").isEmpty());
    }

    @Test
    void registerWithAlreadyUsedEmail_reRendersFormWithError() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Demo Shopper")
                        .param("email", "demo@shopdrop.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk());
    }

    @Test
    void loginWithWrongPassword_isRejected() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin@shopdrop.com").password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void adminLogin_redirectsToTheAdminDashboardNotTheAccountPage() throws Exception {
        mockMvc.perform(formLogin("/login").user("admin@shopdrop.com").password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"));
    }
}