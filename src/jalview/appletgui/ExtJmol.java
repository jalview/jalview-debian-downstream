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
package jalview.appletgui;

import java.awt.Container;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Vector;

import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.api.JmolViewer;

import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureRenderer;
import jalview.api.SequenceRenderer;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JalviewJmolBinding;

/**
 * bind an alignment view to an external Jmol instance.
 * 
 * @author JimP
 * 
 */
public class ExtJmol extends JalviewJmolBinding
{

  private AlignmentPanel ap;

  protected ExtJmol(jalview.appletgui.AlignFrame alframe,
          PDBEntry[] pdbentry, SequenceI[][] seq, String[][] chains,
          String protocol)
  {
    super(alframe.alignPanel.getStructureSelectionManager(), pdbentry, seq, chains, protocol);
  }

  public ExtJmol(JmolViewer viewer, AlignmentPanel alignPanel,
          SequenceI[][] seqs)
  {
    super(alignPanel.getStructureSelectionManager(), viewer);
    ap = alignPanel;
    this.sequence = seqs;
    notifyFileLoaded(null, null, null, null, 0);
  }

  public void updateColours(Object source)
  {

    // TODO Auto-generated method stub

  }

  public void showUrl(String arg0)
  {
    showUrl(arg0, "jmol");
  }

  public FeatureRenderer getFeatureRenderer(AlignmentViewPanel alignment)
  {
    AlignmentPanel ap = (AlignmentPanel)alignment;
    if (ap.av.showSequenceFeatures)
    {
      return ap.getFeatureRenderer();
    }
    else
    {
      return null;
    }
  }

  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
  {
    return ((AlignmentPanel)alignment).getSequenceRenderer();
  }

  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // ignore
  }

  public void sendConsoleEcho(String strEcho)
  {
    // ignore
  }

  public void sendConsoleMessage(String strStatus)
  {
    // ignore
  }

  public void showUrl(String url, String target)
  {
    ap.alignFrame.showURL(url, target);
  }

  public void refreshGUI()
  {
    // ignore
  }

  public void selectionChanged(BitSet arg0)
  {
    System.out.println(arg0);
  }

  public void refreshPdbEntries()
  {
    Vector pdbe = new Vector();
    Hashtable fileids = new Hashtable();
    SequenceI[] sq = ap.av.getAlignment().getSequencesArray();
    for (int s = 0; s < sq.length; s++)
    {
      Vector pdbids = sq[s].getPDBId();
      if (pdbids != null)
      {
        for (int pe = 0, peSize = pdbids.size(); pe < peSize; pe++)
        {
          PDBEntry pentry = (PDBEntry) pdbids.elementAt(pe);
          if (!fileids.containsKey(pentry.getId()))
          {
            pdbe.addElement(pentry);
          }
        }
      }
    }
    pdbentry = new PDBEntry[pdbe.size()];
    for (int pe = 0; pe < pdbe.size(); pe++)
    {
      pdbentry[pe] = (PDBEntry) pdbe.elementAt(pe);
    }
  }

  @Override
  public void showConsole(boolean show)
  {
    // This never gets called because we haven't overriden the associated Jmol's
    // console
    System.err
            .println("WARNING: unexpected call to ExtJmol's showConsole method. (showConsole="
                    + show);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(JmolViewer viewer2,
          Container consolePanel, String buttonsToShow)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void releaseUIResources()
  {
    ap = null;
    if (console != null)
    {
      try
      {
        console.setVisible(false);
      } catch (Error e)
      {
      } catch (Exception x)
      {
      }
      ;
      console = null;
    }

  }

  @Override
  public void releaseReferences(Object svl)
  {
    // TODO Auto-generated method stub
    
  }

}
