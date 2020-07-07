package redis.config;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnection;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import redis.CustomizerRedisSerializer;
import redis.cache.CacheOperation;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Redis Customizer Configuraion.
 *
 * @author renchao
 * @since v1.0
 */
@Configuration
@ConditionalOnClass({JedisConnection.class, RedisOperations.class, Jedis.class})
public class RedisConfigurationCustomizer {

    @Autowired
    private CacheOperation cacheOperation;

    // default expire time (in seconds).
    private static final long DEFAULT_EXPIRATION = 60L;

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(
            RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer redisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        template.setDefaultSerializer(redisSerializer);
        template.setValueSerializer(new CustomizerRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            List<String> collect = Arrays.stream(params)
                    .map(param -> param.getClass().getSimpleName())
                    .collect(Collectors.toList());
            return method.getName() + collect;
        };
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisTemplate<Object, Object> redisTemplate) {
        RedisCacheManager redisCacheManager = new RedisCacheManager(redisTemplate) {

            // hold all names and expire time of caches
            private final Set<Map<String, Long>> expiresSet = new CopyOnWriteArraySet<>();

            // pre cache evict refresh cache time(in second)
            private final long FROM_CACHE_EXPIRATION_REFRESH_TIME = 3L;

            @Override
            public Cache getCache(String name) {
                Cache cache = super.getCache(name);

                if (cache == null) {
                    return cache;
                }

                return new RedisCacheCustomizer(name,
                        this.isUsePrefix() ? this.getCachePrefix().prefix(name) : null,
                        this.getRedisOperations(),
                        computeExpiration(name),
                        FROM_CACHE_EXPIRATION_REFRESH_TIME);
            }

            @Override
            public void setExpires(Map<String, Long> expires) {
                expiresSet.add(expires);
            }

            @Override
            protected long computeExpiration(String name) {
                Set<Map<String, Long>> collect = expiresSet.stream()
                        .filter(expire -> expire.get(name) != null).collect(Collectors.toSet());
                return (collect.size() > 0 ? IterableUtils.get(collect, 0).get(name) : DEFAULT_EXPIRATION);
            }

            class RedisCacheCustomizer extends RedisCache {

                private long fromCacheExpirationRefreshTime = -1;

                private byte[] keyPrefix;

                private long expiration;

                public RedisCacheCustomizer(String name, byte[] prefix,
                                            RedisOperations<?, ?> redisOperations, long expiration) {
                    super(name, prefix, redisOperations, expiration);
                }

                public RedisCacheCustomizer(String name, byte[] prefix,
                                            RedisOperations redisOperations, long expiration, long preTime) {
                    this(name, prefix, redisOperations, expiration);
                    this.fromCacheExpirationRefreshTime = preTime;
                    this.keyPrefix = prefix;
                    this.expiration = expiration;
                }

                @Override
                public ValueWrapper get(Object key) {
                    ValueWrapper valueWrapper = super.get(key);
                    if (null != valueWrapper) {
                        Long expire = getRedisOperations().getExpire(key);
                        if (null != expire && expire <= fromCacheExpirationRefreshTime) {
                            // TODO 性能问题（线程、锁）
                            cacheOperation.refreshCaches(this.getName());
                        }
                    }
                    return valueWrapper;
                }

                @Override
                public void put(Object key, Object value) {
                    put(new RedisCacheElement(getRedisCacheKey(key), toStoreValue(value))
                            .expireAfter(expiration));
                }

                private RedisCacheKey getRedisCacheKey(Object key) {
                    return new RedisCacheKey(key).usePrefix(keyPrefix)
                            .withKeySerializer(getRedisOperations().getKeySerializer());
                }

            }

        };

        redisCacheManager.setDefaultExpiration(DEFAULT_EXPIRATION);

        return redisCacheManager;

    }

}
