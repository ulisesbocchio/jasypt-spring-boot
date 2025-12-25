package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.caching.EncryptableMapWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableSystemEnvironmentPropertySourceWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.support.SystemEnvironmentPropertySourceEnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

/**
 * This class initializes {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableSystemEnvironmentPropertySourceWrapper} after
 * {@link org.springframework.boot.support.SystemEnvironmentPropertySourceEnvironmentPostProcessor} to enable the wrapping of `getSource`
 * to a {@link EncryptableMapWrapper}. This can't be done before because Spring, of course.
 * Spring for some unknown reason converts immediately after environment initialization the system environment property source to a
 * OriginAwareSystemEnvironmentPropertySource (a private inner class).
 *
 * @see org.springframework.boot.support.SystemEnvironmentPropertySourceEnvironmentPostProcessor
 */
@Slf4j
public class EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener implements EnvironmentPostProcessor, Ordered {

    /**
     * Static method to enable getSource wrapping on EncryptableSystemEnvironmentPropertySourceWrapper.
     * Can be called from both EnvironmentPostProcessor and BeanFactoryPostProcessor contexts.
     *
     * @param environment the ConfigurableEnvironment to process
     */
    public static void enableGetSourceWrapping(ConfigurableEnvironment environment) {
        String sourceName = StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME;
        PropertySource<?> propertySource = environment.getPropertySources().get(sourceName);
        log.info("Attempting to bootstrap EncryptableSystemEnvironmentPropertySourceWrapper");
        if (propertySource instanceof EncryptableSystemEnvironmentPropertySourceWrapper) {
            log.info("EncryptableSystemEnvironmentPropertySourceWrapper found! Bootstrapping...");
            ((EncryptableSystemEnvironmentPropertySourceWrapper) propertySource).setWrapGetSource(true);
        }
    }

    @Override
    public void postProcessEnvironment(@NonNull ConfigurableEnvironment environment, @NonNull SpringApplication application) {
        enableGetSourceWrapping(environment);
    }

    @Override
    public int getOrder() {
        return SystemEnvironmentPropertySourceEnvironmentPostProcessor.DEFAULT_ORDER + 1;
    }
}
