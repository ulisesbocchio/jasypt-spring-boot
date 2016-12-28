package com.ulisesbocchio.jasyptspringboot;

import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public interface EncryptablePropertySource<T> {

    PropertySource<T> getDelegate();

    default Object getProperty(EncryptablePropertyResolver resolver, PropertySource<T> source, String name) {
        Object value = source.getProperty(name);
        if (value instanceof String) {
            String stringValue = String.valueOf(value);
            return resolver.resolvePropertyValue(stringValue);
        }
        return value;
    }
}
