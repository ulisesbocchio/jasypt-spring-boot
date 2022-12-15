package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/**
 * Goal which encrypts demarcated values in properties files.
 *
 * @author Rupert Madden-Abbott
 * @version $Id: $Id
 */
@Mojo(name = "encrypt", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class EncryptMojo extends AbstractFileJasyptMojo {
    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptMojo.class);

    /** {@inheritDoc} */
    @Override
    protected void run(final EncryptionService service, final Path path, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) throws
            MojoExecutionException {
        LOGGER.info("Encrypting file " + path);
        try {
            String contents = FileService.read(path);
            String encryptedContents = service.encrypt(contents, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
            FileService.write(path, encryptedContents);

        } catch (Exception e) {
            throw new MojoExecutionException("Error Encrypting: " + e.getMessage(), e);
        }

    }
}
