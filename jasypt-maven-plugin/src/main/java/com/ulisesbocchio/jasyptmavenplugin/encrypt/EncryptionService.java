package com.ulisesbocchio.jasyptmavenplugin.encrypt;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.DOTALL;

/**
 * A service for encrypting and decrypting Strings.
 *
 * @author Rupert Madden-Abbott
 * @version $Id: $Id
 */
@Slf4j
public class EncryptionService {
    private final StringEncryptor encryptor;
    private final Pattern reCharsREP;

    @SuppressWarnings("ReplaceAllDot")
    /**
     * <p>Constructor for EncryptionService.</p>
     *
     * @param encryptor a {@link org.jasypt.encryption.StringEncryptor} object
     */
    public EncryptionService(final StringEncryptor encryptor) {
        this.encryptor = encryptor;
        String regExSpecialChars = "<([{\\^-=$!|]})?*+.>";
        String regExSpecialCharsRE = regExSpecialChars.replaceAll(".", "\\\\$0");
        this.reCharsREP = Pattern.compile("[" + regExSpecialCharsRE + "]");
    }


    private String quoteRegExSpecialChars(String s) {
        Matcher m = reCharsREP.matcher(s);
        return m.replaceAll("\\\\$0");
    }

    /**
     * Replace all instance of pattern in the templateText, according to the replacer.
     *
     * @param templateText the template
     * @param sourcePrefix property prefix
     * @param sourceSuffix property suffix
     * @param targetPrefix property prefix
     * @param targetSuffix property suffix
     * @param mutator      the replacement generator
     * @return the replaced content
     */
    private String replaceAll(
            final String templateText,
            final String sourcePrefix,
            final String sourceSuffix,
            final String targetPrefix,
            final String targetSuffix,
            final Function<String, String> mutator
    ) {
      String regex = quoteRegExSpecialChars(sourcePrefix) + "(.*?)" + quoteRegExSpecialChars(sourceSuffix);
      Pattern pattern = Pattern.compile(regex, DOTALL);
        Matcher matcher = pattern.matcher(templateText);
        StringBuffer result = new StringBuffer();
        String replacement;
        while (matcher.find()) {
            String matched = matcher.group(1);
            replacement = targetPrefix + mutator.apply(matched) + targetSuffix;
            log.debug("Converting value {} to {}", matched, replacement);
            matcher.appendReplacement(result, "");
            result.append(replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    /**
     * Decrypt all placeholders in the input.
     *
     * @param input the string to scan for placeholders and decrypt
     * @return the input with decrypted placeholders.
     * @param encryptPrefix a {@link java.lang.String} object
     * @param encryptSuffix a {@link java.lang.String} object
     * @param decryptPrefix a {@link java.lang.String} object
     * @param decryptSuffix a {@link java.lang.String} object
     */
    public String decrypt(final String input, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) {
        return replaceAll(input, encryptPrefix, encryptSuffix, decryptPrefix, decryptSuffix, encryptor::decrypt);
    }

    /**
     * Decrypt a value
     *
     * @param value the value
     * @return decrypted value
     */
    public String decryptValue(String value) {
        return encryptor.decrypt(value);
    }

    /**
     * Encrypt all placeholders in the input.
     *
     * @param input the string to scan for placeholders and encrypt
     * @return the input with encrypted placeholders.
     * @param encryptPrefix a {@link java.lang.String} object
     * @param encryptSuffix a {@link java.lang.String} object
     * @param decryptPrefix a {@link java.lang.String} object
     * @param decryptSuffix a {@link java.lang.String} object
     */
    public String encrypt(final String input, String encryptPrefix, String encryptSuffix, String decryptPrefix, String decryptSuffix) {
        return replaceAll(input, decryptPrefix, decryptSuffix, encryptPrefix, encryptSuffix, encryptor::encrypt);
    }

    /**
     * Encrypt a value
     *
     * @param value the value
     * @return encrypted value
     */
    public String encryptValue(String value) {
        return encryptor.encrypt(value);
    }

    /**
     * <p>getEncryptableProperties.</p>
     *
     * @return a {@link org.jasypt.properties.EncryptableProperties} object
     */
    public EncryptableProperties getEncryptableProperties() {
        return new EncryptableProperties(encryptor);
    }
}
