package com.ulisesbocchio.jasyptspringboot;

/**
 * Interface that provides the contract to detect an unwrap encrypted properties. For instance, if encrypted properties
 * are to be prefixed with "ENC(" and suffixed with ")" then the implementation of {@link #isEncrypted(String)} would
 * return true when a property effectively enclosed in such prefix/suffix and {@link #unwrapEncryptedValue(String)}
 * would return the encrypted value, the portion of the property without the prefix and suffix.
 *
 * @author Ulises Bocchio
 */
public interface EncryptablePropertyDetector {

    /**
     * Returns whether a property is encrypted or not. Usually based on prefixes and suffixes.
     *
     * @param property the property value to check whether is encrypted or not.
     * @return true if the property is encrypted.
     */
    boolean isEncrypted(String property);

    /**
     * Returns the portion of the property that is actually the encrypted value without any extra metadata such as
     * prefixes and suffixes.
     *
     * @param property the property value to extract the encrypted value.
     * @return the encrypted portion of the property value.
     */
    String unwrapEncryptedValue(String property);
}
