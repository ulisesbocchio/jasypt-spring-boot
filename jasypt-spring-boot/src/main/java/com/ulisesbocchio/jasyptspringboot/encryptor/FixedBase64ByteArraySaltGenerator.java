package com.ulisesbocchio.jasyptspringboot.encryptor;

import org.jasypt.salt.ByteArrayFixedSaltGenerator;

import java.util.Base64;

/**
 * <p>FixedBase64ByteArraySaltGenerator class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class FixedBase64ByteArraySaltGenerator  extends ByteArrayFixedSaltGenerator {
    /**
     * <p>Constructor for FixedBase64ByteArraySaltGenerator.</p>
     *
     * @param base64Salt a {@link java.lang.String} object
     */
    public FixedBase64ByteArraySaltGenerator(String base64Salt) {
        super(Base64.getDecoder().decode(base64Salt));
    }
}
