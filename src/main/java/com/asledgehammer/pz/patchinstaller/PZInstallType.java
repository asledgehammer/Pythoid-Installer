package com.asledgehammer.pz.patchinstaller;

public enum PZInstallType {
  CLIENT(108600),
  SERVER(380870);

  private final int steamId;

  PZInstallType(int steamId) {
    this.steamId = steamId;
  }

  public int getSteamId() {
    return this.steamId;
  }
}
