package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.List;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
import static com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration.FILTER_BEAN_NAME;
import static com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration.RESOLVER_BEAN_NAME;

/**
 * <p>{@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link
 * EncryptablePropertyResolver} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 */
public class EnableEncryptablePropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertiesBeanFactoryPostProcessor.class);
    private ConfigurableEnvironment environment;
    private InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;

    public EnableEncryptablePropertiesBeanFactoryPostProcessor(ConfigurableEnvironment environment, InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses) {
        this.environment = environment;
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Post-processing PropertySource instances");
        EncryptablePropertyResolver propertyResolver = beanFactory.getBean(RESOLVER_BEAN_NAME, EncryptablePropertyResolver.class);
        EncryptablePropertyFilter propertyFilter = beanFactory.getBean(FILTER_BEAN_NAME, EncryptablePropertyFilter.class);
        MutablePropertySources propSources = environment.getPropertySources();
        convertPropertySources(interceptionMode, skipPropertySourceClasses, propertyResolver, propertyFilter, propSources);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }
}
