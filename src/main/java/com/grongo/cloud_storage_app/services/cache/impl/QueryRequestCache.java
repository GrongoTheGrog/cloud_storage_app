package com.grongo.cloud_storage_app.services.cache.impl;

import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import com.grongo.cloud_storage_app.services.cache.CacheKeys;
import com.grongo.cloud_storage_app.services.cache.CacheService;
import com.grongo.cloud_storage_app.services.cache.RedisTemplateFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryRequestCache extends CacheService<ItemDto> {

    public QueryRequestCache(RedisTemplateFactory<ItemDto> templateFactory){
        this.redisTemplate = templateFactory.createReactiveTemplate(ItemDto.class);
    }

}
