package com.icesi.uniplan.service.impl;

import com.icesi.uniplan.dto.request.ConferencistaRequest;
import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.dto.request.DatosEspecificosRequest;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.*;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.model.postgres.Enrollment;
import com.icesi.uniplan.model.postgres.Subject;
import com.icesi.uniplan.repository.mongo.IEventoRepository;
import com.icesi.uniplan.repository.mongo.IUsuarioRepository;
import com.icesi.uniplan.repository.postgres.IEnrollmentRepository;
import com.icesi.uniplan.service.IEstadisticaService;
import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoServiceImpl implements IEventoService {

    private final IEventoRepository eventoRepository;
    private final IUsuarioRepository usuarioRepository;
    private final IEnrollmentRepository enrollmentRepository;
    private final IEstadisticaService estadisticaService;

    // -------------------------------------------------------------------------
    // Crear evento
    // -------------------------------------------------------------------------

    @Override
    public Evento crearEvento(CrearEventoRequest request, String correoOrganizador) {
        Date ahora = new Date();
        if (request.getFechaHoraInicio().before(ahora)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser en el pasado");
        }
        if (!request.getFechaHoraFin().after(request.getFechaHoraInicio())) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        Usuario organizador = usuarioRepository.findByCorreo(correoOrganizador)
                .orElseThrow(() -> new IllegalArgumentException("Organizador no encontrado"));

        DatosEspecificos datosEspecificos = buildDatosEspecificosEvento(request);

        Evento evento = Evento.builder()
                .titulo(request.getTitulo())
                .descripcion(request.getDescripcion())
                .tipo(request.getTipo())
                .fechaHoraInicio(request.getFechaHoraInicio())
                .fechaHoraFin(request.getFechaHoraFin())
                .ubicacion(request.getUbicacion())
                .maxAsistentes(request.getMaxAsistentes())
                .totalInscritos(0)
                .cuposDisponibles(request.getMaxAsistentes())
                .estado(EstadoEvento.PROGRAMADO)
                .organizador(new Organizador(
                        new ObjectId(),
                        organizador.getNombre(),
                        organizador.getCorreo(),
                        organizador.getTipo()))
                .datosEspecificos(datosEspecificos)
                .build();

        Evento guardado = eventoRepository.save(evento);
        estadisticaService.crearEstadistica(guardado);
        return guardado;
    }

    private DatosEspecificos buildDatosEspecificosEvento(CrearEventoRequest request) {
        DatosEspecificosRequest d = request.getDatosEspecificos();
        return switch (request.getTipo()) {
            case TALLER -> {
                if (d.getMaterialesRequeridos() == null || d.getCondicionesPrevias() == null) {
                    throw new IllegalArgumentException("Taller requiere materialesRequeridos y condicionesPrevias");
                }
                yield new Taller(d.getMaterialesRequeridos(), d.getCondicionesPrevias());
            }
            case CHARLA -> {
                if (d.getConferencista() == null || d.getEnlaces() == null) {
                    throw new IllegalArgumentException("Charla requiere conferencista y enlaces");
                }
                ConferencistaRequest c = d.getConferencista();
                yield new Charla(
                        new Conferencista(c.getNombre(), c.getPerfil(), c.getAfiliacion()),
                        d.getEnlaces(),
                        d.getDescripcionExtendida() != null ? d.getDescripcionExtendida() : "");
            }
            case TORNEO -> {
                if (d.getTipoDeporte() == null || d.getReglas() == null
                        || d.getNumeroEquipos() == null || d.getEstructuraTorneo() == null) {
                    throw new IllegalArgumentException(
                            "Torneo requiere: tipoDeporte, reglas, numeroEquipos, estructuraTorneo");
                }
                yield new TorneoDeportivo(d.getTipoDeporte(), d.getReglas(),
                        d.getNumeroEquipos(), d.getEstructuraTorneo());
            }
            case VOLUNTARIADO -> {
                if (d.getCausa() == null || d.getNumeroHorasRequeridas() == null
                        || d.getActividades() == null || d.getPuntosEncuentro() == null
                        || d.getResponsables() == null) {
                    throw new IllegalArgumentException(
                            "Voluntariado requiere: causa, numeroHorasRequeridas, actividades, puntosEncuentro, responsables");
                }
                yield new ActividadVoluntariado(d.getCausa(), d.getNumeroHorasRequeridas(),
                        d.getActividades(), d.getPuntosEncuentro(), d.getResponsables());
            }
            case OTRO -> new OtroEvento(
                    d.getDescripcionAdicional() != null ? d.getDescripcionAdicional() : "");
        };
    }

    // -------------------------------------------------------------------------
    // Listar y obtener
    // -------------------------------------------------------------------------

    @Override
    public List<Evento> listarEventos(TipoEvento tipo, EstadoEvento estado, Date inicio, Date fin) {
        actualizarEstadosEventos();

        if (tipo != null && estado != null) {
            return eventoRepository.findByTipoAndEstado(tipo, estado);
        }
        if (tipo != null) {
            return eventoRepository.findByTipo(tipo);
        }
        if (estado != null) {
            if (inicio != null && fin != null) {
                return eventoRepository.findByEstadoAndFechaHoraInicioBetween(estado, inicio, fin);
            }
            return eventoRepository.findByEstado(estado);
        }
        if (inicio != null && fin != null) {
            return eventoRepository.findByFechaHoraInicioBetween(inicio, fin);
        }
        return eventoRepository.findAll();
    }

    @Override
    public Evento obtenerEvento(String id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado: " + id));
    }

    @Override
    public List<Evento> listarEventosPorOrganizador(String correoOrganizador) {
        return eventoRepository.findByOrganizadorCorreo(correoOrganizador);
    }

    @Override
    public List<Evento> listarEventosPorEstudiante(String correoEstudiante) {
        return eventoRepository.findByInscripcionesCorreo(correoEstudiante);
    }

    // -------------------------------------------------------------------------
    // Inscripción
    // -------------------------------------------------------------------------

    @Override
    public void inscribirEstudiante(String eventoId, String correoEstudiante) {
        Evento evento = obtenerEvento(eventoId);
        Usuario usuario = usuarioRepository.findByCorreo(correoEstudiante)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getTipo() != TipoUsuario.ESTUDIANTE) {
            throw new IllegalArgumentException("Solo los estudiantes pueden inscribirse a eventos");
        }

        EstadoEvento estado = evento.getEstado();
        if (estado == EstadoEvento.FINALIZADO || estado == EstadoEvento.CANCELADO) {
            throw new IllegalArgumentException("El evento no está disponible para inscripción (estado: " + estado + ")");
        }

        if (evento.getCuposDisponibles() <= 0) {
            throw new IllegalArgumentException("No hay cupos disponibles en este evento");
        }

        boolean yaInscrito = evento.getInscripciones().stream()
                .anyMatch(i -> i.getCorreo().equalsIgnoreCase(correoEstudiante));
        if (yaInscrito) {
            throw new IllegalArgumentException("El estudiante ya está inscrito en este evento");
        }

        // Validaciones específicas por tipo de evento
        validarInscripcionPorTipo(evento, usuario);

        Estudiante estudianteDatos = (Estudiante) usuario.getDatosEspecificos();
        Inscripcion inscripcion = new Inscripcion(
                new ObjectId(),
                usuario.getNombre(),
                correoEstudiante,
                estudianteDatos.getCodigo(),
                new Date(),
                false);

        evento.getInscripciones().add(inscripcion);
        evento.setTotalInscritos(evento.getTotalInscritos() + 1);
        evento.setCuposDisponibles(evento.getCuposDisponibles() - 1);

        eventoRepository.save(evento);
        estadisticaService.registrarInscripcion(eventoId);
    }

    private void validarInscripcionPorTipo(Evento evento, Usuario usuario) {
        Estudiante estudianteDatos = (Estudiante) usuario.getDatosEspecificos();
        String codigoEstudiante = estudianteDatos.getCodigo();

        switch (evento.getTipo()) {
            case TALLER -> validarTaller(evento, codigoEstudiante);
            case TORNEO -> validarTorneo(evento, usuario.getCorreo());
            case VOLUNTARIADO -> validarVoluntariado(evento, usuario.getCorreo());
            case CHARLA, OTRO -> { /* Sin validaciones adicionales */ }
        }
    }

    /**
     * Verifica condiciones previas del taller consultando la BD institucional.
     * - "SEMESTRE:N" → el estudiante debe haber completado al menos N semestres distintos
     * - Cualquier otro valor → código o nombre de materia que debe estar completada
     */
    private void validarTaller(Evento evento, String codigoEstudiante) {
        Taller taller = (Taller) evento.getDatosEspecificos();
        List<Enrollment> completadas = enrollmentRepository.findCompletedEnrollmentsByStudent(codigoEstudiante);

        for (String condicion : taller.getCondicionesPrevias()) {
            if (condicion.toUpperCase().startsWith("SEMESTRE:")) {
                int semRequired = Integer.parseInt(condicion.substring(9).trim());
                long semestresCompletados = completadas.stream()
                        .map(e -> e.getGroup().getSemester())
                        .distinct()
                        .count();
                if (semestresCompletados < semRequired) {
                    throw new IllegalArgumentException(String.format(
                            "Se requieren %d semestres completados. El estudiante tiene %d.",
                            semRequired, semestresCompletados));
                }
            } else {
                boolean cumple = completadas.stream().anyMatch(e -> {
                    Subject subject = e.getGroup().getSubject();
                    return subject.getCode().equalsIgnoreCase(condicion)
                            || subject.getName().equalsIgnoreCase(condicion);
                });
                if (!cumple) {
                    throw new IllegalArgumentException(
                            "El estudiante no cumple el requisito previo: " + condicion);
                }
            }
        }
    }

    /**
     * Verifica que el estudiante no tenga otro torneo con horario traslapado.
     */
    private void validarTorneo(Evento eventoNuevo, String correoEstudiante) {
        List<Evento> torneos = eventoRepository.findByTipo(TipoEvento.TORNEO);
        boolean tieneTraslape = torneos.stream()
                .filter(t -> !t.getId().equals(eventoNuevo.getId()))
                .filter(t -> t.getEstado() != EstadoEvento.FINALIZADO
                          && t.getEstado() != EstadoEvento.CANCELADO)
                .filter(t -> t.getInscripciones().stream()
                        .anyMatch(i -> i.getCorreo().equalsIgnoreCase(correoEstudiante)))
                .anyMatch(t -> hayTraslape(t, eventoNuevo));

        if (tieneTraslape) {
            throw new IllegalArgumentException(
                    "El estudiante ya tiene inscripción en otro torneo con horario traslapado");
        }
    }

    private boolean hayTraslape(Evento e1, Evento e2) {
        return e1.getFechaHoraInicio().before(e2.getFechaHoraFin())
                && e2.getFechaHoraInicio().before(e1.getFechaHoraFin());
    }

    /**
     * Verifica que el estudiante tenga suficientes horas de voluntariado previas confirmadas.
     * Las horas se acumulan de participaciones confirmadas en voluntariados ya finalizados.
     */
    private void validarVoluntariado(Evento eventoNuevo, String correoEstudiante) {
        ActividadVoluntariado voluntariado = (ActividadVoluntariado) eventoNuevo.getDatosEspecificos();
        int minHoras = voluntariado.getNumeroHorasRequeridas();

        List<Evento> voluntariadosFinalizados = eventoRepository.findByTipo(TipoEvento.VOLUNTARIADO).stream()
                .filter(v -> !v.getId().equals(eventoNuevo.getId()))
                .filter(v -> v.getEstado() == EstadoEvento.FINALIZADO)
                .collect(Collectors.toList());

        int horasAcumuladas = voluntariadosFinalizados.stream()
                .filter(v -> v.getInscripciones().stream()
                        .anyMatch(i -> i.getCorreo().equalsIgnoreCase(correoEstudiante)
                                    && Boolean.TRUE.equals(i.getConfirmada())))
                .mapToInt(v -> ((ActividadVoluntariado) v.getDatosEspecificos()).getNumeroHorasRequeridas())
                .sum();

        if (horasAcumuladas < minHoras) {
            throw new IllegalArgumentException(String.format(
                    "Se requieren %d horas de voluntariado previas. El estudiante tiene %d horas confirmadas.",
                    minHoras, horasAcumuladas));
        }
    }

    // -------------------------------------------------------------------------
    // Cancelación
    // -------------------------------------------------------------------------

    @Override
    public void cancelarInscripcion(String eventoId, String correoEstudiante) {
        Evento evento = obtenerEvento(eventoId);

        Inscripcion inscripcion = evento.getInscripciones().stream()
                .filter(i -> i.getCorreo().equalsIgnoreCase(correoEstudiante))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El estudiante no tiene inscripción en este evento"));

        evento.getInscripciones().remove(inscripcion);
        evento.setTotalInscritos(Math.max(0, evento.getTotalInscritos() - 1));
        evento.setCuposDisponibles(evento.getCuposDisponibles() + 1);

        eventoRepository.save(evento);
        estadisticaService.registrarCancelacion(eventoId);
    }

    // -------------------------------------------------------------------------
    // Inscritos y CSV
    // -------------------------------------------------------------------------

    @Override
    public List<Inscripcion> listarInscritos(String eventoId, String correoOrganizador) {
        Evento evento = eventoRepository.findByIdAndOrganizadorCorreo(eventoId, correoOrganizador)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Evento no encontrado o no pertenece a este organizador"));
        return evento.getInscripciones();
    }

    @Override
    public String exportarInscritosCSV(String eventoId, String correoOrganizador) {
        List<Inscripcion> inscritos = listarInscritos(eventoId, correoOrganizador);

        StringBuilder csv = new StringBuilder();
        csv.append("Nombre,Codigo Estudiante,Correo,Fecha Inscripcion,Confirmada\n");

        for (Inscripcion i : inscritos) {
            csv.append(String.format("%s,%s,%s,%s,%s\n",
                    i.getNombre(),
                    i.getCodigoEstudiante() != null ? i.getCodigoEstudiante() : "",
                    i.getCorreo(),
                    i.getFechaInscripcion() != null ? i.getFechaInscripcion().toString() : "",
                    Boolean.TRUE.equals(i.getConfirmada()) ? "Si" : "No"));
        }

        return csv.toString();
    }

    // -------------------------------------------------------------------------
    // Actualización automática de estados (cada minuto)
    // -------------------------------------------------------------------------

    @Override
    @Scheduled(fixedRate = 60_000)
    public void actualizarEstadosEventos() {
        Date ahora = new Date();
        List<Evento> eventos = eventoRepository.findAll();

        for (Evento evento : eventos) {
            if (evento.getEstado() == EstadoEvento.CANCELADO) continue;

            EstadoEvento nuevoEstado;
            if (ahora.before(evento.getFechaHoraInicio())) {
                nuevoEstado = EstadoEvento.PROGRAMADO;
            } else if (ahora.before(evento.getFechaHoraFin())) {
                nuevoEstado = EstadoEvento.ACTIVO;
            } else {
                nuevoEstado = EstadoEvento.FINALIZADO;
            }

            if (!nuevoEstado.equals(evento.getEstado())) {
                evento.setEstado(nuevoEstado);
                eventoRepository.save(evento);
            }
        }
    }
}
