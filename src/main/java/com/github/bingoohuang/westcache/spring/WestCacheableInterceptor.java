package com.github.bingoohuang.westcache.spring;

import com.github.bingoohuang.westcache.cglib.CacheMethodInterceptor;
import com.github.bingoohuang.westcache.utils.WestCacheOption;
import lombok.SneakyThrows;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
public class WestCacheableInterceptor
        extends CacheMethodInterceptor<MethodInvocation>
        implements MethodInterceptor {
    @Override
    public Object invoke(final MethodInvocation invocation) {
        // 面向接口服务编程的缓存使用支撑
        Object realObject = invocation.getThis();
        Method method = invocation.getMethod();
        Method realMethod = method;
        try {
            realMethod = realObject.getClass().getMethod(method.getName(), method.getParameterTypes());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return super.intercept(realObject,
                realMethod,
                invocation.getArguments(),
                invocation);
    }

    @Override
    @SneakyThrows
    protected Object invokeRaw(Object obj,
                               Object[] args,
                               MethodInvocation methodProxy) {
        return methodProxy.proceed();
    }

    @Override
    protected String getCacheKey(WestCacheOption option,
                                 Object obj,
                                 Method method,
                                 Object[] args,
                                 MethodInvocation invocation) {
        return option.getKeyer().getCacheKey(option,
                invocation.getMethod(), invocation.getThis(), args);
    }
}
