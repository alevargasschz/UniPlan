package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ActividadVoluntariado extends DatosEspecificos {

    @Field("causa")
    @NotBlank
    private String causa;

    @Field("numero_horas_requeridas")
    @NotNull
    @Min(1)
    private Integer numeroHorasRequeridas;

    @Field("actividades")
    @NotEmpty
    private List<@NotBlank String> actividades;

    @Field("puntos_encuentro")
    @NotEmpty
    private List<@NotBlank String> puntosEncuentro;

    @Field("responsables")
    @NotEmpty
    private List<@NotBlank String> responsables;
}
