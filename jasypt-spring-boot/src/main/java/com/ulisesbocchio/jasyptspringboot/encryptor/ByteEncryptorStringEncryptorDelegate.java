package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.SneakyThrows;
import org.jasypt.encryption.ByteEncryptor;
import org.jasypt.encryption.StringEncryptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * String Encryptor that delegates always to a {@link org.jasypt.encryption.ByteEncryptor} and converts results to/from Base64 for string
 * representation.
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class ByteEncryptorStringEncryptorDelegate implements StringEncryptor {
    private final ByteEncryptor delegate;

    /**
     * <p>Constructor for ByteEncryptorStringEncryptorDelegate.</p>
     *
     * @param delegate a {@link org.jasypt.encryption.ByteEncryptor} object
     */
    public ByteEncryptorStringEncryptorDelegate(ByteEncryptor delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    @SneakyThrows
    public String encrypt(String message) {
        return Base64.getEncoder().encodeToString(delegate.encrypt(message.getBytes(StandardCharsets.UTF_8)));
    }

    /** {@inheritDoc} */
    @Override
    @SneakyThrows
    public String decrypt(String encryptedMessage) {
        return new String(delegate.decrypt(Base64.getDecoder().decode(encryptedMessage)), StandardCharsets.UTF_8);
    }
}
