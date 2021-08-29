package com.ulisesbocchio.jasyptspringboot;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public interface EncryptablePropertySource<T> extends OriginLookup<String> {

    PropertySource<T> getDelegate();

    default Object getProperty(String name) {
        return getDelegate().getProperty(name);
    };

    default void refresh() {
        if(getDelegate() instanceof EncryptablePropertySource) {
            ((EncryptablePropertySource<?>) getDelegate()).refresh();
        }
    }

    default Object getProperty(EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter, PropertySource<T> source, String name) {
        Object value = source.getProperty(name);
        if (value != null && filter.shouldInclude(source, name) && value instanceof String) {
            String stringValue = String.valueOf(value);
            return resolver.resolvePropertyValue(stringValue);
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    default Origin getOrigin(String key) {
        if(getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<String>) getDelegate()).getOrigin(key);
        }
        return null;
    }

    @Override
    default boolean isImmutable() {
        if(getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).isImmutable();
        }
        return OriginLookup.super.isImmutable();
    }

    @Override
    default String getPrefix() {
        if(getDelegate() instanceof OriginLookup) {
            return ((OriginLookup<?>) getDelegate()).getPrefix();
        }
        return OriginLookup.super.getPrefix();
    }
}
