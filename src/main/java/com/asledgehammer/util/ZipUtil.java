package com.asledgehammer.util;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
  public static void extractZipFile(File file, File outDir) throws IOException {

    // create a buffer to improve copy performance later.
    byte[] buffer = new byte[2048];

    if (!outDir.exists() && !outDir.mkdirs()) {
      throw new IOException("Failed to create directory: " + outDir.getPath());
    }

    // open the zip file stream
    InputStream theFile = new FileInputStream(file);

    try (ZipInputStream stream = new ZipInputStream(theFile)) {

      // now iterate through each item in the stream. The get next
      // entry call will return a ZipEntry for each file in the
      // stream
      ZipEntry entry;
      while ((entry = stream.getNextEntry()) != null) {

        if (entry.isDirectory()) {
          File entryDir = new File(outDir, entry.getName());
          if (!entryDir.exists() && !entryDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + entryDir.getPath());
          }
          continue;
        }

        System.out.println("Extracting: " + entry.getName() + "..");

        // Once we get the entry from the stream, the stream is
        // positioned read to read the raw data, and we keep
        // reading until read returns 0 or less.
        String outpath = outDir + File.separator + entry.getName();
        try (FileOutputStream output = new FileOutputStream(outpath)) {
          int len;
          while ((len = stream.read(buffer)) > 0) {
            output.write(buffer, 0, len);
          }
        }
      }
    }
  }

  public static void zipFolder(Path sourceFolderPath, File zipFile) throws Exception {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
    Files.walkFileTree(
        sourceFolderPath,
        new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            zos.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
            Files.copy(file, zos);
            zos.closeEntry();
            return FileVisitResult.CONTINUE;
          }
        });
    zos.close();
  }
}
