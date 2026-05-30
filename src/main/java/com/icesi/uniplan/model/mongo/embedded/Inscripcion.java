package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inscripcion {

    @Field("_id")
    private ObjectId id;
    
    @Field("nombre")
    @NotBlank
    private String nombre;
    
    @Field("correo")
    @NotBlank
    @Email
    private String correo;
    
    @Field("codigo_estudiante")
    private String codigoEstudiante;
    
    @Field("fecha_inscripcion")
    private LocalDateTime fechaInscripcion;
    
    @Field("confirmada")
    private Boolean confirmada;
}
