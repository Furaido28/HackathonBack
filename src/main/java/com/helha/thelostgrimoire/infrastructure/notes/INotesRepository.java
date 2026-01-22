package com.helha.thelostgrimoire.infrastructure.notes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface INotesRepository extends JpaRepository<DbNotes, Long> {
    List<DbNotes> findAllByDirectoryId(Long directoryId);
    List<DbNotes> findAllByUserIdOrderByNameAsc(Long userId);
    List<DbNotes> findAllByDirectoryIdOrderByNameAsc(Long directoryId);
    List<DbNotes> findByUserIdAndDirectoryId(Long userId, Long parentId);
}
