package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.config.StringPBEConfig;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * <p>Configuration class that registers a {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor} that wraps all {@link org.springframework.core.env.PropertySource} defined in the {@link org.springframework.core.env.Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link org.jasypt.encryption.StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>The {@link org.jasypt.encryption.StringEncryptor} bean is only defined when no other
 * bean of type {@link org.jasypt.encryption.StringEncryptor} is present in the Application Context, thus allowing for custom definition if required.</p>
 * <p>The default {@link org.jasypt.encryption.StringEncryptor} can be configured through the following properties: </p>
 * <table border="1">
 *     <tr>
 *         <td>Key</td><td>Required</td><td>Default Value</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.password</td><td><b>True</b></td><td> - </td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.algorithm</td><td>False</td><td>PBEWITHHMACSHA512ANDAES_256</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.keyObtentionIterations</td><td>False</td><td>1000</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.poolSize</td><td>False</td><td>1</td>
 *     </tr><tr>
 *         <td>jasypt.encryptor.providerName</td><td>False</td><td>SunJCE</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.saltGeneratorClassname</td><td>False</td><td>org.jasypt.salt.RandomSaltGenerator</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.ivGeneratorClassname</td><td>False</td><td>org.jasypt.iv.RandomIvGenerator</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.stringOutputType</td><td>False</td><td>base64</td>
 *     </tr>
 * </table>
 *
 * <p>For mor information about the configuration properties</p>
 *
 * @author Ulises Bocchio
 * @see StringPBEConfig
 * @version $Id: $Id
 */
@Configuration
@Import({EncryptablePropertyResolverConfiguration.class, CachingConfiguration.class})
@Slf4j
public class EnableEncryptablePropertiesConfiguration {

    /**
     * <p>enableEncryptablePropertySourcesPostProcessor.</p>
     *
     * @param environment a {@link org.springframework.core.env.ConfigurableEnvironment} object
     * @param converter a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertySourceConverter} object
     * @return a {@link com.ulisesbocchio.jasyptspringboot.configuration.EnableEncryptablePropertiesBeanFactoryPostProcessor} object
     */
    @Bean
    public static EnableEncryptablePropertiesBeanFactoryPostProcessor enableEncryptablePropertySourcesPostProcessor(final ConfigurableEnvironment environment, EncryptablePropertySourceConverter converter) {
        return new EnableEncryptablePropertiesBeanFactoryPostProcessor(environment, converter);
    }
}
