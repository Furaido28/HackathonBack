package com.helha.thelostgrimoire.infrastructure.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<DbUsers, Long> {
    Optional<DbUsers> findByEmailAddress(String emailAddress);
}
