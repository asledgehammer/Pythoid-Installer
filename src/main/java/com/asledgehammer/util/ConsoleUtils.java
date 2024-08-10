package com.asledgehammer.util;

import java.util.Scanner;

public final class ConsoleUtils {

  private ConsoleUtils() {
    throw new RuntimeException("Cannot instantiate ConsoleUtils!");
  }

  public static boolean hasArg(String[] yargs, String arg) {
    for (String yarg : yargs) {
      if (yarg.contains(arg)) return true;
    }
    return false;
  }

  public static void yesOrNo(Scanner scanner, String message, Runnable yes, Runnable no) {
    while (true) {
      println(message + " [Y/N]");
      String line = scanner.nextLine();
      if (line.equalsIgnoreCase("y")) {
        yes.run();
        return;
      } else if (line.equalsIgnoreCase("n")) {
        no.run();
        return;
      }
      println("No decision was entered.");
    }
  }

  public static void yesOrNo(Scanner scanner, String message, Runnable yes) {
    while (true) {
      println(message + " [Y/N]");
      String line = scanner.nextLine();
      if (line.equalsIgnoreCase("y")) {
        yes.run();
        return;
      } else if (line.equalsIgnoreCase("n")) {
        return;
      }
      println("No decision was entered.");
    }
  }

  public static void printlnRight(int length, Object line) {
    String s = line.toString();
    int subLength = Math.max(0, length - s.length());
    System.out.println(" ".repeat(subLength) + s);
  }

  public static void println(Object line) {
    System.out.println(line);
  }

  public static void println() {
    System.out.println();
  }

  public static void print(Object line) {
    System.out.print(line);
  }

  public static void errln(Object line) {
    System.err.println(line);
  }

  public static void errln() {
    System.err.println();
  }

  public static void err(Object line) {
    System.err.print(line);
  }
}
