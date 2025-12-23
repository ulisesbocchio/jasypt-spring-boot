package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import org.jspecify.annotations.NonNull;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Map;

/**
 * <p>EncryptableMapPropertySourceWrapper class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class EncryptableMapPropertySourceWrapper extends MapPropertySource implements EncryptablePropertySource<Map<String, Object>> {

    private final CachingDelegateEncryptablePropertySource<Map<String, Object>> encryptableDelegate;

    /**
     * <p>Constructor for EncryptableMapPropertySourceWrapper.</p>
     *
     * @param delegate a {@link org.springframework.core.env.MapPropertySource} object
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter   a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     */
    public EncryptableMapPropertySourceWrapper(MapPropertySource delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getProperty(@NonNull String name) {
        return encryptableDelegate.getProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertySource<Map<String, Object>> getDelegate() {
        return encryptableDelegate;
    }
}
