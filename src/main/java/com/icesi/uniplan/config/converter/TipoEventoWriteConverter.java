package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import org.springframework.core.convert.converter.Converter;

public class TipoEventoWriteConverter implements Converter<TipoEvento, String> {

    @Override
    public String convert(TipoEvento source) {
        if (source == null) return null;
        return source.getDbValue();
    }
}
