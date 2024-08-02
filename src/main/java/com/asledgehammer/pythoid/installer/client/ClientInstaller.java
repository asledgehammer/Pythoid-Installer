package com.asledgehammer.pythoid.installer.client;

import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ClientInstaller {

  public static final String JYTHON_PATCH_ZIP_URL =
      "https://github.com/asledgehammer/Pythoid-Patch/archive/refs/heads/{VERSION}.zip";
  public static final String JYTHON_INSTALLER_URL =
      "https://repo1.maven.org/maven2/org/python/jython-installer/2.7.3/jython-installer-2.7.3.jar";
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
        dirPZ = resolveProjectZomboidDirectory();

        System.out.println("Steam installation discovered:");
        System.out.println("\t" + dirPZ.getPath());

      } catch (Exception e) {
        System.out.println("!!! Failed to discover Steam installation !!!");
      }

      if (dirPZ == null) {
        dirPZ = requestProjectZomboidDirectory();
      }

      System.out.print("Creating installer cache.. ");

      this.dirCache = new File(dirPZ, ".pythoid-installer-cache");
      if (!this.dirCache.exists() && !this.dirCache.mkdirs()) {
        throw new RuntimeException("Failed to create directory: " + this.dirCache.getPath());
      }

      System.out.println("Success.");

      dirJythonInstall = new File(dirPZ, "jython2.7.3");

      if (!isJythonInstalled()) {
        try {
          installJython();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        System.out.println("Jython 2.7.3 is already installed.");
      }

      requestBackupClassFolders();

      this.pzVersion = getPZVersion(dirPZ);
      if (pzVersion == null) {
        line("Couldn't detect PZ version. Using 'latest'..");
        pzVersion = "latest";
      }

      this.patchURL = JYTHON_PATCH_ZIP_URL.replaceAll("\\{VERSION}", pzVersion);
      this.patchFile = new File(dirCache, "Pythoid-Patch" + this.pzVersion + ".zip");

      if (!hasPatchFiles()) {
        downloadUsingNIO(this.patchURL, this.patchFile.getPath());
      }
    } catch (Exception e) {
      System.err.println("The installer has encountered an error and cannot recover.");
      e.printStackTrace(System.err);
    }

    try {
      if (!hasArg(yargs, "--keep-cache")) {
        System.out.print("Deleting installer cache.. ");
        this.dirCache.delete();
        System.out.println("Success.");
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

  private void requestBackupClassFolders() {
    System.out.println("Backup folders? [Y/N]");
    boolean decision = false;
    while (!decision) {
      String line = scanner.nextLine();
      if (line.toLowerCase().startsWith("y")) {
        backupClassFolders();
        return;
      } else if (line.toLowerCase().startsWith("n")) {
        System.out.println("Skipping backup of folders.");
        return;
      }
      System.out.println("No decision was entered. Try again.");
    }
  }

  private void backupClassFolders() {
    System.out.print("Backing up folders..");

    try {
      zipFolder(new File(dirPZ, "zombie").toPath(), new File(dirPZ, "zombie_backup.zip"));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    System.out.println("Success.");
  }

  private static void zipFolder(Path sourceFolderPath, File zipFile) throws Exception {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
    Files.walkFileTree(
        sourceFolderPath,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
            Files.copy(file, zos);
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
          }
        });
    zos.close();
  }

  private static boolean hasArg(String[] yargs, String arg) {
    for (String yarg : yargs) {
      if (yarg.contains(arg)) return true;
    }
    return false;
  }

  private boolean isJythonInstalled() {
    return dirJythonInstall.exists();
  }

  private void installJython() throws IOException {

    String pathInstallerJar =
        dirCache.getAbsolutePath() + File.separator + "jython-installer-2.7.3.jar";

    File installerJar = new File(pathInstallerJar);
    if (!installerJar.exists()) {
      System.out.print("Downloading jython-installer-2.7.3.jar..");
      downloadUsingNIO(JYTHON_INSTALLER_URL, pathInstallerJar);
      System.out.println("Success.");
    } else {
      System.out.println("jython-installer-2.7.3.jar already exists.");
    }

    try {
      if (!dirJythonInstall.exists() && !dirJythonInstall.mkdirs()) {
        throw new RuntimeException("Failed to make directory: " + dirJythonInstall.getPath());
      }

      System.out.println("Installing Jython 2.7.3..");
      executeCommandVerbose("java -jar " + pathInstallerJar + " -s -d " + dirJythonInstall);
      System.out.println("Installed Jython 2.7.3.");

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
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

      if (!isValidPZDirectory(dirLine)) {
        System.out.println("Directory doesn't contain PZ files. Please try again.");
        continue;
      }

      System.out.println("Directory is valid.");

      return dirLine;
    }

    return null;
  }

  private static String getPZVersion(File dirPZ) {
    try {
      URL url = dirPZ.toURI().toURL();
      URL[] urls = new URL[] {url};
      ClassLoader cl = new URLClassLoader(urls);
      Class<?> cls = cl.loadClass("zombie.core.Core");
      Field field = cls.getDeclaredField("gameVersion");
      field.setAccessible(true);
      Field field2 = cls.getDeclaredField("buildVersion");
      field2.setAccessible(true);
      return field.get(null) + "." + field2.get(null);
    } catch (MalformedURLException
        | ClassNotFoundException
        | NoSuchFieldException
        | IllegalAccessException e) {
      e.printStackTrace(System.err);
    }
    return null;
  }

  public static boolean isValidPZDirectory(File dir) {
    File fExecutable64 = new File(dir, "ProjectZomboid64.exe");
    return fExecutable64.exists() && new File(dir, "zombie").exists();
  }

  public static File resolveProjectZomboidDirectory() throws Exception {
    List<String> lines =
        executeCommandSync(
            "REG QUERY HKLM\\Software\\WOW6432Node\\Valve\\Steam /f \"InstallPath\"", true);
    String steamDirStr = lines.get(1).trim().split("\\s{4}")[2];

    File dirSteam = new File(steamDirStr);
    if (!dirSteam.exists()) {
      throw new FileNotFoundException("Directory doesn't exist: " + dirSteam.getPath());
    }

    File dirSteamapps = new File(dirSteam, "steamapps");
    if (!dirSteamapps.exists()) {
      throw new FileNotFoundException("Directory doesn't exist: " + dirSteamapps.getPath());
    }

    File fileLibraries = new File(dirSteamapps, "libraryfolders.vdf");
    if (!fileLibraries.exists()) {
      throw new FileNotFoundException("File doesn't exist: " + fileLibraries.getPath());
    }

    FileReader fileReader = new FileReader(fileLibraries);
    BufferedReader br = new BufferedReader(fileReader);

    String vdfRaw = "";
    String s;
    while ((s = br.readLine()) != null) {
      vdfRaw += s + "\n";
    }

    br.close();

    VDFNode node = new VDFParser().parse(vdfRaw);

    VDFNode nodeLibraries = node.getSubNode("libraryfolders");

    VDFNode libFound = null;

    for (int index = 0; index < nodeLibraries.size(); index++) {
      VDFNode lib = nodeLibraries.getSubNode("" + index);
      VDFNode libApps = lib.getSubNode("apps");
      if (libApps.containsKey("108600")) {
        libFound = lib;
        break;
      }
    }

    if (libFound == null) {
      throw new RuntimeException("Steam library not found for Project Zomboid.");
    }

    String dirPZStr =
        libFound.getString("path")
            + File.separator
            + "steamapps"
            + File.separator
            + "common"
            + File.separator
            + "ProjectZomboid";

    File dirPZ = new File(dirPZStr);

    if (!dirPZ.exists()) {
      throw new FileNotFoundException("Directory doesn't exit: " + dirPZStr);
    }

    return dirPZ;
  }

  public static void executeCommandVerbose(String command)
      throws IOException, InterruptedException {
    Runtime rt = Runtime.getRuntime();
    Process process = rt.exec(command);

    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    // Read the output from the command
    String s;
    while ((s = stdInput.readLine()) != null) {
      System.out.println(s);
    }

    // Read any errors from the attempted command
    while ((s = stdError.readLine()) != null) {
      System.out.println(s);
    }

    process.waitFor();
  }

  public static List<String> executeCommandSync(String command, boolean silent)
      throws IOException, InterruptedException {

    Process process = Runtime.getRuntime().exec(command);
    process.waitFor();

    if (process.exitValue() == 0) {

      Scanner sc = new Scanner(process.getInputStream());

      List<String> lines = new ArrayList<>();

      do {
        String s = sc.nextLine();
        if (!silent) {
          System.out.println(s);
        }
        if (s.isEmpty()) continue;
        lines.add(s);
      } while (sc.hasNext());
      sc.close();

      return lines;
    } else {
      System.err.println("Query failure..\n" + command);
    }

    return null;
  }

  private static void downloadUsingStream(String urlStr, String file) throws IOException {
    URL url = new URL(urlStr);
    BufferedInputStream bis = new BufferedInputStream(url.openStream());
    FileOutputStream fis = new FileOutputStream(file);
    byte[] buffer = new byte[1024];
    int count = 0;
    while ((count = bis.read(buffer, 0, 1024)) != -1) {
      fis.write(buffer, 0, count);
    }
    fis.close();
    bis.close();
  }

  private static void downloadUsingNIO(String urlStr, String file) throws IOException {
    URL url = new URL(urlStr);
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(file);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    fos.close();
    rbc.close();
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
