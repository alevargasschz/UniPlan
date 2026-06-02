package com.icesi.uniplan.dto.response;

import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;
    private String correo;
    private String nombre;
    private TipoUsuario tipo;
}
