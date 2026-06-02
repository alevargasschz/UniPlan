package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class Taller extends DatosEspecificos {

    @Field("materiales_requeridos")
    @NotEmpty
    private List<@NotBlank String> materialesRequeridos;

    @Field("condiciones_previas")
    @NotEmpty
    private List<@NotBlank String> condicionesPrevias;
}
