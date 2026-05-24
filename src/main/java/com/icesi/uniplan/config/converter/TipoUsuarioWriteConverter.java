package com.icesi.uniplan.config.converter;

import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class TipoUsuarioWriteConverter implements Converter<TipoUsuario, String> {

    @Override
    public String convert(TipoUsuario source) {
        if (source == null) return null;
        return source.getDbValue();
    }
}
