package com.ulisesbocchio.jasyptspringboot.resolver;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.exception.DecryptionException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.util.Optional;

/**
 * @author Ulises Bocchio
 */
public class DefaultPropertyResolver implements EncryptablePropertyResolver {

    private final Environment environment;
    private StringEncryptor encryptor;
    private EncryptablePropertyDetector detector;

    public DefaultPropertyResolver(StringEncryptor encryptor, Environment environment) {
        this(encryptor, new DefaultPropertyDetector(), environment);
    }

    public DefaultPropertyResolver(StringEncryptor encryptor, EncryptablePropertyDetector detector, Environment environment) {
        this.environment = environment;
        Assert.notNull(encryptor, "String encryptor can't be null");
        Assert.notNull(detector, "Encryptable Property detector can't be null");
        this.encryptor = encryptor;
        this.detector = detector;
    }

    @Override
    public String resolvePropertyValue(String value) {
        return Optional.ofNullable(value)
                .map(environment::resolveRequiredPlaceholders)
                .filter(detector::isEncrypted)
                .map(resolvedValue -> {
                    try {
                        String unwrappedProperty = detector.unwrapEncryptedValue(resolvedValue.trim());
                        String resolvedProperty = environment.resolveRequiredPlaceholders(unwrappedProperty);
                        return encryptor.decrypt(resolvedProperty);
                    } catch (EncryptionOperationNotPossibleException e) {
                        throw new DecryptionException("Unable to decrypt: " + value + ". Decryption of Properties failed,  make sure encryption/decryption " +
                                "passwords match", e);
                    }
                })
                .orElse(value);
    }
}
