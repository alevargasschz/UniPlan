package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class EstadoEventoReadConverter implements Converter<String, EstadoEvento> {

    @Override
    public EstadoEvento convert(String source) {
        if (source == null)
            return null;
        for (EstadoEvento e : EstadoEvento.values()) {
            if (e.getDbValue().equalsIgnoreCase(source))
                return e;
        }
        return null;
    }
}
