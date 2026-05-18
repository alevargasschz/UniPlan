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
@TypeAlias("datos_bienestar")
public class PersonalBienestar extends DatosEspecificos {

    @Field("area_administrativa")
    @NotBlank
    private String areaAdministrativa;

    @Field("cargo")
    @NotBlank
    private String cargo;
}
