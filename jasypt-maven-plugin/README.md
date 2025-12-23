# Jasypt Maven Plugin

A Maven plugin that provides utilities for encrypting and decrypting properties using Jasypt encryption in Spring Boot
applications.

## Overview

The Jasypt Maven Plugin offers several goals to help manage encrypted properties in your Spring Boot projects:

- **encrypt**: Encrypt properties in configuration files
- **decrypt**: Decrypt properties in configuration files
- **encrypt-value**: Encrypt a single value
- **decrypt-value**: Decrypt a single value
- **reencrypt**: Re-encrypt properties with new configuration
- **upgrade**: Upgrade encrypted properties to new default configuration
- **load**: Load and decrypt properties into Maven properties

## Installation

Add the plugin to your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.github.ulisesbocchio</groupId>
      <artifactId>jasypt-maven-plugin</artifactId>
      <version>4.0.0-SNAPSHOT</version>
    </plugin>
  </plugins>
</build>
```

## Configuration

The plugin uses the same configuration properties as jasypt-spring-boot. You can configure encryption settings through:

- System properties (`-Djasypt.encryptor.password=mypassword`)
- Environment variables
- Spring Boot configuration files (`application.properties`, `application.yml`)
- Command line arguments

### Common Configuration Properties

| Property                                    | Description              | Default                               |
|---------------------------------------------|--------------------------|---------------------------------------|
| `jasypt.encryptor.password`                 | Encryption password      | Required                              |
| `jasypt.encryptor.algorithm`                | Encryption algorithm     | `PBEWITHHMACSHA512ANDAES_256`         |
| `jasypt.encryptor.key-obtention-iterations` | Key obtention iterations | `1000`                                |
| `jasypt.encryptor.pool-size`                | Encryptor pool size      | `1`                                   |
| `jasypt.encryptor.provider-name`            | Security provider name   | `SunJCE`                              |
| `jasypt.encryptor.salt-generator-classname` | Salt generator class     | `org.jasypt.salt.RandomSaltGenerator` |
| `jasypt.encryptor.iv-generator-classname`   | IV generator class       | `org.jasypt.iv.RandomIvGenerator`     |
| `jasypt.encryptor.string-output-type`       | Output encoding          | `base64`                              |

### Asymmetric Encryption Properties

| Property                                | Description                            |
|-----------------------------------------|----------------------------------------|
| `jasypt.encryptor.private-key-string`   | Private key as string (base64 for DER) |
| `jasypt.encryptor.private-key-location` | Private key file location              |
| `jasypt.encryptor.private-key-format`   | Key format (`DER` or `PEM`)            |
| `jasypt.encryptor.public-key-string`    | Public key as string                   |
| `jasypt.encryptor.public-key-location`  | Public key file location               |
| `jasypt.encryptor.public-key-format`    | Key format (`DER` or `PEM`)            |

## Goals

### encrypt

Encrypts properties marked with `DEC(...)` in configuration files.

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="mypassword"
```

**Parameters:**

- `jasypt.plugin.path` - File path (default: `file:src/main/resources/application.properties`)

**Example:**

```properties
# Before encryption
database.password=DEC(mysecretpassword)
regular.property=normalvalue

# After encryption  
database.password=ENC(encrypted_value_here)
regular.property=normalvalue
```

### decrypt

Decrypts properties marked with `ENC(...)` and displays the result (does not modify files).

```bash
mvn jasypt:decrypt -Djasypt.encryptor.password="mypassword"
```

**Parameters:**

- `jasypt.plugin.path` - File path (default: `file:src/main/resources/application.properties`)

### encrypt-value

Encrypts a single value and displays the result.

```bash
mvn jasypt:encrypt-value -Djasypt.encryptor.password="mypassword" -Djasypt.plugin.value="valueToEncrypt"
```

**Parameters:**

- `jasypt.plugin.value` - The value to encrypt (required)

**Example:**

```bash
mvn jasypt:encrypt-value -Djasypt.encryptor.password="mypassword" -Djasypt.plugin.value="mysecretpassword"
# Output: ENC(encrypted_value_here)
```

### decrypt-value

Decrypts a single encrypted value and displays the result.

```bash
mvn jasypt:decrypt-value -Djasypt.encryptor.password="mypassword" -Djasypt.plugin.value="ENC(encrypted_value)"
```

