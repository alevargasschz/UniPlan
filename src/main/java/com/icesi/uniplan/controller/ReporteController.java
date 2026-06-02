package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.dto.response.EstadisticasDashboardResponse;
import com.icesi.uniplan.dto.response.EventoResumenResponse;
import com.icesi.uniplan.dto.response.ReporteInsightResponse;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.service.IEstadisticaService;
import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final IEstadisticaService estadisticaService;
    private final IEventoService eventoService;

    /**
     * Informe 1: Top de eventos por porcentaje de ocupación.
     * Fuente: PostgreSQL (estadísticas agregadas).
     * Útil para identificar los eventos más populares.
     */
    @GetMapping("/top-eventos")
    public String topEventosPorOcupacion(
            @RequestParam(defaultValue = "10") int limite,
            Model model) {
        List<EstadisticaResponse> estadisticas = estadisticaService.obtenerTopEventosPorOcupacion(limite);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("limite", limite);
        return "reportes/top-eventos";
    }

    /**
     * Informe 2: Historial de inscripciones del estudiante autenticado.
     * Fuente: MongoDB (consulta por correo en las inscripciones de cada evento).
     * Útil para que el estudiante vea en qué eventos participó o está inscrito.
     */
    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasAnyRole('PROFESOR', 'LIDER_ESTUDIANTIL', 'BIENESTAR','ESTUDIANTE')")
    public String misInscripciones(
            Authentication authentication,
            Model model) {

        List<EventoResumenResponse> eventos = eventoService
                .listarEventosPorEstudiante(authentication.getName())
                .stream().map(this::toResumen).collect(Collectors.toList());

        model.addAttribute("eventos", eventos);
        return "reportes/mis-inscripciones";
    }

    /**
     * Estadísticas completas de todos los eventos (acceso admin/organizadores).
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('PROFESOR', 'LIDER_ESTUDIANTIL', 'BIENESTAR')")
    public String estadisticasCompletas(
            @RequestParam(required = false) String semestreAcademico,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Model model) {

        if (fechaInicio != null && fechaFin != null && fechaInicio.isAfter(fechaFin)) {
            LocalDate temporal = fechaInicio;
            fechaInicio = fechaFin;
            fechaFin = temporal;
        }

        EstadisticasDashboardResponse dashboard = estadisticaService.obtenerDashboardFiltrado(6, semestreAcademico,
                fechaInicio, fechaFin);
        Set<String> filtroIds = new HashSet<>(dashboard.getEventoIdsFiltrados());
        boolean hayFiltros = (semestreAcademico != null && !semestreAcademico.isBlank()) || fechaInicio != null
                || fechaFin != null;

        List<EstadisticaResponse> estadisticas = estadisticaService.obtenerEstadisticas().stream()
                .filter(e -> !hayFiltros || filtroIds.contains(e.getEventoId()))
                .collect(Collectors.toList());
        model.addAttribute("estadisticas", estadisticas);

        addInsight(model, "topSemestres", dashboard.getTopSemestres());
        addInsight(model, "topProgramas", dashboard.getTopProgramas());
        addInsight(model, "demandaPorTipo", dashboard.getDemandaPorTipo());
        addInsight(model, "mayorCancelacion", dashboard.getMayorCancelacion());
        addInsight(model, "asistenciaPorTipo", dashboard.getAsistenciaPorTipo());

        model.addAttribute("semestresAcademicos", estadisticaService.obtenerSemestresAcademicosDisponibles());
        model.addAttribute("semestreAcademico", semestreAcademico);
        model.addAttribute("fechaInicio", fechaInicio);
        model.addAttribute("fechaFin", fechaFin);

        return "reportes/estadisticas";
    }

    private void addInsight(Model model, String key, List<ReporteInsightResponse> data) {
        List<ReporteInsightResponse> safeData = data != null ? data : Collections.emptyList();
        model.addAttribute(key, safeData);
        model.addAttribute(key + "Labels",
                safeData.stream().map(ReporteInsightResponse::getEtiqueta).collect(Collectors.toList()));
        model.addAttribute(key + "Values",
                safeData.stream().map(ReporteInsightResponse::getValor).collect(Collectors.toList()));
    }

    private EventoResumenResponse toResumen(Evento e) {
        EventoResumenResponse r = new EventoResumenResponse();
        r.setId(e.getId());
        r.setTitulo(e.getTitulo());
        r.setTipo(e.getTipo());
        r.setFechaHoraInicio(e.getFechaHoraInicio());
        r.setFechaHoraFin(e.getFechaHoraFin());
        r.setUbicacion(e.getUbicacion());
        r.setCuposDisponibles(e.getCuposDisponibles());
        r.setMaxAsistentes(e.getMaxAsistentes());
        r.setEstado(e.getEstado());
        r.setOrganizadorNombre(e.getOrganizador() != null ? e.getOrganizador().getNombre() : null);
        return r;
    }
}
