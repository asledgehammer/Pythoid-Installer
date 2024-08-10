package com.asledgehammer.pz.patchinstaller.task;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.PZInstallType;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;

import java.io.File;
import java.util.Scanner;

import static com.asledgehammer.util.ConsoleUtils.println;

public class ResolvePZVersionTask extends Task {

  private File directory;
  private final VersionResolver resolver;
  private final Scanner scanner;
  private PZVersion version;

  public ResolvePZVersionTask(Scanner scanner) {
    this(scanner, null, null);
  }

  public ResolvePZVersionTask(Scanner scanner, File directory) {
    this(scanner, directory, null);
  }

  public ResolvePZVersionTask(Scanner scanner, File directory, VersionResolver resolver) {
    super("Resolve Project Zomboid Version", false);
    this.scanner = scanner;
    this.directory = directory;
    this.resolver = resolver;
  }

  @Override
  public void run() {

    // Manually-inputted version.
    if (PatchInstaller.Args.PZ_VERSION != null) {
      this.version = PatchInstaller.Args.PZ_VERSION;
    } else {
      try {

        if (this.directory == null) {
          if (PatchInstaller.Args.PZ_INSTALL_TYPE == PZInstallType.SERVER) {
            this.directory = new File(PatchInstaller.Args.PZ_DIRECTORY, "java");
          } else {
            this.directory = PatchInstaller.Args.PZ_DIRECTORY;
          }
          System.out.println(this.directory.getPath());
        }

        this.version = PZVersion.fromJava(this.directory);
        PatchInstaller.Args.PZ_VERSION = this.version;
        println("Detected Project Zomboid version: " + this.version);
      } catch (Exception e) {
        if (PatchInstaller.Args.SILENT) {
          throw new RuntimeException(e);
        } else {
          e.printStackTrace(System.err);
          this.version = requestPZVersion();
        }
        println("Set Project Zomboid version: " + this.version);
      }
    }

    // (optional result lambda)
    if (this.resolver != null) {
      this.resolver.resolve(this.version);
    }
  }

  public PZVersion requestPZVersion() {
    System.out.println("Please enter the Project Zomboid version: (Leave blank to exit installer)");
    String line = scanner.nextLine();
    if (line.isEmpty()) {
      System.exit(0);
    }
    return new PZVersion(line);
  }

  public PZVersion getVersion() {
    return this.version;
  }
}
