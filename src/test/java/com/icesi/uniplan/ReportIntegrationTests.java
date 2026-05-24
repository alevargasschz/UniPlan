package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportIntegrationTests extends IntegrationTestSupport {

    @Test
    void topEventsReportShowsMostOccupiedEventsFirst() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = saveStudentUser("estudiante@icesi.edu.co", "Estudiante Demo", "2024001", "ClaveSegura123");

        String popularEventId = createEventViaHttp(organizador, "Evento popular", TipoEvento.CHARLA, charlaData(), 2);
        String coldEventId = createEventViaHttp(organizador, "Evento frío", TipoEvento.CHARLA, charlaData(), 10);

        inscribirEstudiante(estudiante, popularEventId);

        mockMvc.perform(get("/reportes/top-eventos?limite=5")
                        .header("Authorization", bearerTokenFor(organizador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventoId").value(popularEventId))
                .andExpect(jsonPath("$.data[0].porcentajeOcupacion").value(50.0))
                .andExpect(jsonPath("$.data[1].eventoId").value(coldEventId));
    }

    @Test
    void statisticsReportReturnsAllAggregatedEvents() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = saveStudentUser("estudiante@icesi.edu.co", "Estudiante Demo", "2024001", "ClaveSegura123");

        String firstEventId = createEventViaHttp(organizador, "Evento estadístico 1", TipoEvento.CHARLA, charlaData(), 2);
        createEventViaHttp(organizador, "Evento estadístico 2", TipoEvento.CHARLA, charlaData(), 10);

        inscribirEstudiante(estudiante, firstEventId);

        mockMvc.perform(get("/reportes/estadisticas")
                        .header("Authorization", bearerTokenFor(organizador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].titulo").value("Evento estadístico 1"))
                .andExpect(jsonPath("$.data[1].titulo").value("Evento estadístico 2"));
    }

    private String createEventViaHttp(Usuario organizador, String titulo, TipoEvento tipo, com.icesi.uniplan.dto.request.DatosEspecificosRequest datos, int maxAsistentes) throws Exception {
        var request = baseEventRequest(
                tipo,
                titulo,
                "Descripción del evento.",
                futureDateOffset(60),
                futureDateOffset(120),
                maxAsistentes,
                datos
        );

        MvcResult result = mockMvc.perform(post("/eventos")
                        .header("Authorization", bearerTokenFor(organizador))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asText();
    }

    private void inscribirEstudiante(Usuario estudiante, String eventoId) throws Exception {
        mockMvc.perform(post("/inscripciones/" + eventoId)
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
