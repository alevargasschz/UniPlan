package com.icesi.uniplan;

import com.icesi.uniplan.controller.AuthController;
import com.icesi.uniplan.controller.EventoController;
import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.DatosEspecificosRequest;
import com.icesi.uniplan.dto.request.LoginRequest;
import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.response.ApiResponse;
import com.icesi.uniplan.dto.response.EventoResumenResponse;
import com.icesi.uniplan.dto.response.EventoResponse;
import com.icesi.uniplan.dto.response.TokenResponse;
import com.icesi.uniplan.dto.response.UsuarioResponse;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Campus;
import com.icesi.uniplan.model.postgres.City;
import com.icesi.uniplan.model.postgres.Country;
import com.icesi.uniplan.model.postgres.Department;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.repository.mongo.IEventoRepository;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import com.icesi.uniplan.repository.postgres.ICampusRepository;
import com.icesi.uniplan.repository.postgres.ICityRepository;
import com.icesi.uniplan.repository.postgres.ICountryRepository;
import com.icesi.uniplan.repository.postgres.IDepartmentRepository;
import com.icesi.uniplan.repository.postgres.IEventoEstadisticaRepository;
import com.icesi.uniplan.repository.postgres.IStudentRepository;
import com.icesi.uniplan.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class IntegrationFlowTests {

    @Autowired
    private AuthController authController;

    @Autowired
    private EventoController eventoController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Autowired
    private IEventoRepository eventoRepository;

    @Autowired
    private IEventoEstadisticaRepository eventoEstadisticaRepository;

    @Autowired
    private IStudentRepository studentRepository;

    @Autowired
    private ICountryRepository countryRepository;

    @Autowired
    private IDepartmentRepository departmentRepository;

    @Autowired
    private ICityRepository cityRepository;

    @Autowired
    private ICampusRepository campusRepository;

    @BeforeEach
    void cleanDatabase() {
        eventoRepository.deleteAll();
        usuarioRepository.deleteAll();
        eventoEstadisticaRepository.deleteAll();
        studentRepository.deleteAll();
        campusRepository.deleteAll();
        cityRepository.deleteAll();
        departmentRepository.deleteAll();
        countryRepository.deleteAll();
    }

    @Test
    void shouldLoginWithValidCredentials() {
        usuarioRepository.save(Usuario.builder()
                .correo("estudiante@uniplan.edu.co")
                .contrasena(passwordEncoder.encode("ClaveSegura123"))
                .tipo(TipoUsuario.ESTUDIANTE)
                .nombre("Estudiante Demo")
                .build());

        LoginRequest request = new LoginRequest();
        request.setCorreo("estudiante@uniplan.edu.co");
        request.setContrasena("ClaveSegura123");

        ResponseEntity<ApiResponse<TokenResponse>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Login exitoso");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getCorreo()).isEqualTo("estudiante@uniplan.edu.co");
        assertThat(response.getBody().getData().getToken()).isNotBlank();
    }

    @Test
    void shouldRegisterStudentWhenHeExistsInPostgres() {
        Student student = createStudent("A00123456");

        RegistroEstudianteRequest request = new RegistroEstudianteRequest();
        request.setCodigoEstudiantil(student.getId());
        request.setCorreo(student.getEmail());
        request.setContrasena("ClaveSegura123");

        ResponseEntity<ApiResponse<UsuarioResponse>> response = authController.registrarEstudiante(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getMessage()).isEqualTo("Estudiante registrado exitosamente");
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getCorreo()).isEqualTo(student.getEmail());
        assertThat(response.getBody().getData().getTipo()).isEqualTo(TipoUsuario.ESTUDIANTE);
        assertThat(usuarioRepository.existsByCorreo(student.getEmail())).isTrue();
    }

    @Test
    void shouldCreateAndListEventsForAuthenticatedOrganizer() {
        String organizerCorreo = "profesor@uniplan.edu.co";
        usuarioRepository.save(Usuario.builder()
                .correo(organizerCorreo)
                .contrasena(passwordEncoder.encode("ClaveSegura123"))
                .tipo(TipoUsuario.PROFESOR)
                .nombre("Profesor Demo")
                .build());

        CrearEventoRequest request = new CrearEventoRequest();
        request.setTitulo("Taller de Spring Boot");
        request.setDescripcion("Sesión práctica para conocer el flujo de creación de APIs.");
        request.setTipo(TipoEvento.TALLER);
        request.setFechaHoraInicio(new Date(System.currentTimeMillis() + 60_000L));
        request.setFechaHoraFin(new Date(System.currentTimeMillis() + 120_000L));
        request.setUbicacion("Auditorio Icesi");
        request.setMaxAsistentes(30);

        DatosEspecificosRequest datos = new DatosEspecificosRequest();
        datos.setMaterialesRequeridos(List.of("Laptop", "Conexión a internet"));
        datos.setCondicionesPrevias(List.of("SEMESTRE:3"));
        request.setDatosEspecificos(datos);

        var auth = new UsernamePasswordAuthenticationToken(
                organizerCorreo,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_PROFESOR"))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        try {
            ResponseEntity<ApiResponse<EventoResponse>> createResponse = eventoController.crearEvento(request, auth);

            assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(createResponse.getBody()).isNotNull();
            assertThat(createResponse.getBody().isSuccess()).isTrue();
            assertThat(createResponse.getBody().getData()).isNotNull();
            assertThat(createResponse.getBody().getData().getTitulo()).isEqualTo("Taller de Spring Boot");
            assertThat(createResponse.getBody().getData().getTipo()).isEqualTo(TipoEvento.TALLER);

            ResponseEntity<ApiResponse<List<EventoResumenResponse>>> listResponse = eventoController.listarEventos(null, null, null, null);

            assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(listResponse.getBody()).isNotNull();
            assertThat(listResponse.getBody().isSuccess()).isTrue();
            assertThat(listResponse.getBody().getData()).hasSize(1);
            assertThat(listResponse.getBody().getData().get(0).getTitulo()).isEqualTo("Taller de Spring Boot");
            assertThat(eventoRepository.count()).isEqualTo(1);
            assertThat(eventoEstadisticaRepository.count()).isEqualTo(1);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private Student createStudent(String codigo) {
        Country country = countryRepository.save(new Country(1, "Colombia", null));
        Department department = departmentRepository.save(new Department(1, "Valle del Cauca", country, null));
        City city = cityRepository.save(new City(1, "Cali", department, null));
        Campus campus = campusRepository.save(new Campus(1, "Icesi", city, null, null));

        Student student = new Student();
        student.setId(codigo);
        student.setFirstName("Juan");
        student.setLastName("Pérez");
        student.setEmail("student@icesi.edu.co");
        student.setBirthDate(java.sql.Date.valueOf("2001-01-15"));
        student.setBirthPlace(city);
        student.setCampus(campus);
        student.setEnrollments(List.of());

        return studentRepository.save(student);
    }
}
