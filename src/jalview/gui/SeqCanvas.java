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

import jalview.datamodel.AlignmentI;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.renderer.ScaleRenderer;
import jalview.renderer.ScaleRenderer.ScaleMark;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SeqCanvas extends JComponent
{
  final FeatureRenderer fr;

  final SequenceRenderer sr;

  BufferedImage img;

  Graphics2D gg;

  int imgWidth;

  int imgHeight;

  AlignViewport av;

  boolean fastPaint = false;

  int LABEL_WEST;

  int LABEL_EAST;

  int cursorX = 0;

  int cursorY = 0;

  /**
   * Creates a new SeqCanvas object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public SeqCanvas(AlignmentPanel ap)
  {
    this.av = ap.av;
    updateViewport();
    fr = new FeatureRenderer(ap);
    sr = new SequenceRenderer(av);
    setLayout(new BorderLayout());
    PaintRefresher.Register(this, av.getSequenceSetId());
    setBackground(Color.white);
  }

  public SequenceRenderer getSequenceRenderer()
  {
    return sr;
  }

  public FeatureRenderer getFeatureRenderer()
  {
    return fr;
  }

  int charHeight = 0, charWidth = 0;

  private void updateViewport()
  {
    charHeight = av.getCharHeight();
    charWidth = av.getCharWidth();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startx
   *          DOCUMENT ME!
   * @param endx
   *          DOCUMENT ME!
   * @param ypos
   *          DOCUMENT ME!
   */
  private void drawNorthScale(Graphics g, int startx, int endx, int ypos)
  {
    updateViewport();
    for (ScaleMark mark : new ScaleRenderer().calculateMarks(av, startx,
            endx))
    {
      int mpos = mark.column; // (i - startx - 1)
      if (mpos < 0)
      {
        continue;
      }
      String mstring = mark.text;

      if (mark.major)
      {
        if (mstring != null)
        {
          g.drawString(mstring, mpos * charWidth, ypos - (charHeight / 2));
        }
        g.drawLine((mpos * charWidth) + (charWidth / 2), (ypos + 2)
                - (charHeight / 2), (mpos * charWidth) + (charWidth / 2),
                ypos - 2);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startx
   *          DOCUMENT ME!
   * @param endx
   *          DOCUMENT ME!
   * @param ypos
   *          DOCUMENT ME!
   */
  void drawWestScale(Graphics g, int startx, int endx, int ypos)
  {
    FontMetrics fm = getFontMetrics(av.getFont());
    ypos += charHeight;

    if (av.hasHiddenColumns())
    {
      startx = av.getColumnSelection().adjustForHiddenColumns(startx);
      endx = av.getColumnSelection().adjustForHiddenColumns(endx);
    }

    int maxwidth = av.getAlignment().getWidth();
    if (av.hasHiddenColumns())
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    // WEST SCALE
    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      SequenceI seq = av.getAlignment().getSequenceAt(i);
      int index = startx;
      int value = -1;

      while (index < endx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index++;

          continue;
        }

        value = av.getAlignment().getSequenceAt(i).findPosition(index);

        break;
      }

      if (value != -1)
      {
        int x = LABEL_WEST - fm.stringWidth(String.valueOf(value))
                - charWidth / 2;
        g.drawString(value + "", x, (ypos + (i * charHeight))
                - (charHeight / 5));
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param startx
   *          DOCUMENT ME!
   * @param endx
   *          DOCUMENT ME!
   * @param ypos
   *          DOCUMENT ME!
   */
  void drawEastScale(Graphics g, int startx, int endx, int ypos)
  {
    ypos += charHeight;

    if (av.hasHiddenColumns())
    {
      endx = av.getColumnSelection().adjustForHiddenColumns(endx);
    }

    SequenceI seq;
    // EAST SCALE
    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      seq = av.getAlignment().getSequenceAt(i);
      int index = endx;
      int value = -1;

      while (index > startx)
      {
        if (jalview.util.Comparison.isGap(seq.getCharAt(index)))
        {
          index--;

          continue;
        }

        value = seq.findPosition(index);

        break;
      }

      if (value != -1)
      {
        g.drawString(String.valueOf(value), 0, (ypos + (i * charHeight))
                - (charHeight / 5));
      }
    }
  }

  boolean fastpainting = false;

  /**
   * need to make this thread safe move alignment rendering in response to
   * slider adjustment
   * 
   * @param horizontal
   *          shift along
   * @param vertical
   *          shift up or down in repaint
   */
  public void fastPaint(int horizontal, int vertical)
  {
    if (fastpainting || gg == null)
    {
      return;
    }
    fastpainting = true;
    fastPaint = true;
    updateViewport();
    gg.copyArea(horizontal * charWidth, vertical * charHeight, imgWidth,
            imgHeight, -horizontal * charWidth, -vertical * charHeight);

    int sr = av.startRes;
    int er = av.endRes;
    int ss = av.startSeq;
    int es = av.endSeq;
    int transX = 0;
    int transY = 0;

    if (horizontal > 0) // scrollbar pulled right, image to the left
    {
      er++;
      transX = (er - sr - horizontal) * charWidth;
      sr = er - horizontal;
    }
    else if (horizontal < 0)
    {
      er = sr - horizontal - 1;
    }
    else if (vertical > 0) // scroll down
    {
      ss = es - vertical;

      if (ss < av.startSeq)
      { // ie scrolling too fast, more than a page at a time
        ss = av.startSeq;
      }
      else
      {
        transY = imgHeight - (vertical * charHeight);
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

    gg.translate(transX, transY);
    drawPanel(gg, sr, er, ss, es, 0);
    gg.translate(-transX, -transY);

    repaint();
    fastpainting = false;
  }

  /**
   * Definitions of startx and endx (hopefully): SMJS This is what I'm working
   * towards! startx is the first residue (starting at 0) to display. endx is
   * the last residue to display (starting at 0). starty is the first sequence
   * to display (starting at 0). endy is the last sequence to display (starting
   * at 0). NOTE 1: The av limits are set in setFont in this class and in the
   * adjustment listener in SeqPanel when the scrollbars move.
   */

  // Set this to false to force a full panel paint
  @Override
  public void paintComponent(Graphics g)
  {
    updateViewport();
    BufferedImage lcimg = img; // take reference since other threads may null
    // img and call later.
    super.paintComponent(g);

    if (lcimg != null
            && (fastPaint
                    || (getVisibleRect().width != g.getClipBounds().width) || (getVisibleRect().height != g
                    .getClipBounds().height)))
    {
      g.drawImage(lcimg, 0, 0, this);
      fastPaint = false;
      return;
    }

    // this draws the whole of the alignment
    imgWidth = getWidth();
    imgHeight = getHeight();

    imgWidth -= (imgWidth % charWidth);
    imgHeight -= (imgHeight % charHeight);

    if ((imgWidth < 1) || (imgHeight < 1))
    {
      return;
    }

    if (lcimg == null || imgWidth != lcimg.getWidth()
            || imgHeight != lcimg.getHeight())
    {
      try
      {
        lcimg = img = new BufferedImage(imgWidth, imgHeight,
                BufferedImage.TYPE_INT_RGB);
        gg = (Graphics2D) img.getGraphics();
        gg.setFont(av.getFont());
      } catch (OutOfMemoryError er)
      {
        System.gc();
        System.err.println("SeqCanvas OutOfMemory Redraw Error.\n" + er);
        new OOMWarning("Creating alignment image for display", er);

        return;
      }
    }

    if (av.antiAlias)
    {
      gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);
    }

    gg.setColor(Color.white);
    gg.fillRect(0, 0, imgWidth, imgHeight);

    if (av.getWrapAlignment())
    {
      drawWrappedPanel(gg, getWidth(), getHeight(), av.startRes);
    }
    else
    {
      drawPanel(gg, av.startRes, av.endRes, av.startSeq, av.endSeq, 0);
    }

    g.drawImage(lcimg, 0, 0, this);

  }

  /**
   * DOCUMENT ME!
   * 
   * @param cwidth
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getWrappedCanvasWidth(int cwidth)
  {
    FontMetrics fm = getFontMetrics(av.getFont());

    LABEL_EAST = 0;
    LABEL_WEST = 0;

    if (av.getScaleRightWrapped())
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.getScaleLeftWrapped())
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    return (cwidth - LABEL_EAST - LABEL_WEST) / charWidth;
  }

  /**
   * Generates a string of zeroes.
   * 
   * @return String
   */
  String getMask()
  {
    String mask = "00";
    int maxWidth = 0;
    int tmp;
    for (int i = 0; i < av.getAlignment().getHeight(); i++)
    {
      tmp = av.getAlignment().getSequenceAt(i).getEnd();
      if (tmp > maxWidth)
      {
        maxWidth = tmp;
      }
    }

    for (int i = maxWidth; i > 0; i /= 10)
    {
      mask += "0";
    }
    return mask;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param canvasWidth
   *          DOCUMENT ME!
   * @param canvasHeight
   *          DOCUMENT ME!
   * @param startRes
   *          DOCUMENT ME!
   */
  public void drawWrappedPanel(Graphics g, int canvasWidth,
          int canvasHeight, int startRes)
  {
    updateViewport();
    AlignmentI al = av.getAlignment();

    FontMetrics fm = getFontMetrics(av.getFont());

    if (av.getScaleRightWrapped())
    {
      LABEL_EAST = fm.stringWidth(getMask());
    }

    if (av.getScaleLeftWrapped())
    {
      LABEL_WEST = fm.stringWidth(getMask());
    }

    int hgap = charHeight;
    if (av.getScaleAboveWrapped())
    {
      hgap += charHeight;
    }

    int cWidth = (canvasWidth - LABEL_EAST - LABEL_WEST) / charWidth;
    int cHeight = av.getAlignment().getHeight() * charHeight;

    av.setWrappedWidth(cWidth);

    av.endRes = av.startRes + cWidth;

    int endx;
    int ypos = hgap;
    int maxwidth = av.getAlignment().getWidth() - 1;

    if (av.hasHiddenColumns())
    {
      maxwidth = av.getColumnSelection().findColumnPosition(maxwidth) - 1;
    }

    while ((ypos <= canvasHeight) && (startRes < maxwidth))
    {
      endx = startRes + cWidth - 1;

      if (endx > maxwidth)
      {
        endx = maxwidth;
      }

      g.setFont(av.getFont());
      g.setColor(Color.black);

      if (av.getScaleLeftWrapped())
      {
        drawWestScale(g, startRes, endx, ypos);
      }

      if (av.getScaleRightWrapped())
      {
        g.translate(canvasWidth - LABEL_EAST, 0);
        drawEastScale(g, startRes, endx, ypos);
        g.translate(-(canvasWidth - LABEL_EAST), 0);
      }

      g.translate(LABEL_WEST, 0);

      if (av.getScaleAboveWrapped())
      {
        drawNorthScale(g, startRes, endx, ypos);
      }

      if (av.hasHiddenColumns() && av.getShowHiddenMarkers())
      {
        g.setColor(Color.blue);
        int res;
        for (int i = 0; i < av.getColumnSelection().getHiddenColumns()
                .size(); i++)
        {
          res = av.getColumnSelection().findHiddenRegionPosition(i)
                  - startRes;

          if (res < 0 || res > endx - startRes)
          {
            continue;
          }

          gg.fillPolygon(
                  new int[] { res * charWidth - charHeight / 4,
                      res * charWidth + charHeight / 4, res * charWidth },
                  new int[] { ypos - (charHeight / 2),
                      ypos - (charHeight / 2), ypos - (charHeight / 2) + 8 },
                  3);

        }
      }

      // When printing we have an extra clipped region,
      // the Printable page which we need to account for here
      Shape clip = g.getClip();

      if (clip == null)
      {
        g.setClip(0, 0, cWidth * charWidth, canvasHeight);
      }
      else
      {
        g.setClip(0, (int) clip.getBounds().getY(), cWidth * charWidth,
                (int) clip.getBounds().getHeight());
      }

      drawPanel(g, startRes, endx, 0, al.getHeight(), ypos);

      if (av.isShowAnnotation())
      {
        g.translate(0, cHeight + ypos + 3);
        if (annotations == null)
        {
          annotations = new AnnotationPanel(av);
        }

        annotations.renderer.drawComponent(annotations, av, g, -1,
                startRes, endx + 1);
        g.translate(0, -cHeight - ypos - 3);
      }
      g.setClip(clip);
      g.translate(-LABEL_WEST, 0);

      ypos += cHeight + getAnnotationHeight() + hgap;

      startRes += cWidth;
    }
  }

  AnnotationPanel annotations;

  int getAnnotationHeight()
  {
    if (!av.isShowAnnotation())
    {
      return 0;
    }

    if (annotations == null)
    {
      annotations = new AnnotationPanel(av);
    }

    return annotations.adjustPanelHeight();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g1
   *          DOCUMENT ME!
   * @param startRes
   *          DOCUMENT ME!
   * @param endRes
   *          DOCUMENT ME!
   * @param startSeq
   *          DOCUMENT ME!
   * @param endSeq
   *          DOCUMENT ME!
   * @param offset
   *          DOCUMENT ME!
   */
  public void drawPanel(Graphics g1, int startRes, int endRes,
          int startSeq, int endSeq, int offset)
  {
    updateViewport();
    if (!av.hasHiddenColumns())
    {
      draw(g1, startRes, endRes, startSeq, endSeq, offset);
    }
    else
    {
      List<int[]> regions = av.getColumnSelection().getHiddenColumns();

      int screenY = 0;
      int blockStart = startRes;
      int blockEnd = endRes;

      for (int[] region : regions)
      {
        int hideStart = region[0];
        int hideEnd = region[1];

        if (hideStart <= blockStart)
        {
          blockStart += (hideEnd - hideStart) + 1;
          continue;
        }

        blockEnd = hideStart - 1;

        g1.translate(screenY * charWidth, 0);

        draw(g1, blockStart, blockEnd, startSeq, endSeq, offset);

        if (av.getShowHiddenMarkers())
        {
          g1.setColor(Color.blue);

          g1.drawLine((blockEnd - blockStart + 1) * charWidth - 1,
                  0 + offset, (blockEnd - blockStart + 1) * charWidth - 1,
                  (endSeq - startSeq) * charHeight + offset);
        }

        g1.translate(-screenY * charWidth, 0);
        screenY += blockEnd - blockStart + 1;
        blockStart = hideEnd + 1;

        if (screenY > (endRes - startRes))
        {
          // already rendered last block
          return;
        }
      }

      if (screenY <= (endRes - startRes))
      {
        // remaining visible region to render
        blockEnd = blockStart + (endRes - startRes) - screenY;
        g1.translate(screenY * charWidth, 0);
        draw(g1, blockStart, blockEnd, startSeq, endSeq, offset);

        g1.translate(-screenY * charWidth, 0);
      }
    }

  }

  // int startRes, int endRes, int startSeq, int endSeq, int x, int y,
  // int x1, int x2, int y1, int y2, int startx, int starty,
  private void draw(Graphics g, int startRes, int endRes, int startSeq,
          int endSeq, int offset)
  {
    g.setFont(av.getFont());
    sr.prepare(g, av.isRenderGaps());

    SequenceI nextSeq;

    // / First draw the sequences
    // ///////////////////////////
    for (int i = startSeq; i < endSeq; i++)
    {
      nextSeq = av.getAlignment().getSequenceAt(i);
      if (nextSeq == null)
      {
        // occasionally, a race condition occurs such that the alignment row is
        // empty
        continue;
      }
      sr.drawSequence(nextSeq, av.getAlignment().findAllGroups(nextSeq),
              startRes, endRes, offset + ((i - startSeq) * charHeight));

      if (av.isShowSequenceFeatures())
      {
        fr.drawSequence(g, nextSeq, startRes, endRes, offset
                + ((i - startSeq) * charHeight));
      }

      // / Highlight search Results once all sequences have been drawn
      // ////////////////////////////////////////////////////////
      if (av.hasSearchResults())
      {
        int[] visibleResults = av.getSearchResults().getResults(nextSeq,
                startRes, endRes);
        if (visibleResults != null)
        {
          for (int r = 0; r < visibleResults.length; r += 2)
          {
            sr.drawHighlightedText(nextSeq, visibleResults[r],
                    visibleResults[r + 1], (visibleResults[r] - startRes)
                            * charWidth, offset
                            + ((i - startSeq) * charHeight));
          }
        }
      }

      if (av.cursorMode && cursorY == i && cursorX >= startRes
              && cursorX <= endRes)
      {
        sr.drawCursor(nextSeq, cursorX, (cursorX - startRes) * charWidth,
                offset + ((i - startSeq) * charHeight));
      }
    }

    if (av.getSelectionGroup() != null
            || av.getAlignment().getGroups().size() > 0)
    {
      drawGroupsBoundaries(g, startRes, endRes, startSeq, endSeq, offset);
    }

  }

  void drawGroupsBoundaries(Graphics g1, int startRes, int endRes,
          int startSeq, int endSeq, int offset)
  {
    Graphics2D g = (Graphics2D) g1;
    //
    // ///////////////////////////////////
    // Now outline any areas if necessary
    // ///////////////////////////////////
    SequenceGroup group = av.getSelectionGroup();

    int sx = -1;
    int sy = -1;
    int ex = -1;
    int groupIndex = -1;
    int visWidth = (endRes - startRes + 1) * charWidth;

    if ((group == null) && (av.getAlignment().getGroups().size() > 0))
    {
      group = av.getAlignment().getGroups().get(0);
      groupIndex = 0;
    }

    if (group != null)
    {
      do
      {
        int oldY = -1;
        int i = 0;
        boolean inGroup = false;
        int top = -1;
        int bottom = -1;

        for (i = startSeq; i < endSeq; i++)
        {
          sx = (group.getStartRes() - startRes) * charWidth;
          sy = offset + ((i - startSeq) * charHeight);
          ex = (((group.getEndRes() + 1) - group.getStartRes()) * charWidth) - 1;

          if (sx + ex < 0 || sx > visWidth)
          {
            continue;
          }

          if ((sx <= (endRes - startRes) * charWidth)
                  && group.getSequences(null).contains(
                          av.getAlignment().getSequenceAt(i)))
          {
            if ((bottom == -1)
                    && !group.getSequences(null).contains(
                            av.getAlignment().getSequenceAt(i + 1)))
            {
              bottom = sy + charHeight;
            }

            if (!inGroup)
            {
              if (((top == -1) && (i == 0))
                      || !group.getSequences(null).contains(
                              av.getAlignment().getSequenceAt(i - 1)))
              {
                top = sy;
              }

              oldY = sy;
              inGroup = true;

              if (group == av.getSelectionGroup())
              {
                g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_ROUND, 3f, new float[] { 5f, 3f },
                        0f));
                g.setColor(Color.RED);
              }
              else
              {
                g.setStroke(new BasicStroke());
                g.setColor(group.getOutlineColour());
              }
            }
          }
          else
          {
            if (inGroup)
            {
              if (sx >= 0 && sx < visWidth)
              {
                g.drawLine(sx, oldY, sx, sy);
              }

              if (sx + ex < visWidth)
              {
                g.drawLine(sx + ex, oldY, sx + ex, sy);
              }

              if (sx < 0)
              {
                ex += sx;
                sx = 0;
              }

              if (sx + ex > visWidth)
              {
                ex = visWidth;
              }

              else if (sx + ex >= (endRes - startRes + 1) * charWidth)
              {
                ex = (endRes - startRes + 1) * charWidth;
              }

              if (top != -1)
              {
                g.drawLine(sx, top, sx + ex, top);
                top = -1;
              }

              if (bottom != -1)
              {
                g.drawLine(sx, bottom, sx + ex, bottom);
                bottom = -1;
              }

              inGroup = false;
            }
          }
        }

        if (inGroup)
        {
          sy = offset + ((i - startSeq) * charHeight);
          if (sx >= 0 && sx < visWidth)
          {
            g.drawLine(sx, oldY, sx, sy);
          }

          if (sx + ex < visWidth)
          {
            g.drawLine(sx + ex, oldY, sx + ex, sy);
          }

          if (sx < 0)
          {
            ex += sx;
            sx = 0;
          }

          if (sx + ex > visWidth)
          {
            ex = visWidth;
          }
          else if (sx + ex >= (endRes - startRes + 1) * charWidth)
          {
            ex = (endRes - startRes + 1) * charWidth;
          }

          if (top != -1)
          {
            g.drawLine(sx, top, sx + ex, top);
            top = -1;
          }

          if (bottom != -1)
          {
            g.drawLine(sx, bottom - 1, sx + ex, bottom - 1);
            bottom = -1;
          }

          inGroup = false;
        }

        groupIndex++;

        g.setStroke(new BasicStroke());

        if (groupIndex >= av.getAlignment().getGroups().size())
        {
          break;
        }

        group = av.getAlignment().getGroups().get(groupIndex);

      } while (groupIndex < av.getAlignment().getGroups().size());

    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param results
   *          DOCUMENT ME!
   */
  public void highlightSearchResults(SearchResultsI results)
  {
    img = null;

    av.setSearchResults(results);

    repaint();
  }
}
