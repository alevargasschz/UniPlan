package com.icesi.uniplan.service;

import com.icesi.uniplan.dto.request.CrearEventoRequest;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;

import java.util.Date;
import java.util.List;

public interface IEventoService {

    Evento crearEvento(CrearEventoRequest request, String correoOrganizador);

    Evento actualizarEvento(String eventoId, CrearEventoRequest request, String correoOrganizador);

    void eliminarEvento(String eventoId, String correoOrganizador);

    List<Evento> listarEventos(TipoEvento tipo, EstadoEvento estado, Date inicio, Date fin);

    Evento obtenerEvento(String id);

    void inscribirEstudiante(String eventoId, String correoEstudiante);

    void cancelarInscripcion(String eventoId, String correoEstudiante);

    void confirmarAsistencia(String eventoId, String inscripcionId, String correoOrganizador);

    List<Inscripcion> listarInscritos(String eventoId, String correoOrganizador);

    String exportarInscritosCSV(String eventoId, String correoOrganizador);

    void actualizarEstadosEventos();

    List<Evento> listarEventosPorOrganizador(String correoOrganizador);

    List<Evento> listarEventosPorEstudiante(String correoEstudiante);
}
