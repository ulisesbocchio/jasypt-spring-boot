package com.ulisesbocchio.jasyptspringboot.aop;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

import java.util.function.Function;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMutablePropertySourcesInterceptor implements MethodInterceptor {

    private Function<PropertySource<?>, PropertySource<?>> converter;

    public EncryptableMutablePropertySourcesInterceptor(Function<PropertySource<?>, PropertySource<?>> converter) {
        this.converter = converter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        if(method.equals("addFirst")) {
            return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
        } else if(method.equals("addLast")) {
            return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
        } else if(method.equals("addBefore")) {
            return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
        } else if(method.equals("addAfter")) {
            return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
        } else if(method.equals("replace")) {
            return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
        } else {
            return invocation.proceed();
        }

    }

    private Object makeEncryptable(Object argument) {
        return converter.apply((PropertySource<?>)argument);
    }
}
