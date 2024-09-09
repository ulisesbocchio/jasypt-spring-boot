package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.TestPropertySource;

import java.security.NoSuchProviderException;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@DisplayName("Aes Gcm Custom Provider")
class AesGcmEncryptorIntgnTest {

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    @EnableEncryptableProperties
    static class TestConfig {
    }

    @Nested
    @DisplayName("With Provider Class name")
    @SpringBootTest(classes = AesGcmEncryptorIntgnTest.TestConfig.class)
    @TestPropertySource(locations = "classpath:aes-gcm-provider-class.properties",
            properties = {"jasypt.encryptor.provider-class-name=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider"})
    class WithProviderClassName {

        @Autowired
        private Environment env;

        @Test
        @DisplayName("Decrypt app properties")
        void decryptAppProperties() {
            assertEquals("John Doe", env.getProperty("test.encrypted.name"));
        }
    }

    @Nested
    @DisplayName("With provider name")
    @SpringBootTest(classes = AesGcmEncryptorIntgnTest.TestConfig.class)
    @TestPropertySource(locations = "classpath:aes-gcm-provider-class.properties",
            properties = {"jasypt.encryptor.provider-name=BCFIPS"})
    class WithProviderName {

        @Autowired
        private Environment env;

        @Test
        @DisplayName("Decrypt app properties")
        void decryptAppProperties() {
            assertEquals("John Doe", env.getProperty("test.encrypted.name"));
        }
    }

    @Nested
    @DisplayName("With Invalid Provider Class name")
    @SpringBootTest(classes = AesGcmEncryptorIntgnTest.TestConfig.class)
    @TestPropertySource(locations = "classpath:aes-gcm-provider-class.properties",
            properties = {"jasypt.encryptor.provider-class-name=org.bouncycastle.jashoua.provider.BouncyCastleFipsProvider"})
    class WithInvalidProviderClassName {

        @Autowired
        private Environment env;

        @Test
        @DisplayName("Decrypt app properties")
        void decryptAppProperties() {
            assertThrows(ClassNotFoundException.class, () -> env.getProperty("test.encrypted.name"));
        }
    }

    @Nested
    @DisplayName("With invalid provider name")
    @SpringBootTest(classes = AesGcmEncryptorIntgnTest.TestConfig.class)
    @TestPropertySource(locations = "classpath:aes-gcm-provider-class.properties",
            properties = {"jasypt.encryptor.provider-name=BCSPIF"})
    class WithInvalidProviderName {

        @Autowired
        private Environment env;

        @Test
        @DisplayName("Decrypt app properties")
        void decryptAppProperties() {
            assertThrows(NoSuchProviderException.class, () -> env.getProperty("test.encrypted.name"));
        }
    }
}
