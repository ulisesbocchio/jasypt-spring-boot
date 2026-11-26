package com.ulisesbocchio.jasyptspringboot.resolver;

import static java.util.Objects.requireNonNull;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.exception.DecryptionException;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * <p>DefaultPropertyResolver class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class DefaultPropertyResolver implements EncryptablePropertyResolver {

    private final Environment environment;
    private final StringEncryptor encryptor;
    private final EncryptablePropertyDetector detector;
    private final ThreadLocal<Boolean> isResolving = ThreadLocal.withInitial(() -> false);

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
        this.encryptor = requireNonNull(encryptor, "encryptor can't be null");
        this.detector = requireNonNull(detector, "detector can't be null");
        this.environment = requireNonNull(environment, "environment can't be null");
    }

    /** {@inheritDoc} */
    @Override
    public String resolvePropertyValue(String value) {
        return Optional.ofNullable(value)
                .map(this::resolvePlaceholders)
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

    /**
     * Resolves placeholders only outside an active environment lookup.
     * <p>
     * Boot 4 may enumerate the system environment while resolving a placeholder.
     * Nested resolution would re-enter that enumeration indefinitely.
     */
    private String resolvePlaceholders(String value) {
        if (!value.contains("${") || isResolving.get()) {
            return value;
        }
        try {
            isResolving.set(true);
            return environment.resolvePlaceholders(value);
        } finally {
            isResolving.remove();
        }
    }
}
