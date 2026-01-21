
package com.helha.thelostgrimoire.infrastructure.directories;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "directories")
public class DbDirectories {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "user_id", nullable = false)
    public Long userId;

    @Column(name = "parent_directory_id")
    public Long parentDirectoryId;

    @NotBlank(message = "Name can't be empty")
    @Column(name = "name", nullable = false)
    public String name;

    @Column(name = "is_root", nullable = false)
    public boolean isRoot = false;

    @Column(name = "created_at", nullable = false)
    public LocalDateTime createdAt;
}
