package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * <p>{@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 */
public class EnableEncryptablePropertySourcesPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertySourcesPostProcessor.class);

    private ConfigurableEnvironment environment;
    private InterceptionMode interceptionMode;

    public EnableEncryptablePropertySourcesPostProcessor(ConfigurableEnvironment environment, InterceptionMode interceptionMode) {
        this.environment = environment;
        this.interceptionMode = interceptionMode;
    }

    private <T> PropertySource<T> makeEncryptable(PropertySource<T> propertySource, ConfigurableListableBeanFactory registry) {
        StringEncryptor encryptor = registry.getBean(StringEncryptor.class);
        PropertySource<T> encryptablePropertySource = interceptionMode == InterceptionMode.PROXY
                ? proxyPropertySource(propertySource, encryptor) : instantiatePropertySource(propertySource, encryptor);
        LOG.info("Converting PropertySource {}[{}] to {}", propertySource.getName(), propertySource.getClass().getName(),
                encryptablePropertySource.getClass().getSimpleName());
        return encryptablePropertySource;
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource, StringEncryptor encryptor) {
        //Silly Chris Beams for making CommandLinePropertySource getProperty and containsProperty methods final. Those methods
        //can't be proxied with CGLib because of it. So fallback to wrapper for Command Line Arguments only.
        if (propertySource instanceof CommandLinePropertySource) {
            return instantiatePropertySource(propertySource, encryptor);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new EncryptablePropertySourceMethodInterceptor<>(encryptor));
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySource<T> instantiatePropertySource(PropertySource<T> propertySource, StringEncryptor encryptor) {
        PropertySource<T> encryptablePropertySource;
        if (propertySource instanceof MapPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableMapPropertySourceWrapper((MapPropertySource) propertySource, encryptor);
        } else if (propertySource instanceof EnumerablePropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableEnumerablePropertySourceWrapper<>((EnumerablePropertySource) propertySource, encryptor);
        } else {
            encryptablePropertySource = new EncryptablePropertySourceWrapper<>(propertySource, encryptor);
        }
        return encryptablePropertySource;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        LOG.info("Post-processing PropertySource instances");
        MutablePropertySources propSources = environment.getPropertySources();
        StreamSupport.stream(propSources.spliterator(), false)
                .map(s -> makeEncryptable(s, beanFactory))
                .collect(toList())
                .forEach(ps -> propSources.replace(ps.getName(), ps));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
