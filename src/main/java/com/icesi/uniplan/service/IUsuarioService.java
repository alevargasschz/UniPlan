package com.icesi.uniplan.service;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;

public interface IUsuarioService {

    Usuario registrarEstudiante(RegistroEstudianteRequest request);

    Usuario registrarOrganizador(RegistroOrganizadorRequest request);

    Usuario obtenerPorCorreo(String correo);
}
