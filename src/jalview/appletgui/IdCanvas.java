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

import jalview.datamodel.SequenceI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Panel;
import java.util.List;

public class IdCanvas extends Panel
{
  protected AlignViewport av;

  protected boolean showScores = true;

  protected int maxIdLength = -1;

  protected String maxIdStr = null;

  Image image;

  Graphics gg;

  int imgHeight = 0;

  boolean fastPaint = false;

  List<SequenceI> searchResults;

  public IdCanvas(AlignViewport av)
  {
    setLayout(null);
    this.av = av;
    PaintRefresher.Register(this, av.getSequenceSetId());
  }

  public void drawIdString(Graphics gg, boolean hiddenRows, SequenceI s,
          int i, int starty, int ypos)
  {
    int charHeight = av.getCharHeight();

    if (searchResults != null && searchResults.contains(s))
    {
      gg.setColor(Color.black);
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getSize().width,
              charHeight);
      gg.setColor(Color.white);
    }
    else if (av.getSelectionGroup() != null
            && av.getSelectionGroup().getSequences(null).contains(s))
    {
      gg.setColor(Color.lightGray);
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getSize().width,
              charHeight);
      gg.setColor(Color.white);
    }
    else
    {
      gg.setColor(av.getSequenceColour(s));
      gg.fillRect(0, ((i - starty) * charHeight) + ypos, getSize().width,
              charHeight);
      gg.setColor(Color.black);
    }

    gg.drawString(s.getDisplayId(av.getShowJVSuffix()), 0,
            ((i - starty) * charHeight) + ypos + charHeight
                    - (charHeight / 5));

    if (hiddenRows)
    {
      drawMarker(i, starty, ypos);
    }

  }

  public void fastPaint(int vertical)
  {
    if (gg == null)
    {
      repaint();
      return;
    }

    gg.copyArea(0, 0, getSize().width, imgHeight, 0,
            -vertical * av.getCharHeight());

    int ss = av.startSeq, es = av.endSeq, transY = 0;
    if (vertical > 0) // scroll down
    {
      ss = es - vertical;
      if (ss < av.startSeq) // ie scrolling too fast, more than a page at a time
      {
        ss = av.startSeq;
      }
      else
      {
        transY = imgHeight - vertical * av.getCharHeight();
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

  @Override
  public void update(Graphics g)
  {
    paint(g);
  }

  @Override
  public void paint(Graphics g)
  {
    if (getSize().height < 0 || getSize().width < 0)
    {
      return;
    }
    if (fastPaint)
    {
      fastPaint = false;
      g.drawImage(image, 0, 0, this);
      return;
    }

    imgHeight = getSize().height;
    imgHeight -= imgHeight % av.getCharHeight();

    if (imgHeight < 1)
    {
      return;
    }

    if (image == null || imgHeight != image.getHeight(this))
    {
      image = createImage(getSize().width, imgHeight);
      gg = image.getGraphics();
      gg.setFont(av.getFont());
    }

    // Fill in the background
    gg.setColor(Color.white);
    Font italic = new Font(av.getFont().getName(), Font.ITALIC, av
            .getFont().getSize());
    gg.setFont(italic);

    gg.fillRect(0, 0, getSize().width, getSize().height);
    drawIds(av.startSeq, av.endSeq);
    g.drawImage(image, 0, 0, this);
  }

  /**
   * local copy of av.getCharHeight set at top of drawIds
   */
  private int avcharHeight;

  void drawIds(int starty, int endy)
  {
    // hardwired italic IDs in applet currently
    Font italic = new Font(av.getFont().getName(), Font.ITALIC, av
            .getFont().getSize());
    // temp variable for speed
    avcharHeight = av.getCharHeight();

    gg.setFont(italic);

    Color currentColor = Color.white;
    Color currentTextColor = Color.black;

    final boolean doHiddenCheck = av.isDisplayReferenceSeq()
            || av.hasHiddenRows(), hiddenRows = av.hasHiddenRows()
            && av.getShowHiddenMarkers();

    if (av.getWrapAlignment())
    {
      int maxwidth = av.getAlignment().getWidth();
      int alheight = av.getAlignment().getHeight();

      if (av.hasHiddenColumns())
      {
        maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
      }

      int annotationHeight = 0;
      AnnotationLabels labels = null;

      if (av.isShowAnnotation())
      {
        AnnotationPanel ap = new AnnotationPanel(av);
        annotationHeight = ap.adjustPanelHeight();
        labels = new AnnotationLabels(av);
      }
      int hgap = avcharHeight;
      if (av.getScaleAboveWrapped())
      {
        hgap += avcharHeight;
      }

      int cHeight = alheight * avcharHeight + hgap + annotationHeight;

      int rowSize = av.getEndRes() - av.getStartRes();
      // Draw the rest of the panels
      for (int ypos = hgap, row = av.startRes; (ypos <= getSize().height)
              && (row < maxwidth); ypos += cHeight, row += rowSize)
      {
        for (int i = starty; i < alheight; i++)
        {

          SequenceI s = av.getAlignment().getSequenceAt(i);
          gg.setFont(italic);
          if (doHiddenCheck)
          {
            setHiddenFont(s);
          }
          drawIdString(gg, hiddenRows, s, i, 0, ypos);
        }

        if (labels != null)
        {
          gg.translate(0, ypos + (alheight * avcharHeight));
          labels.drawComponent(gg, getSize().width);
          gg.translate(0, -ypos - (alheight * avcharHeight));
        }

      }
    }
    else
    {
      // Now draw the id strings
      SequenceI seq;
      for (int i = starty; i < endy; i++)
      {

        seq = av.getAlignment().getSequenceAt(i);
        if (seq == null)
        {
          continue;
        }
        gg.setFont(italic);
        // boolean isrep=false;
        if (doHiddenCheck)
        {
          // isrep =
          setHiddenFont(seq);
        }

        // Selected sequence colours
        if ((searchResults != null) && searchResults.contains(seq))
        {
          currentColor = Color.black;
          currentTextColor = Color.white;
        }
        else if ((av.getSelectionGroup() != null)
                && av.getSelectionGroup().getSequences(null).contains(seq))
        {
          currentColor = Color.lightGray;
          currentTextColor = Color.black;
        }
        else
        {
          currentColor = av.getSequenceColour(seq);
          currentTextColor = Color.black;
        }

        gg.setColor(currentColor);
        // TODO: isrep could be used to highlight the representative in a
        // different way
        gg.fillRect(0, (i - starty) * avcharHeight, getSize().width,
                avcharHeight);
        gg.setColor(currentTextColor);

        gg.drawString(seq.getDisplayId(av.getShowJVSuffix()), 0,
                (((i - starty) * avcharHeight) + avcharHeight)
                        - (avcharHeight / 5));

        if (hiddenRows)
        {
          drawMarker(i, starty, 0);
        }
      }
    }
  }

  public void setHighlighted(List<SequenceI> list)
  {
    searchResults = list;
    repaint();
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
      gg.fillPolygon(new int[] { getSize().width - avcharHeight,
          getSize().width - avcharHeight, getSize().width }, new int[] {
          (i - starty) * avcharHeight + yoffset,
          (i - starty) * avcharHeight + yoffset + avcharHeight / 4,
          (i - starty) * avcharHeight + yoffset }, 3);
    }
    if (above)
    {
      gg.fillPolygon(new int[] { getSize().width - avcharHeight,
          getSize().width - avcharHeight, getSize().width }, new int[] {
          (i - starty + 1) * avcharHeight + yoffset,
          (i - starty + 1) * avcharHeight + yoffset - avcharHeight / 4,
          (i - starty + 1) * avcharHeight + yoffset }, 3);

    }
  }

  boolean setHiddenFont(SequenceI seq)
  {
    Font bold = new Font(av.getFont().getName(), Font.BOLD, av.getFont()
            .getSize());

    if (av.isReferenceSeq(seq) || av.isHiddenRepSequence(seq))
    {
      gg.setFont(bold);
      return true;
    }
    return false;
  }
}
