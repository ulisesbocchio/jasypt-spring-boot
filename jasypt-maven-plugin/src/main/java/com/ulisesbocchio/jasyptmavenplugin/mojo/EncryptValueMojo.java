package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which encrypts values.
 *
 * @author ubocchio
 */
@Mojo(name = "encrypt-value", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Slf4j
public class EncryptValueMojo extends AbstractValueJasyptMojo {

    @Override
    protected void run(final EncryptionService service, final String value, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws
            MojoExecutionException {
        log.info("Encrypting value " + value);
        try {
            String encryptedValue = encryptPrefix + service.encryptValue(value) + encryptSuffix;
            log.info("\n" + encryptedValue);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Encrypting: " + e.getMessage(), e);
        }
    }
}
