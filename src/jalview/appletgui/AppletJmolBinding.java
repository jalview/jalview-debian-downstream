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

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.structure.StructureSelectionManager;

import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.api.JmolViewer;
import org.jmol.applet.AppletConsole;
import org.jmol.popup.JmolPopup;

class AppletJmolBinding extends jalview.ext.jmol.JalviewJmolBinding
{

  /**
   * Window that contains the bound Jmol instance
   */
  private AppletJmol appletJmolBinding;

  public AppletJmolBinding(AppletJmol appletJmol, StructureSelectionManager sSm, PDBEntry[] pdbentry,
          SequenceI[][] seq, String[][] chains, String protocol)
  {
    super(sSm, pdbentry, seq, chains, protocol);
    appletJmolBinding = appletJmol;
  }

  public jalview.api.FeatureRenderer getFeatureRenderer(AlignmentViewPanel alignment)
  {
    AlignmentPanel ap = (AlignmentPanel)alignment;
    if (appletJmolBinding.ap.av.showSequenceFeatures)
    {
      if (appletJmolBinding.fr == null)
      {
        appletJmolBinding.fr = new jalview.appletgui.FeatureRenderer(
                appletJmolBinding.ap.av);
      }

      appletJmolBinding.fr
              .transferSettings(appletJmolBinding.ap.seqPanel.seqCanvas
                      .getFeatureRenderer());
    }

    return appletJmolBinding.fr;
  }

  public jalview.api.SequenceRenderer getSequenceRenderer(AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel)alignment).av);
  }

  public void sendConsoleEcho(String strEcho)
  {
    if (appletJmolBinding.scriptWindow == null)
      appletJmolBinding.showConsole(true);

    appletJmolBinding.history.append("\n" + strEcho);
  }

  public void sendConsoleMessage(String strStatus)
  {
    if (appletJmolBinding.history != null && strStatus != null
            && !strStatus.equals("Script completed"))
    {
      appletJmolBinding.history.append("\n" + strStatus);
    }
  }

  public void showUrl(String url, String target)
  {
    appletJmolBinding.ap.alignFrame.showURL(url, target);

  }

  public void refreshGUI()
  {
    appletJmolBinding.updateTitleAndMenus();
  }

  public void updateColours(Object source)
  {
    AlignmentPanel ap = (AlignmentPanel) source;
    colourBySequence(ap.av.getShowSequenceFeatures(), ap);
  }

  public void showUrl(String url)
  {
    try
    {
      appletJmolBinding.ap.av.applet.getAppletContext().showDocument(
              new java.net.URL(url), "jmol");
    } catch (java.net.MalformedURLException ex)
    {
    }
  }

  public void newJmolPopup(boolean translateLocale, String menuName,
          boolean asPopup)
  {

    jmolpopup = JmolPopup.newJmolPopup(viewer, translateLocale, menuName,
            asPopup);
  }

  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // do nothing.
  }

  public void selectionChanged(BitSet arg0)
  {
    // TODO Auto-generated method stub

  }

  public void refreshPdbEntries()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void showConsole(boolean show)
  {
    appletJmolBinding.showConsole(show);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(JmolViewer viewer2,
          Container consolePanel, String buttonsToShow)
  {
    return new AppletConsole(viewer2, consolePanel);
  }

  @Override
  protected void releaseUIResources()
  {
    appletJmolBinding = null;
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
  }

}
