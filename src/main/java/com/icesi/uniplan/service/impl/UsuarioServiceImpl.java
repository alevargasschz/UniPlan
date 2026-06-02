package com.icesi.uniplan.service.impl;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.*;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Employee;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.repository.mongo.IEventoRepository;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import com.icesi.uniplan.repository.postgres.IEnrollmentRepository;
import com.icesi.uniplan.repository.postgres.IEmployeeRepository;
import com.icesi.uniplan.repository.postgres.IStudentRepository;
import com.icesi.uniplan.repository.postgres.IUserRepository;
import com.icesi.uniplan.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IEventoRepository eventoRepository;
    private final IStudentRepository studentRepository;
    private final IEmployeeRepository employeeRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrarEstudiante(RegistroEstudianteRequest request) {
        validarRolEnBaseRelacional(TipoUsuario.ESTUDIANTE);
        log.debug("registrarEstudiante: codigo={}, correo={}, contrasena={}",
                request.getCodigoEstudiantil(), request.getCorreo(),
                request.getContrasena() != null ? "***" : "NULL");
        // Validar que el estudiante exista en la BD institucional por su código
        Student student = studentRepository.findById(request.getCodigoEstudiantil())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El código estudiantil no existe en la base de datos institucional"));

        log.debug("Student encontrado: id={}, firstName={}, lastName={}, email={}",
                student.getId(), student.getFirstName(), student.getLastName(), student.getEmail());
        // Validar que el correo coincida con el del estudiante en la BD institucional
        if (!student.getEmail().equalsIgnoreCase(request.getCorreo())) {
            throw new IllegalArgumentException(
                    "El correo no coincide con el código estudiantil en la base de datos institucional");
        }

        // Validar que no esté ya registrado en UniPlan
        log.debug("ANTES existsByCorreo: correo={}", request.getCorreo());
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado en UniPlan");
        }

        log.debug("ANTES builder: nombre={}, correo={}", student.getFirstName() + " " + student.getLastName(),
                request.getCorreo());
        Usuario usuario = Usuario.builder()
                .nombre(student.getFirstName() + " " + student.getLastName())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .tipo(TipoUsuario.ESTUDIANTE)
                .datosEspecificos(new Estudiante(student.getId()))
                .build();

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario registrarOrganizador(RegistroOrganizadorRequest request) {
        // Solo PROFESOR, LIDER_ESTUDIANTIL y BIENESTAR pueden ser organizadores
        if (request.getTipo() == TipoUsuario.ESTUDIANTE) {
            throw new IllegalArgumentException("Los estudiantes no pueden ser registrados como organizadores");
        }

        Optional<Usuario> existente = usuarioRepository.findByCorreo(request.getCorreo());
        if (existente.isPresent()) {
            Usuario usuarioExistente = existente.get();
            boolean esUpgradeAprobado = request.getTipo() == TipoUsuario.LIDER_ESTUDIANTIL
                    && usuarioExistente.getTipo() == TipoUsuario.ESTUDIANTE;

            if (!esUpgradeAprobado) {
                throw new IllegalArgumentException("El correo ya está registrado en UniPlan");
            }

            return actualizarEstudianteALider(usuarioExistente, request);
        }

        Usuario usuario = construirOrganizador(request);
        usuario.setContrasena(passwordEncoder.encode(request.getContrasena()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario actualizarOrganizador(String organizadorId, RegistroOrganizadorRequest request) {

        Usuario existente = obtenerOrganizadorPorId(organizadorId);

        if (request.getTipo() == TipoUsuario.ESTUDIANTE) {
            throw new IllegalArgumentException("Los estudiantes no pueden ser registrados como organizadores");
        }

        if (!existente.getCorreo().equalsIgnoreCase(request.getCorreo())
                && usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado en UniPlan");
        }

        Usuario actualizado = construirOrganizador(request);
        existente.setNombre(actualizado.getNombre());
        existente.setCorreo(actualizado.getCorreo());
        existente.setTipo(actualizado.getTipo());
        existente.setDatosEspecificos(actualizado.getDatosEspecificos());
        existente.setContrasena(passwordEncoder.encode(request.getContrasena()));

        return usuarioRepository.save(existente);
    }

    @Override
    public void eliminarOrganizador(String organizadorId) {
        Usuario organizador = obtenerOrganizadorPorId(organizadorId);
        long eventosActivos = eventoRepository.countByOrganizadorCorreo(organizador.getCorreo());
        if (eventosActivos > 0) {
            throw new IllegalArgumentException("No se puede eliminar el organizador porque tiene eventos asociados");
        }
        usuarioRepository.deleteById(organizadorId);
    }

    @Override
    public Usuario obtenerOrganizadorPorId(String organizadorId) {
        Usuario usuario = usuarioRepository.findById(organizadorId)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        if (usuario.getTipo() == TipoUsuario.ESTUDIANTE) {
            throw new IllegalArgumentException("El usuario indicado no es organizador");
        }
        return usuario;
    }

    @Override
    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + correo));
    }

        private DatosEspecificos buildDatosOrganizador(RegistroOrganizadorRequest request, Employee employee) {
        return switch (request.getTipo()) {
            case PROFESOR -> {
            String facultad = valorConFallback(
                request.getFacultad(),
                employee.getFaculty() != null ? employee.getFaculty().getName() : null,
                "No especificada");
            String departamento = valorConFallback(
                request.getDepartamento(),
                employee.getFaculty() != null ? employee.getFaculty().getLocation() : null,
                "No especificado");
            String especializacion = valorConFallback(
                request.getEspecializacion(),
                employee.getEmployeeType() != null ? employee.getEmployeeType().getName() : null,
                "No especificada");

            validarCampoRequerido("facultad", facultad);
            validarCampoRequerido("departamento", departamento);
            validarCampoRequerido("especializacion", especializacion);

                yield new Profesor(facultad, departamento, especializacion);
            }
            case LIDER_ESTUDIANTIL -> {
                throw new IllegalArgumentException(
                        "Líder Estudiantil debe construirse con datos de estudiante institucional");
            }
            case BIENESTAR -> {
                String areaAdministrativa = valorConFallback(
                    request.getAreaAdministrativa(),
                    employee.getFaculty() != null ? employee.getFaculty().getName() : null,
                    "Bienestar Universitario");
                String cargo = valorConFallback(
                    request.getCargo(),
                    employee.getEmployeeType() != null ? employee.getEmployeeType().getName() : null,
                    "Personal de Bienestar");

                validarCampoRequerido("areaAdministrativa", areaAdministrativa);
                validarCampoRequerido("cargo", cargo);

                yield new PersonalBienestar(areaAdministrativa, cargo);
            }
            default -> throw new IllegalArgumentException("Tipo de organizador no válido: " + request.getTipo());
        };
    }

    private DatosEspecificos buildDatosLiderEstudiantil(RegistroOrganizadorRequest request, Student student) {
        if (request.getRepresentacion() == null || request.getRepresentacion().isBlank()) {
            throw new IllegalArgumentException(
                    "Para Líder Estudiantil se requiere: representacion");
        }

        String programa = calcularProgramaDesdePostgres(student.getId());
        int semestre = calcularSemestreDesdePostgres(student.getId());

        return new LiderEstudiantil(programa, semestre, request.getRepresentacion());
    }

    private String valorConFallback(String valorPrincipal, String fallback, String valorPorDefecto) {
        if (valorPrincipal != null && !valorPrincipal.isBlank()) {
            return valorPrincipal.trim();
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback.trim();
        }
        return valorPorDefecto;
    }

    private void validarCampoRequerido(String nombreCampo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Para el organizador se requiere: " + nombreCampo);
        }
    }

    private String calcularProgramaDesdePostgres(String codigoEstudiante) {
        Map<String, Long> frecuenciaProgramas = enrollmentRepository.findByStudent_Id(codigoEstudiante).stream()
                .map(e -> e.getGroup() != null ? e.getGroup().getSubject() : null)
                .filter(Objects::nonNull)
                .map(s -> s.getProgram())
                .filter(Objects::nonNull)
                .map(p -> p.getName())
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .collect(Collectors.groupingBy(nombre -> nombre, Collectors.counting()));

        return frecuenciaProgramas.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No especificado");
    }

    /**
     * Calcula semestre académico usando los semestres COMPLETED en la base
     * institucional.
     * Regla: semestre = semestres completados + 1 (rango 1..15).
     */
    private int calcularSemestreDesdePostgres(String codigoEstudiante) {
        long semestresCompletados = enrollmentRepository.findCompletedEnrollmentsByStudent(codigoEstudiante).stream()
                .map(e -> e.getGroup() != null ? e.getGroup().getSemester() : null)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        int semestre = (int) semestresCompletados + 1;
        if (semestre < 1)
            return 1;
        return Math.min(semestre, 15);
    }

    private void validarRolEnBaseRelacional(TipoUsuario tipoUsuario) {
        List<String> posiblesRoles = switch (tipoUsuario) {
            case ESTUDIANTE -> List.of("ESTUDIANTE", "STUDENT");
            case PROFESOR, LIDER_ESTUDIANTIL, BIENESTAR -> List.of(
                tipoUsuario.name(),
                "EMPLOYEE",
                "ORGANIZER");
        };

        boolean existe = posiblesRoles.stream().anyMatch(
            rol -> userRepository.existsRoleIgnoreCase(rol)
                || userRepository.existsRoleIgnoreCase("ROLE_" + rol));

        if (!existe) {
            throw new IllegalArgumentException(
                "No se encontró un rol válido para " + tipoUsuario.name()
                    + " en la base de datos relacional. Roles esperados: "
                    + String.join(", ", posiblesRoles));
        }
    }

    // UsuarioServiceImpl
    @Override
    public List<Usuario> listarOrganizadores(TipoUsuario tipo) {
        List<TipoUsuario> tiposOrganizador = Arrays.asList(
                TipoUsuario.PROFESOR,
                TipoUsuario.LIDER_ESTUDIANTIL,
                TipoUsuario.BIENESTAR);

        if (tipo != null && tiposOrganizador.contains(tipo)) {
            return usuarioRepository.findByTipo(tipo);
        }

        return tiposOrganizador.stream()
                .flatMap(t -> usuarioRepository.findByTipo(t).stream())
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .toList();
    }

    private Usuario construirOrganizador(RegistroOrganizadorRequest request) {
        if (request.getTipo() == TipoUsuario.LIDER_ESTUDIANTIL) {
            Student student = studentRepository.findByEmail(request.getCorreo())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "El correo no corresponde a ningún estudiante en la base de datos institucional"));

            DatosEspecificos datosEspecificos = buildDatosLiderEstudiantil(request, student);
            return Usuario.builder()
                    .nombre(student.getFirstName() + " " + student.getLastName())
                    .correo(request.getCorreo())
                    .tipo(request.getTipo())
                    .datosEspecificos(datosEspecificos)
                    .build();
        }

        Employee employee = employeeRepository.findByEmail(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El correo no corresponde a ningún empleado en la base de datos institucional"));

        DatosEspecificos datosEspecificos = buildDatosOrganizador(request, employee);
        return Usuario.builder()
                .nombre(employee.getFirstName() + " " + employee.getLastName())
                .correo(request.getCorreo())
                .tipo(request.getTipo())
                .datosEspecificos(datosEspecificos)
                .build();
    }

    private Usuario actualizarEstudianteALider(Usuario estudianteExistente, RegistroOrganizadorRequest request) {
        Student student = studentRepository.findByEmail(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El correo no corresponde a ningún estudiante en la base de datos institucional"));

        DatosEspecificos datosEspecificos = buildDatosLiderEstudiantil(request, student);
        estudianteExistente.setNombre(student.getFirstName() + " " + student.getLastName());
        estudianteExistente.setTipo(TipoUsuario.LIDER_ESTUDIANTIL);
        estudianteExistente.setDatosEspecificos(datosEspecificos);
        estudianteExistente.setContrasena(passwordEncoder.encode(request.getContrasena()));
        return usuarioRepository.save(estudianteExistente);
    }
}
