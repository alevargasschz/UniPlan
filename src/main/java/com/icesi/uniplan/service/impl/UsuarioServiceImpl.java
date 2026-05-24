package com.icesi.uniplan.service.impl;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.*;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Employee;
import com.icesi.uniplan.model.postgres.Student;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import com.icesi.uniplan.repository.postgres.IEmployeeRepository;
import com.icesi.uniplan.repository.postgres.IStudentRepository;
import com.icesi.uniplan.service.IUsuarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private final IUsuarioRepository usuarioRepository;
    private final IStudentRepository studentRepository;
    private final IEmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Usuario registrarEstudiante(RegistroEstudianteRequest request) {
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

        log.debug("ANTES builder: nombre={}, correo={}", student.getFirstName() + " " + student.getLastName(), request.getCorreo());
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

        // Validar que el empleado exista en la BD institucional
        Employee employee = employeeRepository.findByEmail(request.getCorreo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El correo no corresponde a ningún empleado en la base de datos institucional"));

        // Validar que no esté ya registrado en UniPlan
        if (usuarioRepository.existsByCorreo(request.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado en UniPlan");
        }

        DatosEspecificos datosEspecificos = buildDatosOrganizador(request, employee);

        Usuario usuario = Usuario.builder()
                .nombre(employee.getFirstName() + " " + employee.getLastName())
                .correo(request.getCorreo())
                .contrasena(passwordEncoder.encode(request.getContrasena()))
                .tipo(request.getTipo())
                .datosEspecificos(datosEspecificos)
                .build();

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + correo));
    }

    private DatosEspecificos buildDatosOrganizador(RegistroOrganizadorRequest request, Employee employee) {
        return switch (request.getTipo()) {
            case PROFESOR -> {
                String facultad = request.getFacultad() != null ? request.getFacultad()
                        : (employee.getFaculty() != null ? employee.getFaculty().getName() : "No especificada");
                String departamento = request.getDepartamento() != null ? request.getDepartamento() : "No especificado";
                String especializacion = request.getEspecializacion() != null ? request.getEspecializacion() : "No especificada";
                yield new Profesor(facultad, departamento, especializacion);
            }
            case LIDER_ESTUDIANTIL -> {
                if (request.getPrograma() == null || request.getSemestre() == null || request.getRepresentacion() == null) {
                    throw new IllegalArgumentException(
                            "Para Líder Estudiantil se requieren: programa, semestre y representacion");
                }
                yield new LiderEstudiantil(request.getPrograma(), request.getSemestre(), request.getRepresentacion());
            }
            case BIENESTAR -> {
                if (request.getAreaAdministrativa() == null || request.getCargo() == null) {
                    throw new IllegalArgumentException(
                            "Para Personal de Bienestar se requieren: areaAdministrativa y cargo");
                }
                yield new PersonalBienestar(request.getAreaAdministrativa(), request.getCargo());
            }
            default -> throw new IllegalArgumentException("Tipo de organizador no válido: " + request.getTipo());
        };
    }
}
