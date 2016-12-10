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
package jalview.gui;

import jalview.datamodel.SequenceI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JPanel;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdCanvas extends JPanel
{
  protected AlignViewport av;

  protected boolean showScores = true;

  protected int maxIdLength = -1;

  protected String maxIdStr = null;

  BufferedImage image;

  Graphics2D gg;

  int imgHeight = 0;

  boolean fastPaint = false;

  List<SequenceI> searchResults;

  FontMetrics fm;

  AnnotationLabels labels = null;

  AnnotationPanel ap;

  private Font idfont;

  /**
   * Creates a new IdCanvas object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public IdCanvas(AlignViewport av)
  {
    setLayout(new BorderLayout());
    this.av = av;
    PaintRefresher.Register(this, av.getSequenceSetId());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param gg
   *          DOCUMENT ME!
   * @param hiddenRows
   *          true - check and display hidden row marker if need be
   * @param s
   *          DOCUMENT ME!
   * @param i
   *          DOCUMENT ME!
   * @param starty
   *          DOCUMENT ME!
   * @param ypos
   *          DOCUMENT ME!
   */
  public void drawIdString(Graphics2D gg, boolean hiddenRows, SequenceI s,
          int i, int starty, int ypos)
  {
    int xPos = 0;
    int panelWidth = getWidth();
    int charHeight = av.getCharHeight();

    if ((searchResults != null) && searchResults.contains(s))
    {
      gg.setColor(Color.black);
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      gg.setColor(Color.white);
    }
    else if ((av.getSelectionGroup() != null)
            && av.getSelectionGroup().getSequences(null).contains(s))
    {
      gg.setColor(Color.lightGray);
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      gg.setColor(Color.white);
    }
    else
    {
      gg.setColor(av.getSequenceColour(s));
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getWidth(),
              charHeight);
      gg.setColor(Color.black);
    }

    if (av.isRightAlignIds())
    {
      xPos = panelWidth
              - fm.stringWidth(s.getDisplayId(av.getShowJVSuffix())) - 4;
    }

    gg.drawString(s.getDisplayId(av.getShowJVSuffix()), xPos,
            (((i - starty + 1) * charHeight) + ypos) - (charHeight / 5));

    if (hiddenRows)
    {
      drawMarker(i, starty, ypos);
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param vertical
   *          DOCUMENT ME!
   */
  public void fastPaint(int vertical)
  {
    if (gg == null)
    {
      repaint();

      return;
    }

    gg.copyArea(0, 0, getWidth(), imgHeight, 0,
            -vertical * av.getCharHeight());

    int ss = av.startSeq;
    int es = av.endSeq;
    int transY = 0;

    if (vertical > 0) // scroll down
    {
      ss = es - vertical;

      if (ss < av.startSeq)
      { // ie scrolling too fast, more than a page at a time
        ss = av.startSeq;
      }
      else
      {
        transY = imgHeight - (vertical * av.getCharHeight());
      }
    }
    else if (vertical < 0)
    {
      es = ss - vertical;

      if (es > av.endSeq)
      {
        es = av.endSeq;
      }
    }

    gg.translate(0, transY);

    drawIds(ss, es);

    gg.translate(0, -transY);

    fastPaint = true;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    g.setColor(Color.white);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (fastPaint)
    {
      fastPaint = false;
      g.drawImage(image, 0, 0, this);

      return;
    }

    int oldHeight = imgHeight;

    imgHeight = getHeight();
    imgHeight -= (imgHeight % av.getCharHeight());

    if (imgHeight < 1)
    {
      return;
    }

    if (oldHeight != imgHeight || image.getWidth(this) != getWidth())
    {
      image = new BufferedImage(getWidth(), imgHeight,
              BufferedImage.TYPE_INT_RGB);
    }

    gg = (Graphics2D) image.getGraphics();

    // Fill in the background
    gg.setColor(Color.white);
    gg.fillRect(0, 0, getWidth(), imgHeight);

    drawIds(av.getStartSeq(), av.endSeq);

    g.drawImage(image, 0, 0, this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param starty
   *          DOCUMENT ME!
   * @param endy
   *          DOCUMENT ME!
   */
  void drawIds(int starty, int endy)
  {
    if (av.isSeqNameItalics())
    {
      setIdfont(new Font(av.getFont().getName(), Font.ITALIC, av.getFont()
              .getSize()));
    }
    else
    {
      setIdfont(av.getFont());
    }

    gg.setFont(getIdfont());
    fm = gg.getFontMetrics();

    if (av.antiAlias)
    {
      gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    Color currentColor = Color.white;
    Color currentTextColor = Color.black;

    final boolean doHiddenCheck = av.isDisplayReferenceSeq()
            || av.hasHiddenRows(), hiddenRows = av.hasHiddenRows();

    if (av.getWrapAlignment())
    {
      int maxwidth = av.getAlignment().getWidth();
      int alheight = av.getAlignment().getHeight();

      if (av.hasHiddenColumns())
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int annotationHeight = 0;

      if (av.isShowAnnotation())
      {
        if (ap == null)
        {
          ap = new AnnotationPanel(av);
        }

        annotationHeight = ap.adjustPanelHeight();
        if (labels == null)
        {
          labels = new AnnotationLabels(av);
        }
      }

      int hgap = av.getCharHeight();
      if (av.getScaleAboveWrapped())
      {
        hgap += av.getCharHeight();
      }

      int cHeight = alheight * av.getCharHeight() + hgap + annotationHeight;

      int rowSize = av.getEndRes() - av.getStartRes();

      // Draw the rest of the panels
      for (int ypos = hgap, row = av.startRes; (ypos <= getHeight())
              && (row < maxwidth); ypos += cHeight, row += rowSize)
      {
        for (int i = starty; i < alheight; i++)
        {
          SequenceI s = av.getAlignment().getSequenceAt(i);
          if (doHiddenCheck)
          {
            setHiddenFont(s);
          }
          else
          {
            gg.setFont(getIdfont());
          }

          drawIdString(gg, hiddenRows, s, i, 0, ypos);
        }

        if (labels != null && av.isShowAnnotation())
        {
          gg.translate(0, ypos + (alheight * av.getCharHeight()));
          labels.drawComponent(gg, getWidth());
          gg.translate(0, -ypos - (alheight * av.getCharHeight()));
        }
      }
    }
    else
    {
      // No need to hang on to labels if we're not wrapped
      labels = null;

      // Now draw the id strings
      int panelWidth = getWidth();
      int xPos = 0;

      SequenceI sequence;
      // Now draw the id strings
      for (int i = starty; i < endy; i++)
      {
        sequence = av.getAlignment().getSequenceAt(i);

        if (sequence == null)
        {
          continue;
        }

        if (doHiddenCheck)
        {
          setHiddenFont(sequence);
        }

        // Selected sequence colours
        if ((searchResults != null) && searchResults.contains(sequence))
        {
          currentColor = Color.black;
          currentTextColor = Color.white;
        }
        else if ((av.getSelectionGroup() != null)
                && av.getSelectionGroup().getSequences(null)
                        .contains(sequence))
        {
          currentColor = Color.lightGray;
          currentTextColor = Color.black;
        }
        else
        {
          currentColor = av.getSequenceColour(sequence);
          currentTextColor = Color.black;
        }

        gg.setColor(currentColor);

        gg.fillRect(0, (i - starty) * av.getCharHeight(), getWidth(),
                av.getCharHeight());

        gg.setColor(currentTextColor);

        String string = sequence.getDisplayId(av.getShowJVSuffix());

        if (av.isRightAlignIds())
        {
          xPos = panelWidth - fm.stringWidth(string) - 4;
        }

        gg.drawString(string, xPos,
                (((i - starty) * av.getCharHeight()) + av.getCharHeight())
                        - (av.getCharHeight() / 5));

        if (hiddenRows)
        {
          drawMarker(i, starty, 0);
        }

      }

    }
  }

  void drawMarker(int i, int starty, int yoffset)
  {

    SequenceI[] hseqs = av.getAlignment().getHiddenSequences().hiddenSequences;
    // Use this method here instead of calling hiddenSeq adjust
    // 3 times.
    int hSize = hseqs.length;

    int hiddenIndex = i;
    int lastIndex = i - 1;
    int nextIndex = i + 1;

    for (int j = 0; j < hSize; j++)
    {
      if (hseqs[j] != null)
      {
        if (j - 1 < hiddenIndex)
        {
          hiddenIndex++;
        }
        if (j - 1 < lastIndex)
        {
          lastIndex++;
        }
        if (j - 1 < nextIndex)
        {
          nextIndex++;
        }
      }
    }

    boolean below = (hiddenIndex > lastIndex + 1);
    boolean above = (nextIndex > hiddenIndex + 1);

    gg.setColor(Color.blue);
    if (below)
    {
      gg.fillPolygon(
              new int[] { getWidth() - av.getCharHeight(),
                  getWidth() - av.getCharHeight(), getWidth() },
              new int[] {
                  (i - starty) * av.getCharHeight() + yoffset,
                  (i - starty) * av.getCharHeight() + yoffset
                          + av.getCharHeight() / 4,
                  (i - starty) * av.getCharHeight() + yoffset }, 3);
    }
    if (above)
    {
      gg.fillPolygon(
              new int[] { getWidth() - av.getCharHeight(),
                  getWidth() - av.getCharHeight(), getWidth() },
              new int[] {
                  (i - starty + 1) * av.getCharHeight() + yoffset,
                  (i - starty + 1) * av.getCharHeight() + yoffset
                          - av.getCharHeight() / 4,
                  (i - starty + 1) * av.getCharHeight() + yoffset }, 3);

    }
  }

  void setHiddenFont(SequenceI seq)
  {
    Font bold = new Font(av.getFont().getName(), Font.BOLD, av.getFont()
            .getSize());

    if (av.isReferenceSeq(seq) || av.isHiddenRepSequence(seq))
    {
      gg.setFont(bold);
    }
    else
    {
      gg.setFont(getIdfont());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param list
   *          DOCUMENT ME!
   */
  public void setHighlighted(List<SequenceI> list)
  {
    searchResults = list;
    repaint();
  }

  public Font getIdfont()
  {
    return idfont;
  }

  public void setIdfont(Font idfont)
  {
    this.idfont = idfont;
  }
}
