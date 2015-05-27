# jasypt-spring-boot
**[Jasypt](http://jasypt.org)** integration for String boot

## What to do First?
Download and build a release artifact with maven and deploy it in your Maven repository since this library
is not in Maven Central or any other public repo.
Then add the dependency on your project:

```xml
    <dependency>
            <groupId>com.ulisesbocchio</groupId>
            <artifactId>jasypt-spring-boot</artifactId>
            <version>1.0</version>
    </dependency>
```
## How this Works?
First add @EnableEncryptableProperties to you Configuration class. For instance:

```java
    @SpringBootApplication
    @EnableEncryptableProperties
    public class MyApplication {
        ...
    }
```

This will trigger some configuration to be loaded that basically does 2 things:

1. It registers a Spring post processor that decorates all PropertySource objects contained in the Spring Environment so that thet are "encryption aware" and detect when properties are encrypted following jasypt's property convention.
2. It defines a a default StringEncryptor that can be configured through regular properties, system properties, or command line arguments.

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
  </table>

The only property required is the encryption password, the rest could be left to use default values. While all this properties could be declared in a properties file, the encryptor password should not be stored in a property file, it should rather be passed as system property, command line argument, or environment variable.

## Demo App
The [jasypt-spring-boot-demo](jasypt-spring-boot-demo) folder contains a working Spring Boot app example 
