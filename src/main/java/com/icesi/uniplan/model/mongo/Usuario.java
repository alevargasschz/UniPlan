package com.icesi.uniplan.model.mongo;

import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import com.icesi.uniplan.validation.UsuarioDatosEspecificosValid;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "usuarios")
@UsuarioDatosEspecificosValid
public class Usuario {
    
    @Id
    private String id;
    
    @Field("nombre")
    @NotBlank
    @Size(min = 3)
    private String nombre;

    @Field("correo")
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@u\\.icesi\\.edu\\.co$")
    private String correo;

    @Field("contrasena")
    @NotBlank
    @Size(min = 8)
    private String contrasena;

    @Field("tipo")
    @NotNull
    private TipoUsuario tipo;

    @Field("datos_especificos")
    @Valid
    @NotNull
    private DatosEspecificos datosEspecificos;
}
