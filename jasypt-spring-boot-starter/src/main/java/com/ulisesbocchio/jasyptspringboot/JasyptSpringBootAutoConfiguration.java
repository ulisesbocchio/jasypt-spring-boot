package com.ulisesbocchio.jasyptspringboot;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Ulises Bocchio
 */
@Configuration
@Import(EnableEncryptablePropertySourcesConfiguration.class)
public class JasyptSpringBootAutoConfiguration {
}
