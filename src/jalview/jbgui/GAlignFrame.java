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

import jalview.analysis.AnnotationSorter.SequenceAnnotationOrder;
import jalview.api.SplitContainerI;
import jalview.bin.Cache;
import jalview.gui.JvSwingUtils;
import jalview.gui.Preferences;
import jalview.schemes.ColourSchemeProperty;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class GAlignFrame extends JInternalFrame
{
  protected JMenuBar alignFrameMenuBar = new JMenuBar();

  protected JMenuItem closeMenuItem = new JMenuItem();

  protected JMenu colourMenu = new JMenu();

  protected JMenu webService = new JMenu();

  protected JMenuItem webServiceNoServices;

  public JCheckBoxMenuItem viewBoxesMenuItem = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem viewTextMenuItem = new JCheckBoxMenuItem();

  protected JMenu sortByAnnotScore = new JMenu();

  public JLabel statusBar = new JLabel();

  protected JMenu outputTextboxMenu = new JMenu();

  protected JRadioButtonMenuItem clustalColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem zappoColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem taylorColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem hydrophobicityColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem helixColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem strandColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem turnColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem buriedColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem userDefinedColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem PIDColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem BLOSUM62Colour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem nucleotideColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem purinePyrimidineColour = new JRadioButtonMenuItem();

  protected JRadioButtonMenuItem RNAInteractionColour = new JRadioButtonMenuItem();

  // protected JRadioButtonMenuItem covariationColour = new
  // JRadioButtonMenuItem();

  protected JRadioButtonMenuItem tcoffeeColour = new JRadioButtonMenuItem();

  public JCheckBoxMenuItem annotationPanelMenuItem = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem colourTextMenuItem = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem showNonconservedMenuItem = new JCheckBoxMenuItem();

  protected JMenuItem undoMenuItem = new JMenuItem();

  protected JMenuItem redoMenuItem = new JMenuItem();

  public JCheckBoxMenuItem conservationMenuItem = new JCheckBoxMenuItem();

  JRadioButtonMenuItem noColourmenuItem = new JRadioButtonMenuItem();

  public JCheckBoxMenuItem wrapMenuItem = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem renderGapsMenuItem = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem abovePIDThreshold = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem showSeqFeatures = new JCheckBoxMenuItem();

  JMenuItem copy = new JMenuItem();

  JMenuItem cut = new JMenuItem();

  JMenu pasteMenu = new JMenu();

  public JCheckBoxMenuItem applyToAllGroups = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem seqLimits = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem scaleAbove = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem scaleLeft = new JCheckBoxMenuItem();

  public JCheckBoxMenuItem scaleRight = new JCheckBoxMenuItem();

  protected JMenuItem modifyConservation = new JMenuItem();

  protected JMenu sortByTreeMenu = new JMenu();

  protected JMenu sort = new JMenu();

  protected JMenu calculateTree = new JMenu();

  protected JCheckBoxMenuItem padGapsMenuitem = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showNpFeatsMenuitem = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showDbRefsMenuitem = new JCheckBoxMenuItem();

  protected ButtonGroup colours = new ButtonGroup();

  protected JMenuItem showTranslation = new JMenuItem();

  protected JMenuItem showReverse = new JMenuItem();

  protected JMenuItem showReverseComplement = new JMenuItem();

  protected JMenu showProducts = new JMenu();

  protected JMenuItem runGroovy = new JMenuItem();

  protected JMenuItem rnahelicesColour = new JMenuItem();

  protected JCheckBoxMenuItem autoCalculate = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem sortByTree = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem listenToViewSelections = new JCheckBoxMenuItem();

  protected JPanel statusPanel = new JPanel();

  protected JMenuItem showAllSeqAnnotations = new JMenuItem();

  protected JMenuItem hideAllSeqAnnotations = new JMenuItem();

  protected JMenuItem showAllAlAnnotations = new JMenuItem();

  protected JMenuItem hideAllAlAnnotations = new JMenuItem();

  protected JCheckBoxMenuItem showComplementMenuItem = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem hiddenMarkers = new JCheckBoxMenuItem();

  protected JTabbedPane tabbedPane = new JTabbedPane();

  protected JMenuItem reload = new JMenuItem();

  protected JMenu formatMenu = new JMenu();

  protected JCheckBoxMenuItem idRightAlign = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem centreColumnLabelsMenuItem = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem followHighlightMenuItem = new JCheckBoxMenuItem();

  protected JMenuItem gatherViews = new JMenuItem();

  protected JMenuItem expandViews = new JMenuItem();

  protected JCheckBoxMenuItem showGroupConsensus = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showGroupConservation = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showConsensusHistogram = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem showSequenceLogo = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem normaliseSequenceLogo = new JCheckBoxMenuItem();

  protected JCheckBoxMenuItem applyAutoAnnotationSettings = new JCheckBoxMenuItem();

  private SequenceAnnotationOrder annotationSortOrder;

  private boolean showAutoCalculatedAbove = false;

  private Map<KeyStroke, JMenuItem> accelerators = new HashMap<KeyStroke, JMenuItem>();

  private SplitContainerI splitFrame;

  public GAlignFrame()
  {
    try
    {
      jbInit();
      setJMenuBar(alignFrameMenuBar);

      // dynamically fill save as menu with available formats
      for (int i = 0; i < jalview.io.FormatAdapter.WRITEABLE_FORMATS.length; i++)
      {
        JMenuItem item = new JMenuItem(
                jalview.io.FormatAdapter.WRITEABLE_FORMATS[i]);

        item.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            outputText_actionPerformed(e);
          }
        });

        outputTextboxMenu.add(item);
      }
    } catch (Exception e)
    {
      System.err.println(e.toString());
    }

    if (!jalview.util.Platform.isAMac())
    {
      closeMenuItem.setMnemonic('C');
      outputTextboxMenu.setMnemonic('T');
      undoMenuItem.setMnemonic('Z');
      redoMenuItem.setMnemonic('0');
      copy.setMnemonic('C');
      cut.setMnemonic('U');
      pasteMenu.setMnemonic('P');
      reload.setMnemonic('R');
    }

    if (jalview.gui.UserDefinedColours.getUserColourSchemes() != null)
    {
      java.util.Enumeration userColours = jalview.gui.UserDefinedColours
              .getUserColourSchemes().keys();

      while (userColours.hasMoreElements())
      {
        final JRadioButtonMenuItem radioItem = new JRadioButtonMenuItem(
                userColours.nextElement().toString());
        radioItem.setName("USER_DEFINED");
        radioItem.addMouseListener(new MouseAdapter()
        {
          @Override
          public void mousePressed(MouseEvent evt)
          {
            if (evt.isPopupTrigger()) // Mac
            {
              offerRemoval(radioItem);
            }
          }

          @Override
          public void mouseReleased(MouseEvent evt)
          {
            if (evt.isPopupTrigger()) // Windows
            {
              offerRemoval(radioItem);
            }
          }

          /**
           * @param radioItem
           */
          void offerRemoval(final JRadioButtonMenuItem radioItem)
          {
            radioItem.removeActionListener(radioItem.getActionListeners()[0]);

            int option = JOptionPane.showInternalConfirmDialog(
                    jalview.gui.Desktop.desktop, MessageManager
                            .getString("label.remove_from_default_list"),
                    MessageManager
                            .getString("label.remove_user_defined_colour"),
                    JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION)
            {
              jalview.gui.UserDefinedColours
                      .removeColourFromDefaults(radioItem.getText());
              colourMenu.remove(radioItem);
            }
            else
            {
              radioItem.addActionListener(new ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                  userDefinedColour_actionPerformed(evt);
                }
              });
            }
          }
        });
        radioItem.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent evt)
          {
            userDefinedColour_actionPerformed(evt);
          }
        });
        colourMenu.insert(radioItem, 15);
        colours.add(radioItem);
      }
    }
    colours.add(noColourmenuItem);
    colours.add(clustalColour);
    colours.add(zappoColour);
    colours.add(taylorColour);
    colours.add(hydrophobicityColour);
    colours.add(helixColour);
    colours.add(strandColour);
    colours.add(turnColour);
    colours.add(buriedColour);
    colours.add(userDefinedColour);
    colours.add(PIDColour);
    colours.add(BLOSUM62Colour);
    colours.add(nucleotideColour);
    colours.add(purinePyrimidineColour);
    // colours.add(covariationColour);
    colours.add(tcoffeeColour);
    colours.add(RNAInteractionColour);
    setColourSelected(jalview.bin.Cache.getDefault(
            Preferences.DEFAULT_COLOUR, "None"));
  }

  public void setColourSelected(String defaultColour)
  {

    if (defaultColour != null)
    {
      int index = ColourSchemeProperty
              .getColourIndexFromName(defaultColour);

      switch (index)
      {
      case ColourSchemeProperty.CLUSTAL:
        clustalColour.setSelected(true);

        break;

      case ColourSchemeProperty.BLOSUM:
        BLOSUM62Colour.setSelected(true);

        break;

      case ColourSchemeProperty.PID:
        PIDColour.setSelected(true);

        break;

      case ColourSchemeProperty.ZAPPO:
        zappoColour.setSelected(true);

        break;

      case ColourSchemeProperty.TAYLOR:
        taylorColour.setSelected(true);
        break;

      case ColourSchemeProperty.HYDROPHOBIC:
        hydrophobicityColour.setSelected(true);

        break;

      case ColourSchemeProperty.HELIX:
        helixColour.setSelected(true);

        break;

      case ColourSchemeProperty.STRAND:
        strandColour.setSelected(true);

        break;

      case ColourSchemeProperty.TURN:
        turnColour.setSelected(true);

        break;

      case ColourSchemeProperty.BURIED:
        buriedColour.setSelected(true);

        break;

      case ColourSchemeProperty.NUCLEOTIDE:
        nucleotideColour.setSelected(true);

        break;

      case ColourSchemeProperty.TCOFFEE:
        tcoffeeColour.setSelected(true);
        break;

      case ColourSchemeProperty.PURINEPYRIMIDINE:
        purinePyrimidineColour.setSelected(true);

        break;

      case ColourSchemeProperty.RNAINTERACTION:
        RNAInteractionColour.setSelected(true);

        break;
      /*
       * case ColourSchemeProperty.COVARIATION:
       * covariationColour.setSelected(true);
       * 
       * break;
       */
      case ColourSchemeProperty.USER_DEFINED:
        userDefinedColour.setSelected(true);

        break;
      case ColourSchemeProperty.NONE:
      default:
        noColourmenuItem.setSelected(true);
        break;

      }
    }

  }

  private void jbInit() throws Exception
  {
    JMenuItem saveAs = new JMenuItem(
            MessageManager.getString("action.save_as"));
    ActionListener al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        saveAs_actionPerformed(e);
      }
    };
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask()
            | KeyEvent.SHIFT_MASK, false);
    addMenuActionAndAccelerator(keyStroke, saveAs, al);

    closeMenuItem.setText(MessageManager.getString("action.close"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeMenuItem_actionPerformed(false);
      }
    };
    addMenuActionAndAccelerator(keyStroke, closeMenuItem, al);

    JMenu editMenu = new JMenu(MessageManager.getString("action.edit"));
    JMenu viewMenu = new JMenu(MessageManager.getString("action.view"));
    JMenu annotationsMenu = new JMenu(
            MessageManager.getString("action.annotations"));
    JMenu showMenu = new JMenu(MessageManager.getString("action.show"));
    colourMenu.setText(MessageManager.getString("action.colour"));
    JMenu calculateMenu = new JMenu(
            MessageManager.getString("action.calculate"));
    webService.setText(MessageManager.getString("action.web_service"));
    JMenuItem selectAllSequenceMenuItem = new JMenuItem(
            MessageManager.getString("action.select_all"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        selectAllSequenceMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, selectAllSequenceMenuItem, al);

    JMenuItem deselectAllSequenceMenuItem = new JMenuItem(
            MessageManager.getString("action.deselect_all"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        deselectAllSequenceMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, deselectAllSequenceMenuItem, al);

    JMenuItem invertSequenceMenuItem = new JMenuItem(
            MessageManager.getString("action.invert_sequence_selection"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        invertSequenceMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, invertSequenceMenuItem, al);

    JMenuItem grpsFromSelection = new JMenuItem(
            MessageManager.getString("action.make_groups_selection"));
    grpsFromSelection.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        makeGrpsFromSelection_actionPerformed(e);
      }
    });
    JMenuItem expandAlignment = new JMenuItem(
            MessageManager.getString("action.view_flanking_regions"));
    expandAlignment.setToolTipText(MessageManager
            .getString("label.view_flanking_regions"));
    expandAlignment.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        expand_newalign(e);
      }
    });
    JMenuItem remove2LeftMenuItem = new JMenuItem(
            MessageManager.getString("action.remove_left"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_L, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        remove2LeftMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, remove2LeftMenuItem, al);

    JMenuItem remove2RightMenuItem = new JMenuItem(
            MessageManager.getString("action.remove_right"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        remove2RightMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, remove2RightMenuItem, al);

    JMenuItem removeGappedColumnMenuItem = new JMenuItem(
            MessageManager.getString("action.remove_empty_columns"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        removeGappedColumnMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, removeGappedColumnMenuItem, al);

    JMenuItem removeAllGapsMenuItem = new JMenuItem(
            MessageManager.getString("action.remove_all_gaps"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask()
            | KeyEvent.SHIFT_MASK, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        removeAllGapsMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, removeAllGapsMenuItem, al);

    JMenuItem justifyLeftMenuItem = new JMenuItem(
            MessageManager.getString("action.left_justify_alignment"));
    justifyLeftMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        justifyLeftMenuItem_actionPerformed(e);
      }
    });
    JMenuItem justifyRightMenuItem = new JMenuItem(
            MessageManager.getString("action.right_justify_alignment"));
    justifyRightMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        justifyRightMenuItem_actionPerformed(e);
      }
    });
    viewBoxesMenuItem.setText(MessageManager.getString("action.boxes"));
    viewBoxesMenuItem.setState(true);
    viewBoxesMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        viewBoxesMenuItem_actionPerformed(e);
      }
    });
    viewTextMenuItem.setText(MessageManager.getString("action.text"));
    viewTextMenuItem.setState(true);
    viewTextMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        viewTextMenuItem_actionPerformed(e);
      }
    });
    showNonconservedMenuItem.setText(MessageManager
            .getString("label.show_non_conversed"));
    showNonconservedMenuItem.setState(false);
    showNonconservedMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showUnconservedMenuItem_actionPerformed(e);
      }
    });
    JMenuItem sortPairwiseMenuItem = new JMenuItem(
            MessageManager.getString("action.by_pairwise_id"));
    sortPairwiseMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortPairwiseMenuItem_actionPerformed(e);
      }
    });
    JMenuItem sortIDMenuItem = new JMenuItem(
            MessageManager.getString("action.by_id"));
    sortIDMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortIDMenuItem_actionPerformed(e);
      }
    });
    JMenuItem sortLengthMenuItem = new JMenuItem(
            MessageManager.getString("action.by_length"));
    sortLengthMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortLengthMenuItem_actionPerformed(e);
      }
    });
    JMenuItem sortGroupMenuItem = new JMenuItem(
            MessageManager.getString("action.by_group"));
    sortGroupMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortGroupMenuItem_actionPerformed(e);
      }
    });

    JMenuItem removeRedundancyMenuItem = new JMenuItem(
            MessageManager.getString("action.remove_redundancy"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        removeRedundancyMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, removeRedundancyMenuItem, al);

    JMenuItem pairwiseAlignmentMenuItem = new JMenuItem(
            MessageManager.getString("action.pairwise_alignment"));
    pairwiseAlignmentMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pairwiseAlignmentMenuItem_actionPerformed(e);
      }
    });
    JMenuItem PCAMenuItem = new JMenuItem(
            MessageManager.getString("label.principal_component_analysis"));
    PCAMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        PCAMenuItem_actionPerformed(e);
      }
    });
    JMenuItem averageDistanceTreeMenuItem = new JMenuItem(
            MessageManager.getString("label.average_distance_identity"));
    averageDistanceTreeMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        averageDistanceTreeMenuItem_actionPerformed(e);
      }
    });
    JMenuItem neighbourTreeMenuItem = new JMenuItem(
            MessageManager.getString("label.neighbour_joining_identity"));
    neighbourTreeMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        neighbourTreeMenuItem_actionPerformed(e);
      }
    });

    this.getContentPane().setLayout(new BorderLayout());
    alignFrameMenuBar.setFont(new java.awt.Font("Verdana", 0, 11));
    statusBar.setBackground(Color.white);
    statusBar.setFont(new java.awt.Font("Verdana", 0, 11));
    statusBar.setBorder(BorderFactory.createLineBorder(Color.black));
    statusBar.setText(MessageManager.getString("label.status_bar"));
    outputTextboxMenu.setText(MessageManager
            .getString("label.out_to_textbox"));
    clustalColour.setText(MessageManager.getString("label.clustalx"));
    clustalColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        clustalColour_actionPerformed(e);
      }
    });
    zappoColour.setText(MessageManager.getString("label.zappo"));
    zappoColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        zappoColour_actionPerformed(e);
      }
    });
    taylorColour.setText(MessageManager.getString("label.taylor"));
    taylorColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        taylorColour_actionPerformed(e);
      }
    });
    hydrophobicityColour.setText(MessageManager
            .getString("label.hydrophobicity"));
    hydrophobicityColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hydrophobicityColour_actionPerformed(e);
      }
    });
    helixColour.setText(MessageManager.getString("label.helix_propensity"));
    helixColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        helixColour_actionPerformed(e);
      }
    });
    strandColour.setText(MessageManager
            .getString("label.strand_propensity"));
    strandColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        strandColour_actionPerformed(e);
      }
    });
    turnColour.setText(MessageManager.getString("label.turn_propensity"));
    turnColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        turnColour_actionPerformed(e);
      }
    });
    buriedColour.setText(MessageManager.getString("label.buried_index"));
    buriedColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        buriedColour_actionPerformed(e);
      }
    });
    userDefinedColour.setText(MessageManager
            .getString("action.user_defined"));
    userDefinedColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        userDefinedColour_actionPerformed(e);
      }
    });
    PIDColour
            .setText(MessageManager.getString("label.percentage_identity"));
    PIDColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        PIDColour_actionPerformed(e);
      }
    });
    BLOSUM62Colour
            .setText(MessageManager.getString("label.blosum62_score"));
    BLOSUM62Colour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        BLOSUM62Colour_actionPerformed(e);
      }
    });
    nucleotideColour.setText(MessageManager.getString("label.nucleotide"));
    nucleotideColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        nucleotideColour_actionPerformed(e);
      }
    });

    purinePyrimidineColour.setText(MessageManager
            .getString("label.purine_pyrimidine"));
    purinePyrimidineColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        purinePyrimidineColour_actionPerformed(e);
      }
    });

    RNAInteractionColour.setText("RNA Interaction type");
    RNAInteractionColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        RNAInteractionColour_actionPerformed(e);
      }
    });
    /*
     * covariationColour.setText("Covariation");
     * covariationColour.addActionListener(new ActionListener() { public void
     * actionPerformed(ActionEvent e) { covariationColour_actionPerformed(e); }
     * });
     */

    JMenuItem avDistanceTreeBlosumMenuItem = new JMenuItem(
            MessageManager.getString("label.average_distance_bloslum62"));
    avDistanceTreeBlosumMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        avTreeBlosumMenuItem_actionPerformed(e);
      }
    });
    JMenuItem njTreeBlosumMenuItem = new JMenuItem(
            MessageManager.getString("label.neighbour_blosum62"));
    njTreeBlosumMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        njTreeBlosumMenuItem_actionPerformed(e);
      }
    });
    annotationPanelMenuItem.setActionCommand("");
    annotationPanelMenuItem.setText(MessageManager
            .getString("label.show_annotations"));
    annotationPanelMenuItem.setState(Cache.getDefault("SHOW_ANNOTATIONS",
            true));
    annotationPanelMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotationPanelMenuItem_actionPerformed(e);
      }
    });
    showAllAlAnnotations.setText(MessageManager
            .getString("label.show_all_al_annotations"));
    final boolean isAnnotationPanelShown = annotationPanelMenuItem
            .getState();
    showAllAlAnnotations.setEnabled(isAnnotationPanelShown);
    showAllAlAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showAllAnnotations_actionPerformed(false, true);
      }
    });
    hideAllAlAnnotations.setText(MessageManager
            .getString("label.hide_all_al_annotations"));
    hideAllAlAnnotations.setEnabled(isAnnotationPanelShown);
    hideAllAlAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideAllAnnotations_actionPerformed(false, true);
      }
    });
    showAllSeqAnnotations.setText(MessageManager
            .getString("label.show_all_seq_annotations"));
    showAllSeqAnnotations.setEnabled(isAnnotationPanelShown);
    showAllSeqAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showAllAnnotations_actionPerformed(true, false);
      }
    });
    hideAllSeqAnnotations.setText(MessageManager
            .getString("label.hide_all_seq_annotations"));
    hideAllSeqAnnotations.setEnabled(isAnnotationPanelShown);
    hideAllSeqAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideAllAnnotations_actionPerformed(true, false);
      }
    });
    SequenceAnnotationOrder sortAnnotationsBy = SequenceAnnotationOrder
            .valueOf(Cache.getDefault(Preferences.SORT_ANNOTATIONS,
                    SequenceAnnotationOrder.NONE.name()));
    final JCheckBoxMenuItem sortAnnBySequence = new JCheckBoxMenuItem(
            MessageManager.getString("label.sort_annotations_by_sequence"));
    final JCheckBoxMenuItem sortAnnByLabel = new JCheckBoxMenuItem(
            MessageManager.getString("label.sort_annotations_by_label"));

    sortAnnBySequence
            .setSelected(sortAnnotationsBy == SequenceAnnotationOrder.SEQUENCE_AND_LABEL);
    sortAnnBySequence.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        boolean newState = sortAnnBySequence.getState();
        sortAnnByLabel.setSelected(false);
        setAnnotationSortOrder(newState ? SequenceAnnotationOrder.SEQUENCE_AND_LABEL
                : SequenceAnnotationOrder.NONE);
        sortAnnotations_actionPerformed();
      }
    });
    sortAnnByLabel
            .setSelected(sortAnnotationsBy == SequenceAnnotationOrder.LABEL_AND_SEQUENCE);
    sortAnnByLabel.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        boolean newState = sortAnnByLabel.getState();
        sortAnnBySequence.setSelected(false);
        setAnnotationSortOrder(newState ? SequenceAnnotationOrder.LABEL_AND_SEQUENCE
                : SequenceAnnotationOrder.NONE);
        sortAnnotations_actionPerformed();
      }
    });
    colourTextMenuItem.setText(MessageManager
            .getString("label.colour_text"));
    colourTextMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        colourTextMenuItem_actionPerformed(e);
      }
    });

    JMenuItem htmlMenuItem = new JMenuItem(
            MessageManager.getString("label.html"));
    htmlMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        htmlMenuItem_actionPerformed(e);
      }
    });

    JMenuItem createBioJS = new JMenuItem(
            MessageManager.getString("label.biojs_html_export"));
    createBioJS.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        bioJSMenuItem_actionPerformed(e);
      }
    });

    JMenuItem overviewMenuItem = new JMenuItem(
            MessageManager.getString("label.overview_window"));
    overviewMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        overviewMenuItem_actionPerformed(e);
      }
    });

    undoMenuItem.setEnabled(false);
    undoMenuItem.setText(MessageManager.getString("action.undo"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        undoMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, undoMenuItem, al);

    redoMenuItem.setEnabled(false);
    redoMenuItem.setText(MessageManager.getString("action.redo"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        redoMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, redoMenuItem, al);

    conservationMenuItem.setText(MessageManager
            .getString("action.by_conservation"));
    conservationMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        conservationMenuItem_actionPerformed(e);
      }
    });
    noColourmenuItem.setText(MessageManager.getString("label.none"));
    noColourmenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        noColourmenuItem_actionPerformed(e);
      }
    });
    wrapMenuItem.setText(MessageManager.getString("label.wrap"));
    wrapMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        wrapMenuItem_actionPerformed(e);
      }
    });

    JMenuItem printMenuItem = new JMenuItem(
            MessageManager.getString("action.print"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        printMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, printMenuItem, al);

    renderGapsMenuItem
            .setText(MessageManager.getString("action.show_gaps"));
    renderGapsMenuItem.setState(true);
    renderGapsMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        renderGapsMenuItem_actionPerformed(e);
      }
    });

    JMenuItem findMenuItem = new JMenuItem(
            MessageManager.getString("action.find"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    findMenuItem.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.find_tip")));
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        findMenuItem_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, findMenuItem, al);

    abovePIDThreshold.setText(MessageManager
            .getString("label.above_identity_threshold"));
    abovePIDThreshold.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        abovePIDThreshold_actionPerformed(e);
      }
    });
    showSeqFeatures.setText(MessageManager
            .getString("label.show_sequence_features"));
    showSeqFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        showSeqFeatures_actionPerformed(actionEvent);
      }
    });
    /*
     * showSeqFeaturesHeight.setText("Vary Sequence Feature Height");
     * showSeqFeaturesHeight.addActionListener(new ActionListener() { public
     * void actionPerformed(ActionEvent actionEvent) {
     * showSeqFeaturesHeight_actionPerformed(actionEvent); } });
     */
    showDbRefsMenuitem.setText(MessageManager
            .getString("label.show_database_refs"));
    showDbRefsMenuitem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showDbRefs_actionPerformed(e);
      }

    });
    showNpFeatsMenuitem.setText(MessageManager
            .getString("label.show_non_positional_features"));
    showNpFeatsMenuitem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showNpFeats_actionPerformed(e);
      }

    });
    showGroupConservation.setText(MessageManager
            .getString("label.group_conservation"));
    showGroupConservation.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showGroupConservation_actionPerformed(e);
      }

    });

    showGroupConsensus.setText(MessageManager
            .getString("label.group_consensus"));
    showGroupConsensus.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showGroupConsensus_actionPerformed(e);
      }

    });
    showConsensusHistogram.setText(MessageManager
            .getString("label.show_consensus_histogram"));
    showConsensusHistogram.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showConsensusHistogram_actionPerformed(e);
      }

    });
    showSequenceLogo.setText(MessageManager
            .getString("label.show_consensus_logo"));
    showSequenceLogo.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        showSequenceLogo_actionPerformed(e);
      }

    });
    normaliseSequenceLogo.setText(MessageManager
            .getString("label.norm_consensus_logo"));
    normaliseSequenceLogo.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        normaliseSequenceLogo_actionPerformed(e);
      }

    });
    applyAutoAnnotationSettings.setText(MessageManager
            .getString("label.apply_all_groups"));
    applyAutoAnnotationSettings.setState(false);
    applyAutoAnnotationSettings.setVisible(true);
    applyAutoAnnotationSettings.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyAutoAnnotationSettings_actionPerformed(e);
      }
    });

    ButtonGroup buttonGroup = new ButtonGroup();
    final JRadioButtonMenuItem showAutoFirst = new JRadioButtonMenuItem(
            MessageManager.getString("label.show_first"));
    final JRadioButtonMenuItem showAutoLast = new JRadioButtonMenuItem(
            MessageManager.getString("label.show_last"));
    buttonGroup.add(showAutoFirst);
    buttonGroup.add(showAutoLast);
    final boolean autoFirst = Cache.getDefault(
            Preferences.SHOW_AUTOCALC_ABOVE, false);
    showAutoFirst.setSelected(autoFirst);
    setShowAutoCalculatedAbove(autoFirst);
    showAutoFirst.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        setShowAutoCalculatedAbove(showAutoFirst.isSelected());
        sortAnnotations_actionPerformed();
      }
    });
    showAutoLast.setSelected(!showAutoFirst.isSelected());
    showAutoLast.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        setShowAutoCalculatedAbove(!showAutoLast.isSelected());
        sortAnnotations_actionPerformed();
      }
    });

    nucleotideColour.setText(MessageManager.getString("label.nucleotide"));
    nucleotideColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        nucleotideColour_actionPerformed(e);
      }
    });

    tcoffeeColour.setText(MessageManager.getString("label.tcoffee_scores"));
    tcoffeeColour.setEnabled(false);
    tcoffeeColour.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        tcoffeeColorScheme_actionPerformed(e);
      }
    });

    JMenuItem deleteGroups = new JMenuItem(
            MessageManager.getString("action.undefine_groups"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_U, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        deleteGroups_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, deleteGroups, al);

    JMenuItem createGroup = new JMenuItem(
            MessageManager.getString("action.create_groups"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createGroup_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, createGroup, al);

    JMenuItem unGroup = new JMenuItem(
            MessageManager.getString("action.remove_group"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask()
            | KeyEvent.SHIFT_MASK, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        unGroup_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, unGroup, al);

    copy.setText(MessageManager.getString("action.copy"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);

    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        copy_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, copy, al);

    cut.setText(MessageManager.getString("action.cut"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        cut_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, cut, al);

    JMenuItem delete = new JMenuItem(
            MessageManager.getString("action.delete"));
    delete.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        delete_actionPerformed(e);
      }
    });

    pasteMenu.setText(MessageManager.getString("action.paste"));
    JMenuItem pasteNew = new JMenuItem(
            MessageManager.getString("label.to_new_alignment"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask()
            | KeyEvent.SHIFT_MASK, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pasteNew_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, pasteNew, al);

    JMenuItem pasteThis = new JMenuItem(
            MessageManager.getString("label.to_this_alignment"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pasteThis_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, pasteThis, al);

    applyToAllGroups.setText(MessageManager
            .getString("label.apply_colour_to_all_groups"));
    applyToAllGroups.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        applyToAllGroups_actionPerformed(e);
      }
    });
    JMenuItem createPNG = new JMenuItem("PNG");
    createPNG.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createPNG(null);
      }
    });
    createPNG.setActionCommand(MessageManager
            .getString("label.save_png_image"));

    JMenuItem font = new JMenuItem(MessageManager.getString("action.font"));
    font.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        font_actionPerformed(e);
      }
    });
    seqLimits.setText(MessageManager
            .getString("label.show_sequence_limits"));
    seqLimits.setState(jalview.bin.Cache.getDefault("SHOW_JVSUFFIX", true));
    seqLimits.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        seqLimit_actionPerformed(e);
      }
    });
    JMenuItem epsFile = new JMenuItem("EPS");
    epsFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createEPS(null);
      }
    });

    JMenuItem createSVG = new JMenuItem("SVG");
    createSVG.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        createSVG(null);
      }
    });

    JMenuItem loadTreeMenuItem = new JMenuItem(
            MessageManager.getString("label.load_associated_tree"));
    loadTreeMenuItem.setActionCommand(MessageManager
            .getString("label.load_tree_for_sequence_set"));
    loadTreeMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        loadTreeMenuItem_actionPerformed(e);
      }
    });

    scaleAbove.setVisible(false);
    scaleAbove.setText(MessageManager.getString("action.scale_above"));
    scaleAbove.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        scaleAbove_actionPerformed(e);
      }
    });
    scaleLeft.setVisible(false);
    scaleLeft.setSelected(true);
    scaleLeft.setText(MessageManager.getString("action.scale_left"));
    scaleLeft.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        scaleLeft_actionPerformed(e);
      }
    });
    scaleRight.setVisible(false);
    scaleRight.setSelected(true);
    scaleRight.setText(MessageManager.getString("action.scale_right"));
    scaleRight.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        scaleRight_actionPerformed(e);
      }
    });
    centreColumnLabelsMenuItem.setVisible(true);
    centreColumnLabelsMenuItem.setState(false);
    centreColumnLabelsMenuItem.setText(MessageManager
            .getString("label.centre_column_labels"));
    centreColumnLabelsMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        centreColumnLabels_actionPerformed(e);
      }
    });
    followHighlightMenuItem.setVisible(true);
    followHighlightMenuItem.setState(true);
    followHighlightMenuItem.setText(MessageManager
            .getString("label.automatic_scrolling"));
    followHighlightMenuItem.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent e)
      {
        followHighlight_actionPerformed();
      }

    });

    JMenuItem modifyPID = new JMenuItem(
            MessageManager.getString("label.modify_identity_threshold"));
    modifyPID.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        modifyPID_actionPerformed(e);
      }
    });
    modifyConservation.setText(MessageManager
            .getString("label.modify_conservation_threshold"));
    modifyConservation.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        modifyConservation_actionPerformed(e);
      }
    });
    sortByTreeMenu
            .setText(MessageManager.getString("action.by_tree_order"));
    sort.setText(MessageManager.getString("action.sort"));
    sort.addMenuListener(new MenuListener()
    {
      @Override
      public void menuSelected(MenuEvent e)
      {
        buildTreeMenu();
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
      }
    });
    sortByAnnotScore.setText(MessageManager
            .getString("label.sort_by_score"));
    sort.add(sortByAnnotScore);
    sort.addMenuListener(new javax.swing.event.MenuListener()
    {

      @Override
      public void menuCanceled(MenuEvent e)
      {
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
      }

      @Override
      public void menuSelected(MenuEvent e)
      {
        buildSortByAnnotationScoresMenu();
      }
    });
    sortByAnnotScore.setVisible(false);

    calculateTree
            .setText(MessageManager.getString("action.calculate_tree"));

    padGapsMenuitem.setText(MessageManager.getString("label.pad_gaps"));
    padGapsMenuitem.setState(jalview.bin.Cache
            .getDefault("PAD_GAPS", false));
    padGapsMenuitem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        padGapsMenuitem_actionPerformed(e);
      }
    });
    JMenuItem vamsasStore = new JMenuItem(
            MessageManager.getString("label.vamsas_store"));
    vamsasStore.setVisible(false);
    vamsasStore.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        vamsasStore_actionPerformed(e);
      }
    });
    showTranslation.setText(MessageManager
            .getString("label.translate_cDNA"));
    showTranslation.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showTranslation_actionPerformed(e);
      }
    });
    showReverse.setText(MessageManager.getString("label.reverse"));
    showReverse.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showReverse_actionPerformed(false);
      }
    });
    showReverseComplement.setText(MessageManager
            .getString("label.reverse_complement"));
    showReverseComplement.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showReverse_actionPerformed(true);
      }
    });

    JMenuItem extractScores = new JMenuItem(
            MessageManager.getString("label.extract_scores"));
    extractScores.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        extractScores_actionPerformed(e);
      }
    });
    extractScores.setVisible(true);
    // JBPNote: TODO: make gui for regex based score extraction

    // for show products actions see AlignFrame.canShowProducts
    showProducts.setText(MessageManager.getString("label.get_cross_refs"));

    runGroovy.setText(MessageManager.getString("label.run_groovy"));
    runGroovy.setToolTipText(MessageManager
            .getString("label.run_groovy_tip"));
    runGroovy.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        runGroovy_actionPerformed();
      }
    });

    JMenuItem openFeatureSettings = new JMenuItem(
            MessageManager.getString("action.feature_settings"));
    openFeatureSettings.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        featureSettings_actionPerformed(e);
      }
    });
    JMenuItem fetchSequence = new JMenuItem(
            MessageManager.getString("label.fetch_sequences"));
    fetchSequence.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        fetchSequence_actionPerformed(e);
      }
    });

    JMenuItem annotationColour = new JMenuItem(
            MessageManager.getString("action.by_annotation"));
    annotationColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotationColour_actionPerformed(e);
      }
    });

    JMenuItem annotationColumn = new JMenuItem(
            MessageManager.getString("action.select_by_annotation"));
    annotationColumn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        annotationColumn_actionPerformed(e);
      }
    });

    rnahelicesColour.setText(MessageManager
            .getString("action.by_rna_helixes"));
    rnahelicesColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        rnahelicesColour_actionPerformed(e);
      }
    });

    JMenuItem associatedData = new JMenuItem(
            MessageManager.getString("label.load_features_annotations"));
    associatedData.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        associatedData_actionPerformed(e);
      }
    });
    autoCalculate.setText(MessageManager
            .getString("label.autocalculate_consensus"));
    autoCalculate.setState(jalview.bin.Cache.getDefault(
            "AUTO_CALC_CONSENSUS", true));
    autoCalculate.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        autoCalculate_actionPerformed(e);
      }
    });
    sortByTree.setText(MessageManager
            .getString("label.sort_alignment_new_tree"));
    sortByTree
            .setToolTipText("<html>"
                    + MessageManager
                            .getString("label.enable_automatically_sort_alignment_when_open_new_tree"));
    sortByTree
            .setState(jalview.bin.Cache.getDefault("SORT_BY_TREE", false));
    sortByTree.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        sortByTreeOption_actionPerformed(e);
      }
    });

    listenToViewSelections.setText(MessageManager
            .getString("label.listen_for_selections"));
    listenToViewSelections
            .setToolTipText("<html>"
                    + MessageManager
                            .getString("label.selections_mirror_selections_made_same_sequences_other_views"));
    listenToViewSelections.setState(false);
    listenToViewSelections.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        listenToViewSelections_actionPerformed(e);
      }
    });

    JMenu addSequenceMenu = new JMenu(
            MessageManager.getString("label.add_sequences"));
    JMenuItem addFromFile = new JMenuItem(
            MessageManager.getString("label.from_file"));
    addFromFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addFromFile_actionPerformed(e);
      }
    });
    JMenuItem addFromText = new JMenuItem(
            MessageManager.getString("label.from_textbox"));
    addFromText.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addFromText_actionPerformed(e);
      }
    });
    JMenuItem addFromURL = new JMenuItem(
            MessageManager.getString("label.from_url"));
    addFromURL.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        addFromURL_actionPerformed(e);
      }
    });
    JMenuItem exportFeatures = new JMenuItem(
            MessageManager.getString("label.export_features"));
    exportFeatures.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        exportFeatures_actionPerformed(e);
      }
    });
    JMenuItem exportAnnotations = new JMenuItem(
            MessageManager.getString("label.export_annotations"));
    exportAnnotations.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        exportAnnotations_actionPerformed(e);
      }
    });
    statusPanel.setLayout(new GridLayout());
    JMenuItem showAllSeqs = new JMenuItem(
            MessageManager.getString("label.all_sequences"));
    showAllSeqs.setToolTipText(MessageManager
            .getString("label.toggle_sequence_visibility"));
    showAllSeqs.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showAllSeqs_actionPerformed(e);
      }
    });
    JMenuItem showAllColumns = new JMenuItem(
            MessageManager.getString("label.all_columns"));
    showAllColumns.setToolTipText(MessageManager
            .getString("label.toggle_columns_visibility"));
    showAllColumns.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showAllColumns_actionPerformed(e);
      }
    });
    JMenu hideMenu = new JMenu(MessageManager.getString("action.hide"));
    JMenuItem hideSelSequences = new JMenuItem(
            MessageManager.getString("label.selected_sequences"));
    hideSelSequences.setToolTipText(MessageManager
            .getString("label.toggle_sequence_visibility"));
    hideSelSequences.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideSelSequences_actionPerformed(e);
      }
    });
    JMenuItem hideSelColumns = new JMenuItem(
            MessageManager.getString("label.selected_columns"));
    hideSelColumns.setToolTipText(MessageManager
            .getString("label.toggle_columns_visibility"));
    hideSelColumns.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideSelColumns_actionPerformed(e);
      }
    });
    JMenuItem hideAllSelection = new JMenuItem(
            MessageManager.getString("label.selected_region"));
    hideAllSelection.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideAllSelection_actionPerformed(e);
      }
    });
    // TODO: should be hidden if no selection exists.
    JMenuItem hideAllButSelection = new JMenuItem(
            MessageManager.getString("label.all_but_selected_region"));
    hideAllButSelection.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hideAllButSelection_actionPerformed(e);
      }
    });
    JMenuItem showAllhidden = new JMenuItem(
            MessageManager.getString("label.all_sequences_columns"));
    showAllhidden.setToolTipText(MessageManager
            .getString("label.toggles_visibility_hidden_selected_regions"));
    showAllhidden.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showAllhidden_actionPerformed(e);
      }
    });
    hiddenMarkers.setText(MessageManager
            .getString("action.show_hidden_markers"));
    hiddenMarkers.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hiddenMarkers_actionPerformed(e);
      }
    });

    JMenuItem invertColSel = new JMenuItem(
            MessageManager.getString("action.invert_column_selection"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask()
            | KeyEvent.ALT_MASK, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        invertColSel_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, invertColSel, al);

    showComplementMenuItem.setVisible(false);
    showComplementMenuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        showComplement_actionPerformed(showComplementMenuItem.getState());
      }
    });

    tabbedPane.addChangeListener(new javax.swing.event.ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent evt)
      {
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        int sel = pane.getSelectedIndex();
        tabSelectionChanged(sel);
      }
    });
    tabbedPane.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent e)
      {
        if (e.isPopupTrigger()) // Mac
        {
          tabbedPane_mousePressed(e);
        }
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        if (e.isPopupTrigger()) // Windows
        {
          tabbedPane_mousePressed(e);
        }
      }
    });
    tabbedPane.addFocusListener(new FocusAdapter()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        tabbedPane_focusGained(e);
      }
    });

    JMenuItem save = new JMenuItem(MessageManager.getString("action.save"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        save_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, save, al);

    reload.setEnabled(false);
    reload.setText(MessageManager.getString("action.reload"));
    reload.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        reload_actionPerformed(e);
      }
    });

    JMenuItem newView = new JMenuItem(
            MessageManager.getString("action.new_view"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit
            .getDefaultToolkit().getMenuShortcutKeyMask(), false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        newView_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, newView, al);

    tabbedPane.setToolTipText("<html><i>"
            + MessageManager.getString("label.rename_tab_eXpand_reGroup")
            + "</i></html>");
    JMenuItem textColour = new JMenuItem(
            MessageManager.getString("action.set_text_colour"));
    textColour.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        textColour_actionPerformed(e);
      }
    });
    formatMenu.setText(MessageManager.getString("action.format"));
    JMenu selectMenu = new JMenu(MessageManager.getString("action.select"));
    idRightAlign.setText(MessageManager
            .getString("label.right_align_sequence_id"));
    idRightAlign.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        idRightAlign_actionPerformed(e);
      }
    });

    gatherViews.setEnabled(false);
    gatherViews.setText(MessageManager.getString("action.gather_views"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_G, 0, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        gatherViews_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, gatherViews, al);

    expandViews.setEnabled(false);
    expandViews.setText(MessageManager.getString("action.expand_views"));
    keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_X, 0, false);
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        expandViews_actionPerformed(e);
      }
    };
    addMenuActionAndAccelerator(keyStroke, expandViews, al);

    JMenuItem pageSetup = new JMenuItem(
            MessageManager.getString("action.page_setup"));
    pageSetup.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pageSetup_actionPerformed(e);
      }
    });
    JMenuItem alignmentProperties = new JMenuItem(
            MessageManager.getString("label.alignment_props"));
    alignmentProperties.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        alignmentProperties();
      }
    });
    JMenuItem selectHighlighted = new JMenuItem(
            MessageManager.getString("action.select_highlighted_columns"));
    selectHighlighted.setToolTipText(MessageManager
            .getString("tooltip.select_highlighted_columns"));
    al = new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent actionEvent)
      {
        selectHighlightedColumns_actionPerformed(actionEvent);
      }
    };
    selectHighlighted.addActionListener(al);
    JMenu tooltipSettingsMenu = new JMenu(
            MessageManager.getString("label.sequence_id_tooltip"));
    JMenu autoAnnMenu = new JMenu(
            MessageManager.getString("label.autocalculated_annotation"));

    JMenu exportImageMenu = new JMenu(
            MessageManager.getString("label.export_image"));
    JMenu fileMenu = new JMenu(MessageManager.getString("action.file"));
    alignFrameMenuBar.add(fileMenu);
    alignFrameMenuBar.add(editMenu);
    alignFrameMenuBar.add(selectMenu);
    alignFrameMenuBar.add(viewMenu);
    alignFrameMenuBar.add(annotationsMenu);
    alignFrameMenuBar.add(formatMenu);
    alignFrameMenuBar.add(colourMenu);
    alignFrameMenuBar.add(calculateMenu);
    alignFrameMenuBar.add(webService);

    fileMenu.add(fetchSequence);
    fileMenu.add(addSequenceMenu);
    fileMenu.add(reload);
    fileMenu.addSeparator();
    fileMenu.add(vamsasStore);
    fileMenu.add(save);
    fileMenu.add(saveAs);
    fileMenu.add(outputTextboxMenu);
    fileMenu.add(pageSetup);
    fileMenu.add(printMenuItem);
    fileMenu.addSeparator();
    fileMenu.add(exportImageMenu);
    fileMenu.add(exportFeatures);
    fileMenu.add(exportAnnotations);
    fileMenu.add(loadTreeMenuItem);
    fileMenu.add(associatedData);
    fileMenu.addSeparator();
    fileMenu.add(closeMenuItem);

    pasteMenu.add(pasteNew);
    pasteMenu.add(pasteThis);
    editMenu.add(undoMenuItem);
    editMenu.add(redoMenuItem);
    editMenu.add(cut);
    editMenu.add(copy);
    editMenu.add(pasteMenu);
    editMenu.add(delete);
    editMenu.addSeparator();
    editMenu.add(remove2LeftMenuItem);
    editMenu.add(remove2RightMenuItem);
    editMenu.add(removeGappedColumnMenuItem);
    editMenu.add(removeAllGapsMenuItem);
    editMenu.add(removeRedundancyMenuItem);
    editMenu.addSeparator();
    // dont add these yet in the CVS build - they cannot be undone!
    // Excluded from Jalview 2.5 release - undo needs to be implemented.
    // editMenu.add(justifyLeftMenuItem);
    // editMenu.add(justifyRightMenuItem);
    // editMenu.addSeparator();
    editMenu.add(padGapsMenuitem);

    showMenu.add(showAllColumns);
    showMenu.add(showAllSeqs);
    showMenu.add(showAllhidden);
    hideMenu.add(hideSelColumns);
    hideMenu.add(hideSelSequences);
    hideMenu.add(hideAllSelection);
    hideMenu.add(hideAllButSelection);
    viewMenu.add(newView);
    viewMenu.add(expandViews);
    viewMenu.add(gatherViews);
    viewMenu.addSeparator();
    viewMenu.add(showMenu);
    viewMenu.add(hideMenu);
    viewMenu.add(showComplementMenuItem);
    viewMenu.addSeparator();
    viewMenu.add(followHighlightMenuItem);
    viewMenu.addSeparator();
    viewMenu.add(showSeqFeatures);
    // viewMenu.add(showSeqFeaturesHeight);
    viewMenu.add(openFeatureSettings);
    tooltipSettingsMenu.add(showDbRefsMenuitem);
    tooltipSettingsMenu.add(showNpFeatsMenuitem);
    viewMenu.add(tooltipSettingsMenu);
    viewMenu.addSeparator();
    viewMenu.add(alignmentProperties);
    viewMenu.addSeparator();
    viewMenu.add(overviewMenuItem);

    annotationsMenu.add(annotationPanelMenuItem);
    annotationsMenu.addSeparator();
    annotationsMenu.add(showAllAlAnnotations);
    annotationsMenu.add(hideAllAlAnnotations);
    annotationsMenu.addSeparator();
    annotationsMenu.add(showAllSeqAnnotations);
    annotationsMenu.add(hideAllSeqAnnotations);
    annotationsMenu.add(sortAnnBySequence);
    annotationsMenu.add(sortAnnByLabel);
    annotationsMenu.addSeparator();
    autoAnnMenu.add(showAutoFirst);
    autoAnnMenu.add(showAutoLast);
    autoAnnMenu.addSeparator();
    autoAnnMenu.add(applyAutoAnnotationSettings);
    autoAnnMenu.add(showConsensusHistogram);
    autoAnnMenu.add(showSequenceLogo);
    autoAnnMenu.add(normaliseSequenceLogo);
    autoAnnMenu.addSeparator();
    autoAnnMenu.add(showGroupConservation);
    autoAnnMenu.add(showGroupConsensus);
    annotationsMenu.add(autoAnnMenu);

    colourMenu.add(applyToAllGroups);
    colourMenu.add(textColour);
    colourMenu.addSeparator();
    colourMenu.add(noColourmenuItem);
    colourMenu.add(clustalColour);
    colourMenu.add(BLOSUM62Colour);
    colourMenu.add(PIDColour);
    colourMenu.add(zappoColour);
    colourMenu.add(taylorColour);
    colourMenu.add(hydrophobicityColour);
    colourMenu.add(helixColour);
    colourMenu.add(strandColour);
    colourMenu.add(turnColour);
    colourMenu.add(buriedColour);
    colourMenu.add(nucleotideColour);
    colourMenu.add(purinePyrimidineColour);
    colourMenu.add(RNAInteractionColour);
    // colourMenu.add(covariationColour);
    colourMenu.add(tcoffeeColour);
    colourMenu.add(userDefinedColour);
    colourMenu.addSeparator();
    colourMenu.add(conservationMenuItem);
    colourMenu.add(modifyConservation);
    colourMenu.add(abovePIDThreshold);
    colourMenu.add(modifyPID);
    colourMenu.add(annotationColour);
    colourMenu.add(rnahelicesColour);

    sort.add(sortIDMenuItem);
    sort.add(sortLengthMenuItem);
    sort.add(sortGroupMenuItem);
    sort.add(sortPairwiseMenuItem);
    sort.add(sortByTreeMenu);
    calculateMenu.add(sort);
    calculateMenu.add(calculateTree);
    calculateMenu.addSeparator();
    calculateMenu.add(pairwiseAlignmentMenuItem);
    calculateMenu.add(PCAMenuItem);
    calculateMenu.addSeparator();
    calculateMenu.add(showTranslation);
    calculateMenu.add(showReverse);
    calculateMenu.add(showReverseComplement);
    calculateMenu.add(showProducts);
    calculateMenu.add(autoCalculate);
    calculateMenu.add(sortByTree);
    calculateMenu.addSeparator();
    calculateMenu.add(expandAlignment);
    calculateMenu.add(extractScores);
    calculateMenu.addSeparator();
    calculateMenu.add(runGroovy);

    webServiceNoServices = new JMenuItem(
            MessageManager.getString("label.no_services"));
    webService.add(webServiceNoServices);
    exportImageMenu.add(htmlMenuItem);
    exportImageMenu.add(epsFile);
    exportImageMenu.add(createPNG);
    exportImageMenu.add(createBioJS);
    exportImageMenu.add(createSVG);
    addSequenceMenu.add(addFromFile);
    addSequenceMenu.add(addFromText);
    addSequenceMenu.add(addFromURL);
    this.getContentPane().add(statusPanel, java.awt.BorderLayout.SOUTH);
    statusPanel.add(statusBar, null);
    this.getContentPane().add(tabbedPane, java.awt.BorderLayout.CENTER);

    formatMenu.add(font);
    formatMenu.addSeparator();
    formatMenu.add(wrapMenuItem);
    formatMenu.add(scaleAbove);
    formatMenu.add(scaleLeft);
    formatMenu.add(scaleRight);
    formatMenu.add(seqLimits);
    formatMenu.add(idRightAlign);
    formatMenu.add(hiddenMarkers);
    formatMenu.add(viewBoxesMenuItem);
    formatMenu.add(viewTextMenuItem);
    formatMenu.add(colourTextMenuItem);
    formatMenu.add(renderGapsMenuItem);
    formatMenu.add(centreColumnLabelsMenuItem);
    formatMenu.add(showNonconservedMenuItem);
    selectMenu.add(findMenuItem);
    selectMenu.addSeparator();
    selectMenu.add(selectAllSequenceMenuItem);
    selectMenu.add(deselectAllSequenceMenuItem);
    selectMenu.add(invertSequenceMenuItem);
    selectMenu.add(invertColSel);
    selectMenu.add(createGroup);
    selectMenu.add(unGroup);
    selectMenu.add(grpsFromSelection);
    selectMenu.add(deleteGroups);
    selectMenu.add(annotationColumn);
    selectMenu.add(selectHighlighted);
    // TODO - determine if the listenToViewSelections button is needed : see bug
    // JAL-574
    // selectMenu.addSeparator();
    // selectMenu.add(listenToViewSelections);
  }

  protected void selectHighlightedColumns_actionPerformed(
          ActionEvent actionEvent)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Generate the reverse sequence (or reverse complement if the flag is true)
   * and add it to the alignment
   * 
   * @param complement
   */
  protected void showReverse_actionPerformed(boolean complement)
  {
  }

  /**
   * Try to run script in a Groovy console, having first ensured that this
   * alignframe is set as currentAlignFrame in Desktop
   */
  protected void runGroovy_actionPerformed()
  {

  }

  /**
   * Adds the given action listener and key accelerator to the given menu item.
   * Also saves in a lookup table to support lookup of action by key stroke.
   * 
   * @param keyStroke
   * @param menuItem
   * @param actionListener
   */
  protected void addMenuActionAndAccelerator(KeyStroke keyStroke,
          JMenuItem menuItem, ActionListener actionListener)
  {
    menuItem.setAccelerator(keyStroke);
    accelerators.put(keyStroke, menuItem);
    menuItem.addActionListener(actionListener);
  }

  /**
   * Action on clicking sort annotations by type.
   * 
   * @param sortOrder
   */
  protected void sortAnnotations_actionPerformed()
  {
  }

  /**
   * Action on clicking Show all annotations.
   * 
   * @param forSequences
   *          update sequence-related annotations
   * @param forAlignment
   *          update non-sequence-related annotations
   */
  protected void showAllAnnotations_actionPerformed(boolean forSequences,
          boolean forAlignment)
  {
    setAnnotationsVisibility(true, forSequences, forAlignment);
  }

  /**
   * Action on clicking Hide all annotations.
   * 
   * @param forSequences
   *          update sequence-related annotations
   * @param forAlignment
   *          update non-sequence-related annotations
   */
  protected void hideAllAnnotations_actionPerformed(boolean forSequences,
          boolean forAlignment)
  {
    setAnnotationsVisibility(false, forSequences, forAlignment);
  }

  /**
   * Set the visibility of annotations to true or false. Can act on
   * sequence-related annotations, or alignment-related, or both.
   * 
   * @param visible
   * @param forSequences
   *          update sequence-related annotations
   * @param forAlignment
   *          update non-sequence-related annotations
   */
  protected void setAnnotationsVisibility(boolean visible,
          boolean forSequences, boolean forAlignment)
  {

  }

  protected void normaliseSequenceLogo_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void listenToViewSelections_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showAllhidden_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void hideAllButSelection_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void hideAllSelection_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void applyAutoAnnotationSettings_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showConsensusHistogram_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showSequenceLogo_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void makeGrpsFromSelection_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showGroupConsensus_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showGroupConservation_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showUnconservedMenuItem_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void justifyRightMenuItem_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void justifyLeftMenuItem_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void followHighlight_actionPerformed()
  {
    // TODO Auto-generated method stub

  }

  protected void showNpFeats_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void showDbRefs_actionPerformed(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected void centreColumnLabels_actionPerformed(ActionEvent e)
  {
  }

  protected void buildSortByAnnotationScoresMenu()
  {
  }

  protected void extractScores_actionPerformed(ActionEvent e)
  {
  }

  protected void outputText_actionPerformed(ActionEvent e)
  {
  }

  public void addFromFile_actionPerformed(ActionEvent e)
  {

  }

  public void addFromText_actionPerformed(ActionEvent e)
  {

  }

  public void addFromURL_actionPerformed(ActionEvent e)
  {

  }

  public void exportFeatures_actionPerformed(ActionEvent e)
  {

  }

  public void exportAnnotations_actionPerformed(ActionEvent e)
  {

  }

  protected void htmlMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void bioJSMenuItem_actionPerformed(ActionEvent e)
  {

  }

  protected void closeMenuItem_actionPerformed(boolean b)
  {
  }

  protected void redoMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void undoMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void selectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void deselectAllSequenceMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void invertSequenceMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void remove2LeftMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void remove2RightMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void removeGappedColumnMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void removeAllGapsMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void wrapMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void viewBoxesMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void viewTextMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void colourTextMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void annotationPanelMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void overviewMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void sortPairwiseMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void sortIDMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void sortLengthMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void sortGroupMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void removeRedundancyMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void pairwiseAlignmentMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void PCAMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void averageDistanceTreeMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void neighbourTreeMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void njTreeBlosumMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void avTreeBlosumMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void clustalColour_actionPerformed(ActionEvent e)
  {
  }

  protected void zappoColour_actionPerformed(ActionEvent e)
  {
  }

  protected void taylorColour_actionPerformed(ActionEvent e)
  {
  }

  protected void hydrophobicityColour_actionPerformed(ActionEvent e)
  {
  }

  protected void helixColour_actionPerformed(ActionEvent e)
  {
  }

  protected void strandColour_actionPerformed(ActionEvent e)
  {
  }

  protected void turnColour_actionPerformed(ActionEvent e)
  {
  }

  protected void buriedColour_actionPerformed(ActionEvent e)
  {
  }

  protected void userDefinedColour_actionPerformed(ActionEvent e)
  {
  }

  protected void PIDColour_actionPerformed(ActionEvent e)
  {
  }

  protected void BLOSUM62Colour_actionPerformed(ActionEvent e)
  {
  }

  protected void purinePyrimidineColour_actionPerformed(ActionEvent e)
  {
  }

  protected void RNAInteractionColour_actionPerformed(ActionEvent e)
  {
  }

  /*
   * protected void covariationColour_actionPerformed(ActionEvent e) { }
   */

  protected void noColourmenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void conservationMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void printMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void renderGapsMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void findMenuItem_actionPerformed(ActionEvent e)
  {
  }

  protected void abovePIDThreshold_actionPerformed(ActionEvent e)
  {
  }

  public void showSeqFeatures_actionPerformed(ActionEvent actionEvent)
  {
  }

  protected void nucleotideColour_actionPerformed(ActionEvent e)
  {
  }

  protected void deleteGroups_actionPerformed(ActionEvent e)
  {
  }

  protected void createGroup_actionPerformed(ActionEvent e)
  {
  }

  protected void unGroup_actionPerformed(ActionEvent e)
  {
  }

  protected void copy_actionPerformed(ActionEvent e)
  {
  }

  protected void cut_actionPerformed(ActionEvent e)
  {
  }

  protected void delete_actionPerformed(ActionEvent e)
  {
  }

  protected void pasteNew_actionPerformed(ActionEvent e)
  {
  }

  protected void pasteThis_actionPerformed(ActionEvent e)
  {
  }

  protected void applyToAllGroups_actionPerformed(ActionEvent e)
  {
  }

  public void createPNG(java.io.File f)
  {
  }

  protected void font_actionPerformed(ActionEvent e)
  {
  }

  protected void seqLimit_actionPerformed(ActionEvent e)
  {
  }

  public void seqDBRef_actionPerformed(ActionEvent e)
  {

  }

  public void createEPS(java.io.File f)
  {
  }

  public void createSVG(java.io.File f)
  {

  }

  protected void loadTreeMenuItem_actionPerformed(ActionEvent e)
  {

  }

  /**
   * Template method to handle the 'load T-Coffee scores' menu event.
   * <p>
   * Subclasses override this method to provide a custom action.
   * 
   * @param event
   *          The raised event
   */
  protected void loadScores_actionPerformed(ActionEvent event)
  {

  }

  /**
   * Template method to handle the 'Color T-Coffee scores' menu event.
   * <p>
   * Subclasses override this method to provide a custom action.
   * 
   * @param event
   *          The raised event
   */
  protected void tcoffeeColorScheme_actionPerformed(ActionEvent event)
  {

  }

  protected void jpred_actionPerformed(ActionEvent e)
  {
  }

  protected void scaleAbove_actionPerformed(ActionEvent e)
  {
  }

  protected void scaleLeft_actionPerformed(ActionEvent e)
  {
  }

  protected void scaleRight_actionPerformed(ActionEvent e)
  {
  }

  protected void modifyPID_actionPerformed(ActionEvent e)
  {
  }

  protected void modifyConservation_actionPerformed(ActionEvent e)
  {
  }

  protected void saveAs_actionPerformed(ActionEvent e)
  {
  }

  protected void padGapsMenuitem_actionPerformed(ActionEvent e)
  {
  }

  public void vamsasStore_actionPerformed(ActionEvent e)
  {

  }

  public void vamsasLoad_actionPerformed(ActionEvent e)
  {

  }

  public void showTranslation_actionPerformed(ActionEvent e)
  {

  }

  public void featureSettings_actionPerformed(ActionEvent e)
  {

  }

  public void fetchSequence_actionPerformed(ActionEvent e)
  {

  }

  public void smoothFont_actionPerformed(ActionEvent e)
  {

  }

  public void annotationColour_actionPerformed(ActionEvent e)
  {

  }

  public void annotationColumn_actionPerformed(ActionEvent e)
  {

  }

  public void rnahelicesColour_actionPerformed(ActionEvent e)
  {

  }

  public void associatedData_actionPerformed(ActionEvent e)
  {

  }

  public void autoCalculate_actionPerformed(ActionEvent e)
  {

  }

  public void sortByTreeOption_actionPerformed(ActionEvent e)
  {

  }

  public void showAllSeqs_actionPerformed(ActionEvent e)
  {

  }

  public void showAllColumns_actionPerformed(ActionEvent e)
  {

  }

  public void hideSelSequences_actionPerformed(ActionEvent e)
  {

  }

  public void hideSelColumns_actionPerformed(ActionEvent e)
  {

  }

  public void hiddenMarkers_actionPerformed(ActionEvent e)
  {

  }

  public void findPdbId_actionPerformed(ActionEvent e)
  {

  }

  public void enterPdbId_actionPerformed(ActionEvent e)
  {

  }

  public void pdbFile_actionPerformed(ActionEvent e)
  {

  }

  public void invertColSel_actionPerformed(ActionEvent e)
  {

  }

  public void tabSelectionChanged(int sel)
  {

  }

  public void tabbedPane_mousePressed(MouseEvent e)
  {

  }

  public void tabbedPane_focusGained(FocusEvent e)
  {
    requestFocus();
  }

  public void save_actionPerformed(ActionEvent e)
  {

  }

  public void reload_actionPerformed(ActionEvent e)
  {

  }

  public void newView_actionPerformed(ActionEvent e)
  {

  }

  public void textColour_actionPerformed(ActionEvent e)
  {

  }

  public void idRightAlign_actionPerformed(ActionEvent e)
  {

  }

  public void expandViews_actionPerformed(ActionEvent e)
  {

  }

  public void gatherViews_actionPerformed(ActionEvent e)
  {

  }

  public void buildTreeMenu()
  {

  }

  public void pageSetup_actionPerformed(ActionEvent e)
  {

  }

  public void alignmentProperties()
  {

  }

  protected void expand_newalign(ActionEvent e)
  {
    // TODO Auto-generated method stub

  }

  protected boolean isShowAutoCalculatedAbove()
  {
    return showAutoCalculatedAbove;
  }

  protected void setShowAutoCalculatedAbove(boolean showAutoCalculatedAbove)
  {
    this.showAutoCalculatedAbove = showAutoCalculatedAbove;
  }

  protected SequenceAnnotationOrder getAnnotationSortOrder()
  {
    return annotationSortOrder;
  }

  protected void setAnnotationSortOrder(
          SequenceAnnotationOrder annotationSortOrder)
  {
    this.annotationSortOrder = annotationSortOrder;
  }

  public Map<KeyStroke, JMenuItem> getAccelerators()
  {
    return this.accelerators;
  }

  /**
   * Returns the selected index of the tabbed pane, or -1 if none selected
   * (including the case where the tabbed pane has not been made visible).
   * 
   * @return
   */
  public int getTabIndex()
  {
    return tabbedPane.getSelectedIndex();
  }

  public JPanel getStatusPanel()
  {
    return statusPanel;
  }

  /**
   * Sets a reference to the containing split frame. Also makes the 'toggle
   * split view' menu item visible and checked.
   * 
   * @param sf
   */
  public void setSplitFrame(SplitContainerI sf)
  {
    this.splitFrame = sf;
    if (sf != null)
    {
      this.showComplementMenuItem.setVisible(true);
      this.showComplementMenuItem.setState(true);
    }
  }

  public SplitContainerI getSplitViewContainer()
  {
    return this.splitFrame;
  }

  protected void showComplement_actionPerformed(boolean complement)
  {
  }
}
