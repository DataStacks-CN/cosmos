package com.weibo.dip.cosmos.node.example;

import java.util.Objects;

/**
 * Hello application.
 *
 * @author yurun
 */
public class AppParams {
  /**
   * Main.
   *
   * @param args (name, queue, user, time, log)
   */
  public static void main(String[] args) {
    if (Objects.isNull(args)) {
      System.err.println("no args");
    }

    System.out.println("App params: " + args.length);

    for (int index = 0; index < args.length; index++) {
      System.out.println(index + ": " + args[index]);
    }
  }
}
