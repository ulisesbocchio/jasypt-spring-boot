package com.ulisesbocchio.jasyptspringboot.configuration;

import com.ulisesbocchio.jasyptspringboot.EnableEncryptablePropertySourcesPostProcessor;
import com.ulisesbocchio.jasyptspringboot.InterceptionMode;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.config.StringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * <p>Configuration class that registers a {@link BeanFactoryPostProcessor} that wraps all {@link PropertySource} defined in the {@link Environment}
 * with {@link com.ulisesbocchio.jasyptspringboot.wrapper.EncryptablePropertySourceWrapper} and defines a default {@link StringEncryptor} for decrypting properties
 * that can be configured through the same properties it wraps.</p>
 * <p>The {@link StringEncryptor} bean is only defined when no other
 * bean of type {@link StringEncryptor} is present in the Application Context, thus allowing for custom definition if required.</p>
 * <p>The default {@link StringEncryptor} can be configured through the following properties: </p>
 * <table border="1">
 *     <tr>
 *         <td>Key</td><td>Required</td><td>Default Value</td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.password</td><td><b>True</b></td><td> - </td>
 *     </tr>
 *     <tr>
 *         <td>jasypt.encryptor.algorithm</td><td>False</td><td>PBEWithMD5AndDES</td>
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
 *         <td>jasypt.encryptor.stringOutputType</td><td>False</td><td>base64</td>
 *     </tr>
 * </table>
 *
 * <p>For mor information about the configuration properties</p>
 * @see StringPBEConfig
 *
 * @author Ulises Bocchio
 */
@Configuration
@Import(StringEncryptorConfiguration.class)
public class EnableEncryptablePropertySourcesConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(EnableEncryptablePropertySourcesConfiguration.class);

    @Bean
    public static EnableEncryptablePropertySourcesPostProcessor enableEncryptablePropertySourcesPostProcessor(ConfigurableEnvironment environment) {
        boolean proxyPropertySources = environment.getProperty("jasypt.encryptor.proxyPropertySources", Boolean.TYPE, false);
        InterceptionMode interceptionMode = proxyPropertySources ? InterceptionMode.PROXY : InterceptionMode.WRAPPER;
        return new EnableEncryptablePropertySourcesPostProcessor(environment, interceptionMode);
    }
}
