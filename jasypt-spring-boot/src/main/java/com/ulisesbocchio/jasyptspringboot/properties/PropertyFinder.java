package com.ulisesbocchio.jasyptspringboot.properties;

import org.jasypt.encryption.StringEncryptor;

public interface PropertyFinder {
    /**
     * Does this environment value represent an encrypted value that we are able to decrypt>
     * @param stringValue Value from the environment
     * @return true if recognised as a value that we can {@link PropertyFinder#decrypt(String, StringEncryptor)}
     */
    boolean isEncryptedValue(String stringValue);

    /**
     * Decrypt encodedValue using provided encryptor.
     *
     * @param encodedValue The entire value from the environment, including the ENC() wrapper, or whatever wrapper satisfies the {@link PropertyFinder#isEncryptedValue(String)} condition for this implementation.
     * @param encryptor The configured StringEncryptor
     * @return Decrypted value, or implementation-specific value or exception on null or decryption failure
     */
    String decrypt(final String encodedValue, final StringEncryptor encryptor);
}
