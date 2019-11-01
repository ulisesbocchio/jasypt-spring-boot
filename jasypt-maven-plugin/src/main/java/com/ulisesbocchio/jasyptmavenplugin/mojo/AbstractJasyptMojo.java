package com.ulisesbocchio.jasyptmavenplugin.mojo;

import java.io.IOException;
import java.nio.file.Path;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import static org.codehaus.plexus.util.FileUtils.getExtension;
import static org.codehaus.plexus.util.FileUtils.removeExtension;

/**
 * A mojo that spins up a Spring Boot application for any encrypt/decrypt function.
 */
public abstract class AbstractJasyptMojo extends AbstractMojo {
  /**
   * The path of the file to operate on.
   */
  @Parameter(property = "jasypt.plugin.path",
      defaultValue = "file:src/main/resources/application.properties")
  private String path = "file:src/main/resources/application.properties";

  @Override
  public void execute() throws MojoExecutionException {
    ConfigurableApplicationContext context = new SpringApplicationBuilder()
        .sources(Application.class)
        .bannerMode(Banner.Mode.OFF)
        .run();

    StringEncryptor encryptor = context.getBean(StringEncryptor.class);

    run(new EncryptionService(encryptor), getFullFilePath(context));
  }

  /**
   * Run the encryption task.
   * @param encryptionService the service for encryption
   * @param fullPath the path to operate on
   */
  abstract void run(EncryptionService encryptionService, Path fullPath)
      throws MojoExecutionException;

  /**
   * Construct the full file path, relative to the active environment.
   * @param context the context (for retrieving active environments)
   * @return the full path
   * @throws MojoExecutionException if the file does not exist
   */
  private Path getFullFilePath(final ApplicationContext context)
      throws MojoExecutionException {
    String fullPath = path;
    Environment env = context.getEnvironment();
    String[] activeProfiles = env.getActiveProfiles();

    if (activeProfiles.length > 0) {
      String extension = getExtension(fullPath);
      String pathWithoutExtension = removeExtension(fullPath);

      fullPath = pathWithoutExtension + "-" + activeProfiles[0];

      if (!extension.isEmpty()) {
        fullPath += "." + extension;
      }
    }

    try {
      return context.getResource(fullPath).getFile().toPath();
    } catch (IOException e) {
      throw new MojoExecutionException("Unable to open configuration file", e);
    }
  }
}
