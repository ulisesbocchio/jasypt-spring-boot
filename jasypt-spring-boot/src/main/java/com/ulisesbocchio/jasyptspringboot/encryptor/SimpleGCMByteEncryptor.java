package com.ulisesbocchio.jasyptspringboot.encryptor;

import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.SneakyThrows;
import org.jasypt.encryption.ByteEncryptor;
import org.jasypt.iv.IvGenerator;
import org.jasypt.salt.SaltGenerator;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SimpleGCMByteEncryptor implements ByteEncryptor {

    public static final int AES_KEY_SIZE = 256;
    public static final int AES_KEY_PASSWORD_SALT_LENGTH = 16;
    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 128;
    private final Singleton<SecretKey> key;
    private final String algorithm;
    private final Singleton<IvGenerator> ivGenerator;

    @SneakyThrows
    @Override
    public byte[] encrypt(byte[] message) {
        byte[] iv = this.ivGenerator.get().generateIv(GCM_IV_LENGTH);

        Cipher cipher = Cipher.getInstance(this.algorithm);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key.get(), gcmParameterSpec);

        byte[] cipherText = cipher.doFinal(message);

        ByteBuffer byteBuffer = ByteBuffer.allocate(GCM_IV_LENGTH + cipherText.length);
        byteBuffer.put(iv);
        byteBuffer.put(cipherText);
        return byteBuffer.array();
    }

    @SneakyThrows
    @Override
    public byte[] decrypt(byte[] encryptedMessage) {
        Cipher cipher = Cipher.getInstance(this.algorithm);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, encryptedMessage, 0, GCM_IV_LENGTH);
        cipher.init(Cipher.DECRYPT_MODE, key.get(), gcmParameterSpec);
        return cipher.doFinal(encryptedMessage, GCM_IV_LENGTH, encryptedMessage.length - GCM_IV_LENGTH);
    }

    @SneakyThrows
    private SecretKey loadSecretKey(SimpleGCMConfig config) {
        if (config.getActualKey() != null) {
            return config.getActualKey();
        } else if (config.getSecretKeyPassword() != null) {
            Assert.notNull(config.getSecretKeySaltGenerator(), "Secret key Salt must be provided with password");
            Assert.notNull(config.getSecretKeyAlgorithm(), "Secret key algorithm must be provided with password");
            return getAESKeyFromPassword(config.getSecretKeyPasswordChars(),  config.getSecretKeySaltGenerator(), config.getSecretKeyIterations(), config.getSecretKeyAlgorithm());
        } else {
            Assert.state(config.getSecretKey() != null || config.getSecretKeyResource() != null || config.getSecretKeyLocation() != null, "No key provided");
            return loadSecretKeyFromResource(config.loadSecretKeyResource());
        }
    }

    @SneakyThrows
    private byte[] getResourceBytes(Resource resource) {
        return FileCopyUtils.copyToByteArray(resource.getInputStream());
    }

    @SneakyThrows
    private SecretKey loadSecretKeyFromResource(Resource resource) {
        byte[] secretKeyBytes = Base64.getDecoder().decode(getResourceBytes(resource));
        return new SecretKeySpec(secretKeyBytes, "AES");
    }

    @SneakyThrows
    public static SecretKey generateSecretKey() {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        SecureRandom random = SecureRandom.getInstanceStrong();
        keyGenerator.init(AES_KEY_SIZE, random);
        return keyGenerator.generateKey();
    }

    @SneakyThrows
    public static String generateBase64EncodedSecretKey() {
        SecretKey key = generateSecretKey();
        byte[] secretKeyBytes = key.getEncoded();
        return Base64.getEncoder().encodeToString(secretKeyBytes);
    }

    @SneakyThrows
    public static SecretKey getAESKeyFromPassword(char[] password, SaltGenerator saltGenerator, int iterations, String algorithm){
        SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
        KeySpec spec = new PBEKeySpec(password, saltGenerator.generateSalt(AES_KEY_PASSWORD_SALT_LENGTH), iterations, AES_KEY_SIZE);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
    }

    public SimpleGCMByteEncryptor(SimpleGCMConfig config) {
        this.key = Singleton.from(this::loadSecretKey, config);
        this.ivGenerator = Singleton.from(config::getActualIvGenerator);
        this.algorithm = config.getAlgorithm();
    }
}
