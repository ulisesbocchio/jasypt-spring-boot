package com.ulisesbocchio.jasyptspringboot.wrapper;

import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver;
import com.ulisesbocchio.jasyptspringboot.EncryptablePropertySource;
import com.ulisesbocchio.jasyptspringboot.caching.CachingDelegateEncryptablePropertySource;
import org.springframework.boot.origin.Origin;
import org.springframework.boot.origin.SystemEnvironmentOrigin;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * <p>EncryptableSystemEnvironmentPropertySourceWrapper class.</p>
 *
 * @author Tomas Tulka (@ttulka)
 * @version $Id: $Id
 */
public class EncryptableSystemEnvironmentPropertySourceWrapper extends SystemEnvironmentPropertySource
    implements EncryptablePropertySource<Map<String, Object>> {


  /**
   * A map that will wrap the System environment variables map and decrypt them.
   */
  private static class DecryptingMap extends AbstractMap<String, Object> {

    final CachingDelegateEncryptablePropertySource<Map<String, Object>> encryptableDelegate;


    DecryptingMap(SystemEnvironmentPropertySource delegate, EncryptablePropertyResolver resolver, EncryptablePropertyFilter filter) {
      encryptableDelegate = new CachingDelegateEncryptablePropertySource<>(delegate, resolver, filter);
    }

    @Override
    public int size() {
      return encryptableDelegate.getSource().size();
    }

    @Override
    public boolean isEmpty() {
      return encryptableDelegate.getSource().isEmpty();
    }

    @Override
    public Set<String> keySet() {
      return encryptableDelegate.getSource().keySet();
    }

    @Override
    public boolean containsKey(Object key) {
      return encryptableDelegate.getSource().containsKey(key);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
      HashSet<Entry<String, Object>> entries = new HashSet<>();
      Set<String> keys = encryptableDelegate.getSource().keySet();
      for (String key : keys) {
        entries.add(new AbstractMap.SimpleEntry<>(key, encryptableDelegate.getProperty(key)));
      }
      return entries;
    }

  }

  private final CachingDelegateEncryptablePropertySource<Map<String, Object>> encryptableDelegate;


  /**
   * <p>Constructor for EncryptableSystemEnvironmentPropertySourceWrapper.</p>
   *
   * @param delegate a {@link org.springframework.core.env.SystemEnvironmentPropertySource} object
   * @param resolver a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyResolver} object
   * @param filter   a {@link com.ulisesbocchio.jasyptspringboot.EncryptablePropertyFilter} object
   */
  public EncryptableSystemEnvironmentPropertySourceWrapper(
      SystemEnvironmentPropertySource delegate,
      EncryptablePropertyResolver resolver,
      EncryptablePropertyFilter filter
  ) {
    super(delegate.getName(), new DecryptingMap(delegate, resolver, filter));
    encryptableDelegate = ((DecryptingMap) getSource()).encryptableDelegate;
  }


  /** {@inheritDoc} */
  @Override
  public PropertySource<Map<String, Object>> getDelegate() {
    return encryptableDelegate;
  }


  /** {@inheritDoc} */
  @Override
  public Origin getOrigin(String key) {
    Origin fromSuper = EncryptablePropertySource.super.getOrigin(key);
    if (fromSuper != null) {
      return fromSuper;
    }
    String property = resolvePropertyName(key);
    if (super.containsProperty(property)) {
      return new SystemEnvironmentOrigin(property);
    }
    return null;
  }


  /** {@inheritDoc} */
  @Override
  public Object getProperty(String name) {
    return encryptableDelegate.getProperty(name);
  }

}
