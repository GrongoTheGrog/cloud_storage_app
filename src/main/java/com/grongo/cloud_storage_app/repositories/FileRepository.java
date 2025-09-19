package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.items.File;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    public List<File> findByName(String name);

    @Query("SELECT f FROM File f WHERE f.owner.id = :userId")
    public List<File> findByUserId(
            @Param("userId") Long userId
    );
}
