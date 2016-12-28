package com.ulisesbocchio.jasyptspringboot.resolver;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;

/**
 * @author Ulises Bocchio
 */
public class DefaultLazyPropertyResolver implements EncryptablePropertyResolver {

    private Singleton<EncryptablePropertyResolver> singleton;

    public DefaultLazyPropertyResolver(String encryptorBeanName, String detectorBeanName, BeanFactory beanFactory, Environment environment) {
        singleton = new Singleton<>(() -> {
            StringEncryptor encryptor = beanFactory.getBean(environment.resolveRequiredPlaceholders(encryptorBeanName), StringEncryptor.class);
            EncryptablePropertyDetector detector = beanFactory.getBean(environment.resolveRequiredPlaceholders(detectorBeanName), EncryptablePropertyDetector.class);
            return new DefaultPropertyResolver(encryptor, detector);
        });

    }

    @Override
    public String resolvePropertyValue(String value) {
        return singleton.get().resolvePropertyValue(value);
    }
}
