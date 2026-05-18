package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import org.springframework.core.convert.converter.Converter;

public class TipoEventoReadConverter implements Converter<String, TipoEvento> {

    @Override
    public TipoEvento convert(String source) {
        if (source == null) return null;
        for (TipoEvento t : TipoEvento.values()) {
            if (t.getDbValue().equalsIgnoreCase(source)) return t;
        }
        return null;
    }
}
