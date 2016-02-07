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
package jalview.gui;

import jalview.bin.Cache;
import jalview.jbgui.GFontChooser;
import jalview.util.MessageManager;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FontChooser extends GFontChooser
{
  AlignmentPanel ap;

  TreePanel tp;

  /*
   * The font on opening the dialog (to be restored on Cancel)
   */
  Font oldFont;

  boolean oldProteinScale;

  boolean init = true;

  JInternalFrame frame;

  /*
   * The last font settings selected in the dialog
   */
  private Font lastSelected = null;

  private boolean lastSelMono = false;

  /**
   * Creates a new FontChooser object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public FontChooser(TreePanel tp)
  {
    this.tp = tp;
    ap = tp.treeCanvas.ap;
    oldFont = tp.getTreeFont();
    defaultButton.setVisible(false);
    smoothFont.setEnabled(false);
    init();
  }

  /**
   * Creates a new FontChooser object.
   * 
   * @param ap
   *          DOCUMENT ME!
   */
  public FontChooser(AlignmentPanel ap)
  {
    oldFont = ap.av.getFont();
    oldProteinScale = ap.av.isScaleProteinAsCdna();

    this.ap = ap;
    init();
  }

  void init()
  {
    frame = new JInternalFrame();
    frame.setContentPane(this);

    smoothFont.setSelected(ap.av.antiAlias);

    /*
     * Enable 'scale protein as cDNA' in a SplitFrame view. The selection is
     * stored in the ViewStyle of both dna and protein Viewport
     */
    scaleAsCdna.setEnabled(false);
    if (ap.av.getCodingComplement() != null)
    {
      scaleAsCdna.setEnabled(true);
      scaleAsCdna.setVisible(true);
      scaleAsCdna.setSelected(ap.av.isScaleProteinAsCdna());
    }

    if (tp != null)
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("action.change_font_tree_panel"),
              400, 200, false);
    }
    else
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("action.change_font"), 380, 200,
              false);
    }

    frame.setLayer(JLayeredPane.PALETTE_LAYER);

    String[] fonts = java.awt.GraphicsEnvironment
            .getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

    for (int i = 0; i < fonts.length; i++)
    {
      fontName.addItem(fonts[i]);
    }

    for (int i = 1; i < 51; i++)
    {
      fontSize.addItem(i);
    }

    fontStyle.addItem("plain");
    fontStyle.addItem("bold");
    fontStyle.addItem("italic");

    fontName.setSelectedItem(oldFont.getName());
    fontSize.setSelectedItem(oldFont.getSize());
    fontStyle.setSelectedIndex(oldFont.getStyle());

    FontMetrics fm = getGraphics().getFontMetrics(oldFont);
    monospaced.setSelected(fm.getStringBounds("M", getGraphics())
            .getWidth() == fm.getStringBounds("|", getGraphics())
            .getWidth());

    init = false;
  }

  public void smoothFont_actionPerformed(ActionEvent e)
  {
    ap.av.antiAlias = smoothFont.isSelected();
    ap.getAnnotationPanel().image = null;
    ap.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void ok_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }

    if (ap != null)
    {
      if (ap.getOverviewPanel() != null)
      {
        ap.getOverviewPanel().updateOverviewImage();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void cancel_actionPerformed(ActionEvent e)
  {
    if (ap != null)
    {
      ap.av.setFont(oldFont, true);
      ap.av.setScaleProteinAsCdna(oldProteinScale);
      ap.paintAlignment(true);
      if (scaleAsCdna.isEnabled())
      {
        ap.av.setScaleProteinAsCdna(oldProteinScale);
        ap.av.getCodingComplement().setScaleProteinAsCdna(oldProteinScale);
      }
    }
    else if (tp != null)
    {
      tp.setTreeFont(oldFont);
    }
    fontName.setSelectedItem(oldFont.getName());
    fontSize.setSelectedItem(oldFont.getSize());
    fontStyle.setSelectedIndex(oldFont.getStyle());

    try
    {
      frame.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   */
  void changeFont()
  {
    if (lastSelected == null)
    {
      // initialise with original font
      lastSelected = oldFont;
      FontMetrics fm = getGraphics().getFontMetrics(oldFont);
      double mw = fm.getStringBounds("M", getGraphics()).getWidth();
      double iw = fm.getStringBounds("I", getGraphics()).getWidth();
      lastSelMono = (mw == iw); // == on double - flaky?
    }

    Font newFont = new Font(fontName.getSelectedItem().toString(),
            fontStyle.getSelectedIndex(),
            (Integer) fontSize.getSelectedItem());
    FontMetrics fm = getGraphics().getFontMetrics(newFont);
    double mw = fm.getStringBounds("M", getGraphics()).getWidth();
    final Rectangle2D iBounds = fm.getStringBounds("I", getGraphics());
    double iw = iBounds.getWidth();
    if (mw < 1 || iw < 1)
    {
      final String messageKey = iBounds.getHeight() < 1 ? "label.font_doesnt_have_letters_defined"
              : "label.font_too_small";
      JOptionPane.showInternalMessageDialog(this,
              MessageManager.getString(messageKey),
              MessageManager.getString("label.invalid_font"),
              JOptionPane.WARNING_MESSAGE);
      /*
       * Restore the changed value - note this will reinvoke this method via the
       * ActionListener, but now validation should pass
       */
      if (lastSelected.getSize() != (Integer) fontSize.getSelectedItem()) // autoboxing
      {
        fontSize.setSelectedItem(lastSelected.getSize());
      }
      if (!lastSelected.getName().equals(
              fontName.getSelectedItem().toString()))
      {
        fontName.setSelectedItem(lastSelected.getName());
      }
      if (lastSelected.getStyle() != fontStyle.getSelectedIndex())
      {
        fontStyle.setSelectedIndex(lastSelected.getStyle());
      }
      if (lastSelMono != monospaced.isSelected())
      {
        monospaced.setSelected(lastSelMono);
      }
      return;
    }
    if (tp != null)
    {
      tp.setTreeFont(newFont);
    }
    else if (ap != null)
    {
      ap.av.setFont(newFont, true);
      ap.fontChanged();
    }

    monospaced.setSelected(mw == iw);

    /*
     * Remember latest valid selection, so it can be restored if followed by an
     * invalid one
     */
    lastSelected = newFont;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void fontName_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void fontSize_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void fontStyle_actionPerformed(ActionEvent e)
  {
    if (init)
    {
      return;
    }

    changeFont();
  }

  /**
   * Make selected settings the defaults by storing them (via Cache class) in
   * the .jalview_properties file (the file is only written when Jalview exits)
   * 
   * @param e
   */
  public void defaultButton_actionPerformed(ActionEvent e)
  {
    Cache.setProperty("FONT_NAME", fontName.getSelectedItem().toString());
    Cache.setProperty("FONT_STYLE", fontStyle.getSelectedIndex() + "");
    Cache.setProperty("FONT_SIZE", fontSize.getSelectedItem().toString());
    Cache.setProperty("ANTI_ALIAS",
            Boolean.toString(smoothFont.isSelected()));
    Cache.setProperty(Preferences.SCALE_PROTEIN_TO_CDNA,
            Boolean.toString(scaleAsCdna.isSelected()));
  }

  /**
   * Turn on/off scaling of protein characters to 3 times the width of cDNA
   * characters
   */
  @Override
  protected void scaleAsCdna_actionPerformed(ActionEvent e)
  {
    ap.av.setScaleProteinAsCdna(scaleAsCdna.isSelected());
    ap.av.getCodingComplement().setScaleProteinAsCdna(
            scaleAsCdna.isSelected());
    final SplitFrame splitFrame = (SplitFrame) ap.alignFrame
            .getSplitViewContainer();
    splitFrame.adjustLayout();
    splitFrame.repaint();
    // ap.paintAlignment(true);
    // TODO would like to repaint
  }
}
