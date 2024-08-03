package com.asledgehammer.pythoid.installer.client;

import com.asledgehammer.util.ZipUtil;

import java.io.*;

public class TestZip {

  public static void main(String[] yargs) throws Exception {
    String fileName = "Pythoid-Patch41.78.16";
    File cacheDir =
        new File("D:\\SteamLibrary\\steamapps\\common\\ProjectZomboid\\.pythoid-installer-cache\\");
    File file = new File(cacheDir, fileName + ".zip");
    File outDir = new File(cacheDir, fileName);
    ZipUtil.extractZipFile(file, outDir);
  }
}
