package com.icesi.uniplan;

import com.icesi.uniplan.dto.request.LoginRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTests extends IntegrationTestSupport {

    @Test
    void loginSucceedsAndReturnsUsableJwtToken() throws Exception {
        Usuario estudiante = saveUser("estudiante@icesi.edu.co", "Estudiante Demo", TipoUsuario.ESTUDIANTE, "ClaveSegura123");

        LoginRequest request = new LoginRequest();
        request.setCorreo(estudiante.getCorreo());
        request.setContrasena("ClaveSegura123");

        MvcResult result = mockMvc.perform(post("/public/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.correo").value(estudiante.getCorreo()))
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("token")
                .asText();

        assertThat(token).isNotBlank();
        assertThat(jwtUtil.extractCorreo(token)).isEqualTo(estudiante.getCorreo());
        assertThat(jwtUtil.extractRole(token)).isEqualTo(TipoUsuario.ESTUDIANTE.name());

        mockMvc.perform(get("/reportes/mis-inscripciones")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void loginRejectsIncorrectPassword() throws Exception {
        saveUser("estudiante@icesi.edu.co", "Estudiante Demo", TipoUsuario.ESTUDIANTE, "ClaveSegura123");

        LoginRequest request = new LoginRequest();
        request.setCorreo("estudiante@icesi.edu.co");
        request.setContrasena("ClaveEquivocada123");

        mockMvc.perform(post("/uniplan/public/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void loginRejectsUnknownUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setCorreo("no-existe@icesi.edu.co");
        request.setContrasena("ClaveSegura123");

        mockMvc.perform(post("/uniplan/public/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
