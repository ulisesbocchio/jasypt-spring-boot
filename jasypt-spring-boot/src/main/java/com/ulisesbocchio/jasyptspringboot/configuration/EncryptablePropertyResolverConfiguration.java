package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.annotation.ConditionalOnMissingBean;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class EncryptablePropertyResolverConfiguration {

    private static final String ENCRYPTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.bean:jasyptStringEncryptor}";
    private static final String DETECTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.property.detector-bean:encryptablePropertyDetector}";
    public static final String RESOLVER_BEAN_PLACEHOLDER = "${jasypt.encryptor.property.resolver-bean:encryptablePropertyResolver}";

    private static final Logger LOG = LoggerFactory.getLogger(EncryptablePropertyResolverConfiguration.class);

    @Bean
    public static BeanNamePlaceholderRegistryPostProcessor beanNamePlaceholderRegistryPostProcessor(ConfigurableEnvironment environment) {
        return new BeanNamePlaceholderRegistryPostProcessor(environment);
    }

    @Bean
    public EnvCopy envCopy(ConfigurableEnvironment environment) {
        return new EnvCopy(environment);
    }

    @ConditionalOnMissingBean
    @Bean(name = ENCRYPTOR_BEAN_PLACEHOLDER)
    public StringEncryptor stringEncryptor(@SuppressWarnings("SpringJavaAutowiringInspection") EnvCopy envCopy) {
        String encryptorBeanName = envCopy.get().resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        LOG.info("String Encryptor custom Bean not found with name '{}'. Initializing String Encryptor based on properties with name '{}'",
                encryptorBeanName, encryptorBeanName);
        return new DefaultLazyEncryptor(envCopy.get());
    }

    @ConditionalOnMissingBean
    @Bean(name = DETECTOR_BEAN_PLACEHOLDER)
    public EncryptablePropertyDetector encryptablePropertyDetector(@SuppressWarnings("SpringJavaAutowiringInspection") EnvCopy envCopy) {
        String prefix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.prefix:ENC(}");
        String suffix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.suffix:)}");
        return new DefaultPropertyDetector(prefix, suffix);
    }

    @ConditionalOnMissingBean
    @Bean(name = RESOLVER_BEAN_PLACEHOLDER)
    public EncryptablePropertyResolver encryptablePropertyResolver(BeanFactory bf, @SuppressWarnings("SpringJavaAutowiringInspection") EnvCopy envCopy) {
        return new DefaultLazyPropertyResolver(ENCRYPTOR_BEAN_PLACEHOLDER, DETECTOR_BEAN_PLACEHOLDER, bf, envCopy.get());
    }

    /**
     * Need a copy of the environment without the Enhanced property sources to avoid circular dependencies.
     */
    private static class EnvCopy {
        StandardEnvironment copy;

        EnvCopy(ConfigurableEnvironment environment) {
            copy = new StandardEnvironment();
            environment.getPropertySources().forEach(ps -> {
                PropertySource<?> original = ps instanceof EncryptablePropertySource ? ((EncryptablePropertySource)ps).getDelegate() : ps;
                copy.getPropertySources().addLast(original);
            });
        }

        ConfigurableEnvironment get() {
            return copy;
        }
    }

}
