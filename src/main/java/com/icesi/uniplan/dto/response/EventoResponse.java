package com.icesi.uniplan.dto.response;

import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class EventoResponse {
    private String id;
    private String titulo;
    private String descripcion;
    private TipoEvento tipo;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String ubicacion;
    private Integer maxAsistentes;
    private Integer totalInscritos;
    private Integer cuposDisponibles;
    private EstadoEvento estado;
    private String organizadorNombre;
    private String organizadorCorreo;
    private DatosEspecificos datosEspecificos;
}
