package com.icesi.uniplan.repository.postgres;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.Campus;

@Repository
public interface ICampusRepository extends JpaRepository<Campus, Integer> {

}
