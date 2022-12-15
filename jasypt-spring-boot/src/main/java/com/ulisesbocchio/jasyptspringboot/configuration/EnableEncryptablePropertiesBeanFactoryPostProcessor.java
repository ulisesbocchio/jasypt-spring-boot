package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

/**
 * <p>{@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that wraps all {@link org.springframework.core.env.PropertySource} defined in the {@link org.springframework.core.env.Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link
 * EncryptablePropertyResolver} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class EnableEncryptablePropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private final ConfigurableEnvironment environment;
    private final EncryptablePropertySourceConverter converter;

    /**
     * <p>Constructor for EnableEncryptablePropertiesBeanFactoryPostProcessor.</p>
     *
     * @param environment a {@link org.springframework.core.env.ConfigurableEnvironment} object
     * @param converter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter} object
     */
    public EnableEncryptablePropertiesBeanFactoryPostProcessor(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        this.environment = environment;
        this.converter = converter;
    }

    /** {@inheritDoc} */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        log.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
    }

    /** {@inheritDoc} */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
