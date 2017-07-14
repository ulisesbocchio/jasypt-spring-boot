package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.stream.StreamSupport;

import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.instantiatePropertySource;
import static com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter.proxyPropertySource;
import static com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration.RESOLVER_BEAN_NAME;
import static java.util.stream.Collectors.toList;

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

    private <T> PropertySource<T> makeEncryptable(PropertySource<T> propertySource, BeanFactory bf) {
        EncryptablePropertyResolver propertyResolver = bf.getBean(RESOLVER_BEAN_NAME, EncryptablePropertyResolver.class);
        PropertySource<T> encryptablePropertySource = interceptionMode == InterceptionMode.PROXY
                ? proxyPropertySource(propertySource, propertyResolver) : instantiatePropertySource(propertySource, propertyResolver);
        LOG.info("Converting PropertySource {} [{}] to {}", propertySource.getName(), propertySource.getClass().getName(),
                AopUtils.isAopProxy(encryptablePropertySource) ? "AOP Proxy" : encryptablePropertySource.getClass().getSimpleName());
        return encryptablePropertySource;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        StreamSupport.stream(propSources.spliterator(), false)
                .filter(ps -> !(ps instanceof EncryptablePropertySource))
                .map(ps -> makeEncryptable(ps, beanFactory))
                .collect(toList())
                .forEach(ps -> propSources.replace(ps.getName(), ps));
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
