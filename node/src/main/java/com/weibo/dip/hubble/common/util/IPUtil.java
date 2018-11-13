package com.weibo.dip.hubble.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * Ip util.
 *
 * @author yurun
 */
public class IPUtil {
  private static final String IP_REGEX =
      "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}";

  /**
   * Get localhost ip address.
   *
   * @return localhost ip address
   * @throws UnknownHostException if get error
   */
  public static String getLocalhost() throws UnknownHostException {
    return InetAddress.getLocalHost().getHostAddress();
  }

  /**
   * Generate a random ip address.
   *
   * @return ip
   */
  public static String randomIp() {
    StringBuilder ip = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      int rand = (int) (Math.random() * 255);
      if (i != 3) {
        ip.append(rand).append(".");
      } else {
        ip.append(rand);
      }
    }

    return ip.toString();
  }

  public static boolean isIp(String ip) {
    if (ip == null || ip.isEmpty()) {
      return false;
    }

    boolean isIp = false;

    try {
      isIp = RegexUtil.match(IP_REGEX, ip);
    } catch (Exception e) {
    }

    return isIp;
  }

  public static long ipToLong(String ip) throws NumberFormatException {
    if (ip == null || ip.isEmpty() || !isIp(ip)) {
      return -1;
    }

    long result = 0;

    StringTokenizer token = new StringTokenizer(ip, ".");

    result += Long.parseLong(token.nextToken()) << 24;
    result += Long.parseLong(token.nextToken()) << 16;
    result += Long.parseLong(token.nextToken()) << 8;
    result += Long.parseLong(token.nextToken());

    return result;
  }
}
