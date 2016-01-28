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
            <version>1.2</version>
    </dependency>
	```
2. IF you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations then add this dependency to your project and encryptable properties will be enabled across the entire Spring Environment (This means any system property, environment property, command line argument, application.properties, yaml properties, and any other custom property sources can contain encrypted properties):
	
	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>1.2</version>
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
3. IF you don't use `@SpringBootApplication` or `@EnableAutoConfiguration` Auto Configuration annotations and you don't want to enable encryptable properties across the entire Spring Environment, there's a third option. First add the following dependency to your project:
	
	```xml
    <dependency>
            <groupId>com.github.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>1.2</version>
    </dependency>
	```
	And then add as many `@EncryptablePropertySource` annotations as you want in your Configuration files. Just like you do with Spring's `@PropertySource` annotation. For instance:
	
	```java
	@Configuration
	@EncryptablePropertySource(name = "EncryptedProperties", value = "classpath:encrypted.properties", ignoreResourceNotFound = false)
	public class MyApplication {
		...
	}
	```

## How this Works?

This will trigger some configuration to be loaded that basically does 2 things:

1. It registers a Spring post processor that decorates all PropertySource objects contained in the Spring Environment so that thet are "encryption aware" and detect when properties are encrypted following jasypt's property convention.
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
Jasypt uses an `StringEncryptor` to decrypt properties. For all 3 methods, if no custom `StringEncryptor` is found in the Spring Context, one is created automatically that can be configured through the following properties (System, properties file, command line arguments, environment variable, etc.):

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

For custom configuration of the encryptor and the source of the encryptor password you can always define your own StringEncryptor bean in your Spring Context, and the default encryptor will be ignored. For instance:

```java
    @Bean
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

## Demo App
The [jasypt-spring-boot-demo](jasypt-spring-boot-demo) folder contains a working Spring Boot app example.
The Demo app explicitly sets a System property with the encryption password before the app in runned. To have a little more realistic scenario try removing the line where the system property is set, build the app with maven, and the run:

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
