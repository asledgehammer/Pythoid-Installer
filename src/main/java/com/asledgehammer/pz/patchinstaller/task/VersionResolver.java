package com.asledgehammer.pz.patchinstaller.task;

import com.asledgehammer.pz.PZVersion;

@FunctionalInterface
public interface VersionResolver {
  void resolve(PZVersion version);
}
