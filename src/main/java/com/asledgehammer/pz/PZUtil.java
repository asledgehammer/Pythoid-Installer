package com.asledgehammer.pz;

import com.asledgehammer.pz.patchinstaller.PZInstallType;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.util.IOUtil;
import net.platinumdigitalgroup.jvdf.VDFNode;
import net.platinumdigitalgroup.jvdf.VDFParser;

import java.io.*;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class PZUtil {

  public static final int PZ_DEDICATED_SERVER_STEAM_ID = 380870;
  public static final int PZ_STEAM_ID = 108600;

  public static final String JYTHON_INSTALLER_URL =
      "https://repo1.maven.org/maven2/org/python/jython-installer/2.7.3/jython-installer-2.7.3.jar";

  public static void installJython(File dirCache, File dirInstall, String version) throws IOException {

    String pathInstallerJar =
        dirCache.getAbsolutePath() + File.separator + "jython-installer-2.7.3.jar";

    File installerJar = new File(pathInstallerJar);
    if (!installerJar.exists()) {
      System.out.print("Downloading jython-installer-2.7.3.jar..");
      IOUtil.downloadUsingNIO(JYTHON_INSTALLER_URL, pathInstallerJar);
      System.out.println("Success.");
    } else {
      System.out.println("jython-installer-2.7.3.jar already exists.");
    }

    try {
      if (!dirInstall.exists() && !dirInstall.mkdirs()) {
        throw new RuntimeException("Failed to make directory: " + dirInstall.getPath());
      }

      System.out.println("Installing Jython 2.7.3..");
      IOUtil.executeCommandVerbose(
          "java -jar " + pathInstallerJar + " -s -d " + dirInstall.getPath());
      System.out.println("Installed Jython 2.7.3.");

    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static boolean isValidPZDirectory(File dir) {
    File fExecutable64 = new File(dir, "ProjectZomboid64.exe");
    return fExecutable64.exists() && new File(dir, "zombie").exists();
  }

  public static File resolveProjectZomboidDirectory(int steamID) throws Exception {
    List<String> lines =
        IOUtil.executeCommandSync(
            "REG QUERY HKLM\\Software\\WOW6432Node\\Valve\\Steam /f \"InstallPath\"", true);
    String steamDirStr = lines.get(1).trim().split("\\s{4}")[2];

    File dirSteam = new File(steamDirStr);
    if (!dirSteam.exists()) {
      throw new FileNotFoundException("Directory doesn't exist: " + dirSteam.getPath());
    }

    File dirSteamapps = new File(dirSteam, "steamapps");
    if (!dirSteamapps.exists()) {
      throw new FileNotFoundException("Directory doesn't exist: " + dirSteamapps.getPath());
    }

    File fileLibraries = new File(dirSteamapps, "libraryfolders.vdf");
    if (!fileLibraries.exists()) {
      throw new FileNotFoundException("File doesn't exist: " + fileLibraries.getPath());
    }

    FileReader fileReader = new FileReader(fileLibraries);
    BufferedReader br = new BufferedReader(fileReader);

    String vdfRaw = "";
    String s;
    while ((s = br.readLine()) != null) {
      vdfRaw += s + "\n";
    }

    br.close();

    VDFNode node = new VDFParser().parse(vdfRaw);

    VDFNode nodeLibraries = node.getSubNode("libraryfolders");

    VDFNode libFound = null;

    for (int index = 0; index < nodeLibraries.size(); index++) {
      VDFNode lib = nodeLibraries.getSubNode("" + index);
      VDFNode libApps = lib.getSubNode("apps");
      if (libApps.containsKey("" + steamID)) {
        libFound = lib;
        break;
      }
    }

    if (libFound == null) {
      throw new RuntimeException("Steam library not found for Project Zomboid.");
    }

    String dirPZStr = "";
    if(PatchInstaller.Args.PZ_INSTALL_TYPE == PZInstallType.CLIENT) {
      dirPZStr =
              libFound.getString("path")
                      + File.separator
                      + "steamapps"
                      + File.separator
                      + "common"
                      + File.separator
                      + "ProjectZomboid";
    } else {
      dirPZStr =
              libFound.getString("path")
                      + File.separator
                      + "steamapps"
                      + File.separator
                      + "common"
                      + File.separator
                      + "Project Zomboid Dedicated Server";
    }



    File dirPZ = new File(dirPZStr);

    if (!dirPZ.exists()) {
      throw new FileNotFoundException("Directory doesn't exit: " + dirPZStr);
    }

    return dirPZ;
  }
}
