package com.icesi.uniplan.dto.response;

import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventoResumenResponse {
    private String id;
    private String titulo;
    private TipoEvento tipo;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String ubicacion;
    private Integer cuposDisponibles;
    private Integer maxAsistentes;
    private EstadoEvento estado;
    private String organizadorNombre;
}
