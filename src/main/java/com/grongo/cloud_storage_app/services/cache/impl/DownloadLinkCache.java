package com.grongo.cloud_storage_app.services.cache.impl;

import com.grongo.cloud_storage_app.services.cache.CacheService;
import com.grongo.cloud_storage_app.services.cache.RedisTemplateFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


@Service
public class DownloadLinkCache extends CacheService<String> {

    public DownloadLinkCache(RedisTemplateFactory<String> templateFactory){
        this.redisTemplate = templateFactory.createReactiveTemplate(String.class);
    }

}
