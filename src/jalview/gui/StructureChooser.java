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

import jalview.bin.Jalview;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.jbgui.GStructureChooser;
import jalview.structure.StructureMapping;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;
import jalview.ws.DBRefFetcher;
import jalview.ws.sifts.SiftsSettings;

import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

/**
 * Provides the behaviors for the Structure chooser Panel
 * 
 * @author tcnofoegbu
 *
 */
@SuppressWarnings("serial")
public class StructureChooser extends GStructureChooser implements
        IProgressIndicator
{
  private SequenceI selectedSequence;

  private SequenceI[] selectedSequences;

  private IProgressIndicator progressIndicator;

  private Collection<FTSData> discoveredStructuresSet;

  private FTSRestRequest lastPdbRequest;

  private FTSRestClientI pdbRestCleint;

  private String selectedPdbFileName;

  private boolean isValidPBDEntry;

  private boolean cachedPDBExists;

  private static int MAX_QLENGHT = 7820;

  public StructureChooser(SequenceI[] selectedSeqs, SequenceI selectedSeq,
          AlignmentPanel ap)
  {
    this.ap = ap;
    this.selectedSequence = selectedSeq;
    this.selectedSequences = selectedSeqs;
    this.progressIndicator = (ap == null) ? null : ap.alignFrame;
    init();
  }

  /**
   * Initializes parameters used by the Structure Chooser Panel
   */
  public void init()
  {
    if (!Jalview.isHeadlessMode())
    {
      progressBar = new ProgressBar(this.statusPanel, this.statusBar);
    }

    // ensure a filter option is in force for search
    populateFilterComboBox(true, cachedPDBExists);
    Thread discoverPDBStructuresThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        long startTime = System.currentTimeMillis();
        updateProgressIndicator(MessageManager
                .getString("status.loading_cached_pdb_entries"), startTime);
        loadLocalCachedPDBEntries();
        updateProgressIndicator(null, startTime);
        updateProgressIndicator(MessageManager
                .getString("status.searching_for_pdb_structures"),
                startTime);
        fetchStructuresMetaData();
        // revise filter options if no results were found
        populateFilterComboBox(isStructuresDiscovered(), cachedPDBExists);
        updateProgressIndicator(null, startTime);
        mainFrame.setVisible(true);
        updateCurrentView();
      }
    });
    discoverPDBStructuresThread.start();
  }

  /**
   * Updates the progress indicator with the specified message
   * 
   * @param message
   *          displayed message for the operation
   * @param id
   *          unique handle for this indicator
   */
  public void updateProgressIndicator(String message, long id)
  {
    if (progressIndicator != null)
    {
      progressIndicator.setProgressBar(message, id);
    }
  }

  /**
   * Retrieve meta-data for all the structure(s) for a given sequence(s) in a
   * selection group
   */
  public void fetchStructuresMetaData()
  {
    long startTime = System.currentTimeMillis();
    pdbRestCleint = PDBFTSRestClient.getInstance();
    Collection<FTSDataColumnI> wantedFields = pdbDocFieldPrefs
            .getStructureSummaryFields();

    discoveredStructuresSet = new LinkedHashSet<FTSData>();
    HashSet<String> errors = new HashSet<String>();
    for (SequenceI seq : selectedSequences)
    {
      FTSRestRequest pdbRequest = new FTSRestRequest();
      pdbRequest.setAllowEmptySeq(false);
      pdbRequest.setResponseSize(500);
      pdbRequest.setFieldToSearchBy("(");
      FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
              .getSelectedItem());
      pdbRequest.setFieldToSortBy(selectedFilterOpt.getValue(),
              !chk_invertFilter.isSelected());
      pdbRequest.setWantedFields(wantedFields);
      pdbRequest.setSearchTerm(buildQuery(seq) + ")");
      pdbRequest.setAssociatedSequence(seq);
      FTSRestResponse resultList;
      try
      {
        resultList = pdbRestCleint.executeRequest(pdbRequest);
      } catch (Exception e)
      {
        e.printStackTrace();
        errors.add(e.getMessage());
        continue;
      }
      lastPdbRequest = pdbRequest;
      if (resultList.getSearchSummary() != null
              && !resultList.getSearchSummary().isEmpty())
      {
        discoveredStructuresSet.addAll(resultList.getSearchSummary());
      }
    }

    int noOfStructuresFound = 0;
    String totalTime = (System.currentTimeMillis() - startTime)
            + " milli secs";
    if (discoveredStructuresSet != null
            && !discoveredStructuresSet.isEmpty())
    {
      getResultTable().setModel(
              FTSRestResponse.getTableModel(lastPdbRequest,
                      discoveredStructuresSet));
      noOfStructuresFound = discoveredStructuresSet.size();
      mainFrame.setTitle(MessageManager.formatMessage(
              "label.structure_chooser_no_of_structures",
              noOfStructuresFound, totalTime));
    }
    else
    {
      mainFrame.setTitle(MessageManager
              .getString("label.structure_chooser_manual_association"));
      if (errors.size() > 0)
      {
        StringBuilder errorMsg = new StringBuilder();
        for (String error : errors)
        {
          errorMsg.append(error).append("\n");
        }
        JOptionPane.showMessageDialog(this, errorMsg.toString(),
                MessageManager.getString("label.pdb_web-service_error"),
                JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  public void loadLocalCachedPDBEntries()
  {
    ArrayList<CachedPDB> entries = new ArrayList<CachedPDB>();
    for (SequenceI seq : selectedSequences)
    {
      if (seq.getDatasetSequence() != null
              && seq.getDatasetSequence().getAllPDBEntries() != null)
      {
        for (PDBEntry pdbEntry : seq.getDatasetSequence()
                .getAllPDBEntries())
        {
          if (pdbEntry.getFile() != null)
          {
            entries.add(new CachedPDB(seq, pdbEntry));
          }
        }
      }
    }
    cachedPDBExists = !entries.isEmpty();
    PDBEntryTableModel tableModelx = new PDBEntryTableModel(entries);
    tbl_local_pdb.setModel(tableModelx);
  }

  /**
   * Builds a query string for a given sequences using its DBRef entries
   * 
   * @param seq
   *          the sequences to build a query for
   * @return the built query string
   */

  public static String buildQuery(SequenceI seq)
  {
    boolean isPDBRefsFound = false;
    boolean isUniProtRefsFound = false;
    StringBuilder queryBuilder = new StringBuilder();
    Set<String> seqRefs = new LinkedHashSet<String>();

    if (seq.getAllPDBEntries() != null
            && queryBuilder.length() < MAX_QLENGHT)
    {
      for (PDBEntry entry : seq.getAllPDBEntries())
      {
        if (isValidSeqName(entry.getId()))
        {
          queryBuilder.append("pdb_id:")
                  .append(entry.getId().toLowerCase()).append(" OR ");
          isPDBRefsFound = true;
        }
      }
    }

    if (seq.getDBRefs() != null && seq.getDBRefs().length != 0)
    {
      for (DBRefEntry dbRef : seq.getDBRefs())
      {
        if (isValidSeqName(getDBRefId(dbRef))
                && queryBuilder.length() < MAX_QLENGHT)
        {
          if (dbRef.getSource().equalsIgnoreCase(DBRefSource.UNIPROT))
          {
            queryBuilder.append("uniprot_accession:")
                    .append(getDBRefId(dbRef)).append(" OR ");
            queryBuilder.append("uniprot_id:").append(getDBRefId(dbRef))
                    .append(" OR ");
            isUniProtRefsFound = true;
          }
          else if (dbRef.getSource().equalsIgnoreCase(DBRefSource.PDB))
          {

            queryBuilder.append("pdb_id:")
                    .append(getDBRefId(dbRef).toLowerCase()).append(" OR ");
            isPDBRefsFound = true;
          }
          else
          {
            seqRefs.add(getDBRefId(dbRef));
          }
        }
      }
    }

    if (!isPDBRefsFound && !isUniProtRefsFound)
    {
      String seqName = seq.getName();
      seqName = sanitizeSeqName(seqName);
      String[] names = seqName.toLowerCase().split("\\|");
      for (String name : names)
      {
        // System.out.println("Found name : " + name);
        name.trim();
        if (isValidSeqName(name))
        {
          seqRefs.add(name);
        }
      }

      for (String seqRef : seqRefs)
      {
        queryBuilder.append("text:").append(seqRef).append(" OR ");
      }
    }

    int endIndex = queryBuilder.lastIndexOf(" OR ");
    if (queryBuilder.toString().length() < 6)
    {
      return null;
    }
    String query = queryBuilder.toString().substring(0, endIndex);
    return query;
  }

  /**
   * Remove the following special characters from input string +, -, &, !, (, ),
   * {, }, [, ], ^, ", ~, *, ?, :, \
   * 
   * @param seqName
   * @return
   */
  static String sanitizeSeqName(String seqName)
  {
    Objects.requireNonNull(seqName);
    return seqName.replaceAll("\\[\\d*\\]", "")
            .replaceAll("[^\\dA-Za-z|_]", "").replaceAll("\\s+", "+");
  }

  /**
   * Ensures sequence ref names are not less than 3 characters and does not
   * contain a database name
   * 
   * @param seqName
   * @return
   */
  public static boolean isValidSeqName(String seqName)
  {
    // System.out.println("seqName : " + seqName);
    String ignoreList = "pdb,uniprot,swiss-prot";
    if (seqName.length() < 3)
    {
      return false;
    }
    if (seqName.contains(":"))
    {
      return false;
    }
    seqName = seqName.toLowerCase();
    for (String ignoredEntry : ignoreList.split(","))
    {
      if (seqName.contains(ignoredEntry))
      {
        return false;
      }
    }
    return true;
  }

  public static String getDBRefId(DBRefEntry dbRef)
  {
    String ref = dbRef.getAccessionId().replaceAll("GO:", "");
    return ref;
  }

  /**
   * Filters a given list of discovered structures based on supplied argument
   * 
   * @param fieldToFilterBy
   *          the field to filter by
   */
  public void filterResultSet(final String fieldToFilterBy)
  {
    Thread filterThread = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        long startTime = System.currentTimeMillis();
        pdbRestCleint = PDBFTSRestClient.getInstance();
        lbl_loading.setVisible(true);
        Collection<FTSDataColumnI> wantedFields = pdbDocFieldPrefs
                .getStructureSummaryFields();
        Collection<FTSData> filteredResponse = new HashSet<FTSData>();
        HashSet<String> errors = new HashSet<String>();

        for (SequenceI seq : selectedSequences)
        {
          FTSRestRequest pdbRequest = new FTSRestRequest();
          if (fieldToFilterBy.equalsIgnoreCase("uniprot_coverage"))
          {
            pdbRequest.setAllowEmptySeq(false);
            pdbRequest.setResponseSize(1);
            pdbRequest.setFieldToSearchBy("(");
            pdbRequest.setSearchTerm(buildQuery(seq) + ")");
            pdbRequest.setWantedFields(wantedFields);
            pdbRequest.setAssociatedSequence(seq);
            pdbRequest.setFacet(true);
            pdbRequest.setFacetPivot(fieldToFilterBy + ",entry_entity");
            pdbRequest.setFacetPivotMinCount(1);
          }
          else
          {
            pdbRequest.setAllowEmptySeq(false);
            pdbRequest.setResponseSize(1);
            pdbRequest.setFieldToSearchBy("(");
            pdbRequest.setFieldToSortBy(fieldToFilterBy,
                    !chk_invertFilter.isSelected());
            pdbRequest.setSearchTerm(buildQuery(seq) + ")");
            pdbRequest.setWantedFields(wantedFields);
            pdbRequest.setAssociatedSequence(seq);
          }
          FTSRestResponse resultList;
          try
          {
            resultList = pdbRestCleint.executeRequest(pdbRequest);
          } catch (Exception e)
          {
            e.printStackTrace();
            errors.add(e.getMessage());
            continue;
          }
          lastPdbRequest = pdbRequest;
          if (resultList.getSearchSummary() != null
                  && !resultList.getSearchSummary().isEmpty())
          {
            filteredResponse.addAll(resultList.getSearchSummary());
          }
        }

        String totalTime = (System.currentTimeMillis() - startTime)
                + " milli secs";
        if (!filteredResponse.isEmpty())
        {
          final int filterResponseCount = filteredResponse.size();
          Collection<FTSData> reorderedStructuresSet = new LinkedHashSet<FTSData>();
          reorderedStructuresSet.addAll(filteredResponse);
          reorderedStructuresSet.addAll(discoveredStructuresSet);
          getResultTable().setModel(
                  FTSRestResponse.getTableModel(lastPdbRequest,
                          reorderedStructuresSet));

          FTSRestResponse.configureTableColumn(getResultTable(),
                  wantedFields, tempUserPrefs);
          getResultTable().getColumn("Ref Sequence").setPreferredWidth(120);
          getResultTable().getColumn("Ref Sequence").setMinWidth(100);
          getResultTable().getColumn("Ref Sequence").setMaxWidth(200);
          // Update table selection model here
          getResultTable().addRowSelectionInterval(0,
                  filterResponseCount - 1);
          mainFrame.setTitle(MessageManager.formatMessage(
                  "label.structure_chooser_filter_time", totalTime));
        }
        else
        {
          mainFrame.setTitle(MessageManager.formatMessage(
                  "label.structure_chooser_filter_time", totalTime));
          if (errors.size() > 0)
          {
            StringBuilder errorMsg = new StringBuilder();
            for (String error : errors)
            {
              errorMsg.append(error).append("\n");
            }
            JOptionPane.showMessageDialog(
                    null,
                    errorMsg.toString(),
                    MessageManager.getString("label.pdb_web-service_error"),
                    JOptionPane.ERROR_MESSAGE);
          }
        }

        lbl_loading.setVisible(false);

        validateSelections();
      }
    });
    filterThread.start();
  }

  /**
   * Handles action event for btn_pdbFromFile
   */
  @Override
  public void pdbFromFile_actionPerformed()
  {
    jalview.io.JalviewFileChooser chooser = new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager.formatMessage(
            "label.select_pdb_file_for",
            selectedSequence.getDisplayId(false)));
    chooser.setToolTipText(MessageManager.formatMessage(
            "label.load_pdb_file_associate_with_sequence",
            selectedSequence.getDisplayId(false)));

    int value = chooser.showOpenDialog(null);
    if (value == jalview.io.JalviewFileChooser.APPROVE_OPTION)
    {
      selectedPdbFileName = chooser.getSelectedFile().getPath();
      jalview.bin.Cache.setProperty("LAST_DIRECTORY", selectedPdbFileName);
      validateSelections();
    }
  }

  /**
   * Populates the filter combo-box options dynamically depending on discovered
   * structures
   */
  protected void populateFilterComboBox(boolean haveData,
          boolean cachedPDBExists)
  {
    /*
     * temporarily suspend the change listener behaviour
     */
    cmb_filterOption.removeItemListener(this);

    cmb_filterOption.removeAllItems();
    if (haveData)
    {
      cmb_filterOption.addItem(new FilterOption("Best Quality",
              "overall_quality", VIEWS_FILTER));
      cmb_filterOption.addItem(new FilterOption("Best Resolution",
              "resolution", VIEWS_FILTER));
      cmb_filterOption.addItem(new FilterOption("Most Protein Chain",
              "number_of_protein_chains", VIEWS_FILTER));
      cmb_filterOption.addItem(new FilterOption("Most Bound Molecules",
              "number_of_bound_molecules", VIEWS_FILTER));
      cmb_filterOption.addItem(new FilterOption("Most Polymer Residues",
              "number_of_polymer_residues", VIEWS_FILTER));
    }
    cmb_filterOption.addItem(new FilterOption("Enter PDB Id", "-",
            VIEWS_ENTER_ID));
    cmb_filterOption.addItem(new FilterOption("From File", "-",
            VIEWS_FROM_FILE));
    FilterOption cachedOption = new FilterOption("Cached PDB Entries", "-",
            VIEWS_LOCAL_PDB);
    cmb_filterOption.addItem(cachedOption);

    if (/*!haveData &&*/cachedPDBExists)
    {
      cmb_filterOption.setSelectedItem(cachedOption);
    }

    cmb_filterOption.addItemListener(this);
  }

  /**
   * Updates the displayed view based on the selected filter option
   */
  protected void updateCurrentView()
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    layout_switchableViews.show(pnl_switchableViews,
            selectedFilterOpt.getView());
    String filterTitle = mainFrame.getTitle();
    mainFrame.setTitle(frameTitle);
    chk_invertFilter.setVisible(false);
    if (selectedFilterOpt.getView() == VIEWS_FILTER)
    {
      mainFrame.setTitle(filterTitle);
      chk_invertFilter.setVisible(true);
      filterResultSet(selectedFilterOpt.getValue());
    }
    else if (selectedFilterOpt.getView() == VIEWS_ENTER_ID
            || selectedFilterOpt.getView() == VIEWS_FROM_FILE)
    {
      mainFrame.setTitle(MessageManager
              .getString("label.structure_chooser_manual_association"));
      idInputAssSeqPanel.loadCmbAssSeq();
      fileChooserAssSeqPanel.loadCmbAssSeq();
    }
    validateSelections();
  }

  /**
   * Validates user selection and activates the view button if all parameters
   * are correct
   */
  @Override
  public void validateSelections()
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    btn_view.setEnabled(false);
    String currentView = selectedFilterOpt.getView();
    if (currentView == VIEWS_FILTER)
    {
      if (getResultTable().getSelectedRows().length > 0)
      {
        btn_view.setEnabled(true);
      }
    }
    else if (currentView == VIEWS_LOCAL_PDB)
    {
      if (tbl_local_pdb.getSelectedRows().length > 0)
      {
        btn_view.setEnabled(true);
      }
    }
    else if (currentView == VIEWS_ENTER_ID)
    {
      validateAssociationEnterPdb();
    }
    else if (currentView == VIEWS_FROM_FILE)
    {
      validateAssociationFromFile();
    }
  }

  /**
   * Validates inputs from the Manual PDB entry panel
   */
  public void validateAssociationEnterPdb()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) idInputAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    lbl_pdbManualFetchStatus.setIcon(errorImage);
    lbl_pdbManualFetchStatus.setToolTipText("");
    if (txt_search.getText().length() > 0)
    {
      lbl_pdbManualFetchStatus
              .setToolTipText(JvSwingUtils.wrapTooltip(true, MessageManager
                      .formatMessage("info.no_pdb_entry_found_for",
                              txt_search.getText())));
    }

    if (errorWarning.length() > 0)
    {
      lbl_pdbManualFetchStatus.setIcon(warningImage);
      lbl_pdbManualFetchStatus.setToolTipText(JvSwingUtils.wrapTooltip(
              true, errorWarning.toString()));
    }

    if (selectedSequences.length == 1
            || !assSeqOpt.getName().equalsIgnoreCase(
                    "-Select Associated Seq-"))
    {
      txt_search.setEnabled(true);
      if (isValidPBDEntry)
      {
        btn_view.setEnabled(true);
        lbl_pdbManualFetchStatus.setToolTipText("");
        lbl_pdbManualFetchStatus.setIcon(goodImage);
      }
    }
    else
    {
      txt_search.setEnabled(false);
      lbl_pdbManualFetchStatus.setIcon(errorImage);
    }
  }

  /**
   * Validates inputs for the manual PDB file selection options
   */
  public void validateAssociationFromFile()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) fileChooserAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    lbl_fromFileStatus.setIcon(errorImage);
    if (selectedSequences.length == 1
            || (assSeqOpt != null && !assSeqOpt.getName().equalsIgnoreCase(
                    "-Select Associated Seq-")))
    {
      btn_pdbFromFile.setEnabled(true);
      if (selectedPdbFileName != null && selectedPdbFileName.length() > 0)
      {
        btn_view.setEnabled(true);
        lbl_fromFileStatus.setIcon(goodImage);
      }
    }
    else
    {
      btn_pdbFromFile.setEnabled(false);
      lbl_fromFileStatus.setIcon(errorImage);
    }
  }

  @Override
  public void cmbAssSeqStateChanged()
  {
    validateSelections();
  }

  /**
   * Handles the state change event for the 'filter' combo-box and 'invert'
   * check-box
   */
  @Override
  protected void stateChanged(ItemEvent e)
  {
    if (e.getSource() instanceof JCheckBox)
    {
      updateCurrentView();
    }
    else
    {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        updateCurrentView();
      }
    }

  }

  /**
   * Handles action event for btn_ok
   */
  @Override
  public void ok_ActionPerformed()
  {
    final long progressSessionId = System.currentTimeMillis();
    final StructureSelectionManager ssm = ap.getStructureSelectionManager();
    final int preferredHeight = pnl_filter.getHeight();
    ssm.setProgressIndicator(this);
    ssm.setProgressSessionId(progressSessionId);
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
                .getSelectedItem());
        String currentView = selectedFilterOpt.getView();
        if (currentView == VIEWS_FILTER)
        {
          int pdbIdColIndex = getResultTable().getColumn("PDB Id")
                  .getModelIndex();
          int refSeqColIndex = getResultTable().getColumn("Ref Sequence")
                  .getModelIndex();
          int[] selectedRows = getResultTable().getSelectedRows();
          PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
          int count = 0;
          ArrayList<SequenceI> selectedSeqsToView = new ArrayList<SequenceI>();
          for (int row : selectedRows)
          {
            String pdbIdStr = getResultTable().getValueAt(row,
                    pdbIdColIndex).toString();
            SequenceI selectedSeq = (SequenceI) getResultTable()
                    .getValueAt(row, refSeqColIndex);
            selectedSeqsToView.add(selectedSeq);
            PDBEntry pdbEntry = selectedSeq.getPDBEntry(pdbIdStr);
            if (pdbEntry == null)
            {
              pdbEntry = getFindEntry(pdbIdStr,
                      selectedSeq.getAllPDBEntries());
            }
            if (pdbEntry == null)
            {
              pdbEntry = new PDBEntry();
              pdbEntry.setId(pdbIdStr);
              pdbEntry.setType(PDBEntry.Type.PDB);
              selectedSeq.getDatasetSequence().addPDBId(pdbEntry);
            }
            pdbEntriesToView[count++] = pdbEntry;
          }
          SequenceI[] selectedSeqs = selectedSeqsToView
                  .toArray(new SequenceI[selectedSeqsToView.size()]);
          launchStructureViewer(ssm, pdbEntriesToView, ap, selectedSeqs);
        }
        else if (currentView == VIEWS_LOCAL_PDB)
        {
          int[] selectedRows = tbl_local_pdb.getSelectedRows();
          PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
          int count = 0;
          int pdbIdColIndex = tbl_local_pdb.getColumn("PDB Id")
                  .getModelIndex();
          int refSeqColIndex = tbl_local_pdb.getColumn("Ref Sequence")
                  .getModelIndex();
          ArrayList<SequenceI> selectedSeqsToView = new ArrayList<SequenceI>();
          for (int row : selectedRows)
          {
            PDBEntry pdbEntry = (PDBEntry) tbl_local_pdb.getValueAt(row,
                    pdbIdColIndex);
            pdbEntriesToView[count++] = pdbEntry;
            SequenceI selectedSeq = (SequenceI) tbl_local_pdb.getValueAt(
                    row, refSeqColIndex);
            selectedSeqsToView.add(selectedSeq);
          }
          SequenceI[] selectedSeqs = selectedSeqsToView
                  .toArray(new SequenceI[selectedSeqsToView.size()]);
          launchStructureViewer(ssm, pdbEntriesToView, ap, selectedSeqs);
        }
        else if (currentView == VIEWS_ENTER_ID)
        {
          SequenceI userSelectedSeq = ((AssociateSeqOptions) idInputAssSeqPanel
                  .getCmb_assSeq().getSelectedItem()).getSequence();
          if (userSelectedSeq != null)
          {
            selectedSequence = userSelectedSeq;
          }

          String pdbIdStr = txt_search.getText();
          PDBEntry pdbEntry = selectedSequence.getPDBEntry(pdbIdStr);
          if (pdbEntry == null)
          {
            pdbEntry = new PDBEntry();
            if (pdbIdStr.split(":").length > 1)
            {
              pdbEntry.setId(pdbIdStr.split(":")[0]);
              pdbEntry.setChainCode(pdbIdStr.split(":")[1].toUpperCase());
            }
            else
            {
              pdbEntry.setId(pdbIdStr);
            }
            pdbEntry.setType(PDBEntry.Type.PDB);
            selectedSequence.getDatasetSequence().addPDBId(pdbEntry);
          }

          PDBEntry[] pdbEntriesToView = new PDBEntry[] { pdbEntry };
          launchStructureViewer(ssm, pdbEntriesToView, ap,
                  new SequenceI[] { selectedSequence });
        }
        else if (currentView == VIEWS_FROM_FILE)
        {
          SequenceI userSelectedSeq = ((AssociateSeqOptions) fileChooserAssSeqPanel
                  .getCmb_assSeq().getSelectedItem()).getSequence();
          if (userSelectedSeq != null)
          {
            selectedSequence = userSelectedSeq;
          }
          PDBEntry fileEntry = new AssociatePdbFileWithSeq()
                  .associatePdbWithSeq(selectedPdbFileName,
                          jalview.io.AppletFormatAdapter.FILE,
                          selectedSequence, true, Desktop.instance);

          launchStructureViewer(ssm, new PDBEntry[] { fileEntry }, ap,
                  new SequenceI[] { selectedSequence });
        }
        closeAction(preferredHeight);
      }
    }).start();
  }

  private PDBEntry getFindEntry(String id, Vector<PDBEntry> pdbEntries)
  {
    Objects.requireNonNull(id);
    Objects.requireNonNull(pdbEntries);
    PDBEntry foundEntry = null;
    for (PDBEntry entry : pdbEntries)
    {
      if (entry.getId().equalsIgnoreCase(id))
      {
        return entry;
      }
    }
    return foundEntry;
  }

  private void launchStructureViewer(StructureSelectionManager ssm,
          final PDBEntry[] pdbEntriesToView,
          final AlignmentPanel alignPanel, SequenceI[] sequences)
  {
    ssm.setProgressBar(MessageManager
            .getString("status.launching_3d_structure_viewer"));
    final StructureViewer sViewer = new StructureViewer(ssm);

    if (SiftsSettings.isMapWithSifts())
    {
      List<SequenceI> seqsWithoutSourceDBRef = new ArrayList<SequenceI>();
      int p = 0;
      // TODO: skip PDBEntry:Sequence pairs where PDBEntry doesn't look like a
      // real PDB ID. For moment, we can also safely do this if there is already
      // a known mapping between the PDBEntry and the sequence.
      for (SequenceI seq : sequences)
      {
        PDBEntry pdbe = pdbEntriesToView[p++];
        if (pdbe != null && pdbe.getFile() != null)
        {
          StructureMapping[] smm = ssm.getMapping(pdbe.getFile());
          if (smm != null && smm.length > 0)
          {
            for (StructureMapping sm : smm)
            {
              if (sm.getSequence() == seq)
              {
                continue;
              }
            }
          }
        }
        if (seq.getPrimaryDBRefs().size() == 0)
        {
          seqsWithoutSourceDBRef.add(seq);
          continue;
        }
      }
      if (!seqsWithoutSourceDBRef.isEmpty())
      {
        int y = seqsWithoutSourceDBRef.size();
        ssm.setProgressBar(null);
        ssm.setProgressBar(MessageManager.formatMessage(
                "status.fetching_dbrefs_for_sequences_without_valid_refs",
                y));
        SequenceI[] seqWithoutSrcDBRef = new SequenceI[y];
        int x = 0;
        for (SequenceI fSeq : seqsWithoutSourceDBRef)
        {
          seqWithoutSrcDBRef[x++] = fSeq;
        }
        DBRefFetcher dbRefFetcher = new DBRefFetcher(seqWithoutSrcDBRef);
        dbRefFetcher.fetchDBRefs(true);
      }
    }
    if (pdbEntriesToView.length > 1)
    {
      ArrayList<SequenceI[]> seqsMap = new ArrayList<SequenceI[]>();
      for (SequenceI seq : sequences)
      {
        seqsMap.add(new SequenceI[] { seq });
      }
      SequenceI[][] collatedSeqs = seqsMap.toArray(new SequenceI[0][0]);
      ssm.setProgressBar(null);
      ssm.setProgressBar(MessageManager
              .getString("status.fetching_3d_structures_for_selected_entries"));
      sViewer.viewStructures(pdbEntriesToView, collatedSeqs, alignPanel);
    }
    else
    {
      ssm.setProgressBar(null);
      ssm.setProgressBar(MessageManager.formatMessage(
              "status.fetching_3d_structures_for",
              pdbEntriesToView[0].getId()));
      sViewer.viewStructures(pdbEntriesToView[0], sequences, alignPanel);
    }
  }

  /**
   * Populates the combo-box used in associating manually fetched structures to
   * a unique sequence when more than one sequence selection is made.
   */
  @Override
  public void populateCmbAssociateSeqOptions(
          JComboBox<AssociateSeqOptions> cmb_assSeq, JLabel lbl_associateSeq)
  {
    cmb_assSeq.removeAllItems();
    cmb_assSeq.addItem(new AssociateSeqOptions("-Select Associated Seq-",
            null));
    lbl_associateSeq.setVisible(false);
    if (selectedSequences.length > 1)
    {
      for (SequenceI seq : selectedSequences)
      {
        cmb_assSeq.addItem(new AssociateSeqOptions(seq));
      }
    }
    else
    {
      String seqName = selectedSequence.getDisplayId(false);
      seqName = seqName.length() <= 40 ? seqName : seqName.substring(0, 39);
      lbl_associateSeq.setText(seqName);
      lbl_associateSeq.setVisible(true);
      cmb_assSeq.setVisible(false);
    }
  }

  public boolean isStructuresDiscovered()
  {
    return discoveredStructuresSet != null
            && !discoveredStructuresSet.isEmpty();
  }

  public Collection<FTSData> getDiscoveredStructuresSet()
  {
    return discoveredStructuresSet;
  }

  @Override
  protected void txt_search_ActionPerformed()
  {
    new Thread()
    {
      @Override
      public void run()
      {
        errorWarning.setLength(0);
        isValidPBDEntry = false;
        if (txt_search.getText().length() > 0)
        {
          String searchTerm = txt_search.getText().toLowerCase();
          searchTerm = searchTerm.split(":")[0];
          // System.out.println(">>>>> search term : " + searchTerm);
          List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
          FTSRestRequest pdbRequest = new FTSRestRequest();
          pdbRequest.setAllowEmptySeq(false);
          pdbRequest.setResponseSize(1);
          pdbRequest.setFieldToSearchBy("(pdb_id:");
          pdbRequest.setWantedFields(wantedFields);
          pdbRequest.setSearchTerm(searchTerm + ")");
          pdbRequest.setAssociatedSequence(selectedSequence);
          pdbRestCleint = PDBFTSRestClient.getInstance();
          wantedFields.add(pdbRestCleint.getPrimaryKeyColumn());
          FTSRestResponse resultList;
          try
          {
            resultList = pdbRestCleint.executeRequest(pdbRequest);
          } catch (Exception e)
          {
            errorWarning.append(e.getMessage());
            return;
          } finally
          {
            validateSelections();
          }
          if (resultList.getSearchSummary() != null
                  && resultList.getSearchSummary().size() > 0)
          {
            isValidPBDEntry = true;
          }
        }
        validateSelections();
      }
    }.start();
  }

  @Override
  public void tabRefresh()
  {
    if (selectedSequences != null)
    {
      Thread refreshThread = new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          fetchStructuresMetaData();
          filterResultSet(((FilterOption) cmb_filterOption
                  .getSelectedItem()).getValue());
        }
      });
      refreshThread.start();
    }
  }

  public class PDBEntryTableModel extends AbstractTableModel
  {
    String[] columns = { "Ref Sequence", "PDB Id", "Chain", "Type", "File" };

    private List<CachedPDB> pdbEntries;

    public PDBEntryTableModel(List<CachedPDB> pdbEntries)
    {
      this.pdbEntries = new ArrayList<CachedPDB>(pdbEntries);
    }

    @Override
    public String getColumnName(int columnIndex)
    {
      return columns[columnIndex];
    }

    @Override
    public int getRowCount()
    {
      return pdbEntries.size();
    }

    @Override
    public int getColumnCount()
    {
      return columns.length;
    }

    @Override
    public boolean isCellEditable(int row, int column)
    {
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {
      Object value = "??";
      CachedPDB entry = pdbEntries.get(rowIndex);
      switch (columnIndex)
      {
      case 0:
        value = entry.getSequence();
        break;
      case 1:
        value = entry.getPdbEntry();
        break;
      case 2:
        value = entry.getPdbEntry().getChainCode() == null ? "_" : entry
                .getPdbEntry().getChainCode();
        break;
      case 3:
        value = entry.getPdbEntry().getType();
        break;
      case 4:
        value = entry.getPdbEntry().getFile();
        break;
      }
      return value;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex)
    {
      return columnIndex == 0 ? SequenceI.class : PDBEntry.class;
    }

    public CachedPDB getPDBEntryAt(int row)
    {
      return pdbEntries.get(row);
    }

  }

  private class CachedPDB
  {
    private SequenceI sequence;

    private PDBEntry pdbEntry;

    public CachedPDB(SequenceI sequence, PDBEntry pdbEntry)
    {
      this.sequence = sequence;
      this.pdbEntry = pdbEntry;
    }

    public SequenceI getSequence()
    {
      return sequence;
    }

    public PDBEntry getPdbEntry()
    {
      return pdbEntry;
    }

  }

  private IProgressIndicator progressBar;

  @Override
  public void setProgressBar(String message, long id)
  {
    progressBar.setProgressBar(message, id);
  }

  @Override
  public void registerHandler(long id, IProgressIndicatorHandler handler)
  {
    progressBar.registerHandler(id, handler);
  }

  @Override
  public boolean operationInProgress()
  {
    return progressBar.operationInProgress();
  }
}
