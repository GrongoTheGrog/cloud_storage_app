package com.grongo.cloud_storage_app.repositories;

import com.grongo.cloud_storage_app.models.tag.TagJoin;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoinTagRepository extends JpaRepository<TagJoin, Long> {
    @Query("SELECT tj FROM TagJoin tj JOIN tag t JOIN item i WHERE i.id = :itemId AND t.id = :tagId")
    public Optional<TagJoin> findByItemIdAndTagId(
            @Param("itemId") Long itemId,
            @Param("tagId") Long tagId
    );

}
