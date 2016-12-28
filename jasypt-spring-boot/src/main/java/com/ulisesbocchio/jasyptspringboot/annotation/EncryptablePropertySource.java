package com.ulisesbocchio.jasyptspringboot.annotation;

import com.ulisesbocchio.jasyptspringboot.configuration.EncryptablePropertySourceConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Encryptable version of Spring {@link org.springframework.context.annotation.PropertySource}
 *
 * @author Ulises Bocchio
 * @see org.springframework.context.annotation.PropertySource
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EncryptablePropertySources.class)
@Import(EncryptablePropertySourceConfiguration.class)
public @interface EncryptablePropertySource {
    /**
     * Indicate the name of this property source. If omitted, a name
     * will be generated based on the description of the underlying
     * resource.
     *
     * @see org.springframework.core.env.PropertySource#getName()
     * @see org.springframework.core.io.Resource#getDescription()
     */
    String name() default "";

    /**
     * Indicate the resource location(s) of the properties file to be loaded.
     * For example, {@code "classpath:/com/myco/app.properties"} or
     * {@code "file:/path/to/file"}.
     * <p>Resource location wildcards (e.g. *&#42;/*.properties) are not permitted; each
     * location must evaluate to exactly one {@code .properties} resource.
     * <p>${...} placeholders will be resolved against any/all property sources already
     * registered with the {@code Environment}. See {@linkplain EncryptablePropertySource above} for
     * examples.
     * <p>Each location will be added to the enclosing {@code Environment} as its own
     * property source, and in the order declared.
     */
    String[] value();

    /**
     * Indicate if failure to find the a {@link #value() property resource} should be
     * ignored.
     * <p>{@code true} is appropriate if the properties file is completely optional.
     * Default is {@code false}.
     */
    boolean ignoreResourceNotFound() default false;

}
