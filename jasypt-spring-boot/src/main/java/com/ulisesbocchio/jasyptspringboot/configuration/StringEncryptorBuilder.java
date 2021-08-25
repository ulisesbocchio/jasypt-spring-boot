package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMStringEncryptor;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.util.function.Supplier;

@Slf4j
public class StringEncryptorBuilder {
    private final JasyptEncryptorConfigurationProperties configProps;

    private final String propertyPrefix;

    public StringEncryptorBuilder(JasyptEncryptorConfigurationProperties configProps, String propertyPrefix) {
        this.configProps = configProps;
        this.propertyPrefix = propertyPrefix;
    }

    public StringEncryptor build() {
        if (isPBEConfig()) {
            return createPBEDefault();
        } else if (isAsymmetricConfig()) {
            return createAsymmetricDefault();
        } else if (isGCMConfig()) {
            return createGCMDefault();
        } else {
            throw new IllegalStateException("either '" + propertyPrefix + ".password' or one of ['" + propertyPrefix + ".private-key-string', '" + propertyPrefix + ".private-key-location'] must be provided for Password-based or Asymmetric encryption");
        }
    }

    private boolean isGCMConfig() {
        return configProps.getAlgorithm().matches(".*AES/GCM.*") || configProps.getGcmSecretKey() != null;
    }

    private boolean isPBEConfig() {
        return configProps.getPassword() != null;
    }

    private boolean isAsymmetricConfig() {
        return configProps.getPrivateKeyString() != null || configProps.getPrivateKeyLocation() != null || configProps.getPublicKeyString() != null || configProps.getPublicKeyLocation() != null;
    }

    private StringEncryptor createGCMDefault() {
        SimpleGCMConfig config = new SimpleGCMConfig();
        config.setAlgorithm(get(configProps::getAlgorithm, propertyPrefix + ".algorithm", "AES/GCM/NoPadding"));
        config.setSecretKey(get(configProps::getGcmSecretKey, propertyPrefix + ".gcm-secret-key", null));
        config.setSecretKeyLocation(get(configProps::getGcmSecretKeyLocation, propertyPrefix + ".gcm-secret-key-location", null));
        config.setSecretKeyPassword(get(configProps::getPassword, propertyPrefix + ".password", null));
        config.setSecretKeySalt(get(configProps::getGcmSecretKeySalt, propertyPrefix + ".gcm-secret-key-salt", null));
        config.setSecretKeyAlgorithm(get(configProps::getGcmSecretKeyAlgorithm, propertyPrefix + ".gcm-secret-key-algorithm", "PBKDF2WithHmacSHA256"));
        config.setSecretKeyIterations(get(configProps::getKeyObtentionIterationsInt, propertyPrefix + ".key-obtention-iterations", 65536));
        return new SimpleGCMStringEncryptor(config);
    }

    private StringEncryptor createAsymmetricDefault() {
        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();
        config.setPrivateKey(get(configProps::getPrivateKeyString, propertyPrefix + ".private-key-string", null));
        config.setPrivateKeyLocation(get(configProps::getPrivateKeyLocation, propertyPrefix + ".private-key-location", null));
        config.setPrivateKeyFormat(get(configProps::getPrivateKeyFormat, propertyPrefix + ".private-key-format", AsymmetricCryptography.KeyFormat.DER));
        config.setPublicKey(get(configProps::getPublicKeyString, propertyPrefix + ".public-key-string", null));
        config.setPublicKeyLocation(get(configProps::getPublicKeyLocation, propertyPrefix + ".public-key-location", null));
        config.setPublicKeyFormat(get(configProps::getPublicKeyFormat, propertyPrefix + ".public-key-format", AsymmetricCryptography.KeyFormat.DER));
        return new SimpleAsymmetricStringEncryptor(config);
    }

    private StringEncryptor createPBEDefault() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getRequired(configProps::getPassword, propertyPrefix + ".password"));
        config.setAlgorithm(get(configProps::getAlgorithm, propertyPrefix + ".algorithm", "PBEWITHHMACSHA512ANDAES_256"));
        config.setKeyObtentionIterations(get(configProps::getKeyObtentionIterations, propertyPrefix + ".key-obtention-iterations", "1000"));
        config.setPoolSize(get(configProps::getPoolSize, propertyPrefix + ".pool-size", "1"));
        config.setProviderName(get(configProps::getProviderName, propertyPrefix + ".provider-name", null));
        config.setProviderClassName(get(configProps::getProviderClassName, propertyPrefix + ".provider-class-name", null));
        config.setSaltGeneratorClassName(get(configProps::getSaltGeneratorClassname, propertyPrefix + ".salt-generator-classname", "org.jasypt.salt.RandomSaltGenerator"));
        config.setIvGeneratorClassName(get(configProps::getIvGeneratorClassname, propertyPrefix + ".iv-generator-classname", "org.jasypt.iv.RandomIvGenerator"));
        config.setStringOutputType(get(configProps::getStringOutputType, propertyPrefix + ".string-output-type", "base64"));
        encryptor.setConfig(config);
        return encryptor;
    }

    private <T> T getRequired(Supplier<T> supplier, String key) {
        T value = supplier.get();
        if (value == null) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return value;
    }

    private <T> T get(Supplier<T> supplier, String key, T defaultValue) {
        T value = supplier.get();
        if (value == defaultValue) {
            log.info("Encryptor config not found for property {}, using default value: {}", key, value);
        }
        return value;
    }
}
