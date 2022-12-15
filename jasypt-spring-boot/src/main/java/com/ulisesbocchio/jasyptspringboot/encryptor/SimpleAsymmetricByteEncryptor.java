package com.ulisesbocchio.jasyptspringboot.encryptor;

import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import org.jasypt.encryption.ByteEncryptor;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Vanilla implementation of an asymmetric encryptor that relies on {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography}
 * Keys are lazily loaded from {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig}
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class SimpleAsymmetricByteEncryptor implements ByteEncryptor {

    private final AsymmetricCryptography crypto;
    private final Singleton<PublicKey> publicKey;
    private final Singleton<PrivateKey> privateKey;

    /**
     * <p>Constructor for SimpleAsymmetricByteEncryptor.</p>
     *
     * @param config a {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig} object
     */
    public SimpleAsymmetricByteEncryptor(SimpleAsymmetricConfig config) {
        crypto = new AsymmetricCryptography(config.getResourceLoader());
        privateKey = Singleton.fromLazy(crypto::getPrivateKey, config::loadPrivateKeyResource, config::getPrivateKeyFormat);
        publicKey = Singleton.fromLazy(crypto::getPublicKey, config::loadPublicKeyResource, config::getPublicKeyFormat);
    }

    /** {@inheritDoc} */
    @Override
    public byte[] encrypt(byte[] message) {
        return this.crypto.encrypt(message, publicKey.get());
    }

    /** {@inheritDoc} */
    @Override
    public byte[] decrypt(byte[] encryptedMessage) {
        return this.crypto.decrypt(encryptedMessage, privateKey.get());
    }
}
