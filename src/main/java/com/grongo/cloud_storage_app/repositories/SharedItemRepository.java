package com.grongo.cloud_storage_app.repositories;

import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharedItemRepository extends JpaRepository<SharedItem, Long> {

    @Query("SELECT si FROM SharedItem si JOIN user u JOIN owner o WHERE o.id = :userId OR u.id = :userId")
    List<SharedItem> getAllSharingItems(
            @Param("userId") Long userId
    );

    @Query("SELECT si FROM SharedItem si JOIN item i WHERE i.id = :itemId")
    List<SharedItem> getByItemId(
            @Param("itemId") Long itemId
    );

    @Query("SELECT si FROM SharedItem si WHERE si.item.id = :itemId AND si.user.id = :userId")
    Optional<SharedItem> findByItemAndUser(
            @Param("itemId") Long itemId,
            @Param("userId") Long userId
    );


    @Query(nativeQuery = true, value = "SELECT * FROM t_shared_items WHERE owner_id = ? AND user_id = ?")
    public List<SharedItem> findByOwnerAndUser(Long ownerId, Long userId);

    @Modifying
    @Query("DELETE FROM SharedItem si WHERE si.owner.id = :userId OR si.user.id = :userId")
    public void deleteByUserId(
            @Param("userId") Long userId
    );

    @Query("SELECT si FROM SharedItem si " +
            "JOIN si.user u " +
            "JOIN FETCH si.item i " +
            "LEFT JOIN FETCH i.folder f " +
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
            "u.id = :userId")
    public List<SharedItem> querySharedItems(
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
