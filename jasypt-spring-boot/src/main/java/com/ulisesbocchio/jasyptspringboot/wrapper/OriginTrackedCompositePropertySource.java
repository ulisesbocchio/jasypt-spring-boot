package com.ulisesbocchio.jasyptspringboot.wrapper;

import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.OriginLookup;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;

public class OriginTrackedCompositePropertySource extends CompositePropertySource implements OriginLookup<String> {

	/**
	 * Create a new {@code CompositePropertySource}.
	 * @param name the name of the property source
	 */
	public OriginTrackedCompositePropertySource(String name) {
		super(name);
	}

	@Override
	@SuppressWarnings({"unchecked", "rawtypes"})
	public Origin getOrigin(String name) {
		for (PropertySource<?> propertySource : getPropertySources()) {
			if (propertySource instanceof OriginLookup) {
				OriginLookup lookup = (OriginLookup) propertySource;
				Origin origin = lookup.getOrigin(name);
				if (origin != null) {
					return origin;
				}
			}
		}
		return null;
	}

}
