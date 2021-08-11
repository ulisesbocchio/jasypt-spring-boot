package com.ulisesbocchio.jasyptspringboot;

import org.springframework.core.env.PropertySource;

/**
 * Interface that provides skip PropertySourcesby jasypt.
 *
 * @author qxo
 *
 */
public interface SkipPropertySourceFilter {

	/**
	 * SPI for skip propertySource.
	 *
	 * @param <T>
	 * @param propertySource
	 * @return
	 */
	public <T> boolean shouldSkip(PropertySource<T> propertySource);
}
