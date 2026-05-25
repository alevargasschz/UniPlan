package com.icesi.uniplan;

import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTests extends IntegrationTestSupport {

    @Test
    void privateEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/login"));
    }

    @Test
    void publicEndpointsDoNotRequireAuthentication() throws Exception {
        mockMvc.perform(get("/public/auth/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "estudiante@icesi.edu.co", roles = "ESTUDIANTE")
    void studentCannotAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/organizadores/crear"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "profesor@icesi.edu.co", roles = "BIENESTAR")
    void organizerCanAccessAdminEndpoints() throws Exception {
        mockMvc.perform(get("/admin/organizadores/crear"))
                .andExpect(status().isOk());
    }
}
