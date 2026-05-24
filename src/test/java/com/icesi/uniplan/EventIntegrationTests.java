package com.icesi.uniplan;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.DatosEspecificosRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.EventoEstadistica;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EventIntegrationTests extends IntegrationTestSupport {

    @Test
    void organizerCanCreateEventAndStatisticsAreGenerated() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        CrearEventoRequest request = baseEventRequest(
                TipoEvento.TALLER,
                "Taller de Spring Boot",
                "Sesión práctica de APIs.",
                futureDateOffset(60),
                futureDateOffset(120),
                30,
                tallerData("SEMESTRE:3")
        );

        MvcResult result = mockMvc.perform(post("/eventos")
                        .header("Authorization", bearerTokenFor(organizador))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.titulo").value("Taller de Spring Boot"))
                .andExpect(jsonPath("$.data.tipo").value("TALLER"))
                .andReturn();

        String eventId = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data")
                .path("id")
                .asText();

        assertThat(eventId).isNotBlank();
        assertThat(eventoRepository.findById(eventId)).isPresent();
        assertThat(eventoEstadisticaRepository.findById(eventId)).isPresent();

        EventoEstadistica stats = eventoEstadisticaRepository.findById(eventId).orElseThrow();
        assertThat(stats.getTotalInscritos()).isZero();
        assertThat(stats.getPorcentajeOcupacion()).isEqualTo(0.0);
    }

    @Test
    void studentCannotCreateEvent() throws Exception {
        Usuario estudiante = saveUser("estudiante@icesi.edu.co", "Estudiante Demo", TipoUsuario.ESTUDIANTE, "ClaveSegura123");

        CrearEventoRequest request = baseEventRequest(
                TipoEvento.CHARLA,
                "Charla de ejemplo",
                "Descripción de charla.",
                futureDateOffset(60),
                futureDateOffset(120),
                20,
                charlaData()
        );

        mockMvc.perform(post("/eventos")
                        .header("Authorization", bearerTokenFor(estudiante))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsEventsStartingInThePast() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        CrearEventoRequest request = baseEventRequest(
                TipoEvento.CHARLA,
                "Charla pasada",
                "Descripción.",
                pastDateOffset(30),
                pastDateOffset(10),
                20,
                charlaData()
        );

        mockMvc.perform(post("/eventos")
                        .header("Authorization", bearerTokenFor(organizador))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsEventsWithZeroCapacity() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        CrearEventoRequest request = baseEventRequest(
                TipoEvento.CHARLA,
                "Charla sin cupos",
                "Descripción válida.",
                futureDateOffset(60),
                futureDateOffset(120),
                0,
                charlaData()
        );

        mockMvc.perform(post("/eventos")
                        .header("Authorization", bearerTokenFor(organizador))
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void createsUniqueEventIds() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        String firstId = createEvent(organizador, "Primer evento", TipoEvento.TALLER, tallerData("SEMESTRE:1"), futureDateOffset(60), futureDateOffset(120));
        String secondId = createEvent(organizador, "Segundo evento", TipoEvento.CHARLA, charlaData(), futureDateOffset(120), futureDateOffset(180));

        assertThat(firstId).isNotBlank();
        assertThat(secondId).isNotBlank();
        assertThat(firstId).isNotEqualTo(secondId);
    }

    @Test
    @Disabled("Test requires manual state updates to simulate finished events")
    void filtersCatalogByTypeStateAndDateRange() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        createEvent(organizador, "Taller filtrado", TipoEvento.TALLER, tallerData("SEMESTRE:2"), futureDateOffset(60), futureDateOffset(120));
        createEvent(organizador, "Charla filtrada", TipoEvento.CHARLA, charlaData(), futureDateOffset(240), futureDateOffset(300));
        createEvent(organizador, "Taller finalizado", TipoEvento.TALLER, tallerData("SEMESTRE:4"), futureDateOffset(60), futureDateOffset(120));

        mockMvc.perform(get("/eventos?tipo=TALLER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/eventos?estado=PROGRAMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        mockMvc.perform(get("/eventos?inicio=" + isoDate(futureDateOffset(30)) + "&fin=" + isoDate(futureDateOffset(180))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void returnsEventDetails() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        String eventId = createEvent(organizador, "Detalle de evento", TipoEvento.TALLER, tallerData("SEMESTRE:1"), futureDateOffset(60), futureDateOffset(120));

        mockMvc.perform(get("/eventos/" + eventId)
                        .header("Authorization", bearerTokenFor(organizador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(eventId))
                .andExpect(jsonPath("$.data.titulo").value("Detalle de evento"))
                .andExpect(jsonPath("$.data.tipo").value("TALLER"));
    }

    private String createEvent(Usuario organizador, String titulo, TipoEvento tipo, DatosEspecificosRequest datos, java.util.Date inicio, java.util.Date fin) throws Exception {
        CrearEventoRequest request = baseEventRequest(tipo, titulo, "Descripción de prueba.", inicio, fin, 10, datos);

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
}
