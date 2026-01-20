package com.helha.thelostgrimoire.infrastructure.notes;

import org.springframework.data.jpa.repository.JpaRepository;

public interface INotesRepository extends JpaRepository<DbNotes, Long> {
}
