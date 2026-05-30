package com.icesi.uniplan.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class InscritoResponse {
    private String nombre;
    private String codigoEstudiante;
    private String correo;
    private LocalDateTime fechaInscripcion;
    private Boolean confirmada;
}
