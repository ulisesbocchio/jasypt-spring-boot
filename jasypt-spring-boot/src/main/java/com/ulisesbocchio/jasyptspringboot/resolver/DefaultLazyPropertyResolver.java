package com.ulisesbocchio.jasyptspringboot.resolver;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

import java.util.Optional;

/**
 * Default Resolver bean that delegates to a custom defined {@link EncryptablePropertyResolver} or creates a new {@link DefaultPropertyResolver}
 *
 * @author Ulises Bocchio
 */
@Slf4j
public class DefaultLazyPropertyResolver implements EncryptablePropertyResolver {

    private Singleton<EncryptablePropertyResolver> singleton;

    public DefaultLazyPropertyResolver(EncryptablePropertyDetector propertyDetector, StringEncryptor encryptor, String customResolverBeanName, BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customResolverBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (EncryptablePropertyResolver) bf.getBean(name))
                        .map(bean -> {
                            log.info("Found Custom Resolver Bean {} with name: {}", bean, customResolverBeanName);
                            return bean;
                        })
                        .orElseGet(() -> {
                            log.info("Property Resolver custom Bean not found with name '{}'. Initializing Default Property Resolver", customResolverBeanName);
                            return new DefaultPropertyResolver(encryptor, propertyDetector);
                        }));
    }

    @Override
    public String resolvePropertyValue(String value) {
        return singleton.get().resolvePropertyValue(value);
    }
}
