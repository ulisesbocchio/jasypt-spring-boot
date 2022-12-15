package com.ulisesbocchio.jasyptspringboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * <p>EncryptablePropertySourceConfiguration class.</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
@Configuration
@Import({EncryptablePropertyResolverConfiguration.class, CachingConfiguration.class})
public class EncryptablePropertySourceConfiguration {

    /**
     * <p>encryptablePropertySourceAnnotationPostProcessor.</p>
     *
     * @param env a {@link org.springframework.core.env.ConfigurableEnvironment} object
     * @return a {@link com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertySourceBeanFactoryPostProcessor} object
     */
    @Bean
    public static EncryptablePropertySourceBeanFactoryPostProcessor encryptablePropertySourceAnnotationPostProcessor(ConfigurableEnvironment env) {
        return new EncryptablePropertySourceBeanFactoryPostProcessor(env);
    }

}
