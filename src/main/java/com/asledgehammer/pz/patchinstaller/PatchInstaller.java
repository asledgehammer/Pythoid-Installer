package com.asledgehammer.pz.patchinstaller;

import com.asledgehammer.pz.PZVersion;
import com.asledgehammer.pz.patchinstaller.client.task.ClientBackupTask;
import com.asledgehammer.pz.patchinstaller.server.task.ServerBackupTask;
import com.asledgehammer.pz.patchinstaller.task.CacheCleanupTask;
import com.asledgehammer.pz.patchinstaller.task.ResolvePZDirectoryTask;
import com.asledgehammer.pz.patchinstaller.task.ResolvePZVersionTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.asledgehammer.util.ConsoleUtils.*;

public class PatchInstaller {

  private final List<Task> tasks = new ArrayList<>();
  private final String name;
  private final String version;
  private final String author;
  private Task taskCleanup;
  protected Scanner scanner;

  public PatchInstaller(String name, String author, String version) {
    this(name, author, version, true);
  }

  public PatchInstaller(String name, String author, String version, boolean addDefaultTasks) {
    this.name = name;
    this.author = author;
    this.version = version;
    this.scanner = new Scanner(System.in);

    // (Default tasks)
    if (addDefaultTasks) {
      addTask(new ResolvePZDirectoryTask(scanner));
      addTask(new ResolvePZVersionTask(scanner));

      if (!Args.NO_BACKUP) {
        if (Args.PZ_INSTALL_TYPE == PZInstallType.CLIENT) {
          addTask(new ClientBackupTask(scanner, Args.SILENT));
        } else {
          addTask(new ServerBackupTask(scanner, Args.SILENT));
        }
      }

      this.taskCleanup = new CacheCleanupTask();
    }
  }

  public void run() {
    printTitle();
    for (Task task : tasks) {
      try {
        String taskName = task.getName();
        String line = "Running task: " + taskName + "..";
        String border = "#".repeat(line.length());
        println("\n" + border + '\n' + line + '\n' + border + '\n');
        task.run();
      } catch (Exception e) {
        if (!task.continueIfFailure()) {
          errln(
              "The task \""
                  + task.getName()
                  + "\" failed to run and the installer cannot continue.");
          e.printStackTrace(System.err);
          cleanup();
          println();
          return;
        }
      }
    }
    cleanup();
    println();
  }

  private void cleanup() {
    if (taskCleanup != null) {
      try {
        String taskName = taskCleanup.getName();
        String line = "Running task: " + taskName + "..";
        String border = "#".repeat(line.length());
        println("\n" + border + '\n' + line + '\n' + border + '\n');
        taskCleanup.run();
      } catch (Exception e) {
        if (!taskCleanup.continueIfFailure()) {
          errln("The task \"" + taskCleanup.getName() + "\" failed to run.");
          cleanup();
        }
      }
    }
  }

  protected void printTitle() {
    String firstString = name + ' ' + version;
    String borderString = "#".repeat(firstString.length());
    println(borderString);
    println();
    println(firstString);
    println();
    printlnRight(borderString.length(), "By " + author);
    println();
    println(borderString);
  }

  public void addTask(Task task) {
    this.tasks.add(task);
  }

  public String getName() {
    return name;
  }

  public String getAuthor() {
    return author;
  }

  public String getVersion() {
    return version;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public Task getCleanupTask() {
    return this.taskCleanup;
  }

  public void setTaskCleanup(Task taskCleanup) {
    this.taskCleanup = taskCleanup;
  }

  public static class Args {

    public static File PZ_DIRECTORY = null;
    public static File CACHE_DIRECTORY = null;
    public static PZVersion PZ_VERSION = null;
    public static PZInstallType PZ_INSTALL_TYPE = PZInstallType.CLIENT;
    public static boolean KEEP_CACHE = false;
    public static boolean NO_BACKUP = false;
    public static boolean SILENT = false;
    public static boolean FORCE_PATCH = false;
    public static boolean HELP = false;

    public static void processArgs(List<String> args) {

      if (args.contains("--help")) {
        HELP = true;
      }

      // Client-Server check. (Default is client)
      // --server (Server-mode)
      if (args.contains("--server")) {
        PZ_INSTALL_TYPE = PZInstallType.SERVER;
      } else {
        PZ_INSTALL_TYPE = PZInstallType.CLIENT;
      }

      // (--keep_cache flag)
      if (args.contains("--keep_cache")) {
        KEEP_CACHE = true;
      }

      // (--silent flag)
      if (args.contains("--silent")) {
        SILENT = true;
      }

      // (--force flag)
      if (args.contains("--force")) {
        FORCE_PATCH = true;
      }

      // (--no_backup flag)
      if (args.contains("--no_backup")) {
        NO_BACKUP = true;
      }

      // --pz_dir <PATH_TO_DIRECTORY>
      if (args.contains("--pz_dir")) {
        int indexOf = args.indexOf("--pz_dir");

        // (no-path provided check)
        if (args.size() <= indexOf + 1) {
          throw new IllegalArgumentException(
              "--pz_dir provided without directory. Use '--pz_dir <DIRECTORY>'.");
        }

        String dir = args.get(indexOf + 1);
        File fDir = new File(dir);

        // (path-not-found check)
        if (!fDir.exists()) {
          throw new IllegalArgumentException("--dir directory doesn't exist: " + fDir.getPath());
        }

        PZ_DIRECTORY = fDir;
      }

      // --pz_version <VERSION>
      if (args.contains("--pz_version")) {
        int indexOf = args.indexOf("--pz_version");

        // (no-path provided check)
        if (args.size() <= indexOf + 1) {
          throw new IllegalArgumentException(
              "--pz_version provided without version. Use '--pz_version <VERSION>'.");
        }

        PZ_VERSION = new PZVersion(args.get(indexOf + 1));
      }
    }

    public static void help() {
      String[] lines =
          new String[] {
            "Arguments:",
            "",
            "\t--force",
            "\t\tForces the installation of the patch regardless of the version installed.",
            "",
            "\t--help",
            "\t\tDisplays this message.",
            "",
            "\t--keep-cache",
            "\t\tDon't remove the '.patch-installer-cache' folder after running tasks.",
            "",
            "\t--no_backup",
            "\t\tSkips backup task.",
            "",
            "\t--pz_dir <PATH>",
            "\t\tManually sets the directory to the ProjectZomboid installation.",
            "",
            "\t--pz_version <VERSION>",
            "\t\tManually sets the version of ProjectZomboid. E.G: 41.78.16",
            "",
            "\t--server",
            "\t\tSets the installer as \"Server Mode\". This is for dedicated servers.",
            "",
            "\t--silent",
            "\t\tRuns tasks without user-input.",
            "",
          };

      for (String line : lines) {
        println(line);
      }
    }
  }
}
