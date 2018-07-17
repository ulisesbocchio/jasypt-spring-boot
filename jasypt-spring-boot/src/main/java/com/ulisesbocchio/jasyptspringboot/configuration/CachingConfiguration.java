package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.caching.RefreshScopeRefreshedEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.ulisesbocchio.jasyptspringboot.caching.RefreshScopeRefreshedEventListener.REFRESHED_EVENT_CLASS;

@Configuration
@ConditionalOnClass(name = REFRESHED_EVENT_CLASS)
public class CachingConfiguration {
    @Bean
    public RefreshScopeRefreshedEventListener refreshScopeRefreshedEventListener(ConfigurableEnvironment environment) {
        return new RefreshScopeRefreshedEventListener(environment);
    }
}
