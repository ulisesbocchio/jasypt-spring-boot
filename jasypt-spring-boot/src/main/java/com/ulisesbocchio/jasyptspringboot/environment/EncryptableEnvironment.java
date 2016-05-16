package com.ulisesbocchio.jasyptspringboot.environment;

import com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorConfiguration;
import com.ulisesbocchio.jasyptspringboot.encryptor.LazyStringEncryptor;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MissingRequiredPropertiesException;
import org.springframework.core.env.MutablePropertySources;

import java.util.Map;

/**
 * @author Ulises Bocchio
 */
public class EncryptableEnvironment implements ConfigurableEnvironment {

    private final ConfigurableEnvironment delegate;
    private StringEncryptor encryptor;

    public EncryptableEnvironment(ConfigurableEnvironment delegate) {
        this(delegate, discoverEncryptor(delegate));
    }

    public EncryptableEnvironment(ConfigurableEnvironment delegate, StringEncryptor encryptor) {
        super();
        this.delegate = delegate;
        this.encryptor = encryptor;
    }

    private static StringEncryptor discoverEncryptor(ConfigurableEnvironment delegate) {
        return new LazyStringEncryptor(StringEncryptorConfiguration.DEFAULT_LAZY_ENCRYPTOR_FACTORY, delegate);
    }

    @Override
    public void addActiveProfile(String profile) {
        delegate.addActiveProfile(profile);
    }

    @Override
    public MutablePropertySources getPropertySources() {
        return delegate.getPropertySources();
    }

    @Override
    public Map<String, Object> getSystemEnvironment() {
        return delegate.getSystemEnvironment();
    }

    @Override
    public Map<String, Object> getSystemProperties() {
        return delegate.getSystemProperties();
    }

    @Override
    public void merge(ConfigurableEnvironment parent) {
        delegate.merge(parent);
    }

    @Override
    public ConfigurableConversionService getConversionService() {
        return delegate.getConversionService();
    }

    @Override
    public void setConversionService(ConfigurableConversionService conversionService) {
        delegate.setConversionService(conversionService);
    }

    @Override
    public void setPlaceholderPrefix(String placeholderPrefix) {
        delegate.setPlaceholderPrefix(placeholderPrefix);
    }

    @Override
    public void setPlaceholderSuffix(String placeholderSuffix) {
        delegate.setPlaceholderSuffix(placeholderSuffix);
    }

    @Override
    public void setValueSeparator(String valueSeparator) {
        delegate.setValueSeparator(valueSeparator);
    }

    @Override
    public void setIgnoreUnresolvableNestedPlaceholders(boolean ignoreUnresolvableNestedPlaceholders) {
        delegate.setIgnoreUnresolvableNestedPlaceholders(ignoreUnresolvableNestedPlaceholders);
    }

    @Override
    public void setRequiredProperties(String... requiredProperties) {
        delegate.setRequiredProperties(requiredProperties);
    }

    @Override
    public void validateRequiredProperties() throws MissingRequiredPropertiesException {
        delegate.validateRequiredProperties();
    }

    @Override
    public String[] getActiveProfiles() {
        return delegate.getActiveProfiles();
    }

    @Override
    public void setActiveProfiles(String... profiles) {
        delegate.setActiveProfiles(profiles);
    }

    @Override
    public String[] getDefaultProfiles() {
        return delegate.getDefaultProfiles();
    }

    @Override
    public void setDefaultProfiles(String... profiles) {
        delegate.setDefaultProfiles(profiles);
    }

    @Override
    public boolean acceptsProfiles(String... profiles) {
        return delegate.acceptsProfiles(profiles);
    }

    @Override
    public boolean containsProperty(String key) {
        return delegate.containsProperty(key);
    }

    @Override
    public String getProperty(String key) {
        return maybeDecrypt(delegate.getProperty(key));
    }

    private String maybeDecrypt(String property) {
        if (PropertyValueEncryptionUtils.isEncryptedValue(property)) {
            return PropertyValueEncryptionUtils.decrypt(property, encryptor);
        }
        return property;
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return maybeDecrypt(delegate.getProperty(key, defaultValue));
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType) {
        T property = delegate.getProperty(key, targetType);
        if (property != null && targetType == String.class) {
            property = (T) maybeDecrypt((String) property);
        }
        return property;
    }

    @Override
    public <T> T getProperty(String key, Class<T> targetType, T defaultValue) {
        T property = delegate.getProperty(key, targetType, defaultValue);
        if (property != null && targetType == String.class) {
            property = (T) maybeDecrypt((String) property);
        }
        return property;
    }

    @Override
    public <T> Class<T> getPropertyAsClass(String key, Class<T> targetType) {
        return delegate.getPropertyAsClass(key, targetType);
    }

    @Override
    public String getRequiredProperty(String key) throws IllegalStateException {
        return maybeDecrypt(delegate.getRequiredProperty(key));
    }

    @Override
    public <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException {
        T property = delegate.getRequiredProperty(key, targetType);
        if (property != null && targetType == String.class) {
            property = (T) maybeDecrypt((String) property);
        }
        return property;
    }

    @Override
    public String resolvePlaceholders(String text) {
        return delegate.resolvePlaceholders(text);
    }

    @Override
    public String resolveRequiredPlaceholders(String text) throws IllegalArgumentException {
        return delegate.resolveRequiredPlaceholders(text);
    }
}
