package net.rawubrn.cache;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Records invocations of methods with @Cacheable annotations,
 * uses the invocations to refresh the cached values.
 *
 * @author rawburn·rc
 * @copyright @2016 http://yantrashala.github.io
 */
public interface InvocationRegistry {

    /**
     * Records invocations of methods with @Cacheable annotations
     */
    void registerInvocation(Object target, Method method, Object[] args, Map<String, CacheItem> cacheItemTable);

}
