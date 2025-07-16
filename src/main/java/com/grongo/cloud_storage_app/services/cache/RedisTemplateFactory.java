package com.grongo.cloud_storage_app.services.cache;


import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Component
public class RedisTemplateFactory <T> {

    private final ReactiveRedisConnectionFactory redisConnectionFactory;

    public RedisTemplateFactory(ReactiveRedisConnectionFactory redisConnectionFactory){
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public ReactiveRedisTemplate<String, T> createTemplate(Class<T> tClass){
        Jackson2JsonRedisSerializer<T> serializer = new Jackson2JsonRedisSerializer<>(tClass);
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder =
                RedisSerializationContext.newSerializationContext(stringRedisSerializer);

        RedisSerializationContext<String, T> context = builder.value(serializer).build();
        return new ReactiveRedisTemplate<String, T>(redisConnectionFactory, context);
    }

}
