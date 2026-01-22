package com.helha.thelostgrimoire.application.repositories.directories.query.getAllByUserId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GetAllDirectoriesByUserIdInput {
    @JsonIgnore
    public Long userId;
    public String directoryId;
}
