package com.ulisesbocchio.jasyptspringbootstarter.resolver;


import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.net.URI;

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
        properties = {
                "spring.config.use-legacy-processing=true",
                "server.port=9625"
        },
        classes = TestApp.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT
)
class DefaultPropertyResolverTest {

    @Value("${spring.cloud.config.server.svn.password}")
    public String secret;

    public int port = 9625;

    @Autowired
    private Environment env;

    @Test
    void encryptedPropertyIsDecryptedInEnvironment() {
        assertThat(env.getProperty("spring.cloud.config.server.svn.password")).isEqualTo("mypassword");
        assertThat(secret).isEqualTo("mypassword");
    }

    @Test
    void propertiesAreAccessibleFromEnvActuator() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(ErrorHandler.DEFAULT);
        ResponseEntity<?> response = restTemplate
                .exchange(RequestEntity.get(URI.create("http://localhost:" + port + "/actuator/env")).build(), JsonNode.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private interface ErrorHandler extends org.springframework.web.client.ResponseErrorHandler {

        ErrorHandler DEFAULT = new ErrorHandler() {
        };

        @Override
        default boolean hasError(@NonNull ClientHttpResponse response) throws IOException {
            return false; // Don't handle errors by default
        }

        @Override
        default void handleError(@NonNull URI url, @NonNull HttpMethod method, @NonNull ClientHttpResponse response) throws IOException {
            // Do nothing by default
        }
    }
}
