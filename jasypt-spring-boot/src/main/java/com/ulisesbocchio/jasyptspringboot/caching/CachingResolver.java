package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertySource;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>CachingResolver class.</p>
 * Handles the actual caching and resolution logic.
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class CachingResolver {
    private final EncryptablePropertyResolver resolver;
    private final EncryptablePropertyFilter filter;
    /**
     * -- GETTER --
     *  <p>Get the delegate PropertySource.</p>
     *
     * @return the delegate PropertySource
     */
    @Getter
    private final PropertySource<?> delegate;
    private final ConcurrentHashMap<String, CachedValue> cache;

    /**
     * <p>Constructor for CachingResolver.</p>
     *
     * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
     * @param filter   a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
     * @param delegate the delegate PropertySource to retrieve values from
     */
    public CachingResolver(EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, PropertySource<?> delegate) {
        this.resolver = resolver;
        this.filter = filter;
        this.delegate = delegate;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * <p>Resolve property with caching.</p>
     * Retrieves the property value from the delegate and resolves it if needed.
     * The purpose of this cache is to reduce the cost of decryption,
     * so it's not a bad idea to read the original property every time, it's generally fast.
     *
     * @param name the property name
     * @return the resolved property value or the original value
     */
    public Object resolveProperty(String name) {
        Object originalValue = delegate.getProperty(name);

        if (!(originalValue instanceof String)) {
            //Because we read the original property every time, if it isn't a String,
            // there's no point in caching it.
            return originalValue;
        }

        CachedValue cachedValue = cache.get(name);
        if (cachedValue != null && Objects.equals(originalValue, cachedValue.originValue)) {
            // If the original property has not changed, it is safe to return the cached result.
            return cachedValue.resolvedValue;
        }

        //originalValue must be String here
        if (filter.shouldInclude(delegate, name)) {
            String originStringValue = (String) originalValue;
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
        return originalValue;
    }

    /**
     * <p>Refresh the cache.</p>
     */
    public void refresh() {
        log.info("CachingResolver cache refreshed");
        cache.clear();
    }

    @AllArgsConstructor
    static class CachedValue {
        private final String originValue;
        private final String resolvedValue;
    }
}

