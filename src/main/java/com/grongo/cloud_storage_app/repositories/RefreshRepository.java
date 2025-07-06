package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.JwtRefresh;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<JwtRefresh, String> {
}
