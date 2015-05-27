package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * <p>{@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 *
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 */
public class EnableEncryptablePropertySourcesPostProcessor implements BeanFactoryPostProcessor, EnvironmentAware, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertySourcesPostProcessor.class);

    private ConfigurableEnvironment environment;


    private <T> PropertySource<T> makeEncryptable(PropertySource<T> propertySource, ConfigurableListableBeanFactory registry) {
        StringEncryptor encryptor = registry.getBean(StringEncryptor.class);
        LOG.info("Converting PropertySource {}[{}] to EncryptablePropertySource", propertySource.getName(), propertySource.getClass().getName());
        return new EncryptablePropertySourceWrapper<>(propertySource, encryptor);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        StreamSupport.stream(propSources.spliterator(), false)
                .map(s -> makeEncryptable(s, beanFactory))
                .collect(toList())
                .forEach(es -> propSources.replace(es.getName(), es));
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
