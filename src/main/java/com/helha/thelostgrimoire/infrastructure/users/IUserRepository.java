package com.helha.thelostgrimoire.infrastructure.users;

import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository {
    Optional<DbUsers> findByEmailAddress(String email_address);
}
