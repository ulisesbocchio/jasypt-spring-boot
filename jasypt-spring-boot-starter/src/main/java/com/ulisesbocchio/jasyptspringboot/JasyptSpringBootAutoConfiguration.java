package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesConfiguration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Ulises Bocchio
 */
@Configuration
@Import(EnableEncryptablePropertiesConfiguration.class)
public class JasyptSpringBootAutoConfiguration {
}
