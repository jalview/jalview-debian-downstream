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
import jalview.bin.Cache;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JalviewJmolBinding;
import jalview.structure.StructureSelectionManager;

import java.awt.Container;
import java.util.Map;

import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.java.BS;
import org.openscience.jmol.app.jmolpanel.console.AppConsole;

public class AppJmolBinding extends JalviewJmolBinding
{
  private AppJmol appJmolWindow;

  private FeatureRenderer fr = null;

  public AppJmolBinding(AppJmol appJmol, StructureSelectionManager sSm,
          PDBEntry[] pdbentry, SequenceI[][] sequenceIs, String[][] chains,
          String protocol)
  {
    super(sSm, pdbentry, sequenceIs, chains, protocol);
    appJmolWindow = appJmol;
  }

  @Override
  public FeatureRenderer getFeatureRenderer(AlignmentViewPanel alignment)
  {
    AlignmentPanel ap = (alignment == null) ? appJmolWindow
            .getAlignmentPanel() : (AlignmentPanel) alignment;
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
  public SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel) alignment).av);
  }

  @Override
  public void sendConsoleEcho(String strEcho)
  {
    if (console != null)
    {
      console.sendConsoleEcho(strEcho);
    }
  }

  @Override
  public void sendConsoleMessage(String strStatus)
  {
    if (console != null && strStatus != null)
    // && !strStatus.equals("Script completed"))
    // should we squash the script completed string ?
    {
      console.sendConsoleMessage(strStatus);
    }
  }

  @Override
  public void showUrl(String url, String target)
  {
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception e)
    {
      Cache.log.error("Failed to launch Jmol-associated url " + url, e);
      // TODO: 2.6 : warn user if browser was not configured.
    }
  }

  @Override
  public void refreshGUI()
  {
    // appJmolWindow.repaint();
    javax.swing.SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        appJmolWindow.updateTitleAndMenus();
        appJmolWindow.revalidate();
      }
    });
  }

  public void updateColours(Object source)
  {
    AlignmentPanel ap = (AlignmentPanel) source;
    // ignore events from panels not used to colour this view
    if (!appJmolWindow.isUsedforcolourby(ap))
    {
      return;
    }
    if (!isLoadingFromArchive())
    {
      colourBySequence(ap);
    }
  }

  @Override
  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // todo - script termination doesn't happen ?
    // if (console != null)
    // console.notifyScriptTermination(strStatus,
    // msWalltime);
  }

  public void showUrl(String url)
  {
    showUrl(url, "jmol");
  }

  public void newJmolPopup(String menuName)
  {
    // jmolpopup = new JmolAwtPopup();
    // jmolpopup.jpiInitialize((viewer), menuName);
  }

  @Override
  public void selectionChanged(BS arg0)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void refreshPdbEntries()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void showConsole(boolean b)
  {
    appJmolWindow.showConsole(b);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow)
  {
    viewer.setJmolCallbackListener(this);
    return new AppConsole(viewer, consolePanel, buttonsToShow);
  }

  @Override
  protected void releaseUIResources()
  {
    appJmolWindow = null;
    closeConsole();
  }

  @Override
  public void releaseReferences(Object svl)
  {
    if (svl instanceof SeqPanel)
    {
      appJmolWindow.removeAlignmentPanel(((SeqPanel) svl).ap);
    }
  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
