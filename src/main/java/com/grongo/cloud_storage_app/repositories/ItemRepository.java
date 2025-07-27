package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM t_items WHERE folder_id IS NULL AND owner_id = ?")
    public List<Item> findAllRootItems(Long userId);


    @Query("SELECT i FROM Item i " +
            "LEFT JOIN i.folder f " +
            "JOIN i.owner o " +
            "LEFT JOIN FETCH i.tagJoins tj " +
            "LEFT JOIN FETCH tj.tag t WHERE " +
            "(:maxDate IS NULL OR i.created_at < :maxDate) AND " +
            "(:minDate IS NULL OR i.created_at > :minDate) AND " +
            "(:maxBytes IS NULL OR i.size < :maxBytes) AND " +
            "(:minBytes IS NULL OR i.size > :minBytes) AND " +
            "(:name IS NULL OR i.name LIKE CONCAT('%', :name, '%')) AND " +
            "(:fileTypes IS NULL OR (i.type = 'FILE' AND i.fileType IN :fileTypes)) AND " +
            "(:folderId = -1 OR f.id = :folderId) AND " +
            "(:tagIds IS NULL OR t.id IN :tagIds) AND " +
            "(:type IS NULL OR i.type = :type) AND " +
            "o.id = :userId")
    public List<Item> queryItem(
            @Param("maxDate") LocalDateTime maxDate,
            @Param("minDate") LocalDateTime minDate,
            @Param("maxBytes") Long maxBytes,
            @Param("minBytes") Long minBytes,
            @Param("name") String name,
            @Param("fileTypes") List<String> fileTypes,
            @Param("folderId") Long folderId,
            @Param("tagIds") List<Long> tagIds,
            @Param("userId") Long userId,
            @Param("type") String type
            );
}
