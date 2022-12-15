package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.SneakyThrows;
import org.jasypt.encryption.pbe.PBEByteEncryptor;
import org.jasypt.salt.SaltGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;

/**
 * <p>SimplePBEByteEncryptor class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class SimplePBEByteEncryptor implements PBEByteEncryptor {

    private String password = null;
    private SaltGenerator saltGenerator = null;
    private int iterations;
    private String algorithm = null;

    /** {@inheritDoc} */
    @Override
    @SneakyThrows
    public byte[] encrypt(byte[] message) {
        // create Key
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        byte[] salt = saltGenerator.generateSalt(8);
        final PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, iterations);
        SecretKey key = factory.generateSecret(keySpec);

        // Build cipher.
        final Cipher cipherEncrypt = Cipher.getInstance(algorithm);
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, key);

        // Save parameters
        byte[] params = cipherEncrypt.getParameters().getEncoded();

        // Encrypted message
        byte[] encryptedMessage = cipherEncrypt.doFinal(message);

        return ByteBuffer
                .allocate(1 + params.length + encryptedMessage.length)
                .put((byte) params.length)
                .put(params)
                .put(encryptedMessage)
                .array();
    }

    /** {@inheritDoc} */
    @Override
    @SneakyThrows
    public byte[] decrypt(byte[] encryptedMessage) {
        int paramsLength = Byte.toUnsignedInt(encryptedMessage[0]);
        int messageLength = encryptedMessage.length - paramsLength - 1;
        byte[] params = new byte[paramsLength];
        byte[] message = new byte[messageLength];
        System.arraycopy(encryptedMessage, 1, params, 0, paramsLength);
        System.arraycopy(encryptedMessage, paramsLength + 1, message, 0, messageLength);

        // create Key
        final SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        final PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKey key = factory.generateSecret(keySpec);

        // Build parameters
        AlgorithmParameters algorithmParameters = AlgorithmParameters.getInstance(algorithm);
        algorithmParameters.init(params);

        // Build Cipher
        final Cipher cipherDecrypt = Cipher.getInstance(algorithm);
        cipherDecrypt.init(
                Cipher.DECRYPT_MODE,
                key,
                algorithmParameters
        );


        return cipherDecrypt.doFinal(message);
    }

    /** {@inheritDoc} */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * <p>Setter for the field <code>saltGenerator</code>.</p>
     *
     * @param saltGenerator a {@link org.jasypt.salt.SaltGenerator} object
     */
    public void setSaltGenerator(SaltGenerator saltGenerator) {
        this.saltGenerator = saltGenerator;
    }

    /**
     * <p>Setter for the field <code>iterations</code>.</p>
     *
     * @param iterations a int
     */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    /**
     * <p>Setter for the field <code>algorithm</code>.</p>
     *
     * @param algorithm a {@link java.lang.String} object
     */
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
