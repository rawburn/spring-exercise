package redis.cache;

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
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Aspect to intercept of methods annotated with @Cacheable.
 *
 * @author renchao
 * @since v1.0
 * @see Cacheable
 */
@Aspect
@Component
public class CacheOperationAspect {

    @Autowired
    private CacheOperationRegistry invocation;

    @Pointcut("@annotation(org.springframework.cache.annotation.Cacheable)")
    public void pointcut() {}

    /**
     * Intercepts invocations of methods annotated with @Cacheable and
     * invokes cacheRefreshSupport with the execution information.
     *
     * <p>Configure this aspect to intercept the classes where refreshing caches are needed.
     */
    @Around("pointcut()")
    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
        Method annotatedElement = getSpecificmethod(joinPoint);
        List<Cacheable> annotations = getMethodAnnotations(annotatedElement, Cacheable.class);

        Set<String> cacheNames = new HashSet<>();
        for (Cacheable cacheable : annotations) {
            cacheNames.addAll(Arrays.asList(cacheable.value()));
        }

        execute(joinPoint.getTarget(), annotatedElement, joinPoint.getArgs(), cacheNames);
        return joinPoint.proceed();
    }

    private void execute(Object target,
                           Method method, Object[] args, Set<String> cacheNames) {
        invocation.registerInvocation(target, method, args, cacheNames);
    }

    /**
     * Parses all annotations declared on the Method
     */
    private static <T extends Annotation> List<T> getMethodAnnotations(AnnotatedElement ae, Class<T> annotationType) {
        List<T> annotations = new ArrayList<>(2);
        // look for raw annotation
        T rawAnno = ae.getAnnotation(annotationType);
        if (rawAnno != null) {
            annotations.add(rawAnno);
        }
        // look for meta-annotations
        for (Annotation metaAnno : ae.getAnnotations()) {
            rawAnno = metaAnno.annotationType().getAnnotation(annotationType);
            if (rawAnno != null) {
                annotations.add(rawAnno);
            }
        }
        return (annotations.isEmpty() ? Collections.emptyList() : annotations);
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

    /**
     * Abstract the invocation of a cache operation.
     *
     * <p>Does not provide a way to transmit checked exceptions but
     * provide a special exception that should be used to wrap any
     * exception that was thrown by the underlying invocation.
     * Callers are expected to handle this issue type specifically.
     *
     * @see org.springframework.cache.interceptor.CacheOperationInvoker
     */
    protected interface CacheOperationInvoker {

        /**
         * Invoke the cache operation defined by this instance. Wraps any exception
         * that is thrown during the invocation in a {@link ThrowableWrapper}.
         * @return the result of the operation
         * @throws ThrowableWrapper if an error occurred while invoking the operation
         */
        Object invoke() throws ThrowableWrapper;

        class ThrowableWrapper extends RuntimeException {

            private final Throwable original;

            public ThrowableWrapper(Throwable original) {
                super(original.getMessage(), original);
                this.original = original;
            }

            public Throwable getOriginal() {
                return this.original;
            }
        }

    }

}
