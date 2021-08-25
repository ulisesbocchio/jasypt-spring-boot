package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
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

    @SuppressWarnings({"rawtypes", "ConstantConditions"})
    public EnvCopy(final ConfigurableEnvironment environment) {
        copy = new StandardEnvironment();
        Optional.ofNullable(environment.getPropertySources()).ifPresent(sources -> sources.forEach(ps -> {
            final PropertySource<?> original = ps instanceof EncryptablePropertySource
                    ? ((EncryptablePropertySource) ps).getDelegate()
                    : ps;
            if (!(ClassUtils.isAssignable(new ParameterizedTypeReference<PropertySource<Iterable<ConfigurationPropertySource>>>() {}, original.getClass()))) {
                copy.getPropertySources().addLast(original);
            }
        }));
    }

    public ConfigurableEnvironment get() {
        return copy;
    }
}
