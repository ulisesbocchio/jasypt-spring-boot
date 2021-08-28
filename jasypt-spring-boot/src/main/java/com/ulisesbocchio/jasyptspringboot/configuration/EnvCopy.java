package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.environment.EncryptableEnvironment;
import com.ulisesbocchio.jasyptspringboot.util.ClassUtils;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Optional;

/**
 * Need a copy of the environment without the Enhanced property sources to avoid circular dependencies.
 */
public class EnvCopy {
    StandardEnvironment copy;

    public EnvCopy(final ConfigurableEnvironment environment) {
        copy = new StandardEnvironment();
        Optional
                .ofNullable(environment instanceof EncryptableEnvironment ? ((EncryptableEnvironment) environment).getOriginalPropertySources() : environment.getPropertySources())
                .ifPresent(sources -> sources.forEach(this::addLast));
    }

    @SuppressWarnings({"rawtypes"})
    private PropertySource<?> getOriginal(PropertySource<?> propertySource) {
        return propertySource instanceof EncryptablePropertySource
                ? ((EncryptablePropertySource) propertySource).getDelegate()
                : propertySource;
    }

    public boolean isAllowed(PropertySource<?> propertySource) {
        final PropertySource<?> original = getOriginal(propertySource);
        return !original.getClass().getName().equals("org.springframework.boot.context.properties.source.ConfigurationPropertySourcesPropertySource");
    }

    public void addFirst(PropertySource<?> propertySource) {
        if (isAllowed(propertySource)) {
            final PropertySource<?> original = getOriginal(propertySource);
            copy.getPropertySources().addFirst(original);
        }
    }

    public void addLast(PropertySource<?> propertySource) {
        if (isAllowed(propertySource)) {
            final PropertySource<?> original = getOriginal(propertySource);
            copy.getPropertySources().addLast(original);
        }
    }

    public void addBefore(String relativePropertySourceName, PropertySource<?> propertySource) {
        if (isAllowed(propertySource)) {
            final PropertySource<?> original = getOriginal(propertySource);
            copy.getPropertySources().addBefore(relativePropertySourceName, original);
        }
    }

    public void addAfter(String relativePropertySourceName, PropertySource<?> propertySource) {
        if (isAllowed(propertySource)) {
            final PropertySource<?> original = getOriginal(propertySource);
            copy.getPropertySources().addAfter(relativePropertySourceName, original);
        }
    }

    public void replace(String name, PropertySource<?> propertySource) {
        if(isAllowed(propertySource)) {
            if(copy.getPropertySources().contains(name)) {
                final PropertySource<?> original = getOriginal(propertySource);
                copy.getPropertySources().replace(name, original);
            }
        }
    }

    public PropertySource<?> remove(String name) {
        return copy.getPropertySources().remove(name);
    }

    public ConfigurableEnvironment get() {
        return copy;
    }
}
