package demo;

import com.ulisesbocchio.jasyptspringboot.annotation.EncryptablePropertySource;
import org.springframework.context.annotation.Configuration;

/**
 * @author Ulises Bocchio
 */
@Configuration
@EncryptablePropertySource(name = "caca", value = "caca.properties")
public class TestConfig {
}
