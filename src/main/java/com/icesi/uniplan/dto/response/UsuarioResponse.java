package com.icesi.uniplan.dto.response;

import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import lombok.Data;

@Data
public class UsuarioResponse {
    private String id;
    private String nombre;
    private String correo;
    private TipoUsuario tipo;
}
