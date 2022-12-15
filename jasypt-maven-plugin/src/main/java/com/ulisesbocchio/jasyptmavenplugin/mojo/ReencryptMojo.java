package com.ulisesbocchio.jasyptmavenplugin.mojo;

import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * <p>ReencryptMojo class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
@Mojo(name = "reencrypt", defaultPhase = LifecyclePhase.PROCESS_RESOURCES)
public class ReencryptMojo extends AbstractReencryptMojo {
    @Parameter(property = "jasypt.plugin.old.password") private String oldPassword;
    @Parameter(property = "jasypt.plugin.old.private-key-string") private String oldPrivateKeyString;
    @Parameter(property = "jasypt.plugin.old.private-key-location") private String oldPrivateKeyLocation;
    @Parameter(property = "jasypt.plugin.old.private-key-format") private AsymmetricCryptography.KeyFormat oldPrivateKeyFormat;
    @Parameter(property = "jasypt.plugin.old.algorithm") private String oldAlgorithm;
    @Parameter(property = "jasypt.plugin.old.key-obtention-iterations") private String oldKeyObtentionIterations;
    @Parameter(property = "jasypt.plugin.old.pool-size") private String oldPoolSize;
    @Parameter(property = "jasypt.plugin.old.provider-name") private String oldProviderName;
    @Parameter(property = "jasypt.plugin.old.provider-class-name") private String oldProviderClassName;
    @Parameter(property = "jasypt.plugin.old.salt-generator-classname") private String oldSaltGeneratorClassname;
    @Parameter(property = "jasypt.plugin.old.iv-generator-classname") private String oldIvGeneratorClassname;
    @Parameter(property = "jasypt.plugin.old.string-output-type") private String oldStringOutputType;

    /** {@inheritDoc} */
    @Override
    protected void configure(JasyptEncryptorConfigurationProperties properties) {
        setIfNotNull(properties::setPassword, oldPassword);
        setIfNotNull(properties::setPrivateKeyString, oldPrivateKeyString);
        setIfNotNull(properties::setPrivateKeyLocation, oldPrivateKeyLocation);
        setIfNotNull(properties::setPrivateKeyFormat, oldPrivateKeyFormat);

        setIfNotNull(properties::setAlgorithm, oldAlgorithm);
        setIfNotNull(properties::setKeyObtentionIterations, oldKeyObtentionIterations);
        setIfNotNull(properties::setPoolSize, oldPoolSize);
        setIfNotNull(properties::setProviderName, oldProviderName);
        setIfNotNull(properties::setProviderClassName, oldProviderClassName);
        setIfNotNull(properties::setSaltGeneratorClassname, oldSaltGeneratorClassname);
        setIfNotNull(properties::setIvGeneratorClassname, oldIvGeneratorClassname);
        setIfNotNull(properties::setStringOutputType, oldStringOutputType);
    }
}
