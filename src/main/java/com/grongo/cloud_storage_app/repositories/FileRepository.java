package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.items.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    public List<File> findByName(String name);
}
