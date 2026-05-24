package com.icesi.uniplan;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RegistrationIntegrationTests extends IntegrationTestSupport {

    @Test
    void registersExistingStudentSuccessfully() throws Exception {
        Student estudiante = seedStudent("A00123456", "student@icesi.edu.co");

        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil(estudiante.getId());
        request.setCorreo(estudiante.getEmail());
        request.setContrasena("ClaveSegura123");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.correo").value(estudiante.getEmail()))
                .andExpect(jsonPath("$.data.tipo").value(TipoUsuario.ESTUDIANTE.name()));

        assertThat(usuarioRepository.existsByCorreo(estudiante.getEmail())).isTrue();
    }

    @Test
    void rejectsStudentThatDoesNotExistInInstitutionalDatabase() throws Exception {
        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil("A99999999");
        request.setCorreo("missing@icesi.edu.co");
        request.setContrasena("ClaveSegura123");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        Student estudiante = seedStudent("A00123457", "duplicate@icesi.edu.co");

        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil(estudiante.getId());
        request.setCorreo(estudiante.getEmail());
        request.setContrasena("ClaveSegura123");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/public/auth/registro/estudiante")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void storesPasswordUsingBCryptHash() throws Exception {
        Student estudiante = seedStudent("A00123458", "secure@icesi.edu.co");

        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil(estudiante.getId());
        request.setCorreo(estudiante.getEmail());
        request.setContrasena("ClaveSegura123");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated());

        Usuario guardado = usuarioRepository.findByCorreo(estudiante.getEmail()).orElseThrow();
        assertThat(guardado.getContrasena()).isNotEqualTo("ClaveSegura123");
        assertThat(passwordEncoder.matches("ClaveSegura123", guardado.getContrasena())).isTrue();
        assertThat(guardado.getDatosEspecificos()).isInstanceOf(Estudiante.class);
    }
}
