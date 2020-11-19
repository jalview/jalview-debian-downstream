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
package jalview.util;

import jalview.bin.Jalview;
import jalview.gui.EPSOptions;
import jalview.gui.IProgressIndicator;
import jalview.gui.SVGOptions;
import jalview.io.JalviewFileChooser;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

import javax.imageio.ImageIO;

import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGHints;
import org.jibble.epsgraphics.EpsGraphics2D;

public class ImageMaker
{
  public static final String SVG_DESCRIPTION = "Scalable Vector Graphics";

  public static final String SVG_EXTENSION = "svg";

  public static final String EPS_DESCRIPTION = "Encapsulated Postscript";

  public static final String EPS_EXTENSION = "eps";

  public static final String PNG_EXTENSION = "png";

  public static final String PNG_DESCRIPTION = "Portable  network graphics";

  public static final String HTML_EXTENSION = "html";

  public static final String HTML_DESCRIPTION = "Hypertext Markup Language";

  EpsGraphics2D pg;

  SVGGraphics2D g2;

  Graphics graphics;

  FileOutputStream out;

  BufferedImage bi;

  TYPE type;

  private IProgressIndicator pIndicator;

  private long pSessionId;

  private boolean headless;

  public enum TYPE
  {
    EPS("EPS", MessageManager.getString("label.eps_file"), EPS_EXTENSION,
            EPS_DESCRIPTION),
    PNG("PNG", MessageManager.getString("label.png_image"),
            PNG_EXTENSION, PNG_DESCRIPTION),
    SVG("SVG", "SVG", SVG_EXTENSION, SVG_DESCRIPTION);

    private String name;

    private String label;

    private String extension;

    private String description;

    TYPE(String name, String label, String ext, String desc)
    {
      this.name = name;
      this.label = label;
      this.extension = ext;
      this.description = desc;
    }

    public String getName()
    {
      return name;
    }

    public JalviewFileChooser getFileChooser()
    {
      return new JalviewFileChooser(extension, description);
    }

    public String getLabel()
    {
      return label;
    }

  }

  public ImageMaker(Component parent, TYPE type, String title, int width,
          int height, File file, String fileTitle,
          IProgressIndicator pIndicator, long pSessionId, boolean headless)
  {
    this.pIndicator = pIndicator;
    this.type = type;
    this.pSessionId = pSessionId;
    this.headless = headless;
    if (file == null)
    {
      setProgressMessage(MessageManager.formatMessage(
              "status.waiting_for_user_to_select_output_file", type.name));
      JalviewFileChooser chooser;
      chooser = type.getFileChooser();
      chooser.setFileView(new jalview.io.JalviewFileView());
      chooser.setDialogTitle(title);
      chooser.setToolTipText(MessageManager.getString("action.save"));
      int value = chooser.showSaveDialog(parent);

      if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
      {
        jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                chooser.getSelectedFile().getParent());
        file = chooser.getSelectedFile();
      }
      else
      {
        setProgressMessage(MessageManager.formatMessage(
                "status.cancelled_image_export_operation", type.name));
      }
    }

    if (file != null)
    {
      try
      {
        out = new FileOutputStream(file);
        setProgressMessage(null);
        setProgressMessage(MessageManager.formatMessage(
                "status.exporting_alignment_as_x_file", type.getName()));
        if (type == TYPE.SVG)
        {
          setupSVG(width, height, fileTitle);
        }
        else if (type == TYPE.EPS)
        {
          setupEPS(width, height, fileTitle);
        }
        else if (type == TYPE.PNG)
        {
          setupPNG(width, height);
        }

      } catch (Exception ex)
      {
        System.out.println("Error creating " + type.getName() + " file.");

        setProgressMessage(MessageManager
                .formatMessage("info.error_creating_file", type.getName()));
      }
    }
  }

  public Graphics getGraphics()
  {
    return graphics;
  }

  public void writeImage()
  {
    try
    {
      switch (type)
      {
      case EPS:
        pg.flush();
        pg.close();
        break;
      case SVG:
        String svgData = ((SVGGraphics2D) getGraphics()).getSVGDocument();
        out.write(svgData.getBytes());
        out.flush();
        out.close();
        break;
      case PNG:
        ImageIO.write(bi, PNG_EXTENSION, out);
        out.flush();
        out.close();
        break;
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  void setupEPS(int width, int height, String title)
  {
    boolean accurateText = true;

    String renderStyle = jalview.bin.Cache.getDefault("EPS_RENDERING",
            "Prompt each time");

    // If we need to prompt, and if the GUI is visible then
    // Prompt for EPS rendering style
    if (renderStyle.equalsIgnoreCase("Prompt each time")
            && !(System.getProperty("java.awt.headless") != null && System
                    .getProperty("java.awt.headless").equals("true")))
    {
      EPSOptions eps = new EPSOptions();
      renderStyle = eps.getValue();

      if (renderStyle == null || eps.cancelled)
      {
        setProgressMessage(MessageManager.formatMessage(
                "status.cancelled_image_export_operation", "EPS"));
        return;
      }
    }

    if (renderStyle.equalsIgnoreCase("text"))
    {
      accurateText = false;
    }

    try
    {
      pg = new EpsGraphics2D(title, out, 0, 0, width, height);
      Graphics2D ig2 = pg;
      ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

      pg.setAccurateTextMode(accurateText);

      graphics = pg;
      setProgressMessage(MessageManager
              .formatMessage("status.export_complete", type.getName()));
    } catch (Exception ex)
    {
    }
  }

  void setupPNG(int width, int height)
  {
    bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    graphics = bi.getGraphics();
    Graphics2D ig2 = (Graphics2D) graphics;
    ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    setProgressMessage(MessageManager
            .formatMessage("status.export_complete", type.getName()));

  }

  void setupSVG(int width, int height, String title)
  {

    g2 = new SVGGraphics2D(width, height);
    Graphics2D ig2 = g2;

    String renderStyle = jalview.bin.Cache.getDefault("SVG_RENDERING",
            "Prompt each time");

    // If we need to prompt, and if the GUI is visible then
    // Prompt for EPS rendering style
    if (renderStyle.equalsIgnoreCase("Prompt each time")
            && !(System.getProperty("java.awt.headless") != null && System
                    .getProperty("java.awt.headless").equals("true")))
    {
      SVGOptions svgOption = new SVGOptions();
      renderStyle = svgOption.getValue();

      if (renderStyle == null || svgOption.cancelled)
      {
        setProgressMessage(MessageManager.formatMessage(
                "status.cancelled_image_export_operation", "SVG"));
        return;
      }
    }

    if (renderStyle.equalsIgnoreCase("Lineart"))
    {
      ig2.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
              SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
    }

    setProgressMessage(MessageManager
            .formatMessage("status.export_complete", type.getName()));
    graphics = g2;
  }

  static JalviewFileChooser getPNGChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new JalviewFileChooser(PNG_EXTENSION, PNG_DESCRIPTION);
  }

  static JalviewFileChooser getEPSChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new JalviewFileChooser(EPS_EXTENSION, EPS_DESCRIPTION);
  }

  private void setProgressMessage(String message)
  {
    if (pIndicator != null && !headless)
    {
      pIndicator.setProgressBar(message, pSessionId);
    }
  }

  static JalviewFileChooser getSVGChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new JalviewFileChooser(SVG_EXTENSION, SVG_DESCRIPTION);
  }
}
