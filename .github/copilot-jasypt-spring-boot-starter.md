# Module playbook: `jasypt-spring-boot-starter`

This module is the “wiring” layer. Adding the starter to an application should:
- import the core configuration (`EnableEncryptablePropertiesConfiguration`)
- expose the right Spring Boot and Spring Cloud metadata so enabling works automatically

---

## 1) Metadata wiring (public contract)

These files are part of the starter’s contract. If you rename/move classes, update them together.

- `jasypt-spring-boot-starter/src/main/resources/META-INF/spring.factories`
  - `EnableAutoConfiguration=com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration`
  - `BootstrapConfiguration=com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringCloudBootstrapConfiguration`

- `jasypt-spring-boot-starter/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
  - `com.ulisesbocchio.jasyptspringbootstarter.JasyptSpringBootAutoConfiguration`

---

## 2) Entry points (the code paths)

### Boot auto-configuration
- `JasyptSpringBootAutoConfiguration`
  - `@Import(EnableEncryptablePropertiesConfiguration.class)`

### Spring Cloud bootstrap configuration
- `JasyptSpringCloudBootstrapConfiguration`
  - `@ConditionalOnProperty(name = "jasypt.encryptor.bootstrap", havingValue = "true", matchIfMissing = true)`
  - `@Import(EnableEncryptablePropertiesConfiguration.class)`

This means:
- `jasypt.encryptor.bootstrap=true` (default) -> enables jasypt wiring in the bootstrap phase
- `jasypt.encryptor.bootstrap=false` -> disables the bootstrap config and relies on “normal” auto-config timing

---

## 3) Behavioral contracts / invariants

### Core expectations
With the starter on the classpath:
- encrypted values must decrypt via `Environment#getProperty` and `@Value`
- applications should start cleanly under web environments

### Actuator stability
- `/actuator/env` must remain accessible (should not error due to wrapped property sources)

### Spring Cloud bootstrap behavior
When `spring.cloud.bootstrap.enabled=true`:
- encrypted values in `bootstrap.properties` must decrypt during the bootstrap phase if `jasypt.encryptor.bootstrap=true`

---

## 4) Tests-as-spec (use these as the living documentation)

### Bootstrap-phase behavior
- `BootstrappingJasyptConfigurationTest`
  - Asserts the “real bug” scenario and the fix:
    - with `spring.cloud.bootstrap.enabled=true` and `jasypt.encryptor.bootstrap=false`
      - the environment property is *not* decrypted during bootstrap listener timing
    - with `spring.cloud.bootstrap.enabled=true` and `jasypt.encryptor.bootstrap=true`
      - the environment property *is* decrypted during bootstrap listener timing
  - Also asserts `EnableEncryptablePropertiesBeanFactoryPostProcessor` exists in both bootstrap-enabled and disabled paths.

### Environment + @Value decryption + actuator env
- `resolver/DefaultPropertyResolverTest`
  - asserts the decrypted property is visible through:
    - `Environment#getProperty`
    - `@Value("${...}")`
  - asserts `/actuator/env` responds `200 OK`.

### Environment-variable binding formats
- `EnvironmentVariablesLoadTest`
  - asserts relaxed-binding formats for env vars bind into `@ConfigurationProperties` (important for real-world deployments)

---

## 5) Changes that usually require tests

If you change:
- metadata files (`spring.factories`, `AutoConfiguration.imports`) -> ensure tests still load auto-config
- bootstrap ordering or listener behavior -> extend `BootstrappingJasyptConfigurationTest`
- property-source wrapping semantics -> keep `DefaultPropertyResolverTest` (and actuator part) green

---

## 6) Quick local validation

Run:
- starter module only: `mvn -pl jasypt-spring-boot-starter -am test`
- full repo tests: `mvn test`
