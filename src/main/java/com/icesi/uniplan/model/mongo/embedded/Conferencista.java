package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conferencista {
    
    @Field("nombre")
    @NotBlank
    private String nombre;
    
    @Field("perfil")
    @NotBlank
    private String perfil;
    
    @Field("afiliacion")
    @NotBlank
    private String afiliacion;
}
