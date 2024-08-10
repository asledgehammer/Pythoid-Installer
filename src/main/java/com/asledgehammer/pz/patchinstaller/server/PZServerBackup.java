package com.asledgehammer.pz.patchinstaller.server;

import static com.asledgehammer.util.IOUtil.copyDirectory;
import static com.asledgehammer.util.IOUtil.copyFile;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.util.IOUtil;
import com.asledgehammer.util.ZipUtil;
import java.io.File;
import java.io.IOException;

public class PZServerBackup {

  private final File dirPZ;
  private final File dirBackup;
  private final File fileZip;

  public PZServerBackup(PZVersion versionPZ, File dirPZ, File dirBackup) {
    this.dirPZ = dirPZ;
    this.dirBackup = dirBackup;
    this.fileZip = new File(dirPZ, "backup_" + versionPZ + ".zip");
  }

  public boolean exists() {
    return this.fileZip.exists();
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

    copyDirectory(pzPath + "java", backupPath + "java");
    copyFile(pzPath + "ProjectZomboid32.json", backupPath + "ProjectZomboid32.json");
    copyFile(pzPath + "ProjectZomboid64.json", backupPath + "ProjectZomboid64.json");
    copyFile(pzPath + "StartServer32.bat", backupPath + "StartServer32.bat");
    copyFile(pzPath + "StartServer64.bat", backupPath + "StartServer64.bat");
    copyFile(pzPath + "StartServer64_nosteam.bat", backupPath + "StartServer64_nosteam.bat");

    ZipUtil.zipFolder(dirBackup.toPath(), fileZip);
  }

  public File getZipFile() {
    return this.fileZip;
  }
}
