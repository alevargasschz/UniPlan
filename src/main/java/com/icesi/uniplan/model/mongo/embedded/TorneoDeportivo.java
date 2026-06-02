package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor

public class TorneoDeportivo extends DatosEspecificos {

    @Field("tipo_deporte")
    @NotBlank
    private String tipoDeporte;

    @Field("reglas")
    @NotEmpty
    private List<@NotBlank String> reglas;

    @Field("numero_equipos")
    @NotNull
    @Min(2)
    private Integer numeroEquipos;

    @Field("estructura_torneo")
    @NotBlank
    private String estructuraTorneo;
}
