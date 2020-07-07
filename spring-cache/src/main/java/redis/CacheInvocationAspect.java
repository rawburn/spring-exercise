package redis;

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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import redis.cache.CacheOperation;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect to intercept invocation of methods annotated with @Cacheable.
 *
 * @author renchao
 * @since v1.0
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CacheInvocationAspect {

    @Autowired
    private CacheOperation cacheOperation;

    @Pointcut("@annotation(redis.CacheableX)")
    public void pointcut() {}

    /**
     * Intercepts invocations of methods annotated with @CacheableX and
     * invokes cacheRefreshSupport to refresh or set a new expire time
     * with the execution information.
     */
    @Around("pointcut()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Method annotatedElement = getSpecificmethod(joinPoint);
        Cacheable cacheable = annotatedElement.getAnnotation(Cacheable.class);
        CacheableX cacheableX = annotatedElement.getAnnotation(CacheableX.class);
        Arrays.stream(cacheable.value())
                .forEach(cacheName -> cacheOperation.setExpire(cacheableX.expireTime(), cacheName));
        return joinPoint.proceed();
    }

    /**
     * Finds out the most specific method when the execution reference is an
     * interface or a method with generic parameters
     *
     * @see AbstractFallbackCacheOperationSource#computeCacheOperations
     */
    private Method getSpecificmethod(ProceedingJoinPoint pjp) {
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
