package com.icesi.uniplan.model.mongo.embedded;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("datos_estudiante")
public class Estudiante extends DatosEspecificos {

    private String codigo;
}
