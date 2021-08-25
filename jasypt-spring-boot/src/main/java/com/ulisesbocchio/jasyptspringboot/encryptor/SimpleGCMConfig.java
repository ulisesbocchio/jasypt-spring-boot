package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.salt.ZeroSaltGenerator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Data
@NoArgsConstructor
public class SimpleGCMConfig {
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private Resource secretKeyResource;
    private String secretKeyLocation;
    private String secretKey;
    private String secretKeyPassword;
    private String secretKeySalt;
    private String algorithm = "AES/GCM/NoPadding";
    private String secretKeyAlgorithm = "PBKDF2WithHmacSHA256";
    private int secretKeyIterations = 65536;
    private SecretKey actualKey = null;

    private Resource loadResource(Resource asResource, String asString, String asLocation) {
        return Optional.ofNullable(asResource)
                .orElseGet(() ->
                        Optional.ofNullable(asString)
                                .map(pk -> (Resource) new ByteArrayResource(pk.getBytes(StandardCharsets.UTF_8)))
                                .orElseGet(() ->
                                        Optional.ofNullable(asLocation)
                                                .map(resourceLoader::getResource)
                                                .orElseThrow(() -> new IllegalArgumentException("Unable to load secret key. Either resource, key as string, or resource location must be provided"))));
    }

    public Resource loadSecretKeyResource() {
        return loadResource(secretKeyResource, secretKey, secretKeyLocation);
    }

    public char[] getSecretKeyPasswordChars() {
        return secretKeyPassword.toCharArray();
    }

    public SaltGenerator getSecretKeySaltGenerator() {
        return secretKeySalt == null ? new ZeroSaltGenerator() : new FixedBase64ByteArraySaltGenerator(secretKeySalt);
    }
}
