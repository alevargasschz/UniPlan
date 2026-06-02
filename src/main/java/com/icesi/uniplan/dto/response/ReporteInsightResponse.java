package com.icesi.uniplan.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteInsightResponse {
    private String etiqueta;
    private Double valor;
}
