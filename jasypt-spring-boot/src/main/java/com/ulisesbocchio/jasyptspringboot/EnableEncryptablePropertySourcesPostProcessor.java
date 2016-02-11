package com.ulisesbocchio.jasyptspringboot;

import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.cglib.proxy.Proxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.*;
import org.springframework.util.ClassUtils;

import java.util.Arrays;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorConfiguration.ENCRYPTOR_BEAN_PLACEHOLDER;
import static java.util.stream.Collectors.toList;

import com.ulisesbocchio.jasyptspringboot.aop.EncryptablePropertySourceMethodInterceptor;
import com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorConfiguration;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableEnumerablePropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableMapPropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper;

/**
 * <p>{@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * <p>It takes the lowest precedence so it does not interfere with Spring Boot's own post processors</p>
 *
 * @author Ulises Bocchio
 */
public class EnableEncryptablePropertySourcesPostProcessor implements BeanFactoryPostProcessor, ApplicationListener<ApplicationEvent>, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertySourcesPostProcessor.class);

    private ConfigurableEnvironment environment;
    private InterceptionMode interceptionMode;

    public EnableEncryptablePropertySourcesPostProcessor() {
        this.interceptionMode = InterceptionMode.PROXY;
    }

    public EnableEncryptablePropertySourcesPostProcessor(ConfigurableEnvironment environment, InterceptionMode interceptionMode) {
        this.environment = environment;
        this.interceptionMode = interceptionMode;
    }

    private <T> PropertySource<T> makeEncryptable(PropertySource<T> propertySource, ConfigurableListableBeanFactory registry) {
        StringEncryptor encryptor = registry.getBean(environment.resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER), StringEncryptor.class);
        PropertySource<T> encryptablePropertySource = interceptionMode == InterceptionMode.PROXY
                ? proxyPropertySource(propertySource, encryptor) : instantiatePropertySource(propertySource, encryptor);
        LOG.info("Converting PropertySource {} [{}] to {}", propertySource.getName(), propertySource.getClass().getName(),
                 AopUtils.isAopProxy(encryptablePropertySource) ? "AOP Proxy" : encryptablePropertySource.getClass().getSimpleName());
        return encryptablePropertySource;
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource, StringEncryptor encryptor) {
        //Silly Chris Beams for making CommandLinePropertySource getProperty and containsProperty methods final. Those methods
        //can't be proxied with CGLib because of it. So fallback to wrapper for Command Line Arguments only.
        if (CommandLinePropertySource.class.isAssignableFrom(propertySource.getClass())) {
            return instantiatePropertySource(propertySource, encryptor);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(EncryptablePropertySource.class);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new EncryptablePropertySourceMethodInterceptor<>(encryptor));
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySource<T> instantiatePropertySource(PropertySource<T> propertySource, StringEncryptor encryptor) {
        PropertySource<T> encryptablePropertySource;
        if (propertySource instanceof MapPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableMapPropertySourceWrapper((MapPropertySource) propertySource, encryptor);
        } else if (propertySource.getClass().getName().equals("org.springframework.boot.context.config.ConfigFileApplicationListener$ConfigurationPropertySources")) {
            //Some Spring Boot code actually casts property sources to this specific type so must be proxied.
            encryptablePropertySource = proxyPropertySource(propertySource, encryptor);
        } else if (propertySource instanceof EnumerablePropertySource) {
            encryptablePropertySource = new EncryptableEnumerablePropertySourceWrapper<>((EnumerablePropertySource) propertySource, encryptor);
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
            .filter(ps -> !(ps instanceof EncryptablePropertySource))
            .map(s -> makeEncryptable(s, beanFactory))
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
