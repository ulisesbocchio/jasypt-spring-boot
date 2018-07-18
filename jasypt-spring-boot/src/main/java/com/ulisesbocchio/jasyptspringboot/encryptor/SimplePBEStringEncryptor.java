package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.SneakyThrows;
import org.jasypt.encryption.pbe.PBEByteEncryptor;
import org.jasypt.encryption.pbe.PBEStringEncryptor;

import java.util.Base64;

public class SimplePBEStringEncryptor implements PBEStringEncryptor {

    private final PBEByteEncryptor delegate;

    public SimplePBEStringEncryptor(PBEByteEncryptor delegate) {
        this.delegate = delegate;
    }

    @Override
    @SneakyThrows
    public String encrypt(String message) {
        return Base64.getEncoder().encodeToString(delegate.encrypt(message.getBytes("UTF-8")));
    }

    @Override
    @SneakyThrows
    public String decrypt(String encryptedMessage) {
        return new String(delegate.decrypt(Base64.getDecoder().decode(encryptedMessage)), "UTF-8");
    }

    @Override
    @SneakyThrows
    public void setPassword(String password) {
        throw new IllegalAccessException("Not Implemented, use delegate");
    }
}
