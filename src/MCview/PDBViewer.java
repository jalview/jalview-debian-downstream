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
package MCview;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.OOMWarning;
import jalview.gui.UserDefinedColours;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.UserColourScheme;
import jalview.schemes.ZappoColourScheme;
import jalview.util.MessageManager;
import jalview.ws.ebi.EBIFetchClient;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

public class PDBViewer extends JInternalFrame implements Runnable
{

  /**
   * The associated sequence in an alignment
   */
  PDBCanvas pdbcanvas;

  PDBEntry pdbentry;

  SequenceI[] seq;

  String[] chains;

  AlignmentPanel ap;

  String protocol;

  String tmpPDBFile;

  public PDBViewer(PDBEntry pdbentry, SequenceI[] seq, String[] chains,
          AlignmentPanel ap, String protocol)
  {
    this.pdbentry = pdbentry;
    this.seq = seq;
    this.chains = chains;
    this.ap = ap;
    this.protocol = protocol;

    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    StringBuffer title = new StringBuffer(seq[0].getName() + ":"
            + pdbentry.getFile());

    pdbcanvas = new PDBCanvas();

    setContentPane(pdbcanvas);

    if (pdbentry.getFile() != null)
    {
      try
      {
        tmpPDBFile = pdbentry.getFile();
        PDBfile pdbfile = new PDBfile(false, false, false, tmpPDBFile,
                jalview.io.AppletFormatAdapter.FILE);

        pdbcanvas.init(pdbentry, seq, chains, ap, protocol);

      } catch (java.io.IOException ex)
      {
        ex.printStackTrace();
      }
    }
    else
    {
      Thread worker = new Thread(this);
      worker.start();
    }

    String method = (String) pdbentry.getProperty("method");
    if (method != null)
    {
      title.append(" Method: ");
      title.append(method);
    }
    String ch = (String) pdbentry.getProperty("chains");
    if (ch != null)
    {
      title.append(" Chain:");
      title.append(ch);
    }
    Desktop.addInternalFrame(this, title.toString(), 400, 400);
  }

  @Override
  public void run()
  {
    try
    {
      EBIFetchClient ebi = new EBIFetchClient();
      String query = "pdb:" + pdbentry.getId();
      pdbentry.setFile(ebi.fetchDataAsFile(query, "default", ".xml")
              .getAbsolutePath());

      if (pdbentry.getFile() != null)
      {
        pdbcanvas.init(pdbentry, seq, chains, ap, protocol);
      }
    } catch (Exception ex)
    {
      pdbcanvas.errorMessage = "Error retrieving file: " + pdbentry.getId();
      ex.printStackTrace();
    }
  }

