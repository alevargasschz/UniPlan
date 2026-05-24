package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrganizerIntegrationTests extends IntegrationTestSupport {

    @Test
    void organizerCanListRegisteredStudentsForItsEvent() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = Usuario.builder()
                .correo("estudiante@icesi.edu.co")
                .nombre("Estudiante Demo")
                .tipo(TipoUsuario.ESTUDIANTE)
                .contrasena(passwordEncoder.encode("ClaveSegura123"))
                .datosEspecificos(new Estudiante("A00123456"))
                .build();
        usuarioRepository.save(estudiante);

        String eventId = createEventViaHttp(organizador, "Evento con inscritos", futureDateOffset(60), futureDateOffset(120));

        mockMvc.perform(post("/inscripciones/" + eventId)
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/eventos/" + eventId + "/inscritos")
                        .header("Authorization", bearerTokenFor(organizador)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].correo").value("estudiante@icesi.edu.co"))
                .andExpect(jsonPath("$.data[0].codigoEstudiante").value("A00123456"));
    }

    @Test
    void exportCsvContainsNameCodeAndEmailForRegisteredStudents() throws Exception {
        Usuario organizador = saveUser("profesor@icesi.edu.co", "Profesor Demo", TipoUsuario.PROFESOR, "ClaveSegura123");
        Usuario estudiante = Usuario.builder()
                .correo("estudiante@icesi.edu.co")
                .nombre("Estudiante Demo")
                .tipo(TipoUsuario.ESTUDIANTE)
                .contrasena(passwordEncoder.encode("ClaveSegura123"))
                .datosEspecificos(new Estudiante("A00123456"))
                .build();
        usuarioRepository.save(estudiante);

        String eventId = createEventViaHttp(organizador, "Evento exportable", futureDateOffset(60), futureDateOffset(120));

        mockMvc.perform(post("/inscripciones/" + eventId)
                        .header("Authorization", bearerTokenFor(estudiante)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/eventos/" + eventId + "/inscritos/export")
                        .header("Authorization", bearerTokenFor(organizador)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("inscritos_" + eventId + ".csv")))
                .andExpect(content().contentTypeCompatibleWith("text/csv;charset=UTF-8"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nombre,Codigo Estudiante,Correo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Estudiante Demo")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("A00123456")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("estudiante@icesi.edu.co")));
    }

    private String createEventViaHttp(Usuario organizador, String titulo, java.util.Date inicio, java.util.Date fin) throws Exception {
        var request = baseEventRequest(
                com.icesi.uniplan.model.mongo.enums.TipoEvento.CHARLA,
                titulo,
                "Descripción del evento.",
                inicio,
                fin,
                20,
                charlaData()
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
