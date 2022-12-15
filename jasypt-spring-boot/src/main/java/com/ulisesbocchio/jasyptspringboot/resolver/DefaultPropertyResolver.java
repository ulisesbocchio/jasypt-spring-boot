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
 * <p>DefaultPropertyResolver class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class DefaultPropertyResolver implements EncryptablePropertyResolver {

    private final Environment environment;
    private StringEncryptor encryptor;
    private EncryptablePropertyDetector detector;

    /**
     * <p>Constructor for DefaultPropertyResolver.</p>
     *
     * @param encryptor a {@link org.jasypt.encryption.StringEncryptor} object
     * @param environment a {@link org.springframework.core.env.Environment} object
     */
    public DefaultPropertyResolver(StringEncryptor encryptor, Environment environment) {
        this(encryptor, new DefaultPropertyDetector(), environment);
    }

    /**
     * <p>Constructor for DefaultPropertyResolver.</p>
     *
     * @param encryptor a {@link org.jasypt.encryption.StringEncryptor} object
     * @param detector a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector} object
     * @param environment a {@link org.springframework.core.env.Environment} object
     */
    public DefaultPropertyResolver(StringEncryptor encryptor, EncryptablePropertyDetector detector, Environment environment) {
        this.environment = environment;
        Assert.notNull(encryptor, "String encryptor can't be null");
        Assert.notNull(detector, "Encryptable Property detector can't be null");
        this.encryptor = encryptor;
        this.detector = detector;
    }

    /** {@inheritDoc} */
    @Override
    public String resolvePropertyValue(String value) {
        return Optional.ofNullable(value)
                .map(environment::resolvePlaceholders)
                .filter(detector::isEncrypted)
                .map(resolvedValue -> {
                    try {
                        String unwrappedProperty = detector.unwrapEncryptedValue(resolvedValue.trim());
                        String resolvedProperty = environment.resolvePlaceholders(unwrappedProperty);
                        return encryptor.decrypt(resolvedProperty);
                    } catch (EncryptionOperationNotPossibleException e) {
                        throw new DecryptionException("Unable to decrypt property: " + value + " resolved to: " + resolvedValue + ". Decryption of Properties failed,  make sure encryption/decryption " +
                                "passwords match", e);
                    }
                })
                .orElse(value);
    }
}
