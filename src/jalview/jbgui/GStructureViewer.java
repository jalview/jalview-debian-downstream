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
package jalview.jbgui;

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.util.MessageManager;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;

public abstract class GStructureViewer extends JInternalFrame implements
        JalviewStructureDisplayI
{
  // private AAStructureBindingModel bindingModel;

  protected JMenu savemenu = new JMenu();

  protected JMenu viewMenu = new JMenu();

  protected JMenu chainMenu = new JMenu();

  protected JMenu viewerActionMenu = new JMenu();

  protected JMenuItem alignStructs = new JMenuItem();

  protected JMenuItem fitToWindow = new JMenuItem();

  protected JRadioButtonMenuItem seqColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem chainColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem chargeColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem zappoColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem taylorColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem hydroColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem strandColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem helixColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem turnColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem buriedColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem purinePyrimidineColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem userColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem viewerColour = new JRadioButtonMenuItem();

  protected JMenuItem helpItem = new JMenuItem();

  protected JLabel statusBar = new JLabel();

  protected JPanel statusPanel = new JPanel();

  /**
   * Constructor
   */
  public GStructureViewer()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    JMenuBar menuBar = new JMenuBar();
    this.setJMenuBar(menuBar);

    JMenu fileMenu = new JMenu();
    fileMenu.setText(MessageManager.getString("action.file"));

    savemenu.setActionCommand(MessageManager.getString("action.save_image"));
    savemenu.setText(MessageManager.getString("action.save_as"));

    JMenuItem pdbFile = new JMenuItem();
    pdbFile.setText(MessageManager.getString("label.pdb_file"));
    pdbFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        pdbFile_actionPerformed(actionEvent);
      }
    });

    JMenuItem png = new JMenuItem();
    png.setText("PNG");
    png.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        png_actionPerformed(actionEvent);
      }
    });

    JMenuItem eps = new JMenuItem();
    eps.setText("EPS");
    eps.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        eps_actionPerformed(actionEvent);
      }
    });

    JMenuItem viewMapping = new JMenuItem();
    viewMapping.setText(MessageManager.getString("label.view_mapping"));
    viewMapping.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        viewMapping_actionPerformed(actionEvent);
      }
    });
    viewMenu.setText(MessageManager.getString("action.view"));

    chainMenu.setText(MessageManager.getString("action.show_chain"));

    fitToWindow.setText(MessageManager.getString("label.fit_to_window"));
    fitToWindow.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        fitToWindow_actionPerformed();
      }
    });

    JMenu colourMenu = new JMenu();
    colourMenu.setText(MessageManager.getString("label.colours"));

    JMenuItem backGround = new JMenuItem();
    backGround
            .setText(MessageManager.getString("action.background_colour"));
    backGround.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        backGround_actionPerformed(actionEvent);
      }
    });
    seqColour.setSelected(false);
    seqColour.setText(MessageManager.getString("action.by_sequence"));
    seqColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        seqColour_actionPerformed(actionEvent);
      }
    });
    chainColour.setText(MessageManager.getString("action.by_chain"));
    chainColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        chainColour_actionPerformed(actionEvent);
      }
    });
    chargeColour.setText(MessageManager.getString("label.charge_cysteine"));
    chargeColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        chargeColour_actionPerformed(actionEvent);
      }
    });
    zappoColour.setText(MessageManager.getString("label.zappo"));
    zappoColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        zappoColour_actionPerformed(actionEvent);
      }
    });
    taylorColour.setText(MessageManager.getString("label.taylor"));
    taylorColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        taylorColour_actionPerformed(actionEvent);
      }
    });
    hydroColour.setText(MessageManager.getString("label.hydrophobicity"));
    hydroColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        hydroColour_actionPerformed(actionEvent);
      }
    });
    strandColour.setText(MessageManager
            .getString("label.strand_propensity"));
    strandColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        strandColour_actionPerformed(actionEvent);
      }
    });
    helixColour.setText(MessageManager.getString("label.helix_propensity"));
    helixColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        helixColour_actionPerformed(actionEvent);
      }
    });
    turnColour.setText(MessageManager.getString("label.turn_propensity"));
    turnColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        turnColour_actionPerformed(actionEvent);
      }
    });
    buriedColour.setText(MessageManager.getString("label.buried_index"));
    buriedColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        buriedColour_actionPerformed(actionEvent);
      }
    });
    purinePyrimidineColour.setText(MessageManager
            .getString("label.purine_pyrimidine"));
    purinePyrimidineColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        purinePyrimidineColour_actionPerformed(actionEvent);
      }
    });

    userColour.setText(MessageManager.getString("action.user_defined"));
    userColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        userColour_actionPerformed(actionEvent);
      }
    });
    viewerColour.setSelected(false);
    viewerColour
            .setText(MessageManager.getString("label.colour_with_jmol"));
    viewerColour.setToolTipText(MessageManager
            .getString("label.let_jmol_manage_structure_colours"));
    viewerColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        viewerColour_actionPerformed(actionEvent);
      }
    });

    JMenu helpMenu = new JMenu();
    helpMenu.setText(MessageManager.getString("action.help"));
    helpItem.setText(MessageManager.getString("label.jmol_help"));
    helpItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        showHelp_actionPerformed(actionEvent);
      }
    });
    alignStructs
            .setText(MessageManager.getString("label.align_structures"));
    alignStructs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        alignStructs_actionPerformed(actionEvent);
      }
    });
    viewerActionMenu.setText(MessageManager.getString("label.jmol"));
    menuBar.add(fileMenu);
    menuBar.add(viewMenu);
    menuBar.add(colourMenu);
    menuBar.add(viewerActionMenu);
    viewerActionMenu.setVisible(false);
    menuBar.add(helpMenu);
    fileMenu.add(savemenu);
    fileMenu.add(viewMapping);
    savemenu.add(pdbFile);
    savemenu.add(png);
    savemenu.add(eps);
    viewMenu.add(chainMenu);

    colourMenu.add(seqColour);
    colourMenu.add(chainColour);
    colourMenu.add(chargeColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydroColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(purinePyrimidineColour);
    colourMenu.add(userColour);
    colourMenu.add(viewerColour);
    colourMenu.add(backGround);

    ButtonGroup colourButtons = new ButtonGroup();

    colourButtons.add(seqColour);
    colourButtons.add(chainColour);
    colourButtons.add(chargeColour);
    colourButtons.add(zappoColour);
    colourButtons.add(taylorColour);
    colourButtons.add(hydroColour);
    colourButtons.add(helixColour);
    colourButtons.add(strandColour);
    colourButtons.add(turnColour);
    colourButtons.add(buriedColour);
    colourButtons.add(purinePyrimidineColour);
    colourButtons.add(userColour);
    colourButtons.add(viewerColour);

    helpMenu.add(helpItem);
    viewerActionMenu.add(alignStructs);

    statusPanel.setLayout(new GridLayout());
    this.getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);
    statusPanel.add(statusBar, null);
  }

  protected void fitToWindow_actionPerformed()
  {
  }

  protected void highlightSelection_actionPerformed()
  {
  }

  protected void viewerColour_actionPerformed(ActionEvent actionEvent)
  {
  }

  protected void alignStructs_actionPerformed(ActionEvent actionEvent)
  {
  }

  public void pdbFile_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void png_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void eps_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void viewMapping_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void seqColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void chainColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void chargeColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void zappoColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void taylorColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void hydroColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void helixColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void strandColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void turnColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void buriedColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void purinePyrimidineColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void userColour_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void backGround_actionPerformed(ActionEvent actionEvent)
  {

  }

  public void showHelp_actionPerformed(ActionEvent actionEvent)
  {

  }

  // {
  // return bindingModel;
  // }

  // public void setBindingModel(AAStructureBindingModel bindingModel)
  // {
  // this.bindingModel = bindingModel;
  // }

}
