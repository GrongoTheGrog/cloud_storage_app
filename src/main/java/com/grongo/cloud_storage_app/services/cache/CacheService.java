package com.grongo.cloud_storage_app.services.cache;

import com.grongo.cloud_storage_app.models.items.dto.ItemDto;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;


@Log4j2
public abstract class CacheService <T> {

    protected ReactiveRedisTemplate<String, T> redisTemplate;

    public T getKey(String key) {
        try{
            T value = redisTemplate.opsForValue().get(key).block();
            if (value == null){
                log.info("Cache miss for key {}.", key);
            }else{
                log.info("Cache hit for key {}.", key);
            }

            return value;
        }catch (Exception e){
            return null;
        }

    }

    public void setKey(String key, T value, Duration expiration){
        redisTemplate.opsForValue().set(key, value, expiration)
                .subscribe(isStored -> {
                   log.info("Cached key {} of TTL of {} seconds.", key, expiration.getSeconds());
                });
    }

    public void delKey(String key){
        redisTemplate.delete(key).subscribe(number -> {
            log.info("Deleted cache for key {}.", key);
        });
    }

    public boolean hasKey(String key){
        boolean hasKey = Boolean.TRUE.equals(redisTemplate.hasKey(key).block());
        if (hasKey) {
            log.info("Cache exists for key {}.", key);
        }else{
            log.info("Cache does not exist for key {}.", key);
        }

        return hasKey;
    }

    public void refreshKey(String key, Duration expiration){
        redisTemplate.expire(key, expiration).subscribe(value -> {
            log.info("Changed the expiration for key {} to {} seconds.", key, expiration);
        });
    }

    public List<T> getKeyList(String key){
        List<T> list = redisTemplate
                .opsForList()
                .range(key, 0, -1)
                .collectList()
                .block();

        log.info("Cache found {} items for key {}.", list == null ? 0 : list.size(), key);
        return list;
    }

    public void setKeyList(String key, List<T> list, Duration duration){
        redisTemplate.opsForList()
                .leftPushAll(key, list)
                .subscribe(v -> {
                    redisTemplate.expire(key, duration);
                    log.info("Set list {} as key {} with TTL of {} seconds.", list.toString(), key, duration.getSeconds());
                });

    }

}
