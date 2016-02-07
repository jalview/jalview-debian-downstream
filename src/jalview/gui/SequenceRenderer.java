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

import jalview.api.FeatureRenderer;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SequenceRenderer implements jalview.api.SequenceRenderer
{
  final static int CHAR_TO_UPPER = 'A' - 'a';

  AlignViewport av;

  FontMetrics fm;

  boolean renderGaps = true;

  SequenceGroup currentSequenceGroup = null;

  SequenceGroup[] allGroups = null;

  Color resBoxColour;

  Graphics graphics;

  boolean monospacedFont;

  boolean forOverview = false;

  /**
   * Creates a new SequenceRenderer object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public SequenceRenderer(AlignViewport av)
  {
    this.av = av;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param b
   *          DOCUMENT ME!
   */
  public void prepare(Graphics g, boolean renderGaps)
  {
    graphics = g;
    fm = g.getFontMetrics();

    // If EPS graphics, stringWidth will be a double, not an int
    double dwidth = fm.getStringBounds("M", g).getWidth();

    monospacedFont = (dwidth == fm.getStringBounds("|", g).getWidth() && av
            .getCharWidth() == dwidth);

    this.renderGaps = renderGaps;
  }

  @Override
  public Color getResidueBoxColour(SequenceI seq, int i)
  {
    allGroups = av.getAlignment().findAllGroups(seq);

    if (inCurrentSequenceGroup(i))
    {
      if (currentSequenceGroup.getDisplayBoxes())
      {
        getBoxColour(currentSequenceGroup.cs, seq, i);
      }
    }
    else if (av.getShowBoxes())
    {
      getBoxColour(av.getGlobalColourScheme(), seq, i);
    }

    return resBoxColour;
  }

  /**
   * Get the residue colour at the given sequence position - as determined by
   * the sequence group colour (if any), else the colour scheme, possibly
   * overridden by a feature colour.
   * 
   * @param seq
   * @param position
   * @param fr
   * @return
   */
  @Override
  public Color getResidueColour(final SequenceI seq, int position,
          FeatureRenderer fr)
  {
    // TODO replace 8 or so code duplications with calls to this method
    // (refactored as needed)
    Color col = getResidueBoxColour(seq, position);

    if (fr != null)
    {
      col = fr.findFeatureColour(col, seq, position);
    }
    return col;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param cs
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   * @param i
   *          DOCUMENT ME!
   */
  void getBoxColour(ColourSchemeI cs, SequenceI seq, int i)
  {
    if (cs != null)
    {
      resBoxColour = cs.findColour(seq.getCharAt(i), i, seq);
    }
    else if (forOverview
            && !jalview.util.Comparison.isGap(seq.getCharAt(i)))
    {
      resBoxColour = Color.lightGray;
    }
    else
    {
      resBoxColour = Color.white;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   * @param sg
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawSequence(SequenceI seq, SequenceGroup[] sg, int start,
          int end, int y1)
  {
    allGroups = sg;

    drawBoxes(seq, start, end, y1);

    if (av.validCharWidth)
    {
      drawText(seq, start, end, y1);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public synchronized void drawBoxes(SequenceI seq, int start, int end,
          int y1)
  {
    if (seq == null)
    {
      return; // fix for racecondition
    }
    int i = start;
    int length = seq.getLength();

    int curStart = -1;
    int curWidth = av.getCharWidth(), avWidth = av.getCharWidth(), avHeight = av
            .getCharHeight();

    Color tempColour = null;

    while (i <= end)
    {
      resBoxColour = Color.white;

      if (i < length)
      {
        if (inCurrentSequenceGroup(i))
        {
          if (currentSequenceGroup.getDisplayBoxes())
          {
            getBoxColour(currentSequenceGroup.cs, seq, i);
          }
        }
        else if (av.getShowBoxes())
        {
          getBoxColour(av.getGlobalColourScheme(), seq, i);
        }

      }

      if (resBoxColour != tempColour)
      {
        if (tempColour != null)
        {
          graphics.fillRect(avWidth * (curStart - start), y1, curWidth,
                  avHeight);
        }

        graphics.setColor(resBoxColour);

        curStart = i;
        curWidth = avWidth;
        tempColour = resBoxColour;
      }
      else
      {
        curWidth += avWidth;
      }

      i++;
    }

    graphics.fillRect(avWidth * (curStart - start), y1, curWidth, avHeight);

  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawText(SequenceI seq, int start, int end, int y1)
  {
    y1 += av.getCharHeight() - av.getCharHeight() / 5; // height/5 replaces pady
    int charOffset = 0;
    char s;

    if (end + 1 >= seq.getLength())
    {
      end = seq.getLength() - 1;
    }
    graphics.setColor(av.getTextColour());

    if (monospacedFont && av.getShowText() && allGroups.length == 0
            && !av.getColourText() && av.getThresholdTextColour() == 0)
    {
      if (av.isRenderGaps())
      {
        graphics.drawString(seq.getSequenceAsString(start, end + 1), 0, y1);
      }
      else
      {
        char gap = av.getGapCharacter();
        graphics.drawString(seq.getSequenceAsString(start, end + 1)
                .replace(gap, ' '), 0, y1);
      }
    }
    else
    {
      boolean srep = av.isDisplayReferenceSeq();
      boolean getboxColour = false;
      boolean isarep = av.getAlignment().getSeqrep() == seq;
      boolean isgrep = currentSequenceGroup != null ? currentSequenceGroup
              .getSeqrep() == seq : false;
      char sr_c;
      for (int i = start; i <= end; i++)
      {

        graphics.setColor(av.getTextColour());
        getboxColour = false;
        s = seq.getCharAt(i);

        if (!renderGaps && jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        if (inCurrentSequenceGroup(i))
        {
          if (!currentSequenceGroup.getDisplayText())
          {
            continue;
          }

          if (currentSequenceGroup.thresholdTextColour > 0
                  || currentSequenceGroup.getColourText())
          {
            getboxColour = true;
            getBoxColour(currentSequenceGroup.cs, seq, i);

            if (currentSequenceGroup.getColourText())
            {
              graphics.setColor(resBoxColour.darker());
            }

            if (currentSequenceGroup.thresholdTextColour > 0)
            {
              if (resBoxColour.getRed() + resBoxColour.getBlue()
                      + resBoxColour.getGreen() < currentSequenceGroup.thresholdTextColour)
              {
                graphics.setColor(currentSequenceGroup.textColour2);
              }
            }
          }
          else
          {
            graphics.setColor(currentSequenceGroup.textColour);
          }
          if (!isarep && !isgrep
                  && currentSequenceGroup.getShowNonconserved()) // todo
                                                                 // optimize
          {
            // todo - use sequence group consensus
            s = getDisplayChar(srep, i, s, '.', currentSequenceGroup);

          }

        }
        else
        {
          if (!av.getShowText())
          {
            continue;
          }

          if (av.getColourText())
          {
            getboxColour = true;
            getBoxColour(av.getGlobalColourScheme(), seq, i);

            if (av.getShowBoxes())
            {
              graphics.setColor(resBoxColour.darker());
            }
            else
            {
              graphics.setColor(resBoxColour);
            }
          }

          if (av.getThresholdTextColour() > 0)
          {
            if (!getboxColour)
            {
              getBoxColour(av.getGlobalColourScheme(), seq, i);
            }

            if (resBoxColour.getRed() + resBoxColour.getBlue()
                    + resBoxColour.getGreen() < av.getThresholdTextColour())
            {
              graphics.setColor(av.getTextColour2());
            }
          }
          if (!isarep && av.getShowUnconserved())
          {
            s = getDisplayChar(srep, i, s, '.', currentSequenceGroup);

          }

        }

        charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                charOffset + av.getCharWidth() * (i - start), y1);

      }
    }
  }

  /**
   * Returns 'conservedChar' to represent the given position if the sequence
   * character at that position is equal to the consensus (ignoring case), else
   * returns the sequence character
   * 
   * @param usesrep
   * @param position
   * @param sequenceChar
   * @param conservedChar
   * @return
   */
  private char getDisplayChar(final boolean usesrep, int position,
          char sequenceChar, char conservedChar, SequenceGroup currentGroup)
  {
    // TODO - use currentSequenceGroup rather than alignment
    // currentSequenceGroup.getConsensus()
    char conschar = (usesrep) ? (currentGroup == null ? av.getAlignment()
            .getSeqrep().getCharAt(position)
            : (currentGroup.getSeqrep() != null ? currentGroup.getSeqrep()
                    .getCharAt(position) : av.getAlignment().getSeqrep()
                    .getCharAt(position)))
            : (currentGroup != null && currentGroup.getConsensus() != null) ? currentGroup
                    .getConsensus().annotations[position].displayCharacter
                    .charAt(0)
                    : av.getAlignmentConsensusAnnotation().annotations[position].displayCharacter
                            .charAt(0);
    if (!jalview.util.Comparison.isGap(conschar)
            && (sequenceChar == conschar || sequenceChar + CHAR_TO_UPPER == conschar))
    {
      sequenceChar = conservedChar;
    }
    return sequenceChar;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param res
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  boolean inCurrentSequenceGroup(int res)
  {
    if (allGroups == null)
    {
      return false;
    }

    for (int i = 0; i < allGroups.length; i++)
    {
      if ((allGroups[i].getStartRes() <= res)
              && (allGroups[i].getEndRes() >= res))
      {
        currentSequenceGroup = allGroups[i];

        return true;
      }
    }

    return false;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void drawHighlightedText(SequenceI seq, int start, int end,
          int x1, int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.BLACK);
    graphics.fillRect(x1, y1, av.getCharWidth() * (end - start + 1),
            av.getCharHeight());
    graphics.setColor(Color.white);

    char s = '~';

    // Need to find the sequence position here.
    if (av.isValidCharWidth())
    {
      for (int i = start; i <= end; i++)
      {
        if (i < seq.getLength())
        {
          s = seq.getCharAt(i);
        }

        charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
        graphics.drawString(String.valueOf(s),
                charOffset + x1 + (av.getCharWidth() * (i - start)),
                (y1 + av.getCharHeight()) - pady);
      }
    }
  }

  public void drawCursor(SequenceI seq, int res, int x1, int y1)
  {
    int pady = av.getCharHeight() / 5;
    int charOffset = 0;
    graphics.setColor(Color.black);
    graphics.fillRect(x1, y1, av.getCharWidth(), av.getCharHeight());

    if (av.isValidCharWidth())
    {
      graphics.setColor(Color.white);

      char s = seq.getCharAt(res);

      charOffset = (av.getCharWidth() - fm.charWidth(s)) / 2;
      graphics.drawString(String.valueOf(s), charOffset + x1,
              (y1 + av.getCharHeight()) - pady);
    }

  }
}
