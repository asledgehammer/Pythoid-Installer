package com.asledgehammer.pz.patchinstaller.pythoid;

import com.asledgehammer.pz.patchinstaller.PZInstallType;
import com.asledgehammer.pz.patchinstaller.PatchInstaller;
import com.asledgehammer.pz.patchinstaller.pythoid.client.PythoidClientInstaller;
import com.asledgehammer.pz.patchinstaller.pythoid.server.PythoidServerInstaller;

import java.util.Arrays;
import java.util.List;

public class PythoidInstaller {

    public static void main(String[] yargs) {
        List<String> args = Arrays.stream(yargs).toList();
        PatchInstaller.Args.processArgs(args);

        if(PatchInstaller.Args.PZ_INSTALL_TYPE == PZInstallType.CLIENT) {
            new PythoidClientInstaller().run();
        } else {
            new PythoidServerInstaller().run();
        }
    }
}
