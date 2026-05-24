package com.icesi.uniplan.dto.response;

import lombok.Data;

import java.util.Date;

@Data
public class InscritoResponse {
    private String nombre;
    private String codigoEstudiante;
    private String correo;
    private Date fechaInscripcion;
    private Boolean confirmada;
}
