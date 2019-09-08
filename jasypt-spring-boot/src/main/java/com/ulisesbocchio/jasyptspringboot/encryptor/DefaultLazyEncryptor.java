package com.ulisesbocchio.jasyptspringboot.encryptor;

import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import java.util.Optional;

import static com.ulisesbocchio.jasyptspringboot.util.Functional.tap;

/**
 * Default Lazy Encryptor that delegates to a custom {@link StringEncryptor} bean or creates a default {@link PooledPBEStringEncryptor} or {@link SimpleAsymmetricStringEncryptor}
 * based on what properties are provided
 *
 * @author Ulises Bocchio
 */
@Slf4j
public class DefaultLazyEncryptor implements StringEncryptor {

    private final Singleton<StringEncryptor> singleton;

    public DefaultLazyEncryptor(final Environment e, final String customEncryptorBeanName, boolean isCustom, final BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customEncryptorBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (StringEncryptor) bf.getBean(name))
                        .map(tap(bean -> log.info("Found Custom Encryptor Bean {} with name: {}", bean, customEncryptorBeanName)))
                        .orElseGet(() -> {
                            if(isCustom) {
                                throw new IllegalStateException(String.format("String Encryptor custom Bean not found with name '%s'", customEncryptorBeanName));
                            }
                            log.info("String Encryptor custom Bean not found with name '{}'. Initializing Default String Encryptor", customEncryptorBeanName);
                            return createDefault(e);
                        }));
    }

    public DefaultLazyEncryptor(final Environment e) {
        singleton = new Singleton<>(() -> createDefault(e));
    }

    private StringEncryptor createDefault(Environment e) {
        return Optional.of(e)
                .filter(DefaultLazyEncryptor::isPBEConfig)
                .map(this::createPBEDefault)
                .orElseGet(() ->
                        Optional.of(e)
                                .filter(DefaultLazyEncryptor::isAsymmetricConfig)
                                .map(this::createAsymmetricDefault)
                                .orElseThrow(() -> new IllegalStateException("either 'jasypt.encryptor.password' or one of ['jasypt.encryptor.private-key-string', 'jasypt.encryptor.private-key-location'] must be provided for Password-based or Asymmetric encryption")));
    }

    private StringEncryptor createAsymmetricDefault(Environment e) {
        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();
        config.setPrivateKey(getProperty(e, "jasypt.encryptor.private-key-string", null));
        config.setPrivateKeyLocation(getProperty(e, "jasypt.encryptor.private-key-location", null));
        config.setPrivateKeyFormat(AsymmetricCryptography.KeyFormat.valueOf(getProperty(e, "jasypt.encryptor.private-key-format", "DER")));
        return new SimpleAsymmetricStringEncryptor(config);
    }

    private StringEncryptor createPBEDefault(Environment e) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getRequiredProperty(e, "jasypt.encryptor.password"));
        config.setAlgorithm(getProperty(e, "jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
        config.setKeyObtentionIterations(getProperty(e, "jasypt.encryptor.key-obtention-iterations", "1000"));
        config.setPoolSize(getProperty(e, "jasypt.encryptor.pool-size", "1"));
        config.setProviderName(getProperty(e, "jasypt.encryptor.provider-name", null));
        config.setProviderClassName(getProperty(e, "jasypt.encryptor.provider-class-name", null));
        config.setSaltGeneratorClassName(getProperty(e, "jasypt.encryptor.salt-generator-classname", "org.jasypt.salt.RandomSaltGenerator"));
        config.setIvGeneratorClassName(getProperty(e, "jasypt.encryptor.iv-generator-classname", "org.jasypt.iv.NoIvGenerator"));
        config.setStringOutputType(getProperty(e, "jasypt.encryptor.string-output-type", "base64"));
        encryptor.setConfig(config);
        return encryptor;
    }

    private static boolean isAsymmetricConfig(Environment e) {
        return propertyExists(e, "jasypt.encryptor.private-key-string") || propertyExists(e, "jasypt.encryptor.private-key-location");
    }

    private static boolean isPBEConfig(Environment e) {
        return propertyExists(e, "jasypt.encryptor.password");
    }

    private static String getProperty(Environment environment, String key, String defaultValue) {
        if (!propertyExists(environment, key)) {
            log.info("Encryptor config not found for property {}, using default value: {}", key, defaultValue);
        }
        return environment.getProperty(key, defaultValue);
    }

    private static boolean propertyExists(final Environment environment, final String key) {
        return environment.getProperty(key) != null;
    }

    private static String getRequiredProperty(final Environment environment, final String key) {
        if (!propertyExists(environment, key)) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return environment.getProperty(key);
    }

    @Override
    public String encrypt(final String message) {
        return singleton.get().encrypt(message);
    }

    @Override
    public String decrypt(final String encryptedMessage) {
        return singleton.get().decrypt(encryptedMessage);
    }

}
