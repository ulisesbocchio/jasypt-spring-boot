package com.ulisesbocchio.jasyptspringboot.encryptor;

import com.ulisesbocchio.jasyptspringboot.configuration.StringEncryptorBuilder;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Optional;

import static com.ulisesbocchio.jasyptspringboot.util.Functional.tap;

/**
 * Default Lazy Encryptor that delegates to a custom {@link org.jasypt.encryption.StringEncryptor} bean or creates a default {@link org.jasypt.encryption.pbe.PooledPBEStringEncryptor} or {@link com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor}
 * based on what properties are provided
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
@Slf4j
public class DefaultLazyEncryptor implements StringEncryptor {

    private final Singleton<StringEncryptor> singleton;

    /**
     * <p>Constructor for DefaultLazyEncryptor.</p>
     *
     * @param e a {@link org.springframework.core.env.ConfigurableEnvironment} object
     * @param customEncryptorBeanName a {@link java.lang.String} object
     * @param isCustom a boolean
     * @param bf a {@link org.springframework.beans.factory.BeanFactory} object
     */
    public DefaultLazyEncryptor(final ConfigurableEnvironment e, final String customEncryptorBeanName, boolean isCustom, final BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customEncryptorBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (StringEncryptor) bf.getBean(name))
                        .map(tap(bean -> log.info("Found Custom Encryptor Bean {} with name: {}", bean, customEncryptorBeanName)))
                        .orElseGet(() -> {
                            if (isCustom) {
                                throw new IllegalStateException(String.format("String Encryptor custom Bean not found with name '%s'", customEncryptorBeanName));
                            }
                            log.info("String Encryptor custom Bean not found with name '{}'. Initializing Default String Encryptor", customEncryptorBeanName);
                            return createDefault(e);
                        }));
    }

    /**
     * <p>Constructor for DefaultLazyEncryptor.</p>
     *
     * @param e a {@link org.springframework.core.env.ConfigurableEnvironment} object
     */
    public DefaultLazyEncryptor(final ConfigurableEnvironment e) {
        singleton = new Singleton<>(() -> createDefault(e));
    }

    private StringEncryptor createDefault(ConfigurableEnvironment e) {
        return new StringEncryptorBuilder(JasyptEncryptorConfigurationProperties.bindConfigProps(e), "jasypt.encryptor").build();
    }

    /** {@inheritDoc} */
    @Override
    public String encrypt(final String message) {
        return singleton.get().encrypt(message);
    }

    /** {@inheritDoc} */
    @Override
    public String decrypt(final String encryptedMessage) {
        return singleton.get().decrypt(encryptedMessage);
    }

}
