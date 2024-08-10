package com.asledgehammer.pz.patchinstaller.task;

import java.io.File;

@FunctionalInterface
public interface DirectoryResolver {
  void result(File dir);
}
