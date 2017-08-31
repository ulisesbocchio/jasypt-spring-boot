package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.convertPropertySources;
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
public class EnableEncryptablePropertiesBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationListener<ApplicationEvent>, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertiesBeanFactoryPostProcessor.class);
    private ConfigurableEnvironment environment;
    private InterceptionMode interceptionMode;

    public EnableEncryptablePropertiesBeanFactoryPostProcessor() {
        this.interceptionMode = InterceptionMode.PROXY;
    }

    public EnableEncryptablePropertiesBeanFactoryPostProcessor(ConfigurableEnvironment environment, InterceptionMode interceptionMode) {
        this.environment = environment;
        this.interceptionMode = interceptionMode;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Post-processing PropertySource instances");
        EncryptablePropertyResolver propertyResolver = beanFactory.getBean(RESOLVER_BEAN_NAME, EncryptablePropertyResolver.class);
        MutablePropertySources propSources = environment.getPropertySources();
        convertPropertySources(interceptionMode, propertyResolver, propSources);
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        LOG.debug("Application Event Raised: {}", event.getClass().getSimpleName());
    }
}
