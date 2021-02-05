package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import com.ulisesbocchio.jasyptspringboot.caching.RefreshScopeRefreshedEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class CachingConfiguration {
    @Bean
    @ConditionalOnClass(name = {RefreshScopeRefreshedEventListener.REFRESHED_EVENT_CLASS, RefreshScopeRefreshedEventListener.ENVIRONMENT_EVENT_CLASS})
    public RefreshScopeRefreshedEventListener refreshScopeRefreshedEventListener(ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        return new RefreshScopeRefreshedEventListener(environment, converter);
    }
}
