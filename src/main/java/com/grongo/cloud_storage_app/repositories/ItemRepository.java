package com.grongo.cloud_storage_app.repositories;


import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.sharedItems.SharedItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query(nativeQuery = true, value = "SELECT * FROM t_items WHERE folder_id IS NULL AND owner_id = ?")
    public List<Item> findAllRootItems(Long userId);
}
