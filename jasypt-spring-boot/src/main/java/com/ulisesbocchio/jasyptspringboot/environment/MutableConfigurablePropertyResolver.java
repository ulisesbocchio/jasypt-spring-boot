package com.ulisesbocchio.jasyptspringboot.environment;

import lombok.experimental.Delegate;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.core.env.MutablePropertySources;

import java.util.function.Function;

/**
 * <p>MutableConfigurablePropertyResolver class.</p>
 *
 * @author boccs002
 * @version $Id: $Id
 */
public class MutableConfigurablePropertyResolver implements ConfigurablePropertyResolver {

    private final Function<MutablePropertySources, ConfigurablePropertyResolver> factory;
    @Delegate
    private ConfigurablePropertyResolver delegate;

    /**
     * <p>Constructor for MutableConfigurablePropertyResolver.</p>
     *
     * @param propertySources a {@link org.springframework.core.env.MutablePropertySources} object
     * @param factory a {@link java.util.function.Function} object
     */
    public MutableConfigurablePropertyResolver(MutablePropertySources propertySources, Function<MutablePropertySources, ConfigurablePropertyResolver> factory) {
        this.factory = factory;
        this.delegate = factory.apply(propertySources);
    }

    /**
     * <p>setPropertySources.</p>
     *
     * @param propertySources a {@link org.springframework.core.env.MutablePropertySources} object
     */
    public void setPropertySources(MutablePropertySources propertySources) {
        this.delegate = this.factory.apply(propertySources);
    }
}
