package com.ulisesbocchio.jasyptspringboot.filter;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.PropertySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.ulisesbocchio.jasyptspringboot.util.Collections.concat;
import static com.ulisesbocchio.jasyptspringboot.util.Functional.tap;
import static java.util.Collections.singletonList;

@Slf4j
public class DefaultLazyPropertyFilter implements EncryptablePropertyFilter {

    private static final List<String> DEFAULT_FILTER_EXCLUDE_PATTERNS = singletonList("^jasypt\\.encryptor\\.*");
    private Singleton<EncryptablePropertyFilter> singleton;

    public DefaultLazyPropertyFilter(List<String> includeSources, List<String> excludeSources, List<String> includeNames, List<String> excludeNames, String customFilterBeanName, BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customFilterBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (EncryptablePropertyFilter) bf.getBean(name))
                        .map(tap(bean -> log.info("Found Custom Filter Bean {} with name: {}", bean, customFilterBeanName)))
                        .orElseGet(() -> {
                            List<String> actualExcludeNames = concat(Optional.ofNullable(excludeNames).orElseGet(ArrayList::new), DEFAULT_FILTER_EXCLUDE_PATTERNS);
                            log.info("Property Filter custom Bean not found with name '{}'. Initializing Default Property Filter", customFilterBeanName);
                            return new DefaultPropertyFilter(includeSources, excludeSources, includeNames, actualExcludeNames);
                        }));
    }

    @Override
    public boolean shouldInclude(PropertySource<?> source, String name) {
        return singleton.get().shouldInclude(source, name);
    }
}
