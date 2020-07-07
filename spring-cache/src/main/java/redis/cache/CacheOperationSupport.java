package redis.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;
import org.springframework.util.MethodInvoker;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Registers cache cacheInvocations support of methods with @Cacheable annotations.
 *
 * @author renchao
 * @since v1.0
 */
@Component
public class CacheOperationSupport implements CacheOperation, CacheOperationRegistry {

    /**
     * Maintains Sets of CachedInvocation objects corresponding to each cache
     * configured in the application. At initialization, this map gets populated
     * with the cache name as the key and a hashSet as the value, for every
     * configured cache.
     */
    private Map<String, Set<CacheInvocation>> cacheToInvocationMap;

    /**
     * Avoid concurrent modification issues by using CopyOnWriteArraySet that
     * copies the internal array on every modification.
     */
    private final Set<CacheInvocation> cacheInvocations = new CopyOnWriteArraySet<>();

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private KeyGenerator keyGenerator;

    /**
     * Initializes the storage objects in a optimum way based upon the number of
     * configured caches. Helps avoid creating Set objects on the fly and
     * related concurrency issues. Populates the cacheToInvocationsMap with the
     * cache name as the key and a hashSet as the value, for every configured
     * cache. Depends on CacheManager to get the configured cache names.
     */
    @PostConstruct
    public void initialize() {
        cacheToInvocationMap = new ConcurrentHashMap<>(getCacheNames().size());
        for (final String cacheName : getCacheNames()) {
            // lazy init for CacheInvocation
            cacheToInvocationMap.put(cacheName, new CopyOnWriteArraySet<>());
        }
    }

    /**
     * Creates a MethodInvoker instance from the cached invocation object and
     * invokes it to get the return value.
     *
     * @return Return value resulted from the method invocation.
     */
    private Object invoke(CacheInvocation invocation)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final MethodInvoker invoker = new MethodInvoker();
        invoker.setTargetObject(invocation.getTargetBean());
        invoker.setArguments(invocation.getArguments());
        invoker.setTargetMethod(invocation.getTargetMethod().getName());
        invoker.prepare();
        return invoker.invoke();
    }

    /**
     * Uses the supplied cached invocation details to invoke the target method
     * with appropriate arguments and update the relevant caches. Updates all
     * caches if the cacheName argument is null.
     */
    private void refreshCache(CacheInvocation invocation, String... cacheNames) {
        boolean invocationSuccess;
        Object cachedValue = null;
        try {
            // db invoked..
            cachedValue = invoke(invocation);
            invocationSuccess = true;
        } catch (final IllegalAccessException | ClassNotFoundException | NoSuchMethodException
                | InvocationTargetException e) {
            invocationSuccess = false;
            // TODO Invocation failed, log the issue, cache can not be updated
        }

        if (invocationSuccess) {
            if (cacheNames == null) {
                cacheNames = cacheToInvocationMap.keySet().toArray(new String[cacheToInvocationMap.size()]);
            }
            for (final String cacheName : cacheNames) {
                if (cacheToInvocationMap.get(cacheName) != null) {
                    cacheManager.getCache(cacheName).put(invocation.getKey(), cachedValue);
                }
            }
        }
    }

    @Override
    public void registerInvocation(Object target, Method method, Object[] args, Set<String> cacheNames) {
        if (!cacheToInvocationMap.keySet().containsAll(cacheNames)) {
            this.initialize();
        }
        Object key = keyGenerator.generate(target, method, args);
        final CacheInvocation invocation = new CacheInvocation(key, target, method, args);
        cacheInvocations.add(invocation);
        for (final String cacheName : cacheNames) {
            cacheToInvocationMap.get(cacheName).add(invocation);
        }
    }

    @Override
    public Set<String> getCacheNames() {
        return (Set<String>) cacheManager.getCacheNames();
    }

    @Override
    public void refreshCaches(String... cacheNames) {
        if (cacheNames == null) {
            for (final CacheInvocation invocation : cacheInvocations) {
                refreshCache(invocation);
            }
            return;
        }

        for (String cacheName : cacheNames) {
            if (cacheToInvocationMap.get(cacheName) != null) {
                for (final CacheInvocation invocation : cacheToInvocationMap.get(cacheName)) {
                    refreshCache(invocation, cacheName);
                }
            }
        }
    }

    @Override
    public void refreshCaches(long fromCacheExpirationRefreshTime, String... cacheNames) {
        if (fromCacheExpirationRefreshTime != -1) {
            // record cache name form cache refresh expire time
            refreshCaches(cacheNames);
        }
    }

    @Override
    public void evictCache(String... cacheNames) {
        if (cacheNames == null) {
            for (final String cacheName : cacheToInvocationMap.keySet()) {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null)
                    cache.clear();
            }
            return;
        }

        for (final String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null)
                cache.clear();
        }
    }

    @Override
    public void setExpire(long expireTime, String cacheName) {
        if (cacheName == null || cacheManager.getCache(cacheName) == null) {
            // TODO Log record with chcheName none null
            return;
        }
        RedisCacheManager cacheManager = (RedisCacheManager) this.cacheManager;
        Map<String, Long> map = new HashMap<>();
        map.put(cacheName, expireTime);
        cacheManager.setExpires(map);
    }

    /**
     * Holds the method invocation information to use while refreshing the
     * cache.
     */
    protected static final class CacheInvocation {
        private Object key;
        private final Object targetBean;
        private final Method targetMethod;
        private Object[] arguments;

        protected CacheInvocation(Object key, Object targetBean, Method targetMethod, Object[] arguments) {
            this.key = key;
            this.targetBean = targetBean;
            this.targetMethod = targetMethod;
            if (arguments != null && arguments.length != 0) {
                this.arguments = Arrays.copyOf(arguments, arguments.length);
                // TODO check if deep cloning is needed and implement
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof CacheInvocation)) {
                return false;
            }
            final CacheInvocation other = (CacheInvocation) obj;
            return key.equals(other.getKey());
        }

        private Object[] getArguments() {
            return arguments;
        }

        private Object getTargetBean() {
            return targetBean;
        }

        private Method getTargetMethod() {
            return targetMethod;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        public Object getKey() {
            return key;
        }

        @Override
        public String toString() {
            return "CacheInvocation [Key=" + key + ", targetBean=" + targetBean + ", targetMethod=" + targetMethod
                    + ", arguments=" + (arguments != null ? arguments.length : "none") + " ]";
        }

    }

    public Map<String, Set<CacheInvocation>> getCacheToInvocationMap() {
        return cacheToInvocationMap;
    }

    public Set<CacheInvocation> getInvocations() {
        return cacheInvocations;
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

}
