package net.rawubrn.cache;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.AbstractFallbackCacheOperationSource;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect to intercept of methods annotated with @Cacheable.
 *
 * @author rawburn·rc
 * @see Cacheable
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class CacheOperationAspect {

    @Autowired
    private InvocationRegistry invocation;

    @Pointcut("@annotation(net.rawubrn.cache.CacheableX)")
    public void pointcut() {
    }

    /**
     * Intercepts invocations of methods annotated with @Cacheable and
     * invokes cacheRefreshSupport with the execution information.
     *
     * <p>Configure this aspect to intercept the classes where refreshing caches are needed.
     */
    @Around("pointcut()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getSpecificMethod(joinPoint);
        Cacheable cacheable = AnnotationUtils.findAnnotation(method, Cacheable.class);
        CacheableX cacheableX = AnnotationUtils.findAnnotation(method, CacheableX.class);
        Assert.notNull(cacheable, "'CacheableX' extend property must built on 'Cacheable'");

        String[] cacheNames = cacheable.value().length > cacheable.cacheNames().length ? cacheable.value() : cacheable.cacheNames();
        Map<String, CacheItem> cacheItemMap = new HashMap<>();
        for (String name : cacheNames) {
            CacheItem cacheItem = new CacheItem();
            cacheItem.setName(name);
            cacheItem.setExpireTime(cacheableX.expireTime());
            cacheItem.setRefreshIntervalTime(cacheableX.refreshIntervalTime());
            cacheItemMap.putIfAbsent(name, cacheItem);
        }
        invocation.registerInvocation(joinPoint.getTarget(), method, joinPoint.getArgs(), cacheItemMap);
        return joinPoint.proceed();
    }

    /**
     * Finds out the most specific method when the execution reference is an
     * interface or a method with generic parameters
     *
     * @see AbstractFallbackCacheOperationSource#computeCacheOperations
     */
    private Method getSpecificMethod(ProceedingJoinPoint pjp) {
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        Method method = methodSignature.getMethod();

        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(pjp.getTarget());
        if (targetClass == null && pjp.getTarget() != null) {
            targetClass = pjp.getTarget().getClass();
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);

        // If we are dealing with method with generic parameters, find the original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);
        return specificMethod;
    }

}
