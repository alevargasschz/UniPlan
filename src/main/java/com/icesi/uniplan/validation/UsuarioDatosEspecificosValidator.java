package com.icesi.uniplan.validation;

import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.embedded.PersonalBienestar;
import com.icesi.uniplan.model.mongo.embedded.Estudiante;
import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.embedded.LiderEstudiantil;
import com.icesi.uniplan.model.mongo.embedded.Profesor;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UsuarioDatosEspecificosValidator implements ConstraintValidator<UsuarioDatosEspecificosValid, Usuario> {

    @Override
    public boolean isValid(Usuario usuario, ConstraintValidatorContext context) {
        if (usuario == null) {
            return true;
        }

        TipoUsuario tipo = usuario.getTipo();
        DatosEspecificos datos = usuario.getDatosEspecificos();

        if (tipo == null || datos == null) {
            return true;
        }

        boolean valid = switch (tipo) {
            case ESTUDIANTE -> datos instanceof Estudiante;
            case PROFESOR -> datos instanceof Profesor;
            case LIDER_ESTUDIANTIL -> datos instanceof LiderEstudiantil;
            case BIENESTAR -> datos instanceof PersonalBienestar;
        };

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "datos_especificos no coincide con el tipo de usuario"
            ).addPropertyNode("datosEspecificos").addConstraintViolation();
        }

        return valid;
    }
}
