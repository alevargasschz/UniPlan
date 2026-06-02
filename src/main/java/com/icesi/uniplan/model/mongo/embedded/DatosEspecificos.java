package com.icesi.uniplan.model.mongo.embedded;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "_class")

public abstract class DatosEspecificos {
    public DatosEspecificos() {}
}