package com.icesi.uniplan.model.mongo.enums;

public enum EstadoEvento {
    PROGRAMADO("programado"),
    ACTIVO("activo"),
    FINALIZADO("finalizado"),
    CANCELADO("cancelado");

    private final String dbValue;

    EstadoEvento(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    @Override
    public String toString() {
        return dbValue;
    }
}
