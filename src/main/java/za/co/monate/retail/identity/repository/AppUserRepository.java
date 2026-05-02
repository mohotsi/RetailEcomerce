package za.co.monate.retail.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.monate.retail.identity.model.AppUser;

import java.util.Optional;

/**
 * ============================================================================
 * INTERFACE: AppUserRepository
 * PURPOSE: Handles all database operations (CRUD) for the AppUser table.
 * ============================================================================
 */
public interface AppUserRepository extends JpaRepository<AppUser, Long> {

    // Spring JPA Magic: By simply naming the method "findByEmail", 
    // Spring automatically generates the SQL: SELECT * FROM app_users WHERE email = ?
    Optional<AppUser> findByEmail(String email);

}