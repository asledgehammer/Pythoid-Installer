# Pythoid

Pythoid is a Java mod for the game `Project Zomboid` that integrates the `Jython 2.7.3` 
interpreter to Java, enabling Python support for modding the game. Pythoid ships utilities that aids
in integration of Python-built classes with Lua's ability to both see and invoke.

# Pythoid-Installer

Pythoid-Installer is an attempt at automating the installation & patching process for Pythoid. 
Pythoid requires `Jython-2.7.3` to be installed properly in order to work. This and patching the 
game-code are technical steps that can easily confuse players who wish to install Pythoid to play
mods that require it. 

This installer attempts to locate Steam installations of both the game and its server companion.
With that in mind, not all installations are Steam so arguments are provided for manually entering
this information.

NOTE: A backup solution is provided for the ability to reverse changes to the game-code before being
patched. A future update to the installer will add the option to apply the backup, removing patches.

# CLI Arguments

```
--force
        Forces the installation of the patch regardless of the version installed.

--help
        Displays this message.

--keep-cache
        Don't remove the '.patch-installer-cache' folder after running tasks.

--no_backup
        Skips backup task.

--pz_dir <PATH>
        Manually sets the directory to the ProjectZomboid installation.

--pz_version <VERSION>
        Manually sets the version of ProjectZomboid. E.G: 41.78.16

--server
        Sets the installer as "Server Mode". This is for dedicated servers.

--silent
        Runs tasks without user-input.
```

# Note
It's possible that a GUI installer can be made in the future to help players who are unfamiliar with
command-line interfaces. Let me know if this is desired.

# Support

![](https://i.imgur.com/ZLnfTK4.png)

# Discord Server

<https://discord.gg/u3vWvcPX8f>