package com.ulisesbocchio.jasyptspringboot.caching;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.properties.JasyptEncryptorConfigurationProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.PropertySources;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Order(Ordered.LOWEST_PRECEDENCE - 10)
@Slf4j
public class RefreshScopeRefreshedEventListener implements ApplicationListener<ApplicationEvent>, InitializingBean {

    public static final List<String> EVENT_CLASS_NAMES = Arrays.asList(
            "org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent",
            "org.springframework.cloud.context.environment.EnvironmentChangeEvent",
            "org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent"
    );
    private final ConfigurableEnvironment environment;
    private final EncryptablePropertySourceConverter converter;
    private final List<Class<?>> eventClasses = new ArrayList<>();
    private final Map<String, Boolean> eventTriggersCache = new ConcurrentHashMap<>();
    private final JasyptEncryptorConfigurationProperties config;

    public RefreshScopeRefreshedEventListener(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter, JasyptEncryptorConfigurationProperties config) {
        this.environment = environment;
        this.converter = converter;
        this.config = config;
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEvent event) {
//        log.info("APPLICATION EVENT: {}", event.getClass().getName());
        if (this.shouldTriggerRefresh(event)) {
            log.info("Refreshing cached encryptable property sources on {}", event.getClass().getSimpleName());
            refreshCachedProperties();
            decorateNewSources();
        }
    }

    private boolean shouldTriggerRefresh(ApplicationEvent event) {
        String className = event.getClass().getName();
        if (!eventTriggersCache.containsKey(className)) {
            eventTriggersCache.put(className, eventClasses.stream().anyMatch(clazz -> this.isAssignable(clazz, event)));
        }
        return eventTriggersCache.get(className);
    }

    private void decorateNewSources() {
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
    }

    boolean isAssignable(Class<?> clazz, Object value) {
        return ClassUtils.isAssignableValue(clazz, value);
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

    private Class<?> getClassSafe(String className) {
        try {
            return ClassUtils.forName(className, null);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Stream
                .concat(EVENT_CLASS_NAMES.stream(), this.config.getRefreshedEventClasses().stream())
                .map(this::getClassSafe).filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> this.eventClasses));
    }
}
