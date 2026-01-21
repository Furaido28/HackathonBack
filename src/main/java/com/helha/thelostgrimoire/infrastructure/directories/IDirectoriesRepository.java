package com.helha.thelostgrimoire.infrastructure.directories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDirectoriesRepository extends JpaRepository<DbDirectories, Long> {

    List<DbDirectories> findAllByUserId(Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    void deleteByParentDirectoryId(Long parentDirectoryId);
    Optional<DbDirectories> findByUserIdAndIsRootTrue(Long userId);
    List<DbDirectories> findByUserIdAndParentDirectoryId(Long userId, Long parentId);
}