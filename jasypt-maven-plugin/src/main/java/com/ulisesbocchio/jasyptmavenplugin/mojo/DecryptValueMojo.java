package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which decrypts demarcated values in properties files.
 *
 * @author ubocchio
 */
@Mojo(name = "decrypt-value", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Slf4j
public class DecryptValueMojo extends AbstractValueJasyptMojo {

    @Override
    protected void run(final EncryptionService service, final String value, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws
            MojoExecutionException {
        log.info("Decrypting value " + value);
        try {
            String decryptedValue = service.decryptValue(value);
            log.info("\n" + decryptedValue);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Decrypting: " + e.getMessage(), e);
        }
    }
}
