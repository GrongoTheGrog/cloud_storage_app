package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.token.JwtRefresh;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<JwtRefresh, String> {
    @Modifying
    @Query("DELETE FROM JwtRefresh jt WHERE jt.user.id = :userId")
    public void deleteByUserId(
            @Param("userId") Long userId
    );
}
