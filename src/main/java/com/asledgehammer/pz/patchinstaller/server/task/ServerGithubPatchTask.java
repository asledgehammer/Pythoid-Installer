package com.asledgehammer.pz.patchinstaller.server.task;

import static com.asledgehammer.util.ConsoleUtils.println;
import static com.asledgehammer.util.IOUtil.copyDirectory;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.PatchVersion;
import com.asledgehammer.pz.patchinstaller.Task;
import com.asledgehammer.util.IOUtil;
import com.asledgehammer.util.ZipUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ServerGithubPatchTask extends Task {

  private final String repository;
  private final String author;
  private final String fileVersionName;

  public ServerGithubPatchTask(String author, String repository, String fileVersionName) {
    super("Install Patch " + author + "/" + repository + "-Server", false);
    this.author = author;
    this.repository = repository;
    this.fileVersionName = fileVersionName;
  }

  @Override
  public void run() {
    try {
      PatchVersion remotePatchVersion = getRemotePatchVersion();
      PatchVersion localPatchVersion = getLocalPatchVersion();
      println(
          "Current "
              + this.repository
              + "-Server"
              + " version: "
              + (localPatchVersion == null ? "(Not installed)" : localPatchVersion));
      println("Latest " + this.repository + "-Server version: " + remotePatchVersion);

      if (PatchInstaller.Args.FORCE_PATCH
          || localPatchVersion == null || localPatchVersion.isNewer(remotePatchVersion)) {

        if (!PatchInstaller.Args.FORCE_PATCH) {
          println("A new version of " + this.repository + " is available!");
        }

        try {
          applyPatch();
        } catch (Exception e) {
          throw new RuntimeException("Failed to apply patch.", e);
        }

      } else {
        println(this.repository + " is up-to-date and doesn't need to be downloaded & applied.");
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void applyPatch() throws IOException {

    File dirPZ = PatchInstaller.Args.PZ_DIRECTORY;
    File dirCache = PatchInstaller.Args.CACHE_DIRECTORY;
    PZVersion versionPZ = PatchInstaller.Args.PZ_VERSION;

    String githubZipURL =
        "https://github.com/"
            + this.author
            + '/'
            + this.repository
            + "-Server"
            + "/archive/refs/heads/"
            + versionPZ.toString()
            + ".zip";

    File patchFile = new File(dirCache, this.repository + versionPZ + ".zip");
    IOUtil.downloadUsingNIO(githubZipURL, patchFile.getPath());
    File dirPatch = new File(dirCache, this.repository + versionPZ);
    ZipUtil.extractZipFile(patchFile, dirPatch);
    println("Applying " + this.author + "/" + this.repository + "-Server/" + versionPZ + "..");
    copyDirectory(
        dirPatch.getPath() + File.separator + repository + "-Server-" + versionPZ, dirPZ.getPath());
    println("Applied " + this.author + "/" + this.repository + "-Server/" + versionPZ + ".");
  }

  private PatchVersion getRemotePatchVersion() throws IOException {
    String url =
        "https://raw.githubusercontent.com/"
            + author
            + '/'
            + repository
            + "-Server/"
            + PatchInstaller.Args.PZ_VERSION.toString()
            + '/'
            + fileVersionName
            + ".txt";
    try {
      return PatchVersion.fromURL(url);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(
          "Repository (Or version) does not exist: "
              + author
              + '/'
              + repository
              + "-Server/"
              + PatchInstaller.Args.PZ_VERSION,
          e);
    }
  }

  private PatchVersion getLocalPatchVersion() throws IOException {
    File file = new File(PatchInstaller.Args.PZ_DIRECTORY, fileVersionName + ".txt");
    if (!file.exists()) return null;
    return PatchVersion.fromFile(file);
  }
}
