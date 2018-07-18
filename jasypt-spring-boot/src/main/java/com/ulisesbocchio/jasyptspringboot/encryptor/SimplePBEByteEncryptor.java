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

public class SimplePBEByteEncryptor implements PBEByteEncryptor {

    private String password = null;
    private SaltGenerator saltGenerator = null;
    private int iterations;
    private String algorithm = null;

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

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    public void setSaltGenerator(SaltGenerator saltGenerator) {
        this.saltGenerator = saltGenerator;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
}
