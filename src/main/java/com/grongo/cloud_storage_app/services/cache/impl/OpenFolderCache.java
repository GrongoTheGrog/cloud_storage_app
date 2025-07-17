package com.grongo.cloud_storage_app.services.cache.impl;

import com.grongo.cloud_storage_app.models.items.Item;
import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.services.cache.CacheService;
import com.grongo.cloud_storage_app.services.cache.RedisTemplateFactory;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;

@Service
public class OpenFolderCache extends CacheService<Item> {

    public OpenFolderCache(RedisTemplateFactory<Item> redisTemplateFactory){
        this.redisTemplate = redisTemplateFactory.createReactiveTemplate(Item.class);
    }

}
