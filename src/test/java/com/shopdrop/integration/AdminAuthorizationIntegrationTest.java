package com.shopdrop.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies the ADMIN-only sections stay locked down: SecurityConfig, CustomUserDetailsService
 * and the seeded accounts from DataLoader all have to work together correctly for these to pass.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AdminAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousUser_isRedirectedToLoginForAdminPages() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void regularUser_isForbiddenFromAdminProductPages() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void regularUser_isForbiddenFromAdminOrderPages() throws Exception {
        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@shopdrop.com")
    void adminUser_canAccessAdminProductPages() throws Exception {
        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void regularUser_canAccessTheirOwnAccountPage() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().isOk());
    }

    @Test
    void anonymousUser_isRedirectedToLoginForAccountPage() throws Exception {
        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithUserDetails("admin@shopdrop.com")
    void adminUser_isForbiddenFromCart() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@shopdrop.com")
    void adminUser_isForbiddenFromCheckout() throws Exception {
        mockMvc.perform(get("/checkout"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void regularUser_canAccessCart() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk());
    }

    @Test
    @WithUserDetails("demo@shopdrop.com")
    void regularUser_isForbiddenFromAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin@shopdrop.com")
    void adminUser_canAccessTheAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }
}
