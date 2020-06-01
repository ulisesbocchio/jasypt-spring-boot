package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptmavenplugin.encrypt.EncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.boot.Banner;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.HashMap;
import java.util.Map;

/**
 * A mojo that spins up a Spring Boot application for any encrypt/decrypt function.
 *
 * @author Rupert Madden-Abbott
 */
@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Slf4j
public abstract class AbstractJasyptMojo extends AbstractMojo {

    /**
     * The encrypted property prefix
     */
    @Parameter(property = "jasypt.plugin.encrypt.prefix", defaultValue = "ENC(")
    private String encryptPrefix = "ENC(";

    /**
     * The encrypted property suffix
     */
    @Parameter(property = "jasypt.plugin.encrypt.suffix", defaultValue = ")")
    private String encryptSuffix = ")";

    /**
     * The decrypted property prefix
     */
    @Parameter(property = "jasypt.plugin.decrypt.prefix", defaultValue = "DEC(")
    private String decryptPrefix = "DEC(";

    /**
     * The decrypted property suffix
     */
    @Parameter(property = "jasypt.plugin.decrypt.suffix", defaultValue = ")")
    private String decryptSuffix = ")";

    private Environment environment;

    protected Environment getEnvironment() {
        return environment;
    }

    @Override
    public void execute() throws MojoExecutionException {
        Map<String, Object> defaultProperties = new HashMap<>();
        defaultProperties.put("spring.config.location", "file:./src/main/resources/");

        ConfigurableApplicationContext context = new SpringApplicationBuilder()
                .sources(Application.class)
                .bannerMode(Banner.Mode.OFF)
                .properties(defaultProperties)
                .run();

        this.environment = context.getEnvironment();

        String[] activeProfiles = context.getEnvironment().getActiveProfiles();
        String profiles = activeProfiles.length != 0 ? String.join(",", activeProfiles) : "Default";
        log.info("Active Profiles: {}", profiles);
        StringEncryptor encryptor = context.getBean(StringEncryptor.class);
        run(new EncryptionService(encryptor), context, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix);
    }

    /**
     * Run the encryption task.
     *
     * @param encryptionService the service for encryption
     * @param context           app context
     */
    abstract void run(EncryptionService encryptionService, ConfigurableApplicationContext context, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix)
            throws MojoExecutionException;
}
