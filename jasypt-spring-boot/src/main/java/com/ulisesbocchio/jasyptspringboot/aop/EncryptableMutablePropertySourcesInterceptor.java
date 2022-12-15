package com.ulisesbocchio.jasyptspringboot.aop;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.env.PropertySource;

import java.util.List;

/**
 * <p>EncryptableMutablePropertySourcesInterceptor class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class EncryptableMutablePropertySourcesInterceptor implements MethodInterceptor {

    private final EncryptablePropertySourceConverter propertyConverter;
    private final EnvCopy envCopy;

    /**
     * <p>Constructor for EncryptableMutablePropertySourcesInterceptor.</p>
     *
     * @param propertyConverter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter} object
     * @param envCopy a {@link com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy} object
     */
    public EncryptableMutablePropertySourcesInterceptor(EncryptablePropertySourceConverter propertyConverter, EnvCopy envCopy) {
        this.propertyConverter = propertyConverter;
        this.envCopy = envCopy;
    }

    private Object makeEncryptable(Object propertySource) {
        return propertyConverter.makeEncryptable((PropertySource<?>) propertySource);
    }

    /** {@inheritDoc} */
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String method = invocation.getMethod().getName();
        Object[] arguments = invocation.getArguments();
        switch (method) {
            case "addFirst":
                envCopy.addFirst((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addLast":
                envCopy.addLast((PropertySource<?>) arguments[0]);
                return invocation.getMethod().invoke(invocation.getThis(), makeEncryptable(arguments[0]));
            case "addBefore":
                envCopy.addBefore((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "addAfter":
                envCopy.addAfter((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "replace":
                envCopy.replace((String) arguments[0], (PropertySource<?>) arguments[1]);
                return invocation.getMethod().invoke(invocation.getThis(), arguments[0], makeEncryptable(arguments[1]));
            case "remove":
                envCopy.remove((String) arguments[0]);
                return invocation.proceed();
            default:
                return invocation.proceed();
        }

    }
}
