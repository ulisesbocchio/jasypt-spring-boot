package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorBuilder;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.jasypt.encryption.StringEncryptor;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Reencrypts a file using an old configuration to decrypt and then a new configuration to encrypt.
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Slf4j
public abstract class AbstractReencryptMojo extends AbstractFileJasyptMojo {
    /** {@inheritDoc} */
    protected void run(final EncryptionService newService, final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws MojoExecutionException {
        String decryptedContents = decrypt(path, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);

        log.info("Re-encrypting file " + path);
        try {
            String encryptedContents = newService.encrypt(decryptedContents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
            FileService.write(path, encryptedContents);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Re-encrypting: " + e.getMessage(), e);
        }
    }

    private String decrypt(final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws MojoExecutionException {
        log.info("Decrypting file " + path);
        try {
            String contents = FileService.read(path);
            return getOldEncryptionService().decrypt(contents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Decrypting: " + e.getMessage(), e);
        }
    }

    private EncryptionService getOldEncryptionService() {
        JasyptEncryptorConfigurationProperties properties = new JasyptEncryptorConfigurationProperties();

        configure(properties);

        StringEncryptor encryptor = new StringEncryptorBuilder(properties, "jasypt.plugin.old").build();
        return new EncryptionService(encryptor);
    }

    /**
     * <p>configure.</p>
     *
     * @param properties a {@link com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties} object
     */
    protected abstract void configure(JasyptEncryptorConfigurationProperties properties);

    /**
     * <p>setIfNotNull.</p>
     *
     * @param setter a {@link java.util.function.Consumer} object
     * @param value a T object
     * @param <T> a T class
     */
    protected <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
