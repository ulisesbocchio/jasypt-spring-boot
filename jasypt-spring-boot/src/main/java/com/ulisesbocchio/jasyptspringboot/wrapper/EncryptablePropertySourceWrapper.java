package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * <p>Wrapper for {@link PropertySource} instances that simply delegates the {@link #getProperty} method
 * to the {@link PropertySource} delegate instance to retrieve properties, while checking if the resulting
 * property is encrypted or not using the Jasypt convention of surrounding encrypted values with "ENC()".</p>
 * <p>When an encrypted property is detected, it is decrypted using the provided {@link StringEncryptor}</p>
 *
 * @author Ulises Bocchio
 */
public class EncryptablePropertySourceWrapper<T> extends PropertySource<T> implements EncryptablePropertySource<T> {
    private final PropertySource<T> delegate;
    EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;

    public EncryptablePropertySourceWrapper(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        Assert.notNull(filter, "EncryptablePropertyFilter cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
        this.filter = filter;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, filter, delegate, name);
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }
}
