package com.ulisesbocchio.jasyptspringboot.filter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.core.env.PropertySource;

import com.ulisesbocchio.jasyptspringboot.SkipPropertySourceFilter;

/**
 * Default SkipPropertySourcesFilter.
 *
 * @author qxo
 *
 */
public class DefaultSkipPropertySourceFilter implements SkipPropertySourceFilter {

    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;

    public DefaultSkipPropertySourceFilter(List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        super();
        this.skipPropertySourceClasses = Optional.ofNullable(skipPropertySourceClasses).orElseGet(Collections::emptyList);
    }

    @Override
    public <T> boolean shouldSkip(PropertySource<T> propertySource) {
        return skipPropertySourceClasses.stream().anyMatch(skipClass -> skipClass.equals(propertySource.getClass()));
    }

}
