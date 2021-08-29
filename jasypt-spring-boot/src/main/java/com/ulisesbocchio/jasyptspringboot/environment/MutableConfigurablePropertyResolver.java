package com.ulisesbocchio.jasyptspringboot.environment;

import lombok.experimental.Delegate;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;

import java.util.function.Function;

/**
 * @author boccs002
 */
public class MutableConfigurablePropertyResolver implements ConfigurablePropertyResolver {

    private final Function<MutablePropertySources, ConfigurablePropertyResolver> factory;
    @Delegate
    private ConfigurablePropertyResolver delegate;

    public MutableConfigurablePropertyResolver(MutablePropertySources propertySources, Function<MutablePropertySources, ConfigurablePropertyResolver> factory) {
        this.factory = factory;
        this.delegate = factory.apply(propertySources);
    }

    public void setPropertySources(MutablePropertySources propertySources) {
        this.delegate = this.factory.apply(propertySources);
    }
}
