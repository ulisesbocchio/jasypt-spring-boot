package com.ulisesbocchio.jasyptspringboot.filter;

import com.ulisesbocchio.jasyptspringboot.util.ClassUtils;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableConfigurationPropertySourcesPropertySource;
import com.ulisesbocchio.jasyptspringboot.wrapper.EncryptableMapPropertySourceWrapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DefaultPropertyFilterTest {

    @Test
    public void should_be_assignable_to_generic_type() {
        assertTrue(ClassUtils.isAssignable(new ParameterizedTypeReference<PropertySource<Iterable<ConfigurationPropertySource>>>() {
        }, EncryptableConfigurationPropertySourcesPropertySource.class));
        assertFalse(ClassUtils.isAssignable(new ParameterizedTypeReference<PropertySource<Map<String, Object>>>() {
        }, EncryptableConfigurationPropertySourcesPropertySource.class));
        assertTrue(ClassUtils.isAssignable(new ParameterizedTypeReference<PropertySource<Map<String, Object>>>() {
        }, EncryptableMapPropertySourceWrapper.class));
        assertFalse(ClassUtils.isAssignable(new ParameterizedTypeReference<PropertySource<Iterable<ConfigurationPropertySource>>>() {
        }, EncryptableMapPropertySourceWrapper.class));
    }

    @Test
    public void shouldInclude_includeAll() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withExclusions_source_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("config"), null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withExclusions_source_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("config.*"), null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withExclusions_property_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, Collections.singletonList("some"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withExclusions_property_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, Collections.singletonList(".*\\.some"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withExclusions_source_and_property() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("config"), null, Collections.singletonList("some"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_source_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("applicationConfig"), null, null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_source_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("application.*"), null, null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_property_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, Collections.singletonList("some.property"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_property_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, Collections.singletonList("some\\..*"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_property_and_source() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("applicationConfig"), null, Collections.singletonList("some\\..*"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_source_regex_and_property_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList(".*"), null, Collections.singletonList("some.other.property"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldInclude_withInclusions_withExclusions_property_and_source() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("applicationConfig"), Collections.singletonList("config"), Collections.singletonList("some\\..*"), Collections.singletonList("some"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertTrue(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_excludeAll_source() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList(".*"), null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_excludeAll_property() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, Collections.singletonList(".*"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withExclusions_source_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("applicationConfig"), null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withExclusions_source_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("application.*"), null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withExclusions_property_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, Collections.singletonList("some\\.property"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withExclusions_property_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, null, Collections.singletonList("some\\..*"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withExclusions_source_and_property() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, Collections.singletonList("applicationConfig"), null, Collections.singletonList("some.property"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_source_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("bootstrapConfig"), null, null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_source_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("bootstrap.*"), null, null, null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_property_exact() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, Collections.singletonList("some.other.property"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_property_regex() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(null, null, Collections.singletonList("some\\.property\\..*"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_property_and_source() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList("bootstrap"), null, Collections.singletonList("some\\.property\\..*"), null);
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }

    @Test
    public void shouldExclude_withInclusions_withExclusions_property_and_source() {
        DefaultPropertyFilter filter = new DefaultPropertyFilter(Collections.singletonList(".*"), Collections.singletonList("applicationConfig"), Collections.singletonList(".*"), Collections.singletonList("some\\.property\\..*"));
        MapPropertySource source = new MapPropertySource("applicationConfig", Collections.emptyMap());
        assertFalse(filter.shouldInclude(source, "some.property"));
    }
}