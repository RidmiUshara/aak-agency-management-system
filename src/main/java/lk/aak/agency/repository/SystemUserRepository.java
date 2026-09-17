package lk.aak.agency.repository;

import lk.aak.agency.model.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SystemUserRepository
        extends JpaRepository<SystemUser, Long> {

    Optional<SystemUser> findByUsername(
            String username
    );

    boolean existsByUsername(
            String username
    );
}