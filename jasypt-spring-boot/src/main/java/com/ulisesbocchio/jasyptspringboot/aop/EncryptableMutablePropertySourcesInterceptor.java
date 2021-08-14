package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMutablePropertySourcesInterceptor implements MethodInterceptor {

    private final EncryptablePropertySourceConverter propertyConverter;
    private final EnvCopy envCopy;

    public EncryptableMutablePropertySourcesInterceptor(EncryptablePropertySourceConverter propertyConverter, EnvCopy envCopy) {
        this.propertyConverter = propertyConverter;
        this.envCopy = envCopy;
    }

    private Object makeEncryptable(Object propertySource) {
        return propertyConverter.makeEncryptable((PropertySource<?>) propertySource);
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        switch (method) {
            case "addFirst":
                envCopy.get().getPropertySources().addFirst((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addLast":
                envCopy.get().getPropertySources().addLast((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addBefore":
                final String name1 = (String) arguments[0];
                envCopy.getPropertySources(name1).addBefore(name1, (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "addAfter":
                final String name2 = (String) arguments[0];
                envCopy.getPropertySources(name2).addAfter(name2, (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "replace":
                final String name3 = (String) arguments[0];
                envCopy.getPropertySources(name3).replace(name3, (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            default:
                return invocation.proceed();
        }

    }
}
