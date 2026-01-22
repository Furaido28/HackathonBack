package com.helha.thelostgrimoire.infrastructure.directories;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IDirectoriesRepository extends JpaRepository<DbDirectories, Long> {
    Optional<DbDirectories> findById(Long directoriesId);
    List<DbDirectories> findAllByUserId(Long userId);
    Optional<DbDirectories> findByUserIdAndIsRootTrue(Long userId);
    List<DbDirectories> findByUserIdAndParentDirectoryId(Long userId, Long parentId);
    boolean existsByNameAndParentDirectoryIdAndUserId(String name , Long userId, Long parentId);
    boolean existsByNameAndParentDirectoryIdAndUserIdAndIdNot(String name, Long parentId, Long userId, Long id);
}