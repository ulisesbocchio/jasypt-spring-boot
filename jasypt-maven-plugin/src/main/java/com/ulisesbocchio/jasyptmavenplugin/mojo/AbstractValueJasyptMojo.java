package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Abstract Mojo for value goals
 *
 * @author ubocchio
 */
@Slf4j
public abstract class AbstractValueJasyptMojo extends AbstractJasyptMojo {

    /**
     * The decrypted property suffix
     */
    @Parameter(property = "jasypt.plugin.value")
    private String value = null;

    @Override
    void run(EncryptionService encryptionService, ConfigurableApplicationContext context, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix)
            throws MojoExecutionException {
        if (value == null) {
            throw new MojoExecutionException("No jasypt.plugin.value property provided");
        }
        run(encryptionService, value, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
    }

    /**
     * Run the encryption task.
     *
     * @param encryptionService the service for encryption
     * @param value             the value to operate on
     * @param encryptPrefix     encryption prefix
     * @param encryptSuffix     encryption suffix
     * @param decryptPrefix     decryption prefix
     * @param decryptSuffix     decryption suffix
     */
    abstract void run(EncryptionService encryptionService, String value, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix)
            throws MojoExecutionException;
}
