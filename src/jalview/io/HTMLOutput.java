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
package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.datamodel.AlignmentExportData;
import jalview.exceptions.NoFileSelectedException;
import jalview.gui.AlignmentPanel;
import jalview.gui.IProgressIndicator;
import jalview.util.MessageManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public abstract class HTMLOutput implements Runnable
{
  protected AlignmentPanel ap;

  protected long pSessionId;

  protected IProgressIndicator pIndicator;

  protected File generatedFile;

  public HTMLOutput(AlignmentPanel ap)
  {
    if (ap != null)
    {
      this.ap = ap;
      this.pIndicator = ap.alignFrame;
    }
  }

  public String getBioJSONData()
  {
    return getBioJSONData(null);
  }

  public String getBioJSONData(AlignExportSettingI exportSettings)
  {
    if (!isEmbedData())
    {
      return null;
    }
    if (exportSettings == null)
    {
      exportSettings = new AlignExportSettingI()
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
    }
    AlignmentExportData exportData = jalview.gui.AlignFrame
            .getAlignmentForExport(FileFormat.Json, ap.getAlignViewport(),
                    exportSettings);
    String bioJSON = new FormatAdapter(ap, exportData.getSettings())
            .formatSequences(FileFormat.Json, exportData.getAlignment(),
                    exportData.getOmitHidden(),
                    exportData.getStartEndPostions(), ap.getAlignViewport()
                            .getAlignment().getHiddenColumns());
    return bioJSON;
  }

  /**
   * Read a template file content as string
   * 
   * @param file
   *          - the file to be read
   * @return File content as String
   * @throws IOException
   */
  public static String readFileAsString(File file) throws IOException
  {
    InputStreamReader isReader = null;
    BufferedReader buffReader = null;
    StringBuilder sb = new StringBuilder();
    Objects.requireNonNull(file, "File must not be null!");
    @SuppressWarnings("deprecation")
    URL url = file.toURL();
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

  public static String getImageMapHTML()
  {
    return new String("<html>\n" + "<head>\n"
            + "<script language=\"JavaScript\">\n"
            + "var ns4 = document.layers;\n"
            + "var ns6 = document.getElementById && !document.all;\n"
            + "var ie4 = document.all;\n" + "offsetX = 0;\n"
            + "offsetY = 20;\n" + "var toolTipSTYLE=\"\";\n"
            + "function initToolTips()\n" + "{\n" + "  if(ns4||ns6||ie4)\n"
            + "  {\n"
            + "    if(ns4) toolTipSTYLE = document.toolTipLayer;\n"
            + "    else if(ns6) toolTipSTYLE = document.getElementById(\"toolTipLayer\").style;\n"
            + "    else if(ie4) toolTipSTYLE = document.all.toolTipLayer.style;\n"
            + "    if(ns4) document.captureEvents(Event.MOUSEMOVE);\n"
            + "    else\n" + "    {\n"
            + "      toolTipSTYLE.visibility = \"visible\";\n"
            + "      toolTipSTYLE.display = \"none\";\n" + "    }\n"
            + "    document.onmousemove = moveToMouseLoc;\n" + "  }\n"
            + "}\n" + "function toolTip(msg, fg, bg)\n" + "{\n"
            + "  if(toolTip.arguments.length < 1) // hide\n" + "  {\n"
            + "    if(ns4) toolTipSTYLE.visibility = \"hidden\";\n"
            + "    else toolTipSTYLE.display = \"none\";\n" + "  }\n"
            + "  else // show\n" + "  {\n"
            + "    if(!fg) fg = \"#555555\";\n"
            + "    if(!bg) bg = \"#FFFFFF\";\n" + "    var content =\n"
            + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + fg + '\"><td>' +\n"
            + "    '<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\" bgcolor=\"' + bg + \n"
            + "    '\"><td align=\"center\"><font face=\"sans-serif\" color=\"' + fg +\n"
            + "    '\" size=\"-2\">&nbsp;' + msg +\n"
            + "    '&nbsp;</font></td></table></td></table>';\n"
            + "    if(ns4)\n" + "    {\n"
            + "      toolTipSTYLE.document.write(content);\n"
            + "      toolTipSTYLE.document.close();\n"
            + "      toolTipSTYLE.visibility = \"visible\";\n" + "    }\n"
            + "    if(ns6)\n" + "    {\n"
            + "      document.getElementById(\"toolTipLayer\").innerHTML = content;\n"
            + "      toolTipSTYLE.display='block'\n" + "    }\n"
            + "    if(ie4)\n" + "    {\n"
            + "      document.all(\"toolTipLayer\").innerHTML=content;\n"
            + "      toolTipSTYLE.display='block'\n" + "    }\n" + "  }\n"
            + "}\n" + "function moveToMouseLoc(e)\n" + "{\n"
            + "  if(ns4||ns6)\n" + "  {\n" + "    x = e.pageX;\n"
            + "    y = e.pageY;\n" + "  }\n" + "  else\n" + "  {\n"
            + "    x = event.x + document.body.scrollLeft;\n"
            + "    y = event.y + document.body.scrollTop;\n" + "  }\n"
            + "  toolTipSTYLE.left = x + offsetX;\n"
            + "  toolTipSTYLE.top = y + offsetY;\n" + "  return true;\n"
            + "}\n" + "</script>\n" + "</head>\n" + "<body>\n"
            + "<div id=\"toolTipLayer\" style=\"position:absolute; visibility: hidden\"></div>\n"
            + "<script language=\"JavaScript\"><!--\n"
            + "initToolTips(); //--></script>\n");

  }

  public String getOutputFile() throws NoFileSelectedException
  {
    String selectedFile = null;
    if (pIndicator != null && !isHeadless())
    {
      pIndicator.setProgressBar(MessageManager.formatMessage(
              "status.waiting_for_user_to_select_output_file", "HTML"),
              pSessionId);
    }

    JalviewFileChooser jvFileChooser = new JalviewFileChooser("html",
            "HTML files");
    jvFileChooser.setFileView(new JalviewFileView());

    jvFileChooser
            .setDialogTitle(MessageManager.getString("label.save_as_html"));
    jvFileChooser.setToolTipText(MessageManager.getString("action.save"));

    int fileChooserOpt = jvFileChooser.showSaveDialog(null);
    if (fileChooserOpt == JalviewFileChooser.APPROVE_OPTION)
    {
      jalview.bin.Cache.setProperty("LAST_DIRECTORY",
              jvFileChooser.getSelectedFile().getParent());
      selectedFile = jvFileChooser.getSelectedFile().getPath();
    }
    else
    {
      throw new NoFileSelectedException("No file was selected.");
    }
    return selectedFile;
  }

  protected void setProgressMessage(String message)
  {
    if (pIndicator != null && !isHeadless())
    {
      pIndicator.setProgressBar(message, pSessionId);
    }
    else
    {
      System.out.println(message);
    }
  }

  /**
   * Answers true if HTML export is invoke in headless mode or false otherwise
   * 
   * @return
   */
  protected boolean isHeadless()
  {
    return System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true");
  }

  /**
   * This method provides implementation of consistent behaviour which should
   * occur before a HTML file export. It MUST be called at the start of the
   * exportHTML() method implementation.
   */
  protected void exportStarted()
  {
    pSessionId = System.currentTimeMillis();
  }

  /**
   * This method provides implementation of consistent behaviour which should
   * occur after a HTML file export. It MUST be called at the end of the
   * exportHTML() method implementation.
   */
  protected void exportCompleted()
  {
    if (isLaunchInBrowserAfterExport() && !isHeadless())
    {
      try
      {
        jalview.util.BrowserLauncher
                .openURL("file:///" + getExportedFile());
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * if this answers true then BioJSON data will be embedded to the exported
   * HTML file otherwise it won't be embedded.
   * 
   * @return
   */
  public abstract boolean isEmbedData();

  /**
   * if this answers true then the generated HTML file is opened for viewing in
   * a browser after its generation otherwise it won't be opened in a browser
   * 
   * @return
   */
  public abstract boolean isLaunchInBrowserAfterExport();

  /**
   * handle to the generated HTML file
   * 
   * @return
   */
  public abstract File getExportedFile();

  /**
   * This is the main method to handle the HTML generation.
   * 
   * @param outputFile
   *          the file path of the generated HTML
   */
  public abstract void exportHTML(String outputFile);
}