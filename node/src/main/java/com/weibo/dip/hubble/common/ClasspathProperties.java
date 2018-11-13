package com.weibo.dip.hubble.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.Properties;
import org.apache.commons.lang.CharEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Load key/value from file in classpath.
 *
 * @author yurun
 */
public class ClasspathProperties {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathProperties.class);

  protected Properties properties;

  protected ClasspathProperties() {}

  /**
   * Construct properties with specified fileName and default time interval.
   *
   * @param fileName classpath: file name
   * @throws Exception if classpath:fileName does not exist, or load properties error
   */
  public ClasspathProperties(String fileName) throws Exception {
    properties = new Properties();

    BufferedReader reader = null;

    try {
      reader =
          new BufferedReader(
              new InputStreamReader(
                  ClasspathProperties.class.getClassLoader().getResourceAsStream(fileName),
                  CharEncoding.UTF_8));

      properties.load(reader);

      LOGGER.info("classpath:{} load success", fileName);
    } finally {
      if (Objects.nonNull(reader)) {
        reader.close();
      }
    }
  }

  public Properties getProperties() {
    return properties;
  }

  /**
   * Get the value(String) through the specified key.
   *
   * @param key the specified key
   * @return string value, "" if the specified key does not exist
   */
  public String getString(String key) {
    return properties.getProperty(key, "");
  }

  /**
   * Get the value(int) through the specified key.
   *
   * @param key the specified key
   * @return int value, -1 if the specified key does not exist
   * @throws NumberFormatException if the value(String) can not convert to int
   */
  public int getInt(String key) throws NumberFormatException {
    return Integer.valueOf(properties.getProperty(key, "-1"));
  }

  /**
   * Get the value(long) through the specified key.
   *
   * @param key the specified key
   * @return long value, -1 if the specified key does not exist
   * @throws NumberFormatException if the value(String) can not convert to long
   */
  public long getLong(String key) throws NumberFormatException {
    return Long.valueOf(properties.getProperty(key, "-1L"));
  }

  /**
   * Get the value(float) through the specified key.
   *
   * @param key the specified key
   * @return float value, -1 if the specified key does not exist
   * @throws NumberFormatException if the value(String) can not convert to float
   */
  public float getFloat(String key) throws NumberFormatException {
    return Float.valueOf(properties.getProperty(key, "-1.0F"));
  }

  /**
   * Get the value(double) through the specified key.
   *
   * @param key the specified key
   * @return double value, -1 if the specified key does not exist
   * @throws NumberFormatException if the value(String) can not convert to double
   */
  public double getDouble(String key) throws NumberFormatException {
    return Double.valueOf(properties.getProperty(key, "-1.0D"));
  }

  /**
   * Get the value(boolean) through the specified key.
   *
   * @param key the specified key
   * @return boolean value, false if the specified key does not exist
   */
  public boolean getBoolean(String key) {
    return Boolean.valueOf(properties.getProperty(key, "false"));
  }

  @Override
  public String toString() {
    return properties.toString();
  }
}
