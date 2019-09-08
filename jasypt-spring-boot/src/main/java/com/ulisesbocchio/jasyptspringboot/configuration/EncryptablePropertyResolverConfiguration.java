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
public class EncryptablePropertyResolverConfiguration {

    private static final String ENCRYPTOR_BEAN_PROPERTY = "jasypt.encryptor.bean";
    private static final String ENCRYPTOR_BEAN_PLACEHOLDER = String.format("${%s:jasyptStringEncryptor}", ENCRYPTOR_BEAN_PROPERTY);
    private static final String DETECTOR_BEAN_PROPERTY = "jasypt.encryptor.property.detector-bean";
    private static final String DETECTOR_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyDetector}", DETECTOR_BEAN_PROPERTY);
    private static final String RESOLVER_BEAN_PROPERTY = "jasypt.encryptor.property.resolver-bean";
    private static final String RESOLVER_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyResolver}", RESOLVER_BEAN_PROPERTY);
    private static final String FILTER_BEAN_PROPERTY = "jasypt.encryptor.property.filter-bean";
    private static final String FILTER_BEAN_PLACEHOLDER = String.format("${%s:encryptablePropertyFilter}", FILTER_BEAN_PROPERTY);

    private static final String ENCRYPTOR_BEAN_NAME = "lazyJasyptStringEncryptor";
    private static final String DETECTOR_BEAN_NAME = "lazyEncryptablePropertyDetector";
    private static final String CONFIG_SINGLETON = "configPropsSingleton";
    static final String RESOLVER_BEAN_NAME = "lazyEncryptablePropertyResolver";
    static final String FILTER_BEAN_NAME = "lazyEncryptablePropertyFilter";

    @Bean
    public EnvCopy envCopy(final ConfigurableEnvironment environment) {
        return new EnvCopy(environment);
    }

    @Bean(name = ENCRYPTOR_BEAN_NAME)
    public StringEncryptor stringEncryptor(
                @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final EnvCopy envCopy,
                final BeanFactory bf) {
        final String customEncryptorBeanName = envCopy.get().resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(ENCRYPTOR_BEAN_PROPERTY);
        return new DefaultLazyEncryptor(envCopy.get(), customEncryptorBeanName, isCustom, bf);
    }

    @Bean(name = DETECTOR_BEAN_NAME)
    public EncryptablePropertyDetector encryptablePropertyDetector(
                @SuppressWarnings({"SpringJavaInjectionPointsAutowiringInspection"}) final EnvCopy envCopy,
                final BeanFactory bf) {
        final String prefix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.prefix:ENC(}");
        final String suffix = envCopy.get().resolveRequiredPlaceholders("${jasypt.encryptor.property.suffix:)}");
        final String customDetectorBeanName = envCopy.get().resolveRequiredPlaceholders(DETECTOR_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(DETECTOR_BEAN_PROPERTY);
        return new DefaultLazyPropertyDetector(prefix, suffix, customDetectorBeanName, isCustom, bf);
    }

    @Bean(name = CONFIG_SINGLETON)
    public Singleton<JasyptEncryptorConfigurationProperties> configProps(
                @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final EnvCopy envCopy,
                final ConfigurableBeanFactory bf) {
        return new Singleton<>(() -> {
            final BindHandler handler = new IgnoreErrorsBindHandler(BindHandler.DEFAULT);
            final MutablePropertySources propertySources = envCopy.get().getPropertySources();
            final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources),
                        new PropertySourcesPlaceholdersResolver(propertySources),
                        ApplicationConversionService.getSharedInstance(), bf::copyRegisteredEditorsTo);
            final JasyptEncryptorConfigurationProperties config = new JasyptEncryptorConfigurationProperties();

            final ResolvableType type = ResolvableType.forClass(JasyptEncryptorConfigurationProperties.class);
            final Annotation annotation = AnnotationUtils.findAnnotation(JasyptEncryptorConfigurationProperties.class,
                        ConfigurationProperties.class);
            final Annotation[] annotations = new Annotation[] {annotation};
            final Bindable<?> target = Bindable.of(type).withExistingValue(config).withAnnotations(annotations);

            binder.bind("jasypt.encryptor", target, handler);
            return config;
        });
    }

    @Bean(name = FILTER_BEAN_NAME)
    public EncryptablePropertyFilter encryptablePropertyFilter(
                @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final EnvCopy envCopy,
                final ConfigurableBeanFactory bf,
                @Qualifier(CONFIG_SINGLETON) final Singleton<JasyptEncryptorConfigurationProperties> configProps) {
        final String customFilterBeanName = envCopy.get().resolveRequiredPlaceholders(FILTER_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(FILTER_BEAN_PROPERTY);
        final FilterConfigurationProperties filterConfig = configProps.get().getProperty().getFilter();
        return new DefaultLazyPropertyFilter(filterConfig.getIncludeSources(), filterConfig.getExcludeSources(),
                    filterConfig.getIncludeNames(), filterConfig.getExcludeNames(), customFilterBeanName, isCustom, bf);
    }

    @Bean(name = RESOLVER_BEAN_NAME)
    public EncryptablePropertyResolver encryptablePropertyResolver(
                @Qualifier(DETECTOR_BEAN_NAME) final EncryptablePropertyDetector propertyDetector,
                @Qualifier(ENCRYPTOR_BEAN_NAME) final StringEncryptor encryptor, final BeanFactory bf,
                @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") final EnvCopy envCopy, final ConfigurableEnvironment environment) {
        final String customResolverBeanName = envCopy.get().resolveRequiredPlaceholders(RESOLVER_BEAN_PLACEHOLDER);
        final boolean isCustom = envCopy.get().containsProperty(RESOLVER_BEAN_PROPERTY);
        return new DefaultLazyPropertyResolver(propertyDetector, encryptor, customResolverBeanName, isCustom, bf, environment);
    }

    /**
     * Need a copy of the environment without the Enhanced property sources to avoid circular dependencies.
     */
    private static class EnvCopy {
        StandardEnvironment copy;

        EnvCopy(final ConfigurableEnvironment environment) {
            copy = new StandardEnvironment();
            environment.getPropertySources().forEach(ps -> {
                final PropertySource<?> original = ps instanceof EncryptablePropertySource
                            ? ((EncryptablePropertySource) ps).getDelegate()
                            : ps;
                copy.getPropertySources().addLast(original);
            });
        }

        ConfigurableEnvironment get() {
            return copy;
        }
    }

}
