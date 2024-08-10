package com.asledgehammer.pz.patchinstaller.pythoid.task;

import com.asledgehammer.pz.patchinstaller.PZInstallType;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.util.IOUtil;

import java.io.File;
import java.io.IOException;

import static com.asledgehammer.util.ConsoleUtils.println;

public class JythonInstallerTask extends Task {

  private File dirInstall;
  private File dirCache;
  private final String version;

  public JythonInstallerTask(String version) {
    this(version, null, null);
  }

  public JythonInstallerTask(String version, File dirCache, File dirInstall) {
    super("Install Jython " + version, true);
    this.version = version;
    this.dirCache = dirCache;
    this.dirInstall = dirInstall;
  }

  public void run() {

    if (this.dirCache == null) {
      this.dirCache = PatchInstaller.Args.CACHE_DIRECTORY;
    }

    if (this.dirInstall == null) {
      if (PatchInstaller.Args.PZ_INSTALL_TYPE == PZInstallType.SERVER) {
        this.dirInstall = new File(PatchInstaller.Args.PZ_DIRECTORY, "java/jython" + version);
      } else {
        this.dirInstall = new File(PatchInstaller.Args.PZ_DIRECTORY, "jython" + version);
      }
    }

    if (!dirInstall.exists()) {
      try {
        installJython();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      println("Jython " + this.version + " is already installed.");
    }
  }

  private void installJython() throws IOException {
    String pathInstallerJar =
        dirCache.getAbsolutePath() + File.separator + "jython-installer-" + version + ".jar";
    File installerJar = new File(pathInstallerJar);
    if (!installerJar.exists()) {
      System.out.print("Downloading jython-installer-" + version + ".jar..");
      String jythonInstallerUrl =
          "https://repo1.maven.org/maven2/org/python/jython-installer/"
              + version
              + "/jython-installer-"
              + version
              + ".jar";
      IOUtil.downloadUsingNIO(jythonInstallerUrl, pathInstallerJar);
      System.out.println("Success.");
    } else {
      System.out.println("jython-installer-" + version + ".jar already exists.");
    }
    try {
      if (!dirInstall.exists() && !dirInstall.mkdirs()) {
        throw new RuntimeException("Failed to make directory: " + dirInstall.getPath());
      }
      System.out.println("Installing Jython " + version + "..");
      String cmd = "java -jar \"" + pathInstallerJar + "\" -s -d \"" + dirInstall.getPath() + "\"";
      IOUtil.executeCommandVerbose(cmd);
      System.out.println("Installed Jython " + version + ".");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
