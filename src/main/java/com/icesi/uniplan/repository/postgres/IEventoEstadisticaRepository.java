package com.icesi.uniplan.repository.postgres;

import com.icesi.uniplan.model.postgres.EventoEstadistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IEventoEstadisticaRepository extends JpaRepository<EventoEstadistica, String> {

    List<EventoEstadistica> findByTipo(String tipo);

    @Query("SELECT e FROM EventoEstadistica e ORDER BY e.porcentajeOcupacion DESC")
    List<EventoEstadistica> findAllOrderByPorcentajeOcupacionDesc();

    @Query("SELECT e FROM EventoEstadistica e ORDER BY e.totalInscritos DESC")
    List<EventoEstadistica> findAllOrderByTotalInscritosDesc();
}
