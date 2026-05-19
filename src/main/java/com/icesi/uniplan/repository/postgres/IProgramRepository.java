package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Program;

@Repository
public interface IProgramRepository extends JpaRepository<Program, Integer> {

    /**
     * Busca un programa por su código.
     */
    Optional<Program> findByCode(Integer code);

    /**
     * RF06 - Busca programas académicos por área.
     */
    List<Program> findByArea_Code(Integer areaCode);

    /**
     * Busca programas por nombre (búsqueda flexible).
     */
    List<Program> findByNameContainingIgnoreCase(String name);

}
