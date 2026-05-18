package com.icesi.uniplan.model.mongo.enums;

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

    @Override
    public String toString() {
        return dbValue;
    }
}