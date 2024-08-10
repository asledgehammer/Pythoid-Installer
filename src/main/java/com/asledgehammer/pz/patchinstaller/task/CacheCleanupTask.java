package com.asledgehammer.pz.patchinstaller.task;

import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.util.IOUtil;

import java.io.IOException;

import static com.asledgehammer.util.ConsoleUtils.print;

public class CacheCleanupTask extends Task {

  public CacheCleanupTask() {
    super("Cache Cleanup", false);
  }

  @Override
  public void run() {
    if (!PatchInstaller.Args.KEEP_CACHE) {
      try {
        print("Cleaning up Installer cache.. ");
        IOUtil.deleteDirectory(PatchInstaller.Args.CACHE_DIRECTORY);
        print("Success.");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
