package com.weibo.dip.hubble.scheduler.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Consule table.
 *
 * @author yurun
 */
public class ConsoleTable {
  private List<String[]> rows = new ArrayList<>();

  public ConsoleTable() {}

  public List<String[]> getRows() {
    return rows;
  }

  /**
   * Add row.
   *
   * @param columns columns
   */
  public void addRow(String... columns) {
    if (columns != null) {
      rows.add(columns);
    }
  }

  private String copy(String str, int times) {
    StringBuilder buffer = new StringBuilder();

    for (int index = 0; index < times; index++) {
      buffer.append(str);
    }

    return buffer.toString();
  }

  private String format(int[] columnLengths) {
    StringBuilder buffer = new StringBuilder("|");

    for (int columnLength : columnLengths) {
      buffer.append(" %-").append(columnLength).append("s |");
    }

    return buffer.toString();
  }

  private String line(int[] columnLengths) {
    StringBuilder buffer = new StringBuilder("+");

    for (int columnLength : columnLengths) {
      buffer.append(copy("-", columnLength + 2)).append("+");
    }

    return buffer.toString();
  }

  /** print. */
  public void print() {
    if (rows.size() <= 0) {
      return;
    }

    int columnLength = rows.get(0).length;

    if (columnLength <= 0) {
      return;
    }

    int[] columnLengths = new int[columnLength];

    String[][] array = rows.toArray(new String[0][]);

    for (int column = 0; column < columnLength; column++) {
      int max = Integer.MIN_VALUE;

      for (String[] anArray : array) {
        int length = anArray[column].length();

        if (length > max) {
          max = length;
        }
      }

      columnLengths[column] = max;
    }

    String line = line(columnLengths);

    String format = format(columnLengths);

    System.out.println(line);

    System.out.println(String.format(format, (Object[]) array[0]));

    System.out.println(line);

    for (int index = 1; index < array.length; index++) {
      System.out.println(String.format(format, (Object[]) array[index]));
    }

    System.out.println(line);

    System.out.println((rows.size() - 1) + " rows in set");
  }

  /**
   * Example.
   *
   * @param args no params
   */
  public static void main(String[] args) {
    ConsoleTable table = new ConsoleTable();

    table.addRow("a", "b", "c44223eeefwfewf");
    table.addRow("1", "234", "3");
    table.addRow("4dfsd", "5", "6");
    table.addRow("7", "8dfsfefwew", "9");

    table.print();
  }
}
