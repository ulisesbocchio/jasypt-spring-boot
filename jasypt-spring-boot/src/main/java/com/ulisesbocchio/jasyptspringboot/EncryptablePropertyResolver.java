package com.ulisesbocchio.jasyptspringboot;

/**
 * An interface to resolve property values that may be encrypted.
 *
 * @author Ulises Bocchio
 */
public interface EncryptablePropertyResolver {

    /**
     * Returns the unencrypted version of the value provided free on any prefixes/suffixes or any other metadata
     * surrounding the encrypted value. Or the actual same String if no encryption was detected.
     *
     * @param value the property value
     * @return either the same value if the value is not encrypted, or the decrypted version.
     */
    String resolvePropertyValue(String value);
}
