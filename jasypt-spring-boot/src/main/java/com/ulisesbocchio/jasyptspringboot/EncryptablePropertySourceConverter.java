package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.aop.EncryptablePropertySourceMethodInterceptor;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableEnumerablePropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableMapPropertySourceWrapper;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

/**
 * @author Ulises Bocchio
 */
public class EncryptablePropertySourceConverter {
    @SuppressWarnings("unchecked")
    public static <T> PropertySource<T> proxyPropertySource(PropertySource<T> propertySource, EncryptablePropertyResolver resolver) {
        //Silly Chris Beams for making CommandLinePropertySource getProperty and containsProperty methods final. Those methods
        //can't be proxied with CGLib because of it. So fallback to wrapper for Command Line Arguments only.
        if (CommandLinePropertySource.class.isAssignableFrom(propertySource.getClass())) {
            return instantiatePropertySource(propertySource, resolver);
        }
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTargetClass(propertySource.getClass());
        proxyFactory.setProxyTargetClass(true);
        proxyFactory.addInterface(EncryptablePropertySource.class);
        proxyFactory.setTarget(propertySource);
        proxyFactory.addAdvice(new EncryptablePropertySourceMethodInterceptor<>(propertySource, resolver));
        return (PropertySource<T>) proxyFactory.getProxy();
    }

    @SuppressWarnings("unchecked")
    public static <T> PropertySource<T> instantiatePropertySource(PropertySource<T> propertySource, EncryptablePropertyResolver resolver) {
        PropertySource<T> encryptablePropertySource;
        if (propertySource instanceof MapPropertySource) {
            encryptablePropertySource = (PropertySource<T>) new EncryptableMapPropertySourceWrapper((MapPropertySource) propertySource, resolver);
        } else if (propertySource.getClass().getName().equals("org.springframework.boot.context.config.ConfigFileApplicationListener$ConfigurationPropertySources")) {
            //Some Spring Boot code actually casts property sources to this specific type so must be proxied.
            encryptablePropertySource = proxyPropertySource(propertySource, resolver);
        } else if (propertySource instanceof EnumerablePropertySource) {
            encryptablePropertySource = new EncryptableEnumerablePropertySourceWrapper<>((EnumerablePropertySource) propertySource, resolver);
        } else {
            encryptablePropertySource = new EncryptablePropertySourceWrapper<>(propertySource, resolver);
        }
        return encryptablePropertySource;
    }
}
