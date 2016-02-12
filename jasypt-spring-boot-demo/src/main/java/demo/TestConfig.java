package demo;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ulises Bocchio
 */
@Configuration
@EncryptablePropertySource(name = "caca", value = "classpath:caca.properties", ignoreResourceNotFound = true)
public class TestConfig {
}
