package com.icesi.uniplan.model.mongo.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class OtroEvento extends DatosEspecificos {

    @Field("descripcion_adicional")
    private String descripcionAdicional;
}
