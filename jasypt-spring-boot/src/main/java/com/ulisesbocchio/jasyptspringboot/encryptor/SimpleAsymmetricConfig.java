package com.ulisesbocchio.jasyptspringboot.encryptor;

import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

/**
 * High level configuration class that provides a fallback mechanism to load private/public keys from three different
 * sources in the following order:
 * <p>
 * A Spring Resource
 * <p>
 * A String containing the public/private key
 * <p>
 * A String containing the resource location that contains the public/private key
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */

@Data
@NoArgsConstructor
public class SimpleAsymmetricConfig {

    private String privateKey = null;
    private String publicKey = null;
    private String privateKeyLocation = null;
    private String publicKeyLocation = null;
    private Resource privateKeyResource = null;
    private Resource publicKeyResource = null;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();
    private KeyFormat privateKeyFormat = KeyFormat.DER;
    private KeyFormat publicKeyFormat = KeyFormat.DER;

    private Resource loadResource(Resource asResource, String asString, String asLocation, KeyFormat format, String type) {
        return Optional.ofNullable(asResource)
                .orElseGet(() ->
                        Optional.ofNullable(asString)
                                .map(pk -> (Resource) new ByteArrayResource(format == KeyFormat.DER ? Base64.getDecoder().decode(pk) : pk.getBytes(StandardCharsets.UTF_8)))
                                .orElseGet(() ->
                                        Optional.ofNullable(asLocation)
                                                .map(resourceLoader::getResource)
                                                .orElseThrow(() -> new IllegalArgumentException("Unable to load " + type + " key. Either resource, key as string, or resource location must be provided"))));
    }

    /**
     * <p>loadPrivateKeyResource.</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object
     */
    public Resource loadPrivateKeyResource() {
        return loadResource(privateKeyResource, privateKey, privateKeyLocation, privateKeyFormat, "Private");
    }

    /**
     * <p>loadPublicKeyResource.</p>
     *
     * @return a {@link org.springframework.core.io.Resource} object
     */
    public Resource loadPublicKeyResource() {
        return loadResource(publicKeyResource, publicKey, publicKeyLocation, publicKeyFormat, "Public");
    }

    /**
     * <p>setKeyFormat.</p>
     *
     * @param keyFormat a {@link com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat} object
     */
    public void setKeyFormat(KeyFormat keyFormat) {
        setPublicKeyFormat(keyFormat);
        setPrivateKeyFormat(keyFormat);
    }

}
