package com.ulisesbocchio.jasyptspringboot.condition;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.ConfigurationCondition;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Condition that checks whether the specified bean by placeholder like: ${bean.name:defaultName} exists.
 * Spring's ConditionalOnMissingBean does not support placeholder resolution.
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class OnMissingBeanCondition extends SpringBootCondition implements ConfigurationCondition {


    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ConditionOutcome getMatchOutcome(@NonNull ConditionContext context, AnnotatedTypeMetadata metadata) {
        Map<String, Object> beanAttributes = metadata.getAnnotationAttributes(Bean.class.getName());
        if (beanAttributes == null) {
            throw new IllegalStateException("OnMissingBeanCondition can't detect bean attributes!");
        }
        String beanName = ((String[]) beanAttributes.get("name"))[0];
        if (!StringUtils.hasLength(beanName)) {
            throw new IllegalStateException("OnMissingBeanCondition can't detect bean name!");
        }
        boolean missingBean = context.getBeanFactory() != null && !context.getBeanFactory().containsBean(context.getEnvironment().resolveRequiredPlaceholders(beanName));
        return missingBean ? ConditionOutcome.match(beanName + " not found") : ConditionOutcome.noMatch(beanName + " found");
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public ConfigurationPhase getConfigurationPhase() {
        return ConfigurationPhase.REGISTER_BEAN;
    }
}
