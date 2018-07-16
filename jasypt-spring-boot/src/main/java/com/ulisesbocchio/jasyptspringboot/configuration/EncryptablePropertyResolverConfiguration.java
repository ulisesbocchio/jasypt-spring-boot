package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.detector.DefaultLazyPropertyDetector;
import com.ulisesbocchio.jasyptspringboot.encryptor.DefaultLazyEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties.PropertyConfigurationProperties.FilterConfigurationProperties;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultLazyPropertyResolver;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.lang.annotation.Annotation;

/**
 * @author Ulises Bocchio
 */
@Configuration
@Slf4j
public class EncryptablePropertyResolverConfiguration {

    private static final String ENCRYPTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.bean:jasyptStringEncryptor}";
    private static final String DETECTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.property.detector-bean:encryptablePropertyDetector}";
    private static final String RESOLVER_BEAN_PLACEHOLDER = "${jasypt.encryptor.property.resolver-bean:encryptablePropertyResolver}";
    private static final String FILTER_BEAN_PLACEHOLDER = "${jasypt.encryptor.property.filter-bean:encryptablePropertyFilter}";

    private static final String ENCRYPTOR_BEAN_NAME = "lazyJasyptStringEncryptor";
    private static final String DETECTOR_BEAN_NAME = "lazyEncryptablePropertyDetector";
    private static final String CONFIG_SINGLETON = "configPropsSingleton";
    static final String RESOLVER_BEAN_NAME = "lazyEncryptablePropertyResolver";
    static final String FILTER_BEAN_NAME = "lazyEncryptablePropertyFilter";

    @Bean
    public EnvCopy envCopy(ConfigurableEnvironment environment) {
        return new EnvCopy(environment);
    }

    @Bean(name = ENCRYPTOR_BEAN_NAME)
    public StringEncryptor stringEncryptor(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EnvCopy envCopy, BeanFactory bf) {
        String customEncryptorBeanName = envCopy.get().resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        return new DefaultLazyEncryptor(envCopy.get(), customEncryptorBeanName, bf);
    }

    @Bean(name = DETECTOR_BEAN_NAME)
    public EncryptablePropertyDetector encryptablePropertyDetector(@SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"}) EnvCopy envCopy, BeanFactory bf) {
        String prefix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.prefix:ENC(}");
        String suffix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.suffix:)}");
        String customDetectorBeanName = envCopy.get().resolveRequiredPlaceholders(DETECTOR_BEAN_PLACEHOLDER);
        return new DefaultLazyPropertyDetector(prefix, suffix, customDetectorBeanName, bf);
    }

    @Bean(name = CONFIG_SINGLETON)
    public Singleton<JasyptEncryptorConfigurationProperties> configProps(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EnvCopy envCopy, ConfigurableBeanFactory bf) {
        return new Singleton<>(() -> {
            BindHandler handler = new IgnoreErrorsBindHandler(BindHandler.DEFAULT);
            MutablePropertySources propertySources = envCopy.get().getPropertySources();
            Binder binder = new Binder(ConfigurationPropertySources.from(propertySources),
                    new PropertySourcesPlaceholdersResolver(propertySources),
                    ApplicationConversionService.getSharedInstance(),
                    bf::copyRegisteredEditorsTo);
            JasyptEncryptorConfigurationProperties config = new JasyptEncryptorConfigurationProperties();

            ResolvableType type = ResolvableType.forClass(JasyptEncryptorConfigurationProperties.class);
            Annotation annotation = AnnotationUtils.findAnnotation(JasyptEncryptorConfigurationProperties.class, ConfigurationProperties.class);
            Annotation[] annotations = new Annotation[]{annotation};
            Bindable<?> target = Bindable.of(type).withExistingValue(config)
                    .withAnnotations(annotations);

            binder.bind("jasypt.encryptor", target, handler);
            return config;
        });
    }

    @SuppressWarnings("unchecked")
    @Bean(name = FILTER_BEAN_NAME)
    public EncryptablePropertyFilter encryptablePropertyFilter(@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EnvCopy envCopy, ConfigurableBeanFactory bf, @Qualifier(CONFIG_SINGLETON) Singleton<JasyptEncryptorConfigurationProperties> configProps) {
        String customFilterBeanName = envCopy.get().resolveRequiredPlaceholders(FILTER_BEAN_PLACEHOLDER);
        FilterConfigurationProperties filterConfig = configProps.get().getProperty().getFilter();
        return new DefaultLazyPropertyFilter(filterConfig.getIncludeSources(), filterConfig.getExcludeSources(), filterConfig.getIncludeNames(), filterConfig.getExcludeNames(), customFilterBeanName, bf);
    }


    @Bean(name = RESOLVER_BEAN_NAME)
    public EncryptablePropertyResolver encryptablePropertyResolver(@Qualifier(DETECTOR_BEAN_NAME) EncryptablePropertyDetector propertyDetector, @Qualifier(ENCRYPTOR_BEAN_NAME) StringEncryptor encryptor, BeanFactory bf, @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") EnvCopy envCopy) {
        String customResolverBeanName = envCopy.get().resolveRequiredPlaceholders(RESOLVER_BEAN_PLACEHOLDER);
        return new DefaultLazyPropertyResolver(propertyDetector, encryptor, customResolverBeanName, bf);
    }

    /**
     * Need a copy of the environment without the Enhanced property sources to avoid circular dependencies.
     */
    private static class EnvCopy {
        StandardEnvironment copy;

        EnvCopy(ConfigurableEnvironment environment) {
            copy = new StandardEnvironment();
            environment.getPropertySources().forEach(ps -> {
                PropertySource<?> original = ps instanceof EncryptablePropertySource ? ((EncryptablePropertySource) ps).getDelegate() : ps;
                copy.getPropertySources().addLast(original);
            });
        }

        ConfigurableEnvironment get() {
            return copy;
        }
    }

}
