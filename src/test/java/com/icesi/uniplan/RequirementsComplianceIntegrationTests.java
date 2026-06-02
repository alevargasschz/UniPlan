package com.icesi.uniplan;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.mongo.embedded.Organizador;
import com.icesi.uniplan.model.mongo.embedded.Profesor;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Campus;
import com.icesi.uniplan.model.postgres.City;
import com.icesi.uniplan.model.postgres.ContractType;
import com.icesi.uniplan.model.postgres.Country;
import com.icesi.uniplan.model.postgres.Department;
import com.icesi.uniplan.model.postgres.Employee;
import com.icesi.uniplan.model.postgres.EmployeeType;
import com.icesi.uniplan.model.postgres.EventoEstadistica;
import com.icesi.uniplan.model.postgres.Faculty;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.support.IntegrationTestSupport;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RequirementsComplianceIntegrationTests extends IntegrationTestSupport {

    @Autowired
    private com.icesi.uniplan.service.IUsuarioService usuarioService;

    @Autowired
    private com.icesi.uniplan.service.IEventoService eventoService;

    @Autowired
    private com.icesi.uniplan.service.IEstadisticaService estadisticaService;

    @Test
    @DisplayName("RF01, RF02, RNF08: registro de estudiante sin nombre manual + contraseña cifrada + autenticación")
    void rf01_rf02_rnf08_studentRegistrationAndAuthentication() throws Exception {
        Student student = seedStudent("A00400001", "rf01@icesi.edu.co");

        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil(student.getId());
        request.setCorreo(student.getEmail());
        request.setContrasena("ClaveSegura123");

        Usuario registrado = usuarioService.registrarEstudiante(request);

        assertThat(registrado.getNombre()).isEqualTo("Juan Pérez");
        assertThat(passwordEncoder.matches("ClaveSegura123", registrado.getContrasena())).isTrue();

        mockMvc.perform(post("/public/auth/login")
                .with(csrf())
                .param("correo", student.getEmail())
                .param("contrasena", "ClaveSegura123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/eventos"));
    }

    @Test
    @DisplayName("RF04, RF05, RF06, RF39: registro de profesor organizador con validación institucional")
    void rf04_rf05_rf06_rf39_registerProfessorOrganizer() {
        seedEmployee("profesor.rf@icesi.edu.co");

        RegistroOrganizadorRequest request = new RegistroOrganizadorRequest();
        request.setNombre("Nombre Ignorado");
        request.setCorreo("profesor.rf@icesi.edu.co");
        request.setContrasena("ClaveSegura123");
        request.setTipo(TipoUsuario.PROFESOR);

        Usuario organizador = usuarioService.registrarOrganizador(request);

        assertThat(organizador.getTipo()).isEqualTo(TipoUsuario.PROFESOR);
        assertThat(organizador.getDatosEspecificos()).isInstanceOf(Profesor.class);
        Profesor profesor = (Profesor) organizador.getDatosEspecificos();
        assertThat(profesor.getFacultad()).isNotBlank();
        assertThat(profesor.getDepartamento()).isNotBlank();
        assertThat(profesor.getEspecializacion()).isNotBlank();
    }

    @Test
    @DisplayName("RF39: rechaza organizador no existente en base institucional")
    void rf39_rejectsOrganizerNotInInstitutionalDatabase() {
        RegistroOrganizadorRequest request = new RegistroOrganizadorRequest();
        request.setNombre("No Existe");
        request.setCorreo("no.existe@icesi.edu.co");
        request.setContrasena("ClaveSegura123");
        request.setTipo(TipoUsuario.PROFESOR);

        assertThatThrownBy(() -> usuarioService.registrarOrganizador(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("base de datos institucional");
    }

    @Test
    @DisplayName("RF21, RF27, RF29: creación de eventos válida + fecha futura + id único")
    void rf21_rf27_rf29_eventCreationAndUniqueIdentifier() {
        saveUser("organizador1@icesi.edu.co", "Org Uno", TipoUsuario.PROFESOR, "ClaveSegura123");

        CrearEventoRequest r1 = baseEventRequest(
                TipoEvento.charla,
                "Evento RF21 A",
                "Descripción suficientemente larga A",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                25,
                charlaData());

        Evento e1 = eventoService.crearEvento(r1, "organizador1@icesi.edu.co");

        CrearEventoRequest r2 = baseEventRequest(
                TipoEvento.charla,
                "Evento RF21 B",
                "Descripción suficientemente larga B",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2),
                20,
                charlaData());

        Evento e2 = eventoService.crearEvento(r2, "organizador1@icesi.edu.co");

        assertThat(e1.getEstado()).isEqualTo(EstadoEvento.PROGRAMADO);
        assertThat(e1.getId()).isNotBlank();
        assertThat(e2.getId()).isNotBlank();
        assertThat(e1.getId()).isNotEqualTo(e2.getId());

        CrearEventoRequest enPasado = baseEventRequest(
                TipoEvento.charla,
                "Evento inválido",
                "Descripción en pasado inválida",
                LocalDateTime.now().minusHours(2),
                LocalDateTime.now().plusHours(2),
                10,
                charlaData());

        assertThatThrownBy(() -> eventoService.crearEvento(enPasado, "organizador1@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("pasado");
    }

    @Test
    @WithMockUser(username = "organizador1@icesi.edu.co", roles = "PROFESOR")
    @DisplayName("RF28: validación de cupos > 0 en formulario de creación")
    void rf28_rejectsZeroCapacityAtControllerValidation() throws Exception {
        mockMvc.perform(post("/eventos/crear")
                .with(csrf())
                .param("titulo", "Evento cupos")
                .param("descripcion", "Descripción para validar cupos mínimos")
                .param("tipo", "charla")
                .param("fechaHoraInicio", LocalDateTime.now().plusDays(2).withSecond(0).withNano(0).toString())
                .param("fechaHoraFin",
                        LocalDateTime.now().plusDays(2).plusHours(1).withSecond(0).withNano(0).toString())
                .param("ubicacion", "Auditorio")
                .param("maxAsistentes", "0")
                .param("datosEspecificos.conferencista.nombre", "Ana")
                .param("datosEspecificos.conferencista.perfil", "Docente")
                .param("datosEspecificos.conferencista.afiliacion", "Icesi")
                .param("datosEspecificos.enlaces[0]", "https://icesi.edu.co")
                .param("datosEspecificos.descripcionExtendida", "Detalle"))
                .andExpect(status().isOk());

        assertThat(eventoRepository.count()).isZero();
    }

    @Test
    @DisplayName("RF11, RF12, RF13: inscripción exitosa, sin cupos y sin duplicados")
    void rf11_rf12_rf13_enrollmentRules() {
        saveUser("organizador2@icesi.edu.co", "Org Dos", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveStudentUser("est1@icesi.edu.co", "Est Uno", "A00400002", "ClaveSegura123");
        saveStudentUser("est2@icesi.edu.co", "Est Dos", "A00400003", "ClaveSegura123");
        saveStudentUser("est3@icesi.edu.co", "Est Tres", "A00400011", "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Charla RF11",
                "Descripción extensa para RF11",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                2,
                charlaData()), "organizador2@icesi.edu.co");

        eventoService.inscribirEstudiante(evento.getId(), "est1@icesi.edu.co");
        Evento actualizado = eventoService.obtenerEvento(evento.getId());
        assertThat(actualizado.getTotalInscritos()).isEqualTo(1);
        assertThat(actualizado.getCuposDisponibles()).isEqualTo(1);

        assertThatThrownBy(() -> eventoService.inscribirEstudiante(evento.getId(), "est1@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está inscrito");

        eventoService.inscribirEstudiante(evento.getId(), "est2@icesi.edu.co");

        assertThatThrownBy(() -> eventoService.inscribirEstudiante(evento.getId(), "est3@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No hay cupos");
    }

    @Test
    @DisplayName("RF14: taller valida requisitos previos contra información académica relacional")
    void rf14_workshopPrerequisiteValidation() {
        saveUser("organizador3@icesi.edu.co", "Org Tres", TipoUsuario.PROFESOR, "ClaveSegura123");
        Student student = seedStudent("A00400004", "est.taller@icesi.edu.co");
        saveStudentUser("est.taller@icesi.edu.co", "Est Taller", student.getId(), "ClaveSegura123");
        seedCompletedEnrollmentForStudent(student, "ISIS101", "2024-1");

        Evento tallerConReqCumplido = eventoService.crearEvento(baseEventRequest(
                TipoEvento.taller,
                "Taller con requisito",
                "Descripción larga del taller",
                LocalDateTime.now().plusDays(4),
                LocalDateTime.now().plusDays(4).plusHours(2),
                20,
                tallerData("ISIS101")), "organizador3@icesi.edu.co");

        eventoService.inscribirEstudiante(tallerConReqCumplido.getId(), "est.taller@icesi.edu.co");
        assertThat(eventoService.obtenerEvento(tallerConReqCumplido.getId()).getTotalInscritos()).isEqualTo(1);
    }

    @Test
    @DisplayName("RF15: torneos deportivos rechazan traslape horario")
    void rf15_tournamentOverlapValidation() {
        saveUser("organizador4@icesi.edu.co", "Org Cuatro", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveStudentUser("est.torneo@icesi.edu.co", "Est Torneo", "A00400005", "ClaveSegura123");

        Evento torneo1 = eventoService.crearEvento(baseEventRequest(
                TipoEvento.torneo,
                "Torneo A",
                "Descripción del torneo A",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(2),
                30,
                torneoData()), "organizador4@icesi.edu.co");

        Evento torneo2 = eventoService.crearEvento(baseEventRequest(
                TipoEvento.torneo,
                "Torneo B",
                "Descripción del torneo B",
                LocalDateTime.now().plusDays(3).plusMinutes(30),
                LocalDateTime.now().plusDays(3).plusHours(3),
                30,
                torneoData()), "organizador4@icesi.edu.co");

        eventoService.inscribirEstudiante(torneo1.getId(), "est.torneo@icesi.edu.co");

        assertThatThrownBy(() -> eventoService.inscribirEstudiante(torneo2.getId(), "est.torneo@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("traslapado");
    }

    @Test
    @DisplayName("RF16: voluntariado valida horas mínimas previas")
    void rf16_volunteerHoursValidation() {
        saveUser("organizador5@icesi.edu.co", "Org Cinco", TipoUsuario.BIENESTAR, "ClaveSegura123");
        saveStudentUser("est.vol@icesi.edu.co", "Est Vol", "A00400006", "ClaveSegura123");

        Evento antecedente = Evento.builder()
                .titulo("Voluntariado previo")
                .descripcion("Actividad previa finalizada")
                .tipo(TipoEvento.voluntariado)
                .fechaHoraInicio(LocalDateTime.now().minusDays(5))
                .fechaHoraFin(LocalDateTime.now().minusDays(5).plusHours(2))
                .ubicacion("Campus")
                .maxAsistentes(20)
                .totalInscritos(1)
                .cuposDisponibles(19)
                .estado(EstadoEvento.FINALIZADO)
                .organizador(new Organizador(new ObjectId(), "Bienestar", "organizador5@icesi.edu.co",
                        TipoUsuario.BIENESTAR))
                .datosEspecificos(new com.icesi.uniplan.model.mongo.embedded.ActividadVoluntariado(
                        "Causa", 5, List.of("Actividad"), List.of("Punto"), List.of("Responsable")))
                .inscripciones(List.of(new Inscripcion(new ObjectId(), "Est Vol", "est.vol@icesi.edu.co", "A00400006",
                        LocalDateTime.now().minusDays(5), true)))
                .build();
        eventoRepository.save(antecedente);

        Evento voluntariadoExigente = eventoService.crearEvento(baseEventRequest(
                TipoEvento.voluntariado,
                "Voluntariado nuevo",
                "Descripción del voluntariado nuevo",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                30,
                voluntariadoData()), "organizador5@icesi.edu.co");

        assertThatThrownBy(
                () -> eventoService.inscribirEstudiante(voluntariadoExigente.getId(), "est.vol@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Se requieren");
    }

    @Test
    @DisplayName("RF17: charlas solo requieren cupo disponible")
    void rf17_talkEnrollmentWithoutAcademicRules() {
        saveUser("organizador6@icesi.edu.co", "Org Seis", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveStudentUser("est.charla@icesi.edu.co", "Est Charla", "A00400007", "ClaveSegura123");

        Evento charla = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Charla abierta",
                "Descripción de charla abierta",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                100,
                charlaData()), "organizador6@icesi.edu.co");

        eventoService.inscribirEstudiante(charla.getId(), "est.charla@icesi.edu.co");
        assertThat(eventoService.obtenerEvento(charla.getId()).getTotalInscritos()).isEqualTo(1);
    }

    @Test
    @DisplayName("RF19, RF20: cancelar inscripción libera cupo y decrementa inscritos")
    void rf19_rf20_cancelEnrollmentReleasesSlot() {
        saveUser("organizador7@icesi.edu.co", "Org Siete", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveStudentUser("est.cancel@icesi.edu.co", "Est Cancel", "A00400008", "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Charla cancelable",
                "Descripción para cancelación",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(3).plusHours(1),
                2,
                charlaData()), "organizador7@icesi.edu.co");

        eventoService.inscribirEstudiante(evento.getId(), "est.cancel@icesi.edu.co");
        eventoService.cancelarInscripcion(evento.getId(), "est.cancel@icesi.edu.co");

        Evento actualizado = eventoService.obtenerEvento(evento.getId());
        assertThat(actualizado.getTotalInscritos()).isZero();
        assertThat(actualizado.getCuposDisponibles()).isEqualTo(2);
    }

    @Test
    @DisplayName("RF09: filtrado de eventos por tipo, estado y rango de fechas")
    void rf09_eventFilteringByTypeStatusAndDateRange() {
        saveUser("organizador9@icesi.edu.co", "Org Nueve", TipoUsuario.PROFESOR, "ClaveSegura123");

        Evento charla = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Charla filtro",
                "Descripción de charla para filtros",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                20,
                charlaData()), "organizador9@icesi.edu.co");

        eventoService.crearEvento(baseEventRequest(
                TipoEvento.torneo,
                "Torneo filtro",
                "Descripción de torneo para filtros",
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(10).plusHours(2),
                20,
                torneoData()), "organizador9@icesi.edu.co");

        List<Evento> filtrados = eventoService.listarEventos(
                TipoEvento.charla,
                EstadoEvento.PROGRAMADO,
                futureDateOffset(60),
                futureDateOffset(60 * 24 * 5));

        assertThat(filtrados).extracting(Evento::getId).contains(charla.getId());
        assertThat(filtrados).allMatch(e -> e.getTipo() == TipoEvento.charla);
    }

    @Test
    @WithMockUser(username = "estudiante@icesi.edu.co", roles = "ESTUDIANTE")
    @DisplayName("RF10: detalle de evento disponible para usuario autenticado")
    void rf10_eventDetailForAuthenticatedUser() throws Exception {
        saveUser("organizador10@icesi.edu.co", "Org Diez", TipoUsuario.PROFESOR, "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.otro,
                "Evento detalle",
                "Descripción para consulta detallada",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(1),
                15,
                new com.icesi.uniplan.dto.request.DatosEspecificosRequest()), "organizador10@icesi.edu.co");

        mockMvc.perform(get("/eventos/" + evento.getId()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("RF41: confirmación de asistencia por organizador")
    void rf41_attendanceRegistration() {
        saveUser("organizador11@icesi.edu.co", "Org Once", TipoUsuario.BIENESTAR, "ClaveSegura123");
        saveStudentUser("est.asis@icesi.edu.co", "Est Asis", "A00400012", "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Evento asistencia",
                "Descripción para asistencia",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                10,
                charlaData()), "organizador11@icesi.edu.co");

        eventoService.inscribirEstudiante(evento.getId(), "est.asis@icesi.edu.co");
        String inscripcionId = eventoService.obtenerEvento(evento.getId()).getInscripciones().get(0).getId()
                .toHexString();

        eventoService.confirmarAsistencia(evento.getId(), inscripcionId, "organizador11@icesi.edu.co");

        Evento actualizado = eventoService.obtenerEvento(evento.getId());
        assertThat(actualizado.getInscripciones().get(0).getConfirmada()).isTrue();
    }

    @Test
    @DisplayName("RNF07: consulta de estadísticas filtradas responde sin demora perceptible en dataset pequeño")
    void rnf07_filteredStatsRespondQuicklyInSmallDataset() {
        saveUser("organizador12@icesi.edu.co", "Org Doce", TipoUsuario.PROFESOR, "ClaveSegura123");

        for (int i = 0; i < 6; i++) {
            eventoService.crearEvento(baseEventRequest(
                    i % 2 == 0 ? TipoEvento.charla : TipoEvento.taller,
                    "Evento rendimiento " + i,
                    "Descripción de rendimiento " + i + " suficientemente larga",
                    LocalDateTime.now().plusDays(2 + i),
                    LocalDateTime.now().plusDays(2 + i).plusHours(1),
                    25,
                    i % 2 == 0 ? charlaData() : tallerData("SEMESTRE:1")), "organizador12@icesi.edu.co");
        }

        assertTimeoutPreemptively(Duration.ofSeconds(2),
                () -> estadisticaService.obtenerDashboardFiltrado(6, null, null, null));
    }

    @Test
    @DisplayName("RF30, RF31, RF32, RF38, RF44, RNF10, RNF13: inscritos + CSV + acceso restringido")
    void rf30_rf31_rf32_rf38_rf44_rnf10_rnf13_inscritosCsvAndRestrictions() {
        saveUser("owner@icesi.edu.co", "Owner", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveUser("otro.org@icesi.edu.co", "Otro", TipoUsuario.PROFESOR, "ClaveSegura123");
        saveStudentUser("est.insc@icesi.edu.co", "Est Insc", "A00400009", "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Evento inscritos",
                "Descripción de inscritos",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                20,
                charlaData()), "owner@icesi.edu.co");
        eventoService.inscribirEstudiante(evento.getId(), "est.insc@icesi.edu.co");

        List<Inscripcion> inscritos = eventoService.listarInscritos(evento.getId(), "owner@icesi.edu.co");
        assertThat(inscritos).hasSize(1);
        assertThat(inscritos.get(0).getNombre()).isEqualTo("Est Insc");
        assertThat(inscritos.get(0).getCodigoEstudiante()).isEqualTo("A00400009");
        assertThat(inscritos.get(0).getCorreo()).isEqualTo("est.insc@icesi.edu.co");

        String csv = eventoService.exportarInscritosCSV(evento.getId(), "owner@icesi.edu.co");
        assertThat(csv).contains("Nombre,Codigo Estudiante,Correo,Fecha Inscripcion,Confirmada");
        assertThat(csv).contains("Est Insc,A00400009,est.insc@icesi.edu.co");

        assertThatThrownBy(() -> eventoService.listarInscritos(evento.getId(), "otro.org@icesi.edu.co"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pertenece");
    }

    @Test
    @DisplayName("RF33, RF34, RF35, RNF02, RNF14: estadísticas consistentes entre Mongo y PostgreSQL")
    void rf33_rf34_rf35_rnf02_rnf14_statsConsistencyAcrossDatastores() {
        saveUser("organizador8@icesi.edu.co", "Org Ocho", TipoUsuario.BIENESTAR, "ClaveSegura123");
        saveStudentUser("est.stats@icesi.edu.co", "Est Stats", "A00400010", "ClaveSegura123");

        Evento evento = eventoService.crearEvento(baseEventRequest(
                TipoEvento.charla,
                "Evento stats",
                "Descripción para estadísticas",
                LocalDateTime.now().plusDays(2),
                LocalDateTime.now().plusDays(2).plusHours(2),
                1,
                charlaData()), "organizador8@icesi.edu.co");

        EventoEstadistica statsInicial = eventoEstadisticaRepository.findById(evento.getId()).orElseThrow();
        assertThat(statsInicial.getTotalInscritos()).isZero();
        assertThat(statsInicial.getTotalCancelaciones()).isZero();
        assertThat(statsInicial.getTotalAsistentes()).isZero();

        eventoService.inscribirEstudiante(evento.getId(), "est.stats@icesi.edu.co");
        String inscripcionId = eventoService.obtenerEvento(evento.getId()).getInscripciones().get(0).getId()
                .toHexString();
        eventoService.confirmarAsistencia(evento.getId(), inscripcionId, "organizador8@icesi.edu.co");

        Evento mongo = eventoService.obtenerEvento(evento.getId());
        EventoEstadistica stats = eventoEstadisticaRepository.findById(evento.getId()).orElseThrow();

        assertThat(stats.getTotalInscritos()).isEqualTo(mongo.getTotalInscritos());
        assertThat(stats.getTotalAsistentes()).isEqualTo(1);
        assertThat(stats.getPorcentajeOcupacion()).isEqualTo(100.0);

        assertThat(estadisticaService.obtenerTopEventosPorOcupacion(5)).isNotEmpty();
        assertThat(estadisticaService.obtenerDashboardFiltrado(6, null, null, null).getDemandaPorTipo()).isNotEmpty();
    }

    @Test
    @WithMockUser(username = "estudiante@icesi.edu.co", roles = "ESTUDIANTE")
    @DisplayName("RF37, RNF09: estudiante no puede acceder a administración ni estadísticas administrativas")
    void rf37_rnf09_studentCannotAccessAdminOrStats() throws Exception {
        mockMvc.perform(get("/admin/organizadores"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/reportes/estadisticas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "organizador@icesi.edu.co", roles = "PROFESOR")
    @DisplayName("RF36: reportes principales disponibles para usuarios autorizados")
    void rf36_reportsAreAvailableForAuthorizedUsers() throws Exception {
        mockMvc.perform(get("/reportes/top-eventos"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reportes/mis-inscripciones"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/reportes/estadisticas"))
                .andExpect(status().isOk());
    }

    private Employee seedEmployee(String email) {
        Country country = countryRepository.save(new Country(11, "Colombia", null));
        Department department = departmentRepository.save(new Department(11, "Valle del Cauca", country, null));
        City city = cityRepository.save(new City(11, "Cali", department, null));
        Campus campus = campusRepository.save(new Campus(11, "Icesi", city, null, null));
        Faculty faculty = facultyRepository.save(new Faculty(11, "Ingeniería", "Cali", "5555", null, null, null));

        ContractType contractType = contractTypeRepository.save(new ContractType("TIEMPO_COMPLETO"));
        EmployeeType employeeType = employeeTypeRepository.save(new EmployeeType("DOCENTE"));

        Employee employee = new Employee(
                "EMP-RF-001",
                "Ada",
                "Lovelace",
                email,
                contractType,
                employeeType,
                faculty,
                campus,
                city,
                null);

        return employeeRepository.save(employee);
    }
}