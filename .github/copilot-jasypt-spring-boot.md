# Module playbook: `jasypt-spring-boot` (core library)

This module is the **engine** that makes Spring `Environment` property sources “encryptable”, so values like `ENC(...)` decrypt transparently when accessed through Spring’s `Environment`, `@Value`, `@ConfigurationProperties`, etc.

It works by:
1) building a `StringEncryptor` + detector/resolver/filter (lazy by default)
2) converting `PropertySource` instances into encryptable wrappers (or proxies)
3) optionally converting the `MutablePropertySources` container itself
4) ensuring **bootstrap (Spring Cloud)** and **logging initialization** see decrypted values early enough

---

## 1) Where it starts (entrypoints)

### Metadata wiring (must remain intact)
- `jasypt-spring-boot/src/main/resources/META-INF/spring.factories`
  - `org.springframework.context.ApplicationListener`:
    - `com.ulisesbocchio.jasyptspringboot.configuration.BootstrapSpringApplicationListener`
  - `org.springframework.boot.env.EnvironmentPostProcessor`:
    - `com.ulisesbocchio.jasyptspringboot.configuration.EncryptableLoggingEnvironmentListener`
    - `com.ulisesbocchio.jasyptspringboot.configuration.EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener`

### Bootstrap detection + bootstrapping
- `configuration/BootstrapSpringApplicationListener`
  - listens to `ApplicationEnvironmentPreparedEvent`
  - detects a “bootstrap” context by checking for a PropertySource named `bootstrap`
  - looks for the special property source inserted by this library: `jasyptInitializer` and calls `EnvironmentInitializer#initializeBootstrap`.

### Late environment post-processing
- `configuration/EncryptableLoggingEnvironmentListener` (an `EnvironmentPostProcessor`, ordered `HIGHEST_PRECEDENCE + 13`)
  - also detects `bootstrap` environments and triggers `EnvironmentInitializer#initializeBootstrap`
  - the goal is to ensure logging config sees decrypted values.

### System env property source `getSource()` wrapping
- `configuration/EncryptableSystemEnvironmentPropertySourceWrapperGetSourceWrapperEnvironmentListener` (an `EnvironmentPostProcessor`)
  - runs after Spring Boot’s `SystemEnvironmentPropertySourceEnvironmentPostProcessor`
  - toggles `EncryptableSystemEnvironmentPropertySourceWrapper#setWrapGetSource(true)` so downstream systems that call `getSource()` get an encryptable map view.

### BeanFactory post-processing (wrapping + logging reset)
- `configuration/EnableEncryptablePropertiesBeanFactoryPostProcessor`
  - wraps all property sources via `EncryptablePropertySourceConverter#convertPropertySources`
  - forces the `EncryptableSystemEnvironmentPropertySourceWrapper` getSource wrapping
  - reinitializes `LoggingSystem` by re-firing `ApplicationEnvironmentPreparedEvent` through `LoggingApplicationListener`

---

## 2) Core flow and “why it’s built this way”

### The initializer hook (critical cross-module contract)
- `environment/EnvironmentInitializer`
  - `initialize(EncryptableEnvironment)` converts original property sources, then builds encryptable `MutablePropertySources`
  - inserts a `MapPropertySource` named `jasyptInitializer` with key `"jasypt.initializer.instance"` pointing back to the initializer instance

This inserted `jasyptInitializer` source is a **deliberate hook** used by:
- `BootstrapSpringApplicationListener` (bootstrap-phase wrapping)
- `EncryptableLoggingEnvironmentListener` and `EnableEncryptablePropertiesBeanFactoryPostProcessor` (logging reinit + bootstrap)

If you remove or rename it, bootstrap + logging behavior breaks.

### Converting property sources: wrapper vs proxy
- `EncryptablePropertySourceConverter`
  - `convertPropertySources(MutablePropertySources)` replaces each entry with an encryptable wrapper/proxy
  - `makeEncryptable(PropertySource)`:
    - skips already-encryptable sources
    - skips classes configured in `jasypt.encryptor.skip-property-sources` plus defaults:
      - `PropertySource$StubPropertySource`
      - `ConfigurationPropertySourcesPropertySource`
    - chooses **proxy** or **wrapper** interception based on `InterceptionMode`

`InterceptionMode`:
- `WRAPPER`: uses concrete wrapper implementations (`EncryptableMapPropertySourceWrapper`, `EncryptableEnumerablePropertySourceWrapper`, etc.)
- `PROXY`: uses Spring AOP proxy + interceptors in `aop/*`
  - automatically falls back to wrappers for `CommandLinePropertySource` and final classes.

