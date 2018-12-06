package com.ulisesbocchio.jasyptspringboot.encryptor;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * High level configuration class that provides a fallback mechanism to load private/public keys from three different
 * sources in the following order:
 *
 * A Spring Resource
 *
 * A String containing the public/private key
 *
 * A String containing the resource location that contains the public/private key
 *
 * @author Ulises Bocchio
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

    private Resource loadResource(Resource asResource, String asString, String asLocation) {
        return Optional.of(asResource)
                .orElseGet(() ->
                        Optional.of(asString)
                                .map(pk -> (Resource) new ByteArrayResource(pk.getBytes(StandardCharsets.UTF_8)))
                                .orElseGet(() ->
                                        Optional.of(asLocation)
                                                .map(resourceLoader::getResource)
                                                .orElseThrow(() -> new IllegalArgumentException("Unable to load key. Either resource, key as string, or resource location must be provided"))));
    }

    public Resource loadPrivateKeyResource() {
        return loadResource(privateKeyResource, privateKey, privateKeyLocation);
    }

    public Resource loadPublicKeyResource() {
        return loadResource(publicKeyResource, publicKey, publicKeyLocation);
    }

}
