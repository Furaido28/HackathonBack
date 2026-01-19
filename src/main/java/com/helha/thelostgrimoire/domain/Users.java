package com.helha.thelostgrimoire.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Users {
    private int user_id;
    private String name;
    private String first_name;
    private String email_address;
    private String hash_password;
    private LocalDateTime created_at;
}
