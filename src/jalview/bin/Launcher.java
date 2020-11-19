/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *  
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 * The Jalview Authors are detailed in the 'AUTHORS' file.
 */
package jalview.bin;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * A Launcher class for Jalview. This class is used to launch Jalview from the
 * shadowJar when Getdown is not used or available. It attempts to take all the
 * command line arguments to pass on to the jalview.bin.Jalview class, but to
 * insert a -Xmx memory setting to a sensible default, using the -jvmmempc and
 * -jvmmemmax application arguments if specified. If not specified then system
 * properties will be looked for by jalview.bin.MemorySetting. If the user has
 * provided the JVM with a -Xmx setting directly and not set -jvmmempc or
 * -jvmmemmax then this setting will be used and system properties ignored. If
 * -Xmx is set as well as -jvmmempc or -jvmmemmax as argument(s) then the -Xmx
 * argument will NOT be passed on to the main application launch.
 * 
 * @author bsoares
 *
 */
public class Launcher
{
  private final static String startClass = "jalview.bin.Jalview";

  private final static String dockIconPath = "JalviewLogo_Huge.png";

  /**
   * main method for jalview.bin.Launcher. This restarts the same JRE's JVM with
   * the same arguments but with memory adjusted based on extracted -jvmmempc
   * and -jvmmemmax application arguments. If on a Mac then extra dock:icon and
   * dock:name arguments are also set.
   * 
   * @param args
   */
  public static void main(String[] args)
  {
    final String javaBin = System.getProperty("java.home") + File.separator
            + "bin" + File.separator + "java";

    List<String> command = new ArrayList<>();
    command.add(javaBin);

    String memSetting = null;

    boolean isAMac = System.getProperty("os.name").indexOf("Mac") > -1;

    for (String jvmArg : ManagementFactory.getRuntimeMXBean()
            .getInputArguments())
    {
      command.add(jvmArg);
    }
    command.add("-cp");
    command.add(ManagementFactory.getRuntimeMXBean().getClassPath());

    String jvmmempc = null;
    String jvmmemmax = null;
    ArrayList<String> arguments = new ArrayList<>();
    for (String arg : args)
    {
      // jvmmempc and jvmmemmax args used to set memory and are not passed on to
      // startClass
      if (arg.startsWith(
              "-" + MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "="))
      {
        jvmmempc = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PERCENT_PROPERTY_NAME.length()
                        + 2);
      }
      else if (arg.startsWith(
              "-" + MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME + "="))
      {
        jvmmemmax = arg.substring(
                MemorySetting.MAX_HEAPSIZE_PROPERTY_NAME.length() + 2);
      }
      else
      {
        arguments.add(arg);
      }
    }

    // add memory setting if not specified
    boolean memSet = false;
    boolean dockIcon = false;
    boolean dockName = false;
    for (int i = 0; i < command.size(); i++)
    {
      String arg = command.get(i);
      if (arg.startsWith("-Xmx"))
      {
        // only use -Xmx if jvmmemmax and jvmmempc have not been set
        if (jvmmempc == null && jvmmemmax == null)
        {
          memSetting = arg;
          memSet = true;
        }
      }
      else if (arg.startsWith("-Xdock:icon"))
      {
        dockIcon = true;
      }
      else if (arg.startsWith("-Xdock:name"))
      {
        dockName = true;
      }
    }

    if (!memSet)
    {
      long maxMemLong = MemorySetting.getMemorySetting(jvmmemmax, jvmmempc);

      if (maxMemLong > 0)
      {
        memSetting = "-Xmx" + Long.toString(maxMemLong);
        memSet = true;
        command.add(memSetting);
      }
    }

    if (isAMac)
    {
      if (!dockIcon)
      {
        command.add("-Xdock:icon=" + dockIconPath);
      }
      if (!dockName)
      {
        // -Xdock:name=... doesn't actually work :(
        // Leaving it in in case it gets fixed
        command.add("-Xdock:name=" + "Jalview");
      }
    }

    String scalePropertyArg = HiDPISetting.getScalePropertyArg();
    if (scalePropertyArg != null)
    {
      System.out.println("Running " + startClass + " with scale setting "
              + scalePropertyArg);
      command.add(scalePropertyArg);
    }

    command.add(startClass);
    command.addAll(arguments);

    final ProcessBuilder builder = new ProcessBuilder(command);

    // System.out.println("COMMAND: " + String.join(" ", builder.command()));
    System.out.println("Running " + startClass + " with "
            + (memSetting == null ? "no memory setting"
                    : ("memory setting " + memSetting)));

    if (Boolean.parseBoolean(System.getProperty("launcherstop")))
    {
      System.exit(0);
    }
    try
    {
      builder.inheritIO();
      Process process = builder.start();
      process.waitFor();
    } catch (IOException e)
    {
      if (e.getMessage().toLowerCase().contains("memory"))
      {
        System.out.println("Caught a memory exception: " + e.getMessage());
        // Probably the "Cannot allocate memory" error, try without the memory
        // setting
        ArrayList<String> commandNoMem = new ArrayList<>();
        for (int i = 0; i < command.size(); i++)
        {
          if (!command.get(i).startsWith("-Xmx"))
          {
            commandNoMem.add(command.get(i));
          }
        }
        final ProcessBuilder builderNoMem = new ProcessBuilder(
                commandNoMem);
        System.out.println("Command without memory setting: "
                + String.join(" ", builderNoMem.command()));
        try
        {
          builderNoMem.inheritIO();
          Process processNoMem = builderNoMem.start();
          processNoMem.waitFor();
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      else
      {
        e.printStackTrace();
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    // System.exit(0);

  }

}
