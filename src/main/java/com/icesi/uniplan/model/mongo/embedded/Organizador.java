package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Organizador {

    @Field("_id")
    private ObjectId id;
    
    @Field("nombre")
    @NotBlank
    private String nombre;
    
    @Field("correo")
    @NotBlank
    @Email
    private String correo;
    
    @Field("tipo")
    @NotNull
    private TipoUsuario tipo;
}
