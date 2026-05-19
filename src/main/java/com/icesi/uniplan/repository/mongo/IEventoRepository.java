package com.icesi.uniplan.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.mongo.Evento;

@Repository
public interface IEventoRepository extends MongoRepository<Evento, String> {

}
