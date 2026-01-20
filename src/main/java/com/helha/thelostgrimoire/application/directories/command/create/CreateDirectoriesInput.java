
package com.helha.thelostgrimoire.application.directories.command.create;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.modelmapper.internal.bytebuddy.asm.Advice;

import java.time.LocalDateTime;

public class CreateDirectoriesInput {
    @JsonIgnore
    public Long userId;

    @NotBlank(message = "Name can't be empty")
    public String name;

    public Long parentDirectoryId;

    @JsonIgnore
    public LocalDateTime createdAt;
}
