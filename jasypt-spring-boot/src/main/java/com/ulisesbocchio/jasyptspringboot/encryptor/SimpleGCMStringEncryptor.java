package com.ulisesbocchio.jasyptspringboot.encryptor;

/**
 * {@link org.jasypt.encryption.StringEncryptor} version of {@link SimpleAsymmetricByteEncryptor} that just relies on
 * delegation from {@link ByteEncryptorStringEncryptorDelegate} and provides a constructor for {@link SimpleAsymmetricConfig}
 *
 * @author Ulises Bocchio
 */
public class SimpleGCMStringEncryptor extends ByteEncryptorStringEncryptorDelegate {

    public SimpleGCMStringEncryptor(SimpleAsymmetricByteEncryptor delegate) {
        super(delegate);
    }

    public SimpleGCMStringEncryptor(SimpleGCMConfig config) {
        super(new SimpleGCMByteEncryptor(config));
    }
}
