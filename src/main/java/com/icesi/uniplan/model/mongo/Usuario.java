package com.icesi.uniplan.model.mongo;

import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "usuarios")
public class Usuario {

    @Id
    private String id;

    private String nombre;

    private String correo;

    private String contrasena;

    private TipoUsuario tipo;

    @Field("datos_especificos")
    private DatosEspecificos datosEspecificos;
}
