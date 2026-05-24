package com.icesi.uniplan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegistroEstudianteRequest {

    @NotBlank(message = "El código estudiantil es requerido")
    private String codigoEstudiantil;

    @NotBlank(message = "El correo es requerido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@u\\.icesi\\.edu\\.co$",
             message = "Debe ser un correo institucional @u.icesi.edu.co")
    private String correo;

    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String contrasena;
}
