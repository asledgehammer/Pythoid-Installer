package com.asledgehammer.pz.patchinstaller.task;

import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.pz.PZUtil;

import java.io.File;
import java.util.Scanner;

import static com.asledgehammer.util.ConsoleUtils.errln;
import static com.asledgehammer.util.ConsoleUtils.println;

public class ResolvePZDirectoryTask extends Task {

  private final int steamID;
  private final Scanner scanner;
  private final DirectoryResolver resolver;
  private File directory;

  public ResolvePZDirectoryTask(Scanner scanner) {
    this(scanner, PatchInstaller.Args.PZ_INSTALL_TYPE.getSteamId(), null);
  }

  public ResolvePZDirectoryTask(Scanner scanner, int steamID) {
    this(scanner, steamID, null);
  }

  public ResolvePZDirectoryTask(Scanner scanner, int steamID, DirectoryResolver resolver) {
    super("Resolve Project Zomboid Directory", false);
    this.scanner = scanner;
    this.steamID = steamID;
    this.resolver = resolver;
  }

  @Override
  public void run() {

    if (PatchInstaller.Args.PZ_DIRECTORY == null) {
      try {
        this.directory = PZUtil.resolveProjectZomboidDirectory(steamID);
        println("Steam installation discovered:");
        println('\t' + this.directory.getPath());
      } catch (Exception e) {
        if (PatchInstaller.Args.SILENT) {
          throw new RuntimeException(e);
        } else {
          errln("Failed to discover Steam installation.");
          this.directory = requestProjectZomboidDirectory();
        }
      }

      PatchInstaller.Args.PZ_DIRECTORY = this.directory;
    }

    // (Check if --cache_directory is provided)
    if (PatchInstaller.Args.CACHE_DIRECTORY == null) {
      PatchInstaller.Args.CACHE_DIRECTORY = new File(this.directory, ".patch-installer-cache");
      if (!PatchInstaller.Args.CACHE_DIRECTORY.exists()
          && !PatchInstaller.Args.CACHE_DIRECTORY.mkdirs()) {
        throw new RuntimeException(
            "Failed to create directory: " + PatchInstaller.Args.CACHE_DIRECTORY.getPath());
      }
    }

    if (resolver != null) {
      resolver.result(this.directory);
    }
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
}
