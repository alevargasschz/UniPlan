package com.icesi.uniplan.repository.mongo;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.mongo.Evento;
import com.icesi.uniplan.model.mongo.enums.EstadoEvento;
import com.icesi.uniplan.model.mongo.enums.TipoEvento;

@Repository
public interface IEventoRepository extends MongoRepository<Evento, String> {

    /**
     * RF09 - Filtra eventos por estado (Próximos, En curso, Finalizados).
     */
    List<Evento> findByEstado(EstadoEvento estado);

    /**
     * RF09 - Filtra eventos por tipo.
     */
    List<Evento> findByTipo(TipoEvento tipo);

    /**
     * RF09 - Filtra eventos por rango de fechas.
     */
    List<Evento> findByFechaHoraInicioBetween(Date inicio, Date fin);

    /**
     * RF21, RF30 - Obtiene eventos creados por un organizador específico.
     */
    List<Evento> findByOrganizadorUsername(String username);

    /**
     * RF30 - Valida que el evento pertenezca al organizador.
     */
    Optional<Evento> findByIdAndOrganizadorUsername(String id, String username);

    /**
     * RF09 - Combinación de filtros: estado y rango de fechas.
     */
    List<Evento> findByEstadoAndFechaHoraInicioBetween(EstadoEvento estado, Date inicio, Date fin);

    /**
     * RF09 - Combinación de filtros: tipo y estado.
     */
    List<Evento> findByTipoAndEstado(TipoEvento tipo, EstadoEvento estado);

    /**
     * RF09 - Búsqueda flexible por título.
     */
    List<Evento> findByTituloContainingIgnoreCase(String titulo);

    /**
     * RF09 - Búsqueda por ubicación.
     */
    List<Evento> findByUbicacionContainingIgnoreCase(String ubicacion);

    /**
     * RF30 - Cuenta eventos de un organizador.
     */
    long countByOrganizadorUsername(String username);

    /**
     * RF12 - Valida disponibilidad de cupos.
     */
    @Query("{ 'id': ?0, 'cuposDisponibles': { $gt: 0 } }")
    Optional<Evento> findByIdAndCuposAvailable(String id);

}
