/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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
package jalview.appletgui;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class TitledPanel extends Panel
{

  private String title;

  private Insets insets = new Insets(10, 10, 10, 10);

  public TitledPanel()
  {
    this("");
  }

  public TitledPanel(String title)
  {
    this.setTitle(title);
  }

  public Insets getInsets()
  {
    return insets;
  }

  public void paint(Graphics g)
  {
    super.paint(g);
    g.setColor(getForeground());
    g.drawRect(5, 5, getWidth() - 10, getHeight() - 10);
    int width = g.getFontMetrics().stringWidth(getTitle());
    g.setColor(getBackground());
    g.fillRect(10, 0, width, 10);
    g.setColor(getForeground());
    g.drawString(getTitle(), 10, 10);
  }

  public static void main(String[] args)
  {
    Frame f = new Frame("TitledPanel Tester");

    TitledPanel p = new TitledPanel("Title of Panel");
    p.add(new Label("Label 1"));
    p.add(new Label("Label 2"));
    p.add(new Label("Label 3"));
    f.add(p);

    f.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });
    f.setBounds(300, 300, 300, 300);
    f.setVisible(true);
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }
}
