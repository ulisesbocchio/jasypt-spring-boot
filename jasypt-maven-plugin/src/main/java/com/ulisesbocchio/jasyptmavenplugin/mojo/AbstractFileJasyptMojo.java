package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Abstract Mojo for Files
 *
 * @author Rupert Madden-Abbott
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Slf4j
public abstract class AbstractFileJasyptMojo extends AbstractJasyptMojo {

    /**
     * The path of the file to operate on.
     */
    @Parameter(property = "jasypt.plugin.path",
            defaultValue = "file:src/main/resources/application.properties")
    private String path = "file:src/main/resources/application.properties";

    @Override
    void run(EncryptionService encryptionService, ConfigurableApplicationContext context, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix)
            throws MojoExecutionException {
        run(encryptionService, getFullFilePath(context), encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
    }

    /**
     * Run the encryption task.
     *
     * @param encryptionService the service for encryption
     * @param fullPath          the path to operate on
     * @param encryptPrefix     encryption prefix
     * @param encryptSuffix     encryption suffix
     * @param decryptPrefix     decryption prefix
     * @param decryptSuffix     decryption suffix
     */
    abstract void run(EncryptionService encryptionService, Path fullPath, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix)
            throws MojoExecutionException;

    /**
     * Construct the full file path, relative to the active environment.
     *
     * @param context the context (for retrieving active environments)
     * @return the full path
     * @throws MojoExecutionException if the file does not exist
     */
    private Path getFullFilePath(final ApplicationContext context)
            throws MojoExecutionException {
        try {
            return context.getResource(path).getFile().toPath();
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to open configuration file", e);
        }
    }
}
