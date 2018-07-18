package com.ulisesbocchio.jasyptspringboot;

import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEByteEncryptor;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimplePBEStringEncryptor;
import lombok.SneakyThrows;
import org.jasypt.salt.RandomSaltGenerator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EncryptorTest {

    private SimplePBEByteEncryptor _PBEWITHHMACSHA512ANDAES_256 = null;
    private SimplePBEStringEncryptor stringEncryptor = null;
    
    @BeforeClass
    public static void setupClass() {
        System.out.println("====== Algorithms ==========");
        System.out.println(Stream.of(Security.getProviders()).map(EncryptorTest::getProviderDetails).collect(Collectors.joining("\n")));
        System.out.println("===========================");
        System.out.println();
    }

    @Before
    public void setup() {
        setup_PBEWITHHMACSHA512ANDAES_256();
        setup_stringEncryptor();
    }

    private static String getProviderDetails(Provider provider) {
        final String serviceName = "Cipher";
        final String type = "PBE";
        return provider.getName() + "\n\t"
                + provider.keySet().stream()
                .map(k -> ((String) k).toUpperCase())
                .filter(k -> k.startsWith(serviceName.toUpperCase()) && !k.contains(" "))
                .map(k -> k.substring(serviceName.length() + 1))
                .filter(k -> k.startsWith(type))
                .collect(Collectors.joining("\n\t"));
    }

    private void setup_stringEncryptor() {
        stringEncryptor = new SimplePBEStringEncryptor(_PBEWITHHMACSHA512ANDAES_256);
    }

    @SneakyThrows
    private void setup_PBEWITHHMACSHA512ANDAES_256() {
        SimplePBEByteEncryptor encryptor = new SimplePBEByteEncryptor();
        encryptor.setPassword("some password loco");
        encryptor.setSaltGenerator(new RandomSaltGenerator());
        encryptor.setIterations(1000);
        encryptor.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        _PBEWITHHMACSHA512ANDAES_256 = encryptor;
    }

    @Test
    public void test_PBEWITHHMACSHA512ANDAES_256_encryption() {
        final String message = "This is the secret message... BOOHOOO!";
        final byte[] messageBytes = message.getBytes(StandardCharsets.US_ASCII);

        final byte[] ciphertext = _PBEWITHHMACSHA512ANDAES_256.encrypt(messageBytes);

        final String decrypted = new String(
                _PBEWITHHMACSHA512ANDAES_256.decrypt(ciphertext),
                StandardCharsets.US_ASCII);

        Assert.assertEquals(message, decrypted);
    }

    @Test
    public void test_StringEncrytor_encryption() {
        final String message = "This is the secret message... BOOHOOO!";

        final String ciphertext = stringEncryptor.encrypt(message);

        final String decrypted = stringEncryptor.decrypt(ciphertext);

        Assert.assertEquals(message, decrypted);
    }
}
