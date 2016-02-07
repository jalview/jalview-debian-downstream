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
package jalview.renderer.seqfeatures;

import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class FeatureRenderer extends
        jalview.viewmodel.seqfeatures.FeatureRendererModel
{

  FontMetrics fm;

  int charOffset;

  boolean offscreenRender = false;

  protected SequenceI lastSeq;

  char s;

  int i;

  int av_charHeight, av_charWidth;

  boolean av_validCharWidth, av_isShowSeqFeatureHeight;

  protected void updateAvConfig()
  {
    av_charHeight = av.getCharHeight();
    av_charWidth = av.getCharWidth();
    av_validCharWidth = av.isValidCharWidth();
    av_isShowSeqFeatureHeight = av.isShowSequenceFeaturesHeight();
  }

  void renderFeature(Graphics g, SequenceI seq, int fstart, int fend,
          Color featureColour, int start, int end, int y1)
  {
    updateAvConfig();
    if (((fstart <= end) && (fend >= start)))
    {
      if (fstart < start)
      { // fix for if the feature we have starts before the sequence start,
        fstart = start; // but the feature end is still valid!!
      }

      if (fend >= end)
      {
        fend = end;
      }
      int pady = (y1 + av_charHeight) - av_charHeight / 5;
      for (i = fstart; i <= fend; i++)
      {
        s = seq.getCharAt(i);

        if (jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        g.setColor(featureColour);

        g.fillRect((i - start) * av_charWidth, y1, av_charWidth,
                av_charHeight);

        if (offscreenRender || !av_validCharWidth)
        {
          continue;
        }

        g.setColor(Color.white);
        charOffset = (av_charWidth - fm.charWidth(s)) / 2;
        g.drawString(String.valueOf(s), charOffset
                + (av_charWidth * (i - start)), pady);

      }
    }
  }

  void renderScoreFeature(Graphics g, SequenceI seq, int fstart, int fend,
          Color featureColour, int start, int end, int y1, byte[] bs)
  {
    updateAvConfig();
    if (((fstart <= end) && (fend >= start)))
    {
      if (fstart < start)
      { // fix for if the feature we have starts before the sequence start,
        fstart = start; // but the feature end is still valid!!
      }

      if (fend >= end)
      {
        fend = end;
      }
      int pady = (y1 + av_charHeight) - av_charHeight / 5;
      int ystrt = 0, yend = av_charHeight;
      if (bs[0] != 0)
      {
        // signed - zero is always middle of residue line.
        if (bs[1] < 128)
        {
          yend = av_charHeight * (128 - bs[1]) / 512;
          ystrt = av_charHeight - yend / 2;
        }
        else
        {
          ystrt = av_charHeight / 2;
          yend = av_charHeight * (bs[1] - 128) / 512;
        }
      }
      else
      {
        yend = av_charHeight * bs[1] / 255;
        ystrt = av_charHeight - yend;

      }
      for (i = fstart; i <= fend; i++)
      {
        s = seq.getCharAt(i);

        if (jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        g.setColor(featureColour);
        int x = (i - start) * av_charWidth;
        g.drawRect(x, y1, av_charWidth, av_charHeight);
        g.fillRect(x, y1 + ystrt, av_charWidth, yend);

        if (offscreenRender || !av_validCharWidth)
        {
          continue;
        }

        g.setColor(Color.black);
        charOffset = (av_charWidth - fm.charWidth(s)) / 2;
        g.drawString(String.valueOf(s), charOffset
                + (av_charWidth * (i - start)), pady);
      }
    }
  }

  BufferedImage offscreenImage;

  public Color findFeatureColour(Color initialCol, SequenceI seq, int res)
  {
    return new Color(findFeatureColour(initialCol.getRGB(), seq, res));
  }

  /**
   * This is used by the Molecule Viewer and Overview to get the accurate
   * colourof the rendered sequence
   */
  public synchronized int findFeatureColour(int initialCol,
          final SequenceI seq, int column)
  {
    if (!av.isShowSequenceFeatures())
    {
      return initialCol;
    }

    SequenceFeature[] sequenceFeatures = seq.getSequenceFeatures();
    if (seq != lastSeq)
    {
      lastSeq = seq;
      lastSequenceFeatures = sequenceFeatures;
      if (lastSequenceFeatures != null)
      {
        sfSize = lastSequenceFeatures.length;
      }
    }
    else
    {
      if (lastSequenceFeatures != sequenceFeatures)
      {
        lastSequenceFeatures = sequenceFeatures;
        if (lastSequenceFeatures != null)
        {
          sfSize = lastSequenceFeatures.length;
        }
      }
    }

    if (lastSequenceFeatures == null || sfSize == 0)
    {
      return initialCol;
    }

    if (jalview.util.Comparison.isGap(lastSeq.getCharAt(column)))
    {
      return Color.white.getRGB();
    }

    // Only bother making an offscreen image if transparency is applied
    if (transparency != 1.0f && offscreenImage == null)
    {
      offscreenImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    currentColour = null;
    // TODO: non-threadsafe - each rendering thread needs its own instance of
    // the feature renderer - or this should be synchronized.
    offscreenRender = true;

    if (offscreenImage != null)
    {
      offscreenImage.setRGB(0, 0, initialCol);
      drawSequence(offscreenImage.getGraphics(), lastSeq, column, column, 0);

      return offscreenImage.getRGB(0, 0);
    }
    else
    {
      drawSequence(null, lastSeq, lastSeq.findPosition(column), -1, -1);

      if (currentColour == null)
      {
        return initialCol;
      }
      else
      {
        return ((Integer) currentColour).intValue();
      }
    }

  }

  private volatile SequenceFeature[] lastSequenceFeatures;

  int sfSize;

  int sfindex;

  int spos;

  int epos;

  public synchronized void drawSequence(Graphics g, final SequenceI seq,
          int start, int end, int y1)
  {
    SequenceFeature[] sequenceFeatures = seq.getSequenceFeatures();
    if (sequenceFeatures == null || sequenceFeatures.length == 0)
    {
      return;
    }

    if (g != null)
    {
      fm = g.getFontMetrics();
    }

    updateFeatures();

    if (lastSeq == null || seq != lastSeq
            || sequenceFeatures != lastSequenceFeatures)
    {
      lastSeq = seq;
      lastSequenceFeatures = sequenceFeatures;
    }

    if (transparency != 1 && g != null)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              transparency));
    }

    if (!offscreenRender)
    {
      spos = lastSeq.findPosition(start);
      epos = lastSeq.findPosition(end);
    }

    sfSize = lastSequenceFeatures.length;
    String type;
    for (int renderIndex = 0; renderIndex < renderOrder.length; renderIndex++)
    {
      type = renderOrder[renderIndex];

      if (type == null || !showFeatureOfType(type))
      {
        continue;
      }

      // loop through all features in sequence to find
      // current feature to render
      for (sfindex = 0; sfindex < sfSize; sfindex++)
      {
        final SequenceFeature sequenceFeature = lastSequenceFeatures[sfindex];
        if (!sequenceFeature.type.equals(type))
        {
          continue;
        }

        if (featureGroups != null
                && sequenceFeature.featureGroup != null
                && sequenceFeature.featureGroup.length() != 0
                && featureGroups.containsKey(sequenceFeature.featureGroup)
                && !featureGroups.get(sequenceFeature.featureGroup)
                        .booleanValue())
        {
          continue;
        }

        if (!offscreenRender
                && (sequenceFeature.getBegin() > epos || sequenceFeature
                        .getEnd() < spos))
        {
          continue;
        }

        if (offscreenRender && offscreenImage == null)
        {
          if (sequenceFeature.begin <= start
                  && sequenceFeature.end >= start)
          {
            // this is passed out to the overview and other sequence renderers
            // (e.g. molecule viewer) to get displayed colour for rendered
            // sequence
            currentColour = new Integer(getColour(sequenceFeature).getRGB());
            // used to be retreived from av.featuresDisplayed
            // currentColour = av.featuresDisplayed
            // .get(sequenceFeatures[sfindex].type);

          }
        }
        else if (sequenceFeature.type.equals("disulfide bond"))
        {
          renderFeature(g, seq, seq.findIndex(sequenceFeature.begin) - 1,
                  seq.findIndex(sequenceFeature.begin) - 1,
                  getColour(sequenceFeature)
                  // new Color(((Integer) av.featuresDisplayed
                  // .get(sequenceFeatures[sfindex].type)).intValue())
                  , start, end, y1);
          renderFeature(g, seq, seq.findIndex(sequenceFeature.end) - 1,
                  seq.findIndex(sequenceFeature.end) - 1,
                  getColour(sequenceFeature)
                  // new Color(((Integer) av.featuresDisplayed
                  // .get(sequenceFeatures[sfindex].type)).intValue())
                  , start, end, y1);

        }
        else if (showFeature(sequenceFeature))
        {
          if (av_isShowSeqFeatureHeight
                  && !Float.isNaN(sequenceFeature.score))
          {
            renderScoreFeature(g, seq,
                    seq.findIndex(sequenceFeature.begin) - 1,
                    seq.findIndex(sequenceFeature.end) - 1,
                    getColour(sequenceFeature), start, end, y1,
                    normaliseScore(sequenceFeature));
          }
          else
          {
            renderFeature(g, seq, seq.findIndex(sequenceFeature.begin) - 1,
                    seq.findIndex(sequenceFeature.end) - 1,
                    getColour(sequenceFeature), start, end, y1);
          }
        }

      }

    }

    if (transparency != 1.0f && g != null && transparencyAvailable)
    {
      Graphics2D g2 = (Graphics2D) g;
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
              1.0f));
    }
  }

  boolean transparencyAvailable = true;

  protected void setTransparencyAvailable(boolean isTransparencyAvailable)
  {
    transparencyAvailable = isTransparencyAvailable;
  }

  @Override
  public boolean isTransparencyAvailable()
  {
    return transparencyAvailable;
  }

  /**
   * Called when alignment in associated view has new/modified features to
   * discover and display.
   * 
   */
  public void featuresAdded()
  {
    lastSeq = null;
    findAllFeatures();
  }
}
