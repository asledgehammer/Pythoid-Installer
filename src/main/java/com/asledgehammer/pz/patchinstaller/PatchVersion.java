package com.asledgehammer.pz.patchinstaller;

import com.asledgehammer.pz.PZVersion;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Scanner;

public class PatchVersion {

  private final PZVersion pzVersion;
  private final int patchVersion;
  private final Date timestamp;

  /**
   * @param raw The un-parsed line containing the PZ_VERSION (String) ; PATCH_VERSION (UInt) ;
   *     TIMESTAMP (Long) E.G: 41.78.16;1;1722973881
   */
  public PatchVersion(String raw) {
    String[] split = raw.split(";");
    this.pzVersion = new PZVersion(split[0]);
    this.patchVersion = Integer.parseInt(split[1]);
    this.timestamp = new Date(Long.parseLong(split[2]) * 1000L);
  }


  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PatchVersion pv2)
        && pv2.pzVersion.equals(this.pzVersion)
        && pv2.patchVersion == this.patchVersion
        && pv2.timestamp.equals(this.timestamp);
  }

  public boolean isNewer(PatchVersion other) {
    if (other.pzVersion.isNewer(this.pzVersion)) return true;
    else if (other.pzVersion.isOlder(this.pzVersion)) return false;
    return other.patchVersion > this.patchVersion;
  }

  public boolean isOlder(PatchVersion other) {
    if (other.pzVersion.isOlder(this.pzVersion)) return true;
    else if (other.pzVersion.isNewer(this.pzVersion)) return false;
    return other.patchVersion < this.patchVersion;
  }

  @Override
  public String toString() {
    return pzVersion + " (Build #" + patchVersion + " @ " + timestamp + ")";
  }

  public Date getTimestamp() {
    return this.timestamp;
  }

  public int getPatchVersion() {
    return this.patchVersion;
  }

  public PZVersion getPzVersion() {
    return this.pzVersion;
  }

  public static PatchVersion fromFile(File file) throws IOException {
    FileReader fr = new FileReader(file);
    BufferedReader br = new BufferedReader(fr);
    String out = br.readLine();
    br.close();
    return new PatchVersion(out);
  }

  public static PatchVersion fromURL(String url) throws IOException {
    InputStream is = URI.create(url).toURL().openStream();
    Scanner scanner = new Scanner(is, StandardCharsets.UTF_8).useDelimiter("\\A");
    String out = scanner.next();
    scanner.close();
    return new PatchVersion(out);
  }
}
