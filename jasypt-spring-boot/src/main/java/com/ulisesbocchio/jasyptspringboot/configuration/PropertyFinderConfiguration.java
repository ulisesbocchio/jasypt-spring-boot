package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.properties.JasyptPropertyFinder;
import com.ulisesbocchio.jasyptspringboot.properties.PropertyFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

import static com.ulisesbocchio.jasyptspringboot.configuration.PlaceHolderInitialisation.PROPERTY_FINDER_BEAN_PLACEHOLDER;

/**
 * @author Ulises Bocchio
 */
@Configuration
public class PropertyFinderConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyFinderConfiguration.class);

    @Conditional(OnMissingPropertyFinderBean.class)
    @Bean(name = PROPERTY_FINDER_BEAN_PLACEHOLDER)
    public PropertyFinder propertyFinder(Environment environment) {
        String propertyFinderBeanName = environment.resolveRequiredPlaceholders(PROPERTY_FINDER_BEAN_PLACEHOLDER);
        LOG.info("Property Finder custom Bean not found with name '{}'. Initializing default JasyptPropertyFinder",
                propertyFinderBeanName);
        return new JasyptPropertyFinder();
    }

    /**
     * Condition that checks whether the StringEncryptor specified by placeholder: {@link PlaceHolderInitialisation#PROPERTY_FINDER_BEAN_PLACEHOLDER} exists.
     * ConditionalOnMissingBean does not support placeholder resolution.
     */
    private static class OnMissingPropertyFinderBean implements ConfigurationCondition {

        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !context.getBeanFactory().containsBean(context.getEnvironment().resolveRequiredPlaceholders(PROPERTY_FINDER_BEAN_PLACEHOLDER));
        }

        @Override
        public ConfigurationPhase getConfigurationPhase() {
            return ConfigurationPhase.REGISTER_BEAN;
        }
    }

}
