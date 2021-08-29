package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

/**
 * @author Tomas Tulka (@ttulka)
 */
public class EncryptableSystemEnvironmentPropertySourceWrapper extends SystemEnvironmentPropertySource implements EncryptablePropertySource<Map<String, Object>> {

    private final CachingDelegateEncryptablePropertySource<Map<String, Object>> encryptableDelegate;

    public EncryptableSystemEnvironmentPropertySourceWrapper(SystemEnvironmentPropertySource delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    @Override
    public Object getProperty(String name) {
        return encryptableDelegate.getProperty(name);
    }

    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return encryptableDelegate;
    }

    @Override
    public Origin getOrigin(String key) {
        Origin fromSuper = EncryptablePropertySource.super.getOrigin(key);
        if (fromSuper != null) {
            return fromSuper;
        }
        String property = resolvePropertyName(key);
        if (super.containsProperty(property)) {
            return new SystemEnvironmentOrigin(property);
        }
        return null;
    }
}
