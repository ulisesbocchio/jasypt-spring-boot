package com.ulisesbocchio.jasyptspringboot;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public class EncryptablePropertySourceMethodInterceptor<T> implements MethodInterceptor, EncryptablePropertySource<T> {

    private final StringEncryptor encryptor;

    public EncryptablePropertySourceMethodInterceptor(StringEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object returnValue = invocation.proceed();
        if(isGetPropertyCall(invocation)) {
            return new DefaultMethods<T>().getProperty(encryptor, getPropertySource(invocation), getNameArgument(invocation));
        }
        return returnValue;
    }

    @SuppressWarnings("unchecked")
    private PropertySource<T> getPropertySource(MethodInvocation invocation) {
        return (PropertySource<T>) invocation.getThis();
    }

    private String getNameArgument(MethodInvocation invocation) {
        return (String) invocation.getArguments()[0];
    }

    private boolean isGetPropertyCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("getProperty")
                && invocation.getMethod().getParameterTypes().length == 1
                && invocation.getMethod().getParameterTypes()[0] == String.class;
    }
}
