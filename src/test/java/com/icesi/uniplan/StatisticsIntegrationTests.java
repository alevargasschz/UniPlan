package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatisticsIntegrationTests extends IntegrationTestSupport {

    @Test
    void createsStatisticsForPublishedEvent() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");

        String eventId = createEventViaHttp(organizador, "Evento estadístico", TipoEvento.CHARLA, charlaData());

        assertThat(eventoEstadisticaRepository.findById(eventId)).isPresent();
    }

    @Test
    void updatesStatisticsWhenStudentRegistersAndCancels() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = Usuario.builder()
                .correo("estudiante@icesi.edu.co")
                .nombre("Estudiante Demo")
                .tipo(TipoUsuario.ESTUDIANTE)
                .contrasena(passwordEncoder.encode("ClaveSegura123"))
                .datosEspecificos(new Estudiante("A00123456"))
                .build();
        usuarioRepository.save(estudiante);

        String eventId = createEventViaHttp(organizador, "Evento con estadística", TipoEvento.CHARLA, charlaData());

        mockMvc.perform(post("/inscripciones/" + eventId)
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        var stats = eventoEstadisticaRepository.findById(eventId).orElseThrow();
        assertThat(stats.getTotalInscritos()).isEqualTo(1);
        assertThat(stats.getTotalCancelaciones()).isZero();
        assertThat(stats.getPorcentajeOcupacion()).isEqualTo(100.0);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/inscripciones/" + eventId)
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        stats = eventoEstadisticaRepository.findById(eventId).orElseThrow();
        assertThat(stats.getTotalInscritos()).isZero();
        assertThat(stats.getTotalCancelaciones()).isEqualTo(1);
        assertThat(stats.getPorcentajeOcupacion()).isEqualTo(0.0);
    }

    private String createEventViaHttp(Usuario organizador, String titulo, TipoEvento tipo, com.icesi.uniplan.dto.request.DatosEspecificosRequest datos) throws Exception {
        var request = baseEventRequest(
                tipo,
                titulo,
                "Descripción del evento.",
                futureDateOffset(60),
                futureDateOffset(120),
                1,
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
}
