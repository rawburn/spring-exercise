package redis.cache;

import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.Set;

/**
 * Provides methods to refresh or evict cached objects.
 *
 * @author renchao
 * @since v1.0
 * @see CacheManager
 */
public interface CacheOperation {

    /**
     * Return a collection of the caches known by this cache manager.
     *
     * @return names of caches known by the cache manager.
     */
    Set<String> getCacheNames();

    /**
     * Refreshes caches corresponding to the supplied cache names array,
     * if none that cache names then refresh all cached objects.
     *
     * @param cacheNames names of caches to refresh
     */
    void refreshCaches(String... cacheNames);

    /**
     *
     * @param cacheNames
     * @param fromCacheExpirationRefreshTime
     */
    void refreshCaches(long fromCacheExpirationRefreshTime, String... cacheNames);

    /**
     * Clears all values from the named caches, if none that supplied
     * cache names then evict all cached objects.
     *
     * @param cacheNames names of caches to evict
     */
    void evictCache(String... cacheNames);

    /**
     * Indicate expire time to the supplied cache name.
     *
     * @param cacheName name of cache to set a specific expire time
     * @param expireTime expire time
     * @see RedisCacheManager#defaultExpiration
     */
    void setExpire(long expireTime, String cacheName);

}
