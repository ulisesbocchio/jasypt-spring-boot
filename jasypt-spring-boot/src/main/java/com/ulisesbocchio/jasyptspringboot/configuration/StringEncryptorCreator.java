package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class StringEncryptorCreator {
    public static StringEncryptor create(JasyptEncryptorConfigurationProperties configProps) {
        return Optional.of(configProps)
                .filter(StringEncryptorCreator::isPBEConfig)
                .map(StringEncryptorCreator::createPBEDefault)
                .orElseGet(() ->
                        Optional.of(configProps)
                                .filter(StringEncryptorCreator::isAsymmetricConfig)
                                .map(StringEncryptorCreator::createAsymmetricDefault)
                                .orElseThrow(() -> new IllegalStateException("either 'jasypt.encryptor.password' or one of ['jasypt.encryptor.private-key-string', 'jasypt.encryptor.private-key-location'] must be provided for Password-based or Asymmetric encryption")));
    }

    private static boolean isPBEConfig(JasyptEncryptorConfigurationProperties config) {
        return config.getPassword() != null;
    }

    private static boolean isAsymmetricConfig(JasyptEncryptorConfigurationProperties config) {
        return config.getPrivateKeyString() != null || config.getPrivateKeyLocation() != null;
    }

    private static StringEncryptor createAsymmetricDefault(JasyptEncryptorConfigurationProperties configProps) {
        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();
        config.setPrivateKey(get(configProps::getPrivateKeyString, "jasypt.encryptor.private-key-string", null));
        config.setPrivateKeyLocation(get(configProps::getPrivateKeyLocation, "jasypt.encryptor.private-key-location", null));
        config.setPrivateKeyFormat(get(configProps::getPrivateKeyFormat, "jasypt.encryptor.private-key-format", AsymmetricCryptography.KeyFormat.DER));
        return new SimpleAsymmetricStringEncryptor(config);
    }

    private static StringEncryptor createPBEDefault(JasyptEncryptorConfigurationProperties configProps) {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(getRequired(configProps::getPassword, "jasypt.encryptor.password"));
        config.setAlgorithm(get(configProps::getAlgorithm, "jasypt.encryptor.algorithm", "PBEWITHHMACSHA512ANDAES_256"));
        config.setKeyObtentionIterations(get(configProps::getKeyObtentionIterations, "jasypt.encryptor.key-obtention-iterations", "1000"));
        config.setPoolSize(get(configProps::getPoolSize, "jasypt.encryptor.pool-size", "1"));
        config.setProviderName(get(configProps::getProviderName, "jasypt.encryptor.provider-name", null));
        config.setProviderClassName(get(configProps::getProviderClassName, "jasypt.encryptor.provider-class-name", null));
        config.setSaltGeneratorClassName(get(configProps::getSaltGeneratorClassname, "jasypt.encryptor.salt-generator-classname", "org.jasypt.salt.RandomSaltGenerator"));
        config.setIvGeneratorClassName(get(configProps::getIvGeneratorClassname, "jasypt.encryptor.iv-generator-classname", "org.jasypt.iv.RandomIvGenerator"));
        config.setStringOutputType(get(configProps::getStringOutputType, "jasypt.encryptor.string-output-type", "base64"));
        encryptor.setConfig(config);
        return encryptor;
    }

    private static <T> T getRequired(Supplier<T> supplier, String key) {
        T value = supplier.get();
        if (value == null) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return value;
    }

    private static <T> T get(Supplier<T> supplier, String key, T defaultValue) {
        T value = supplier.get();
        if (value == defaultValue) {
            log.info("Encryptor config not found for property {}, using default value: {}", key, value);
        }
        return value;
    }
}
