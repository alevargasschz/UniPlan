package com.icesi.uniplan.service.impl;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.postgres.EventoEstadistica;
import com.icesi.uniplan.repository.postgres.IEventoEstadisticaRepository;
import com.icesi.uniplan.service.IEstadisticaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EstadisticaServiceImpl implements IEstadisticaService {

    private final IEventoEstadisticaRepository estadisticaRepository;

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

    private double calcularPorcentaje(int inscritos, int max) {
        if (max == 0) return 0.0;
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
        r.setPorcentajeOcupacion(stats.getPorcentajeOcupacion());
        return r;
    }
}
