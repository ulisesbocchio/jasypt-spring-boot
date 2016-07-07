package com.ulisesbocchio.jasyptspringboot;

import java.io.FileNotFoundException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySources;

/**
 * @author Ulises Bocchio
 */
@Configuration
@Import(StringEncryptorConfiguration.class)
public class EncryptablePropertySourcesInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(EncryptablePropertySourcesInitializer.class);

    @Bean
    public static BeanFactoryPostProcessor encrytablePropertySourceAnnotationPostProcessor() {
        return new EncryptablePropertySourceAnnotationBeanFactoryPostProcessor();
    }

    private static class EncryptablePropertySourceAnnotationBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
            ConfigurableEnvironment env = beanFactory.getBean(ConfigurableEnvironment.class);
            ApplicationContext ac = new StaticApplicationContext();//Figure out how to get this from beanFactory
            StringEncryptor encryptor = beanFactory.getBean(StringEncryptor.class);
            MutablePropertySources propertySources = env.getPropertySources();
            List<AnnotationAttributes> encryptablePropertiesMetadata = getEncryptablePropertiesMetadata(beanFactory);
            for (AnnotationAttributes annotationAttributes : encryptablePropertiesMetadata) {
              loadEncryptablePropertySource(annotationAttributes, env, ac, encryptor, propertySources);
            }
        }

        private static void loadEncryptablePropertySource(AnnotationAttributes encryptablePropertySource, ConfigurableEnvironment env, ApplicationContext ac, StringEncryptor encryptor, MutablePropertySources propertySources) throws BeansException {
            try {
                PropertySource ps = createPropertySource(encryptablePropertySource, env, ac, encryptor);
                if(ps != null) {
                    propertySources.addLast(ps);
                }
            } catch (Exception e) {
                throw new ApplicationContextException("Exception Creating PropertySource", e);
            }
        }

        private static PropertySource createPropertySource(AnnotationAttributes propertySource, ConfigurableEnvironment environment, ResourceLoader resourceLoader, StringEncryptor encryptor) throws Exception {
            MapPropertySource rps = null;
            String name = propertySource.getString("name");
            String[] locations = propertySource.getStringArray("value");
            boolean ignoreResourceNotFound = propertySource.getBoolean("ignoreResourceNotFound");
            Assert.isTrue(locations.length > 0, "At least one @PropertySource(value) location is required");
            for (String location : locations) {
                try {
                    String resolvedLocation = environment.resolveRequiredPlaceholders(location);
                    Resource resource = resourceLoader.getResource(resolvedLocation);
                    rps = (StringUtils.hasText(name) ?
                            new ResourcePropertySource(name, resource) : new ResourcePropertySource(resource));
                    rps = new EncryptableMapPropertySourceWrapper(rps, encryptor);
                } catch (IllegalArgumentException ex) {
                    // from resolveRequiredPlaceholders
                    if (!ignoreResourceNotFound) {
                        throw ex;
                    }
                } catch (FileNotFoundException ex) {
                    // from resolveRequiredPlaceholders
                    if (!ignoreResourceNotFound) {
                        throw ex;
                    }
                }
            }
            return rps;
        }

        private static List<AnnotationAttributes> getEncryptablePropertiesMetadata(ConfigurableListableBeanFactory beanFactory) {
          List<AnnotationAttributes> source = getBeanDefinitionsForAnnotation(beanFactory, EncryptablePropertySource.class);
          List<AnnotationAttributes> sources = getBeanDefinitionsForAnnotation(beanFactory, EncryptablePropertySources.class);
          List<AnnotationAttributes> flatSources = new ArrayList<AnnotationAttributes>();
          for (AnnotationAttributes annotationAttributes : sources) {
            if(annotationAttributes.containsKey("value")) {
              flatSources.addAll(Arrays.asList((AnnotationAttributes[]) annotationAttributes.get("value")));
            }
          }
          List<AnnotationAttributes> concat = new ArrayList<AnnotationAttributes>(source);
          concat.addAll(flatSources);
          return concat;
        }

        private static List<AnnotationAttributes> getBeanDefinitionsForAnnotation(ConfigurableListableBeanFactory bf, Class<? extends Annotation> annotation) {
          List<AnnotationAttributes> annotationAttributes = new ArrayList<AnnotationAttributes>();
          for (String beanName : bf.getBeanNamesForAnnotation(annotation)) {
            BeanDefinition bd = bf.getBeanDefinition(beanName);
            if(bd instanceof AnnotatedGenericBeanDefinition) {
              AnnotatedGenericBeanDefinition abd = (AnnotatedGenericBeanDefinition) bd;
              AnnotationMetadata metadata = abd.getMetadata();
              if (metadata.hasAnnotation(annotation.getName())) {
                AnnotationAttributes attributes = (AnnotationAttributes) metadata.getAnnotationAttributes(annotation.getName());
                annotationAttributes.add(attributes);
              }
            }
          }
          return annotationAttributes;
        }
    }
}
