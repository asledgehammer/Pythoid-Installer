package com.asledgehammer.pz;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;

public class PZVersion {

  private final int major;
  private final int minor;
  private final int patch;

  public PZVersion(String raw) {
    String[] split = raw.split("\\.");
    this.major = Integer.parseInt(split[0]);
    if (split.length > 1) {
      this.minor = Integer.parseInt(split[1]);
    } else {
      this.minor = 0;
    }
    if (split.length > 2) {
      this.patch = Integer.parseInt(split[2]);
    } else {
      this.patch = 0;
    }
  }

  @Override
  public String toString() {
    return this.major + "." + this.minor + "." + this.patch;
  }

  public boolean isNewer(PZVersion other) {
    if (other.major < this.major) return true;
    else if (other.major > this.major) return false;
    if (other.minor < this.minor) return true;
    else if (other.minor > this.minor) return true;
    if (other.patch < this.patch) return true;
    else if (other.patch > this.patch) return false;
    return false;
  }

  public boolean isOlder(PZVersion other) {
    if (other.major > this.major) return true;
    else if (other.major < this.major) return false;
    if (other.minor > this.minor) return true;
    else if (other.minor < this.minor) return true;
    if (other.patch > this.patch) return true;
    else if (other.patch < this.patch) return false;
    return false;
  }

  public boolean equals(Object other) {
    return other instanceof PZVersion pz2
        && pz2.major == this.major
        && pz2.minor == this.minor
        && pz2.patch == this.patch;
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getPatch() {
    return patch;
  }

  public static PZVersion fromJava(File dirPZ) throws Exception {
    System.out.println(dirPZ.getPath());
    URL url = dirPZ.toURI().toURL();
    ClassLoader cl = new URLClassLoader(new URL[] {url});
    Class<?> cls = cl.loadClass("zombie.core.Core");
    Field field = cls.getDeclaredField("gameVersion");
    field.setAccessible(true);
    Field field2 = cls.getDeclaredField("buildVersion");
    field2.setAccessible(true);
    return new PZVersion(field.get(null) + "." + field2.get(null));
  }
}
