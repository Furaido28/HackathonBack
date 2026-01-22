package com.helha.thelostgrimoire.application.repositories.directories.command.update;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class UpdateDirectoriesInput {
    public long id;
    public String name;

    @JsonIgnore
    public Long userId;
}