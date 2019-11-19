package com.ulisesbocchio.jasyptmavenplugin.encrypt;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.regex.Pattern.DOTALL;

/**
 * A service for encrypting and decrypting Strings.
 *
 * @author Rupert Madden-Abbott
 */
public class EncryptionService {
  private static final Pattern ENCRYPTED_PATTERN = Pattern.compile("ENC\\((.*?)\\)", DOTALL);

  private static final Pattern DECRYPTED_PATTERN = Pattern.compile("DEC\\((.*?)\\)", DOTALL);

  private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionService.class);

  private final StringEncryptor encryptor;

  public EncryptionService(final StringEncryptor encryptor) {
    this.encryptor = encryptor;
  }

  /**
   * Replace all instance of pattern in the templateText, according to the replacer.
   * @param templateText the template
   * @param pattern the pattern to replace
   * @param replacer the replacement generator
   * @return the replaced content
   */
  private static String replaceAll(
      final String templateText, final Pattern pattern,
      final Function<String, String> replacer
  ) {
    Matcher matcher = pattern.matcher(templateText);
    StringBuffer result = new StringBuffer();
    String replace;
    while (matcher.find()) {
      replace = replacer.apply(matcher.group(1));
      matcher.appendReplacement(result, "");
      result.append(replace);
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Execute the supplied mutator on the contents.
   * @param contents the contents to mutate
   * @param pattern the pattern to identify placeholders
   * @param prefix the prefix for the mutated placeholder
   * @param mutationName the name of the operation
   * @param mutator the operation
   * @return the mutated contents
   */
  private String run(
      final String contents, final Pattern pattern, final String prefix,
      final String mutationName, final Function<String, String> mutator
  ) {
    return replaceAll(contents, pattern, matched -> {
      String mutatedValue = prefix + "(" + mutator.apply(matched) + ")";
      LOGGER.debug("{} value {} to {}", mutationName, matched, mutatedValue);
      return mutatedValue;
    });
  }

  /**
   * Decrypt all placeholders in the input.
   * @param input the string to scan for placeholders and decrypt
   * @return the input with decrypted placeholders.
   */
  public String decrypt(final String input) {
    return run(input, ENCRYPTED_PATTERN, "DEC", "Decrypted", encryptor::decrypt);
  }

  /**
   * Encrypt all placeholders in the input.
   * @param input the string to scan for placeholders and encrypt
   * @return the input with encrypted placeholders.
   */
  public String encrypt(final String input) {
    return run(input, DECRYPTED_PATTERN, "ENC", "Encrypted", encryptor::encrypt);
  }

  public EncryptableProperties getEncryptableProperties() {
    return new EncryptableProperties(encryptor);
  }
}
