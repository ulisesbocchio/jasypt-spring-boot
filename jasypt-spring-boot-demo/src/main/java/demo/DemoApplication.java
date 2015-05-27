package demo;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;

/**
 * Sample Boot application that showcases easy integration of Jasypt encryption by
 * simply adding {@literal @EnableEncryptableProperties} to any Configuration class.
 * For decryption a password is required and is set through system properties in this example,
 * but it could be passed command line argument too like this: --jasypt.encryptor.password=password
 *
 * @author Ulises Bocchio
 */
@SpringBootApplication
@EnableEncryptableProperties
@PropertySource(name="EncryptedProperties", value = "encrypted.properties")
public class DemoApplication implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(DemoApplication.class);

    @Autowired
    ApplicationContext appCtx;

    public static void main(String[] args) {
        System.setProperty("jasypt.encryptor.password", "password");
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        MyService service = appCtx.getBean(MyService.class);
        LOG.info("MyService's secret: {}", service.getSecret());
        LOG.info("Done!");
    }
}