  private void jbInit() throws Exception
  {
    this.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        pdbcanvas.keyPressed(evt);
      }
    });

    this.setJMenuBar(jMenuBar1);
    fileMenu.setText(MessageManager.getString("action.file"));
    coloursMenu.setText(MessageManager.getString("label.colours"));
    saveMenu.setActionCommand(MessageManager.getString("action.save_image"));
    saveMenu.setText(MessageManager.getString("action.save_as"));
    png.setText("PNG");
    png.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        png_actionPerformed(e);
      }
    });
    eps.setText("EPS");
    eps.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        eps_actionPerformed(e);
      }
    });
    mapping.setText(MessageManager.getString("label.view_mapping"));
    mapping.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        mapping_actionPerformed(e);
      }
    });
    wire.setText(MessageManager.getString("label.wireframe"));
    wire.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        wire_actionPerformed(e);
      }
    });
    depth.setSelected(true);
    depth.setText(MessageManager.getString("label.depthcue"));
    depth.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        depth_actionPerformed(e);
      }
    });
    zbuffer.setSelected(true);
    zbuffer.setText(MessageManager.getString("label.z_buffering"));
    zbuffer.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        zbuffer_actionPerformed(e);
      }
    });
    charge.setText(MessageManager.getString("label.charge_cysteine"));
    charge.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        charge_actionPerformed(e);
      }
    });
    chain.setText(MessageManager.getString("action.by_chain"));
    chain.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        chain_actionPerformed(e);
      }
    });
    seqButton.setSelected(true);
    seqButton.setText(MessageManager.getString("action.by_sequence"));
    seqButton.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        seqButton_actionPerformed(e);
      }
    });
    allchains.setSelected(true);
    allchains.setText(MessageManager.getString("label.show_all_chains"));
    allchains.addItemListener(new ItemListener()
    {
      @Override
      public void itemStateChanged(ItemEvent e)
      {
        allchains_itemStateChanged(e);
      }
    });
    zappo.setText(MessageManager.getString("label.zappo"));
    zappo.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        zappo_actionPerformed(e);
      }
    });
    taylor.setText(MessageManager.getString("label.taylor"));
    taylor.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        taylor_actionPerformed(e);
      }
    });
    hydro.setText(MessageManager.getString("label.hydrophobicity"));
    hydro.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        hydro_actionPerformed(e);
      }
    });
    helix.setText(MessageManager.getString("label.helix_propensity"));
    helix.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        helix_actionPerformed(e);
      }
    });
    strand.setText(MessageManager.getString("label.strand_propensity"));
    strand.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        strand_actionPerformed(e);
      }
    });
    turn.setText(MessageManager.getString("label.turn_propensity"));
    turn.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        turn_actionPerformed(e);
      }
    });
    buried.setText(MessageManager.getString("label.buried_index"));
    buried.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        buried_actionPerformed(e);
      }
    });
    user.setText(MessageManager.getString("action.user_defined"));
    user.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        user_actionPerformed(e);
      }
    });
    viewMenu.setText(MessageManager.getString("action.view"));
    background
            .setText(MessageManager.getString("action.background_colour"));
    background.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        background_actionPerformed(e);
      }
    });
    savePDB.setText(MessageManager.getString("label.pdb_file"));
    savePDB.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        savePDB_actionPerformed(e);
      }
    });
    jMenuBar1.add(fileMenu);
    jMenuBar1.add(coloursMenu);
    jMenuBar1.add(viewMenu);
    fileMenu.add(saveMenu);
    fileMenu.add(mapping);
    saveMenu.add(savePDB);
    saveMenu.add(png);
    saveMenu.add(eps);
    coloursMenu.add(seqButton);
    coloursMenu.add(chain);
    coloursMenu.add(charge);
    coloursMenu.add(zappo);
    coloursMenu.add(taylor);
    coloursMenu.add(hydro);
    coloursMenu.add(helix);
    coloursMenu.add(strand);
    coloursMenu.add(turn);
    coloursMenu.add(buried);
    coloursMenu.add(user);
    coloursMenu.add(background);
    ButtonGroup bg = new ButtonGroup();
    bg.add(seqButton);
    bg.add(chain);
    bg.add(charge);
    bg.add(zappo);
    bg.add(taylor);
    bg.add(hydro);
    bg.add(helix);
    bg.add(strand);
    bg.add(turn);
    bg.add(buried);
    bg.add(user);

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
              coloursMenu.remove(radioItem);
            }
            else
            {
              radioItem.addActionListener(new ActionListener()
              {
                @Override
                public void actionPerformed(ActionEvent evt)
                {
                  user_actionPerformed(evt);
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
            user_actionPerformed(evt);
          }
        });
        coloursMenu.add(radioItem);
        bg.add(radioItem);
      }
    }

    viewMenu.add(wire);
    viewMenu.add(depth);
    viewMenu.add(zbuffer);
    viewMenu.add(allchains);
  }

  JMenuBar jMenuBar1 = new JMenuBar();

  JMenu fileMenu = new JMenu();

  JMenu coloursMenu = new JMenu();

  JMenu saveMenu = new JMenu();

  JMenuItem png = new JMenuItem();

  JMenuItem eps = new JMenuItem();

  JMenuItem mapping = new JMenuItem();

  JCheckBoxMenuItem wire = new JCheckBoxMenuItem();

  JCheckBoxMenuItem depth = new JCheckBoxMenuItem();

  JCheckBoxMenuItem zbuffer = new JCheckBoxMenuItem();

  JCheckBoxMenuItem allchains = new JCheckBoxMenuItem();

  JRadioButtonMenuItem charge = new JRadioButtonMenuItem();

  JRadioButtonMenuItem chain = new JRadioButtonMenuItem();

  JRadioButtonMenuItem seqButton = new JRadioButtonMenuItem();

  JRadioButtonMenuItem hydro = new JRadioButtonMenuItem();

  JRadioButtonMenuItem taylor = new JRadioButtonMenuItem();

  JRadioButtonMenuItem zappo = new JRadioButtonMenuItem();

  JRadioButtonMenuItem user = new JRadioButtonMenuItem();

  JRadioButtonMenuItem buried = new JRadioButtonMenuItem();

  JRadioButtonMenuItem turn = new JRadioButtonMenuItem();

  JRadioButtonMenuItem strand = new JRadioButtonMenuItem();

  JRadioButtonMenuItem helix = new JRadioButtonMenuItem();

  JMenu viewMenu = new JMenu();

  JMenuItem background = new JMenuItem();

  JMenuItem savePDB = new JMenuItem();

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void eps_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.TYPE.EPS);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void png_actionPerformed(ActionEvent e)
  {
    makePDBImage(jalview.util.ImageMaker.TYPE.PNG);
  }

  void makePDBImage(jalview.util.ImageMaker.TYPE type)
  {
    int width = pdbcanvas.getWidth();
    int height = pdbcanvas.getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.TYPE.PNG)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.PNG, "Make PNG image from view",
              width, height, null, null, null, 0, false);
    }
    else if (type == jalview.util.ImageMaker.TYPE.EPS)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.EPS, "Make EPS file from view",
              width, height, null, this.getTitle(), null, 0, false);
    }
    else
    {

      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.SVG, "Make SVG file from PCA",
              width, height, null, this.getTitle(), null, 0, false);
    }

    if (im.getGraphics() != null)
    {
      pdbcanvas.drawAll(im.getGraphics(), width, height);
      im.writeImage();
    }
  }

  public void charge_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setChargeColours();
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void hydro_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new HydrophobicColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void chain_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setChainColours();
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void zbuffer_actionPerformed(ActionEvent e)
  {
    pdbcanvas.zbuffer = !pdbcanvas.zbuffer;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void molecule_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bymolecule = !pdbcanvas.bymolecule;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void depth_actionPerformed(ActionEvent e)
  {
    pdbcanvas.depthcue = !pdbcanvas.depthcue;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void wire_actionPerformed(ActionEvent e)
  {
    pdbcanvas.wire = !pdbcanvas.wire;
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void seqButton_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = true;
    pdbcanvas.updateSeqColours();
  }

  public void mapping_actionPerformed(ActionEvent e)
  {
    jalview.gui.CutAndPasteTransfer cap = new jalview.gui.CutAndPasteTransfer();
    try
    {
      cap.setText(pdbcanvas.mappingDetails.toString());
      Desktop.addInternalFrame(cap,
              MessageManager.getString("label.pdb_sequence_mapping"), 550,
              600);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("Opening sequence to structure mapping report", oom);
      cap.dispose();
    }
  }

  public void allchains_itemStateChanged(ItemEvent e)
  {
    pdbcanvas.setAllchainsVisible(allchains.getState());
  }

  public void zappo_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new ZappoColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void taylor_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new TaylorColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void helix_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new HelixColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void strand_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new StrandColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void turn_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new TurnColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void buried_actionPerformed(ActionEvent e)
  {
    pdbcanvas.bysequence = false;
    pdbcanvas.pdb.setColours(new BuriedColourScheme());
    pdbcanvas.redrawneeded = true;
    pdbcanvas.repaint();
  }

  public void user_actionPerformed(ActionEvent e)
  {
    if (e.getActionCommand().equals(
            MessageManager.getString("action.user_defined")))
    {
      // new UserDefinedColours(pdbcanvas, null);
    }
    else
    {
      UserColourScheme udc = (UserColourScheme) UserDefinedColours
              .getUserColourSchemes().get(e.getActionCommand());

      pdbcanvas.pdb.setColours(udc);
      pdbcanvas.redrawneeded = true;
      pdbcanvas.repaint();
    }
  }

  public void background_actionPerformed(ActionEvent e)
  {
    java.awt.Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_backgroud_colour"),
            pdbcanvas.backgroundColour);

    if (col != null)
    {
      pdbcanvas.backgroundColour = col;
      pdbcanvas.redrawneeded = true;
      pdbcanvas.repaint();
    }
  }

  public void savePDB_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.save_pdb_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        BufferedReader in = new BufferedReader(new FileReader(tmpPDBFile));
        File outFile = chooser.getSelectedFile();

        PrintWriter out = new PrintWriter(new FileOutputStream(outFile));
        String data;
        while ((data = in.readLine()) != null)
        {
          if (!(data.indexOf("<PRE>") > -1 || data.indexOf("</PRE>") > -1))
          {
            out.println(data);
          }
        }
        out.close();
        in.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }
}
