package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Employee;
import com.icesi.uniplan.model.postgres.EmployeeType;

@Repository
public interface IEmployeeRepository extends JpaRepository<Employee, String> {

    /**
     * RF04, RF05, RF06 - Valida existencia de empleado.
     */
    Optional<Employee> findByEmail(String email);

    /**
     * RF05, RF06 - Busca empleados por facultad.
     */
    List<Employee> findByFaculty_Code(Integer facultyCode);

    /**
     * RF05, RF06 - Busca empleados por tipo (profesor, personal bienestar, etc).
     */
    List<Employee> findByEmployeeType(EmployeeType employeeType);

    /**
     * Busca empleados por campus.
     */
    List<Employee> findByCampus_Code(String campusCode);

    /**
     * Valida si un empleado existe con email específico.
     */
    boolean existsByEmail(String email);

}
