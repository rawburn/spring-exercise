package redis.cache;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Records invocations of methods with @Cacheable annotations,
 * uses the invocations to refresh the cached values.
 *
 * @author renchao
 * @since v1.0
 */
public interface CacheOperationRegistry {

    /**
     * Records invocations of methods with @Cacheable annotations
     */
    void registerInvocation(Object target, Method method, Object[] args, Set<String> cacheNames);

}
