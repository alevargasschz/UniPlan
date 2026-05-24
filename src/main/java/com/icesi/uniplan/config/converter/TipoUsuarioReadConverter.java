package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class TipoUsuarioReadConverter implements Converter<String, TipoUsuario> {

    @Override
    public TipoUsuario convert(String source) {
        if (source == null) return null;
        for (TipoUsuario t : TipoUsuario.values()) {
            if (t.getDbValue().equalsIgnoreCase(source)) return t;
        }
        return null;
    }
}
