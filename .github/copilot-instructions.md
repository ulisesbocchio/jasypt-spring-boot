# GitHub Copilot instructions (JetBrains IntelliJ)

This repo is a multi-module Maven build.

- Java: 17 (code should compile on Java 17).
- Frameworks: Spring Boot 3.5.x, Spring Cloud 2023.0.x, Jasypt 1.9.3.
- Prefer BOM-managed dependency versions from the parent `pom.xml`; avoid adding new dependencies unless necessary.
- Preserve module boundaries (`jasypt-spring-boot`, `jasypt-spring-boot-starter`, `jasypt-maven-plugin`); don’t move code across modules unless required.
- Keep public API stable: prefer additive changes, avoid breaking signatures/behavior without tests and docs updates.
- Testing: use JUnit 5; prefer existing test utilities already used in the project.
- Security: never log secrets (passwords, private keys, or decrypted property values).

Module notes:

- jasypt-spring-boot: core library that wraps Spring `PropertySource` so `ENC(...)` values decrypt transparently; keep Spring bootstrapping listeners (`META-INF/spring.factories`) intact.
- jasypt-spring-boot-starter: starter wiring for auto-configuration + Spring Cloud bootstrap; keep metadata files (`spring.factories`, `AutoConfiguration.imports`) consistent with class names and keep tests green.
- jasypt-maven-plugin: Maven plugin goals (encrypt/decrypt/encrypt-value/decrypt-value/reencrypt/upgrade/load); keep goal names/parameters stable and keep plugin lightweight (no logback/log4j).

For deeper module-specific guidance, see:
- `.github/copilot-jasypt-spring-boot.md`
- `.github/copilot-jasypt-spring-boot-starter.md`
- `.github/copilot-jasypt-maven-plugin.md`
