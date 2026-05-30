package com.icesi.uniplan.dto.request;

import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
@Data
public class CrearEventoRequest {

    @NotBlank(message = "El título es requerido")
    @Size(min = 5, message = "El título debe tener al menos 5 caracteres")
    private String titulo;

    @NotBlank(message = "La descripción es requerida")
    @Size(min = 10, message = "La descripción debe tener al menos 10 caracteres")
    private String descripcion;

    @NotNull(message = "El tipo de evento es requerido")
    private TipoEvento tipo;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHoraInicio;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime fechaHoraFin;

    @NotBlank(message = "La ubicación es requerida")
    private String ubicacion;

    @NotNull(message = "El número máximo de asistentes es requerido")
    @Min(value = 1, message = "Debe haber al menos 1 cupo")
    private Integer maxAsistentes;

    @NotNull(message = "Los datos específicos son requeridos")
    @Valid
    private DatosEspecificosRequest datosEspecificos;
}
