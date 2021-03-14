package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.aop.EncryptableMutablePropertySourcesInterceptor;
import com.ulisesbocchio.jasyptspringboot.aop.EncryptablePropertySourceMethodInterceptor;
import com.ulisesbocchio.jasyptspringboot.configuration.EnvCopy;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableEnumerablePropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableMapPropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableSystemEnvironmentPropertySourceWrapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.env.*;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * @author Ulises Bocchio
 */
@Slf4j
public class EncryptablePropertySourceConverter {

    private final InterceptionMode interceptionMode;
    private final List<Class<PropertySource<?>>> skipPropertySourceClasses;
    private final EncryptablePropertyResolver propertyResolver;
    private final EncryptablePropertyFilter propertyFilter;

    public EncryptablePropertySourceConverter(InterceptionMode interceptionMode, List<Class<PropertySource<?>>> skipPropertySourceClasses, EncryptablePropertyResolver propertyResolver, EncryptablePropertyFilter propertyFilter) {
        this.interceptionMode = interceptionMode;
        this.skipPropertySourceClasses = skipPropertySourceClasses;
        this.propertyResolver = propertyResolver;
        this.propertyFilter = propertyFilter;
    }

    public void convertPropertySources(MutablePropertySources propSources) {
        StreamSupport.stream(propSources.spliterator(), false)
                .filter(ps -> !(ps instanceof EncryptablePropertySource || ps instanceof PropertySource.StubPropertySource))
                .map(this::makeEncryptable)
                .collect(toList())
                .forEach(ps -> propSources.replace(ps.getName(), ps));
    }

    @SuppressWarnings("unchecked")
    public <T> PropertySource<T> makeEncryptable(PropertySource<T> propertySource) {
        if (propertySource instanceof EncryptablePropertySource || skipPropertySourceClasses.stream().anyMatch(skipClass -> skipClass.equals(propertySource.getClass()))) {
            log.info("Skipping PropertySource {} [{}", propertySource.getName(), propertySource.getClass());
            return propertySource;
        }
        PropertySource<T> encryptablePropertySource = convertPropertySource(propertySource);
        log.info("Converting PropertySource {} [{}] to {}", propertySource.getName(), propertySource.getClass().getName(),
                AopUtils.isAopProxy(encryptablePropertySource) ? "AOP Proxy" : encryptablePropertySource.getClass().getSimpleName());
        return encryptablePropertySource;
    }

    public MutablePropertySources proxyPropertySources(MutablePropertySources propertySources, EnvCopy envCopy) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(MutablePropertySources.class);
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(PropertySources.class);
        proxyFactory.setTarget(propertySources);
        proxyFactory.addAdvice(new EncryptableMutablePropertySourcesInterceptor(this, envCopy));
        return (MutablePropertySources) proxyFactory.getProxy();
    }

    private <T> PropertySource<T> convertPropertySource(PropertySource<T> propertySource) {
        return interceptionMode == InterceptionMode.PROXY
                ? proxyPropertySource(propertySource) : instantiatePropertySource(propertySource);
    }

    @SuppressWarnings("unchecked")
    private <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource) {
        //Silly Chris Beams for making CommandLinePropertySource getProperty and containsProperty methods final. Those methods
        //can't be proxied with CGLib because of it. So fallback to wrapper for Command Line Arguments only.
        if (CommandLinePropertySource.class.isAssignableFrom(propertySource.getClass())
                // Other PropertySource classes like org.springframework.boot.env.OriginTrackedMapPropertySource
                // are final classes as well
                || Modifier.isFinal(propertySource.getClass().getModifiers())) {
            return instantiatePropertySource(propertySource);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(EncryptablePropertySource.class);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new EncryptablePropertySourceMethodInterceptor<>(propertySource, propertyResolver, propertyFilter));
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> PropertySource<T> instantiatePropertySource(PropertySource<T> propertySource) {
        PropertySource<T> encryptablePropertySource;
        if (needsProxyAnyway(propertySource)) {
            encryptablePropertySource = proxyPropertySource(propertySource);
        } else if (propertySource instanceof  SystemEnvironmentPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableSystemEnvironmentPropertySourceWrapper((SystemEnvironmentPropertySource) propertySource, propertyResolver, propertyFilter);
        } else if (propertySource instanceof MapPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableMapPropertySourceWrapper((MapPropertySource) propertySource, propertyResolver, propertyFilter);
        } else if (propertySource instanceof EnumerablePropertySource) {
            encryptablePropertySource = new EncryptableEnumerablePropertySourceWrapper<>((EnumerablePropertySource) propertySource, propertyResolver, propertyFilter);
        } else {
            encryptablePropertySource = new EncryptablePropertySourceWrapper<>(propertySource, propertyResolver, propertyFilter);
        }
        return encryptablePropertySource;
    }

    @SuppressWarnings("unchecked")
    private static boolean needsProxyAnyway(PropertySource<?> ps) {
        return needsProxyAnyway((Class<? extends PropertySource<?>>) ps.getClass());
    }

    private static boolean needsProxyAnyway(Class<? extends PropertySource<?>> psClass) {
        return needsProxyAnyway(psClass.getName());
    }

    /**
     *  Some Spring Boot code actually casts property sources to this specific type so must be proxied.
     */
    private static boolean needsProxyAnyway(String className) {
        return Stream.of(
                "org.springframework.boot.context.config.ConfigFileApplicationListener$ConfigurationPropertySources",
                "org.springframework.boot.context.properties.source.ConfigurationPropertySourcesPropertySource"
                ).anyMatch(className::equals);
    }
}
