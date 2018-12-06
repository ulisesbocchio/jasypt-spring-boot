package com.ulisesbocchio.jasyptspringboot.encryptor;

/**
 * {@link org.jasypt.encryption.StringEncryptor} version of {@link SimpleAsymmetricByteEncryptor} that just relies on
 * delegation from {@link ByteEncryptorStringEncryptorDelegate} and provides a constructor for {@link SimpleAsymmetricConfig}
 *
 * @author Ulises Bocchio
 */
public class SimpleAsymmetricStringEncryptor extends ByteEncryptorStringEncryptorDelegate {

    public SimpleAsymmetricStringEncryptor(SimpleAsymmetricByteEncryptor delegate) {
        super(delegate);
    }

    public SimpleAsymmetricStringEncryptor(SimpleAsymmetricConfig config) {
        super(new SimpleAsymmetricByteEncryptor(config));
    }
}
