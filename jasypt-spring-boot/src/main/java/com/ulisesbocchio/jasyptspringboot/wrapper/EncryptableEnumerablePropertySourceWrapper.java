package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public class EncryptableEnumerablePropertySourceWrapper<T> extends EnumerablePropertySource<T> implements EncryptablePropertySource<T> {
    private final EncryptablePropertySource<T> encryptableDelegate;

    public EncryptableEnumerablePropertySourceWrapper(EnumerablePropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    @Override
    public Object getProperty(String name) {
        return encryptableDelegate.getProperty(name);
    }

    @Override
    public void refresh() {
        encryptableDelegate.refresh();
    }

    @Override
    public PropertySource<T> getDelegate() {
        return encryptableDelegate.getDelegate();
    }

    @Override
    public String[] getPropertyNames() {
        return ((EnumerablePropertySource) encryptableDelegate.getDelegate()).getPropertyNames();
    }
}
