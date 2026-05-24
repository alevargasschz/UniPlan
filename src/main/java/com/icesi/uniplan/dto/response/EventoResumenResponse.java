package com.icesi.uniplan.dto.response;

import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import lombok.Data;

import java.util.Date;

@Data
public class EventoResumenResponse {
    private String id;
    private String titulo;
    private TipoEvento tipo;
    private Date fechaHoraInicio;
    private Date fechaHoraFin;
    private String ubicacion;
    private Integer cuposDisponibles;
    private Integer maxAsistentes;
    private EstadoEvento estado;
    private String organizadorNombre;
}
