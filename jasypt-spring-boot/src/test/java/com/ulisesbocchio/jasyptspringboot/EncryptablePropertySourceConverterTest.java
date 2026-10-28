package com.ulisesbocchio.jasyptspringboot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EncryptablePropertySourceConverterTest {

    private static final String DYNAMIC_PROPERTY_SOURCE = "org.springframework.test.context.support.DynamicValuesPropertySource";

    @ParameterizedTest
    @EnumSource(InterceptionMode.class)
    void shouldSkipDynamicPropertySources(InterceptionMode mode) throws Exception {
        Constructor<?> constructor = Class.forName(DYNAMIC_PROPERTY_SOURCE).getDeclaredConstructor();
        constructor.setAccessible(true);
        PropertySource<?> source = (PropertySource<?>) constructor.newInstance();
        EncryptablePropertySourceConverter converter = converter(mode, Collections.emptyList());

        assertSame(source, converter.makeEncryptable(source));
    }

    @ParameterizedTest
    @EnumSource(InterceptionMode.class)
    void shouldKeepExplicitSkipClasses(InterceptionMode mode) {
        MapPropertySource source = new MapPropertySource("custom", Collections.emptyMap());
        EncryptablePropertySourceConverter converter = converter(mode,
                Collections.singletonList(EncryptablePropertySourceConverter.getPropertiesClass(MapPropertySource.class.getName())));

        assertSame(source, converter.makeEncryptable(source));
    }

    @ParameterizedTest
    @EnumSource(InterceptionMode.class)
    void shouldStillConvertOrdinaryPropertySources(InterceptionMode mode) {
        MapPropertySource source = new MapPropertySource("custom", Map.of("key", "value"));
        PropertySource<?> converted = converter(mode, Collections.emptyList()).makeEncryptable(source);

        assertInstanceOf(EncryptablePropertySource.class, converted);
        assertEquals("value", converted.getProperty("key"));
    }

    @Test
    void shouldRejectInvalidExplicitSkipClasses() {
        assertThrows(IllegalArgumentException.class,
                () -> EncryptablePropertySourceConverter.getPropertiesClass("missing.PropertySource"));
        assertThrows(IllegalArgumentException.class,
                () -> EncryptablePropertySourceConverter.getPropertiesClass(String.class.getName()));
    }

    @Test
    void shouldConstructWithoutSpringTestOnTheClasspath() throws Exception {
        String converterName = EncryptablePropertySourceConverter.class.getName();
        URL location = EncryptablePropertySourceConverter.class.getProtectionDomain().getCodeSource().getLocation();
        try (FilteredClassLoader parent = new FilteredClassLoader("org.springframework.test");
             URLClassLoader loader = new URLClassLoader(new URL[]{location}, parent) {
                 @Override
                 protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                     if (!name.equals(converterName)) {
                         return super.loadClass(name, resolve);
                     }
                     Class<?> loaded = findLoadedClass(name);
                     if (loaded == null) {
                         loaded = findClass(name);
                     }
                     if (resolve) {
                         resolveClass(loaded);
                     }
                     return loaded;
                 }
             }) {
            assertThrows(ClassNotFoundException.class, () -> loader.loadClass(DYNAMIC_PROPERTY_SOURCE));
            Class<?> isolatedConverter = loader.loadClass(converterName);
            assertSame(loader, isolatedConverter.getClassLoader());
            assertNotNull(isolatedConverter.getConstructor(InterceptionMode.class, List.class,
                            EncryptablePropertyResolver.class, EncryptablePropertyFilter.class)
                    .newInstance(InterceptionMode.WRAPPER, Collections.emptyList(),
                            (EncryptablePropertyResolver) value -> value,
                            (EncryptablePropertyFilter) (source, name) -> true));
        }
    }

    private EncryptablePropertySourceConverter converter(InterceptionMode mode, List<Class<PropertySource<?>>> skipClasses) {
        return new EncryptablePropertySourceConverter(mode, skipClasses, value -> value, (source, name) -> true);
    }
}
