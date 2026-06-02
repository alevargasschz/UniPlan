package com.icesi.uniplan.config;

import com.icesi.uniplan.config.converter.*;
import com.icesi.uniplan.model.mongo.embedded.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MongoConfig {

    // Mapa de alias → clase concreta
    private static final Map<String, Class<? extends DatosEspecificos>> TYPE_MAP = new HashMap<>();
    static {
        TYPE_MAP.put("datos_estudiante", Estudiante.class);
        TYPE_MAP.put("datos_bienestar", PersonalBienestar.class);
        TYPE_MAP.put("datos_profesor", Profesor.class);
        TYPE_MAP.put("datos_lider_estudiantil", LiderEstudiantil.class);
        TYPE_MAP.put("detalles_charla", Charla.class);
        TYPE_MAP.put("detalles_volunatariado", ActividadVoluntariado.class);
        TYPE_MAP.put("detalles_otro", OtroEvento.class);
        TYPE_MAP.put("detalles_taller", Taller.class);
        TYPE_MAP.put("detalles_torneo", TorneoDeportivo.class);
    }

    @Bean
    public MongoCustomConversions customConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new TipoEventoReadConverter(),
                new TipoEventoWriteConverter(),
                new EstadoEventoReadConverter(),
                new EstadoEventoWriteConverter(),
                new TipoUsuarioReadConverter(),
                new TipoUsuarioWriteConverter()));
    }

}