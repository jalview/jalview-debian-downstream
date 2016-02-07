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
package jalview.workers;

import jalview.analysis.AAFrequency;
import jalview.api.AlignCalcWorkerI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;

import java.util.Hashtable;

public class ConsensusThread extends AlignCalcWorker implements
        AlignCalcWorkerI
{
  public ConsensusThread(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
  }

  @Override
  public void run()
  {
    if (calcMan.isPending(this))
    {
      return;
    }
    calcMan.notifyStart(this);
    long started = System.currentTimeMillis();
    try
    {
      AlignmentAnnotation consensus = getConsensusAnnotation();
      if (consensus == null || calcMan.isPending(this))
      {
        calcMan.workerComplete(this);
        return;
      }
      while (!calcMan.notifyWorking(this))
      {
        // System.err.println("Thread (Consensus"+Thread.currentThread().getName()+") Waiting around.");
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
      if (alignViewport.isClosed())
      {
        abortAndDestroy();
        return;
      }
      AlignmentI alignment = alignViewport.getAlignment();

      int aWidth = -1;

      if (alignment == null || (aWidth = alignment.getWidth()) < 0)
      {
        calcMan.workerComplete(this);
        return;
      }

      eraseConsensus(aWidth);
      computeConsensus(alignment);
      updateResultAnnotation(true);

      if (ap != null)
      {
        ap.paintAlignment(true);
      }
    } catch (OutOfMemoryError error)
    {
      calcMan.workerCannotRun(this);
      ap.raiseOOMWarning("calculating consensus", error);
    } finally
    {
      /*
       * e.g. ArrayIndexOutOfBoundsException can happen due to a race condition
       * - alignment was edited at same time as calculation was running
       */
      calcMan.workerComplete(this);
    }
  }

  /**
   * Clear out any existing consensus annotations
   * 
   * @param aWidth
   *          the width (number of columns) of the annotated alignment
   */
  protected void eraseConsensus(int aWidth)
  {
    AlignmentAnnotation consensus = getConsensusAnnotation();
    consensus.annotations = new Annotation[aWidth];
  }

  /**
   * @param alignment
   */
  protected void computeConsensus(AlignmentI alignment)
  {
    Hashtable[] hconsensus = new Hashtable[alignment.getWidth()];

    SequenceI[] aseqs = getSequences();
    AAFrequency.calculate(aseqs, 0, alignment.getWidth(), hconsensus, true);

    alignViewport.setSequenceConsensusHash(hconsensus);
    setColourSchemeConsensus(hconsensus);
  }

  /**
   * @return
   */
  protected SequenceI[] getSequences()
  {
    return alignViewport.getAlignment().getSequencesArray();
  }

  /**
   * @param hconsensus
   */
  protected void setColourSchemeConsensus(Hashtable[] hconsensus)
  {
    ColourSchemeI globalColourScheme = alignViewport
            .getGlobalColourScheme();
    if (globalColourScheme != null)
    {
      globalColourScheme.setConsensus(hconsensus);
    }
  }

  /**
   * Get the Consensus annotation for the alignment
   * 
   * @return
   */
  protected AlignmentAnnotation getConsensusAnnotation()
  {
    return alignViewport.getAlignmentConsensusAnnotation();
  }

  /**
   * update the consensus annotation from the sequence profile data using
   * current visualization settings.
   */
  @Override
  public void updateAnnotation()
  {
    updateResultAnnotation(false);
  }

  public void updateResultAnnotation(boolean immediate)
  {
    AlignmentAnnotation consensus = getConsensusAnnotation();
    Hashtable[] hconsensus = getViewportConsensus();
    if (immediate || !calcMan.isWorking(this) && consensus != null
            && hconsensus != null)
    {
      deriveConsensus(consensus, hconsensus);
    }
  }

  /**
   * Convert the computed consensus data into the desired annotation for
   * display.
   * 
   * @param consensusAnnotation
   *          the annotation to be populated
   * @param consensusData
   *          the computed consensus data
   */
  protected void deriveConsensus(AlignmentAnnotation consensusAnnotation,
          Hashtable[] consensusData)
  {
    long nseq = getSequences().length;
    AAFrequency.completeConsensus(consensusAnnotation, consensusData, 0,
            consensusData.length, alignViewport.isIgnoreGapsConsensus(),
            alignViewport.isShowSequenceLogo(), nseq);
  }

  /**
   * Get the consensus data stored on the viewport.
   * 
   * @return
   */
  protected Hashtable[] getViewportConsensus()
  {
    return alignViewport.getSequenceConsensusHash();
  }
}
