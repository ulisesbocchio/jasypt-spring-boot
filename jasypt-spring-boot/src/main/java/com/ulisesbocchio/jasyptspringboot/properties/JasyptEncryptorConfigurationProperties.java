package com.ulisesbocchio.jasyptspringboot.properties;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.BindHandler;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.PropertySourcesPlaceholdersResolver;
import org.springframework.boot.context.properties.bind.handler.IgnoreErrorsBindHandler;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

/**
 * Partially used to load {@link EncryptablePropertyFilter} config.
 *
 * @author Ulises Bocchio
 */
@SuppressWarnings("ConfigurationProperties")
@ConfigurationProperties(prefix = "jasypt.encryptor", ignoreUnknownFields = true)
@Data
public class JasyptEncryptorConfigurationProperties {

    public static JasyptEncryptorConfigurationProperties bindConfigProps(ConfigurableEnvironment environment) {
        final BindHandler handler = new IgnoreErrorsBindHandler(BindHandler.DEFAULT);
        final MutablePropertySources propertySources = environment.getPropertySources();
        final Binder binder = new Binder(ConfigurationPropertySources.from(propertySources),
                new PropertySourcesPlaceholdersResolver(propertySources),
                ApplicationConversionService.getSharedInstance());
        final JasyptEncryptorConfigurationProperties config = new JasyptEncryptorConfigurationProperties();

        final ResolvableType type = ResolvableType.forClass(JasyptEncryptorConfigurationProperties.class);
        final Annotation annotation = AnnotationUtils.findAnnotation(JasyptEncryptorConfigurationProperties.class,
                ConfigurationProperties.class);
        final Annotation[] annotations = new Annotation[]{annotation};
        final Bindable<?> target = Bindable.of(type).withExistingValue(config).withAnnotations(annotations);

        binder.bind("jasypt.encryptor", target, handler);
        return config;
    }

    /**
     * Whether to use JDK/Cglib (depending on classpath availability) proxy with an AOP advice as a decorator for
     * existing {@link org.springframework.core.env.PropertySource} or just simply use targeted wrapper Classes. Default
     * Value is {@code false}.
     */
    private Boolean proxyPropertySources = false;

    /**
     * Define a list of {@link org.springframework.core.env.PropertySource} to skip from wrapping/proxying. Properties held
     * in classes on this list will not be eligible for decryption. Default Value is {@code empty list}.
     */
    private List<String> skipPropertySources = Collections.emptyList();

    /**
     * Specify the name of bean to override jasypt-spring-boot's default properties based
     * {@link org.jasypt.encryption.StringEncryptor}. Default Value is {@code jasyptStringEncryptor}.
     */
    private String bean = "jasyptStringEncryptor";

    /**
     * Master Password used for Encryption/Decryption of properties.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getPassword()
     */
    private String password;

    /**
     * Encryption/Decryption Algorithm to be used by Jasypt. For more info on how to get available algorithms visit:
     * <a href="http://www.jasypt.org/cli.html"/>Jasypt CLI Tools Page</a>. Default Value is {@code "PBEWITHHMACSHA512ANDAES_256"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getAlgorithm()
     */
    private String algorithm = "PBEWITHHMACSHA512ANDAES_256";

    /**
     * Number of hashing iterations to obtain the signing key. Default Value is {@code "1000"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getKeyObtentionIterations()
     */
    private String keyObtentionIterations = "1000";

    /**
     * The size of the pool of encryptors to be created. Default Value is {@code "1"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getPoolSize()
     */
    private String poolSize = "1";

    /**
     * The name of the {@link java.security.Provider} implementation to be used by the encryptor for obtaining the
     * encryption algorithm. Default Value is {@code null}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getProviderName()
     */
    private String providerName = null;

    /**
     * The class name of the {@link java.security.Provider} implementation to be used by the encryptor for obtaining the
     * encryption algorithm. Default Value is {@code null}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.SimpleStringPBEConfig#setProviderClassName(String)
     */
    private String providerClassName = null;

    /**
     * A {@link org.jasypt.salt.SaltGenerator} implementation to be used by the encryptor. Default Value is
     * {@code "org.jasypt.salt.RandomSaltGenerator"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getSaltGenerator()
     */
    private String saltGeneratorClassname = "org.jasypt.salt.RandomSaltGenerator";

