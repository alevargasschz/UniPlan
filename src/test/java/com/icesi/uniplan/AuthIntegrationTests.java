package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTests extends IntegrationTestSupport {

    @Test
    void loginPageReturnsLoginForm() throws Exception {
        mockMvc.perform(get("/public/auth/login"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @WithMockUser(username = "estudiante@icesi.edu.co", roles = "ESTUDIANTE")
    void authenticatedUserCanAccessProtectedEndpoints() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        saveUser("estudiante@icesi.edu.co", "Estudiante Demo", TipoUsuario.ESTUDIANTE, "ClaveSegura123");

        mockMvc.perform(post("/public/auth/login")
                        .with(csrf())
                        .param("correo", "estudiante@icesi.edu.co")
                        .param("contrasena", "ClaveEquivocada123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/login?error=true")); // ← cambiado
    }

    @Test
    void loginRejectsUnknownUser() throws Exception {
        mockMvc.perform(post("/public/auth/login")
                        .with(csrf())
                        .param("correo", "no-existe@icesi.edu.co")
                        .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/login?error=true")); // ← cambiado
    }
}
