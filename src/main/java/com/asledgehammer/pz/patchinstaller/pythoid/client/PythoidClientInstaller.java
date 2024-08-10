package com.asledgehammer.pz.patchinstaller.pythoid.client;

import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.client.task.ClientGithubPatchTask;
import com.asledgehammer.pz.patchinstaller.pythoid.task.JythonInstallerTask;

import static com.asledgehammer.util.ConsoleUtils.println;

public class PythoidClientInstaller extends PatchInstaller {

  public PythoidClientInstaller() {
    super("Project Zomboid Pythoid-Client-Patch Installer", "asledgehammer", "1.0.0");

    if (Args.HELP) {
      printTitle();
      println();
      Args.help();
      System.exit(0);
    }

    addTask(new JythonInstallerTask("2.7.3"));
    addTask(new ClientGithubPatchTask("asledgehammer", "Pythoid-Patch", "pythoid_version"));
  }
}
