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

/**
 * Methods to decide on appropriate memory setting for Jalview based on two
 * optionally provided values: jvmmempc - the maximum percentage of total
 * physical memory to allocate, and jvmmemmax - the maximum absolute amount of
 * physical memory to allocate. These can be provided as arguments or system
 * properties. Other considerations such as minimum application requirements and
 * leaving space for OS are used too.
 * 
 * @author bsoares
 *
 */
public class MemorySetting
{
  public static final String MAX_HEAPSIZE_PERCENT_PROPERTY_NAME = "jvmmempc";

  public static final String MAX_HEAPSIZE_PROPERTY_NAME = "jvmmemmax";

  private static final int MAX_HEAPSIZE_PERCENT_DEFAULT = 90; // 90%

  private static final long GIGABYTE = 1073741824; // 1GB

  public static final long LEAVE_FREE_MIN_MEMORY = GIGABYTE/2;

  public static final long APPLICATION_MIN_MEMORY = GIGABYTE/2;

  private static final long MAX_HEAPSIZE_GB_DEFAULT = 32;

  private static final long NOMEM_MAX_HEAPSIZE_GB_DEFAULT = 8;

  protected static boolean logToClassChecked = false;

  public static long getMemorySetting()
  {
    return getMemorySetting(null, null);
  }

