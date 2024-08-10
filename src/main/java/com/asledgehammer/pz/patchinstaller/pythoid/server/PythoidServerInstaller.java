package com.asledgehammer.pz.patchinstaller.pythoid.server;

import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.pythoid.task.JythonInstallerTask;
import com.asledgehammer.pz.patchinstaller.server.task.ServerGithubPatchTask;

import static com.asledgehammer.util.ConsoleUtils.println;

public class PythoidServerInstaller extends PatchInstaller {

  public PythoidServerInstaller() {
    super("Project Zomboid Pythoid-Server-Patch Installer", "asledgehammer", "1.0.0");

    if (Args.HELP) {
      printTitle();
      println();
      Args.help();
      System.exit(0);
    }

    addTask(new JythonInstallerTask("2.7.3"));
    addTask(new ServerGithubPatchTask("asledgehammer", "Pythoid-Patch", "pythoid_version"));
  }
}
