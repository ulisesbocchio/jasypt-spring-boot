package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

public class CachingDelegateEncryptablePropertySource<T> extends PropertySource<T> implements EncryptablePropertySource<T> {
    private final PropertySource<T> delegate;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final ConcurrentMapCache cache;

    public CachingDelegateEncryptablePropertySource(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        Assert.notNull(filter, "EncryptablePropertyFilter cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
        this.filter = filter;
        this.cache = new ConcurrentMapCache("encryptablePropertiesCache");
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }

    @Override
    public Object getProperty(String name) {
        return cache.get(name, () -> getProperty(resolver, filter, delegate, name));
    }

    @Override
    public void refresh() {
        cache.clear();
    }
}
