package com.icesi.uniplan.model.mongo;

import com.icesi.uniplan.model.mongo.embedded.DatosEspecificos;
import com.icesi.uniplan.model.mongo.embedded.Inscripcion;
import com.icesi.uniplan.model.mongo.embedded.Organizador;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;
import com.icesi.uniplan.validation.EventoDatosEspecificosValid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "eventos")
@EventoDatosEspecificosValid
public class Evento {

    @Id
    private String id;

    @Field("titulo")
    @NotBlank
    @Size(min = 5)
    private String titulo;

    @Field("descripcion")
    @NotBlank
    @Size(min = 10)
    private String descripcion;

    @Field("tipo")
    @NotNull
    private TipoEvento tipo;

    @Field("fecha_hora_inicio")
    @NotNull
    private LocalDateTime fechaHoraInicio;

    @Field("fecha_hora_fin")
    @NotNull
    private LocalDateTime fechaHoraFin;

    @Field("ubicacion")
    @NotBlank
    private String ubicacion;

    @Field("max_asistentes")
    @NotNull
    @Min(1)
    private Integer maxAsistentes;

    @Field("total_inscritos")
    @NotNull
    @Min(0)
    private Integer totalInscritos;

    @Field("cupos_disponibles")
    @NotNull
    @Min(0)
    private Integer cuposDisponibles;

    @Field("estado")
    @NotNull
    private EstadoEvento estado;

    @Field("inscripciones")
    @Builder.Default
    @Valid
    private List<Inscripcion> inscripciones = new ArrayList<>();

    @Field("organizador")
    @NotNull
    @Valid
    private Organizador organizador;

    @Field("datos_especificos")
    private DatosEspecificos datosEspecificos;
}
