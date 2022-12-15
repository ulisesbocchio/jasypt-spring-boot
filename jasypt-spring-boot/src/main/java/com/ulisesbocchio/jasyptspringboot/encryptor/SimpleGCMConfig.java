package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jasypt.iv.IvGenerator;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.salt.ZeroSaltGenerator;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * <p>SimpleGCMConfig class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
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
    private int secretKeyIterations = 1000;
    private SecretKey actualKey = null;
    private SaltGenerator saltGenerator = null;
    private IvGenerator ivGenerator = null;
    private String ivGeneratorClassName = "org.jasypt.iv.RandomIvGenerator";

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

    /**
     * <p>loadSecretKeyResource.</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object
     */
    public Resource loadSecretKeyResource() {
        return loadResource(secretKeyResource, secretKey, secretKeyLocation);
    }

    /**
     * <p>getSecretKeyPasswordChars.</p>
     *
     * @return an array of {@link char} objects
     */
    public char[] getSecretKeyPasswordChars() {
        return secretKeyPassword.toCharArray();
    }

    /**
     * <p>getSecretKeySaltGenerator.</p>
     *
     * @return a {@link org.jasypt.salt.SaltGenerator} object
     */
    public SaltGenerator getSecretKeySaltGenerator() {
        return saltGenerator != null ?
                saltGenerator :
                (secretKeySalt == null ?
                        new ZeroSaltGenerator() :
                        new FixedBase64ByteArraySaltGenerator(secretKeySalt));
    }

    @SneakyThrows
    private IvGenerator instantiateIvGenerator() {
        return (IvGenerator)Class.forName(this.ivGeneratorClassName).newInstance();
    }

    /**
     * <p>getActualIvGenerator.</p>
     *
     * @return a {@link org.jasypt.iv.IvGenerator} object
     */
    public IvGenerator getActualIvGenerator() {
        return Optional.ofNullable(ivGenerator).orElseGet(this::instantiateIvGenerator);
    }
}
