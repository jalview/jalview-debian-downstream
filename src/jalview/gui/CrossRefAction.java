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

import jalview.analysis.AlignmentUtils;
import jalview.analysis.CrossRef;
import jalview.api.AlignmentViewPanel;
import jalview.api.FeatureSettingsModelI;
import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceI;
import jalview.io.gff.SequenceOntologyI;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.ws.SequenceFetcher;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * Factory constructor and runnable for discovering and displaying
 * cross-references for a set of aligned sequences
 * 
 * @author jprocter
 *
 */
public class CrossRefAction implements Runnable
{
  private AlignFrame alignFrame;

  private SequenceI[] sel;

  private boolean _odna;

  private String source;

  List<AlignmentViewPanel> xrefViews = new ArrayList<AlignmentViewPanel>();

  public List<jalview.api.AlignmentViewPanel> getXrefViews()
  {
    return xrefViews;
  }

  @Override
  public void run()
  {
    final long sttime = System.currentTimeMillis();
    alignFrame.setProgressBar(
            MessageManager.formatMessage(
                    "status.searching_for_sequences_from",
                    new Object[] { source }), sttime);
    try
    {
      AlignmentI alignment = alignFrame.getViewport().getAlignment();
      AlignmentI dataset = alignment.getDataset() == null ? alignment
              : alignment.getDataset();
      boolean dna = alignment.isNucleotide();
      if (_odna != dna)
      {
        System.err
                .println("Conflict: showProducts for alignment originally "
                        + "thought to be " + (_odna ? "DNA" : "Protein")
                        + " now searching for " + (dna ? "DNA" : "Protein")
                        + " Context.");
      }
      AlignmentI xrefs = new CrossRef(sel, dataset).findXrefSequences(
              source, dna);
      if (xrefs == null)
      {
        return;
      }
      /*
       * get display scheme (if any) to apply to features
       */
      FeatureSettingsModelI featureColourScheme = new SequenceFetcher()
              .getFeatureColourScheme(source);

      AlignmentI xrefsAlignment = makeCrossReferencesAlignment(dataset,
              xrefs);
      if (!dna)
      {
        xrefsAlignment = AlignmentUtils.makeCdsAlignment(
                xrefsAlignment.getSequencesArray(), dataset, sel);
        xrefsAlignment.alignAs(alignment);
      }

      /*
       * If we are opening a splitframe, make a copy of this alignment (sharing the same dataset
       * sequences). If we are DNA, drop introns and update mappings
       */
      AlignmentI copyAlignment = null;

      if (Cache.getDefault(Preferences.ENABLE_SPLIT_FRAME, true))
      {
        boolean copyAlignmentIsAligned = false;
        if (dna)
        {
          copyAlignment = AlignmentUtils.makeCdsAlignment(sel, dataset,
                  xrefsAlignment.getSequencesArray());
          if (copyAlignment.getHeight() == 0)
          {
            JOptionPane.showMessageDialog(alignFrame,
                    MessageManager.getString("label.cant_map_cds"),
                    MessageManager.getString("label.operation_failed"),
                    JOptionPane.OK_OPTION);
            System.err.println("Failed to make CDS alignment");
          }

          /*
           * pending getting Embl transcripts to 'align', 
           * we are only doing this for Ensembl
           */
          // TODO proper criteria for 'can align as cdna'
          if (DBRefSource.ENSEMBL.equalsIgnoreCase(source)
                  || AlignmentUtils.looksLikeEnsembl(alignment))
          {
            copyAlignment.alignAs(alignment);
            copyAlignmentIsAligned = true;
          }
        }
        else
        {
          copyAlignment = AlignmentUtils.makeCopyAlignment(sel,
                  xrefs.getSequencesArray(), dataset);
        }
        copyAlignment
                .setGapCharacter(alignFrame.viewport.getGapCharacter());

        StructureSelectionManager ssm = StructureSelectionManager
                .getStructureSelectionManager(Desktop.instance);

        /*
         * register any new mappings for sequence mouseover etc
         * (will not duplicate any previously registered mappings)
         */
        ssm.registerMappings(dataset.getCodonFrames());

        if (copyAlignment.getHeight() <= 0)
        {
          System.err.println("No Sequences generated for xRef type "
                  + source);
          return;
        }
        /*
         * align protein to dna
         */
        if (dna && copyAlignmentIsAligned)
        {
          xrefsAlignment.alignAs(copyAlignment);
        }
        else
        {
          /*
           * align cdna to protein - currently only if 
           * fetching and aligning Ensembl transcripts!
           */
          // TODO: generalise for other sources of locus/transcript/cds data
          if (dna && DBRefSource.ENSEMBL.equalsIgnoreCase(source))
          {
            copyAlignment.alignAs(xrefsAlignment);
          }
        }
      }
      /*
       * build AlignFrame(s) according to available alignment data
       */
      AlignFrame newFrame = new AlignFrame(xrefsAlignment,
              AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
      if (Cache.getDefault("HIDE_INTRONS", true))
      {
        newFrame.hideFeatureColumns(SequenceOntologyI.EXON, false);
      }
      String newtitle = String.format("%s %s %s",
              dna ? MessageManager.getString("label.proteins")
                      : MessageManager.getString("label.nucleotides"),
              MessageManager.getString("label.for"), alignFrame.getTitle());
      newFrame.setTitle(newtitle);

      if (copyAlignment == null)
      {
        /*
         * split frame display is turned off in preferences file
         */
        Desktop.addInternalFrame(newFrame, newtitle,
                AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
        xrefViews.add(newFrame.alignPanel);
        return; // via finally clause
      }
      AlignFrame copyThis = new AlignFrame(copyAlignment,
              AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
      copyThis.setTitle(alignFrame.getTitle());

      boolean showSequenceFeatures = alignFrame.getViewport()
              .isShowSequenceFeatures();
      newFrame.setShowSeqFeatures(showSequenceFeatures);
      copyThis.setShowSeqFeatures(showSequenceFeatures);
      FeatureRenderer myFeatureStyling = alignFrame.alignPanel
              .getSeqPanel().seqCanvas.getFeatureRenderer();

      /*
       * copy feature rendering settings to split frame
       */
      newFrame.alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer()
              .transferSettings(myFeatureStyling);
      copyThis.alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer()
              .transferSettings(myFeatureStyling);

      /*
       * apply 'database source' feature configuration
       * if any was found
       */
      // TODO is this the feature colouring for the original
      // alignment or the fetched xrefs? either could be Ensembl
      newFrame.getViewport().applyFeaturesStyle(featureColourScheme);
      copyThis.getViewport().applyFeaturesStyle(featureColourScheme);

      SplitFrame sf = new SplitFrame(dna ? copyThis : newFrame,
              dna ? newFrame : copyThis);
      newFrame.setVisible(true);
      copyThis.setVisible(true);
      String linkedTitle = MessageManager
              .getString("label.linked_view_title");
      Desktop.addInternalFrame(sf, linkedTitle, -1, -1);
      sf.adjustDivider();

      // finally add the top, then bottom frame to the view list
      xrefViews.add(dna ? copyThis.alignPanel : newFrame.alignPanel);
      xrefViews.add(!dna ? copyThis.alignPanel : newFrame.alignPanel);

    } catch (OutOfMemoryError e)
    {
      new OOMWarning("whilst fetching crossreferences", e);
    } catch (Throwable e)
    {
      Cache.log.error("Error when finding crossreferences", e);
    } finally
    {
      alignFrame.setProgressBar(MessageManager.formatMessage(
              "status.finished_searching_for_sequences_from",
              new Object[] { source }), sttime);
    }
  }

  /**
   * Makes an alignment containing the given sequences, and adds them to the
   * given dataset, which is also set as the dataset for the new alignment
   * 
   * TODO: refactor to DatasetI method
   * 
   * @param dataset
   * @param seqs
   * @return
   */
  protected AlignmentI makeCrossReferencesAlignment(AlignmentI dataset,
          AlignmentI seqs)
  {
    SequenceI[] sprods = new SequenceI[seqs.getHeight()];
    for (int s = 0; s < sprods.length; s++)
    {
      sprods[s] = (seqs.getSequenceAt(s)).deriveSequence();
      if (dataset.getSequences() == null
              || !dataset.getSequences().contains(
                      sprods[s].getDatasetSequence()))
      {
        dataset.addSequence(sprods[s].getDatasetSequence());
      }
      sprods[s].updatePDBIds();
    }
    Alignment al = new Alignment(sprods);
    al.setDataset(dataset);
    return al;
  }

  public CrossRefAction(AlignFrame alignFrame, SequenceI[] sel,
          boolean _odna, String source)
  {
    this.alignFrame = alignFrame;
    this.sel = sel;
    this._odna = _odna;
    this.source = source;
  }

  public static CrossRefAction showProductsFor(final SequenceI[] sel,
          final boolean _odna, final String source,
          final AlignFrame alignFrame)
  {
    return new CrossRefAction(alignFrame, sel, _odna, source);
  }

}
