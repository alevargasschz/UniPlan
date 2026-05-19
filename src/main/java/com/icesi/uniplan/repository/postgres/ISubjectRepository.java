package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Subject;

@Repository
public interface ISubjectRepository extends JpaRepository<Subject, String> {

    /**
     * Busca un curso por su código.
     */
    Optional<Subject> findByCode(String code);

    /**
     * RF14, RF22 - Busca cursos de un programa académico (para validar requisitos).
     */
    List<Subject> findByProgram_Code(Integer programCode);

    /**
     * Búsqueda flexible de cursos por nombre.
     */
    List<Subject> findByNameContainingIgnoreCase(String name);

}
