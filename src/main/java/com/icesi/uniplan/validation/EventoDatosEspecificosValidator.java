package com.icesi.uniplan.validation;

import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.embedded.Charla;
import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.embedded.Taller;
import com.icesi.uniplan.model.mongo.embedded.TorneoDeportivo;
import com.icesi.uniplan.model.mongo.embedded.ActividadVoluntariado;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventoDatosEspecificosValidator implements ConstraintValidator<EventoDatosEspecificosValid, Evento> {

    @Override
    public boolean isValid(Evento evento, ConstraintValidatorContext context) {
        if (evento == null) {
            return true;
        }

        TipoEvento tipo = evento.getTipo();
        DatosEspecificos datos_especificos = evento.getDatosEspecificos();

        if (tipo == null || datos_especificos == null) {
            return true;
        }

        boolean valid = switch (tipo) {
            case taller -> datos_especificos instanceof Taller;
            case charla -> datos_especificos instanceof Charla;
            case torneo -> datos_especificos instanceof TorneoDeportivo;
            case voluntariado -> datos_especificos instanceof ActividadVoluntariado;
            case otro -> true;
        };

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "datos_especificos no coincide con el tipo de evento").addPropertyNode("datosEspecificos")
                    .addConstraintViolation();
        }

        return valid;
    }
}
