package demo;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * Sample Configuration File that imports a Property Source
 *
 * @author Ulises Bocchio
 */
@Configuration
@PropertySource(name = "encrypted", value = "encrypted.properties")
public class TestConfig {
}
