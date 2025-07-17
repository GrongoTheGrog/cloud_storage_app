package com.grongo.cloud_storage_app.repositories;

import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SharedItemRepository extends JpaRepository<SharedItem, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM t_shared_items WHERE item_id = ? AND user_id = ?")
    Optional<SharedItem> findByItemAndUser(Long itemId, Long userId);

}
