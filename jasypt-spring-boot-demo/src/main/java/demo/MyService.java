package demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author Ulises Bocchio
 */
@Component
public class MyService {
    @Value("${secret.property:defaultValue}")
    private String secret;

    public String getSecret() {
        return secret;
    }
}
