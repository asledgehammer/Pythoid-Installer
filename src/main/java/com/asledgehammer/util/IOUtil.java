package com.asledgehammer.util;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IOUtil {

  public static void downloadUsingStream(String urlStr, String file) throws IOException {
    URL url = new URL(urlStr);
    BufferedInputStream bis = new BufferedInputStream(url.openStream());
    FileOutputStream fis = new FileOutputStream(file);
    byte[] buffer = new byte[1024];
    int count = 0;
    while ((count = bis.read(buffer, 0, 1024)) != -1) {
      fis.write(buffer, 0, count);
    }
    fis.close();
    bis.close();
  }

  public static void downloadUsingNIO(String urlStr, String file) throws IOException {
    URL url = new URL(urlStr);
    ReadableByteChannel rbc = Channels.newChannel(url.openStream());
    FileOutputStream fos = new FileOutputStream(file);
    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    fos.close();
    rbc.close();
  }

  public static void executeCommandVerbose(String command)
      throws IOException, InterruptedException {
    Runtime rt = Runtime.getRuntime();
    Process process = rt.exec(command);

    BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

    // Read the output from the command
    String s;
    while ((s = stdInput.readLine()) != null) {
      System.out.println(s);
    }

    // Read any errors from the attempted command
    while ((s = stdError.readLine()) != null) {
      System.out.println(s);
    }

    process.waitFor();
  }

  public static List<String> executeCommandSync(String command, boolean silent)
      throws IOException, InterruptedException {

    Process process = Runtime.getRuntime().exec(command);
    process.waitFor();

    if (process.exitValue() == 0) {

      Scanner sc = new Scanner(process.getInputStream());

      List<String> lines = new ArrayList<>();

      do {
        String s = sc.nextLine();
        if (!silent) {
          System.out.println(s);
        }
        if (s.isEmpty()) continue;
        lines.add(s);
      } while (sc.hasNext());
      sc.close();

      return lines;
    } else {
      System.err.println("Query failure..\n" + command);
    }

    return null;
  }
}
