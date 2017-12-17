package com.ulisesbocchio.jasyptspringboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;

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
@ConditionalOnClass(name = "org.springframework.cloud.bootstrap.BootstrapApplicationListener")
@ConditionalOnProperty(name = "spring.cloud.bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class JasyptSpringCloudBootstrapConfiguration {

	@Configuration
	@ConditionalOnProperty(name = "jasypt.encryptor.bootstrap", havingValue = "true", matchIfMissing = true)
	@Import(EnableEncryptablePropertiesConfiguration.class)
	protected static class BootstrappingEncryptablePropertiesConfiguration {

	}
}
