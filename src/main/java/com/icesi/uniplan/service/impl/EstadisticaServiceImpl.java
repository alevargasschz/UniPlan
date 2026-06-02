package com.icesi.uniplan.service.impl;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.dto.response.EstadisticasDashboardResponse;
import com.icesi.uniplan.dto.response.ReporteInsightResponse;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.postgres.EventoEstadistica;
import com.icesi.uniplan.model.postgres.Enrollment;
import com.icesi.uniplan.repository.mongo.IEventoRepository;
import com.icesi.uniplan.repository.postgres.IEnrollmentRepository;
import com.icesi.uniplan.repository.postgres.IEventoEstadisticaRepository;
import com.icesi.uniplan.service.IEstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticaServiceImpl implements IEstadisticaService {

    private final IEventoEstadisticaRepository estadisticaRepository;
    private final IEventoRepository eventoRepository;
    private final IEnrollmentRepository enrollmentRepository;

    @Override
    public void crearEstadistica(Evento evento) {
        EventoEstadistica stats = EventoEstadistica.builder()
                .eventoId(evento.getId())
                .titulo(evento.getTitulo())
                .tipo(evento.getTipo().getDbValue())
                .maxAsistentes(evento.getMaxAsistentes())
                .totalInscritos(0)
                .totalCancelaciones(0)
                .porcentajeOcupacion(0.0)
                .fechaUltimaActualizacion(LocalDateTime.now())
                .build();
        estadisticaRepository.save(stats);
    }

    @Override
    public void registrarInscripcion(String eventoId) {
        estadisticaRepository.findById(eventoId).ifPresent(stats -> {
            int nuevosInscritos = stats.getTotalInscritos() + 1;
            stats.setTotalInscritos(nuevosInscritos);
            stats.setPorcentajeOcupacion(calcularPorcentaje(nuevosInscritos, stats.getMaxAsistentes()));
            stats.setFechaUltimaActualizacion(LocalDateTime.now());
            estadisticaRepository.save(stats);
        });
    }

    @Override
    public void registrarCancelacion(String eventoId) {
        estadisticaRepository.findById(eventoId).ifPresent(stats -> {
            int nuevosInscritos = Math.max(0, stats.getTotalInscritos() - 1);
            stats.setTotalInscritos(nuevosInscritos);
            stats.setTotalCancelaciones(stats.getTotalCancelaciones() + 1);
            stats.setPorcentajeOcupacion(calcularPorcentaje(nuevosInscritos, stats.getMaxAsistentes()));
            stats.setFechaUltimaActualizacion(LocalDateTime.now());
            estadisticaRepository.save(stats);
        });
    }

    @Override
    public void registrarAsistencia(String eventoId) {
        estadisticaRepository.findById(eventoId).ifPresent(stats -> {
            stats.setTotalAsistentes(stats.getTotalAsistentes() + 1);
            stats.setFechaUltimaActualizacion(LocalDateTime.now());
            estadisticaRepository.save(stats);
        });
    }

    @Override
    public void actualizarEstadistica(Evento evento) {
        estadisticaRepository.findById(evento.getId()).ifPresentOrElse(stats -> {
            stats.setTitulo(evento.getTitulo());
            stats.setTipo(evento.getTipo().getDbValue());
            stats.setMaxAsistentes(evento.getMaxAsistentes());
            stats.setTotalInscritos(evento.getTotalInscritos());
            stats.setPorcentajeOcupacion(calcularPorcentaje(evento.getTotalInscritos(), evento.getMaxAsistentes()));
            stats.setFechaUltimaActualizacion(LocalDateTime.now());
            estadisticaRepository.save(stats);
        }, () -> crearEstadistica(evento));
    }

    @Override
    public void eliminarEstadistica(String eventoId) {
        if (estadisticaRepository.existsById(eventoId)) {
            estadisticaRepository.deleteById(eventoId);
        }
    }

    @Override
    public List<EstadisticaResponse> obtenerEstadisticas() {
        return estadisticaRepository.findAllOrderByTotalInscritosDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EstadisticaResponse> obtenerTopEventosPorOcupacion(int limit) {
        return estadisticaRepository.findAllOrderByPorcentajeOcupacionDesc().stream()
                .limit(limit)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerSemestresAcademicosDisponibles() {
        return eventoRepository.findAll().stream()
                .filter(e -> e.getFechaHoraInicio() != null)
                .map(e -> toSemestreAcademico(e.getFechaHoraInicio()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> obtenerEventoIdsFiltrados(String semestreAcademico, LocalDate fechaInicio, LocalDate fechaFin) {
        return filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin).stream()
                .map(Evento::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReporteInsightResponse> obtenerTopSemestresParticipacion(int limit) {
        return obtenerTopSemestresParticipacion(limit, null, null, null);
    }

    @Override
    public List<ReporteInsightResponse> obtenerTopSemestresParticipacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        return topSemestresDesdeEventos(limit, filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin),
                new HashMap<>());
    }

    @Override
    public List<ReporteInsightResponse> obtenerTopProgramasParticipacion(int limit) {
        return obtenerTopProgramasParticipacion(limit, null, null, null);
    }

    @Override
    public List<ReporteInsightResponse> obtenerTopProgramasParticipacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        return topProgramasDesdeEventos(limit, filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin),
                new HashMap<>());
    }

    @Override
    public List<ReporteInsightResponse> obtenerDemandaPorTipoEvento(int limit) {
        return obtenerDemandaPorTipoEvento(limit, null, null, null);
    }

    @Override
    public List<ReporteInsightResponse> obtenerDemandaPorTipoEvento(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        return demandaPorTipoDesdeEventos(limit, filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin));
    }

    @Override
    public List<ReporteInsightResponse> obtenerEventosMayorTasaCancelacion(int limit) {
        return obtenerEventosMayorTasaCancelacion(limit, null, null, null);
    }

    @Override
    public List<ReporteInsightResponse> obtenerEventosMayorTasaCancelacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        boolean hayFiltros = (semestreAcademico != null && !semestreAcademico.isBlank()) || fechaInicio != null
                || fechaFin != null;
        Set<String> eventoIdsFiltrados = new HashSet<>(
                obtenerEventoIdsFiltrados(semestreAcademico, fechaInicio, fechaFin));
        return mayorCancelacionDesdeStats(limit, hayFiltros, eventoIdsFiltrados, estadisticaRepository.findAll());
    }

    @Override
    public List<ReporteInsightResponse> obtenerAsistenciaPorTipoEvento() {
        return obtenerAsistenciaPorTipoEvento(null, null, null);
    }

    @Override
    public List<ReporteInsightResponse> obtenerAsistenciaPorTipoEvento(
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        Map<String, EventoEstadistica> statsById = estadisticaRepository.findAll().stream()
                .collect(Collectors.toMap(EventoEstadistica::getEventoId, s -> s, (a, b) -> a));
        return asistenciaPorTipoDesdeEventos(filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin),
                statsById);
    }

    @Override
    public EstadisticasDashboardResponse obtenerDashboardFiltrado(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin) {
        List<Evento> eventosFiltrados = filtrarEventosPorPeriodo(semestreAcademico, fechaInicio, fechaFin);
        List<String> eventoIdsFiltrados = eventosFiltrados.stream().map(Evento::getId).collect(Collectors.toList());
        Set<String> eventoIdsSet = new HashSet<>(eventoIdsFiltrados);
        boolean hayFiltros = (semestreAcademico != null && !semestreAcademico.isBlank()) || fechaInicio != null
                || fechaFin != null;

        Map<String, Enrollment> cacheEnrollment = new HashMap<>();
        List<ReporteInsightResponse> topSemestres = topSemestresDesdeEventos(limit, eventosFiltrados, cacheEnrollment);
        List<ReporteInsightResponse> topProgramas = topProgramasDesdeEventos(limit, eventosFiltrados, cacheEnrollment);
        List<ReporteInsightResponse> demandaPorTipo = demandaPorTipoDesdeEventos(limit, eventosFiltrados);

        List<EventoEstadistica> allStats = estadisticaRepository.findAll();
        List<ReporteInsightResponse> mayorCancelacion = mayorCancelacionDesdeStats(limit, hayFiltros, eventoIdsSet,
                allStats);
        Map<String, EventoEstadistica> statsById = allStats.stream()
                .collect(Collectors.toMap(EventoEstadistica::getEventoId, s -> s, (a, b) -> a));
        List<ReporteInsightResponse> asistenciaPorTipo = asistenciaPorTipoDesdeEventos(eventosFiltrados, statsById);

        return new EstadisticasDashboardResponse(
                eventoIdsFiltrados,
                topSemestres,
                topProgramas,
                demandaPorTipo,
                mayorCancelacion,
                asistenciaPorTipo);
    }

    @SuppressWarnings("null")
    private List<ReporteInsightResponse> topSemestresDesdeEventos(
            int limit,
            List<Evento> eventosFiltrados,
            Map<String, Enrollment> cacheEnrollment) {
        Map<String, Double> acumuladoPorSemestre = new HashMap<>();

        for (Evento evento : eventosFiltrados) {
            for (Inscripcion inscripcion : evento.getInscripciones()) {
                String codigo = inscripcion.getCodigoEstudiante();
                if (codigo == null || codigo.isBlank()) {
                    continue;
                }

                Enrollment enrollment = cacheEnrollment.computeIfAbsent(
                        codigo,
                        key -> enrollmentRepository.findTopByStudent_IdOrderByEnrollmentDateDesc(key).orElse(null));

                if (enrollment == null || enrollment.getGroup() == null
                        || enrollment.getGroup().getSemester() == null) {
                    continue;
                }

                String semestre = enrollment.getGroup().getSemester();
                acumuladoPorSemestre.merge(semestre, 1.0, Double::sum);
            }
        }

        return ordenarYLimitar(acumuladoPorSemestre, limit);
    }

    @SuppressWarnings("null")
    private List<ReporteInsightResponse> topProgramasDesdeEventos(
            int limit,
            List<Evento> eventosFiltrados,
            Map<String, Enrollment> cacheEnrollment) {
        Map<String, Double> acumuladoPorPrograma = new HashMap<>();

        for (Evento evento : eventosFiltrados) {
            for (Inscripcion inscripcion : evento.getInscripciones()) {
                String codigo = inscripcion.getCodigoEstudiante();
                if (codigo == null || codigo.isBlank()) {
                    continue;
                }

                Enrollment enrollment = cacheEnrollment.computeIfAbsent(
                        codigo,
                        key -> enrollmentRepository.findTopByStudent_IdOrderByEnrollmentDateDesc(key).orElse(null));

                if (enrollment == null
                        || enrollment.getGroup() == null
                        || enrollment.getGroup().getSubject() == null
                        || enrollment.getGroup().getSubject().getProgram() == null
                        || enrollment.getGroup().getSubject().getProgram().getName() == null) {
                    continue;
                }

                String programa = enrollment.getGroup().getSubject().getProgram().getName();
                acumuladoPorPrograma.merge(programa, 1.0, Double::sum);
            }
        }

        return ordenarYLimitar(acumuladoPorPrograma, limit);
    }

    @SuppressWarnings("null")
    private List<ReporteInsightResponse> demandaPorTipoDesdeEventos(int limit, List<Evento> eventosFiltrados) {
        Map<String, Double> demandaPorTipo = new HashMap<>();

        for (Evento evento : eventosFiltrados) {
            String tipo = evento.getTipo() != null ? evento.getTipo().getDbValue() : "desconocido";
            double inscritos = evento.getInscripciones() != null ? evento.getInscripciones().size() : 0;
            demandaPorTipo.merge(tipo, inscritos, Double::sum);
        }

        return ordenarYLimitar(demandaPorTipo, limit);
    }

    private List<ReporteInsightResponse> mayorCancelacionDesdeStats(
            int limit,
            boolean hayFiltros,
            Set<String> eventoIdsFiltrados,
            List<EventoEstadistica> allStats) {
        if (hayFiltros && eventoIdsFiltrados.isEmpty()) {
            return List.of();
        }

        return allStats.stream()
                .filter(stats -> !hayFiltros || eventoIdsFiltrados.contains(stats.getEventoId()))
                .map(stats -> {
                    double totalIntentos = stats.getTotalInscritos() + stats.getTotalCancelaciones();
                    double tasa = totalIntentos > 0
                            ? Math.round((stats.getTotalCancelaciones() / totalIntentos) * 10000.0) / 100.0
                            : 0.0;
                    return new ReporteInsightResponse(stats.getTitulo(), tasa);
                })
                .sorted(Comparator.comparing(ReporteInsightResponse::getValor).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<ReporteInsightResponse> asistenciaPorTipoDesdeEventos(
            List<Evento> eventosFiltrados,
            Map<String, EventoEstadistica> statsById) {
        Map<String, int[]> acumulado = new HashMap<>();

        for (Evento evento : eventosFiltrados) {
            EventoEstadistica stats = statsById.get(evento.getId());
            if (stats == null) {
                continue;
            }

            String tipo = evento.getTipo() != null ? evento.getTipo().getDbValue() : "desconocido";
            int[] valores = acumulado.computeIfAbsent(tipo, key -> new int[] { 0, 0 });
            valores[0] += stats.getTotalAsistentes() != null ? stats.getTotalAsistentes() : 0;
            valores[1] += stats.getTotalInscritos() != null ? stats.getTotalInscritos() : 0;
        }

        return acumulado.entrySet().stream()
                .map(entry -> {
                    int asistentes = entry.getValue()[0];
                    int inscritos = entry.getValue()[1];
                    double tasa = inscritos > 0
                            ? Math.round(((double) asistentes / inscritos) * 10000.0) / 100.0
                            : 0.0;
                    return new ReporteInsightResponse(entry.getKey(), tasa);
                })
                .sorted(Comparator.comparing(ReporteInsightResponse::getValor).reversed())
                .collect(Collectors.toList());
    }

    private double calcularPorcentaje(int inscritos, int max) {
        if (max == 0)
            return 0.0;
        return Math.round((double) inscritos / max * 100 * 100.0) / 100.0;
    }

    private EstadisticaResponse toResponse(EventoEstadistica stats) {
        EstadisticaResponse r = new EstadisticaResponse();
        r.setEventoId(stats.getEventoId());
        r.setTitulo(stats.getTitulo());
        r.setTipo(stats.getTipo());
        r.setMaxAsistentes(stats.getMaxAsistentes());
        r.setTotalInscritos(stats.getTotalInscritos());
        r.setTotalCancelaciones(stats.getTotalCancelaciones());
        r.setTotalAsistentes(stats.getTotalAsistentes());
        r.setPorcentajeOcupacion(stats.getPorcentajeOcupacion());
        return r;
    }

    private List<ReporteInsightResponse> ordenarYLimitar(Map<String, Double> valores, int limit) {
        return valores.entrySet().stream()
                .map(entry -> new ReporteInsightResponse(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(ReporteInsightResponse::getValor).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<Evento> filtrarEventosPorPeriodo(String semestreAcademico, LocalDate fechaInicio, LocalDate fechaFin) {
        return eventoRepository.findAll().stream()
                .filter(evento -> evento.getFechaHoraInicio() != null)
                .filter(evento -> {
                    if (semestreAcademico == null || semestreAcademico.isBlank()) {
                        return true;
                    }
                    return toSemestreAcademico(evento.getFechaHoraInicio()).equalsIgnoreCase(semestreAcademico);
                })
                .filter(evento -> {
                    LocalDate fechaEvento = evento.getFechaHoraInicio().toLocalDate();
                    boolean cumpleInicio = fechaInicio == null || !fechaEvento.isBefore(fechaInicio);
                    boolean cumpleFin = fechaFin == null || !fechaEvento.isAfter(fechaFin);
                    return cumpleInicio && cumpleFin;
                })
                .collect(Collectors.toList());
    }

    private String toSemestreAcademico(LocalDateTime fechaHora) {
        int semestre = fechaHora.getMonthValue() <= 6 ? 1 : 2;
        return fechaHora.getYear() + "-" + semestre;
    }
}
