
package com.helha.thelostgrimoire.infrastructure.directories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IDirectoriesRepository extends JpaRepository<DbDirectories, Long> {
}
