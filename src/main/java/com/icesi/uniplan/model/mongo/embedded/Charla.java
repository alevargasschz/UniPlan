package com.icesi.uniplan.model.mongo.embedded;

import jakarta.validation.Valid;
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
public class Charla extends DatosEspecificos {

    @Field("conferencista")
    @NotNull
    @Valid
    private Conferencista conferencista;

    @Field("enlaces")
    @NotEmpty
    private List<@NotBlank String> enlaces;

    @Field("descripcion")
    @NotBlank
    private String descripcion;
}
