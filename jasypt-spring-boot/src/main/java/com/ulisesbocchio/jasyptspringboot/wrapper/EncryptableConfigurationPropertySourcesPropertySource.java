package com.ulisesbocchio.jasyptspringboot.wrapper;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.util.Iterables;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.InvalidConfigurationPropertyNameException;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.PropertySource;

/**
 * <p>EncryptableConfigurationPropertySourcesPropertySource class.</p>
 *
 * @author Sergio.U.Bocchio
 * @version $Id: $Id
 */
public class EncryptableConfigurationPropertySourcesPropertySource extends PropertySource<Iterable<ConfigurationPropertySource>>
        implements EncryptablePropertySource<Iterable<ConfigurationPropertySource>> {

    private final PropertySource<Iterable<ConfigurationPropertySource>> delegate;

    /**
     * <p>Constructor for EncryptableConfigurationPropertySourcesPropertySource.</p>
     *
     * @param delegate a {@link org.springframework.core.env.PropertySource} object
     */
    public EncryptableConfigurationPropertySourcesPropertySource(PropertySource<Iterable<ConfigurationPropertySource>> delegate) {
        super(delegate.getName(), Iterables.filter(delegate.getSource(), configurationPropertySource -> !configurationPropertySource.getUnderlyingSource().getClass().equals(EncryptableConfigurationPropertySourcesPropertySource.class)));
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public PropertySource<Iterable<ConfigurationPropertySource>> getDelegate() {
        return delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void refresh() {

    }

    /** {@inheritDoc} */
    @Override
    public Object getProperty(String name) {
        ConfigurationProperty configurationProperty = findConfigurationProperty(name);
        return (configurationProperty != null) ? configurationProperty.getValue() : null;
    }

    /** {@inheritDoc} */
    @Override
    public Origin getOrigin(String name) {
        return Origin.from(findConfigurationProperty(name));
    }

    private ConfigurationProperty findConfigurationProperty(String name) {
        try {
            return findConfigurationProperty(ConfigurationPropertyName.of(name));
        }
        catch (InvalidConfigurationPropertyNameException ex) {
            // simulate non-exposed version of ConfigurationPropertyName.of(name, nullIfInvalid)
            if(ex.getInvalidCharacters().size() == 1 && ex.getInvalidCharacters().get(0).equals('.')) {
                return null;
            }
            throw ex;
        }
    }

    private ConfigurationProperty findConfigurationProperty(ConfigurationPropertyName name) {
        if (name == null) {
            return null;
        }
        for (ConfigurationPropertySource configurationPropertySource : getSource()) {
            if (!configurationPropertySource.getUnderlyingSource().getClass().equals(EncryptableConfigurationPropertySourcesPropertySource.class)) {
                ConfigurationProperty configurationProperty = configurationPropertySource.getConfigurationProperty(name);
                if (configurationProperty != null) {
                    return configurationProperty;
                }
            }
        }
        return null;
    }
}
