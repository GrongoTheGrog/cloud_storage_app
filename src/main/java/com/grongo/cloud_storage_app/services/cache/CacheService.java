package com.grongo.cloud_storage_app.services.cache;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;


@Log4j2
public abstract class CacheService <T> {

    protected ReactiveRedisTemplate<String, T> redisTemplate;
    protected String prefix;

    public T getKey(String key) {
        String finalKey = prefix + key;
        try{
            T value = redisTemplate.opsForValue().get(finalKey).block();
            if (value == null){
                log.info("Cache miss for key {}.", finalKey);
            }else{
                log.info("Cache hit for key {}.", finalKey);
            }

            return value;
        }catch (Exception e){
            return null;
        }

    }

    public void setKey(String key, T value, Duration expiration){
        String finalKey = prefix + key;
        redisTemplate.opsForValue().set(finalKey, value, expiration)
                .subscribe(isStored -> {
                   log.info("Cached key {} of TTL of {} seconds.", finalKey, expiration.getSeconds());
                });
    }

    public void delKey(String key){
        String finalKey = prefix + key;
        redisTemplate.delete(prefix + key).subscribe(number -> {
            log.info("Deleted cache for key {}.", finalKey);
        });
    }

    public boolean hasKey(String key){
        String finalKey = prefix + key;
        boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(finalKey).block());
        if (hasKey) {
            log.info("Cache exists for key {}.", finalKey);
        }else{
            log.info("Cache does not exist for key {}.", finalKey);
        }

        return hasKey;
    }

    public void refreshKey(String key, Duration expiration){
        String finalKey = prefix + key;
        redisTemplate.expire(key, expiration).subscribe(value -> {
            log.info("Changed the expiration for key {} to {} seconds.", finalKey, expiration);
        });
    }

}
