package com.icesi.uniplan;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.embedded.LiderEstudiantil;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.test.context.support.WithMockUser;

class RegistrationIntegrationTests extends IntegrationTestSupport {

    @Test
    void registersExistingStudentSuccessfully() throws Exception {
        Student estudiante = seedStudent("A00123456", "student@icesi.edu.co");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                .with(csrf())
                .param("codigoEstudiantil", estudiante.getId())
                .param("correo", estudiante.getEmail())
                .param("nombre", "Student Name")
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/login"));

        assertThat(usuarioRepository.existsByCorreo(estudiante.getEmail())).isTrue();
    }

    @Test
    void rejectsStudentThatDoesNotExistInInstitutionalDatabase() throws Exception {
        mockMvc.perform(post("/public/auth/registro/estudiante")
                .with(csrf())
                .param("codigoEstudiantil", "A99999999")
                .param("correo", "missing@icesi.edu.co")
                .param("nombre", "Missing Student")
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/registro/estudiante"));
    }

    @Test
    void rejectsDuplicateRegistration() throws Exception {
        Student estudiante = seedStudent("A00123457", "duplicate@icesi.edu.co");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                .with(csrf())
                .param("codigoEstudiantil", estudiante.getId())
                .param("correo", estudiante.getEmail())
                .param("nombre", "Duplicate Student")
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(post("/public/auth/registro/estudiante")
                .with(csrf())
                .param("codigoEstudiantil", estudiante.getId())
                .param("correo", estudiante.getEmail())
                .param("nombre", "Duplicate Student")
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/public/auth/registro/estudiante"));
    }

    @Test
    void storesPasswordUsingBCryptHash() throws Exception {
        Student estudiante = seedStudent("A00123458", "secure@icesi.edu.co");

        mockMvc.perform(post("/public/auth/registro/estudiante")
                .with(csrf())
                .param("codigoEstudiantil", estudiante.getId())
                .param("correo", estudiante.getEmail())
                .param("nombre", "Secure Student")
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection());

        Usuario guardado = usuarioRepository.findByCorreo(estudiante.getEmail()).orElseThrow();
        assertThat(guardado.getContrasena()).isNotEqualTo("ClaveSegura123");
        assertThat(passwordEncoder.matches("ClaveSegura123", guardado.getContrasena())).isTrue();
        assertThat(guardado.getDatosEspecificos()).isInstanceOf(Estudiante.class);
    }

    @Test
    void registrationPageReturnsForm() throws Exception {
        mockMvc.perform(get("/public/auth/registro/estudiante"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@icesi.edu.co", roles = "BIENESTAR")
    void registersStudentLeaderUsingSemesterFromPostgres() throws Exception {
        Student estudiante = seedStudent("A00123999", "lider@icesi.edu.co");
        seedCompletedEnrollmentForStudent(estudiante, "ISIS101", "2024-1");

        mockMvc.perform(post("/admin/organizadores/crear")
                .with(csrf())
                .param("correo", estudiante.getEmail())
                .param("contrasena", "ClaveSegura123")
                .param("tipo", "LIDER_ESTUDIANTIL")
                .param("representacion", "Consejo Estudiantil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/organizadores"));

        Usuario guardado = usuarioRepository.findByCorreo(estudiante.getEmail()).orElseThrow();
        assertThat(guardado.getTipo()).isEqualTo(TipoUsuario.LIDER_ESTUDIANTIL);
        assertThat(guardado.getDatosEspecificos()).isInstanceOf(LiderEstudiantil.class);
        LiderEstudiantil lider = (LiderEstudiantil) guardado.getDatosEspecificos();
        assertThat(lider.getPrograma()).isEqualTo("Ingeniería de Sistemas");
        assertThat(lider.getSemestre()).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "admin@icesi.edu.co", roles = "BIENESTAR")
    void upgradesExistingStudentToStudentLeader() throws Exception {
        Student estudiante = seedStudent("A00123998", "upgrade.lider@icesi.edu.co");
        seedCompletedEnrollmentForStudent(estudiante, "ISIS111", "2024-1");
        saveStudentUser(estudiante.getEmail(), "Estudiante Base", estudiante.getId(), "Inicial123");

        mockMvc.perform(post("/admin/organizadores/crear")
                .with(csrf())
                .param("correo", estudiante.getEmail())
                .param("contrasena", "NuevaClave123")
                .param("tipo", "LIDER_ESTUDIANTIL")
                .param("representacion", "Representante de semestre"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/organizadores"));

        Usuario actualizado = usuarioRepository.findByCorreo(estudiante.getEmail()).orElseThrow();
        assertThat(actualizado.getTipo()).isEqualTo(TipoUsuario.LIDER_ESTUDIANTIL);
        assertThat(actualizado.getDatosEspecificos()).isInstanceOf(LiderEstudiantil.class);
        assertThat(passwordEncoder.matches("NuevaClave123", actualizado.getContrasena())).isTrue();
    }
}
