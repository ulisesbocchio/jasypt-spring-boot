package com.ulisesbocchio.jasyptspringboot.configuration;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NamedBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.function.Supplier;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class StringEncryptorConfiguration {

    public static final String ENCRYPTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.bean:jasyptStringEncryptor}";
    private static final Logger LOG = LoggerFactory.getLogger(StringEncryptorConfiguration.class);

    @ConditionalOnMissingBean(name = ENCRYPTOR_BEAN_PLACEHOLDER)
    @Bean(name = ENCRYPTOR_BEAN_PLACEHOLDER)
    public StringEncryptor stringEncryptor(Environment environment) {
        String encrytorBeanName = environment.resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        LOG.info("String Encryptor custom Bean not found with name '{}'. Initializing String Encryptor based on properties with name '{}'",
                 encrytorBeanName, encrytorBeanName);
        return new LazyStringEncryptor(() -> {
            PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword(getRequiredProperty(environment, "jasypt.encryptor.password"));
            config.setAlgorithm(getProperty(environment, "jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
            config.setKeyObtentionIterations(getProperty(environment, "jasypt.encryptor.keyObtentionIterations", "1000"));
            config.setPoolSize(getProperty(environment, "jasypt.encryptor.poolSize", "1"));
            config.setProviderName(getProperty(environment, "jasypt.encryptor.providerName", "SunJCE"));
            config.setSaltGeneratorClassName(getProperty(environment, "jasypt.encryptor.saltGeneratorClassname", "org.jasypt.salt.RandomSaltGenerator"));
            config.setStringOutputType(getProperty(environment, "jasypt.encryptor.stringOutputType", "base64"));
            encryptor.setConfig(config);
            return encryptor;
        });
    }

    private String getProperty(Environment environment, String key, String defaultValue) {
        if (!propertyExists(environment, key)) {
            LOG.info("Encryptor config not found for property {}, using default value: {}", key, defaultValue);
        }
        return environment.getProperty(key, defaultValue);
    }

    private boolean propertyExists(Environment environment, String key) {
        return environment.getProperty(key) != null;
    }

    private String getRequiredProperty(Environment environment, String key) {
        if (!propertyExists(environment, key)) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return environment.getProperty(key);
    }

    private static final class LazyStringEncryptor implements StringEncryptor {

        private final Supplier<StringEncryptor> supplier;

        private LazyStringEncryptor(final Supplier<StringEncryptor> encryptorFactory) {
            supplier = new SingletonSupplier<>(encryptorFactory);
        }

        @Override
        public String encrypt(String message) {
            return supplier.get().encrypt(message);
        }

        @Override
        public String decrypt(String encryptedMessage) {
            return supplier.get().decrypt(encryptedMessage);
        }
    }

    private static final class SingletonSupplier<T> implements Supplier<T> {

        private Supplier<T> singleton;
        private T value;

        private SingletonSupplier(final Supplier<T> original) {
            this.singleton = () -> {
                synchronized (original) {
                    if (value == null) {
                        value = original.get();
                        singleton = () -> value;
                    }
                    return value;
                }
            };
        }

        @Override
        public T get() {
            return singleton.get();
        }
    }
}
