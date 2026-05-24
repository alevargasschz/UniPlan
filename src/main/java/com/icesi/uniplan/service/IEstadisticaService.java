package com.icesi.uniplan.service;

import com.icesi.uniplan.dto.response.EstadisticaResponse;
import com.icesi.uniplan.model.mongo.Evento;

import java.util.List;

public interface IEstadisticaService {

    void crearEstadistica(Evento evento);

    void registrarInscripcion(String eventoId);

    void registrarCancelacion(String eventoId);

    List<EstadisticaResponse> obtenerEstadisticas();

    List<EstadisticaResponse> obtenerTopEventosPorOcupacion(int limit);
}
