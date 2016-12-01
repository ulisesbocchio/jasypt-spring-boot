package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.properties.PropertyFinder;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public class EncryptablePropertySourceMethodInterceptor<T> implements MethodInterceptor, EncryptablePropertySource<T> {

    private final StringEncryptor encryptor;
    private final PropertyFinder propertyFinder;

    public EncryptablePropertySourceMethodInterceptor(StringEncryptor encryptor, PropertyFinder propertyFinder) {
        this.encryptor = encryptor;
        this.propertyFinder = propertyFinder;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object returnValue = invocation.proceed();
        if (isGetPropertyCall(invocation)) {
            return getProperty(encryptor, getPropertySource(invocation), getNameArgument(invocation));
        }
        return returnValue;
    }

    @Override
    public boolean isEncryptedValue(String stringValue) {
        return propertyFinder.isEncryptedValue(stringValue);
    }

    @Override
    public String decrypt(String encodedValue, StringEncryptor encryptor) {
        return propertyFinder.decrypt(encodedValue, encryptor);
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
                && invocation.getMethod().getParameters().length == 1
                && invocation.getMethod().getParameters()[0].getType() == String.class;
    }
}
