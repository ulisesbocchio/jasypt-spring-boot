package com.ulisesbocchio.jasyptmavenplugin.mojo;

import java.nio.file.Path;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goal which encrypts demarcated values in properties files.
 *
 * @author Rupert Madden-Abbott
 */
@Mojo(name = "encrypt", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class EncryptMojo extends AbstractJasyptMojo {
  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptMojo.class);

  @Override
  protected void run(final EncryptionService service, final Path path) throws
      MojoExecutionException {
    LOGGER.info("Encrypting file " + path);

    String contents = FileService.read(path);
    String encryptedContents = service.encrypt(contents);

    FileService.write(path, encryptedContents);
  }
}
