package com.icesi.uniplan.service;

import com.icesi.uniplan.dto.request.RegistroEstudianteRequest;
import com.icesi.uniplan.dto.request.RegistroOrganizadorRequest;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;
import java.util.List;

public interface IUsuarioService {

    Usuario registrarEstudiante(RegistroEstudianteRequest request);

    Usuario registrarOrganizador(RegistroOrganizadorRequest request);

    Usuario actualizarOrganizador(String organizadorId, RegistroOrganizadorRequest request);

    void eliminarOrganizador(String organizadorId);

    Usuario obtenerOrganizadorPorId(String organizadorId);

    List<Usuario> listarOrganizadores(TipoUsuario tipo);

    Usuario obtenerPorCorreo(String correo);
}
