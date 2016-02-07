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
package jalview.appletgui;

import jalview.api.AlignmentViewPanel;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.ext.jmol.JalviewJmolBinding;
import jalview.structure.StructureSelectionManager;

import java.awt.Container;
import java.util.Map;

import javajs.awt.Dimension;

import org.jmol.api.JmolAppConsoleInterface;
import org.jmol.console.AppletConsole;
import org.jmol.java.BS;

class AppletJmolBinding extends JalviewJmolBinding
{

  /**
   * Window that contains the bound Jmol instance
   */
  private AppletJmol appletJmolBinding;

  public AppletJmolBinding(AppletJmol appletJmol,
          StructureSelectionManager sSm, PDBEntry[] pdbentry,
          SequenceI[][] seq, String[][] chains, String protocol)
  {
    super(sSm, pdbentry, seq, chains, protocol);
    appletJmolBinding = appletJmol;
  }

  @Override
  public jalview.api.FeatureRenderer getFeatureRenderer(
          AlignmentViewPanel alignment)
  {
    AlignmentPanel ap = (AlignmentPanel) alignment;
    if (appletJmolBinding.ap.av.isShowSequenceFeatures())
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

  @Override
  public jalview.api.SequenceRenderer getSequenceRenderer(
          AlignmentViewPanel alignment)
  {
    return new SequenceRenderer(((AlignmentPanel) alignment).av);
  }

  @Override
  public void sendConsoleEcho(String strEcho)
  {
    if (appletJmolBinding.scriptWindow == null)
    {
      appletJmolBinding.showConsole(true);
    }

    appletJmolBinding.addToHistory(strEcho);
  }

  @Override
  public void sendConsoleMessage(String strStatus)
  {
    if (appletJmolBinding.history != null && strStatus != null
            && !strStatus.equals("Script completed"))
    {
      appletJmolBinding.addToHistory(strStatus);
    }
  }

  @Override
  public void showUrl(String url, String target)
  {
    appletJmolBinding.ap.alignFrame.showURL(url, target);

  }

  @Override
  public void refreshGUI()
  {
    appletJmolBinding.updateTitleAndMenus();
  }

  public void updateColours(Object source)
  {
    AlignmentPanel ap = (AlignmentPanel) source;
    colourBySequence(ap);
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
    // jmolpopup = new JmolAwtPopup(); // is this used?
    // jmolpopup.jpiInitialize((viewer), menuName);
  }

  @Override
  public void notifyScriptTermination(String strStatus, int msWalltime)
  {
    // do nothing.
  }

  public void selectionChanged(BS arg0)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void refreshPdbEntries()
  {
    // noop
  }

  @Override
  public void showConsole(boolean show)
  {
    appletJmolBinding.showConsole(show);
  }

  @Override
  protected JmolAppConsoleInterface createJmolConsole(
          Container consolePanel, String buttonsToShow)
  {
    JmolAppConsoleInterface appc = new AppletConsole();
    appc.start(viewer);
    return appc;
  }

  @Override
  protected void releaseUIResources()
  {
    appletJmolBinding = null;
    closeConsole();
  }

  @Override
  public void releaseReferences(Object svl)
  {
  }

  @Override
  public Dimension resizeInnerPanel(String data)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    // TODO Auto-generated method stub
    return null;
  }
}
