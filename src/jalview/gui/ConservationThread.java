/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.gui;

import jalview.analysis.Conservation;
import jalview.datamodel.Annotation;

import java.awt.Color;

class ConservationThread extends Thread
{
  /**
   * 
   */
  private AlignViewport alignViewport;

  AlignmentPanel ap;

  public ConservationThread(AlignViewport alignViewport, AlignmentPanel ap)
  {
    this.alignViewport = alignViewport;
    this.ap = ap;
  }

  public void run()
  {
    try
    {
      this.alignViewport.updatingConservation = true;

      while (AlignViewport.UPDATING_CONSERVATION)
      {
        try
        {
          if (ap != null)
          {
            ap.paintAlignment(false);
          }
          Thread.sleep(200);
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }

      AlignViewport.UPDATING_CONSERVATION = true;

      int alWidth;
      
      if (alignViewport==null || alignViewport.alignment==null || (alWidth=alignViewport.alignment.getWidth())< 0)
      {
        this.alignViewport.updatingConservation = false;
        AlignViewport.UPDATING_CONSERVATION = false;
        return;
      }

      Conservation cons = new jalview.analysis.Conservation("All",
              jalview.schemes.ResidueProperties.propHash, 3,
              this.alignViewport.alignment.getSequences(), 0, alWidth - 1);

      cons.calculate();
      cons.verdict(false, this.alignViewport.ConsPercGaps);

      if (this.alignViewport.quality != null)
      {
        cons.findQuality();
      }
      cons.completeAnnotations(alignViewport.conservation,
              alignViewport.quality, 0, alWidth);
    } catch (OutOfMemoryError error)
    {
      new OOMWarning("calculating conservation", error);

      this.alignViewport.conservation = null;
      this.alignViewport.quality = null;

    }

    AlignViewport.UPDATING_CONSERVATION = false;
    this.alignViewport.updatingConservation = false;

    if (ap != null)
    {
      ap.paintAlignment(true);
    }

  }
}
