package com.helha.thelostgrimoire.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Users {
    private long id;                        // User identification
    private String name;                    // Name of the user
    private String firstname;               // Firstname of the user
    private String email_address;           // Email address of the user
    private String hash_password;           // Hashed password
    private LocalDateTime created_at;       // User creation date
}
