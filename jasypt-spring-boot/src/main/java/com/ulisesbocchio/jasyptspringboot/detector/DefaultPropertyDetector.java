package com.ulisesbocchio.jasyptspringboot.detector;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector;
import org.springframework.util.Assert;

/**
 * Default property detector that detects encrypted property values with the format "$prefix$encrypted_value$suffix"
 * Default values are "ENC(" and ")" respectively.
 *
 * @author Ulises Bocchio
 */
public class DefaultPropertyDetector implements EncryptablePropertyDetector {

    public static final String PATTERN_ENV_VARS = "^[$][{]([^}]+)[}]$";

    private String prefix = "ENC(";
    private String suffix = ")";

    public DefaultPropertyDetector() {
    }

    public DefaultPropertyDetector(String prefix, String suffix) {
        Assert.notNull(prefix, "Prefix can't be null");
        Assert.notNull(suffix, "Suffix can't be null");
        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public boolean isEncrypted(String property) {
        if (property == null) {
            return false;
        }
        final String trimmedValue = property.trim();
        return (trimmedValue.startsWith(prefix) &&
                trimmedValue.endsWith(suffix));
    }

    @Override
    public String unwrapEncryptedValue(String property) {
        String value = property.substring(
                prefix.length(),
                (property.length() - suffix.length()));

        if (value.matches(PATTERN_ENV_VARS)) {
            value = value.replaceFirst(PATTERN_ENV_VARS, "$1");
            value = System.getenv(value);
        }

        return value;
    }
}
