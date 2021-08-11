package com.ulisesbocchio.jasyptspringboot.filter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import com.ulisesbocchio.jasyptspringboot.SkipPropertySourceFilter;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;

import lombok.extern.slf4j.Slf4j;

import static com.ulisesbocchio.jasyptspringboot.util.Functional.tap;

@Slf4j
public class DefaultLazySkipPropertySourceFilter implements SkipPropertySourceFilter {

    private Singleton<SkipPropertySourceFilter> singleton;

    public DefaultLazySkipPropertySourceFilter(ConfigurableEnvironment e, String customFilterBeanName, boolean isCustom, BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customFilterBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (SkipPropertySourceFilter) bf.getBean(name))
                        .map(tap(bean -> log.info("Found Custom Filter Bean {} with name: {}", bean, customFilterBeanName)))
                        .orElseGet(() -> {
                            if (isCustom) {
                                throw new IllegalStateException(String.format("Property Filter custom Bean not found with name '%s'", customFilterBeanName));
                            }

                            log.info("Property Filter custom Bean not found with name '{}'. Initializing Default Property Filter", customFilterBeanName);
                            return createDefault(e);
                        }));
    }

    public DefaultLazySkipPropertySourceFilter(ConfigurableEnvironment environment) {
        singleton = new Singleton<>(() -> createDefault(environment));
    }

    private SkipPropertySourceFilter createDefault(ConfigurableEnvironment environment) {
      final List<String> skipPropertySources = (List<String>) environment.getProperty("jasypt.encryptor.skip-property-sources", List.class, Collections.EMPTY_LIST);
      final List<Class<PropertySource<?>>> skipPropertySourceClasses = skipPropertySources.stream().map(DefaultLazySkipPropertySourceFilter::getPropertiesClass).collect(Collectors.toList());
      return new DefaultSkipPropertySourceFilter(skipPropertySourceClasses);
    }

    @SuppressWarnings("unchecked")
    private static Class<PropertySource<?>> getPropertiesClass(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (PropertySource.class.isAssignableFrom(clazz)) {
                return (Class<PropertySource<?>>) clazz;
            }
            throw new IllegalArgumentException(String.format("Invalid jasypt.encryptor.skip-property-sources: Class %s does not implement %s", className, PropertySource.class.getName()));
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(String.format("Invalid jasypt.encryptor.skip-property-sources: Class %s not found", className), e);
        }
    }
    
    @Override
    public <T> boolean shouldSkip(final PropertySource<T> propertySource) {
      return singleton.get().shouldSkip(propertySource);
    }
}
