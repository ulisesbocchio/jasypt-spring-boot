package com.ulisesbocchio.jasyptspringboot;

import org.springframework.core.env.PropertySource;

/**
 * Interface that provides the contract for what property sources and/or properties should be inspected for encryption.
 * This allows a decision point before {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} is invoked, and thereby can also be used to
 * avoid scenarios where inspection is unwanted due to initialization conflicts, such as circular dependencies.
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public interface EncryptablePropertyFilter {
    /**
     * Given a property source and a property name, returns true if the property should be analyzed for decryption.
     *
     * @param source The property source, useful to enabled/disable encryption for specific property sources.
     * @param name   The actual property being requested, useful to enable/disable encryption for specific properties/patterns.
     * @return true if the property should be considered for decryption.
     */
    boolean shouldInclude(PropertySource<?> source, String name);
}
