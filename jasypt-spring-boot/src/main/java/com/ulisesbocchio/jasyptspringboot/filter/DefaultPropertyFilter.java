package com.ulisesbocchio.jasyptspringboot.filter;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Default Strategy for contemplating properties for decryption based on the following constructor args:
 * <p>
 * <b>includeSourceNames:</b> To include property sources by name, provide a list of regex. If set, property sources that don't match will be excluded
 * <b>includePropertyNames:</b> To include properties by name, provide a list of regex. If set, properties that don't match will be excluded
 * </p>
 *
 * @author Ulises Bocchio
 * @version $Id: $Id
 */
public class DefaultPropertyFilter implements EncryptablePropertyFilter {

    private final List<Pattern> includeSourcePatterns;
    private final List<Pattern> excludeSourcePatterns;
    private final List<Pattern> includePropertyPatterns;
    private final List<Pattern> excludePropertyPatterns;

    /**
     * <p>Constructor for DefaultPropertyFilter.</p>
     */
    public DefaultPropertyFilter() {
        includeSourcePatterns = null;
        includePropertyPatterns = null;
        excludeSourcePatterns = null;
        excludePropertyPatterns = null;
    }

    /**
     * <p>Constructor for DefaultPropertyFilter.</p>
     *
     * @param includeSourceNames a {@link java.util.List} object
     * @param excludeSourceNames a {@link java.util.List} object
     * @param includePropertyNames a {@link java.util.List} object
     * @param excludePropertyNames a {@link java.util.List} object
     */
    public DefaultPropertyFilter(List<String> includeSourceNames, List<String> excludeSourceNames, List<String> includePropertyNames, List<String> excludePropertyNames) {
        this.includeSourcePatterns = includeSourceNames == null ? null : includeSourceNames.stream().map(Pattern::compile).collect(Collectors.toList());
        this.excludeSourcePatterns = excludeSourceNames == null ? null : excludeSourceNames.stream().map(Pattern::compile).collect(Collectors.toList());
        this.includePropertyPatterns = includePropertyNames == null ? null : includePropertyNames.stream().map(Pattern::compile).collect(Collectors.toList());
        this.excludePropertyPatterns = excludePropertyNames == null ? null : excludePropertyNames.stream().map(Pattern::compile).collect(Collectors.toList());
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldInclude(PropertySource<?> source, String name) {
        if (isIncludeAll()) {
            return true;
        }

        if (isMatch(source.getName(), excludeSourcePatterns) || isMatch(name, excludePropertyPatterns)) {
            return false;
        }

        return isIncludeUnset() || isMatch(source.getName(), includeSourcePatterns) || isMatch(name, includePropertyPatterns);

    }

    private boolean isIncludeAll() {
        return isIncludeUnset() && isExcludeUnset();
    }

    private boolean isIncludeUnset() {
        return isEmpty(includeSourcePatterns) && isEmpty(includePropertyPatterns);
    }

    private boolean isExcludeUnset() {
        return isEmpty(excludeSourcePatterns) && isEmpty(excludePropertyPatterns);
    }

    private boolean isEmpty(List<Pattern> patterns) {
        return patterns == null || patterns.isEmpty();
    }

    private boolean isMatch(String name, List<Pattern> patterns) {
        return name != null && !isEmpty(patterns) && patterns.stream().anyMatch(p -> p.matcher(name).matches());
    }
}
