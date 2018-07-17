package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.ClassUtils;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RefreshScopeRefreshedEventListener implements ApplicationListener<ApplicationEvent> {

    public static final String REFRESHED_EVENT_CLASS = "org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent";
    private final ConfigurableEnvironment environment;

    public RefreshScopeRefreshedEventListener(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEvent event) {
        ClassLoader classLoader = this.getClass().getClassLoader();
        boolean refreshedPresent = ClassUtils.isPresent(REFRESHED_EVENT_CLASS, classLoader);
        if (refreshedPresent) {
            Class<?> refreshedClass = ClassUtils.forName(REFRESHED_EVENT_CLASS, classLoader);
            if (ClassUtils.isAssignableValue(refreshedClass, event)) {
                log.info("Refreshing cached encryptable property sources");
                refreshCachedProperties();
            }
        }
    }

    private void refreshCachedProperties() {
        PropertySources propertySources = environment.getPropertySources();
        propertySources.forEach(this::refreshPropertySource);
    }

    private void refreshPropertySource(PropertySource<?> propertySource) {
        if (propertySource instanceof CompositePropertySource) {
            CompositePropertySource cps = (CompositePropertySource) propertySource;
            cps.getPropertySources().forEach(this::refreshPropertySource);
        } else if (propertySource instanceof EncryptablePropertySource){
            EncryptablePropertySource eps = (EncryptablePropertySource) propertySource;
            eps.refresh();
        }
    }
}
