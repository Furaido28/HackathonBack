package com.helha.thelostgrimoire.infrastructure.users;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class DbUsers {
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private long id;
     @NotBlank(message = "Name can't be empty")
     public String name;
     @NotBlank(message = "Firstname can't be empty")
     public String firstname;
     @NotBlank(message = "Email address can't be empty")
     public String email_address;
     @NotBlank(message = "Password can't be empty")
     public String hash_password;
     public LocalDate created_at;
}
