package com.icesi.uniplan.model.mongo.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class LiderEstudiantil extends DatosEspecificos {

    @Field("programa")
    @NotBlank
    private String programa;

    @Field("semestre")
    @NotNull
    @Min(1)
    @Max(15)
    private Integer semestre;

    @Field("representacion")
    @NotBlank
    private String representacion;
}
