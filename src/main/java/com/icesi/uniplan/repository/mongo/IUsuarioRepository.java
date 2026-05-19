package com.icesi.uniplan.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.mongo.Usuario;

@Repository
public interface IUsuarioRepository extends MongoRepository<Usuario, String> {

}
