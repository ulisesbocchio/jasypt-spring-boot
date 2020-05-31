package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorBuilder;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.jasypt.encryption.StringEncryptor;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Reencrypts a file using an old configuration to decrypt and then a new configuration to encrypt.
 */
@Mojo(name = "reencrypt", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Slf4j
public class ReencryptMojo extends AbstractFileJasyptMojo {
    @Parameter(property = "jasypt.plugin.old.password") private String oldPassword;
    @Parameter(property = "jasypt.plugin.old.algorithm") private String oldAlgorithm;
    @Parameter(property = "jasypt.plugin.old.key-obtention-iterations") private String oldKeyObtentionIterations;
    @Parameter(property = "jasypt.plugin.old.pool-size") private String oldPoolSize;
    @Parameter(property = "jasypt.plugin.old.provider-name") private String oldProviderName;
    @Parameter(property = "jasypt.plugin.old.provider-class-name") private String oldProviderClassName;
    @Parameter(property = "jasypt.plugin.old.salt-generator-class-name") private String oldSaltGeneratorClassName;
    @Parameter(property = "jasypt.plugin.old.iv-generator-class-name") private String oldIvGeneratorClassName;
    @Parameter(property = "jasypt.plugin.old.string-output-type") private String oldStringOutputType;
    @Parameter(property = "jasypt.plugin.old.private-key-string") private String oldPrivateKeyString;
    @Parameter(property = "jasypt.plugin.old.private-key-location") private String oldPrivateKeyLocation;
    @Parameter(property = "jasypt.plugin.old.private-key-format") private AsymmetricCryptography.KeyFormat oldPrivateKeyFormat;

    protected void run(final EncryptionService newService, final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws MojoExecutionException {
        String decryptedContents = decrypt(newService, path, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);

        log.info("Re-encrypting file " + path);
        try {
            String encryptedContents = newService.encrypt(decryptedContents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
            FileService.write(path, encryptedContents);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Encrypting: " + e.getMessage(), e);
        }

    }

    private String decrypt(final EncryptionService service, final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws MojoExecutionException {
        log.info("Decrypting file " + path);
        try {
            String contents = FileService.read(path);
            return service.decrypt(contents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Decrypting: " + e.getMessage(), e);
        }
    }

    private EncryptionService getOldEncryptionService(EncryptionService newEncryptionService) {
        JasyptEncryptorConfigurationProperties properties = new JasyptEncryptorConfigurationProperties();

        setIfNotNull(properties::setPassword, oldPassword);
        setIfNotNull(properties::setAlgorithm, oldAlgorithm);
        setIfNotNull(properties::setKeyObtentionIterations, oldKeyObtentionIterations);
        setIfNotNull(properties::setPoolSize, oldPoolSize);
        setIfNotNull(properties::setProviderName, oldProviderName);
        setIfNotNull(properties::setProviderClassName, oldProviderClassName);
        setIfNotNull(properties::setSaltGeneratorClassname, oldSaltGeneratorClassName);
        setIfNotNull(properties::setIvGeneratorClassname, oldIvGeneratorClassName);
        setIfNotNull(properties::setStringOutputType, oldStringOutputType);
        setIfNotNull(properties::setPrivateKeyString, oldPrivateKeyString);
        setIfNotNull(properties::setPrivateKeyLocation, oldPrivateKeyLocation);
        setIfNotNull(properties::setPrivateKeyFormat, oldPrivateKeyFormat);

        StringEncryptor encryptor = new StringEncryptorBuilder(properties, "jasypt.plugin.old").build();
        return new EncryptionService(encryptor);
    }

    private <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
