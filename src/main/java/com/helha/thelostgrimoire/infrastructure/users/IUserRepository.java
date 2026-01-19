package com.helha.thelostgrimoire.infrastructure.users;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface IUserRepository extends JpaRepository<DbUsers, Long> {
    Optional<DbUsers> findByEmailAddress(String emailAddress);
}
