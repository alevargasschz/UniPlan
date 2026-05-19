package com.icesi.uniplan.repository.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Faculty;

@Repository
public interface IFacultyRepository extends JpaRepository<Faculty, Integer> {

}