  /**
   * Decide on appropriate memory setting for Jalview based on the two arguments
   * values: jvmmempc - the maximum percentage of total physical memory to
   * allocate, and jvmmemmax - the maximum absolute amount of physical memory to
   * allocate. These can be provided as arguments. If not provided as arguments
   * (or set as null) system properties will be used instead (if set). The memory
   * setting returned will be the lower of the two values. If either of the values
   * are not provided then defaults will be used (jvmmempc=90, jvmmemmax=32GB). If
   * total physical memory can't be ascertained when jvmmempc was set or neither
   * jvmmempc nor jvmmemmax were set, then jvmmemmax defaults to a much safer 8GB.
   * In this case explicitly setting jvmmemmax and not setting jvmmempc can set a
   * higher memory for Jalview. The calculation also tries to ensure 0.5GB memory
   * for the OS, but also tries to ensure at least 0.5GB memory for Jalview (which
   * takes priority over the OS) If there is less then 0.5GB of physical memory
   * then the total physical memory is used for Jalview.
   * 
   * @param jvmmemmaxarg
   *                       Maximum value of memory to set. This can be a numeric
   *                       string optionally followed by "b", "k", "m", "g", "t"
   *                       (case insensitive) to indicate bytes, kilobytes,
   *                       megabytes, gigabytes, terabytes respectively. If null a
   *                       default value of 32G will be used. If null and either
   *                       physical memory can't be determined then the default is
   *                       8GB.
   * @param jvmmempcarg
   *                       Max percentage of physical memory to use. Defaults to
   *                       "90".
   * 
   * @return The amount of memory (in bytes) to allocate to Jalview
   */
  public static long getMemorySetting(String jvmmemmaxarg,
          String jvmmempcarg)
  {
    // actual Xmx value-to-be
    long maxMemLong = -1;

    // (absolute) jvmmaxmem setting, start with default
    long memmax = MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
    if (jvmmemmaxarg == null)
    {
      jvmmemmaxarg = System.getProperty(MAX_HEAPSIZE_PROPERTY_NAME);
    }
    String jvmmemmax = jvmmemmaxarg;
    if (jvmmemmax != null && jvmmemmax.length() > 0)
    {
      long multiplier = 1;
      switch (jvmmemmax.toLowerCase().substring(jvmmemmax.length() - 1))
      {
      case "t":
        multiplier = 1099511627776L; // 2^40
        jvmmemmax = jvmmemmax.substring(0, jvmmemmax.length() - 1);
        break;
      case "g":
        multiplier = 1073741824; // 2^30
        jvmmemmax = jvmmemmax.substring(0, jvmmemmax.length() - 1);
        break;
      case "m":
        multiplier = 1048576; // 2^20
        jvmmemmax = jvmmemmax.substring(0, jvmmemmax.length() - 1);
        break;
      case "k":
        multiplier = 1024; // 2^10
        jvmmemmax = jvmmemmax.substring(0, jvmmemmax.length() - 1);
        break;
      case "b":
        multiplier = 1; // 2^0
        jvmmemmax = jvmmemmax.substring(0, jvmmemmax.length() - 1);
        break;
      default:
        break;
      }

      // parse the arg
      try
      {
        memmax = Long.parseLong(jvmmemmax);
      } catch (NumberFormatException e)
      {
        memmax = MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
        System.out.println("MemorySetting Property '"
                + MAX_HEAPSIZE_PROPERTY_NAME
                + "' ("
                + jvmmemmaxarg + "') badly formatted, using default ("
                + MAX_HEAPSIZE_GB_DEFAULT + "g).");
      }

      // apply multiplier if not too big (i.e. bigger than a long)
      if (Long.MAX_VALUE / memmax < multiplier)
      {
        memmax = MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
        System.out.println(
                "MemorySetting Property '" + MAX_HEAPSIZE_PROPERTY_NAME + "' ("
                        + jvmmemmaxarg
                        + ") too big, using default ("
                        + MAX_HEAPSIZE_GB_DEFAULT + "g).");
      }
      else
      {
        memmax = multiplier * memmax;
      }

      // check at least minimum value (this accounts for negatives too)
      if (memmax < APPLICATION_MIN_MEMORY)
      {
        memmax = APPLICATION_MIN_MEMORY;
        System.out.println(
                "MemorySetting Property '" + MAX_HEAPSIZE_PROPERTY_NAME + "' ("
                        + jvmmemmaxarg
                        + ") too small, using minimum ("
                        + APPLICATION_MIN_MEMORY + ").");
      }

    }
    else
    {
      // no need to warn if no setting
      // System.out.println("MemorySetting Property '" + maxHeapSizeProperty
      // + "' not
      // set.");
    }

    // get max percent of physical memory, starting with default
    float percent = MAX_HEAPSIZE_PERCENT_DEFAULT;
    if (jvmmempcarg == null)
    {
      jvmmempcarg = System.getProperty(MAX_HEAPSIZE_PERCENT_PROPERTY_NAME);
    }
    String jvmmempc = jvmmempcarg;
    long mempc = -1;
    try
    {
      if (jvmmempc != null)
      {
        float trypercent = Float.parseFloat(jvmmempc);
        if (0 < trypercent && trypercent <= 100f)
        {
          percent = trypercent;
        }
        else
        {
          System.out.println(
                  "MemorySetting Property '"
                          + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME
                          + "' should be in range 1..100. Using default "
                          + percent + "%");
        }
      }
    } catch (NumberFormatException e)
    {
      System.out.println(
              "MemorySetting property '" + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME
                      + "' (" + jvmmempcarg + ") badly formatted");
    }

    // catch everything in case of no com.sun.management.OperatingSystemMXBean
    boolean memoryPercentError = false;
    try
    {
      long physicalMem = GetMemory.getPhysicalMemory();
      if (physicalMem > APPLICATION_MIN_MEMORY)
      {
        // try and set at least applicationMinMemory and thereafter ensure
        // leaveFreeMinMemory is left for the OS

        mempc = (long) ((physicalMem / 100F) * percent);

        // check for memory left for OS
        boolean reducedmempc = false;
        if (physicalMem - mempc < LEAVE_FREE_MIN_MEMORY)
        {
          mempc = physicalMem - LEAVE_FREE_MIN_MEMORY;
          reducedmempc = true;
          System.out.println("MemorySetting Property '"
                  + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' (" + jvmmempcarg
                  + ") too large. Leaving free space for OS and reducing to ("
                  + mempc + ").");
        }

        // check for minimum application memsize
        if (mempc < APPLICATION_MIN_MEMORY)
        {
          if (reducedmempc)
          {
            System.out.println("Reduced MemorySetting (" + mempc
                    + ") too small. Increasing to application minimum ("
                    + APPLICATION_MIN_MEMORY + ").");
          }
          else
          {
            System.out.println("MemorySetting Property '"
                    + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' (" + jvmmempcarg
                    + ") too small. Using minimum (" + APPLICATION_MIN_MEMORY
                    + ").");
          }
          mempc = APPLICATION_MIN_MEMORY;
        }
      }
      else
      {
        // not enough memory for application, just try and grab what we can!
        mempc = physicalMem;
        System.out.println(
                "Not enough physical memory for application. Ignoring MemorySetting Property '"
                        + MAX_HEAPSIZE_PERCENT_PROPERTY_NAME + "' ("
                        + jvmmempcarg
                        + "). Using maximum memory available ("
                        + physicalMem + ").");
      }

    } catch (Throwable t)
    {
      memoryPercentError = true;
      System.out.println(
              "Problem calling GetMemory.getPhysicalMemory(). Likely to be problem with com.sun.management.OperatingSystemMXBean");
      t.printStackTrace();
    }

    // In the case of an error reading the percentage of physical memory (when
    // jvmmempc was set OR neither jvmmempc nor jvmmemmax were set), let's cap
    // maxMemLong to 8GB
    if (memoryPercentError && mempc == -1
            && !(jvmmempcarg == null && jvmmemmaxarg != null) // the same as (jvmmempcarg != null || (jvmmempcarg == null && jvmmemmaxarg
                                                              // == null))
            && memmax > NOMEM_MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE)
    {
      System.out.println(
              "Capping maximum memory to " + NOMEM_MAX_HEAPSIZE_GB_DEFAULT
                      + "g due to failure to read physical memory size.");
      memmax = NOMEM_MAX_HEAPSIZE_GB_DEFAULT * GIGABYTE;
    }

    if (mempc == -1) // percentage memory not set
    {
      maxMemLong = memmax;
    }
    else
    {
      maxMemLong = Math.min(mempc, memmax);
    }

    return maxMemLong;
  }

}