package com.ulisesbocchio.jasyptspringboot.wrapper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMConfig;
import com.ulisesbocchio.jasyptspringboot.encryptor.SimpleGCMStringEncryptor;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultLazyPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.filter.DefaultPropertyFilter;
import com.ulisesbocchio.jasyptspringboot.resolver.DefaultPropertyResolver;
import org.jasypt.encryption.StringEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.source.ConfigurationProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertyName;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.mock.env.MockEnvironment;

class EncryptableSystemEnvironmentPropertySourceWrapperTest {

  @Test
  void environmentVariablesAreDecrypted() {
    SimpleGCMConfig simpleGCMConfig = new SimpleGCMConfig();
    simpleGCMConfig.setActualKey(new SecretKeySpec(new byte[] { 0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31 },"AES"));
    StringEncryptor stringEncryptor = new SimpleGCMStringEncryptor(simpleGCMConfig);

    MockEnvironment environment = new MockEnvironment();

    HashMap<String,Object> map = new HashMap<>();
    map.put("TEST_KEY_PLAIN", "PLAIN_VALUE");
    map.put("TEST_KEY_ENCRYPTED", "ENC(" + stringEncryptor.encrypt("ENCRYPTED_VALUE") + ")");

    SystemEnvironmentPropertySource delegate = new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, map);
    EncryptablePropertyResolver resolver = new DefaultPropertyResolver(stringEncryptor,environment);
    EncryptablePropertyFilter filter = new DefaultLazyPropertyFilter(environment);

    EncryptableSystemEnvironmentPropertySourceWrapper wrapper = new EncryptableSystemEnvironmentPropertySourceWrapper(delegate, resolver, filter);

    ConfigurationPropertySource configurationPropertySource = ConfigurationPropertySource.from(wrapper);

    ConfigurationProperty value = configurationPropertySource.getConfigurationProperty(ConfigurationPropertyName.of("test.key.plain"));
    assertEquals("PLAIN_VALUE", value.getValue());

    value = configurationPropertySource.getConfigurationProperty(ConfigurationPropertyName.of("test.key.encrypted"));
    assertEquals("ENCRYPTED_VALUE", value.getValue());
  }

}
