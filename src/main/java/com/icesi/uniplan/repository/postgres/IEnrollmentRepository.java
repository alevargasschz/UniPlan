package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Enrollment;
import com.icesi.uniplan.model.postgres.EnrollmentId;

@Repository
public interface IEnrollmentRepository extends JpaRepository<Enrollment, EnrollmentId> {

    /**
     * RF14 - Valida requisitos previos (cursos previos completados).
     */
    List<Enrollment> findByStudent_Id(String studentId);

    /**
     * RF14 - Busca estudiantes inscritos en un grupo/curso.
     */
    List<Enrollment> findByGroup_Nrc(Integer groupNrc);

    /**
     * RF13 - Valida que no exista inscripción duplicada en un curso.
     */
    Optional<Enrollment> findByStudent_IdAndGroup_Nrc(String studentId, Integer groupNrc);

    /**
     * RF14 - Cuenta inscripciones de un estudiante en un grupo.
     */
    long countByStudent_IdAndGroup_Nrc(String studentId, Integer groupNrc);

    /**
     * Busca inscripciones por estado.
     */
    List<Enrollment> findByStatus(String status);

    /**
     * RF14 - Busca cursos completados por un estudiante.
     */
    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId AND e.status = 'COMPLETED'")
    List<Enrollment> findCompletedEnrollmentsByStudent(@Param("studentId") String studentId);

}
