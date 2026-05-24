package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityIntegrationTests extends IntegrationTestSupport {

    @Test
    void privateEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/eventos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void organizerOnlySeesOwnEvents() throws Exception {
        Usuario organizadorA = saveUser("organizador-a@icesi.edu.co", "Organizador A", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario organizadorB = saveUser("organizador-b@icesi.edu.co", "Organizador B", TipoUsuario.PROFESOR, "ClaveSegura123");

        createEventForOrganizer(organizadorA, "Evento A");
        createEventForOrganizer(organizadorB, "Evento B");

        mockMvc.perform(get("/eventos/mis-eventos")
                        .header("Authorization", bearerTokenFor(organizadorA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].titulo").value("Evento A"));
    }

    @Test
    void studentCannotAccessSensitiveInscritosListing() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = saveUser("estudiante@icesi.edu.co", "Estudiante Demo", TipoUsuario.ESTUDIANTE, "ClaveSegura123");

        String eventId = createEventForOrganizer(organizador, "Evento sensible");

        mockMvc.perform(get("/eventos/" + eventId + "/inscritos")
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    private String createEventForOrganizer(Usuario organizador, String titulo) throws Exception {
        var request = baseEventRequest(
                com.icesi.uniplan.model.mongo.enums.TipoEvento.CHARLA,
                titulo,
                "Descripción del evento.",
                futureDateOffset(60),
                futureDateOffset(120),
                20,
                charlaData()
        );

        return objectMapper.readTree(mockMvc.perform(post("/eventos")
                                .header("Authorization", bearerTokenFor(organizador))
                                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                                .content(toJson(request)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString())
                .path("data")
                .path("id")
                .asText();
    }
}
