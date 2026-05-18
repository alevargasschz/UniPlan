package com.icesi.uniplan.model.mongo.enums;

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

    @Override
    public String toString() {
        return dbValue;
    }
}
