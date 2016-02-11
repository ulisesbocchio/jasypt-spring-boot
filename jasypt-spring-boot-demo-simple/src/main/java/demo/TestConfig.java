package demo;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.springframework.context.annotation.Configuration;

/**
 * Sample Configuration File that imports an Encryptable Property Source
 *
 * @author Ulises Bocchio
 */
@Configuration
@EncryptablePropertySource(name = "encrypted2", value = "encrypted.properties")
public class TestConfig {
}
