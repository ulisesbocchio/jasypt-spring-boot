# jasypt-spring-boot
**[Jasypt](http://jasypt.org)** integration for Spring boot

[![Build Status](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot.svg?branch=master)](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ulisesbocchio/jasypt-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot)

Jasypt Spring Boot provides Encryption support for property sources in Spring Boot Applications.<br/>
There are 3 ways to integrate `jasypt-spring-boot` in your project:

- Simply adding the starter jar `jasypt-spring-boot-starter` to your classpath if using `@SpringBootApplication` or `@EnableAutoConfiguration` will enable encryptable properties across the entire Spring Environment
- Adding `jasypt-spring-boot` to your classpath and adding `@EnableEncryptableProperties` to your main Configuration class to enable encryptable properties across the entire Spring Environment
- Adding `jasypt-spring-boot` to your classpath and declaring individual encryptable property sources with `@EncrytablePropertySource`

## What to do First?
Update 7/18/2015: `jasypt-spring-boot` is now in Maven Central!<br/>
Use one of the following 3 methods (briefly explained above):

1. Simply add the starter jar dependency to your project if your Spring Boot application uses `@SpringBootApplication` or `@EnableAutoConfiguration` and encryptable properties will be enabled across the entire Spring Environment (This means any system property, environment property, command line argument, application.properties, yaml properties, and any other custom property sources can contain encrypted properties):

	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>1.9</version>
    </dependency>
	```
2. IF you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations then add this dependency to your project:
	
	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>1.9</version>
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
            <version>1.9</version>
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
As of version 1.7, a 4th method of enabling encryptable properties exists for some special cases. A custom `ConfigurableEnvironment` class is provided: `EncryptableEnvironment` that can be used with `SpringApplicationBuilder` to define the custom environment this way:

```java
new SpringApplicationBuilder()
    .environment(new EncryptableEnvironment(new StandardServletEnvironment()))
    .sources(YourApplicationClass.class).run(args);

```

This method would only require using a dependency for `jasypt-spring-boot`. Notice that `EncryptableEnvironment` is just a wrapper, so you have to provide the actual Environment implementation, in this case `StandardServletEnvironment`. No starter jar dependency is required. While this method is not the recommended one since it has some limitations (explained below) it is useful for early access of encrypted properties on bootstrap. While not required in most scenarios could be useful when customizing Spring Boot's init behavior or integrating with certain capabilities that are configured very early, such as Logging configuration. For a concrete example, this method of enabling encryptable properties is the only one that works with Spring Properties replacement in `logback-spring.xml` files, using the `springProperty` tag. For instance:

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
 
### Limitations
Using this method, StringEncryptor configuration is limited to System Properties or System Environment variables out of the box. And, decryption of properties is **ONLY** available for `String` properties.
Alternatively, if a custom `StringEncryptor` is needed to be provided, a second constructor `EncryptableEnvironment(ConfigurableEnvironment, StringEncryptor)` is available for that purpose.

## How this Works?

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

## Encryption Configuration
Jasypt uses an `StringEncryptor` to decrypt properties. For all 3 methods, if no custom `StringEncryptor` (see the [Custom Encryptor](#customEncryptor) section for details) is found in the Spring Context, one is created automatically that can be configured through the following properties (System, properties file, command line arguments, environment variable, etc.):

<table border="1">
      <tr>
          <td>Key</td><td>Required</td><td>Default Value</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.password</td><td><b>True</b></td><td> - </td>
      </tr>
      <tr>
          <td>jasypt.encryptor.algorithm</td><td>False</td><td>PBEWithMD5AndDES</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.keyObtentionIterations</td><td>False</td><td>1000</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.poolSize</td><td>False</td><td>1</td>
      </tr><tr>
          <td>jasypt.encryptor.providerName</td><td>False</td><td>SunJCE</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.saltGeneratorClassname</td><td>False</td><td>org.jasypt.salt.RandomSaltGenerator</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.stringOutputType</td><td>False</td><td>base64</td>
      </tr>
      <tr>
          <td>jasypt.encryptor.proxyPropertySources</td><td>False</td><td>false</td>
      </tr>
  </table>

The only property required is the encryption password, the rest could be left to use default values. While all this properties could be declared in a properties file, the encryptor password should not be stored in a property file, it should rather be passed as system property, command line argument, or environment variable and as far as its name is `jasypt.encryptor.password` it'll work.<br/>

The last property, `jasypt.encryptor.proxyPropertySources` is used to indicate `jasyp-spring-boot` how property values are going to be intercepted for decryption. The default value, `false` uses custom wrapper implementations of `PropertySource`, `EnumerablePropertySource`, and `MapPropertySource`. When `true` is specified for this property, the interception mechanism will use CGLib proxies on each specific `PropertySource` implementation. This may be useful on some scenarios where the type of the original `PropertySource` must be preserved. 

## <a name="customEncryptor"></a>Use you own Custom Encryptor
For custom configuration of the encryptor and the source of the encryptor password you can always define your own StringEncryptor bean in your Spring Context, and the default encryptor will be ignored. For instance:

```java
    @Bean("jasyptStringEncryptor")
    static public StringEncryptor stringEncryptor() {
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("password");
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        return encryptor;
    }
```
Notice that the bean name is required, as `jasypt-spring-boot` detects custom String Encyptors by name as of version `1.5`. The default bean name is:

``` jasyptStringEncryptor ```

But one can also override this by defining the property:

``` jasypt.encryptor.bean ```

So for instance, if you define `jasypt.encryptor.bean=encryptorBean` then you would define your custom encryptor with that name:

```java
    @Bean("encryptorBean")
    static public StringEncryptor stringEncryptor() {
        ...
    }
```

**Note:** Notice the bean is declared `static`. This is necessary for this library's `BeanDefinitionRegistryPostProcessor` to find the custom bean. 


## <a name="customPropertyFinder"></a>Use you own Custom Property Finder
If the format of your encrypted properties do not follow the Jasypt format of ENC(xxx) you can always define your own PropertyFinder bean in your Spring Context, and the default JasyptPropertyFinder will be ignored. For instance:

```java
    @Bean(name = "jasyptPropertyFinder")
    static public PropertyFinder propertyFinder() {
        return new MyPropertyFinder();
    }

    static class MyPropertyFinder implements PropertyFinder {
        private static final String ENCRYPTED_VALUE_PREFIX = "ENC[";
        private static final String ENCRYPTED_VALUE_SUFFIX = "]";

        @Override
        public boolean isEncryptedValue(String stringValue) {
            if (stringValue == null) {
                return false;
            }
            final String trimmedValue = stringValue.trim();
            return trimmedValue.startsWith(ENCRYPTED_VALUE_PREFIX) && trimmedValue.endsWith(ENCRYPTED_VALUE_SUFFIX);
        }

        @Override
        public String decrypt(String encodedValue, StringEncryptor encryptor) {
            return encryptor.decrypt(getInnerEncryptedValue(encodedValue.trim()));
        }

        private String getInnerEncryptedValue(final String value) {
            return value.substring(ENCRYPTED_VALUE_PREFIX.length(), (value.length() - ENCRYPTED_VALUE_SUFFIX.length()));
        }
    }
```
You need to provide an implementation of a method that extracts out the inner encrypted value before passing it along to the provided StringEncryptor implementation. 
The example subclass above should be sufficient for most use cases.

Note that the bean name is required, as `jasypt-spring-boot` detects custom Property Finders by name as of version `1.9`. The default bean name is:

``` jasyptPropertyFinder ```

But one can also override this by defining the property:

``` jasypt.propertyFinder.bean ```

So for instance, if you define `jasypt.propertyFinder.bean=finderBean` then you would define your custom finder with that name:

```java
    @Bean(name = "finderBean")
    static public PropertyFinder propertyFinder() {
        ...
    }
```

**Note:** Notice the bean is declared `static`. This is necessary for this library's `BeanDefinitionRegistryPostProcessor` to find the custom bean. 

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
