package com.asledgehammer.pz.patchinstaller.server.task;

import static com.asledgehammer.util.ConsoleUtils.println;
import static com.asledgehammer.util.ConsoleUtils.yesOrNo;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.pz.patchinstaller.server.PZServerBackup;

import java.io.File;
import java.util.Scanner;

public class ServerBackupTask extends Task {

  private final Scanner scanner;
  private final boolean silent;

  public ServerBackupTask(Scanner scanner, boolean silent) {
    super("ProjectZomboid-Server-Backup", false);
    this.scanner = scanner;
    this.silent = silent;
  }

  @Override
  public void run() {
    File dirPZ = PatchInstaller.Args.PZ_DIRECTORY;
    File dirCache = PatchInstaller.Args.CACHE_DIRECTORY;
    PZVersion versionPZ = PatchInstaller.Args.PZ_VERSION;
    File dirBackup = new File(dirCache, "backup_" + versionPZ.toString());
    PZServerBackup backup = new PZServerBackup(versionPZ, dirPZ, dirBackup);
    if (!backup.exists()) {
      if (!silent) {
        yesOrNo(scanner, "Backup Project Zomboid folders?", () -> backUp(backup));
      } else {
        backUp(backup);
      }
    } else {
      println("Backup already exists: " + backup.getZipFile().getPath());
    }
  }

  private void backUp(PZServerBackup backup) {
    println("Backing up folders..");
    try {
      backup.backUp();
      println("Completed backup: " + backup.getZipFile().getPath());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
