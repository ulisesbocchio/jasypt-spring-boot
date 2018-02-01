package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Bootstrap configuration applicable only in spring-cloud environments. Can
 * be explicitly turned-off by <code>jasypt.encryptor.bootstrap=false</code>
 * configuration (in bootstrap.properties or as a command line argument) in that case
 * Jasypt will be auto-configured as usual.
 * 
 * @author Fahim Farook
 *
 */
@Configuration
@ConditionalOnProperty(name = "jasypt.encryptor.bootstrap", havingValue = "true", matchIfMissing = true)
@Import(EnableEncryptablePropertiesConfiguration.class)
public class JasyptSpringCloudBootstrapConfiguration {

}
