/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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
package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentExportData;
import jalview.exceptions.NoFileSelectedException;
import jalview.json.binding.biojs.BioJSReleasePojo;
import jalview.json.binding.biojs.BioJSRepositoryPojo;
import jalview.util.MessageManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.TreeMap;

public class BioJsHTMLOutput
{
  private AlignmentViewPanel ap;

  private static File currentBJSTemplateFile;

  private static TreeMap<String, File> bioJsMSAVersions;

  public static final String DEFAULT_DIR = System.getProperty("user.home")
          + File.separatorChar + ".biojs_templates" + File.separatorChar;

  public static final String BJS_TEMPLATES_LOCAL_DIRECTORY = jalview.bin.Cache
          .getDefault("biojs_template_directory", DEFAULT_DIR);

  public static final String BJS_TEMPLATE_GIT_REPO = jalview.bin.Cache
          .getDefault(
                  "biojs_template_git_repo",
                  "https://raw.githubusercontent.com/jalview/exporter-templates/master/biojs/package.json");

  public BioJsHTMLOutput(AlignmentViewPanel ap)
  {
    if (ap != null)
    {
      this.ap = ap;
    }
  }

  public void exportJalviewAlignmentAsBioJsHtmlFile()
  {
    try
    {
      String outputFile = getOutputFile();
      // String jalviewAlignmentJson = JSONFile.getJSONData(ap);
      AlignExportSettingI exportSettings = new AlignExportSettingI()
      {
        @Override
        public boolean isExportHiddenSequences()
        {
          return true;
        }

        @Override
        public boolean isExportHiddenColumns()
        {
          return true;
        }

        @Override
        public boolean isExportAnnotations()
        {
          return true;
        }

        @Override
        public boolean isExportFeatures()
        {
          return true;
        }

        @Override
        public boolean isExportGroups()
        {
          return true;
        }

        @Override
        public boolean isCancelled()
        {
          return false;
        }

      };
      AlignmentExportData exportData = jalview.gui.AlignFrame
              .getAlignmentForExport(JSONFile.FILE_DESC,
                      ap.getAlignViewport(), exportSettings);
      if (exportData.getSettings().isCancelled())
      {
        return;
      }
      String jalviewAlignmentJson = new FormatAdapter(ap,
              exportData.getSettings()).formatSequences(JSONFile.FILE_DESC,
              exportData.getAlignment(), exportData.getOmitHidden(),
              exportData.getStartEndPostions(), ap.getAlignViewport()
                      .getColumnSelection());

      String bioJSTemplateString = getBioJsTemplateAsString();
      String generatedBioJsWithJalviewAlignmentAsJson = bioJSTemplateString
              .replaceAll("#sequenceData#", jalviewAlignmentJson)
              .toString();

      PrintWriter out = new java.io.PrintWriter(new java.io.FileWriter(
              outputFile));
      out.print(generatedBioJsWithJalviewAlignmentAsJson);
      out.flush();
      out.close();
      jalview.util.BrowserLauncher.openURL("file:///" + outputFile);
    } catch (NoFileSelectedException ex)
    {
      // do noting if no file was selected
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  public String getOutputFile() throws NoFileSelectedException
  {
    String selectedFile = null;
    JalviewFileChooser jvFileChooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "html" }, new String[] { "HTML files" },
            "HTML files");
    jvFileChooser.setFileView(new JalviewFileView());

    jvFileChooser.setDialogTitle(MessageManager
            .getString("label.save_as_biojs_html"));
    jvFileChooser.setToolTipText(MessageManager.getString("action.save"));

    int fileChooserOpt = jvFileChooser.showSaveDialog(null);
    if (fileChooserOpt == JalviewFileChooser.APPROVE_OPTION)
    {
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", jvFileChooser
              .getSelectedFile().getParent());
      selectedFile = jvFileChooser.getSelectedFile().getPath();
    }
    else
    {
      throw new NoFileSelectedException("No file was selected.");
    }
    return selectedFile;
  }

  public static String getBioJsTemplateAsString() throws IOException
  {
    InputStreamReader isReader = null;
    BufferedReader buffReader = null;
    StringBuilder sb = new StringBuilder();
    Objects.requireNonNull(getCurrentBJSTemplateFile(),
            "BioJsTemplate File not initialized!");
    @SuppressWarnings("deprecation")
    URL url = getCurrentBJSTemplateFile().toURL();
    if (url != null)
    {
      try
      {
        isReader = new InputStreamReader(url.openStream());
        buffReader = new BufferedReader(isReader);
        String line;
        String lineSeparator = System.getProperty("line.separator");
        while ((line = buffReader.readLine()) != null)
        {
          sb.append(line).append(lineSeparator);
        }

      } catch (Exception ex)
      {
        ex.printStackTrace();
      } finally
      {
        if (isReader != null)
        {
          isReader.close();
        }

        if (buffReader != null)
        {
          buffReader.close();
        }
      }
    }
    return sb.toString();
  }

  public static void refreshBioJSVersionsInfo(String dirName)
          throws URISyntaxException
  {
    File directory = new File(BJS_TEMPLATES_LOCAL_DIRECTORY);
    Objects.requireNonNull(dirName, "dirName MUST not be null!");
    Objects.requireNonNull(directory, "directory MUST not be null!");
    TreeMap<String, File> versionFileMap = new TreeMap<String, File>();

    for (File file : directory.listFiles())
    {
      if (file.isFile())
      {
        String fileName = file.getName().substring(0,
                file.getName().lastIndexOf("."));
        String fileMeta[] = fileName.split("_");
        if (fileMeta.length > 2)
        {
          setCurrentBJSTemplateFile(file);
          versionFileMap.put(fileMeta[2], file);
        }
        else if (fileMeta.length > 1)
        {
          versionFileMap.put(fileMeta[1], file);
        }
      }
    }
    if (getCurrentBJSTemplateFile() == null && versionFileMap.size() > 0)
    {
      setCurrentBJSTemplateFile(versionFileMap.lastEntry().getValue());
    }
    setBioJsMSAVersions(versionFileMap);
  }

  public static void updateBioJS()
  {
    Thread updateThread = new Thread()
    {
      public void run()
      {
        try
        {
          String gitRepoPkgJson = getURLContentAsString(BJS_TEMPLATE_GIT_REPO);
          if (gitRepoPkgJson != null)
          {
            BioJSRepositoryPojo release = new BioJSRepositoryPojo(
                    gitRepoPkgJson);
            syncUpdates(BJS_TEMPLATES_LOCAL_DIRECTORY, release);
            refreshBioJSVersionsInfo(BJS_TEMPLATES_LOCAL_DIRECTORY);
          }
        } catch (URISyntaxException e)
        {
          e.printStackTrace();
        }
      }
    };
    updateThread.start();

  }

  public static void syncUpdates(String localDir, BioJSRepositoryPojo repo)
  {
    for (BioJSReleasePojo bjsRelease : repo.getReleases())
    {
      String releaseUrl = bjsRelease.getUrl();
      String releaseVersion = bjsRelease.getVersion();
      String releaseFile = "BioJsMSA_" + releaseVersion + ".txt";
      if (releaseVersion.equals(repo.getLatestReleaseVersion()))
      {
        releaseFile = "Latest_BioJsMSA_" + releaseVersion + ".txt";
      }

      File biojsDirectory = new File(BJS_TEMPLATES_LOCAL_DIRECTORY);
      if (!biojsDirectory.exists())
      {
        if (!biojsDirectory.mkdirs())
        {
          System.out.println("Couldn't create local directory : "
                  + BJS_TEMPLATES_LOCAL_DIRECTORY);
          return;
        }
      }

      File file = new File(BJS_TEMPLATES_LOCAL_DIRECTORY + releaseFile);
      if (!file.exists())
      {

        PrintWriter out = null;
        try
        {
          out = new java.io.PrintWriter(new java.io.FileWriter(file));
          out.print(getURLContentAsString(releaseUrl));
        } catch (IOException e)
        {
          e.printStackTrace();
        } finally
        {
          if (out != null)
          {
            out.flush();
            out.close();
          }
        }
      }
    }

  }

  public static String getURLContentAsString(String url)
          throws OutOfMemoryError
  {
    StringBuilder responseStrBuilder = null;
    InputStream is = null;
    try
    {
      URL resourceUrl = new URL(url);
      is = new BufferedInputStream(resourceUrl.openStream());
      BufferedReader br = new BufferedReader(new InputStreamReader(is));
      responseStrBuilder = new StringBuilder();
      String lineContent;

      while ((lineContent = br.readLine()) != null)
      {
        responseStrBuilder.append(lineContent).append("\n");
      }
    } catch (OutOfMemoryError er)
    {
      er.printStackTrace();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (is != null)
      {
        try
        {
          is.close();
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    }
    return responseStrBuilder == null ? null : responseStrBuilder
            .toString();
  }

  public static File getCurrentBJSTemplateFile()
  {
    return currentBJSTemplateFile;
  }

  public static void setCurrentBJSTemplateFile(File currentBJSTemplateFile)
  {
    BioJsHTMLOutput.currentBJSTemplateFile = currentBJSTemplateFile;
  }

  public static TreeMap<String, File> getBioJsMSAVersions()
  {
    return bioJsMSAVersions;
  }

  public static void setBioJsMSAVersions(
          TreeMap<String, File> bioJsMSAVersions)
  {
    BioJsHTMLOutput.bioJsMSAVersions = bioJsMSAVersions;
  }

}
