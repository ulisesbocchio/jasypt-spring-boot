package com.ulisesbocchio.jasyptspringboot.caching;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * <p>EncryptableMapWrapper class.</p>
 * Decorates a Map to resolve property values on access through a CachingResolver.
 * All value retrieval methods (get, values, entrySet, forEach, etc.) trigger property resolution.
 * The CachingResolver already contains the delegate PropertySource, so we don't need to duplicate it.
 *
 * @author Sergio.U.Bocchio
 */
public class EncryptableMapWrapper implements Map<String, Object> {

    @Delegate
    private final Map<String, Object> delegate;

    private final CachingResolver cachingResolver;

    /**
     * <p>Constructor for EncryptableMapWrapper.</p>
     *
     * @param cachingResolver a {@link CachingResolver} object that handles property resolution
     */
    public EncryptableMapWrapper(CachingResolver cachingResolver) {
        @SuppressWarnings("unchecked")
        Map<String, Object> source = (Map<String, Object>) cachingResolver.getDelegate().getSource();
        this.delegate = source;
        this.cachingResolver = cachingResolver;
    }

    @Override
    public Object get(Object key) {
        return key instanceof String ? cachingResolver.resolveProperty((String) key) : delegate.get(key);
    }

    @Override
    public Object getOrDefault(Object key, Object defaultValue) {
        return key instanceof String ? cachingResolver.resolveProperty((String) key) : delegate.getOrDefault(key, defaultValue);
    }

    @Override
    @NonNull
    public Collection<Object> values() {
        return new AbstractCollection<>() {
            @Override
            @NonNull
            public Iterator<Object> iterator() {
                return new DecoratingEntryValueIterator(delegate.entrySet().iterator());
            }

            @Override
            public int size() {
                return delegate.size();
            }
        };
    }

    @Override
    @NonNull
    public Set<Entry<String, Object>> entrySet() {
        return new AbstractSet<>() {
            @Override
            @NonNull
            public Iterator<Entry<String, Object>> iterator() {
                return new DecoratingEntryIterator(delegate.entrySet().iterator());
            }

            @Override
            public int size() {
                return delegate.size();
            }
        };
    }

    private class DecoratingEntryIterator implements Iterator<Entry<String, Object>> {
        private final Iterator<Entry<String, Object>> delegate;

        DecoratingEntryIterator(Iterator<Entry<String, Object>> delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean hasNext() {
            return delegate.hasNext();
        }

        @Override
        public Entry<String, Object> next() {
            Entry<String, Object> entry = delegate.next();
            return new AbstractMap.SimpleEntry<>(entry.getKey(), cachingResolver.resolveProperty(entry.getKey()));
        }
    }

    @Override
    public void forEach(BiConsumer<? super String, ? super Object> action) {
        delegate.forEach((k, v) -> action.accept(k, cachingResolver.resolveProperty(k)));
    }

    private class DecoratingEntryValueIterator implements Iterator<Object> {
        private final Iterator<Entry<String, Object>> entryIterator;

        DecoratingEntryValueIterator(Iterator<Entry<String, Object>> entryIterator) {
            this.entryIterator = entryIterator;
        }

        @Override
        public boolean hasNext() {
            return entryIterator.hasNext();
        }

        @Override
        public Object next() {
            Entry<String, Object> entry = entryIterator.next();
            return cachingResolver.resolveProperty(entry.getKey());
        }
    }
}

