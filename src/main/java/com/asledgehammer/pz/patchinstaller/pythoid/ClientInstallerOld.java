package com.asledgehammer.pz.patchinstaller.pythoid;

import com.asledgehammer.pz.patchinstaller.PZInstallType;
import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.PatchVersion;
import com.asledgehammer.pz.patchinstaller.client.PZClientBackup;
import com.asledgehammer.util.IOUtil;
import com.asledgehammer.pz.PZUtil;
import com.asledgehammer.util.ZipUtil;

import java.io.*;
import java.util.Scanner;

import static com.asledgehammer.util.IOUtil.*;
import static com.asledgehammer.util.ConsoleUtils.*;

public class ClientInstallerOld {

  public static final String INSTALLER_NAME = "Project Zomboid Client-Patch Installer";
  public static final String INSTALLER_VERSION = "1.0.0";

  public final String githubAccount;
  public final String githubRepository;
  public final String patchVersionFile;
  private final Scanner scanner;

  private PZVersion pzVersion;
  private File dirCache;
  private File dirPZ;

  public ClientInstallerOld(
      String githubAccount, String githubRepository, String patchVersionFile, String[] yargs) {

    this.githubAccount = githubAccount;
    this.githubRepository = githubRepository;
    this.patchVersionFile = patchVersionFile;

    printTitle();
    println();

    this.scanner = new Scanner(System.in);
    this.dirPZ = null;

    try {
      resolvePZDirectory();
      resolvePZVersion();
      createCache();
      processJythonInstall();
      processBackup();
      processPatch();
    } catch (Exception e) {
      errln("The installer has encountered an error and cannot recover.");
      e.printStackTrace(System.err);
    }

    if (!hasArg(yargs, "--keep-cache")) {
      cleanupCache();
    }

    scanner.close();
  }

  private void processPatch() throws IOException {

    PatchVersion remotePatchVersion = getRemotePatchVersion();
    PatchVersion localPatchVersion = getLocalPatchVersion();

    println(
        "Current "
            + this.githubRepository
            + " version: "
            + (localPatchVersion == null ? "(Not installed)" : localPatchVersion));
    println("Latest " + this.githubRepository + " version: " + remotePatchVersion);

    if (localPatchVersion == null || remotePatchVersion.isNewer(localPatchVersion)) {
      println("A new version of " + this.githubRepository + " is available!");
      yesOrNo(
          scanner,
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
      println(
          this.githubRepository + " is up-to-date and doesn't need to be downloaded & applied.");
    }
  }

  private void processBackup() {
    File dirBackup = new File(dirCache, "backup_" + pzVersion);
    PZClientBackup backup = new PZClientBackup(pzVersion, dirPZ, dirBackup);
    if (!backup.exists()) {
      yesOrNo(
          scanner,
          "Backup Project Zomboid folders?",
          () -> {
            println("Backing up folders..");
            try {

              backup.backUp();
              println("Completed backup: " + backup.getZipFile().getPath());
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          });
    } else {
      println("Backup already exists: " + backup.getZipFile().getPath());
    }
  }

  private void applyPatch() throws IOException {

    String githubZipURL =
        "https://github.com/"
            + this.githubAccount
            + '/'
            + this.githubRepository
            + "/archive/refs/heads/"
            + this.pzVersion
            + ".zip";

    File patchFile = new File(dirCache, githubRepository + this.pzVersion + ".zip");
    IOUtil.downloadUsingNIO(githubZipURL, patchFile.getPath());
    File dirPatch = new File(dirCache, githubRepository + pzVersion);
    ZipUtil.extractZipFile(patchFile, dirPatch);
    println("Applying " + githubRepository + pzVersion + "..");
    copyDirectory(
        dirPatch.getPath() + File.separator + githubRepository + "-" + pzVersion, dirPZ.getPath());
    println("Applied " + githubRepository + pzVersion + ".");
  }

  public File requestProjectZomboidDirectory() {
    System.out.println(
        "Please enter the Project Zomboid install directory: (Leave blank to exit installer)");
    while (true) {
      String line = scanner.nextLine();

      if (line.isEmpty()) {
        System.exit(0);
      }

      File dirLine = new File(line);
      if (!dirLine.exists()) {
        println("Directory does not exist. Please try again.");
        continue;
      }

      if (!PZUtil.isValidPZDirectory(dirLine)) {
        println("Directory doesn't contain Project Zomboid files. Please try again.");
        continue;
      }

      println("Directory is valid.");

      return dirLine;
    }
  }

  private PatchVersion getRemotePatchVersion() throws IOException {
    String url =
        "https://raw.githubusercontent.com/"
            + githubAccount
            + '/'
            + githubRepository
            + '/'
            + pzVersion
            + '/'
            + patchVersionFile;
    return PatchVersion.fromURL(url);
  }

  private PatchVersion getLocalPatchVersion() throws IOException {
    File file = new File(dirPZ, patchVersionFile);
    if (!file.exists()) return null;
    return PatchVersion.fromFile(file);
  }

  private void printTitle() {
    String firstString = INSTALLER_NAME + ' ' + INSTALLER_VERSION;
    String borderString = "=".repeat(firstString.length());
    println(borderString);
    println();
    println(firstString);
    println();
    printlnRight(borderString.length(), "By asledgehammer");
    println();
    println(borderString);
  }

  private void cleanupCache() {
    try {
      print("Deleting installer cache.. ");
      IOUtil.deleteDirectory(this.dirCache);
      println("Success.");
    } catch (Exception e) {
      errln("Failed to cleanup resources.");
      e.printStackTrace(System.err);
    }
  }

  private void createCache() throws RuntimeException {
    print("Creating installer cache.. ");
    this.dirCache = new File(this.dirPZ, ".pythoid-installer-cache");
    if (!this.dirCache.exists() && !this.dirCache.mkdirs()) {
      throw new RuntimeException("Failed to create directory: " + this.dirCache.getPath());
    }
    println("Success.");
  }

  private void resolvePZVersion() throws Exception {
    this.pzVersion = PZVersion.fromJava(this.dirPZ);
    println("Detected Project Zomboid: " + this.pzVersion);
  }

  private void resolvePZDirectory() {
    try {
      this.dirPZ = PZUtil.resolveProjectZomboidDirectory(PZInstallType.CLIENT.getSteamId());
      println("Steam installation discovered:");
      println('\t' + this.dirPZ.getPath());
    } catch (Exception e) {
      errln("Failed to discover Steam installation.");
    }
    if (this.dirPZ == null) {
      this.dirPZ = requestProjectZomboidDirectory();
    }
  }

  private void processJythonInstall() throws IOException {
    File dirJythonInstall = new File(this.dirPZ, "jython2.7.3");
    if (!dirJythonInstall.exists()) {
      PZUtil.installJython(this.dirCache, dirJythonInstall, "2.7.3");
    } else {
      println("Jython 2.7.3 is already installed.");
    }
  }

  public static void main(String[] yargs) {
    new ClientInstallerOld("asledgehammer", "Pythoid-Patch", "pythoid_version.txt", yargs);
  }
}
