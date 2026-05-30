package com.icesi.uniplan.model.mongo.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Profesor extends DatosEspecificos {

    @Field("facultad")
    @NotBlank
    private String facultad;

    @Field("departamento")
    @NotBlank
    private String departamento;

    @Field("especializacion")
    @NotBlank
    private String especializacion;
}
