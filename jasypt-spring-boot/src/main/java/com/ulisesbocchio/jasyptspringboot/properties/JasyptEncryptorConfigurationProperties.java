package com.ulisesbocchio.jasyptspringboot.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Not Actually used. To this moment this class is only added for meta data auto generation.
 * By the time this configuration is used the configuration properties mechanism doesn't actually
 * load the properties property.
 * @author Ulises Bocchio
 */
@ConfigurationProperties(prefix = "jasypt.encryptor", ignoreUnknownFields = true)
public class JasyptEncryptorConfigurationProperties {

  /**
   * Master Password used for Encryption/Decryption of properties.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getPassword()
   */
  private String password;

  /**
   * Encryption/Decryption Algorithm to be used by Jasypt.
   * For more info on how to get available algorithms visit: <a href="http://www.jasypt.org/cli.html"/>Jasypt CLI Tools Page</a>.
   * Default Value is {@code "PBEWithMD5AndDES"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getAlgorithm()
   */
  private String algorithm = "PBEWithMD5AndDES";

  /**
   * Number of hashing iterations to obtain the signing key.
   * Default Value is {@code "1000"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getKeyObtentionIterations()
   */
  private String keyObtentionIterations = "1000";

  /**
   * The size of the pool of encryptors to be created.
   * Default Value is {@code "1"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getPoolSize()
   */
  private String poolSize = "1";

  /**
   * The name of the {@link java.security.Provider} implementation
   * to be used by the encryptor for obtaining the encryption algorithm.
   * Default Value is {@code "SunJCE"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getProviderName()
   */
  private String providerName = "SunJCE";

  /**
   * A {@link org.jasypt.salt.SaltGenerator} implementation to be used by the
   * encryptor.
   * Default Value is {@code "org.jasypt.salt.RandomSaltGenerator"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getSaltGenerator()
   */
  private String saltGeneratorClassname = "org.jasypt.salt.RandomSaltGenerator";

  /**
   * Specify the form in which String output will be encoded. {@code "base64"} or {@code "hexadecimal"}.
   * Default Value is {@code "base64"}.
   *
   * @see org.jasypt.encryption.pbe.PBEStringEncryptor
   * @see org.jasypt.encryption.pbe.config.StringPBEConfig#getStringOutputType()
   */
  private String stringOutputType = "base64";

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAlgorithm() {
    return algorithm;
  }

  public void setAlgorithm(String algorithm) {
    this.algorithm = algorithm;
  }

  public String getKeyObtentionIterations() {
    return keyObtentionIterations;
  }

  public void setKeyObtentionIterations(String keyObtentionIterations) {
    this.keyObtentionIterations = keyObtentionIterations;
  }

  public String getPoolSize() {
    return poolSize;
  }

  public void setPoolSize(String poolSize) {
    this.poolSize = poolSize;
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  public String getSaltGeneratorClassname() {
    return saltGeneratorClassname;
  }

  public void setSaltGeneratorClassname(String saltGeneratorClassname) {
    this.saltGeneratorClassname = saltGeneratorClassname;
  }

  public String getStringOutputType() {
    return stringOutputType;
  }

  public void setStringOutputType(String stringOutputType) {
    this.stringOutputType = stringOutputType;
  }
}