### Lazy resolution pipeline
Default selection is **lazy** and environment-driven:
- `DefaultLazyEncryptor`:
  - uses `jasypt.encryptor.bean` (default `jasyptStringEncryptor`) to select a custom bean
  - otherwise builds a default encryptor using `StringEncryptorBuilder` + `JasyptEncryptorConfigurationProperties.bindConfigProps`.
- `DefaultLazyPropertyResolver`:
  - uses `jasypt.encryptor.property.resolver-bean` / detector bean / filter bean indirections
  - otherwise creates `DefaultPropertyResolver(encryptor, detector, environment)`

---

## 3) Configuration surface area (what to be careful with)

The main config model is:
- `properties/JasyptEncryptorConfigurationProperties`
  - `@ConfigurationProperties(prefix = "jasypt.encryptor")`

Important knobs people rely on:
- `jasypt.encryptor.password` (required for default PBE; also relevant for GCM password mode)
- `jasypt.encryptor.algorithm`, `pool-size`, `key-obtention-iterations`, etc.
- `jasypt.encryptor.proxy-property-sources` (switches to `InterceptionMode.PROXY`)
- `jasypt.encryptor.skip-property-sources` (list of `PropertySource` *class names* to skip)
- Asymmetric key modes:
  - `privateKey*` / `publicKey*` and formats (DER/PEM)
- GCM key modes:
  - `gcmSecretKeyString`, `gcmSecretKeyLocation`, `gcmSecretKeyPassword`, `gcmSecretKeySalt`, `gcmSecretKeyAlgorithm`
- Cache refresh events:
  - `jasypt.encryptor.refreshed-event-classes` (see `caching/RefreshScopeRefreshedEventListener`)

Also documented at the metadata layer:
- `META-INF/additional-spring-configuration-metadata.json`:
  - `jasypt.encryptor.bootstrap` (bootstrap behavior)

---

## 4) Invariants (things you should not break)

### Bootstrap + logging correctness
- In a Spring Cloud bootstrap environment (presence of `bootstrap` property source), encrypted values must be decryptable early.
- Logging must be reinitialized after wrapping so that encrypted values used by logging config don’t leak into the logging system as ciphertext.

### Compatibility with Spring Boot environment internals
- Do not break the default skip list or the ability to skip specific property source classes.
- Don’t assume property source classes are non-final (proxy mode must fall back gracefully).
- Do not “eagerly decrypt” in logs. This project logs *class names and decisions*, but **must not log decrypted values**.

### Public API/SPI stability
The following are effectively public contracts:
- `EncryptablePropertyResolver`, `EncryptablePropertyDetector`, `EncryptablePropertyFilter`
- `EncryptablePropertySource` marker + wrapper types
- `InterceptionMode`
- the hook property source name `jasyptInitializer` and key `jasypt.initializer.instance` (used across boot phases)

---

## 5) Tests-as-spec (keep these green, extend them when changing behavior)

- `src/test/java/com/ulisesbocchio/jasyptspringboot/EncryptorTest`
  - acts as a spec for supported encryptor modes:
    - PBE (default algorithm)
    - asymmetric DER/PEM (file/resource/string)
    - GCM (secret key, key location, password-derived, pooled)
  - note: the test prints encrypted material; avoid adding anything that prints *decrypted secrets* beyond what exists.

- `src/test/java/com/ulisesbocchio/jasyptspringboot/filter/DefaultPropertyFilterTest`
  - defines the include/exclude semantics and the regex matching behavior
  - also exercises generic type assignability helpers used by wrappers

When changing:
- `EncryptablePropertySourceConverter` / wrappers / proxy logic -> add a focused unit test (or extend existing ones)
- bootstrap/logging ordering -> add or update an integration-style test in starter (that’s where bootstrap behavior is asserted)

---

## 6) Common edge-cases (protect against regressions)

- Multiline encrypted values (should not break resolution)
- `@ConfigurationProperties` + Spring Boot’s `ConfigurationPropertySources` should keep working even when property sources are wrapped
- `SystemEnvironmentPropertySource` behavior: `getSource()` wrapping must remain lazy and safe
- Proxy mode must never proxy a final `PropertySource` or command-line sources; wrappers must be the fallback

---

## 7) Quick local validation

Run:
- whole build: `mvn test`
- core module only: `mvn -pl jasypt-spring-boot -am test`
