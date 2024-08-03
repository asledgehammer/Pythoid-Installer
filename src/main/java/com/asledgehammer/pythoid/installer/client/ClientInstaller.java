package com.asledgehammer.pythoid.installer.client;

import com.asledgehammer.util.IOUtil;
import com.asledgehammer.util.PZUtil;
import com.asledgehammer.util.ZipUtil;

import java.io.*;
import java.util.Scanner;

public class ClientInstaller {

  public static final String JYTHON_PATCH_ZIP_URL =
      "https://github.com/asledgehammer/Pythoid-Patch/archive/refs/heads/{VERSION}.zip";
  public static final String VERSION = "1.00_00";

  private final Scanner scanner;
  private String patchURL;
  private File patchFile;
  private String pzVersion;
  private File dirCache;
  private File dirPZ;
  private File dirJythonInstall;

  public ClientInstaller(String[] yargs) {

    printTitle();
    line();

    this.scanner = new Scanner(System.in);

    dirPZ = null;

    try {

      try {
        dirPZ = PZUtil.resolveProjectZomboidDirectory();

        System.out.println("Steam installation discovered:");
        System.out.println("\t" + dirPZ.getPath());

      } catch (Exception e) {
        System.out.println("!!! Failed to discover Steam installation !!!");
      }

      if (dirPZ == null) {
        dirPZ = requestProjectZomboidDirectory();
      }

      this.pzVersion = PZUtil.getPZVersion(dirPZ);
      if (pzVersion == null) {
        line("Couldn't detect PZ version. Using 'latest'..");
        pzVersion = "latest";
      }

      System.out.print("Creating installer cache.. ");

      this.dirCache = new File(dirPZ, ".pythoid-installer-cache");
      if (!this.dirCache.exists() && !this.dirCache.mkdirs()) {
        throw new RuntimeException("Failed to create directory: " + this.dirCache.getPath());
      }

      System.out.println("Success.");

      dirJythonInstall = new File(dirPZ, "jython2.7.3");

      if (!dirJythonInstall.exists()) {
        try {
          PZUtil.installJython(dirCache, dirJythonInstall);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        System.out.println("Jython 2.7.3 is already installed.");
      }

      requestBackupClassFolders();

      this.patchURL = JYTHON_PATCH_ZIP_URL.replaceAll("\\{VERSION}", pzVersion);
      this.patchFile = new File(dirCache, "Pythoid-Patch" + this.pzVersion + ".zip");

      if (!hasPatchFiles()) {
        IOUtil.downloadUsingNIO(this.patchURL, this.patchFile.getPath());
      }

      ZipUtil.extractZipFile(this.patchFile, new File(dirCache, "Pythoid-Path" + pzVersion));

    } catch (Exception e) {
      System.err.println("The installer has encountered an error and cannot recover.");
      e.printStackTrace(System.err);
    }

    try {
      if (!hasArg(yargs, "--keep-cache")) {
        System.out.print("Deleting installer cache.. ");
        if (this.dirCache.delete()) {
          System.out.println("Success.");
        } else {
          System.out.println("Failure.");
        }
      }
    } catch (Exception e) {
      System.err.println("Failed to cleanup resources.");
      e.printStackTrace();
    }

    scanner.close();
  }

  private boolean hasPatchFiles() {
    return this.patchFile.exists();
  }

  private void yesOrNo(String message, Runnable yes, Runnable no) {
    while (true) {
      System.out.println(message + " [Y/N]");
      String line = scanner.nextLine();
      if (line.toLowerCase().startsWith("y")) {
        yes.run();
        return;
      } else if (line.toLowerCase().startsWith("n")) {
        no.run();
        return;
      }
      System.out.println("No decision was entered.");
    }
  }

  private void requestBackupClassFolders() {
    yesOrNo(
        "Backup folders?",
        () -> {
          System.out.print("Backing up folders..");
          backupClassFolders();
          System.out.println("Success.");
        },
        () -> System.out.println("Skipping backup of folders."));
  }

  private void backupClassFolders() {
    try {
      ZipUtil.zipFolder(new File(dirPZ, "zombie").toPath(), new File(dirPZ, "zombie_backup.zip"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static boolean hasArg(String[] yargs, String arg) {
    for (String yarg : yargs) {
      if (yarg.contains(arg)) return true;
    }
    return false;
  }

  private void printTitle() {
    String firstString = "Pythoid Client Installer V" + VERSION;
    String borderString = "=".repeat(firstString.length());
    line(borderString);
    line();
    line(firstString);
    line();
    lineRight(borderString.length(), "By asledgehammer");
    line();
    line(borderString);
  }

  public static void lineRight(int length, Object line) {
    String s = line.toString();
    int subLength = Math.max(0, length - s.length());
    System.out.println(" ".repeat(subLength) + s);
  }

  public File requestProjectZomboidDirectory() {
    System.out.println("Please enter the PZ directory: (Leave blank to exit installer)");
    boolean found = false;
    while (!found) {
      String line = scanner.nextLine();

      if (line.isEmpty()) {
        System.exit(0);
      }

      File dirLine = new File(line);
      if (!dirLine.exists()) {
        System.out.println("Directory does not exist. Please try again.");
        continue;
      }

      if (!PZUtil.isValidPZDirectory(dirLine)) {
        System.out.println("Directory doesn't contain PZ files. Please try again.");
        continue;
      }

      System.out.println("Directory is valid.");

      return dirLine;
    }

    return null;
  }

  public static void line(Object line) {
    System.out.println(line);
  }

  public static void line() {
    System.out.println();
  }

  public static void main(String[] yargs) {
    new ClientInstaller(yargs);
  }
}
