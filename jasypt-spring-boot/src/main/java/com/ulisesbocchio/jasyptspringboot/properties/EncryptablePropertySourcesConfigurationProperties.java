package com.ulisesbocchio.jasyptspringboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Not Actually used. To this moment this class is only added for meta data auto generation.
 * By the time this configuration is used the configuration properties mechanism doesn't actually
 * load the properties property.
 * @author Ulises Bocchio, Sergio.U.Bocchio@Disney.com (BOCCS002)
 */
@ConfigurationProperties(prefix = "jasypt.encryptor", ignoreUnknownFields = true)
public class EncryptablePropertySourcesConfigurationProperties {

  /**
   * Whether to use JDK/Cglib (depending on classpath availability) proxy with an AOP advice as a decorator for existing {@link org.springframework.core.env.PropertySource}
   * or just simply use targeted wrapper Classes.
   * Default Value is {@code false}.
   */
  private Boolean proxyPropertySources = false;

  public Boolean getProxyPropertySources() {
    return proxyPropertySources;
  }

  public void setProxyPropertySources(Boolean proxyPropertySources) {
    this.proxyPropertySources = proxyPropertySources;
  }
}
