package com.grongo.cloud_storage_app.repositories;

import com.grongo.cloud_storage_app.models.tag.Tag;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    @Query("SELECT t FROM Tag t JOIN user u WHERE t.name = :tagName AND u.id = :userId")
    public List<Tag> findByNameAndUserId(
            @Param("tagName") String tagName,
            @Param("userId") Long userId
    );
}
