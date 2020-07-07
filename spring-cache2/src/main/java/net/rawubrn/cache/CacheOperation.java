package net.rawubrn.cache;

import java.util.Collection;

/**
 * Provides methods to refresh or evict cached objects.
 *
 * @author rawburn·rc
 */
public interface CacheOperation {

    /**
     * Returns all the configured cache names.
     */
    Collection<String> getCacheNames();

    /**
     * Refreshes caches corresponding to the supplied cache names array.
     */
    void refreshCaches(String... cacheNames);

    /**
     * Refreshes caches corresponding to the supplied cache name.
     */
    void refreshCache(String cacheName);

    /**
     * Refreshes caches corresponding to the supplied cache name and cache key.
     */
    void refreshCacheWithKey(String cacheName, String cacheKey);

    /**
     * Refreshes all caches configured in the application
     */
    void refreshAllCaches();

    /**
     * Clears all values from the named caches
     */
    void evictCache(String... cacheNames);

    /**
     * Clears all values from the named cache
     */
    void evictCache(String cacheName);

}
