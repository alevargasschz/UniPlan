package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Student;

@Repository
public interface IStudentRepository extends JpaRepository<Student, String> {

    /**
     * RF01 - Valida que el estudiante exista en la BD institucional.
     */
    Optional<Student> findByEmail(String email);

    /**
     * RF14, RF15, RF16 - Valida existencia de estudiante y campus.
     */
    Optional<Student> findByIdAndCampus_Code(String studentId, String campusCode);

    /**
     * Busca estudiantes por campus.
     */
    List<Student> findByCampus_Code(String campusCode);

    /**
     * Búsqueda flexible de estudiantes por nombre.
     */
    List<Student> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);

    /**
     * Valida si un estudiante existe con email específico.
     */
    boolean existsByEmail(String email);

}
