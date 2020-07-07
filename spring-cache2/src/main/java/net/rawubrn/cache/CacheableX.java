package net.rawubrn.cache;

import java.lang.annotation.*;

/**
 * @author rawburn·rc
 * @see org.springframework.cache.annotation.Cacheable
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface CacheableX {

    /**
     * Cache time to live, default never expire(in seconds)
     * @see org.springframework.data.redis.cache.RedisCacheConfiguration#entryTtl
     */
    long expireTime() default 0;

    /**
     * Sets the default refresh interval to arrived expire time
     */
    long refreshIntervalTime() default -1;
}
