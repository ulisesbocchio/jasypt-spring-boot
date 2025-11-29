package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.caching.EncryptableMapWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableSystemEnvironmentPropertySourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * This class initializes {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableSystemEnvironmentPropertySourceWrapper} after
 * {@link org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor} to enable the wrapping of `getSource`
 * to a {@link EncryptableMapWrapper}. This can't be done before because Spring, of course.
 * Spring for some unknown reason converts immediately after environment initialization the system environment property source to a
 * {@link org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor.OriginAwareSystemEnvironmentPropertySource}
 * @see org.springframework.boot.env.SystemEnvironmentPropertySourceEnvironmentPostProcessor
 */
@Slf4j
public class EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener implements EnvironmentPostProcessor, Ordered {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String sourceName = StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
        PropertySource<?> propertySource = environment.getPropertySources().get(sourceName);
        log.info("Attempting to bootstrap EncryptableSystemEnvironmentPropertySourceWrapper");
        if (propertySource instanceof EncryptableSystemEnvironmentPropertySourceWrapper) {
            log.info("EncryptableSystemEnvironmentPropertySourceWrapper found! Bootstrapping...");
            ((EncryptableSystemEnvironmentPropertySourceWrapper)propertySource).setWrapGetSource(true);
        }
    }

    @Override
    public int getOrder() {
        return SystemEnvironmentPropertySourceEnvironmentPostProcessor.DEFAULT_ORDER +1;
    }
}
