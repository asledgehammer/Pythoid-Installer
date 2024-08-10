package com.asledgehammer.pz.patchinstaller;

public abstract class Task implements Runnable {

  private final boolean continueIfFailure;
  private final String name;

  public Task(String name, boolean continueIfFailure) {
    this.name = name;
    this.continueIfFailure = continueIfFailure;
  }

  public String getName() {
    return this.name;
  }

  public boolean continueIfFailure() {
    return this.continueIfFailure;
  }
}
