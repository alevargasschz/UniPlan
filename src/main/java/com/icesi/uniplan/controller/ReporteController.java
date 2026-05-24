package com.icesi.uniplan.controller;

import com.icesi.uniplan.dto.response.ApiResponse;
import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.dto.response.EventoResumenResponse;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.service.IEstadisticaService;
import com.icesi.uniplan.service.IEventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
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
    public ResponseEntity<ApiResponse<List<EstadisticaResponse>>> topEventosPorOcupacion(
            @RequestParam(defaultValue = "10") int limite) {
        return ResponseEntity.ok(ApiResponse.ok(
                estadisticaService.obtenerTopEventosPorOcupacion(limite)));
    }

    /**
     * Informe 2: Historial de inscripciones del estudiante autenticado.
     * Fuente: MongoDB (consulta por correo en las inscripciones de cada evento).
     * Útil para que el estudiante vea en qué eventos participó o está inscrito.
     */
    @GetMapping("/mis-inscripciones")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<ApiResponse<List<EventoResumenResponse>>> misInscripciones(
            Authentication authentication) {

        List<EventoResumenResponse> response = eventoService
                .listarEventosPorEstudiante(authentication.getName())
                .stream().map(this::toResumen).collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Estadísticas completas de todos los eventos (acceso admin/organizadores).
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('PROFESOR', 'LIDER_ESTUDIANTIL', 'BIENESTAR')")
    public ResponseEntity<ApiResponse<List<EstadisticaResponse>>> estadisticasCompletas() {
        return ResponseEntity.ok(ApiResponse.ok(estadisticaService.obtenerEstadisticas()));
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
