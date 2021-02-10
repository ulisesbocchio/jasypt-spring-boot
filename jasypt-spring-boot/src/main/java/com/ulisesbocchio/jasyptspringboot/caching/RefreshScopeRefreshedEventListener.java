package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.*;
import org.springframework.util.ClassUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RefreshScopeRefreshedEventListener implements ApplicationListener<ApplicationEvent> {

    public static final String REFRESHED_EVENT_CLASS = "org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent";
    public static final String ENVIRONMENT_EVENT_CLASS = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";
    private final ConfigurableEnvironment environment;
    private final EncryptablePropertySourceConverter converter;
    private Boolean cloudDependencyExists = true;

    public RefreshScopeRefreshedEventListener(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        this.environment = environment;
        this.converter = converter;
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEvent event) {
        if (isAssignable(ENVIRONMENT_EVENT_CLASS, event) || isAssignable(REFRESHED_EVENT_CLASS, event)) {
            log.info("Refreshing cached encryptable property sources");
            refreshCachedProperties();
            decorateNewSources();
        }
    }

    private void decorateNewSources() {
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
    }

    boolean isAssignable(String className, Object value) {
        try {
            return cloudDependencyExists && ClassUtils.isAssignableValue(ClassUtils.forName(className, null), value);
        } catch (ClassNotFoundException e) {
            cloudDependencyExists = false;
            return false;
        }
    }

    private void refreshCachedProperties() {
        PropertySources propertySources = environment.getPropertySources();
        propertySources.forEach(this::refreshPropertySource);
    }

    @SuppressWarnings("rawtypes")
    private void refreshPropertySource(PropertySource<?> propertySource) {
        if (propertySource instanceof CompositePropertySource) {
            CompositePropertySource cps = (CompositePropertySource) propertySource;
            cps.getPropertySources().forEach(this::refreshPropertySource);
        } else if (propertySource instanceof EncryptablePropertySource) {
            EncryptablePropertySource eps = (EncryptablePropertySource) propertySource;
            eps.refresh();
        }
    }
}
