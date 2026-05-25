package com.icesi.uniplan.dto.request;

import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroOrganizadorRequest {

    @NotBlank(message = "El nombre es requerido")
    private String nombre;

    @NotBlank(message = "El correo es requerido")
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;

    @NotNull(message = "El tipo de organizador es requerido")
    private TipoUsuario tipo;

    // Campos para PROFESOR
    private String facultad;
    private String departamento;
    private String especializacion;

    // Campos para LIDER_ESTUDIANTIL
    private String programa;
    private Integer semestre;
    private String representacion;

    // Campos para BIENESTAR
    private String areaAdministrativa;
    private String cargo;
}
