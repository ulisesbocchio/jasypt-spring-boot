package com.ulisesbocchio.jasyptspringboot.configuration;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class StringEncryptorConfiguration {

    public static final String ENCRYPTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.bean:jasyptStringEncryptor}";

    private static final Logger LOG = LoggerFactory.getLogger(StringEncryptorConfiguration.class);

    @Conditional(OnMissingEncryptorBean.class)
    @Bean
    public BeanNamePlaceholderRegistryPostProcessor beanNamePlaceholderRegistryPostProcessor(Environment environment) {
        return new BeanNamePlaceholderRegistryPostProcessor(environment);
    }

    @Conditional(OnMissingEncryptorBean.class)
    @Bean(name = ENCRYPTOR_BEAN_PLACEHOLDER)
    public StringEncryptor stringEncryptor(Environment environment) {
        String encryptorBeanName = environment.resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        LOG.info("String Encryptor custom Bean not found with name '{}'. Initializing String Encryptor based on properties with name '{}'",
                 encryptorBeanName, encryptorBeanName);
        return new LazyStringEncryptor(() -> {
            PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPassword(getRequiredProperty(environment, "jasypt.encryptor.password"));
            config.setAlgorithm(getProperty(environment, "jasypt.encryptor.algorithm", "PBEWithMD5AndDES"));
            config.setKeyObtentionIterations(getProperty(environment, "jasypt.encryptor.keyObtentionIterations", "1000"));
            config.setPoolSize(getProperty(environment, "jasypt.encryptor.poolSize", "1"));
            config.setProviderName(getProperty(environment, "jasypt.encryptor.providerName", "SunJCE"));
            config.setSaltGeneratorClassName(getProperty(environment, "jasypt.encryptor.saltGeneratorClassname", "org.jasypt.salt.RandomSaltGenerator"));
            config.setStringOutputType(getProperty(environment, "jasypt.encryptor.stringOutputType", "base64"));
            encryptor.setConfig(config);
            return encryptor;
        });
    }

    private String getProperty(Environment environment, String key, String defaultValue) {
        if (!propertyExists(environment, key)) {
            LOG.info("Encryptor config not found for property {}, using default value: {}", key, defaultValue);
        }
        return environment.getProperty(key, defaultValue);
    }

    private boolean propertyExists(Environment environment, String key) {
        return environment.getProperty(key) != null;
    }

    private String getRequiredProperty(Environment environment, String key) {
        if (!propertyExists(environment, key)) {
            throw new IllegalStateException(String.format("Required Encryption configuration property missing: %s", key));
        }
        return environment.getProperty(key);
    }

    /**
     * Condition that checks whether the StringEncryptor specified by placeholder: {@link #ENCRYPTOR_BEAN_PLACEHOLDER} exists.
     * ConditionalOnMissingBean does not support placeholder resolution.
     */
    private static class OnMissingEncryptorBean implements ConfigurationCondition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !context.getBeanFactory().containsBean(context.getEnvironment().resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER));
        }

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.REGISTER_BEAN;
        }
    }

    /**
     * Bean Definition Registry Post Processor that looks for placeholders in bean names and resolves them, re-defining those beans
     * with the new names.
     */
    private static class BeanNamePlaceholderRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {

        private Environment environment;

        private BeanNamePlaceholderRegistryPostProcessor(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            DefaultListableBeanFactory bf = (DefaultListableBeanFactory) registry;
            Stream.of(bf.getBeanDefinitionNames())
                //Look for beans with placeholders name format: '${placeholder}' or '${placeholder:defaultValue}'
                .filter(name -> name.matches("\\$\\{[\\w\\.-]+(?>:[\\w\\.-]+)?\\}"))
                .forEach(placeholder -> {
                    String actualName = environment.resolveRequiredPlaceholders(placeholder);
                    BeanDefinition bd = bf.getBeanDefinition(placeholder);
                    bf.removeBeanDefinition(placeholder);
                    bf.registerBeanDefinition(actualName, bd);
                    LOG.debug("Registering new name '{}' for Bean definition with placeholder name: {}", actualName, placeholder);
                });
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE - 1;
        }
    }

    /**
     * String encryptor that delays pulling configuration properties to configure the encryptor until the moment when the first encrypted property is retrieved. Thus allowing for late retrieval of
     * configuration when all property sources have been established, and avoids missing configuration properties errors when no encrypted properties are present in configuration files.
     */
    private static final class LazyStringEncryptor implements StringEncryptor {

        private final Supplier<StringEncryptor> supplier;

        private LazyStringEncryptor(final Supplier<StringEncryptor> encryptorFactory) {
            supplier = new SingletonSupplier<>(encryptorFactory);
        }

        @Override
        public String encrypt(String message) {
            return supplier.get().encrypt(message);
        }

        @Override
        public String decrypt(String encryptedMessage) {
            return supplier.get().decrypt(encryptedMessage);
        }
    }

    /**
     * Singleton initializer class that uses an internal supplier to supply the singleton instance. The supplier originally checks whether the instanceSupplier
     * has been initialized or not, but after initialization the instance supplier is changed to avoid extra logic execution.
     */
    private static final class SingletonSupplier<T> implements Supplier<T> {

        private boolean initialized = false;
        private volatile Supplier<T> instanceSupplier;

        private SingletonSupplier(final Supplier<T> original) {
            this.instanceSupplier = () -> {
                synchronized (original) {
                    if (!initialized) {
                        final T singletonInstance = original.get();
                        instanceSupplier = () -> {
                            return singletonInstance;
                        };
                        initialized = true;
                    }
                    return instanceSupplier.get();
                }
            };
        }

        @Override
        public T get() {
            return instanceSupplier.get();
        }
    }
}
