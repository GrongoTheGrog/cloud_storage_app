package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.items.Folder;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    @Query("SELECT f FROM Folder f LEFT JOIN FETCH f.storedFiles WHERE f.id = :id")
    Optional<Folder> findByIdWithStoredFiles(@Param("id") Long id);
}
