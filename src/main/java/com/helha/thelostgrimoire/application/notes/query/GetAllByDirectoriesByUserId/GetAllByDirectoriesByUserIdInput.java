package com.helha.thelostgrimoire.application.notes.query.GetAllByDirectoriesByUserId;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class GetAllByDirectoriesByUserIdInput {
    public Long directoryId;

    @JsonIgnore
    public Long userId;
}