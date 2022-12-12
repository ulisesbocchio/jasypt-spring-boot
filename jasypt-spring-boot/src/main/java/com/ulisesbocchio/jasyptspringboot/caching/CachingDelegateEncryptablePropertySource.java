package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;
import org.springframework.util.Assert;

@Slf4j
public class CachingDelegateEncryptablePropertySource<T> extends PropertySource<T> implements EncryptablePropertySource<T> {
    private final PropertySource<T> delegate;
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    private final Map<String, CachedValue> cache;

    public CachingDelegateEncryptablePropertySource(PropertySource<T> delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
        super(delegate.getName(), delegate.getSource());
        Assert.notNull(delegate, "PropertySource delegate cannot be null");
        Assert.notNull(resolver, "EncryptablePropertyResolver cannot be null");
        Assert.notNull(filter, "EncryptablePropertyFilter cannot be null");
        this.delegate = delegate;
        this.resolver = resolver;
        this.filter = filter;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public PropertySource<T> getDelegate() {
        return delegate;
    }

    @Override
    public Object getProperty(String name) {
        //The purpose of this cache is to reduce the cost of decryption,
        // so it's not a bad idea to read the original property every time, it's generally fast.
        Object originValue = delegate.getProperty(name);
        if (!(originValue instanceof String)) {
            //Because we read the original property every time, if it isn't a String,
            // there's no point in caching it.
            return originValue;
        }

        CachedValue cachedValue = cache.get(name);
        if (cachedValue != null && Objects.equals(originValue, cachedValue.originValue)) {
            // If the original property has not changed, it is safe to return the cached result.
            return cachedValue.resolvedValue;
        }

        //originValue must be String here
        if (filter.shouldInclude(delegate, name)) {
            String originStringValue = (String) originValue;
            String resolved = resolver.resolvePropertyValue(originStringValue);
            CachedValue newCachedValue = new CachedValue(originStringValue, resolved);
            //If the mapping relationship in the cache changes during
            // the calculation process, then ignore it directly.
            if (cachedValue == null) {
                cache.putIfAbsent(name, newCachedValue);
            } else {
                cache.replace(name, cachedValue, newCachedValue);
            }
            //return the result calculated this time
            return resolved;
        }
        return originValue;
    }

    @Override
    public void refresh() {
        log.info("Property Source {} refreshed", delegate.getName());
        cache.clear();
    }

    @AllArgsConstructor
    private static class CachedValue {
        private final String originValue;
        private final String resolvedValue;
    }
}
