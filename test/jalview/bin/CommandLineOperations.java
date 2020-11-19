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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import jalview.gui.JvOptionPane;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.IdentifyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.testng.Assert;
import org.testng.FileAssert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ModuleRef;
import io.github.classgraph.ScanResult;

public class CommandLineOperations
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  private static final int TEST_TIMEOUT = 9000; // Note longer timeout needed
                                                // on
                                                // full test run than on
                                                // individual tests

  private static final int SETUP_TIMEOUT = 9000;

  private static final int MINFILESIZE_SMALL = 2096;

  private static final int MINFILESIZE_BIG = 4096;

  private ArrayList<String> successfulCMDs = new ArrayList<>();

  /***
   * from
   * http://stackoverflow.com/questions/808276/how-to-add-a-timeout-value-when
   * -using-javas-runtime-exec
   * 
   * @author jimp
   * 
   */
  private static class Worker extends Thread
  {
    private final Process process;

    private BufferedReader outputReader;

    private BufferedReader errorReader;

    private Integer exit;

    private Worker(Process process)
    {
      this.process = process;
    }

    @Override
    public void run()
    {
      try
      {
        exit = process.waitFor();
      } catch (InterruptedException ignore)
      {
        return;
      }
    }

    public BufferedReader getOutputReader()
    {
      return outputReader;
    }

    public void setOutputReader(BufferedReader outputReader)
    {
      this.outputReader = outputReader;
    }

    public BufferedReader getErrorReader()
    {
      return errorReader;
    }

    public void setErrorReader(BufferedReader errorReader)
    {
      this.errorReader = errorReader;
    }
  }

  private static ClassGraph scanner = null;

  private static String classpath = null;

  private static String modules = null;

  private static String java_exe = null;

  public synchronized static String getClassPath()
  {
    if (scanner == null)
    {
      scanner = new ClassGraph();
      ScanResult scan = scanner.scan();
      classpath = scan.getClasspath();
      modules = "";
      for (ModuleRef mr : scan.getModules())
      {
        modules.concat(mr.getName());
      }
      java_exe = System.getProperty("java.home") + File.separator + "bin"
              + File.separator + "java";

    }
    while (classpath == null)
    {
      try
      {
        Thread.sleep(10);
      } catch (InterruptedException x)
      {

      }
    }
    return classpath;
  }

  private Worker getJalviewDesktopRunner(boolean withAwt, String cmd,
          int timeout)
  {
    // Note: JAL-3065 - don't include quotes for lib/* because the arguments are
    // not expanded by the shell
    String classpath = getClassPath();
    String _cmd = java_exe + " "
            + (withAwt ? "-Djava.awt.headless=true" : "")
            + " -classpath " + classpath
            + (modules.length() > 2 ? "--add-modules=\"" + modules + "\""
                    : "")
            + " jalview.bin.Jalview ";
    Process ls2_proc = null;
    Worker worker = null;
    try
    {
      ls2_proc = Runtime.getRuntime().exec(_cmd + cmd);
    } catch (Throwable e1)
    {
      e1.printStackTrace();
    }
    if (ls2_proc != null)
    {
      BufferedReader outputReader = new BufferedReader(
              new InputStreamReader(ls2_proc.getInputStream()));
      BufferedReader errorReader = new BufferedReader(
              new InputStreamReader(ls2_proc.getErrorStream()));
      worker = new Worker(ls2_proc);
      worker.start();
      try
      {
        worker.join(timeout);
      } catch (InterruptedException e)
      {
        System.err.println("Thread interrupted");
      }
      worker.setOutputReader(outputReader);
      worker.setErrorReader(errorReader);
    }
    return worker;
  }

  @Test(groups = { "Functional" })
  public void reportCurrentWorkingDirectory()
  {
    try
    {
      Path currentRelativePath = Paths.get("");
      String s = currentRelativePath.toAbsolutePath().toString();
      System.out.println("Test CWD is " + s);
    } catch (Exception q)
    {
      q.printStackTrace();
    }
  }

  @BeforeTest(alwaysRun = true)
  public void initialize()
  {
    new CommandLineOperations();
  }

  @BeforeTest(alwaysRun = true)
  public void setUpForHeadlessCommandLineInputOperations()
          throws IOException
  {
    String cmds = "nodisplay -open examples/uniref50.fa -sortbytree -props test/jalview/io/testProps.jvprops -colour zappo "
            + "-jabaws http://www.compbio.dundee.ac.uk/jabaws -nosortbytree "
            + "-features examples/testdata/plantfdx.features -annotations examples/testdata/plantfdx.annotations -tree examples/testdata/uniref50_test_tree";
    Worker worker = getJalviewDesktopRunner(true, cmds, SETUP_TIMEOUT);
    String ln = null;
    while ((ln = worker.getOutputReader().readLine()) != null)
    {
      System.out.println(ln);
      successfulCMDs.add(ln);
    }
    while ((ln = worker.getErrorReader().readLine()) != null)
    {
      System.err.println(ln);
    }
  }

  @BeforeTest(alwaysRun = true)
  public void setUpForCommandLineInputOperations() throws IOException
  {
    String cmds = "-open examples/uniref50.fa -noquestionnaire -nousagestats";
    Worker worker = getJalviewDesktopRunner(false, cmds, SETUP_TIMEOUT);
    String ln = null;
    int count = 0;
    while ((ln = worker.getErrorReader().readLine()) != null)
    {
      System.out.println(ln);
      successfulCMDs.add(ln);
      if (++count > 25)
      {
        break;
      }
    }
    if (worker != null && worker.exit == null)
    {
      worker.interrupt();
      Thread.currentThread().interrupt();
      worker.process.destroy();
    }
  }

  @Test(groups = { "Functional" }, dataProvider = "allInputOperationsData")
  public void testAllInputOperations(String expectedString,
          String failureMsg)
  {
    Assert.assertTrue(successfulCMDs.contains(expectedString), failureMsg);
  }

  @Test(
    groups =
    { "Functional", "testben" },
    dataProvider = "headlessModeOutputOperationsData")
  public void testHeadlessModeOutputOperations(String harg, String type,
          String fileName, boolean withAWT, int expectedMinFileSize,
          int timeout, String fileFormatType)
  {
    String cmd = harg + type + " " + fileName;
    // System.out.println(">>>>>>>>>>>>>>>> Command : " + cmd);
    File file = new File(fileName);
    file.deleteOnExit();
    Worker worker = getJalviewDesktopRunner(withAWT, cmd, timeout);
    assertNotNull(worker, "worker is null");
    String msg = "Didn't create an output" + type + " file.[" + harg + "]";
    assertTrue(file.exists(), msg);
    FileAssert.assertFile(file, msg);
    FileAssert.assertMinLength(file, expectedMinFileSize);
    if (fileFormatType!=null && fileFormatType.length()>0)
    {
      FileFormatI format = FileFormats.getInstance()
              .forName(fileFormatType);
      if (format!=null)
      {
        try
        {
          FileFormatI exportedType = new IdentifyFile()
                  .identify(file.getAbsolutePath(), DataSourceType.FILE);
          assertEquals(exportedType, format,
                  "Exported file type was wrong");
        } catch (FileFormatException e)
        {
          Assert.fail("Couldn't identify file " + file
                  + " as an alignment format", e);
        }
      }
    }
    if (worker != null && worker.exit == null)
    {
      worker.interrupt();
      Thread.currentThread().interrupt();
      worker.process.destroy();
      Assert.fail("Jalview did not exit after " + type
              + " generation (try running test again to verify - timeout at "
              + timeout + "ms). [" + harg + "]");
    }
    file.delete();
  }

  @DataProvider(name = "allInputOperationsData")
  public Object[][] getHeadlessModeInputParams()
  {
    return new Object[][] {
        // headless mode input operations
        { "CMD [-color zappo] executed successfully!",
            "Failed command : -color zappo" },
        { "CMD [-props test/jalview/io/testProps.jvprops] executed successfully!",
            "Failed command : -props File" },
        { "CMD [-sortbytree] executed successfully!",
            "Failed command : -sortbytree" },
        { "CMD [-jabaws http://www.compbio.dundee.ac.uk/jabaws] executed successfully!",
            "Failed command : -jabaws http://www.compbio.dundee.ac.uk/jabaws" },
        { "CMD [-open examples/uniref50.fa] executed successfully!",
            "Failed command : -open examples/uniref50.fa" },
        { "CMD [-nosortbytree] executed successfully!",
            "Failed command : -nosortbytree" },
        { "CMD [-features examples/testdata/plantfdx.features]  executed successfully!",
            "Failed command : -features examples/testdata/plantfdx.features" },
        { "CMD [-annotations examples/testdata/plantfdx.annotations] executed successfully!",
            "Failed command : -annotations examples/testdata/plantfdx.annotations" },
        { "CMD [-tree examples/testdata/uniref50_test_tree] executed successfully!",
            "Failed command : -tree examples/testdata/uniref50_test_tree" },
        // non headless mode input operations
        { "CMD [-nousagestats] executed successfully!",
            "Failed command : -nousagestats" },
        { "CMD [-noquestionnaire] executed successfully!",
            "Failed command : -noquestionnaire" } };
  }

  @DataProvider(name = "headlessModeOutputOperationsData")
  public static Object[][] getHeadlessModeOutputParams()
  {
    // JBPNote: I'm not clear why need to specify full path for output file
    // when running tests on build server, but we will keep this patch for now
    // since it works.
    // https://issues.jalview.org/browse/JAL-1889?focusedCommentId=21609&page=com.atlassian.jira.plugin.system.issuetabpanels:comment-tabpanel#comment-21609
    String workingDir = "test/jalview/bin/";
    return new Object[][] { { "nodisplay -open examples/uniref50.fa",
        " -eps", workingDir + "test_uniref50_out.eps", true,
        MINFILESIZE_BIG, TEST_TIMEOUT, null },
        { "nodisplay -open examples/uniref50.fa", " -eps",
            workingDir + "test_uniref50_out.eps", false,
            MINFILESIZE_BIG, TEST_TIMEOUT, null },
        { "nogui -open examples/uniref50.fa", " -eps",
            workingDir + "test_uniref50_out.eps", true, MINFILESIZE_BIG,
            TEST_TIMEOUT, null },
        { "nogui -open examples/uniref50.fa", " -eps",
            workingDir + "test_uniref50_out.eps", false,
            MINFILESIZE_BIG, TEST_TIMEOUT, null },
        { "headless -open examples/uniref50.fa", " -eps",
            workingDir + "test_uniref50_out.eps", true, MINFILESIZE_BIG,
            TEST_TIMEOUT, null },
        { "headless -open examples/uniref50.fa", " -svg",
            workingDir + "test_uniref50_out.svg", false,
            MINFILESIZE_BIG, TEST_TIMEOUT, null },
        { "headless -open examples/uniref50.fa", " -png",
            workingDir + "test_uniref50_out.png", true, MINFILESIZE_BIG,
            TEST_TIMEOUT, null },
        { "headless -open examples/uniref50.fa", " -html",
            workingDir + "test_uniref50_out.html", true,
            MINFILESIZE_BIG, TEST_TIMEOUT, null },
        { "headless -open examples/uniref50.fa", " -fasta",
            workingDir + "test_uniref50_out.mfa", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.Fasta.toString() },
        { "headless -open examples/uniref50.fa", " -clustal",
            workingDir + "test_uniref50_out.aln", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.Clustal.toString() },
        { "headless -open examples/uniref50.fa", " -msf",
            workingDir + "test_uniref50_out.msf", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.MSF.toString() },
        { "headless -open examples/uniref50.fa", " -pileup",
            workingDir + "test_uniref50_out.aln", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.Pileup.toString() },
        { "headless -open examples/uniref50.fa", " -pir",
            workingDir + "test_uniref50_out.pir", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.PIR.toString() },
        { "headless -open examples/uniref50.fa", " -pfam",
            workingDir + "test_uniref50_out.pfam", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.Pfam.toString() },
        { "headless -open examples/uniref50.fa", " -blc",
            workingDir + "test_uniref50_out.blc", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.BLC.toString() },
        { "headless -open examples/uniref50.fa", " -jalview",
            workingDir + "test_uniref50_out.jvp", true, MINFILESIZE_SMALL,
            TEST_TIMEOUT, FileFormat.Jalview.toString() }, };
  }
}
