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

    @Value("${secret2.property:defaultValue}")
    private String secret2;

    public String getSecret() {
        return secret;
    }
    public String getSecret2() {
        return secret2;
    }
}
