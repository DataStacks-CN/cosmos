package com.weibo.dip.hubble.common;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global resource.
 *
 * @author yurun
 */
public class GlobalResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalResource.class);

  private static final Map<Class<?>, Object> RESOURCES = new HashMap<>();

  private GlobalResource() {}

  /**
   * Register resource.
   *
   * @param instance resource instance
   */
  public static void register(Object instance) {
    Class<?> clazz = instance.getClass();

    if (RESOURCES.containsKey(clazz)) {
      LOGGER.warn("Class {} instance already exists", clazz.getSimpleName());
      return;
    }

    RESOURCES.put(clazz, instance);
    LOGGER.info("Class {} instance registered", clazz.getName());
  }

  /**
   * Unregister resource.
   *
   * @param clazz resource class
   */
  public static <T> T unregister(Class<T> clazz) {
    LOGGER.info("Class {} instance unregistered", clazz.getSimpleName());

    return (T) RESOURCES.remove(clazz);
  }

  /**
   * Get resource.
   *
   * @param clazz resource class
   * @return resource instance
   */
  @SuppressWarnings("unchecked")
  public static <T> T get(Class<T> clazz) {
    if (!RESOURCES.containsKey(clazz)) {
      LOGGER.warn("Class {} instance does not exist", clazz.getSimpleName());
      return null;
    }

    return (T) RESOURCES.get(clazz);
  }

  /** Clear resources. */
  public static void clear() {
    RESOURCES.clear();
    LOGGER.info("global resource cleared");
  }
}
