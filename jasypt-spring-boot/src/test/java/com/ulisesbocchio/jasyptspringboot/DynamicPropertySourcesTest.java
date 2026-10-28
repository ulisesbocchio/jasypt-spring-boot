package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig(DynamicPropertySourcesTest.TestConfiguration.class)
class DynamicPropertySourcesTest {

    private static final AtomicReference<String> dynamicValue = new AtomicReference<>("initial");

    @Autowired
    private Environment environment;

    @Value("${from.registrar}")
    private String registeredValue;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("from.method", dynamicValue::get);
    }

    @Test
    void shouldPreserveDynamicPropertySourcesAndRegistrars() {
        assertEquals("initial", environment.getProperty("from.method"));
        assertEquals("registrar", registeredValue);
        try {
            dynamicValue.set("updated");
            assertEquals("updated", environment.getProperty("from.method"));
        } finally {
            dynamicValue.set("initial");
        }
    }

    @Configuration
    @EnableEncryptableProperties
    static class TestConfiguration {
        @Bean
        DynamicPropertyRegistrar dynamicPropertyRegistrar() {
            return registry -> registry.add("from.registrar", () -> "registrar");
        }
    }
}
