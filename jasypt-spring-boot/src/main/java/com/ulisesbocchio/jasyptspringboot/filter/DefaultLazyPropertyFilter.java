package com.ulisesbocchio.jasyptspringboot.filter;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.util.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.PropertySource;

import java.util.List;
import java.util.Optional;

import static com.ulisesbocchio.jasyptspringboot.util.Functional.tap;

@Slf4j
public class DefaultLazyPropertyFilter implements EncryptablePropertyFilter {

    private Singleton<EncryptablePropertyFilter> singleton;

    public DefaultLazyPropertyFilter(List<String> includeSources, List<String> excludeSources, List<String> includeNames, List<String> excludeNames, String customFilterBeanName, BeanFactory bf) {
        singleton = new Singleton<>(() ->
                Optional.of(customFilterBeanName)
                        .filter(bf::containsBean)
                        .map(name -> (EncryptablePropertyFilter) bf.getBean(name))
                        .map(tap(bean -> log.info("Found Custom Filter Bean {} with name: {}", bean, customFilterBeanName)))
                        .orElseGet(() -> {
                            log.info("Property Filter custom Bean not found with name '{}'. Initializing Default Property Filter", customFilterBeanName);
                            return new DefaultPropertyFilter(includeSources, excludeSources, includeNames, excludeNames);
                        }));
    }

    @Override
    public boolean shouldInclude(PropertySource<?> source, String name) {
        return singleton.get().shouldInclude(source, name);
    }
}
