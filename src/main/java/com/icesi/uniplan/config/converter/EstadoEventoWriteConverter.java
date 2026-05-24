package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class EstadoEventoWriteConverter implements Converter<EstadoEvento, String> {

    @Override
    public String convert(EstadoEvento source) {
        if (source == null) return null;
        return source.getDbValue();
    }
}
