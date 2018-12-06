package com.ulisesbocchio.jasyptspringboot.util;

import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class AsymmetricCryptography {

    private final ResourceLoader resourceLoader;

    public AsymmetricCryptography(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @SneakyThrows
    private byte[] getResourceBytes(Resource resource) {
        return FileCopyUtils.copyToByteArray(resource.getInputStream());
    }

    @SneakyThrows
    public PrivateKey getPrivateKey(String resourceLocation) {
        return getPrivateKey(resourceLoader.getResource(resourceLocation));
    }

    @SneakyThrows
    public PrivateKey getPrivateKey(Resource resource) {
        byte[] keyBytes = getResourceBytes(resource);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    @SneakyThrows
    public PublicKey getPublicKey(String resourceLocation) {
        return getPublicKey(resourceLoader.getResource(resourceLocation));
    }

    @SneakyThrows
    public PublicKey getPublicKey(Resource resource) {
        byte[] keyBytes = getResourceBytes(resource);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    @SneakyThrows
    public byte[] encrypt(byte[] msg, PublicKey key) {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(msg);
    }

    @SneakyThrows
    public byte[] decrypt(byte[] msg, PrivateKey key) {
        final Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(msg);
    }
}