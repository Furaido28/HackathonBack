
package com.helha.thelostgrimoire.infrastructure.directories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IDirectoriesRepository extends JpaRepository<DbDirectories, Long> {
    List<DbDirectories> findAllByUserId(Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);

}
