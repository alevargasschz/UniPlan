package com.icesi.uniplan.repository.postgres;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Department;

@Repository
public interface IDepartmentRepository extends JpaRepository<Department, Integer> {

    /**
     * RF06 - Busca departamento por código.
     */
    Optional<Department> findByCode(Integer code);

}
