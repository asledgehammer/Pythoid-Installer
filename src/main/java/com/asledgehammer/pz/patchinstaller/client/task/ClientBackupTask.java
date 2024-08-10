package com.asledgehammer.pz.patchinstaller.client.task;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.pz.patchinstaller.client.PZClientBackup;

import java.io.File;
import java.util.Scanner;

import static com.asledgehammer.util.ConsoleUtils.println;
import static com.asledgehammer.util.ConsoleUtils.yesOrNo;

public class ClientBackupTask extends Task {

  private final Scanner scanner;
  private final boolean silent;

  public ClientBackupTask(Scanner scanner, boolean silent) {
    super("ProjectZomboid-Client-Backup", false);
    this.scanner = scanner;
    this.silent = silent;
  }

  @Override
  public void run() {
    File dirPZ = PatchInstaller.Args.PZ_DIRECTORY;
    File dirCache = PatchInstaller.Args.CACHE_DIRECTORY;
    PZVersion versionPZ = PatchInstaller.Args.PZ_VERSION;
    File dirBackup = new File(dirCache, "backup_" + versionPZ.toString());
    PZClientBackup backup = new PZClientBackup(versionPZ, dirPZ, dirBackup);
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

  private void backUp(PZClientBackup backup) {
    println("Backing up folders..");
    try {
      backup.backUp();
      println("Completed backup: " + backup.getZipFile().getPath());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
