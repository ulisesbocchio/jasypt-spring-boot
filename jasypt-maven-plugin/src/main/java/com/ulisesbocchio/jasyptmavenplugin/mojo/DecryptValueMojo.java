package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Goal which decrypts values.
 *
 * @author ubocchio
 */
@Mojo(name = "decrypt-value", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, requiresProject = false)
@Slf4j
public class DecryptValueMojo extends AbstractValueJasyptMojo {

    @Override
    protected void run(final EncryptionService service, final String value, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws
            MojoExecutionException {
        try {
            String actualValue = value.startsWith(encryptPrefix) ? value.substring(encryptPrefix.length(), value.length() - encryptSuffix.length()) : value;
            log.info("Decrypting value " + actualValue);
            String decryptedValue = service.decryptValue(actualValue);
            log.info("\n" + decryptedValue);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Decrypting: " + e.getMessage(), e);
        }
    }
}
