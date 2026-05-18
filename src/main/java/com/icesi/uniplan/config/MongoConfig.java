package com.icesi.uniplan.config;

import com.icesi.uniplan.config.converter.EstadoEventoReadConverter;
import com.icesi.uniplan.config.converter.EstadoEventoWriteConverter;
import com.icesi.uniplan.config.converter.TipoEventoReadConverter;
import com.icesi.uniplan.config.converter.TipoEventoWriteConverter;
import com.icesi.uniplan.config.converter.TipoUsuarioReadConverter;
import com.icesi.uniplan.config.converter.TipoUsuarioWriteConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<?> converters = Arrays.asList(
            new TipoEventoReadConverter(),
            new TipoEventoWriteConverter(),
            new EstadoEventoReadConverter(),
            new EstadoEventoWriteConverter(),
            new TipoUsuarioReadConverter(),
            new TipoUsuarioWriteConverter()
        );
        return new MongoCustomConversions(converters);
    }
}
