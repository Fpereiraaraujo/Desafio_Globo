package com.fernando.sistema_assinaturas.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig {

	@Bean
	public CacheManager caffeineCacheManager() {
		var manager = new CaffeineCacheManager("plans");
		manager.setCacheSpecification("maximumSize=10,expireAfterWrite=1h");
		return manager;
	}

	@Bean
	@Primary
	public CacheManager redisCacheManager(
		RedisConnectionFactory connectionFactory,
		ObjectMapper objectMapper
	) {
		var valueSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
		var configuration = RedisCacheConfiguration.defaultCacheConfig()
			.entryTtl(Duration.ofMinutes(10))
			.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(valueSerializer));
		return RedisCacheManager.builder(connectionFactory)
			.cacheDefaults(configuration)
			.build();
	}
}
