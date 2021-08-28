package com.ulisesbocchio.jasyptspringboot.environment;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

public interface EncryptableEnvironment extends ConfigurableEnvironment {
    MutablePropertySources getOriginalPropertySources();
    void setEncryptablePropertySources(MutablePropertySources propertySources);
}
