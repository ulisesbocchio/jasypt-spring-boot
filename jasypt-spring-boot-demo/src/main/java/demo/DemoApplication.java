package demo;

import com.ulisesbocchio.jasyptspringboot.InterceptionMode;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.style.ToStringCreator;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Sample Boot application that showcases easy integration of Jasypt encryption by
 * simply adding {@literal @EnableEncryptableProperties} to any Configuration class.
 * For decryption a password is required and is set through system properties in this example,
 * but it could be passed command line argument too like this: --jasypt.encryptor.password=password
 *
 * @author Ulises Bocchio
 */
@SpringBootApplication
@PropertySource(name="EncryptedProperties", value = "classpath:encrypted.properties")
@EncryptablePropertySource(name="EncryptedProperties2", value = "classpath:encrypted2.properties")
@Import(TestConfig.class)
//Uncomment this if not using jasypt-spring-boot-starter (use jasypt-spring-boot) dependency in pom instead
@EnableEncryptableProperties
@EnableConfigurationProperties(ItemConfig.class)
public class DemoApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    ApplicationContext appCtx;

    public static void main(String[] args) {
        //try commenting the following line out and run the app from the command line passing the password as
        //a command line argument: java -jar target/jasypt-spring-boot-demo-0.0.1-SNAPSHOT.jar --jasypt.encryptor.password=password
        System.setProperty("jasypt.encryptor.password", "password");
        //Enable proxy mode for intercepting encrypted properties
        //System.setProperty("jasypt.encryptor.proxyPropertySources", "true");
        SpringApplication.run(DemoApplication.class, args);
    }

//Uncomment this code block for custom StringEncryptor configuration
//    @Bean
//    static public StringEncryptor stringEncryptor() {
//        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
//        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
//        config.setPassword("password");
//        config.setAlgorithm("PBEWithMD5AndDES");
//        config.setKeyObtentionIterations("1000");
//        config.setPoolSize("1");
//        config.setProviderName("SunJCE");
//        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
//        config.setStringOutputType("base64");
//        encryptor.setConfig(config);
//        return encryptor;
//    }

    @Override
    public void run(String... args) throws Exception {
        MyService service = appCtx.getBean(MyService.class);
        LOG.info("MyService's secret: {}", service.getSecret());
        ItemConfig itemConfig = appCtx.getBean(ItemConfig.class);
        LOG.info("ItemConfig: {}", itemConfig);
        Environment env = appCtx.getEnvironment();
        LOG.info("Secret from @EncryptablePropertySource annotation: {}", env.getProperty("secret2.property"));
        LOG.info("Done!");
    }
}
