# jasypt-spring-boot
**[Jasypt](http://www.jasypt.org)** integration for Spring boot 1.4.X , 1.5.X and 2.0.X

[![Build Status](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot.svg?branch=master)](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ulisesbocchio/jasypt-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot)


[![Code Climate](https://codeclimate.com/github/rsercano/mongoclient/badges/gpa.svg)](https://codeclimate.com/github/ulisesbocchio/jasypt-spring-boot)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/6a75fc4e1d3f480f811b5339202400b5)](https://www.codacy.com/app/ulisesbocchio/jasypt-spring-boot?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ulisesbocchio/jasypt-spring-boot&amp;utm_campaign=Badge_Grade)
[![GitHub release](https://img.shields.io/github/release/ulisesbocchio/jasypt-spring-boot.svg)](https://github.com/ulisesbocchio/jasypt-spring-boot)
[![Github All Releases](https://img.shields.io/github/downloads/ulisesbocchio/jasypt-spring-boot/total.svg)](https://github.com/ulisesbocchio/jasypt-spring-boot)
[![MIT License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](https://github.com/ulisesbocchio/jasypt-spring-boot/blob/master/LICENSE)
[![volkswagen status](https://auchenberg.github.io/volkswagen/volkswargen_ci.svg?v=1)](https://github.com/ulisesbocchio/jasypt-spring-boot)

[![Paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=9J2V5HJT8AZF8)


Jasypt Spring Boot provides Encryption support for property sources in Spring Boot Applications.<br/>
There are 3 ways to integrate `jasypt-spring-boot` in your project:

- Simply adding the starter jar `jasypt-spring-boot-starter` to your classpath if using `@SpringBootApplication` or `@EnableAutoConfiguration` will enable encryptable properties across the entire Spring Environment
- Adding `jasypt-spring-boot` to your classpath and adding `@EnableEncryptableProperties` to your main Configuration class to enable encryptable properties across the entire Spring Environment
- Adding `jasypt-spring-boot` to your classpath and declaring individual encryptable property sources with `@EncrytablePropertySource`

## What's new?
### Update 01/11/2020: Version 3.0.2 Release Includes
* Allows unresolvable properties from env actuator (Thanks [@thorntonrp](https://github.com/thorntonrp))
* Fixes [jasypt-maven-plugin]((#maven-plugin)) issues
* Adds support to [jasypt-maven-plugin](#maven-plugin) for encryption/decryption of single values
### Update 12/31/2019: Version 3.0.1 Release Includes
* Adds support for [skipping classes](#filter-out-propertysource-classes-from-being-introspected) from being introspected
* Usage of `replacePlaceHolders` instead of `replaceRequiredPlaceholders` on property resolver to mirror Spring's default behavior
* Refactored `StandardEncryptableEnvironment` to use builder pattern and lazy load resolver/filter/detector/encryptor
* Removed deprecated `EncryptableENvironment`
### Update 11/24/2019: Version 3.0.0 Release Includes
* Adds support for Spring Boot 2.1.X
* Spring Boot 1.5.X No longer supported
* Changed default encryption to PBEWITHHMACSHA512ANDAES_256 (Thanks [@rupert-madden-abbott](https://github.com/rupert-madden-abbott))
* Switched properties cache to HashMap to avoid concurrency issues (Thanks [@krm1312](https://github.com/krm1312))
* Higher priority for Properties post processor (Thanks [@ttulka](https://github.com/ttulka))
* Jasypt Spring Boot [Maven Plugin](#maven-plugin) (Thanks [@rupert-madden-abbott](https://github.com/rupert-madden-abbott))
* To keep your encrypted properties with previous default config use:
```yaml
jasypt:
  encryptor:
    algorithm: PBEWithMD5AndDES
    iv-generator-classname: org.jasypt.iv.NoIvGenerator
```
### Update 9/8/2019: Version 2.1.2 Release Includes
* jasypt 1.9.3 rollback with IV Generators (thanks [@tkalmar](https://github.com/tkalmar))
* interpolation inside `ENC()` and `${}` blocks (thanks [@ttulka](https://github.com/ttulka))
* fixes for relaxed bindings, fail on custom bean not found, filters, and double app listener

### Update 1/8/2019: Version 2.1.1 Release Including
* [Asymmetric Encryption](#asymmetric-encryption)
* and support for JSB96 with IV Generators (Thanks [@melloware](https://github.com/melloware)!!)

### Update 7/17/2018: Version 2.1.0 Release Including
* [Filters](#using-filters)

### Update 3/17/2018: Version 2.0.0 has been released supporting
* Spring Boot 2.0.X.RELEASE. [SemVer](https://semver.org/) adopted.

### Update 7/18/2015: `jasypt-spring-boot` is now in Maven Central!

## What to do First?
Use one of the following 3 methods (briefly explained above):

1. Simply add the starter jar dependency to your project if your Spring Boot application uses `@SpringBootApplication` or `@EnableAutoConfiguration` and encryptable properties will be enabled across the entire Spring Environment (This means any system property, environment property, command line argument, application.properties, yaml properties, and any other custom property sources can contain encrypted properties):

	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>3.0.2</version>
    </dependency>
	```
2. IF you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations then add this dependency to your project:
	
	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>3.0.2</version>
    </dependency>
	```

	And then add `@EnableEncryptableProperties` to you Configuration class. For instance:

	```java
    @Configuration
    @EnableEncryptableProperties
    public class MyApplication {
        ...
    }
	```
 And encryptable properties will be enabled across the entire Spring Environment (This means any system property, environment property, command line argument, application.properties, yaml properties, and any other custom property sources can contain encrypted properties)

3. IF you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations and you don't want to enable encryptable properties across the entire Spring Environment, there's a third option. First add the following dependency to your project:
	
	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>3.0.2</version>
    </dependency>
	```
	And then add as many `@EncryptablePropertySource` annotations as you want in your Configuration files. Just like you do with Spring's `@PropertySource` annotation. For instance:
	
	```java
	@Configuration
	@EncryptablePropertySource(name = "EncryptedProperties", value = "classpath:encrypted.properties")
	public class MyApplication {
		...
	}
	```
Conveniently, there's also a `@EncryptablePropertySources` annotation that one could use to group annotations of type `@EncryptablePropertySource` like this:

```java
	@Configuration
	@EncryptablePropertySources({@EncryptablePropertySource("classpath:encrypted.properties"),
	                             @EncryptablePropertySource("classpath:encrypted2.properties")})
	public class MyApplication {
		...
	}
```

Also, note that as of version 1.8, `@EncryptablePropertySource` supports YAML files
	
## Custom Environment
As of version ~~1.7~~ 1.15, a 4th method of enabling encryptable properties exists for some special cases. A custom `ConfigurableEnvironment` class is provided: ~~`EncryptableEnvironment`~~ `StandardEncryptableEnvironment` and `StandardEncryptableServletEnvironment` that can be used with `SpringApplicationBuilder` to define the custom environment this way:

```java
new SpringApplicationBuilder()
    .environment(new StandardEncryptableEnvironment())
    .sources(YourApplicationClass.class).run(args);

```

This method would only require using a dependency for `jasypt-spring-boot`. No starter jar dependency is required. This method is useful for early access of encrypted properties on bootstrap. While not required in most scenarios could be useful when customizing Spring Boot's init behavior or integrating with certain capabilities that are configured very early, such as Logging configuration. For a concrete example, this method of enabling encryptable properties is the only one that works with Spring Properties replacement in `logback-spring.xml` files, using the `springProperty` tag. For instance:

```xml
<springProperty name="user" source="db.user"/>
<springProperty name="password" source="db.password"/>
<appender name="db" class="ch.qos.logback.classic.db.DBAppender">
    <connectionSource
        class="ch.qos.logback.core.db.DriverManagerConnectionSource">
        <driverClass>org.postgresql.Driver</driverClass>
        <url>jdbc:postgresql://localhost:5432/simple</url>
        <user>${user}</user>
        <password>${password}</password>
    </connectionSource>
</appender>
```

This mechanism could be used for instance (as shown) to initialize Database Logging Appender that require sensitive credentials to be passed.
Alternatively, if a custom `StringEncryptor` is needed to be provided, a static builder method is provided `StandardEncryptableEnvironment#builder` for customization (other customizations are possible):

```java
StandardEncryptableEnvironment
    .builder()
    .encryptor(new MyEncryptor())
    .build()
```

## How everything Works?

This will trigger some configuration to be loaded that basically does 2 things:

1. It registers a Spring post processor that decorates all PropertySource objects contained in the Spring Environment so they are "encryption aware" and detect when properties are encrypted following jasypt's property convention.
2. It defines a default `StringEncryptor` that can be configured through regular properties, system properties, or command line arguments.

## Where do I put my encrypted properties?
When using METHODS 1 and 2 you can define encrypted properties in any of the PropertySource contained in the Environment. For instance, using the @PropertySource annotation:

```java
    @SpringBootApplication
    @EnableEncryptableProperties
    @PropertySource(name="EncryptedProperties", value = "classpath:encrypted.properties")
    public class MyApplication {
        ...
    }
```
And your encrypted.properties file would look something like this:

```properties
	secret.property=ENC(nrmZtkF7T0kjG/VodDvBw93Ct8EgjCA+)
```
Now when you do `environment.getProperty("secret.property")` or use `@Value("${secret.property}")` what you get is the decrypted version of `secret.property`.<br/>
When using METHOD 3 (`@EncryptablePropertySource`) then you can access the encrypted properties the same way, the only difference is that you must put the properties in the resource that was declared within the `@EncryptablePropertySource` annotation so that the properties can be decrypted properly.

## Password-based Encryption Configuration
Jasypt uses an `StringEncryptor` to decrypt properties. For all 3 methods, if no custom `StringEncryptor` (see the [Custom Encryptor](#customEncryptor) section for details) is found in the Spring Context, one is created automatically that can be configured through the following properties (System, properties file, command line arguments, environment variable, etc.):

<table border="1">
      <tr>
          <td>Key</td><td>Required</td><td>Default Value</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.password</td><td><b>True</b></td><td> - </td>
      </tr>
      <tr>
          <td>jasypt.encryptor.algorithm</td><td>False</td><td>PBEWITHHMACSHA512ANDAES_256</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.key-obtention-iterations</td><td>False</td><td>1000</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.pool-size</td><td>False</td><td>1</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.provider-name</td><td>False</td><td>SunJCE</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.provider-class-name</td><td>False</td><td>null</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.salt-generator-classname</td><td>False</td><td>org.jasypt.salt.RandomSaltGenerator</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.iv-generator-classname</td><td>False</td><td>org.jasypt.iv.RandomIvGenerator</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.string-output-type</td><td>False</td><td>base64</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.proxy-property-sources</td><td>False</td><td>false</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.skip-property-sources</td><td>False</td><td>empty list</td>
      </tr>
  </table>

The only property required is the encryption password, the rest could be left to use default values. While all this properties could be declared in a properties file, the encryptor password should not be stored in a property file, it should rather be passed as system property, command line argument, or environment variable and as far as its name is `jasypt.encryptor.password` it'll work.<br/>

The last property, `jasypt.encryptor.proxyPropertySources` is used to indicate `jasyp-spring-boot` how property values are going to be intercepted for decryption. The default value, `false` uses custom wrapper implementations of `PropertySource`, `EnumerablePropertySource`, and `MapPropertySource`. When `true` is specified for this property, the interception mechanism will use CGLib proxies on each specific `PropertySource` implementation. This may be useful on some scenarios where the type of the original `PropertySource` must be preserved. 

## <a name="customEncryptor"></a>Use you own Custom Encryptor
For custom configuration of the encryptor and the source of the encryptor password you can always define your own StringEncryptor bean in your Spring Context, and the default encryptor will be ignored. For instance:

```java
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("password");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
```
Notice that the bean name is required, as `jasypt-spring-boot` detects custom String Encyptors by name as of version `1.5`. The default bean name is:

``` jasyptStringEncryptor ```

But one can also override this by defining property:

``` jasypt.encryptor.bean ```

So for instance, if you define `jasypt.encryptor.bean=encryptorBean` then you would define your custom encryptor with that name:

```java
    @Bean("encryptorBean")
    public StringEncryptor stringEncryptor() {
        ...
    }
```

## Custom Property Detector, Prefix, Suffix and/or Resolver

As of `jasypt-spring-boot-1.10` there are new extensions points. `EncryptablePropertySource` now uses `EncryptablePropertyResolver` to resolve all properties:

```java
public interface EncryptablePropertyResolver {
    String resolvePropertyValue(String value);
}
```

Implementations of this interface are responsible of both **detecting** and **decrypting** properties. The default implementation, `DefaultPropertyResolver` uses the before mentioned
`StringEncryptor` and a new `EncryptablePropertyDetector`.

### Provide a Custom `EncryptablePropertyDetector`

You can override the default implementation by providing a Bean of type `EncryptablePropertyDetector` with name `encryptablePropertyDetector` or if you wanna provide
your own bean name, override property `jasypt.encryptor.property.detector-bean` and specify the name you wanna give the bean. When providing this, you'll be responsible for
detecting encrypted properties.
Example:

```java
private static class MyEncryptablePropertyDetector implements EncryptablePropertyDetector {
    @Override
    public boolean isEncrypted(String value) {
        if (value != null) {
            return value.startsWith("ENC@");
        }
        return false;
    }

    @Override
    public String unwrapEncryptedValue(String value) {
        return value.substring("ENC@".length());
    }
}
```

```java
@Bean(name = "encryptablePropertyDetector")
    public EncryptablePropertyDetector encryptablePropertyDetector() {
        return new MyEncryptablePropertyDetector();
    }
```

### Provide a Custom Encrypted Property `prefix` and `suffix`

If all you want to do is to have different prefix/suffix for encrypted properties, you can keep using all the default implementations
and just override the following properties in `application.properties` (or `application.yml`):

```YAML
jasypt:
  encryptor:
    property:
      prefix: "ENC@["
      suffix: "]"
```

### Provide a Custom `EncryptablePropertyResolver`

You can override the default implementation by providing a Bean of type `EncryptablePropertyResolver` with name `encryptablePropertyResolver` or if you wanna provide
your own bean name, override property `jasypt.encryptor.property.resolver-bean` and specify the name you wanna give the bean. When providing this, you'll be responsible for
detecting and decrypting encrypted properties.
Example:

```java
    class MyEncryptablePropertyResolver implements EncryptablePropertyResolver {
    
    
        private final PooledPBEStringEncryptor encryptor;
    
        public MyEncryptablePropertyResolver(char[] password) {
            this.encryptor = new PooledPBEStringEncryptor();
            SimpleStringPBEConfig config = new SimpleStringPBEConfig();
            config.setPasswordCharArray(password);
            config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
            config.setKeyObtentionIterations("1000");
            config.setPoolSize(1);
            config.setProviderName("SunJCE");
            config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
            config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
            config.setStringOutputType("base64");
            encryptor.setConfig(config);
        }
    
        @Override
        public String resolvePropertyValue(String value) {
            if (value != null && value.startsWith("{cipher}")) {
                return encryptor.decrypt(value.substring("{cipher}".length()));
            }
            return value;
        }
    }
```

```java
@Bean(name="encryptablePropertyResolver")
    EncryptablePropertyResolver encryptablePropertyResolver(@Value("${jasypt.encryptor.password}") String password) {
        return new MyEncryptablePropertyResolver(password.toCharArray());
    }
```

Notice that by overriding `EncryptablePropertyResolver`, any other configuration or overrides you may have for prefixes, suffixes, 
`EncryptablePropertyDetector` and `StringEncryptor` will stop working since the Default resolver is what uses them. You'd have to
wire all that stuff yourself. Fortunately, you don't have to override this bean in most cases, the previous options should suffice.

But as you can see in the implementation, the detection and decryption of the encrypted properties are internal to `MyEncryptablePropertyResolver`

## Using Filters

`jasypt-spring-boot:2.1.0` introduces a new feature to specify property filters. The filter is part of the `EncryptablePropertyResolver` API
and allows you to determine which properties or property sources to contemplate for decryption. This is, before even examining the actual
property value to search for, or try to, decrypt it. For instance, by default, all properties which name start with `jasypt.encryptor`
are excluded from examination. This is to avoid circular dependencies at load time when the library beans are configured.

### DefaultPropertyFilter properties

By default, the `DefaultPropertyResolver` uses `DefaultPropertyFilter`, which allows you to specify the following string pattern lists:

* jasypt.encryptor.property.filter.include-sources: Specify the property sources name patterns to be included for decryption
* jasypt.encryptor.property.filter.exclude-sources: Specify the property sources name patterns to be EXCLUDED for decryption
* jasypt.encryptor.property.filter.include-names: Specify the property name patterns to be included for decryption
* jasypt.encryptor.property.filter.exclude-names: Specify the property name patterns to be EXCLUDED for decryption

### Provide a custom `EncryptablePropertyFilter`

You can override the default implementation by providing a Bean of type `EncryptablePropertyFilter` with name `encryptablePropertyFilter` or if you wanna provide
your own bean name, override property `jasypt.encryptor.property.filter-bean` and specify the name you wanna give the bean. When providing this, you'll be responsible for
detecting properties and/or property sources you want to contemplate for decryption.
Example:

```java
    class MyEncryptablePropertyFilter implements EncryptablePropertyFilter {
    
        public boolean shouldInclude(PropertySource<?> source, String name) {
            return name.startsWith('encrypted.');
        }
    }
```

```java
@Bean(name="encryptablePropertyFilter")
    EncryptablePropertyFilter encryptablePropertyFilter() {
        return new MyEncryptablePropertyFilter();
    }
```

Notice that for this mechanism to work, you should not provide a custom `EncryptablePropertyResolver` and use the default
resolver instead. If you provide custom resolver, you are responsible for the entire process of detecting and decrypting
properties.

## Filter out `PropertySource` classes from being introspected
Define a comma-separated list of fully-qualified class names to be skipped from introspection. This classes will not be
wrapped/proxied by this plugin and thereby properties contained in them won't supported encryption/decryption:

```properties
jasypt.encryptor.skip-property-sources=org.springframework.boot.env.RandomValuePropertySource,org.springframework.boot.ansi.AnsiPropertySource
```

## Maven Plugin

A Maven plugin is provided with a number of helpful utilities.

To use the plugin, just add the following to your pom.xml:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.ulisesbocchio</groupId>
      <artifactId>jasypt-maven-plugin</artifactId>
      <version>3.0.2</version>
    </plugin>
  </plugins>
</build>
```

The plugin reads you encryption configuration directly from your Spring Boot configuration

### Encryption

To encrypt a single value run:

```bash
mvn jasypt:encrypt-value -Djasypt.encryptor.password="the password" -Djasypt.plugin.value="theValueYouWantToEncrypt"
```


To encrypt placeholders in a file, simply wrap any string with `DEC(...)`. For example:

```properties
sensitive.password=DEC(secret value)
regular.property=example
```

This can be encrypted as follows:

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password"
```

Which would edit that file in place resulting in:

```properties
sensitive.password=ENC(encrypted)
regular.property=example
```

### Decryption

To decrypt a single value run:

```bash
mvn jasypt:decrypt-value -Djasypt.encryptor.password="the password" -Djasypt.plugin.value="DbG1GppXOsFa2G69PnmADvQFI3esceEhJYbaEIKCcEO5C85JEqGAhfcjFMGnoRFf"
```

To decrypt placeholders in a file, simply wrap any string with `ENC(...)`. For example:

```properties
sensitive.password=ENC(encrypted)
regular.property=example
```

This can be decrypted as follows:

```bash
mvn jasypt:decrypt -Djasypt.encryptor.password="the password"
```

Which would output the decrypted contents to the screen:

```properties
sensitive.password=DEC(decrypted)
regular.property=example
```

Note that outputting to the screen, rather than editing the file in place, is designed to reduce
accidental committing of decrypted values to version control. When decrypting, you most likely
just want to check what value has been encrypted, rather than wanting to permanently decrypt that
value.

### Re-encryption
Changing the configuration for existing encrypted properties is slightly awkward using the encrypt/decrypt goals. You
must run the decrypt goal using the old configuration, then copy the decrypted output back into the original file, then
run the encrypt goal with the new configuration.

The re-encrypt goal simplifies this by re-encrypting a file in place. 2 sets of configuration must be provided. The
new configuration is supplied in the same way as you would configure the other maven goals. The old configuration
is supplied via system properties prefixed with "jasypt.plugin.old" instead of "jasypt.encryptor".

For example, to re-encrypt application.properties that was previously encrypted with the password OLD and then
encrypt with the new password NEW:

```bash
mvn jasypt:reencrypt -Djasypt.plugin.old.password=OLD -Djasypt.encryptor.password=NEW
```

### Upgrade
Sometimes the default encryption configuration might change between versions of jasypt-spring-boot. You can
automatically upgrade your encrypted properties to the new defaults with the upgrade goal. This will decrypt your
application.properties file using the old default configuration and re-encrypt using the new default configuration.

```bash
mvn jasypt:upgrade -Djasypt.encryptor.password=EXAMPLE
```

You can also pass the system property `-Djasypt.plugin.old.major-version` to specify the version you are upgrading from.
This will always default to the last major version where the configuration changed. Currently, the only major version
where the defaults changed is version 2, so there is no need to set this property, but it is there for future use.

### Load
You can also decrypt a properties file and load all of its properties into memory and make them accessible to Maven. This is useful when you want to make encrypted properties available to other Maven plugins.

You can chain the goals of the later plugins directly after this one. For example, with flyway:

```bash
mvn jasypt:load flyway:migrate -Djasypt.encryptor.password="the password"
```

You can also specify a prefix for each property with `-Djasypt.plugin.keyPrefix=example.`. This
helps to avoid potential clashes with other Maven properties.

### File Path

For all of the above utilities, the file path defaults to `file:src/main/resources/application.properties`.

You can insert the name of a Spring profile between the file name and it's extension by specifying by specifying an active profile. For example, the file `file:src/main/resources/application-dev.properties` could be encrypted as follows:

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Dspring.profiles.active=dev
```

You can also changed the file path completely. For example to encrypt a file in your test resources directory:

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Djasypt.plugin.path="file:src/main/test/application.properties"
```

Or you can encrypt a file with a different name:

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Djasypt.plugin.path="file:src/main/resources/flyway.properties"
```

Both of these would also work with decryption and loading.

You can also specify a different extension. However, please note that loading only works with property files. Encryption/Decryption work with any file type.

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Djasypt.plugin.path="file:src/main/resources/application.yaml"
```

You can also specify a file on the classpath, instead of the file system. However, please note that this will not work for encryption, as this will attempt to write the encrypted contents back to disk. Also this will only load files from the plugin's classpath, and not the classpath of the application.

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Djasypt.plugin.path="classpath:application.properties"
```

### Spring profiles and other spring config
You can override any spring config you support in your application when running the plugin, for instance selecting a given spring profile:
 
```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="the password" -Djasypt.plugin.path="classpath:application.properties" -Dspring.profiles.active=cloud
```

## Asymmetric Encryption
`jasypt-spring-boot:2.1.1` introduces a new feature to encrypt/decrypt properties using asymmetric encryption with a pair of private/public keys
in DER or PEM formats.

### Config Properties

The following are the configuration properties you can use to config asymmetric decryption of properties;

<table border="1">
      <tr>
          <td>Key</td><td>Default Value</td><td>Description</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.privateKeyString</td><td>null</td><td> private key for decryption in String format</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.privateKeyLocation</td><td>null</td><td>location of the private key for decryption in spring resource format</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.privateKeyFormat</td><td>DER</td><td>Key format. DER or PEM</td>
      </tr>
  </table>
  
  You should either use `privateKeyString` or `privateKeyLocation`, the String format takes precedence if set.
  To specify a private key in DER format with `privateKeyString`, please encode the key bytes to `base64`.
  
  __Note__ that `jasypt.encryptor.password` still takes precedences for PBE encryption over the asymmetric config. 

### Sample config

#### DER key as string
```yaml
jasypt:
    encryptor:
      privateKeyString: MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCtB/IYK8E52CYMZTpyIY9U0HqMewyKnRvSo6s+9VNIn/HSh9+MoBGiADa2MaPKvetS3CD3CgwGq/+LIQ1HQYGchRrSORizOcIp7KBx+Wc1riatV/tcpcuFLC1j6QJ7d2I+T7RA98Sx8X39orqlYFQVysTw/aTawX/yajx0UlTW3rNAY+ykeQ0CBHowtTxKM9nGcxLoQbvbYx1iG9JgAqye7TYejOpviOH+BpD8To2S8zcOSojIhixEfayay0gURv0IKJN2LP86wkpAuAbL+mohUq1qLeWdTEBrIRXjlnrWs1M66w0l/6JwaFnGOqEB6haMzE4JWZULYYpr2yKyoGCRAgMBAAECggEAQxURhs1v3D0wgx27ywO3zeoFmPEbq6G9Z6yMd5wk7cMUvcpvoNVuAKCUlY4pMjDvSvCM1znN78g/CnGF9FoxJb106Iu6R8HcxOQ4T/ehS+54kDvL999PSBIYhuOPUs62B/Jer9FfMJ2veuXb9sGh19EFCWlMwILEV/dX+MDyo1qQaNzbzyyyaXP8XDBRDsvPL6fPxL4r6YHywfcPdBfTc71/cEPksG8ts6um8uAVYbLIDYcsWopjVZY/nUwsz49xBCyRcyPnlEUJedyF8HANfVEO2zlSyRshn/F+rrjD6aKBV/yVWfTEyTSxZrBPl4I4Tv89EG5CwuuGaSagxfQpAQKBgQDXEe7FqXSaGk9xzuPazXy8okCX5pT6545EmqTP7/JtkMSBHh/xw8GPp+JfrEJEAJJl/ISbdsOAbU+9KAXuPmkicFKbodBtBa46wprGBQ8XkR4JQoBFj1SJf7Gj9ozmDycozO2Oy8a1QXKhHUPkbPQ0+w3efwoYdfE67ZodpFNhswKBgQDN9eaYrEL7YyD7951WiK0joq0BVBLK3rwO5+4g9IEEQjhP8jSo1DP+zS495t5ruuuuPsIeodA79jI8Ty+lpYqqCGJTE6muqLMJDiy7KlMpe0NZjXrdSh6edywSz3YMX1eAP5U31pLk0itMDTf2idGcZfrtxTLrpRffumowdJ5qqwKBgF+XZ+JRHDN2aEM0atAQr1WEZGNfqG4Qx4o0lfaaNs1+H+knw5kIohrAyvwtK1LgUjGkWChlVCXb8CoqBODMupwFAqKL/IDImpUhc/t5uiiGZqxE85B3UWK/7+vppNyIdaZL13a1mf9sNI/p2whHaQ+3WoW/P3R5z5uaifqM1EbDAoGAN584JnUnJcLwrnuBx1PkBmKxfFFbPeSHPzNNsSK3ERJdKOINbKbaX+7DlT4bRVbWvVj/jcw/c2Ia0QTFpmOdnivjefIuehffOgvU8rsMeIBsgOvfiZGx0TP3+CCFDfRVqjIBt3HAfAFyZfiP64nuzOERslL2XINafjZW5T0pZz8CgYAJ3UbEMbKdvIuK+uTl54R1Vt6FO9T5bgtHR4luPKoBv1ttvSC6BlalgxA0Ts/AQ9tCsUK2JxisUcVgMjxBVvG0lfq/EHpL0Wmn59SHvNwtHU2qx3Ne6M0nQtneCCfR78OcnqQ7+L+3YCMqYGJHNFSard+dewfKoPnWw0WyGFEWCg==

```

#### DER key as a resource location
```yaml
jasypt:
    encryptor:
      privateKeyLocation: classpath:private_key.der

```

#### PEM key as string
```yaml
jasypt:
    encryptor:
      privateKeyFormat: PEM
      privateKeyString: |-
          -----BEGIN PRIVATE KEY-----
          MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCtB/IYK8E52CYM
          ZTpyIY9U0HqMewyKnRvSo6s+9VNIn/HSh9+MoBGiADa2MaPKvetS3CD3CgwGq/+L
          IQ1HQYGchRrSORizOcIp7KBx+Wc1riatV/tcpcuFLC1j6QJ7d2I+T7RA98Sx8X39
          orqlYFQVysTw/aTawX/yajx0UlTW3rNAY+ykeQ0CBHowtTxKM9nGcxLoQbvbYx1i
          G9JgAqye7TYejOpviOH+BpD8To2S8zcOSojIhixEfayay0gURv0IKJN2LP86wkpA
          uAbL+mohUq1qLeWdTEBrIRXjlnrWs1M66w0l/6JwaFnGOqEB6haMzE4JWZULYYpr
          2yKyoGCRAgMBAAECggEAQxURhs1v3D0wgx27ywO3zeoFmPEbq6G9Z6yMd5wk7cMU
          vcpvoNVuAKCUlY4pMjDvSvCM1znN78g/CnGF9FoxJb106Iu6R8HcxOQ4T/ehS+54
          kDvL999PSBIYhuOPUs62B/Jer9FfMJ2veuXb9sGh19EFCWlMwILEV/dX+MDyo1qQ
          aNzbzyyyaXP8XDBRDsvPL6fPxL4r6YHywfcPdBfTc71/cEPksG8ts6um8uAVYbLI
          DYcsWopjVZY/nUwsz49xBCyRcyPnlEUJedyF8HANfVEO2zlSyRshn/F+rrjD6aKB
          V/yVWfTEyTSxZrBPl4I4Tv89EG5CwuuGaSagxfQpAQKBgQDXEe7FqXSaGk9xzuPa
          zXy8okCX5pT6545EmqTP7/JtkMSBHh/xw8GPp+JfrEJEAJJl/ISbdsOAbU+9KAXu
          PmkicFKbodBtBa46wprGBQ8XkR4JQoBFj1SJf7Gj9ozmDycozO2Oy8a1QXKhHUPk
          bPQ0+w3efwoYdfE67ZodpFNhswKBgQDN9eaYrEL7YyD7951WiK0joq0BVBLK3rwO
          5+4g9IEEQjhP8jSo1DP+zS495t5ruuuuPsIeodA79jI8Ty+lpYqqCGJTE6muqLMJ
          Diy7KlMpe0NZjXrdSh6edywSz3YMX1eAP5U31pLk0itMDTf2idGcZfrtxTLrpRff
          umowdJ5qqwKBgF+XZ+JRHDN2aEM0atAQr1WEZGNfqG4Qx4o0lfaaNs1+H+knw5kI
          ohrAyvwtK1LgUjGkWChlVCXb8CoqBODMupwFAqKL/IDImpUhc/t5uiiGZqxE85B3
          UWK/7+vppNyIdaZL13a1mf9sNI/p2whHaQ+3WoW/P3R5z5uaifqM1EbDAoGAN584
          JnUnJcLwrnuBx1PkBmKxfFFbPeSHPzNNsSK3ERJdKOINbKbaX+7DlT4bRVbWvVj/
          jcw/c2Ia0QTFpmOdnivjefIuehffOgvU8rsMeIBsgOvfiZGx0TP3+CCFDfRVqjIB
          t3HAfAFyZfiP64nuzOERslL2XINafjZW5T0pZz8CgYAJ3UbEMbKdvIuK+uTl54R1
          Vt6FO9T5bgtHR4luPKoBv1ttvSC6BlalgxA0Ts/AQ9tCsUK2JxisUcVgMjxBVvG0
          lfq/EHpL0Wmn59SHvNwtHU2qx3Ne6M0nQtneCCfR78OcnqQ7+L+3YCMqYGJHNFSa
          rd+dewfKoPnWw0WyGFEWCg==
          -----END PRIVATE KEY-----

```

#### PEM key as a resource location
```yaml
jasypt:
    encryptor:
      privateKeyFormat: PEM
      privateKeyLocation: classpath:private_key.pem

```

### Encrypting properties

There is no program/command to encrypt properties using asymmetric keys but you can use the following code snippet to encrypt
your properties:

#### DER Format

```java
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor;
import org.jasypt.encryption.StringEncryptor;

public class PropertyEncryptor {
    public static void main(String[] args) {
        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();
        config.setPublicKey("MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArQfyGCvBOdgmDGU6ciGPVNB6jHsMip0b0qOrPvVTSJ/x0offjKARogA2tjGjyr3rUtwg9woMBqv/iyENR0GBnIUa0jkYsznCKeygcflnNa4mrVf7XKXLhSwtY+kCe3diPk+0QPfEsfF9/aK6pWBUFcrE8P2k2sF/8mo8dFJU1t6zQGPspHkNAgR6MLU8SjPZxnMS6EG722MdYhvSYAKsnu02Hozqb4jh/gaQ/E6NkvM3DkqIyIYsRH2smstIFEb9CCiTdiz/OsJKQLgGy/pqIVKtai3lnUxAayEV45Z61rNTOusNJf+icGhZxjqhAeoWjMxOCVmVC2GKa9sisqBgkQIDAQAB");
        StringEncryptor encryptor = new SimpleAsymmetricStringEncryptor(config);
        String message = "chupacabras";
        String encrypted = encryptor.encrypt(message);
        System.out.printf("Encrypted message %s\n", encrypted);
    }
}
```

#### PEM Format

```java
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleAsymmetricStringEncryptor;
import org.jasypt.encryption.StringEncryptor;
import static com.ulisesbocchio.jasyptspringboot.util.AsymmetricCryptography.KeyFormat.PEM;

public class PropertyEncryptor {
    public static void main(String[] args) {
        SimpleAsymmetricConfig config = new SimpleAsymmetricConfig();
        config.setKeyFormat(PEM);
        config.setPublicKey("-----BEGIN PUBLIC KEY-----\n" +
                "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArQfyGCvBOdgmDGU6ciGP\n" +
                "VNB6jHsMip0b0qOrPvVTSJ/x0offjKARogA2tjGjyr3rUtwg9woMBqv/iyENR0GB\n" +
                "nIUa0jkYsznCKeygcflnNa4mrVf7XKXLhSwtY+kCe3diPk+0QPfEsfF9/aK6pWBU\n" +
                "FcrE8P2k2sF/8mo8dFJU1t6zQGPspHkNAgR6MLU8SjPZxnMS6EG722MdYhvSYAKs\n" +
                "nu02Hozqb4jh/gaQ/E6NkvM3DkqIyIYsRH2smstIFEb9CCiTdiz/OsJKQLgGy/pq\n" +
                "IVKtai3lnUxAayEV45Z61rNTOusNJf+icGhZxjqhAeoWjMxOCVmVC2GKa9sisqBg\n" +
                "kQIDAQAB\n" +
                "-----END PUBLIC KEY-----\n");
        StringEncryptor encryptor = new SimpleAsymmetricStringEncryptor(config);
        String message = "chupacabras";
        String encrypted = encryptor.encrypt(message);
        System.out.printf("Encrypted message %s\n", encrypted);
    }
}
```

## Demo App
The [jasypt-spring-boot-demo-samples](https://github.com/ulisesbocchio/jasypt-spring-boot-samples) repo contains working Spring Boot app examples.
The main [jasypt-spring-boot-demo](https://github.com/ulisesbocchio/jasypt-spring-boot-samples/tree/master/jasypt-spring-boot-demo) Demo app explicitly sets a System property with the encryption password before the app runs.
To have a little more realistic scenario try removing the line where the system property is set, build the app with maven, and the run:

```
	java -jar target/jasypt-spring-boot-demo-0.0.1-SNAPSHOT.jar --jasypt.encryptor.password=password
```
And you'll be passing the encryption password as a command line argument.
Run it like this:

```
	java -Djasypt.encryptor.password=password -jar target/jasypt-spring-boot-demo-0.0.1-SNAPSHOT.jar
```
And you'll be passing the encryption password as a System property.

If you need to pass this property as an Environment Variable you can accomplish this by creating application.properties or application.yml and adding:
```
jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD:}
```
or in YAML
```
jasypt:
    encryptor:
        password: ${JASYPT_ENCRYPTOR_PASSWORD:}
```
basically what this does is to define the `jasypt.encryptor.password` property pointing to a different property `JASYPT_ENCRYPTOR_PASSWORD` that you can set with an Environment Variable, and you can also override via System Properties. This technique can also be used to translate property name/values for any other library you need.
This is also available in the Demo app. So you can run the Demo app like this:

```
JASYPT_ENCRYPTOR_PASSWORD=password java -jar target/jasypt-spring-boot-demo-1.5-SNAPSHOT.jar
```

**Note:** When using Gradle as build tool, processResources task fails because of '$' character, to solve this you just need to scape this variable like this '\$'.

## Other Demo Apps
While [jasypt-spring-boot-demo](https://github.com/ulisesbocchio/jasypt-spring-boot-samples/tree/master/jasypt-spring-boot-demo) is a comprehensive Demo that showcases all possible ways to encrypt/decrypt properties, there are other multiple Demos that demo isolated scenarios. 

## Flattr

[![Flattr this git repo](http://api.flattr.com/button/flattr-badge-large.png)](https://flattr.com/submit/auto?fid=9zegzy&url=https%3A%2F%2Fgithub.com%2Fulisesbocchio)
