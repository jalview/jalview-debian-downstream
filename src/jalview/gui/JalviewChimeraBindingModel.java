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

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.rbvi.chimera.JalviewChimeraBinding;
import jalview.structure.StructureSelectionManager;

public class JalviewChimeraBindingModel extends JalviewChimeraBinding
{
  private ChimeraViewFrame cvf;

  private FeatureRenderer fr = null;

  public JalviewChimeraBindingModel(ChimeraViewFrame chimeraViewFrame,
          StructureSelectionManager ssm, PDBEntry[] pdbentry,
          SequenceI[][] sequenceIs, String[][] chains, String protocol)
  {
    super(ssm, pdbentry, sequenceIs, chains, protocol);
    cvf = chimeraViewFrame;
  }

  @Override
  public FeatureRenderer getFeatureRenderer(AlignmentViewPanel alignment)
  {
    AlignmentPanel ap = (alignment == null) ? cvf.getAlignmentPanel()
            : (AlignmentPanel) alignment;
    if (ap.av.isShowSequenceFeatures())
    {
      if (fr == null)
      {
        fr = (jalview.gui.FeatureRenderer) ap.cloneFeatureRenderer();
      }
      else
      {
        ap.updateFeatureRenderer(fr);
      }
    }

    return fr;
  }

  @Override
  public jalview.api.SequenceRenderer getSequenceRenderer(
          AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel) alignment).av);
  }

  @Override
  public void refreshGUI()
  {
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        cvf.updateTitleAndMenus();
        cvf.revalidate();
      }
    });
  }

  public void updateColours(Object source)
  {
    AlignmentPanel ap = (AlignmentPanel) source;
    // ignore events from panels not used to colour this view
    if (!cvf.isUsedforcolourby(ap))
    {
      return;
    }
    if (!isLoadingFromArchive())
    {
      colourBySequence(ap.av.isShowSequenceFeatures(), ap);
    }
  }

  @Override
  public void releaseReferences(Object svl)
  {
  }

  @Override
  protected void releaseUIResources()
  {
  }

  @Override
  public void refreshPdbEntries()
  {
  }

  /**
   * Send an asynchronous command to Chimera, in a new thread, optionally with
   * an 'in progress' message in a progress bar somewhere
   */
  protected void sendAsynchronousCommand(final String command,
          final String progressMsg)
  {
    Thread thread = new Thread(new Runnable()
    {

      @Override
      public void run()
      {
        long stm = cvf.startProgressBar(progressMsg);
        try
        {
          sendChimeraCommand(command, false);
        } finally
        {
          cvf.stopProgressBar(null, stm);
        }
      }
    });
    thread.start();

  }
}
