package com.ulisesbocchio.jasyptspringboot.annotation;

import com.ulisesbocchio.jasyptspringboot.EnableEncryptablePropertySourcesConfiguration;
import com.ulisesbocchio.jasyptspringboot.EnableEncryptablePropertySourcesPostProcessor;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceWrapper;
import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.lang.annotation.*;

/**
 * <p>Annotation that enables Jasypt for properties decryption by annotating {@link Configuration} classes.
 * Only one occurrence of this annotation is needed.</p>
 *
 * <p>This works well in conjunction with the {@link org.springframework.context.annotation.PropertySource} annotation.
 * For instance:</p>
 * <pre>
 *  {@literal @SpringBootApplication}
 *  {@literal @EnableEncryptableProperties}
 *  {@literal @PropertySource(name="EncryptedProperties", "classpath:app.properties")}
 *   public class MySpringBootApp {
 *      public static void main(String[] args) {
 *          SpringApplication.run(MySpringBootApp.class, args);
 *      }
 *   }
 * </pre>
 * <p>The above code will then enable encryptable properties within all {@link PropertySource}s defined in the environment,
 * not only the ones defined with the {@link org.springframework.context.annotation.PropertySource} annotation, but also
 * all system properties, command line properties, and those auto-magically picked up from application.properties and application.yml
 * if they exist.<p/>
 *
 * <p>This Configuration class basically registers a {@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>
 * For more information on how to declare encrypted properties, encrypt them, and encryption configuration go to  <a href="http://jasypt.org">http://jasypt.org</a>
 * </p>
 *
 * @see EnableEncryptablePropertySourcesConfiguration
 * @see EnableEncryptablePropertySourcesPostProcessor
 * @see EncryptablePropertySourceWrapper
 * @see org.springframework.context.annotation.PropertySource
 *
 * @author Ulises Bocchio
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Configuration
@Import({EnableEncryptablePropertySourcesConfiguration.class})
public @interface EnableEncryptableProperties {
}
