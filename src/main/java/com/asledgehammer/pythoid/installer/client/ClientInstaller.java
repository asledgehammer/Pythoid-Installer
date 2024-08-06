package com.asledgehammer.pythoid.installer.client;

import com.asledgehammer.util.IOUtil;
import com.asledgehammer.util.PZUtil;
import com.asledgehammer.util.ZipUtil;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static com.asledgehammer.util.IOUtil.*;

public class ClientInstaller {

  public static final String JYTHON_PATCH_ZIP_URL =
      "https://github.com/asledgehammer/Pythoid-Patch/archive/refs/heads/{VERSION}.zip";
  public static final String VERSION = "1.00_00";

  private final Scanner scanner;
  private int remotePatchVersion = -1;
  private int localPatchVersion = -1;
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

      this.remotePatchVersion = getRemotePatchVersion();
      this.localPatchVersion = getLocalPatchVersion();

      line(
          "Current Pythoid version: "
              + (localPatchVersion == -1 ? "(Not installed)" : localPatchVersion));
      line(
          "Latest Pythoid version: "
              + (remotePatchVersion == -1 ? "(Unknown)" : remotePatchVersion));

      if (localPatchVersion == -1 || remotePatchVersion > localPatchVersion) {
        line("A new version of Pythoid-Patch is available!");
        yesOrNo(
            "Apply patch?",
            () -> {
              try {
                applyPatch();
              } catch (Exception e) {
                e.printStackTrace(System.err);
              }
            },
            () -> {});

      } else {
        line("Pythoid-Patch is up-to-date and doesn't need to be downloaded & applied.");
      }

    } catch (Exception e) {
      System.err.println("The installer has encountered an error and cannot recover.");
      e.printStackTrace(System.err);
    }

    try {
      if (!hasArg(yargs, "--keep-cache")) {
        System.out.print("Deleting installer cache.. ");

        IOUtil.deleteDirectory(this.dirCache);

        System.out.println("Success.");
      }
    } catch (Exception e) {
      System.err.println("Failed to cleanup resources.");
      e.printStackTrace();
    }

    scanner.close();
  }

  private void applyPatch() throws IOException {
    this.patchURL = JYTHON_PATCH_ZIP_URL.replaceAll("\\{VERSION}", pzVersion);
    this.patchFile = new File(dirCache, "Pythoid-Patch" + this.pzVersion + ".zip");
    IOUtil.downloadUsingNIO(this.patchURL, this.patchFile.getPath());
    File dirPatch = new File(dirCache, "Pythoid-Patch" + pzVersion);
    ZipUtil.extractZipFile(this.patchFile, dirPatch);
    line("Applying Pythoid-Patch" + pzVersion + "..");
    copyDirectory(
        dirPatch.getPath() + File.separator + "Pythoid-Patch-" + pzVersion, dirPZ.getPath());
    line("Applied Pythoid-Patch" + pzVersion + ".");
  }

  private int getLocalPatchVersion() {

    File file = new File(dirPZ, "pythoid_version.txt");
    if (!file.exists()) {
      return -1;
    }

    try {
      FileReader fr = new FileReader(file);
      BufferedReader br = new BufferedReader(fr);
      String out = br.readLine();
      br.close();
      return Integer.parseInt(out.split(";")[1].trim());
    } catch (Exception e) {
      e.printStackTrace(System.err);
      return -1;
    }
  }

  private int getRemotePatchVersion() {
    try {
      String url =
          "https://raw.githubusercontent.com/asledgehammer/Pythoid-Patch/"
              + pzVersion
              + "/pythoid_version.txt";
      Scanner scanner =
          new Scanner(new URL(url).openStream(), StandardCharsets.UTF_8).useDelimiter("\\A");
      String out = scanner.next();
      scanner.close();
      return Integer.parseInt(out.split(";")[1].trim());
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
    return -1;
  }

  private boolean hasPatchFiles() {
    return this.patchFile.exists();
  }

  private void requestBackupClassFolders() {
    yesOrNo(
        "Backup folders?",
        () -> {
          File dirBackup = new File(dirCache, "backup_" + pzVersion);
          System.out.println("Backing up folders..");
          backupClassFolders(dirBackup);
          System.out.println("Completed backup \"backup_" + pzVersion + ".zip\".");
        },
        () -> System.out.println("Skipping backup of folders."));
  }

  private void backupClassFolders(File dirBackup) {
    try {

      if (dirBackup.exists()) {
        IOUtil.deleteDirectory(dirBackup);
      }

      if (!dirBackup.mkdirs()) {
        throw new IOException("Failed to create directory: " + dirBackup.getPath());
      }

      String backupPath = dirBackup.getPath() + File.separator;
      String pzPath = dirPZ.getPath() + File.separator;

      copyDirectory(pzPath + "astar", backupPath + "astar");
      copyDirectory(pzPath + "com", backupPath + "com");
      copyDirectory(pzPath + "de", backupPath + "de");
      copyDirectory(pzPath + "fmod", backupPath + "fmod");
      copyDirectory(pzPath + "javax", backupPath + "javax");
      copyDirectory(pzPath + "N3D", backupPath + "N3D");
      copyDirectory(pzPath + "org", backupPath + "org");
      copyDirectory(pzPath + "se", backupPath + "se");
      copyDirectory(pzPath + "zombie", backupPath + "zombie");
      copyFile(pzPath + "ProjectZomboid32.bat", backupPath + "ProjectZomboid32.bat");
      copyFile(pzPath + "ProjectZomboid64.bat", backupPath + "ProjectZomboid64.bat");
      copyFile(
          pzPath + "ProjectZomboid64ShowConsole.bat",
          backupPath + "ProjectZomboid64ShowConsole.bat");
      copyFile(
          pzPath + "ProjectZomboidOpenGLDebug32.bat",
          backupPath + "ProjectZomboidOpenGLDebug32.bat");
      copyFile(
          pzPath + "ProjectZomboidOpenGLDebug64.bat",
          backupPath + "ProjectZomboidOpenGLDebug64.bat");
      copyFile(pzPath + "ProjectZomboidServer.bat", backupPath + "ProjectZomboidServer.bat");
      copyFile(pzPath + "ProjectZomboid32.json", backupPath + "ProjectZomboid32.json");
      copyFile(pzPath + "ProjectZomboid64.json", backupPath + "ProjectZomboid64.json");

      ZipUtil.zipFolder(dirBackup.toPath(), new File(dirPZ, "backup_" + pzVersion + ".zip"));
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

  public File requestProjectZomboidDirectory() {
    System.out.println("Please enter the PZ directory: (Leave blank to exit installer)");
    while (true) {
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
