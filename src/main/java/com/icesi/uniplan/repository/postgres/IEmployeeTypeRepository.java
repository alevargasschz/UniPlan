package com.icesi.uniplan.repository.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.EmployeeType;

@Repository
public interface IEmployeeTypeRepository extends JpaRepository<EmployeeType, String> {

}
