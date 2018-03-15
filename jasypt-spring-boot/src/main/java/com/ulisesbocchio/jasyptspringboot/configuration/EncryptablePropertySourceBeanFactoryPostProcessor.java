package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySources;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableEnumerablePropertySourceWrapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.context.ApplicationContextException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertyResolverConfiguration.RESOLVER_BEAN_NAME;

/**
 * @author Ulises Bocchio
 */
@Slf4j
public class EncryptablePropertySourceBeanFactoryPostProcessor implements BeanFactoryPostProcessor, Ordered {

    private ConfigurableEnvironment env;

    public EncryptablePropertySourceBeanFactoryPostProcessor(ConfigurableEnvironment env) {
        this.env = env;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ResourceLoader ac = new DefaultResourceLoader();
        MutablePropertySources propertySources = env.getPropertySources();
        Stream<AnnotationAttributes> encryptablePropertySourcesMetadata = getEncryptablePropertySourcesMetadata(beanFactory);
        EncryptablePropertyResolver propertyResolver = beanFactory.getBean(RESOLVER_BEAN_NAME, EncryptablePropertyResolver.class);
        List<PropertySourceLoader> loaders = initPropertyLoaders();
        encryptablePropertySourcesMetadata.forEach(eps -> loadEncryptablePropertySource(eps, env, ac, propertyResolver, propertySources, loaders));
    }

    private List<PropertySourceLoader> initPropertyLoaders() {
        return SpringFactoriesLoader.loadFactories(PropertySourceLoader.class, getClass().getClassLoader());
    }

    private void loadEncryptablePropertySource(AnnotationAttributes encryptablePropertySource, ConfigurableEnvironment env, ResourceLoader resourceLoader, EncryptablePropertyResolver resolver, MutablePropertySources propertySources, List<PropertySourceLoader> loaders) throws BeansException {
        try {
            PropertySource ps = createPropertySource(encryptablePropertySource, env, resourceLoader, resolver, loaders);
            propertySources.addLast(ps);
            log.info("Created Encryptable Property Source '{}' from locations: {}", ps.getName(), Arrays.asList(encryptablePropertySource.getStringArray("value")));
        } catch (Exception e) {
            throw new ApplicationContextException("Exception Creating PropertySource", e);
        }
    }

    private PropertySource createPropertySource(AnnotationAttributes attributes, ConfigurableEnvironment environment, ResourceLoader resourceLoader, EncryptablePropertyResolver resolver, List<PropertySourceLoader> loaders) throws Exception {
        String name = generateName(attributes.getString("name"));
        String[] locations = attributes.getStringArray("value");
        boolean ignoreResourceNotFound = attributes.getBoolean("ignoreResourceNotFound");
        CompositePropertySource compositePropertySource = new CompositePropertySource(name);
        Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
        for (String location : locations) {
            String resolvedLocation = environment.resolveRequiredPlaceholders(location);
            Resource resource = resourceLoader.getResource(resolvedLocation);
            if (!resource.exists()) {
                if (!ignoreResourceNotFound) {
                    throw new IllegalStateException(String.format("Encryptable Property Source '%s' from location: %s Not Found", name, resolvedLocation));
                } else {
                    log.info("Ignoring NOT FOUND Encryptable Property Source '{}' from locations: {}", name, resolvedLocation);
                }
            } else {
                String actualName = name + "#" + resolvedLocation;
                loadPropertySource(loaders, resource, actualName)
                        .ifPresent(psources -> psources.forEach(compositePropertySource::addPropertySource));
            }
        }
        return new EncryptableEnumerablePropertySourceWrapper<>(compositePropertySource, resolver);
    }

    private String generateName(String name) {
        return !StringUtils.isEmpty(name) ? name : "EncryptedPropertySource#" + System.currentTimeMillis();
    }

    private Stream<AnnotationAttributes> getEncryptablePropertySourcesMetadata(ConfigurableListableBeanFactory beanFactory) {
        Stream<AnnotationAttributes> source = getBeanDefinitionsForAnnotation(beanFactory, com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource.class);
        Stream<AnnotationAttributes> sources = getBeanDefinitionsForAnnotation(beanFactory, EncryptablePropertySources.class)
                .flatMap(map -> Arrays.stream((AnnotationAttributes[]) map.get("value")));
        return Stream.concat(source, sources);
    }

    private Stream<AnnotationAttributes> getBeanDefinitionsForAnnotation(ConfigurableListableBeanFactory bf, Class<? extends Annotation> annotation) {
        return Arrays.stream(bf.getBeanNamesForAnnotation(annotation))
                .map(bf::getBeanDefinition)
                .filter(bd -> bd instanceof AnnotatedBeanDefinition)
                .map(bd -> (AnnotatedBeanDefinition) bd)
                .map(AnnotatedBeanDefinition::getMetadata)
                .filter(md -> md.hasAnnotation(annotation.getName()))
                .map(md -> (AnnotationAttributes) md.getAnnotationAttributes(annotation.getName()));
    }

    private Optional<List<PropertySource<?>>> loadPropertySource(List<PropertySourceLoader> loaders, Resource resource, String sourceName) throws IOException {
        return Optional.of(resource)
                .filter(this::isFile)
                .map(res -> loaders.stream()
                        .filter(loader -> canLoadFileExtension(loader, resource))
                        .findFirst()
                        .map(loader -> load(loader, sourceName, resource))
                        .orElse(null));
    }

    @SneakyThrows
    private List<PropertySource<?>> load(PropertySourceLoader loader, String sourceName, Resource resource) {
        return loader.load(sourceName, resource);
    }

    private boolean canLoadFileExtension(PropertySourceLoader loader, Resource resource) {
        return Arrays.stream(loader.getFileExtensions())
                .anyMatch(extension -> resource.getFilename().toLowerCase().endsWith("." + extension.toLowerCase()));
    }

    private boolean isFile(Resource resource) {
        return resource != null && resource.exists() && StringUtils
                .hasText(StringUtils.getFilenameExtension(resource.getFilename()));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
