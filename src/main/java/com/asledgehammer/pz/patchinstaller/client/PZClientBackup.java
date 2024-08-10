package com.asledgehammer.pz.patchinstaller.client;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.util.IOUtil;
import com.asledgehammer.util.ZipUtil;

import java.io.File;
import java.io.IOException;

import static com.asledgehammer.util.IOUtil.copyDirectory;
import static com.asledgehammer.util.IOUtil.copyFile;

public class PZClientBackup {

  private final File dirPZ;
  private final File dirBackup;
  private final File fileZip;

  public PZClientBackup(PZVersion versionPZ, File dirPZ, File dirBackup) {
    this.dirPZ = dirPZ;
    this.dirBackup = dirBackup;
    this.fileZip = new File(dirPZ, "backup_" + versionPZ + ".zip");
  }

  public boolean exists() {
    return this.dirBackup.exists();
  }

  public void backUp() throws Exception {

    if (dirBackup.exists()) {
      IOUtil.deleteDirectory(dirBackup);
    }

    if (!dirBackup.mkdirs()) {
      throw new IOException("Failed to create directory: " + dirBackup.getPath());
    }

    String backupPath = dirBackup.getPath() + File.separator;
    String pzPath = dirPZ.getPath() + File.separator;

    copyDirectory(pzPath + "astar", backupPath + "astar");
    copyDirectory(pzPath + "com", backupPath + "com");
    copyDirectory(pzPath + "de", backupPath + "de");
    copyDirectory(pzPath + "fmod", backupPath + "fmod");
    copyDirectory(pzPath + "javax", backupPath + "javax");
    copyDirectory(pzPath + "N3D", backupPath + "N3D");
    copyDirectory(pzPath + "org", backupPath + "org");
    copyDirectory(pzPath + "se", backupPath + "se");
    copyDirectory(pzPath + "zombie", backupPath + "zombie");
    copyFile(pzPath + "ProjectZomboid32.bat", backupPath + "ProjectZomboid32.bat");
    copyFile(pzPath + "ProjectZomboid64.bat", backupPath + "ProjectZomboid64.bat");
    copyFile(
        pzPath + "ProjectZomboid64ShowConsole.bat", backupPath + "ProjectZomboid64ShowConsole.bat");
    copyFile(
        pzPath + "ProjectZomboidOpenGLDebug32.bat", backupPath + "ProjectZomboidOpenGLDebug32.bat");
    copyFile(
        pzPath + "ProjectZomboidOpenGLDebug64.bat", backupPath + "ProjectZomboidOpenGLDebug64.bat");
    copyFile(pzPath + "ProjectZomboidServer.bat", backupPath + "ProjectZomboidServer.bat");
    copyFile(pzPath + "ProjectZomboid32.json", backupPath + "ProjectZomboid32.json");
    copyFile(pzPath + "ProjectZomboid64.json", backupPath + "ProjectZomboid64.json");

    ZipUtil.zipFolder(dirBackup.toPath(), fileZip);
  }

  public File getZipFile() {
    return this.fileZip;
  }
}
