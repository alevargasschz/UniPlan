package com.icesi.uniplan.model.mongo.embedded;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.annotation.TypeAlias;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_class")

public abstract class DatosEspecificos {
    public DatosEspecificos() {}
}