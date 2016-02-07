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
package jalview.util;

import jalview.bin.Jalview;
import jalview.gui.EPSOptions;
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
  EpsGraphics2D pg;

  SVGGraphics2D g2;

  Graphics graphics;

  FileOutputStream out;

  BufferedImage bi;

  TYPE type;

  public enum TYPE
  {
    EPS("EPS", MessageManager.getString("label.eps_file"), getEPSChooser()), PNG(
            "PNG", MessageManager.getString("label.png_image"),
            getPNGChooser()), SVG("SVG", "SVG", getSVGChooser());

    private JalviewFileChooser chooser;

    private String name;

    private String label;

    TYPE(String name, String label, JalviewFileChooser chooser)
    {
      this.name = name;
      this.label = label;
      this.chooser = chooser;
    }

    public String getName()
    {
      return name;
    }

    public JalviewFileChooser getChooser()
    {
      return chooser;
    }

    public String getLabel()
    {
      return label;
    }

  }

  public ImageMaker(Component parent, TYPE type, String title, int width,
          int height, File file, String fileTitle)
  {
    this.type = type;

    if (file == null)
    {
      JalviewFileChooser chooser;
      chooser = type.getChooser();
      chooser.setFileView(new jalview.io.JalviewFileView());
      chooser.setDialogTitle(title);
      chooser.setToolTipText(MessageManager.getString("action.save"));
      int value = chooser.showSaveDialog(parent);

      if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
      {
        jalview.bin.Cache.setProperty("LAST_DIRECTORY", chooser
                .getSelectedFile().getParent());
        file = chooser.getSelectedFile();
      }
    }

    if (file != null)
    {
      try
      {
        out = new FileOutputStream(file);
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
        ImageIO.write(bi, "png", out);
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
        return;
      }
    }

    if (renderStyle.equalsIgnoreCase("lineart"))
    {
      ig2.setRenderingHint(SVGHints.KEY_DRAW_STRING_TYPE,
              SVGHints.VALUE_DRAW_STRING_TYPE_VECTOR);
    }

    graphics = g2;
  }

  static JalviewFileChooser getPNGChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "png" },
            new String[] { "Portable network graphics" },
            "Portable network graphics");
  }

  static JalviewFileChooser getEPSChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "eps" },
            new String[] { "Encapsulated Postscript" },
            "Encapsulated Postscript");
  }

  static JalviewFileChooser getSVGChooser()
  {
    if (Jalview.isHeadlessMode())
    {
      return null;
    }
    return new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "svg" },
            new String[] { "Scalable Vector Graphics" },
            "Scalable Vector Graphics");
  }
}
