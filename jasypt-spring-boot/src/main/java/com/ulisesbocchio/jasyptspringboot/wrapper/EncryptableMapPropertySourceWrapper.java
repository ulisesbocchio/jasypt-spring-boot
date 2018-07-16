package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author Ulises Bocchio
 */
public class EncryptableMapPropertySourceWrapper extends MapPropertySource implements EncryptablePropertySource<Map<String, Object>> {
    private final EncryptablePropertyResolver resolver;
    private final MapPropertySource delegate;
    private final EncryptablePropertyFilter filter;

    public EncryptableMapPropertySourceWrapper(MapPropertySource delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        Assert.notNull(filter, "EncryptablePropertyFilter cannot be null");
        this.resolver = resolver;
        this.delegate = delegate;
        this.filter = filter;
    }

    @Override
    public Object getProperty(String name) {
        return getProperty(resolver, filter, delegate, name);
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return delegate;
    }

    @Override
    public boolean containsProperty(String name) {
       return delegate.containsProperty(name);
    }

    @Override
    public String[] getPropertyNames() {
       return delegate.getPropertyNames();
    }
}
