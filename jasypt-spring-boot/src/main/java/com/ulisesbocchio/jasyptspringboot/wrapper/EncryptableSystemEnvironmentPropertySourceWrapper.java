package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

/**
 * @author Tomas Tulka (@ttulka)
 */
public class EncryptableSystemEnvironmentPropertySourceWrapper extends SystemEnvironmentPropertySource implements EncryptablePropertySource<Map<String, Object>> {

    private final EncryptablePropertySource<Map<String, Object>> encryptableDelegate;

    public EncryptableSystemEnvironmentPropertySourceWrapper(SystemEnvironmentPropertySource delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    @Override
    public void refresh() {
        encryptableDelegate.refresh();
    }

    @Override
    public Object getProperty(String name) {
        return encryptableDelegate.getProperty(name);
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return encryptableDelegate.getDelegate();
    }
}
