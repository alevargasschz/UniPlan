package com.icesi.uniplan.service;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.dto.response.EstadisticasDashboardResponse;
import com.icesi.uniplan.dto.response.ReporteInsightResponse;
import com.icesi.uniplan.model.mongo.Evento;

import java.time.LocalDate;
import java.util.List;

public interface IEstadisticaService {

    void crearEstadistica(Evento evento);

    void registrarInscripcion(String eventoId);

    void registrarCancelacion(String eventoId);

    void registrarAsistencia(String eventoId);

    void actualizarEstadistica(Evento evento);

    void eliminarEstadistica(String eventoId);

    List<EstadisticaResponse> obtenerEstadisticas();

    List<EstadisticaResponse> obtenerTopEventosPorOcupacion(int limit);

    List<String> obtenerSemestresAcademicosDisponibles();

    List<String> obtenerEventoIdsFiltrados(String semestreAcademico, LocalDate fechaInicio, LocalDate fechaFin);

    List<ReporteInsightResponse> obtenerTopSemestresParticipacion(int limit);

    List<ReporteInsightResponse> obtenerTopSemestresParticipacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<ReporteInsightResponse> obtenerTopProgramasParticipacion(int limit);

    List<ReporteInsightResponse> obtenerTopProgramasParticipacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<ReporteInsightResponse> obtenerDemandaPorTipoEvento(int limit);

    List<ReporteInsightResponse> obtenerDemandaPorTipoEvento(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<ReporteInsightResponse> obtenerEventosMayorTasaCancelacion(int limit);

    List<ReporteInsightResponse> obtenerEventosMayorTasaCancelacion(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    List<ReporteInsightResponse> obtenerAsistenciaPorTipoEvento();

    List<ReporteInsightResponse> obtenerAsistenciaPorTipoEvento(
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);

    EstadisticasDashboardResponse obtenerDashboardFiltrado(
            int limit,
            String semestreAcademico,
            LocalDate fechaInicio,
            LocalDate fechaFin);
}
