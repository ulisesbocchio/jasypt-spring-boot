package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

/**
 * @author Ulises Bocchio
 */
public class EncryptableEnumerablePropertySourceWrapper<T> extends EnumerablePropertySource<T> implements EncryptablePropertySource<T> {
    private final EnumerablePropertySource<T> delegate;
    private final EncryptablePropertyResolver resolver;

    public EncryptableEnumerablePropertySourceWrapper(EnumerablePropertySource<T> delegate, EncryptablePropertyResolver resolver) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, delegate, name);
    }

    @Override
    public String[] getPropertyNames() {
        return delegate.getPropertyNames();
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }
}
