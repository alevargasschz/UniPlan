package com.icesi.uniplan.repository.postgres;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.icesi.uniplan.model.postgres.User;

@Repository
public interface IUserRepository extends JpaRepository<User, String> {

    /**
     * RF02 - Encuentra un usuario por su username.
     */
    Optional<User> findByUsername(String username);

    /**
     * RF03 - Busca usuario por username o email (sin modificar BD institucional).
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.student.email = :email OR u.employee.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    /**
     * RNF15 - Encuentra todos los usuarios con un rol específico.
     */
    List<User> findByRole(String role);

    /**
     * Valida si existe al menos un registro con el rol indicado (ignorando mayúsculas/minúsculas).
     */
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE UPPER(u.role) = UPPER(:role)")
    boolean existsRoleIgnoreCase(@Param("role") String role);

    /**
     * Encuentra un usuario por su estudiante asociado.
     */
    Optional<User> findByStudent_Id(String studentId);

    /**
     * Encuentra un usuario por su empleado asociado.
     */
    Optional<User> findByEmployee_Id(String employeeId);

    /**
     * Encuentra usuarios activos o inactivos.
     */
    List<User> findByIsActive(Boolean isActive);

}
