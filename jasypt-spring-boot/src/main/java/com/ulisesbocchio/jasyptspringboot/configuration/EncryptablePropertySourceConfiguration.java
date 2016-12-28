package com.ulisesbocchio.jasyptspringboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Ulises Bocchio
 */
@Configuration
@Import(EncryptablePropertyResolverConfiguration.class)
public class EncryptablePropertySourceConfiguration {

    @Bean
    public static EncryptablePropertySourceBeanFactoryPostProcessor encryptablePropertySourceAnnotationPostProcessor() {
        return new EncryptablePropertySourceBeanFactoryPostProcessor();
    }

}
