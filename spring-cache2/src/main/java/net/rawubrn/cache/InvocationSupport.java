package net.rawubrn.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.MethodInvoker;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Registers cache cacheInvocations support of methods with @Cacheable annotations.
 *
 * @author rawburn·rc
 */
@Component
public class InvocationSupport implements CacheOperation, InvocationRegistry {

    private Map<String, CacheItem> cacheItemMap = new ConcurrentHashMap<>(16);

    /**
     * Maintains Sets of CachedInvocation objects corresponding to each cache
     * configured in the application. At initialization, this map gets populated
     * with the cache name as the key and a hashSet as the value, for every
     * configured cache.
     */
    private Map<String, Set<CachedInvocation>> cacheToInvocationsMap;

    /**
     * Avoid concurrent modification issues by using CopyOnWriteArraySet that
     * copies the internal array on every modification.
     */
    private final Set<CachedInvocation> cachedInvocations = new CopyOnWriteArraySet<>();

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
        cacheToInvocationsMap = new ConcurrentHashMap<>(getCacheNames().size());
        for (final String cacheName : getCacheNames()) {
            // lazy init for CacheInvocation
            cacheToInvocationsMap.put(cacheName, new CopyOnWriteArraySet<>());
        }
    }

    /**
     * Creates a MethodInvoker instance from the cached invocation object and
     * invokes it to get the return value.
     *
     * @return Return value resulted from the method invocation.
     */
    private Object execute(CachedInvocation invocation)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final MethodInvoker invoker = new MethodInvoker();
        invoker.setTargetObject(invocation.getTargetBean());
        invoker.setArguments(invocation.getArguments());
        invoker.setTargetMethod(invocation.getTargetMethod().getName());
        invoker.prepare();
        return invoker.invoke();
    }

    @Override
    public void registerInvocation(Object targetBean, Method targetMethod, Object[] arguments,
                                   Map<String, CacheItem> cacheItemTable) {
        this.cacheItemMap.putAll(cacheItemTable);
        Object key = keyGenerator.generate(targetBean, targetMethod, arguments);
        final CachedInvocation invocation = new CachedInvocation(key, targetBean, targetMethod, arguments);
        cachedInvocations.add(invocation);

        for (final String cacheName : cacheItemTable.keySet()) {
            if (!cacheToInvocationsMap.containsKey(cacheName)) {
                cacheToInvocationsMap.put(cacheName, new CopyOnWriteArraySet<>());
            }
            cacheToInvocationsMap.get(cacheName).add(invocation);
        }
    }

    /**
     * Uses the supplied cached invocation details to invoke the target method
     * with appropriate arguments and update the relevant caches. Updates all
     * caches if the cacheName argument is null.
     */
    private void updateCache(CachedInvocation invocation, String... cacheNames) {
        String[] cacheNamesArray = cacheNames;
        boolean invocationSuccess;
        Object computed = null;
        try {
            computed = execute(invocation);
            invocationSuccess = true;
        } catch (Exception e) {
            invocationSuccess = false;
            //TODO Invocation failed, log the issue, cache can not be updated
        }

        if (invocationSuccess) {
            if (cacheNamesArray == null) {
                cacheNamesArray = cacheToInvocationsMap.keySet().toArray(new String[cacheToInvocationsMap.size()]);
            }
            for (final String cacheName : cacheNamesArray) {
                if (cacheToInvocationsMap.get(cacheName) != null) {
                    cacheManager.getCache(cacheName).put(invocation.getKey(), computed);
                }
            }
        }
    }

    @Override
    public void refreshAllCaches() {
        for (final CachedInvocation invocation : cachedInvocations) {
            updateCache(invocation, (String) null);
        }
    }

    @Override
    public void refreshCache(String cacheName) {
        if (cacheToInvocationsMap.get(cacheName) != null) {
            for (final CachedInvocation invocation : cacheToInvocationsMap.get(cacheName)) {
                updateCache(invocation, cacheName);
            }
        }
        // Otherwise Wrong cache name, missing spring configuration for the
        // cache name used in annotations
    }

    @Override
    public void refreshCacheWithKey(String cacheName, String cacheKey) {
        if (cacheToInvocationsMap.get(cacheName) != null) {
            for (final CachedInvocation invocation : cacheToInvocationsMap.get(cacheName)) {
                if (StringUtils.hasText(cacheKey) && invocation.getKey().toString().equals(cacheKey)) {
                    updateCache(invocation, cacheName);
                }
            }
        }
    }

    @Override
    public void refreshCaches(String... cacheNames) {
        for (final String cacheName : cacheNames) {
            refreshCache(cacheName);
        }
    }

    public void evictCache(String... cacheNames) {
        if (cacheNames != null) {
            for (final String cacheName : cacheNames) {
                evictCache(cacheName);
            }
        }
    }

    public void evictCache(String cacheName) {
        if (cacheName != null) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected Map<String, Set<CachedInvocation>> getCacheGrid() {
        return cacheToInvocationsMap;
    }

    public void setCacheGrid(Map<String, Set<CachedInvocation>> cacheGrid) {
        this.cacheToInvocationsMap = cacheGrid;
    }

    protected Set<CachedInvocation> getInvocations() {
        return cachedInvocations;
    }

    /**
     * Holds the method invocation information to use while refreshing the
     * cache.
     */
    protected static final class CachedInvocation {
        private Object key;
        private final Object targetBean;
        private final Method targetMethod;
        private Object[] arguments;

        protected CachedInvocation(Object key, Object targetBean, Method targetMethod, Object[] arguments) {
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
            if (!(obj instanceof CachedInvocation)) {
                return false;
            }
            final CachedInvocation other = (CachedInvocation) obj;
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

    public Map<String, CacheItem> getCacheItemMap() {
        return cacheItemMap;
    }

    public void setCacheItemMap(Map<String, CacheItem> cacheItemMap) {
        this.cacheItemMap = cacheItemMap;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheManager.getCacheNames();
    }
}
