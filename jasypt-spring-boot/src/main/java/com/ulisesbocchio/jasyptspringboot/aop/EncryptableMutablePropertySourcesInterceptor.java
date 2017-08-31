package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.function.Function;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMutablePropertySourcesInterceptor implements MethodInterceptor {

    private final InterceptionMode interceptionMode;
    private final EncryptablePropertyResolver resolver;

    public EncryptableMutablePropertySourcesInterceptor(InterceptionMode interceptionMode, EncryptablePropertyResolver resolver) {
        this.interceptionMode = interceptionMode;
        this.resolver = resolver;
    }

    private Object makeEncryptable(Object propertySource) {
        return EncryptablePropertySourceConverter.makeEncryptable(interceptionMode, resolver, (PropertySource<?>) propertySource);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        switch (method) {
            case "addFirst":
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addLast":
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addBefore":
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "addAfter":
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "replace":
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            default:
                return invocation.proceed();
        }

    }
}
