package com.helha.thelostgrimoire.infrastructure.users;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

@Entity
@Table(name = "users")
public class DbUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "Name can't be empty")
    @Column(name = "name")
    public String name;

    @NotBlank(message = "Firstname can't be empty")
    @Column(name = "firstname")
    public String firstname;

    @NotBlank(message = "Email address can't be empty")
    @Column(name = "email_address")
    public String emailAddress;

    @NotBlank(message = "Password can't be empty")
    @Column(name = "hash_password")
    public String hashPassword;

    @Column(name = "created_at")
    public LocalDate createdAt;
}
