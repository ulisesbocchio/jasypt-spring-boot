package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.*;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class EncryptablePropertyResolverConfiguration {

    private static final String ENCRYPTOR_BEAN_PROPERTY = "jasypt.encryptor.bean";
    private static final String ENCRYPTOR_BEAN_PLACEHOLDER = String.format("${%s:jasyptStringEncryptor}", ENCRYPTOR_BEAN_PROPERTY);
    private static final String DETECTOR_BEAN_PROPERTY = "jasypt.encryptor.property.detector-bean";
    private static final String DETECTOR_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyDetector}", DETECTOR_BEAN_PROPERTY);
    private static final String RESOLVER_BEAN_PROPERTY = "jasypt.encryptor.property.resolver-bean";
    private static final String RESOLVER_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyResolver}", RESOLVER_BEAN_PROPERTY);
    private static final String FILTER_BEAN_PROPERTY = "jasypt.encryptor.property.filter-bean";
    private static final String FILTER_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyFilter}", FILTER_BEAN_PROPERTY);

    private static final String ENCRYPTOR_BEAN_NAME = "lazyJasyptStringEncryptor";
    private static final String DETECTOR_BEAN_NAME = "lazyEncryptablePropertyDetector";
    private static final String CONFIG_SINGLETON = "configPropsSingleton";
    public static final String RESOLVER_BEAN_NAME = "lazyEncryptablePropertyResolver";
    public static final String FILTER_BEAN_NAME = "lazyEncryptablePropertyFilter";

    @SuppressWarnings("unchecked")
    @Bean
    public static EncryptablePropertySourceConverter encryptablePropertySourceConverter(ConfigurableEnvironment environment, @Qualifier(RESOLVER_BEAN_NAME)  EncryptablePropertyResolver propertyResolver, @Qualifier(FILTER_BEAN_NAME) EncryptablePropertyFilter propertyFilter) {
        final boolean proxyPropertySources = environment.getProperty("jasypt.encryptor.proxy-property-sources", Boolean.TYPE, false);
        final List<String> skipPropertySources = (List<String>) environment.getProperty("jasypt.encryptor.skip-property-sources", List.class, Collections.EMPTY_LIST);
        final List<Class<PropertySource<?>>> skipPropertySourceClasses = skipPropertySources.stream().map(EncryptablePropertySourceConverter::getPropertiesClass).collect(Collectors.toList());
        final InterceptionMode interceptionMode = proxyPropertySources ? InterceptionMode.PROXY : InterceptionMode.WRAPPER;
        return new EncryptablePropertySourceConverter(interceptionMode, skipPropertySourceClasses, propertyResolver, propertyFilter);
    }

    @SuppressWarnings("unchecked")
    @Bean
    public EnvCopy envCopy(final ConfigurableEnvironment environment) {
        return new EnvCopy(environment);
    }

    @Bean(name = ENCRYPTOR_BEAN_NAME)
    public StringEncryptor stringEncryptor(
            final EnvCopy envCopy,
            final BeanFactory bf) {
        final String customEncryptorBeanName = envCopy.get().resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(ENCRYPTOR_BEAN_PROPERTY);
        return new DefaultLazyEncryptor(envCopy.get(), customEncryptorBeanName, isCustom, bf);
    }

    @Bean(name = DETECTOR_BEAN_NAME)
    public EncryptablePropertyDetector encryptablePropertyDetector(
            final EnvCopy envCopy,
            final BeanFactory bf) {
        final String customDetectorBeanName = envCopy.get().resolveRequiredPlaceholders(DETECTOR_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(DETECTOR_BEAN_PROPERTY);
        return new DefaultLazyPropertyDetector(envCopy.get(), customDetectorBeanName, isCustom, bf);
    }

    @Bean(name = CONFIG_SINGLETON)
    public Singleton<JasyptEncryptorConfigurationProperties> configProps(
            final EnvCopy envCopy) {
        return new Singleton<>(() -> JasyptEncryptorConfigurationProperties.bindConfigProps(envCopy.get()));
    }

    @Bean(name = FILTER_BEAN_NAME)
    public EncryptablePropertyFilter encryptablePropertyFilter(
            final EnvCopy envCopy,
            final ConfigurableBeanFactory bf) {
        final String customFilterBeanName = envCopy.get().resolveRequiredPlaceholders(FILTER_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(FILTER_BEAN_PROPERTY);
        return new DefaultLazyPropertyFilter(envCopy.get(), customFilterBeanName, isCustom, bf);
    }

    @Bean(name = RESOLVER_BEAN_NAME)
    public EncryptablePropertyResolver encryptablePropertyResolver(
            @Qualifier(DETECTOR_BEAN_NAME) final EncryptablePropertyDetector propertyDetector,
            @Qualifier(ENCRYPTOR_BEAN_NAME) final StringEncryptor encryptor, final BeanFactory bf,
            final EnvCopy envCopy, final ConfigurableEnvironment environment) {
        final String customResolverBeanName = envCopy.get().resolveRequiredPlaceholders(RESOLVER_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(RESOLVER_BEAN_PROPERTY);
        return new DefaultLazyPropertyResolver(propertyDetector, encryptor, customResolverBeanName, isCustom, bf, environment);
    }

}
