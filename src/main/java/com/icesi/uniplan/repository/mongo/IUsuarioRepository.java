package com.icesi.uniplan.repository.mongo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.mongo.Usuario;
import com.icesi.uniplan.model.mongo.enums.TipoUsuario;

@Repository
public interface IUsuarioRepository extends MongoRepository<Usuario, String> {

    /**
     * RF02, RF03 - Busca usuario por email para autenticación.
     */
    Optional<Usuario> findByCorreo(String correo);

    /**
     * RF03 - Búsqueda flexible de usuarios por nombre.
     */
    Optional<Usuario> findByNombreIgnoreCase(String nombre);

    /**
     * RF05, RF06 - Busca usuarios por tipo (estudiante, profesor, líder, etc).
     */
    List<Usuario> findByTipo(TipoUsuario tipo);

    /**
     * Búsqueda flexible de usuarios por email (patrón).
     */
    List<Usuario> findByCorreoContainingIgnoreCase(String correoPattern);

    /**
     * Valida si existe un usuario con ese email.
     */
    boolean existsByCorreo(String correo);

    /**
     * Valida si existe un usuario con ese nombre.
     */
    boolean existsByNombreIgnoreCase(String nombre);

}
