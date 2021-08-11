package com.ulisesbocchio.jasyptspringboot.caching;

import java.util.ArrayList;
import java.util.List;

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
    private final List<Class<?>> refreshedEventClasses;

    public RefreshScopeRefreshedEventListener(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        this.environment = environment;
        this.converter = converter;
        final String tmp = environment.getProperty("jasypt.refreshed_event_classes");
        String[] classes;
        if (tmp == null) {
           classes = new String[] {
               REFRESHED_EVENT_CLASS, ENVIRONMENT_EVENT_CLASS
           };
        } else {
           classes = tmp.split("[ ,;]+");
        }
        refreshedEventClasses = new ArrayList<>();
        for (String cl : classes) {
            try {
                refreshedEventClasses.add(ClassUtils.forName(cl, null));
            } catch (ClassNotFoundException e) {
              log.warn("ignore {}", cl);
            }
        }
    }

    @Override
    @SneakyThrows
    public void onApplicationEvent(ApplicationEvent event) {
        if (isAssignable(event)) {
            log.info("Refreshing cached encryptable property sources");
            refreshCachedProperties();
            decorateNewSources();
        }
    }

    private void decorateNewSources() {
        MutablePropertySources propSources = environment.getPropertySources();
        converter.convertPropertySources(propSources);
    }

    boolean isAssignable(Object value) {
      for (Class<?> cl : refreshedEventClasses) {
          if (cl.isInstance(value)) {
              return true;
          }
      }
      return false;
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
