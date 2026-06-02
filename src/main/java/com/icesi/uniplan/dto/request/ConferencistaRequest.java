package com.icesi.uniplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConferencistaRequest {

    @NotBlank(message = "El nombre del conferencista es requerido")
    private String nombre;

    @NotBlank(message = "El perfil del conferencista es requerido")
    private String perfil;

    @NotBlank(message = "La afiliación del conferencista es requerida")
    private String afiliacion;
}
