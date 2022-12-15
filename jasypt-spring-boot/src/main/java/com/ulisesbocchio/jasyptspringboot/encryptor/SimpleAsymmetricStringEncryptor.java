package com.ulisesbocchio.jasyptspringboot.encryptor;

/**
 * {@link org.jasypt.encryption.StringEncryptor} version of {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricByteEncryptor} that just relies on
 * delegation from {@link com.ulisesbocchio.jasyptspringboot.encryptor.ByteEncryptorStringEncryptorDelegate} and provides a constructor for {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig}
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class SimpleAsymmetricStringEncryptor extends ByteEncryptorStringEncryptorDelegate {

    /**
     * <p>Constructor for SimpleAsymmetricStringEncryptor.</p>
     *
     * @param delegate a {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricByteEncryptor} object
     */
    public SimpleAsymmetricStringEncryptor(SimpleAsymmetricByteEncryptor delegate) {
        super(delegate);
    }

    /**
     * <p>Constructor for SimpleAsymmetricStringEncryptor.</p>
     *
     * @param config a {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig} object
     */
    public SimpleAsymmetricStringEncryptor(SimpleAsymmetricConfig config) {
        super(new SimpleAsymmetricByteEncryptor(config));
    }
}