**Parameters:**

- `jasypt.plugin.value` - The encrypted value to decrypt (required)

### reencrypt

Re-encrypts a file using old configuration to decrypt and new configuration to encrypt.

```bash
mvn jasypt:reencrypt -Djasypt.plugin.old.password="oldpassword" -Djasypt.encryptor.password="newpassword"
```

**Old Configuration Parameters (prefixed with `jasypt.plugin.old.`):**

- `password` - Old encryption password
- `algorithm` - Old algorithm
- `private-key-string` - Old private key
- `private-key-location` - Old private key location
- `private-key-format` - Old key format
- And other encryption properties...

### upgrade

Upgrades encrypted properties from an older version's default configuration to the current defaults.

```bash
mvn jasypt:upgrade -Djasypt.encryptor.password="mypassword"
```

**Parameters:**

- `jasypt.plugin.old.major-version` - Version to upgrade from (default: `2`)

### load

Loads and decrypts properties from a file into Maven properties for use by other plugins.

```bash
mvn jasypt:load flyway:migrate -Djasypt.encryptor.password="mypassword"
```

**Parameters:**

- `jasypt.plugin.path` - Properties file path (default: `file:src/main/resources/application.properties`)
- `jasypt.plugin.keyPrefix` - Prefix to add to property names

**Example with Flyway:**

```bash
mvn jasypt:load flyway:migrate -Djasypt.encryptor.password="mypassword" -Djasypt.plugin.keyPrefix="db."
```

## File Path Configuration

All file-based goals support the `jasypt.plugin.path` parameter to specify different files:

```bash
# Different file
mvn jasypt:encrypt -Djasypt.plugin.path="file:src/main/resources/database.properties"

# YAML file
mvn jasypt:encrypt -Djasypt.plugin.path="file:src/main/resources/application.yml"

# Test resources
mvn jasypt:encrypt -Djasypt.plugin.path="file:src/test/resources/application-test.properties"
```

## Spring Profiles

You can use Spring profiles when running the plugin:

```bash
mvn jasypt:encrypt -Dspring.profiles.active=production -Djasypt.encryptor.password="mypassword"
```

## Multi-Module Projects

For multi-module projects, use the `-N` flag to prevent recursion:

```bash
mvn jasypt:encrypt -Djasypt.plugin.path="file:module/src/main/resources/application.properties" -Djasypt.encryptor.password="mypassword" -N
```

## Asymmetric Encryption Example

Using PEM format keys:

```bash
mvn jasypt:encrypt-value \
  -Dspring.config.location="file:src/main/resources/application.yml" \
  -Djasypt.encryptor.public-key-format="PEM" \
  -Djasypt.encryptor.public-key-location="file:src/main/resources/publickey.pem" \
  -Djasypt.plugin.value="valueToEncrypt"
```

## Troubleshooting

### Missing Configuration

If you get configuration errors, ensure:

- The encryption password is provided
- Configuration files exist and are accessible
- Spring profiles are correctly specified

### Multi-Module Issues

- Use `-N` flag to avoid recursion
- Specify full paths to configuration files
- Ensure parent POM dependencies are available

### Logging Conflicts

The plugin includes `slf4j-simple` to avoid logging conflicts with Maven's logging system.

## Examples

### Basic Encryption Workflow

1. **Add encrypted placeholders to your properties:**

```properties
database.password=DEC(mysecretpassword)
api.key=DEC(myapikey)
```

2. **Encrypt the file:**

```bash
mvn jasypt:encrypt -Djasypt.encryptor.password="mypassword"
```

3. **Result:**

```properties
database.password=ENC(encrypted_value_1)
api.key=ENC(encrypted_value_2)
```

### Changing Encryption Password

```bash
mvn jasypt:reencrypt -Djasypt.plugin.old.password="oldpassword" -Djasypt.encryptor.password="newpassword"
```

### Using with CI/CD

```bash
# Encrypt in CI pipeline
mvn jasypt:encrypt -Djasypt.encryptor.password="${ENCRYPTION_PASSWORD}"

# Load properties for database migration
mvn jasypt:load flyway:migrate -Djasypt.encryptor.password="${ENCRYPTION_PASSWORD}"
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Spring Boot 3.5.0 or higher (for the underlying jasypt-spring-boot dependency)

## License

This project is licensed under the MIT License.