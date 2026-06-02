package com.icesi.uniplan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadisticasDashboardResponse {

    private List<String> eventoIdsFiltrados;
    private List<ReporteInsightResponse> topSemestres;
    private List<ReporteInsightResponse> topProgramas;
    private List<ReporteInsightResponse> demandaPorTipo;
    private List<ReporteInsightResponse> mayorCancelacion;
    private List<ReporteInsightResponse> asistenciaPorTipo;
}
