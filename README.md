# jasypt-spring-boot
**[Jasypt](http://jasypt.org)** integration for String boot

[![Build Status](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot.svg?branch=master)](https://travis-ci.org/ulisesbocchio/jasypt-spring-boot)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ulisesbocchio/jasypt-spring-boot?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/com.github.ulisesbocchio/jasypt-spring-boot)

## What to do First?
Download and build a release artifact of `jasypt-spring-boot` (and optionally `jasypt-spring-boot-starter`) with maven and deploy it to your Maven repository since this library
is not in Maven Central or any other public repo.
Then add this dependency to your project if you plan to enable the Jasypt using Annotations:

```xml
    <dependency>
            <groupId>com.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>1.0</version>
    </dependency>
```
Or if you plan to use Auto Configuration add only this one:

```xml
    <dependency>
            <groupId>com.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot-starter</artifactId>
            <version>1.0</version>
    </dependency>
```
## How this Works?
Simply add @EnableEncryptableProperties to you Configuration class. For instance:

```java
    @SpringBootApplication
    @EnableEncryptableProperties
    public class MyApplication {
        ...
    }
```
**Or** 

If you use Auto Configuration (i.e. using annotations @SpringBootApplication  or @EnableAutoConfiguration) just make sure `jasypt-spring-boot-starter` is in your classpath and you don't need to add any extra annotation. Please just use one of the methods described and not both.


This will trigger some configuration to be loaded that basically does 2 things:

1. It registers a Spring post processor that decorates all PropertySource objects contained in the Spring Environment so that thet are "encryption aware" and detect when properties are encrypted following jasypt's property convention.
2. It defines a a default StringEncryptor that can be configured through regular properties, system properties, or command line arguments.

## Where do I put my encrypted properties?
You can define encrypted properties in any of the PropertySource contained in the Environment. For instance, using the @PropertySource annotation:

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
Now when you do `environment.getProperty("secret.property")` or use `@Value("${secret.property}")` what you get is the decrypted version of secret.property.

## Encryption Configuration
If no custom StringEncryptor is found in the Spring Context, one is created automatically that can be configured through the following properties (System, properties file, command line arguments, environment variable, etc.):

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

The last property, `jasypt.encryptor.proxyPropertySources` is used to indicate `jasyp-spring-boot` how property values are going to be intercepted for decryption. The default value, `false` uses custom wrapper implementations of `PropertySource`, `EnumerablePropertySource`, and `MapPropertySource`. When `true` is specify for this property, the interception mechanism will use CGLib proxies on each specific `PropertySource` implementation. This may be useful on some scenarios where the type of the original `PropertySource` must be preserved. 

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
