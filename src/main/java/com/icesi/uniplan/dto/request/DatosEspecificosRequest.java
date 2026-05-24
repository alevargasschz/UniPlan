package com.icesi.uniplan.dto.request;

import lombok.Data;
import java.util.List;

/**
 * DTO plano que agrupa los campos de todos los tipos de datos específicos.
 * El servicio construye el subtipo correcto (Taller/Charla/Torneo/Voluntariado/OtroEvento)
 * según el TipoEvento del evento.
 */
@Data
public class DatosEspecificosRequest {

    // --- Taller ---
    private List<String> materialesRequeridos;
    private List<String> condicionesPrevias;

    // --- Charla ---
    private ConferencistaRequest conferencista;
    private List<String> enlaces;
    private String descripcionExtendida;

    // --- Torneo Deportivo ---
    private String tipoDeporte;
    private List<String> reglas;
    private Integer numeroEquipos;
    private String estructuraTorneo;

    // --- Actividad de Voluntariado ---
    private String causa;
    private Integer numeroHorasRequeridas;
    private List<String> actividades;
    private List<String> puntosEncuentro;
    private List<String> responsables;

    // --- Otro ---
    private String descripcionAdicional;
}
