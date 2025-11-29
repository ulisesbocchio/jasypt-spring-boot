package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

/**
 * <p>CachingDelegateEncryptablePropertySource class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class CachingDelegateEncryptablePropertySource<T> extends PropertySource<T> implements EncryptablePropertySource<T> {
    private final PropertySource<T> delegate;
    private final CachingResolver cachingResolver;
    @Setter
    private boolean wrapGetSource = false;

    /**
     * <p>Constructor for CachingDelegateEncryptablePropertySource.</p>
     *
     * @param delegate a {@link org.springframework.core.env.PropertySource} object
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     */
    public CachingDelegateEncryptablePropertySource(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        Assert.notNull(filter, "EncryptablePropertyFilter cannot be null");
        this.delegate = delegate;
        this.cachingResolver = new CachingResolver(
            resolver,
            filter,
            delegate
        );
    }

    /** {@inheritDoc} */
    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public Object getProperty(@NonNull String name) {
        return cachingResolver.resolveProperty(name);
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {
        log.info("Property Source {} refreshed", delegate.getName());
        cachingResolver.refresh();
    }

    /** {@inheritDoc} */
    @Override
    @NonNull
    public T getSource() {
        T source = delegate.getSource();
        if (this.wrapGetSource && source instanceof java.util.Map) {
            @SuppressWarnings("unchecked")
            T wrapped = (T) new EncryptableMapWrapper(cachingResolver);
            return wrapped;
        }
        return source;
    }
}
