package com.ulisesbocchio.jasyptspringboot.util;

import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * <p>AsymmetricCryptography class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class AsymmetricCryptography {

    private static final String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----";
    private static final String PUBLIC_KEY_HEADER = "-----BEGIN PUBLIC KEY-----";
    private static final String PRIVATE_KEY_FOOTER = "-----END PRIVATE KEY-----";
    private static final String PUBLIC_KEY_FOOTER = "-----END PUBLIC KEY-----";
    private final ResourceLoader resourceLoader;

    /**
     * <p>Constructor for AsymmetricCryptography.</p>
     *
     * @param resourceLoader a {@link org.springframework.core.io.ResourceLoader} object
     */
    public AsymmetricCryptography(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @SneakyThrows
    private byte[] getResourceBytes(Resource resource) {
        return FileCopyUtils.copyToByteArray(resource.getInputStream());
    }

    @SneakyThrows
    private byte[] decodePem(byte[] bytes, String... headers) {
        String pem = new String(bytes, StandardCharsets.UTF_8);
        for (String header : headers) {
            pem = pem.replace(header, "");
        }
        return Base64.getMimeDecoder().decode(pem);
    }

    /**
     * <p>getPrivateKey.</p>
     *
     * @param resourceLocation a {@link java.lang.String} object
     * @param format a {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat} object
     * @return a {@link java.security.PrivateKey} object
     */
    @SneakyThrows
    public PrivateKey getPrivateKey(String resourceLocation, KeyFormat format) {
        return getPrivateKey(resourceLoader.getResource(resourceLocation), format);
    }

    /**
     * <p>getPrivateKey.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object
     * @param format a {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat} object
     * @return a {@link java.security.PrivateKey} object
     */
    @SneakyThrows
    public PrivateKey getPrivateKey(Resource resource, KeyFormat format) {
        byte[] keyBytes = getResourceBytes(resource);
        if (format == KeyFormat.PEM) {
            keyBytes = decodePem(keyBytes, PRIVATE_KEY_HEADER, PRIVATE_KEY_FOOTER);
        }
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * <p>getPublicKey.</p>
     *
     * @param resourceLocation a {@link java.lang.String} object
     * @param format a {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat} object
     * @return a {@link java.security.PublicKey} object
     */
    @SneakyThrows
    public PublicKey getPublicKey(String resourceLocation, KeyFormat format) {
        return getPublicKey(resourceLoader.getResource(resourceLocation), format);
    }

    /**
     * <p>getPublicKey.</p>
     *
     * @param resource a {@link org.springframework.core.io.Resource} object
     * @param format a {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat} object
     * @return a {@link java.security.PublicKey} object
     */
    @SneakyThrows
    public PublicKey getPublicKey(Resource resource, KeyFormat format) {
        byte[] keyBytes = getResourceBytes(resource);
        if (format == KeyFormat.PEM) {
            keyBytes = decodePem(keyBytes, PUBLIC_KEY_HEADER, PUBLIC_KEY_FOOTER);
        }
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * <p>encrypt.</p>
     *
     * @param msg an array of {@link byte} objects
     * @param key a {@link java.security.PublicKey} object
     * @return an array of {@link byte} objects
     */
    @SneakyThrows
    public byte[] encrypt(byte[] msg, PublicKey key) {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(msg);
    }

    /**
     * <p>decrypt.</p>
     *
     * @param msg an array of {@link byte} objects
     * @param key a {@link java.security.PrivateKey} object
     * @return an array of {@link byte} objects
     */
    @SneakyThrows
    public byte[] decrypt(byte[] msg, PrivateKey key) {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(msg);
    }

    public enum KeyFormat {
        DER,
        PEM;
    }
}
