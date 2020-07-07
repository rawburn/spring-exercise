package net.rawubrn.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.rawubrn.cache.RedisCacheSupport.RefreshCacheExecutor.*;
import static org.springframework.data.redis.cache.RedisCacheWriter.nonLockingRedisCacheWriter;

/**
 * @author rawburn·rc
 */
@Slf4j
@Configuration
public class RedisCacheSupport {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return conf(redisConnectionFactory);
    }

    public RedisTemplate conf(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public KeyGenerator keyGenerator() {
        return new SimpleKeyGenerator();
    }

    @Bean
    public RedisCacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheWriter redisCacheWriter = nonLockingRedisCacheWriter(connectionFactory);
        return new RedisCacheManager(redisCacheWriter, determineConfiguration()) {

            @Autowired
            InvocationSupport cacheOperation;

            @Override
            protected RedisCache getMissingCache(String name) {
                RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfig();
                CacheItem cacheItem = cacheOperation.getCacheItemMap().get(name);
                if (cacheItem != null) {
                    redisCacheConfiguration = redisCacheConfiguration.entryTtl(Duration.ofSeconds(cacheItem.getExpireTime()));
                }
                return createRedisCache(name, redisCacheConfiguration);
            }

            public RedisCache createRedisCache(String name, RedisCacheConfiguration cacheConfig) {
                return new RedisCacheCustomizer(name, redisCacheWriter, cacheConfig, cacheOperation);
            }
        };
    }

    private static final Lock CACHE_REFRESH_LOCK = new ReentrantLock();

    class RedisCacheCustomizer extends RedisCache {

        private CacheItem cacheItem;

        private InvocationSupport cacheOperation;

        protected RedisCacheCustomizer(String name, RedisCacheWriter cacheWriter, RedisCacheConfiguration cacheConfig, InvocationSupport invocationSupport) {
            super(name, cacheWriter, cacheConfig);
            this.cacheOperation = invocationSupport;
            this.cacheItem = invocationSupport.getCacheItemMap().get(name);
        }

        @Override
        public ValueWrapper get(final Object key) {
            ValueWrapper valueWrapper = super.get(key);
            if (null != valueWrapper) {
                if (cacheItem != null) {
                    String cacheKey = this.createCacheKey(key);
                    long refreshIntervalTime = cacheItem.getRefreshIntervalTime();
                    Long ttl = redisTemplate.getExpire(cacheKey);
                    if (null != ttl && ttl <= refreshIntervalTime) {
                        log.info("key: {} ttl: {} refreshIntervalTime: {}", cacheKey, ttl, refreshIntervalTime);

                        // TODO(rawburn) 保证单次仅一个线程刷新缓存
                        if (existRunningRefreshCacheTask(cacheKey)) {
                            log.info("Skip refresh time");
                        } else {
                            run(() -> {
                                try {
                                    CACHE_REFRESH_LOCK.lock();
                                    if (existRunningRefreshCacheTask(cacheKey)) {
                                        log.info("Skip refresh time");
                                    } else {
                                        putRefreshCacheTask(cacheKey);
                                        String cacheName = RedisCacheCustomizer.super.getName();
                                        log.info("Refresh with cache key: {}", cacheKey);
                                        cacheOperation.refreshCacheWithKey(cacheName, key.toString());
                                    }
                                } finally {
                                    removeRefreshCacheTask(cacheKey);
                                    CACHE_REFRESH_LOCK.unlock();
                                }
                            });
                        }
                    }
                }
            }
            return valueWrapper;
        }
    }

    public RedisCacheConfiguration defaultCacheConfig() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
    }

    private RedisCacheConfiguration determineConfiguration() {
        RedisCacheConfiguration redisCacheConfiguration = defaultCacheConfig();
        // TODO(rawburn) other config for default
        return redisCacheConfiguration;
    }

    static class RefreshCacheExecutor {

        private static ExecutorService executorService = Executors.newFixedThreadPool(20);

        private static Map<String, String> RUNNING_REFRESH_CACHE = new ConcurrentHashMap(20);

        public static void putRefreshCacheTask(String cacheKey) {
            if (!existRunningRefreshCacheTask(cacheKey)) {
                RUNNING_REFRESH_CACHE.put(cacheKey, cacheKey);
            }
        }

        public static void removeRefreshCacheTask(String cacheKey) {
            if (existRunningRefreshCacheTask(cacheKey)) {
                RUNNING_REFRESH_CACHE.remove(cacheKey);
            }
        }

        public static boolean existRunningRefreshCacheTask(String cacheKey) {
            return RUNNING_REFRESH_CACHE.containsKey(cacheKey);
        }

        public static void run(Runnable runnable) {
            executorService.execute(runnable);
        }
    }
}