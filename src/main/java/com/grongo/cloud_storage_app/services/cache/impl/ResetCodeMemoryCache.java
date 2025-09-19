package com.grongo.cloud_storage_app.services.cache.impl;

import com.grongo.cloud_storage_app.models.resetCode.ResetCodeMemory;
import com.grongo.cloud_storage_app.services.cache.CacheService;
import com.grongo.cloud_storage_app.services.cache.RedisTemplateFactory;
import org.springframework.stereotype.Service;

@Service
public class ResetCodeMemoryCache extends CacheService<ResetCodeMemory> {

    ResetCodeMemoryCache(RedisTemplateFactory<ResetCodeMemory> factory){
        this.redisTemplate = factory.createReactiveTemplate(ResetCodeMemory.class);
    }

}
