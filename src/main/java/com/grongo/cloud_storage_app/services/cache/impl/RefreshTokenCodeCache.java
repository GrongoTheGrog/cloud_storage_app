package com.grongo.cloud_storage_app.services.cache.impl;

import com.grongo.cloud_storage_app.services.cache.CacheService;
import com.grongo.cloud_storage_app.services.cache.RedisTemplateFactory;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenCodeCache extends CacheService<String> {
    public RefreshTokenCodeCache(RedisTemplateFactory<String> templateFactory){
        this.redisTemplate = templateFactory.createReactiveTemplate(String.class);
    }
}
