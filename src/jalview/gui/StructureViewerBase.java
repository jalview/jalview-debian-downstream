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
package jalview.gui;

import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.gui.StructureViewer.ViewerType;
import jalview.gui.ViewSelectionMenu.ViewSetProvider;
import jalview.io.AppletFormatAdapter;
import jalview.jbgui.GStructureViewer;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * Base class with common functionality for JMol, Chimera or other structure
 * viewers.
 * 
 * @author gmcarstairs
 *
 */
public abstract class StructureViewerBase extends GStructureViewer
        implements Runnable, ViewSetProvider
{

  /**
   * list of sequenceSet ids associated with the view
   */
  protected List<String> _aps = new ArrayList<String>();

  /**
   * list of alignment panels to use for superposition
   */
  protected Vector<AlignmentPanel> _alignwith = new Vector<AlignmentPanel>();

  /**
   * list of alignment panels that are used for colouring structures by aligned
   * sequences
   */
  protected Vector<AlignmentPanel> _colourwith = new Vector<AlignmentPanel>();

  private String viewId = null;

  private AlignmentPanel ap;

  protected boolean alignAddedStructures = false;

  protected boolean _started = false;

  protected boolean addingStructures = false;

  protected Thread worker = null;

  protected boolean allChainsSelected = false;

  /**
   * 
   * @param ap2
   * @return true if this Jmol instance is linked with the given alignPanel
   */
  public boolean isLinkedWith(AlignmentPanel ap2)
  {
    return _aps.contains(ap2.av.getSequenceSetId());
  }

  public boolean isUsedforaligment(AlignmentPanel ap2)
  {

    return (_alignwith != null) && _alignwith.contains(ap2);
  }

  public boolean isUsedforcolourby(AlignmentPanel ap2)
  {
    return (_colourwith != null) && _colourwith.contains(ap2);
  }

  /**
   * 
   * @return TRUE if the view is NOT being coloured by the alignment colours.
   */
  public boolean isColouredByViewer()
  {
    return !getBinding().isColourBySequence();
  }

  public String getViewId()
  {
    if (viewId == null)
    {
      viewId = System.currentTimeMillis() + "." + this.hashCode();
    }
    return viewId;
  }

  protected void setViewId(String viewId)
  {
    this.viewId = viewId;
  }

  public abstract String getStateInfo();

  protected void buildActionMenu()
  {
    if (_alignwith == null)
    {
      _alignwith = new Vector<AlignmentPanel>();
    }
    if (_alignwith.size() == 0 && ap != null)
    {
      _alignwith.add(ap);
    }
    ;
    for (Component c : viewerActionMenu.getMenuComponents())
    {
      if (c != alignStructs)
      {
        viewerActionMenu.remove((JMenuItem) c);
      }
    }
  }

  public AlignmentPanel getAlignmentPanel()
  {
    return ap;
  }

  protected void setAlignmentPanel(AlignmentPanel alp)
  {
    this.ap = alp;
  }

  @Override
  public AlignmentPanel[] getAllAlignmentPanels()
  {
    AlignmentPanel[] t, list = new AlignmentPanel[0];
    for (String setid : _aps)
    {
      AlignmentPanel[] panels = PaintRefresher.getAssociatedPanels(setid);
      if (panels != null)
      {
        t = new AlignmentPanel[list.length + panels.length];
        System.arraycopy(list, 0, t, 0, list.length);
        System.arraycopy(panels, 0, t, list.length, panels.length);
        list = t;
      }
    }

    return list;
  }

  /**
   * set the primary alignmentPanel reference and add another alignPanel to the
   * list of ones to use for colouring and aligning
   * 
   * @param nap
   */
  public void addAlignmentPanel(AlignmentPanel nap)
  {
    if (getAlignmentPanel() == null)
    {
      setAlignmentPanel(nap);
    }
    if (!_aps.contains(nap.av.getSequenceSetId()))
    {
      _aps.add(nap.av.getSequenceSetId());
    }
  }

  /**
   * remove any references held to the given alignment panel
   * 
   * @param nap
   */
  public void removeAlignmentPanel(AlignmentPanel nap)
  {
    try
    {
      _alignwith.remove(nap);
      _colourwith.remove(nap);
      if (getAlignmentPanel() == nap)
      {
        setAlignmentPanel(null);
        for (AlignmentPanel aps : getAllAlignmentPanels())
        {
          if (aps != nap)
          {
            setAlignmentPanel(aps);
            break;
          }
        }
      }
    } catch (Exception ex)
    {
    }
    if (getAlignmentPanel() != null)
    {
      buildActionMenu();
    }
  }

  public void useAlignmentPanelForSuperposition(AlignmentPanel nap)
  {
    addAlignmentPanel(nap);
    if (!_alignwith.contains(nap))
    {
      _alignwith.add(nap);
    }
  }

  public void excludeAlignmentPanelForSuperposition(AlignmentPanel nap)
  {
    if (_alignwith.contains(nap))
    {
      _alignwith.remove(nap);
    }
  }

  public void useAlignmentPanelForColourbyseq(AlignmentPanel nap,
          boolean enableColourBySeq)
  {
    useAlignmentPanelForColourbyseq(nap);
    getBinding().setColourBySequence(enableColourBySeq);
    seqColour.setSelected(enableColourBySeq);
    viewerColour.setSelected(!enableColourBySeq);
  }

  public void useAlignmentPanelForColourbyseq(AlignmentPanel nap)
  {
    addAlignmentPanel(nap);
    if (!_colourwith.contains(nap))
    {
      _colourwith.add(nap);
    }
  }

  public void excludeAlignmentPanelForColourbyseq(AlignmentPanel nap)
  {
    if (_colourwith.contains(nap))
    {
      _colourwith.remove(nap);
    }
  }

  public abstract ViewerType getViewerType();

  protected abstract AAStructureBindingModel getBindingModel();

  /**
   * add a new structure (with associated sequences and chains) to this viewer,
   * retrieving it if necessary first.
   * 
   * @param pdbentry
   * @param seqs
   * @param chains
   * @param align
   *          if true, new structure(s) will be aligned using associated
   *          alignment
   * @param alignFrame
   */
  protected void addStructure(final PDBEntry pdbentry,
          final SequenceI[] seqs, final String[] chains,
          final boolean align, final IProgressIndicator alignFrame)
  {
    if (pdbentry.getFile() == null)
    {
      if (worker != null && worker.isAlive())
      {
        // a retrieval is in progress, wait around and add ourselves to the
        // queue.
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            while (worker != null && worker.isAlive() && _started)
            {
              try
              {
                Thread.sleep(100 + ((int) Math.random() * 100));

              } catch (Exception e)
              {
              }
            }
            // and call ourselves again.
            addStructure(pdbentry, seqs, chains, align, alignFrame);
          }
        }).start();
        return;
      }
    }
    // otherwise, start adding the structure.
    getBindingModel().addSequenceAndChain(new PDBEntry[] { pdbentry },
            new SequenceI[][] { seqs }, new String[][] { chains });
    addingStructures = true;
    _started = false;
    alignAddedStructures = align;
    worker = new Thread(this);
    worker.start();
    return;
  }

  /**
   * Presents a dialog with the option to add an align a structure to an
   * existing structure view
   * 
   * @param pdbId
   * @param view
   * @return YES, NO or CANCEL JOptionPane code
   */
  protected int chooseAlignStructureToViewer(String pdbId,
          StructureViewerBase view)
  {
    int option = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
            MessageManager.formatMessage("label.add_pdbentry_to_view",
                    new Object[] { pdbId, view.getTitle() }),
            MessageManager
                    .getString("label.align_to_existing_structure_view"),
            JOptionPane.YES_NO_CANCEL_OPTION);
    return option;
  }

  protected abstract boolean hasPdbId(String pdbId);

  protected abstract List<StructureViewerBase> getViewersFor(
          AlignmentPanel alp);

  /**
   * Check for any existing views involving this alignment and give user the
   * option to add and align this molecule to one of them
   * 
   * @param pdbentry
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbId
   * @return true if user adds to a view, or cancels entirely, else false
   */
  protected boolean addToExistingViewer(PDBEntry pdbentry, SequenceI[] seq,
          String[] chains, final AlignmentPanel apanel, String pdbId)
  {
    for (StructureViewerBase view : getViewersFor(apanel))
    {
      // TODO: highlight the view somehow
      /*
       * JAL-1742 exclude view with this structure already mapped (don't offer
       * to align chain B to chain A of the same structure)
       */
      if (view.hasPdbId(pdbId))
      {
        continue;
      }
      int option = chooseAlignStructureToViewer(pdbId, view);
      if (option == JOptionPane.CANCEL_OPTION)
      {
        return true;
      }
      else if (option == JOptionPane.YES_OPTION)
      {
        view.useAlignmentPanelForSuperposition(apanel);
        view.addStructure(pdbentry, seq, chains, true, apanel.alignFrame);
        return true;
      }
      else
      {
        // NO_OPTION - offer the next viewer if any
      }
    }

    /*
     * nothing offered and selected
     */
    return false;
  }

  /**
   * Adds mappings for the given sequences to an already opened PDB structure,
   * and updates any viewers that have the PDB file
   * 
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbFilename
   */
  protected void addSequenceMappingsToStructure(SequenceI[] seq,
          String[] chains, final AlignmentPanel apanel, String pdbFilename)
  {
    // TODO : Fix multiple seq to one chain issue here.
    /*
     * create the mappings
     */
    apanel.getStructureSelectionManager().setMapping(seq, chains,
            pdbFilename, AppletFormatAdapter.FILE);

    /*
     * alert the FeatureRenderer to show new (PDB RESNUM) features
     */
    if (apanel.getSeqPanel().seqCanvas.fr != null)
    {
      apanel.getSeqPanel().seqCanvas.fr.featuresAdded();
      apanel.paintAlignment(true);
    }

    /*
     * add the sequences to any other viewers (of the same type) for this pdb
     * file
     */
    // JBPNOTE: this looks like a binding routine, rather than a gui routine
    for (StructureViewerBase viewer : getViewersFor(null))
    {
      AAStructureBindingModel bindingModel = viewer.getBindingModel();
      for (int pe = 0; pe < bindingModel.getPdbCount(); pe++)
      {
        if (bindingModel.getPdbEntry(pe).getFile().equals(pdbFilename))
        {
          bindingModel.addSequence(pe, seq);
          viewer.addAlignmentPanel(apanel);
          /*
           * add it to the set of alignments used for colouring structure by
           * sequence
           */
          viewer.useAlignmentPanelForColourbyseq(apanel);
          viewer.buildActionMenu();
          apanel.getStructureSelectionManager().sequenceColoursChanged(
                  apanel);
          break;
        }
      }
    }
  }

  /**
   * Check if the PDB file is already loaded, if so offer to add it to the
   * existing viewer
   * 
   * @param seq
   * @param chains
   * @param apanel
   * @param pdbId
   * @return true if the user chooses to add to a viewer, or to cancel entirely
   */
  protected boolean addAlreadyLoadedFile(SequenceI[] seq, String[] chains,
          final AlignmentPanel apanel, String pdbId)
  {
    boolean finished = false;
    String alreadyMapped = apanel.getStructureSelectionManager()
            .alreadyMappedToFile(pdbId);

    if (alreadyMapped != null)
    {
      /*
       * the PDB file is already loaded
       */
      int option = JOptionPane.showInternalConfirmDialog(Desktop.desktop,
              MessageManager.formatMessage(
                      "label.pdb_entry_is_already_displayed",
                      new Object[] { pdbId }), MessageManager
                      .formatMessage(
                              "label.map_sequences_to_visible_window",
                              new Object[] { pdbId }),
              JOptionPane.YES_NO_CANCEL_OPTION);
      if (option == JOptionPane.CANCEL_OPTION)
      {
        finished = true;
      }
      else if (option == JOptionPane.YES_OPTION)
      {
        addSequenceMappingsToStructure(seq, chains, apanel, alreadyMapped);
        finished = true;
      }
    }
    return finished;
  }

  void setChainMenuItems(List<String> chainNames)
  {
    chainMenu.removeAll();
    if (chainNames == null || chainNames.isEmpty())
    {
      return;
    }
    JMenuItem menuItem = new JMenuItem(
            MessageManager.getString("label.all"));
    menuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        allChainsSelected = true;
        for (int i = 0; i < chainMenu.getItemCount(); i++)
        {
          if (chainMenu.getItem(i) instanceof JCheckBoxMenuItem)
          {
            ((JCheckBoxMenuItem) chainMenu.getItem(i)).setSelected(true);
          }
        }
        showSelectedChains();
        allChainsSelected = false;
      }
    });

    chainMenu.add(menuItem);

    for (String chain : chainNames)
    {
      menuItem = new JCheckBoxMenuItem(chain, true);
      menuItem.addItemListener(new ItemListener()
      {
        @Override
        public void itemStateChanged(ItemEvent evt)
        {
          if (!allChainsSelected)
          {
            showSelectedChains();
          }
        }
      });

      chainMenu.add(menuItem);
    }
  }

  abstract void showSelectedChains();

}
