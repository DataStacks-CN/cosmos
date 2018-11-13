package com.weibo.dip.hubble.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Regex util.
 *
 * @author yurun
 */
public class RegexUtil {
  private static final Map<String, Pattern> PATTERNS = new HashMap<>();

  private static synchronized Pattern getPattern(String regex) throws PatternSyntaxException {
    Pattern pattern = PATTERNS.get(regex);

    if (pattern == null) {
      pattern = Pattern.compile(regex);

      PATTERNS.put(regex, pattern);
    }

    return pattern;
  }

  public static boolean match(String regex, String value) throws PatternSyntaxException {
    if (regex == null || value == null) {
      return false;
    }

    Pattern pattern = getPattern(regex);

    Matcher matcher = pattern.matcher(value);

    return matcher.matches();
  }

  public static String[] split(String regex, String value) throws PatternSyntaxException {
    if (value == null) {
      return null;
    }

    return value.split(regex);
  }

  public static String[] split(String regex, String value, int limit) {
    if (value == null) {
      return null;
    }

    return value.split(regex, -1);
  }

  public static String[] groups(String regex, String value)
      throws PatternSyntaxException, IllegalStateException, IndexOutOfBoundsException {
    String[] columns = null;

    Pattern pattern = getPattern(regex);

    Matcher matcher = pattern.matcher(value);

    if (matcher.matches()) {
      columns = new String[matcher.groupCount()];

      for (int index = 0; index < columns.length; index++) {
        columns[index] = matcher.group(index + 1);
      }
    }

    return columns;
  }
}
