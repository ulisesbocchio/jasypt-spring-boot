package com.ulisesbocchio.jasyptspringboot.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import java.util.stream.Stream;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class PlaceHolderInitialisation {

    public static final String ENCRYPTOR_BEAN_PLACEHOLDER = "${jasypt.encryptor.bean:jasyptStringEncryptor}";
    public static final String PROPERTY_FINDER_BEAN_PLACEHOLDER = "${jasypt.propertyFinder.bean:jasyptPropertyFinder}";

    private static final Logger LOG = LoggerFactory.getLogger(PlaceHolderInitialisation.class);

    @Bean
    public static BeanNamePlaceholderRegistryPostProcessor beanNamePlaceholderRegistryPostProcessor(Environment environment) {
        return new BeanNamePlaceholderRegistryPostProcessor(environment);
    }

    /**
     * Bean Definition Registry Post Processor that looks for placeholders in bean names and resolves them, re-defining those beans
     * with the new names.
     */
    static class BeanNamePlaceholderRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor, Ordered {

        private Environment environment;

        private BeanNamePlaceholderRegistryPostProcessor(Environment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            DefaultListableBeanFactory bf = (DefaultListableBeanFactory) registry;
            Stream.of(bf.getBeanDefinitionNames())
                // Look for beans with one of the placeholders we support
                .filter(name -> name.equals(ENCRYPTOR_BEAN_PLACEHOLDER) || name.equals(PROPERTY_FINDER_BEAN_PLACEHOLDER))
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
}
