package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySources;
import com.ulisesbocchio.jasyptspringboot.properties.PropertyFinder;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableEnumerablePropertySourceWrapper;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.env.PropertySourcesLoader;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.stream.Stream;

import static com.ulisesbocchio.jasyptspringboot.configuration.PlaceHolderInitialisation.ENCRYPTOR_BEAN_PLACEHOLDER;
import static com.ulisesbocchio.jasyptspringboot.configuration.PlaceHolderInitialisation.PROPERTY_FINDER_BEAN_PLACEHOLDER;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class EncryptablePropertySourcesInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptablePropertySourcesInitializer.class);

    @Bean
    public static EncryptablePropertySourceAnnotationBeanFactoryPostProcessor encryptablePropertySourceAnnotationPostProcessor() {
        return new EncryptablePropertySourceAnnotationBeanFactoryPostProcessor();
    }

    private static class EncryptablePropertySourceAnnotationBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
            ResourceLoader ac = new DefaultResourceLoader();
            StringEncryptor encryptor = beanFactory.getBean(env.resolveRequiredPlaceholders(ENCRYPTOR_BEAN_PLACEHOLDER), StringEncryptor.class);
            PropertyFinder propertyFinder = beanFactory.getBean(env.resolveRequiredPlaceholders(PROPERTY_FINDER_BEAN_PLACEHOLDER), PropertyFinder.class);
            MutablePropertySources propertySources = env.getPropertySources();
            Stream<AnnotationAttributes> encryptablePropertiesMetadata = getEncryptablePropertiesMetadata(beanFactory);
            encryptablePropertiesMetadata.forEach(eps -> loadEncryptablePropertySource(eps, env, ac, encryptor, propertyFinder, propertySources));
        }

        private static void loadEncryptablePropertySource(AnnotationAttributes encryptablePropertySource, ConfigurableEnvironment env, ResourceLoader resourceLoader, StringEncryptor encryptor, PropertyFinder propertyFinder, MutablePropertySources propertySources) throws BeansException {
            try {
                PropertySource ps = createPropertySource(encryptablePropertySource, env, resourceLoader, encryptor, propertyFinder);
                if (ps != null) {
                    propertySources.addLast(ps);
                    LOG.info("Created Encryptable Property Source '{}' from locations: {}", ps.getName(), Arrays.asList(encryptablePropertySource.getStringArray("value")));
                } else {
                    LOG.info("Ignoring NOT FOUND Encryptable Property Source '{}' from locations: {}", encryptablePropertySource.getString("name"), Arrays.asList(encryptablePropertySource.getStringArray("value")));
                }
            } catch (Exception e) {
                throw new ApplicationContextException("Exception Creating PropertySource", e);
            }
        }

        private static PropertySource createPropertySource(AnnotationAttributes attributes, ConfigurableEnvironment environment, ResourceLoader resourceLoader, StringEncryptor encryptor, PropertyFinder propertyFinder) throws Exception {
            String name = attributes.getString("name");
            String[] locations = attributes.getStringArray("value");
            boolean ignoreResourceNotFound = attributes.getBoolean("ignoreResourceNotFound");
            CompositePropertySource compositePropertySource = new CompositePropertySource(generateName(name));
            Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
            for (String location : locations) {
                String resolvedLocation = environment.resolveRequiredPlaceholders(location);
                Resource resource = resourceLoader.getResource(resolvedLocation);
                if (!resource.exists() && !ignoreResourceNotFound) {
                    throw new IllegalStateException("Resource not found: " + location);
                }
                PropertySourcesLoader loader = new PropertySourcesLoader();
                PropertySource propertySource = loader.load(resource, resolvedLocation, null);
                if (propertySource != null) {
                    compositePropertySource.addPropertySource(propertySource);
                }
            }
            return new EncryptableEnumerablePropertySourceWrapper<>(compositePropertySource, encryptor, propertyFinder);
        }

        private static String generateName(String name) {
            return !StringUtils.isEmpty(name) ? name : "EncryptedPropertySource#" + System.currentTimeMillis();
        }

        private static Stream<AnnotationAttributes> getEncryptablePropertiesMetadata(ConfigurableListableBeanFactory beanFactory) {
            Stream<AnnotationAttributes> source = getBeanDefinitionsForAnnotation(beanFactory, EncryptablePropertySource.class);
            Stream<AnnotationAttributes> sources = getBeanDefinitionsForAnnotation(beanFactory, EncryptablePropertySources.class)
                    .flatMap(map -> Arrays.stream((AnnotationAttributes[]) map.get("value")));
            return Stream.concat(source, sources);
        }

        private static Stream<AnnotationAttributes> getBeanDefinitionsForAnnotation(ConfigurableListableBeanFactory bf, Class<? extends Annotation> annotation) {
            return Arrays.stream(bf.getBeanNamesForAnnotation(annotation))
                    .map(bf::getBeanDefinition)
                    .filter(bd -> bd instanceof AnnotatedBeanDefinition)
                    .map(bd -> (AnnotatedBeanDefinition) bd)
                    .map(AnnotatedBeanDefinition::getMetadata)
                    .filter(md -> md.hasAnnotation(annotation.getName()))
                    .map(md -> (AnnotationAttributes) md.getAnnotationAttributes(annotation.getName()));
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }
}
