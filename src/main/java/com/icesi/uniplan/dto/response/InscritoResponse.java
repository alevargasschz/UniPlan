package com.icesi.uniplan.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InscritoResponse {
    private String id;
    private String nombre;
    private String codigoEstudiante;
    private String correo;
    private LocalDateTime fechaInscripcion;
    private Boolean confirmada;
}
