package com.icesi.uniplan.support;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.DatosEspecificosRequest;
import com.icesi.uniplan.dto.request.ConferencistaRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.*;
import com.icesi.uniplan.repository.mongo.IEventoRepository;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import com.icesi.uniplan.repository.postgres.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.Timestamp;
import java.util.Date;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected IUsuarioRepository usuarioRepository;

    @Autowired
    protected IEventoRepository eventoRepository;

    @Autowired
    protected IEventoEstadisticaRepository eventoEstadisticaRepository;

    @Autowired
    protected IStudentRepository studentRepository;

    @Autowired
    protected IEnrollmentRepository enrollmentRepository;

    @Autowired
    protected ICountryRepository countryRepository;

    @Autowired
    protected IDepartmentRepository departmentRepository;

    @Autowired
    protected ICityRepository cityRepository;

    @Autowired
    protected ICampusRepository campusRepository;

    @Autowired
    protected IFacultyRepository facultyRepository;

    @Autowired
    protected IEmployeeRepository employeeRepository;

    @Autowired
    protected IContractTypeRepository contractTypeRepository;

    @Autowired
    protected IEmployeeTypeRepository employeeTypeRepository;

    @Autowired
    protected IAreaRepository areaRepository;

    @Autowired
    protected IProgramRepository programRepository;

    @Autowired
    protected ISubjectRepository subjectRepository;

    @Autowired
    protected IGroupRepository groupRepository;

    @Autowired
    protected IUserRepository userRepository;

    @BeforeEach
    void cleanDatabase() {
        enrollmentRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();
        subjectRepository.deleteAll();
        programRepository.deleteAll();
        areaRepository.deleteAll();
        employeeRepository.deleteAll();
        facultyRepository.deleteAll();
        studentRepository.deleteAll();
        campusRepository.deleteAll();
        cityRepository.deleteAll();
        departmentRepository.deleteAll();
        countryRepository.deleteAll();
        eventoEstadisticaRepository.deleteAll();
        eventoRepository.deleteAll();
        usuarioRepository.deleteAll();
        contractTypeRepository.deleteAll();
        employeeTypeRepository.deleteAll();
        seedRoleCatalog();
        mockMvc = webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    protected void seedRoleCatalog() {
        seedRole("ESTUDIANTE");
        seedRole("PROFESOR");
        seedRole("LIDER_ESTUDIANTIL");
        seedRole("BIENESTAR");
    }

    protected void seedRole(String role) {
        String username = "__role_" + role;
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }

        com.icesi.uniplan.model.postgres.User rolCatalogo = new com.icesi.uniplan.model.postgres.User();
        rolCatalogo.setUsername(username);
        rolCatalogo.setPasswordHash("seed-role");
        rolCatalogo.setRole(role);
        rolCatalogo.setIsActive(Boolean.TRUE);
        rolCatalogo.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        userRepository.save(rolCatalogo);
    }

    protected Usuario saveUser(String correo, String nombre, TipoUsuario tipo, String rawPassword) {
        return usuarioRepository.save(Usuario.builder()
                .correo(correo)
                .nombre(nombre)
                .tipo(tipo)
                .contrasena(passwordEncoder.encode(rawPassword))
                .build());
    }

    protected Usuario saveStudentUser(String correo, String nombre, String codigo, String rawPassword) {
        return usuarioRepository.save(Usuario.builder()
                .correo(correo)
                .nombre(nombre)
                .tipo(TipoUsuario.ESTUDIANTE)
                .contrasena(passwordEncoder.encode(rawPassword))
                .datosEspecificos(new Estudiante(codigo))
                .build());
    }

    protected Date futureDateOffset(long minutes) {
        return Date.from(ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(minutes).toInstant());
    }

    protected Date pastDateOffset(long minutes) {
        return Date.from(ZonedDateTime.now(ZoneId.systemDefault()).minusMinutes(minutes).toInstant());
    }

    protected String isoDate(Date date) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    protected Student seedStudent(String codigo, String email) {
        Country country = countryRepository.save(new Country(1, "Colombia", null));
        Department department = departmentRepository.save(new Department(1, "Valle del Cauca", country, null));
        City city = cityRepository.save(new City(1, "Cali", department, null));
        Campus campus = campusRepository.save(new Campus(1, "Icesi", city, null, null));

        Student student = new Student();
        student.setId(codigo);
        student.setFirstName("Juan");
        student.setLastName("Pérez");
        student.setEmail(email);
        student.setBirthDate(java.sql.Date.valueOf("2001-01-15"));
        student.setBirthPlace(city);
        student.setCampus(campus);
        student.setEnrollments(java.util.List.of());
        return studentRepository.save(student);
    }

    protected Enrollment seedCompletedEnrollmentForStudent(Student student, String subjectCode, String semester) {
        Country country = countryRepository.save(new Country(1, "Colombia", null));
        Department department = departmentRepository.save(new Department(1, "Valle del Cauca", country, null));
        City city = cityRepository.save(new City(1, "Cali", department, null));
        Campus campus = campusRepository.save(new Campus(1, "Icesi", city, null, null));
        Faculty faculty = facultyRepository.save(new Faculty(1, "Ingeniería", "Cali", "5555", null, null, null));
        contractTypeRepository.save(new ContractType("TIEMPO_COMPLETO"));
        employeeTypeRepository.save(new EmployeeType("DOCENTE"));
        Employee professor = employeeRepository.save(new Employee(
                "EMP001",
                "Prof",
                "Docente",
                "profesor@icesi.edu.co",
                new ContractType("TIEMPO_COMPLETO"),
                new EmployeeType("DOCENTE"),
                faculty,
                campus,
                city,
                null
        ));

        Area area = areaRepository.save(new Area(1, "Ciencias", faculty, professor, null));
        Program program = programRepository.save(new Program(1, "Ingeniería de Sistemas", area, null));
        Subject subject = subjectRepository.save(new Subject(subjectCode, "Materia " + subjectCode, program, null));
        Group group = groupRepository.save(new Group("G1", 1, semester, subject, professor, null));

        Enrollment enrollment = new Enrollment(
                new EnrollmentId(student.getId(), group.getNrc()),
                student,
                group,
                new java.sql.Date(System.currentTimeMillis()),
                "COMPLETED"
        );
        return enrollmentRepository.save(enrollment);
    }

    protected CrearEventoRequest baseEventRequest(TipoEvento tipo, String title, String description, 
                                               LocalDateTime start, LocalDateTime end,
                                               Integer maxAsistentes, DatosEspecificosRequest datosEspecificos) {
    CrearEventoRequest request = new CrearEventoRequest();
    request.setTitulo(title);
    request.setDescripcion(description);
    request.setTipo(tipo);
    request.setFechaHoraInicio(start);
    request.setFechaHoraFin(end);
    request.setUbicacion("Auditorio Icesi");
    request.setMaxAsistentes(maxAsistentes);
    request.setDatosEspecificos(datosEspecificos);
    return request;
}

    protected DatosEspecificosRequest tallerData(String... previas) {
        DatosEspecificosRequest datos = new DatosEspecificosRequest();
        datos.setMaterialesRequeridos(java.util.List.of("Laptop"));
        datos.setCondicionesPrevias(java.util.List.of(previas));
        return datos;
    }

    protected DatosEspecificosRequest charlaData() {
        DatosEspecificosRequest datos = new DatosEspecificosRequest();
        ConferencistaRequest conferencista = new ConferencistaRequest();
        conferencista.setNombre("Ana");
        conferencista.setPerfil("Docente");
        conferencista.setAfiliacion("Icesi");
        datos.setConferencista(conferencista);
        datos.setEnlaces(java.util.List.of("https://icesi.edu.co"));
        datos.setDescripcionExtendida("Charla de ejemplo");
        return datos;
    }

    protected DatosEspecificosRequest torneoData() {
        DatosEspecificosRequest datos = new DatosEspecificosRequest();
        datos.setTipoDeporte("Fútbol");
        datos.setReglas(java.util.List.of("Juego limpio"));
        datos.setNumeroEquipos(4);
        datos.setEstructuraTorneo("Eliminación sencilla");
        return datos;
    }

    protected DatosEspecificosRequest voluntariadoData() {
        DatosEspecificosRequest datos = new DatosEspecificosRequest();
        datos.setCausa("Apoyo comunitario");
        datos.setNumeroHorasRequeridas(10);
        datos.setActividades(java.util.List.of("Apoyo en jornada"));
        datos.setPuntosEncuentro(java.util.List.of("Plazoleta principal"));
        datos.setResponsables(java.util.List.of("Bienestar"));
        return datos;
    }
}
