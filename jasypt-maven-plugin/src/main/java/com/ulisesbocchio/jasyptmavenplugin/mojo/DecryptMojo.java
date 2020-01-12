package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.nio.file.Path;

/**
 * Goal which decrypts demarcated values in properties files.
 *
 * @author Rupert Madden-Abbott
 */
@Mojo(name = "decrypt", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
@Slf4j
public class DecryptMojo extends AbstractFileJasyptMojo {

    @Override
    protected void run(final EncryptionService service, final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws
            MojoExecutionException {
        log.info("Decrypting file " + path);
        try {
            String contents = FileService.read(path);
            String decryptedContents = service.decrypt(contents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
            log.info("\n" + decryptedContents);
        } catch (Exception e) {
            throw new MojoExecutionException("Error Decrypting: " + e.getMessage(), e);
        }
    }
}
