package io.stock.evaluation.reactive_data.config;

import io.stock.evaluation.reactive_data.ticker.meta.dto.TickerMetaItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Profile("test-docker")
@Configuration
public class TestRedisConfig {
    @Bean(name = "reactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory(){
        // 추후 Embedded Redis 로 전환 예정.
        return new LettuceConnectionFactory("localhost", 16379);
    }

    @Bean
    public ReactiveRedisOperations<String, String> tickerMapReactiveRedisOperation(
        ReactiveRedisConnectionFactory redisConnectionFactory
    ){
        StringRedisSerializer serializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext.<String, String>newSerializationContext()
                    .key(serializer)
                    .value(serializer)
                    .hashKey(serializer)
                    .hashValue(serializer)
                    .build();

        ReactiveRedisTemplate<String, String> redisTemplate =
                new ReactiveRedisTemplate<>(redisConnectionFactory, serializationContext);

        return redisTemplate;
    }

    @Bean
    public ReactiveRedisOperations<String, TickerMetaItem> tickerMetaMapReactiveRedisOperation(
        ReactiveRedisConnectionFactory redisConnectionFactory
    ){
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<TickerMetaItem> valueSerializer = new Jackson2JsonRedisSerializer<TickerMetaItem>(TickerMetaItem.class);

        RedisSerializationContext<String, TickerMetaItem> serializationContext =
                RedisSerializationContext.<String, TickerMetaItem>newSerializationContext()
                        .key(keySerializer)
                        .value(valueSerializer)
                        .hashKey(keySerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<String, TickerMetaItem>(redisConnectionFactory, serializationContext);
    }
}