package com.icesi.uniplan.dto.response;

import lombok.Data;

@Data
public class EstadisticaResponse {
    private String eventoId;
    private String titulo;
    private String tipo;
    private Integer maxAsistentes;
    private Integer totalInscritos;
    private Integer totalCancelaciones;
    private Double porcentajeOcupacion;
}
