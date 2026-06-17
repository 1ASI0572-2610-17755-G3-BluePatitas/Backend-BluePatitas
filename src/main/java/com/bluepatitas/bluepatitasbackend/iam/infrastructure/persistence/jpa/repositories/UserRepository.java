package com.bluepatitas.bluepatitasbackend.iam.infrastructure.persistence.jpa.repositories;

import com.bluepatitas.bluepatitasbackend.iam.domain.model.aggregates.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * This interface is responsible for providing the User entity related operations.
 * It extends the JpaRepository interface.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * This method is responsible for finding the user by email.
     *
     * @param email The email.
     * @return The user object.
     */
    Optional<User> findByEmail(String email);

    /**
     * This method is responsible for checking if the user exists by email.
     *
     * @param email The email.
     * @return True if the user exists, false otherwise.
     */
    boolean existsByEmail(String email);
}
