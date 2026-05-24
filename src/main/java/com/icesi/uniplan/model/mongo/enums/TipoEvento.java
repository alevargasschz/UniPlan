package com.icesi.uniplan.model.mongo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEvento {
    TALLER("taller"),
    CHARLA("charla"),
    TORNEO("torneo"),
    VOLUNTARIADO("voluntariado"),
    OTRO("otro");

    private final String dbValue;

    TipoEvento(String dbValue) {
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
    public static TipoEvento fromValue(String value) {
        for (TipoEvento tipo : TipoEvento.values()) {
            if (tipo.dbValue.equalsIgnoreCase(value) || tipo.name().equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Unknown TipoEvento: " + value);
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
