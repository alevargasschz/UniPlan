package com.icesi.uniplan.model.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "evento_estadistica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoEstadistica {

    @Id
    @Column(name = "evento_id", length = 30)
    private String eventoId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "max_asistentes", nullable = false)
    private Integer maxAsistentes;

    @Column(name = "total_inscritos", nullable = false)
    @Builder.Default
    private Integer totalInscritos = 0;

    @Column(name = "total_cancelaciones", nullable = false)
    @Builder.Default
    private Integer totalCancelaciones = 0;

    @Column(name = "porcentaje_ocupacion")
    @Builder.Default
    private Double porcentajeOcupacion = 0.0;

    @Column(name = "fecha_ultima_actualizacion")
    private LocalDateTime fechaUltimaActualizacion;
}
