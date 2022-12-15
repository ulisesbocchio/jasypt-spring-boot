package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

/**
 * <p>EncryptablePropertySourceMethodInterceptor class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class EncryptablePropertySourceMethodInterceptor<T> extends CachingDelegateEncryptablePropertySource<T> implements MethodInterceptor {

    /**
     * <p>Constructor for EncryptablePropertySourceMethodInterceptor.</p>
     *
     * @param delegate a {@link org.springframework.core.env.PropertySource} object
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     */
    public EncryptablePropertySourceMethodInterceptor(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate, resolver, filter);
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (isRefreshCall(invocation)) {
            refresh();
            return null;
        }
        if (isGetDelegateCall(invocation)) {
            return getDelegate();
        }
        if (isGetPropertyCall(invocation)) {
            return getProperty(getNameArgument(invocation));
        }
        return invocation.proceed();
    }

    private String getNameArgument(MethodInvocation invocation) {
        return (String) invocation.getArguments()[0];
    }

    private boolean isGetDelegateCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("getDelegate");
    }

    private boolean isRefreshCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("refresh");
    }

    private boolean isGetPropertyCall(MethodInvocation invocation) {
        return invocation.getMethod().getName().equals("getProperty")
                && invocation.getMethod().getParameters().length == 1
                && invocation.getMethod().getParameters()[0].getType() == String.class;
    }
}
