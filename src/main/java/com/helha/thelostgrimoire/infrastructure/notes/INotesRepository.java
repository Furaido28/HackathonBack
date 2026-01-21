package com.helha.thelostgrimoire.infrastructure.notes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface INotesRepository extends JpaRepository<DbNotes, Long> {
    List<DbNotes> findAllByDirectoryId(Long directoryId);
    List<DbNotes> findAllByUserId(Long userId);
}
