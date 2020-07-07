package redis;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.lang.annotation.*;

/**
 * Annotation indicating that the result of invoking a method (or all methods
 * in a class) can be cached through assign expire or refresh time cumtomize on {@link Cacheable Cacheable}.
 *
 * @author renchao
 * @see Cacheable
 * @see CacheConfig
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheableX {

    /**
     * Cache time to live, default never expire(in seconds).
     * @see RedisCacheManager#defaultExpiration
     */
    long expireTime() default 0;
    //
    // /**
    //  * Sets the default refresh time interval to expire, defalut -1 -> (none refresh)
    //  * @see RedisCacheManager#defaultExpiration
    //  */
    // long fromCacheExpirationRefreshTime() default -1;

}
