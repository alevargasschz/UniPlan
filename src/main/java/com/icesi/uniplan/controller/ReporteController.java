package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.dto.response.EventoResumenResponse;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.service.IEstadisticaService;
import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
    public String estadisticasCompletas(Model model) {
        List<EstadisticaResponse> estadisticas = estadisticaService.obtenerEstadisticas();
        model.addAttribute("estadisticas", estadisticas);
        return "reportes/estadisticas";
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
