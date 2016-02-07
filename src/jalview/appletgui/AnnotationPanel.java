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
package jalview.appletgui;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.renderer.AnnotationRenderer;
import jalview.renderer.AwtRenderPanelI;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class AnnotationPanel extends Panel implements AwtRenderPanelI,
        AdjustmentListener, ActionListener, MouseListener,
        MouseMotionListener
{
  AlignViewport av;

  AlignmentPanel ap;

  int activeRow = -1;

  final String HELIX = "Helix";

  final String SHEET = "Sheet";

  /**
   * For RNA secondary structure "stems" aka helices
   */
  final String STEM = "RNA Helix";

  final String LABEL = "Label";

  final String REMOVE = "Remove Annotation";

  final String COLOUR = "Colour";

  final Color HELIX_COLOUR = Color.red.darker();

  final Color SHEET_COLOUR = Color.green.darker().darker();

  Image image;

  Graphics gg;

  FontMetrics fm;

  int imgWidth = 0;

  boolean fastPaint = false;

  // Used For mouse Dragging and resizing graphs
  int graphStretch = -1;

  int graphStretchY = -1;

  boolean mouseDragging = false;

  public static int GRAPH_HEIGHT = 40;

  boolean MAC = false;

  public final AnnotationRenderer renderer;

  public AnnotationPanel(AlignmentPanel ap)
  {
    MAC = new jalview.util.Platform().isAMac();
    this.ap = ap;
    av = ap.av;
    setLayout(null);
    int height = adjustPanelHeight();
    ap.apvscroll.setValues(0, getSize().height, 0, height);

    addMouseMotionListener(this);

    addMouseListener(this);

    // ap.annotationScroller.getVAdjustable().addAdjustmentListener( this );
    renderer = new AnnotationRenderer();
  }

  public AnnotationPanel(AlignViewport av)
  {
    this.av = av;
    renderer = new AnnotationRenderer();
  }

  @Override
  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void actionPerformed(ActionEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }
    Annotation[] anot = aa[activeRow].annotations;

    if (anot.length < av.getColumnSelection().getMax())
    {
      Annotation[] temp = new Annotation[av.getColumnSelection().getMax() + 2];
      System.arraycopy(anot, 0, temp, 0, anot.length);
      anot = temp;
      aa[activeRow].annotations = anot;
    }

    String label = "";
    if (av.getColumnSelection() != null
            && av.getColumnSelection().size() > 0
            && anot[av.getColumnSelection().getMin()] != null)
    {
      label = anot[av.getColumnSelection().getMin()].displayCharacter;
    }

    if (evt.getActionCommand().equals(REMOVE))
    {
      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        anot[av.getColumnSelection().columnAt(i)] = null;
      }
    }
    else if (evt.getActionCommand().equals(LABEL))
    {
      label = enterLabel(label, "Enter Label");

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
      }

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", ' ', 0);
        }

        anot[index].displayCharacter = label;
      }
    }
    else if (evt.getActionCommand().equals(COLOUR))
    {
      UserDefinedColours udc = new UserDefinedColours(this, Color.black,
              ap.alignFrame);

      Color col = udc.getColor();

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation("", "", ' ', 0);
        }

        anot[index].colour = col;
      }
    }
    else
    // HELIX OR SHEET
    {
      char type = 0;
      String symbol = "\u03B1";

      if (evt.getActionCommand().equals(HELIX))
      {
        type = 'H';
      }
      else if (evt.getActionCommand().equals(SHEET))
      {
        type = 'E';
        symbol = "\u03B2";
      }

      // Added by LML to color stems
      else if (evt.getActionCommand().equals(STEM))
      {
        type = 'S';
        symbol = "\u03C3";
      }

      if (!aa[activeRow].hasIcons)
      {
        aa[activeRow].hasIcons = true;
      }

      label = enterLabel(symbol, "Enter Label");

      if (label == null)
      {
        return;
      }

      if ((label.length() > 0) && !aa[activeRow].hasText)
      {
        aa[activeRow].hasText = true;
        if (evt.getActionCommand().equals(STEM))
        {
          aa[activeRow].showAllColLabels = true;
        }
      }

      for (int i = 0; i < av.getColumnSelection().size(); i++)
      {
        int index = av.getColumnSelection().columnAt(i);

        if (!av.getColumnSelection().isVisible(index))
        {
          continue;
        }

        if (anot[index] == null)
        {
          anot[index] = new Annotation(label, "", type, 0);
        }

        anot[index].secondaryStructure = type != 'S' ? type : label
                .length() == 0 ? ' ' : label.charAt(0);
        anot[index].displayCharacter = label;
      }
    }

    av.getAlignment().validateAnnotation(aa[activeRow]);

    ap.alignmentChanged();
    adjustPanelHeight();
    repaint();

    return;
  }

  String enterLabel(String text, String label)
  {
    EditNameDialog dialog = new EditNameDialog(text, null, label, null,
            ap.alignFrame, "Enter Label", 400, 200, true);

    if (dialog.accept)
    {
      return dialog.getName();
    }
    else
    {
      return null;
    }
  }

  @Override
  public void mousePressed(MouseEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }

    int height = -scrollOffset;
    activeRow = -1;

    for (int i = 0; i < aa.length; i++)
    {
      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (evt.getY() < height)
      {
        if (aa[i].editable)
        {
          activeRow = i;
        }
        else if (aa[i].graph > 0)
        {
          // Stretch Graph
          graphStretch = i;
          graphStretchY = evt.getY();
        }

        break;
      }
    }

    if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK
            && activeRow != -1)
    {
      if (av.getColumnSelection() == null)
      {
        return;
      }

      PopupMenu pop = new PopupMenu(
              MessageManager.getString("label.structure_type"));
      MenuItem item;
      /*
       * Just display the needed structure options
       */
      if (av.getAlignment().isNucleotide() == true)
      {
        item = new MenuItem(STEM);
        item.addActionListener(this);
        pop.add(item);
      }
      else
      {
        item = new MenuItem(HELIX);
        item.addActionListener(this);
        pop.add(item);
        item = new MenuItem(SHEET);
        item.addActionListener(this);
        pop.add(item);
      }
      item = new MenuItem(LABEL);
      item.addActionListener(this);
      pop.add(item);
      item = new MenuItem(COLOUR);
      item.addActionListener(this);
      pop.add(item);
      item = new MenuItem(REMOVE);
      item.addActionListener(this);
      pop.add(item);
      ap.alignFrame.add(pop);
      pop.show(this, evt.getX(), evt.getY());

      return;
    }

    ap.scalePanel.mousePressed(evt);
  }

  @Override
  public void mouseReleased(MouseEvent evt)
  {
    graphStretch = -1;
    graphStretchY = -1;
    mouseDragging = false;
    if (needValidating)
    {
      ap.validate();
      needValidating = false;
    }
    ap.scalePanel.mouseReleased(evt);
  }

  @Override
  public void mouseClicked(MouseEvent evt)
  {
  }

  boolean needValidating = false;

  @Override
  public void mouseDragged(MouseEvent evt)
  {
    if (graphStretch > -1)
    {
      av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight += graphStretchY
              - evt.getY();
      if (av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight < 0)
      {
        av.getAlignment().getAlignmentAnnotation()[graphStretch].graphHeight = 0;
      }
      graphStretchY = evt.getY();
      av.calcPanelHeight();
      needValidating = true;
      ap.paintAlignment(true);
    }
    else
    {
      ap.scalePanel.mouseDragged(evt);
    }
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
    if (aa == null)
    {
      return;
    }

    int row = -1;
    int height = -scrollOffset;
    for (int i = 0; i < aa.length; i++)
    {

      if (aa[i].visible)
      {
        height += aa[i].height;
      }

      if (evt.getY() < height)
      {
        row = i;
        break;
      }
    }

    int res = evt.getX() / av.getCharWidth() + av.getStartRes();

    if (av.hasHiddenColumns())
    {
      res = av.getColumnSelection().adjustForHiddenColumns(res);
    }

    if (row > -1 && res < aa[row].annotations.length
            && aa[row].annotations[res] != null)
    {
      StringBuffer text = new StringBuffer("Sequence position " + (res + 1));
      if (aa[row].annotations[res].description != null)
      {
        text.append("  " + aa[row].annotations[res].description);
      }
      ap.alignFrame.statusBar.setText(text.toString());
    }
  }

  @Override
  public void mouseEntered(MouseEvent evt)
  {
    ap.scalePanel.mouseEntered(evt);
  }

  @Override
  public void mouseExited(MouseEvent evt)
  {
    ap.scalePanel.mouseExited(evt);
  }

  public int adjustPanelHeight()
  {
    return adjustPanelHeight(true);
  }

  public int adjustPanelHeight(boolean repaint)
  {
    int height = av.calcPanelHeight();
    this.setSize(new Dimension(getSize().width, height));
    if (repaint)
    {
      repaint();
    }
    return height;
  }

  /**
   * calculate the height for visible annotation, revalidating bounds where
   * necessary ABSTRACT GUI METHOD
   * 
   * @return total height of annotation
   */

  public void addEditableColumn(int i)
  {
    if (activeRow == -1)
    {
      AlignmentAnnotation[] aa = av.getAlignment().getAlignmentAnnotation();
      if (aa == null)
      {
        return;
      }

      for (int j = 0; j < aa.length; j++)
      {
        if (aa[j].editable)
        {
          activeRow = j;
          break;
        }
      }
    }
  }

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    Dimension d = getSize();
    imgWidth = d.width;
    // (av.endRes - av.startRes + 1) * av.charWidth;
    if (imgWidth < 1 || d.height < 1)
    {
      return;
    }
    if (image == null || imgWidth != image.getWidth(this)
            || d.height != image.getHeight(this))
    {
      image = createImage(imgWidth, d.height);
      gg = image.getGraphics();
      gg.setFont(av.getFont());
      fm = gg.getFontMetrics();
      fastPaint = false;
    }

    if (fastPaint)
    {
      g.drawImage(image, 0, 0, this);
      fastPaint = false;
      return;
    }

    gg.setColor(Color.white);
    gg.fillRect(0, 0, getSize().width, getSize().height);
    drawComponent(gg, av.startRes, av.endRes + 1);

    g.drawImage(image, 0, 0, this);
  }

  public void fastPaint(int horizontal)
  {
    if (horizontal == 0
            || av.getAlignment().getAlignmentAnnotation() == null
            || av.getAlignment().getAlignmentAnnotation().length < 1)
    {
      repaint();
      return;
    }

    gg.copyArea(0, 0, imgWidth, getSize().height,
            -horizontal * av.getCharWidth(), 0);
    int sr = av.startRes, er = av.endRes + 1, transX = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      transX = (er - sr - horizontal) * av.getCharWidth();
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal;
    }

    gg.translate(transX, 0);

    drawComponent(gg, sr, er);

    gg.translate(-transX, 0);

    fastPaint = true;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startRes
   *          DOCUMENT ME!
   * @param endRes
   *          DOCUMENT ME!
   */
  public void drawComponent(Graphics g, int startRes, int endRes)
  {
    Font ofont = av.getFont();
    g.setFont(ofont);

    g.setColor(Color.white);
    g.fillRect(0, 0, (endRes - startRes) * av.getCharWidth(),
            getSize().height);

    if (fm == null)
    {
      fm = g.getFontMetrics();
    }

    if ((av.getAlignment().getAlignmentAnnotation() == null)
            || (av.getAlignment().getAlignmentAnnotation().length < 1))
    {
      g.setColor(Color.white);
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(Color.black);
      if (av.validCharWidth)
      {
        g.drawString(MessageManager
                .getString("label.alignment_has_no_annotations"), 20, 15);
      }

      return;
    }
    g.translate(0, -scrollOffset);
    renderer.drawComponent(this, av, g, activeRow, startRes, endRes);
    g.translate(0, +scrollOffset);
  }

  int scrollOffset = 0;

  public void setScrollOffset(int value, boolean repaint)
  {
    scrollOffset = value;
    if (repaint)
    {
      repaint();
    }
  }

  @Override
  public FontMetrics getFontMetrics()
  {
    return fm;
  }

  @Override
  public Image getFadedImage()
  {
    return image;
  }

  @Override
  public int getFadedImageWidth()
  {
    return imgWidth;
  }

  private int[] bounds = new int[2];

  @Override
  public int[] getVisibleVRange()
  {
    if (ap != null && ap.alabels != null)
    {
      int sOffset = -ap.alabels.scrollOffset;
      int visHeight = sOffset + ap.annotationPanelHolder.getHeight();
      bounds[0] = sOffset;
      bounds[1] = visHeight;
      return bounds;
    }
    else
    {
      return null;
    }
  }
}
