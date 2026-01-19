package com.helha.thelostgrimoire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Users {
    private long id;
    private String name;
    private String firstname;
    private String email_address;
    private String hash_password;
    private LocalDateTime created_at;
}
