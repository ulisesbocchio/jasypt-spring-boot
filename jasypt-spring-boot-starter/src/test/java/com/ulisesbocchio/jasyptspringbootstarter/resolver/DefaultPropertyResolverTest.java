package com.ulisesbocchio.jasyptspringbootstarter.resolver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.client.RestTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootApplication
class TestApp {
    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(TestApp.class)
                .run(args);
    }
}

@SpringBootTest(
        properties = "spring.config.use-legacy-processing=true",
        classes = TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureRestTestClient
class DefaultPropertyResolverTest {

    @Value("${spring.cloud.config.server.svn.password}")
    public String secret;

    @Autowired
    private Environment env;

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void encryptedPropertyIsDecryptedInEnvironment() {
        assertThat(env.getProperty("spring.cloud.config.server.svn.password")).isEqualTo("mypassword");
        assertThat(secret).isEqualTo("mypassword");
    }

    @Test
    void propertiesAreAccessibleFromEnvActuator() {
        restTestClient.get()
                .uri("/actuator/env")
                .exchange()
                .expectStatus().isOk();
    }
}
