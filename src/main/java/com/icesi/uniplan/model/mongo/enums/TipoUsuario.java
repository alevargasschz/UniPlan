package com.icesi.uniplan.model.mongo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoUsuario {
    ESTUDIANTE("estudiante"),
    PROFESOR("profesor"),
    LIDER_ESTUDIANTIL("lider_estudiantil"),
    BIENESTAR("bienestar");

    private final String dbValue;

    TipoUsuario(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @JsonValue
    public String toJsonValue() {
        return name();
    }

    @JsonCreator
    public static TipoUsuario fromValue(String value) {
        for (TipoUsuario tipo : TipoUsuario.values()) {
            if (tipo.dbValue.equalsIgnoreCase(value) || tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Unknown TipoUsuario: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}