    /**
     * A {@link org.jasypt.iv.IvGenerator} implementation to be used by the encryptor. Default Value is
     * {@code "org.jasypt.iv.RandomIvGenerator"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getIvGenerator()
     */
    private String ivGeneratorClassname = "org.jasypt.iv.RandomIvGenerator";

    /**
     * Specify the form in which String output will be encoded. {@code "base64"} or {@code "hexadecimal"}. Default Value
     * is {@code "base64"}.
     *
     * @see org.jasypt.encryption.pbe.PBEStringEncryptor
     * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getStringOutputType()
     */
    private String stringOutputType = "base64";

    /**
     * Specify a PEM/DER base64 encoded string. PEM encoded keys can simply omit the "BEGIN/END PRIVATE KEY" header/footer
     * and just specify the base64 encoded key. This property takes precedence over {@link #setPrivateKeyLocation(String)}
     */
    private String privateKeyString = null;

    /**
     * Specify a PEM/DER private key location, in Spring's resource nomenclature (i.e. classpath:resource/path or file://path/to/file)
     */
    private String privateKeyLocation = null;

    /**
     * Specify the private key format to use: DER (default) or PEM
     */
    private KeyFormat privateKeyFormat = KeyFormat.DER;

    /**
     * Specify a PEM/DER base64 encoded string. PEM encoded keys can simply omit the "BEGIN/END PUBLIC KEY" header/footer
     * and just specify the base64 encoded key. This property takes precedence over {@link #setPrivateKeyLocation(String)}
     */
    private String publicKeyString = null;

    /**
     * Specify a PEM/DER public key location, in Spring's resource nomenclature (i.e. classpath:resource/path or file://path/to/file)
     */
    private String publicKeyLocation = null;

    /**
     * Specify the public key format to use: DER (default) or PEM
     */
    private KeyFormat publicKeyFormat = KeyFormat.DER;

    @NestedConfigurationProperty
    private PropertyConfigurationProperties property = new PropertyConfigurationProperties();

    @Data
    public static class PropertyConfigurationProperties {

        /**
         * Specify the name of the bean to be provided for a custom
         * {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyDetector}. Default value is
         * {@code "encryptablePropertyDetector"}
         */
        private String detectorBean = "encryptablePropertyDetector";

        /**
         * Specify the name of the bean to be provided for a custom
         * {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver}. Default value is
         * {@code "encryptablePropertyResolver"}
         */
        private String resolverBean = "encryptablePropertyResolver";

        /**
         * Specify the name of the bean to be provided for a custom {@link EncryptablePropertyFilter}. Default value is
         * {@code "encryptablePropertyFilter"}
         */
        private String filterBean = "encryptablePropertyFilter";

        /**
         * Specify a custom {@link String} to identify as prefix of encrypted properties. Default value is
         * {@code "ENC("}
         */
        private String prefix = "ENC(";

        /**
         * Specify a custom {@link String} to identify as suffix of encrypted properties. Default value is {@code ")"}
         */
        private String suffix = ")";

        @NestedConfigurationProperty
        private FilterConfigurationProperties filter = new FilterConfigurationProperties();

        @Data
        public static class FilterConfigurationProperties {

            /**
             * Specify the property sources name patterns to be included for decryption
             * by{@link EncryptablePropertyFilter}. Default value is {@code null}
             */
            private List<String> includeSources = null;

            /**
             * Specify the property sources name patterns to be EXCLUDED for decryption
             * by{@link EncryptablePropertyFilter}. Default value is {@code null}
             */
            private List<String> excludeSources = null;

            /**
             * Specify the property name patterns to be included for decryption by{@link EncryptablePropertyFilter}.
             * Default value is {@code null}
             */
            private List<String> includeNames = null;

            /**
             * Specify the property name patterns to be EXCLUDED for decryption by{@link EncryptablePropertyFilter}.
             * Default value is {@code jasypt\\.encryptor\\.*}
             */
            private List<String> excludeNames = singletonList("^jasypt\\.encryptor\\.*");
        }
    }
}
