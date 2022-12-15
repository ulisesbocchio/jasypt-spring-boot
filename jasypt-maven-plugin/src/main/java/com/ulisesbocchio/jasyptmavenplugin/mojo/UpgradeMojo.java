package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.springframework.core.env.Environment;

/**
 * <p>UpgradeMojo class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Mojo(name = "upgrade", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class UpgradeMojo extends AbstractReencryptMojo {
    @Parameter(property = "jasypt.plugin.old.major-version", defaultValue = "2")
    private int oldMajorVersion = 2;

    /** {@inheritDoc} */
    @Override
    protected void configure(JasyptEncryptorConfigurationProperties properties) {
        Environment environment = getEnvironment();

        setIfNotNull(properties::setPassword, environment.getProperty("jasypt.encryptor.password"));
        setIfNotNull(properties::setPrivateKeyFormat, environment.getProperty("jasypt.encryptor.private-key-format", AsymmetricCryptography.KeyFormat.class));
        setIfNotNull(properties::setPrivateKeyString, environment.getProperty("jasypt.encryptor.private-key-string"));
        setIfNotNull(properties::setPrivateKeyLocation, environment.getProperty("jasypt.encryptor.private-key-location"));

        if (oldMajorVersion == 2) {
            upgradeFrom2(properties);
        } else {
            throw new RuntimeException("Unrecognised major version " + oldMajorVersion);
        }
    }

    private void upgradeFrom2(JasyptEncryptorConfigurationProperties properties) {
        properties.setAlgorithm("PBEWithMD5AndDES");
        properties.setKeyObtentionIterations("1000");
        properties.setPoolSize("1");
        properties.setProviderName(null);
        properties.setProviderClassName(null);
        properties.setSaltGeneratorClassname("org.jasypt.salt.RandomSaltGenerator");
        properties.setIvGeneratorClassname("org.jasypt.iv.NoIvGenerator");
        properties.setStringOutputType("base64");
    }
}
