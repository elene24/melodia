/*
 * UserRepository.java
 *
 * Version 1.0
 *
 * 11 November 2026
 *
 * Data access for users.
 */
package ge.melodia.repository;

import java.util.List;

import ge.melodia.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for {@link User}.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    /* Method-name query with "And": is the email used by someone else? */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByUsernameIgnoreCaseAndIdNot(String username, Long id);

    List<User> findAllByOrderByDisplayNameAsc();
}
