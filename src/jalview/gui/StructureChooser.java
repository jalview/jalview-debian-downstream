/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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

import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
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
import jalview.io.DataSourceType;
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
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

/**
 * Provides the behaviors for the Structure chooser Panel
 * 
 * @author tcnofoegbu
 *
 */
@SuppressWarnings("serial")
public class StructureChooser extends GStructureChooser
        implements IProgressIndicator
{
  private static final String AUTOSUPERIMPOSE = "AUTOSUPERIMPOSE";

  private static int MAX_QLENGTH = 7820;

  private SequenceI selectedSequence;

  private SequenceI[] selectedSequences;

  private IProgressIndicator progressIndicator;

  private Collection<FTSData> discoveredStructuresSet;

  private FTSRestRequest lastPdbRequest;

  private FTSRestClientI pdbRestCleint;

  private String selectedPdbFileName;

  private boolean isValidPBDEntry;

  private boolean cachedPDBExists;

  private static StructureViewer lastTargetedView = null;

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
  protected void init()
  {
    if (!Jalview.isHeadlessMode())
    {
      progressBar = new ProgressBar(this.statusPanel, this.statusBar);
    }

    chk_superpose.setSelected(Cache.getDefault(AUTOSUPERIMPOSE, true));

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
        updateProgressIndicator(MessageManager.getString(
                "status.searching_for_pdb_structures"), startTime);
        fetchStructuresMetaData();
        // revise filter options if no results were found
        populateFilterComboBox(isStructuresDiscovered(), cachedPDBExists);
        discoverStructureViews();
        updateProgressIndicator(null, startTime);
        mainFrame.setVisible(true);
        updateCurrentView();
      }
    });
    discoverPDBStructuresThread.start();
  }

  /**
   * Builds a drop-down choice list of existing structure viewers to which new
   * structures may be added. If this list is empty then it, and the 'Add'
   * button, are hidden.
   */
  private void discoverStructureViews()
  {
    if (Desktop.instance != null)
    {
      targetView.removeAllItems();
      if (lastTargetedView != null && !lastTargetedView.isVisible())
      {
        lastTargetedView = null;
      }
      int linkedViewsAt = 0;
      for (StructureViewerBase view : Desktop.instance
              .getStructureViewers(null, null))
      {
        StructureViewer viewHandler = (lastTargetedView != null
                && lastTargetedView.sview == view) ? lastTargetedView
                        : StructureViewer.reconfigure(view);

        if (view.isLinkedWith(ap))
        {
          targetView.insertItemAt(viewHandler,
                  linkedViewsAt++);
        }
        else
        {
          targetView.addItem(viewHandler);
        }
      }

      /*
       * show option to Add to viewer if at least 1 viewer found
       */
      targetView.setVisible(false);
      if (targetView.getItemCount() > 0)
      {
        targetView.setVisible(true);
        if (lastTargetedView != null)
        {
          targetView.setSelectedItem(lastTargetedView);
        }
        else
        {
          targetView.setSelectedIndex(0);
        }
      }
      btn_add.setVisible(targetView.isVisible());
    }
  }

  /**
   * Updates the progress indicator with the specified message
   * 
   * @param message
   *          displayed message for the operation
   * @param id
   *          unique handle for this indicator
   */
  protected void updateProgressIndicator(String message, long id)
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
  void fetchStructuresMetaData()
  {
    long startTime = System.currentTimeMillis();
    pdbRestCleint = PDBFTSRestClient.getInstance();
    Collection<FTSDataColumnI> wantedFields = pdbDocFieldPrefs
            .getStructureSummaryFields();

    discoveredStructuresSet = new LinkedHashSet<>();
    HashSet<String> errors = new HashSet<>();
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
      getResultTable().setModel(FTSRestResponse
              .getTableModel(lastPdbRequest, discoveredStructuresSet));
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
        JvOptionPane.showMessageDialog(this, errorMsg.toString(),
                MessageManager.getString("label.pdb_web-service_error"),
                JvOptionPane.ERROR_MESSAGE);
      }
    }
  }

  protected void loadLocalCachedPDBEntries()
  {
    ArrayList<CachedPDB> entries = new ArrayList<>();
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

  static String buildQuery(SequenceI seq)
  {
    boolean isPDBRefsFound = false;
    boolean isUniProtRefsFound = false;
    StringBuilder queryBuilder = new StringBuilder();
    Set<String> seqRefs = new LinkedHashSet<>();
    
    /*
     * note PDBs as DBRefEntry so they are not duplicated in query
     */
    Set<String> pdbids = new HashSet<>();

    if (seq.getAllPDBEntries() != null
            && queryBuilder.length() < MAX_QLENGTH)
    {
      for (PDBEntry entry : seq.getAllPDBEntries())
      {
        if (isValidSeqName(entry.getId()))
        {
          String id = entry.getId().toLowerCase();
          queryBuilder.append("pdb_id:").append(id).append(" OR ");
          isPDBRefsFound = true;
          pdbids.add(id);
        }
      }
    }

    if (seq.getDBRefs() != null && seq.getDBRefs().length != 0)
    {
      for (DBRefEntry dbRef : seq.getDBRefs())
      {
        if (isValidSeqName(getDBRefId(dbRef))
                && queryBuilder.length() < MAX_QLENGTH)
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

            String id = getDBRefId(dbRef).toLowerCase();
            if (!pdbids.contains(id))
            {
              queryBuilder.append("pdb_id:").append(id).append(" OR ");
              isPDBRefsFound = true;
              pdbids.add(id);
            }
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
  static boolean isValidSeqName(String seqName)
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

  static String getDBRefId(DBRefEntry dbRef)
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
  void filterResultSet(final String fieldToFilterBy)
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
        Collection<FTSData> filteredResponse = new HashSet<>();
        HashSet<String> errors = new HashSet<>();

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
          Collection<FTSData> reorderedStructuresSet = new LinkedHashSet<>();
          reorderedStructuresSet.addAll(filteredResponse);
          reorderedStructuresSet.addAll(discoveredStructuresSet);
          getResultTable().setModel(FTSRestResponse
                  .getTableModel(lastPdbRequest, reorderedStructuresSet));

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
            JvOptionPane.showMessageDialog(null, errorMsg.toString(),
                    MessageManager.getString("label.pdb_web-service_error"),
                    JvOptionPane.ERROR_MESSAGE);
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
  protected void pdbFromFile_actionPerformed()
  {
    jalview.io.JalviewFileChooser chooser = new jalview.io.JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.formatMessage("label.select_pdb_file_for",
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
          boolean cachedPDBExist)
  {
    /*
     * temporarily suspend the change listener behaviour
     */
    cmb_filterOption.removeItemListener(this);

    cmb_filterOption.removeAllItems();
    if (haveData)
    {
      cmb_filterOption.addItem(new FilterOption(
              MessageManager.getString("label.best_quality"),
              "overall_quality", VIEWS_FILTER, false));
      cmb_filterOption.addItem(new FilterOption(
              MessageManager.getString("label.best_resolution"),
              "resolution", VIEWS_FILTER, false));
      cmb_filterOption.addItem(new FilterOption(
              MessageManager.getString("label.most_protein_chain"),
              "number_of_protein_chains", VIEWS_FILTER, false));
      cmb_filterOption.addItem(new FilterOption(
              MessageManager.getString("label.most_bound_molecules"),
              "number_of_bound_molecules", VIEWS_FILTER, false));
      cmb_filterOption.addItem(new FilterOption(
              MessageManager.getString("label.most_polymer_residues"),
              "number_of_polymer_residues", VIEWS_FILTER, true));
    }
    cmb_filterOption.addItem(
            new FilterOption(MessageManager.getString("label.enter_pdb_id"),
                    "-", VIEWS_ENTER_ID, false));
    cmb_filterOption.addItem(
            new FilterOption(MessageManager.getString("label.from_file"),
                    "-", VIEWS_FROM_FILE, false));

    if (cachedPDBExist)
    {
      FilterOption cachedOption = new FilterOption(
              MessageManager.getString("label.cached_structures"),
              "-", VIEWS_LOCAL_PDB, false);
      cmb_filterOption.addItem(cachedOption);
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
   * Validates user selection and enables the 'Add' and 'New View' buttons if
   * all parameters are correct (the Add button will only be visible if there is
   * at least one existing structure viewer open). This basically means at least
   * one structure selected and no error messages.
   * <p>
   * The 'Superpose Structures' option is enabled if either more than one
   * structure is selected, or the 'Add' to existing view option is enabled, and
   * disabled if the only option is to open a new view of a single structure.
   */
  @Override
  protected void validateSelections()
  {
    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    btn_add.setEnabled(false);
    String currentView = selectedFilterOpt.getView();
    int selectedCount = 0;
    if (currentView == VIEWS_FILTER)
    {
      selectedCount = getResultTable().getSelectedRows().length;
      if (selectedCount > 0)
      {
        btn_add.setEnabled(true);
      }
    }
    else if (currentView == VIEWS_LOCAL_PDB)
    {
      selectedCount = tbl_local_pdb.getSelectedRows().length;
      if (selectedCount > 0)
      {
        btn_add.setEnabled(true);
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

    btn_newView.setEnabled(btn_add.isEnabled());

    /*
     * enable 'Superpose' option if more than one structure is selected,
     * or there are view(s) available to add structure(s) to
     */
    chk_superpose
            .setEnabled(selectedCount > 1 || targetView.getItemCount() > 0);
  }

  /**
   * Validates inputs from the Manual PDB entry panel
   */
  protected void validateAssociationEnterPdb()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) idInputAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    lbl_pdbManualFetchStatus.setIcon(errorImage);
    lbl_pdbManualFetchStatus.setToolTipText("");
    if (txt_search.getText().length() > 0)
    {
      lbl_pdbManualFetchStatus.setToolTipText(JvSwingUtils.wrapTooltip(true,
              MessageManager.formatMessage("info.no_pdb_entry_found_for",
                      txt_search.getText())));
    }

    if (errorWarning.length() > 0)
    {
      lbl_pdbManualFetchStatus.setIcon(warningImage);
      lbl_pdbManualFetchStatus.setToolTipText(
              JvSwingUtils.wrapTooltip(true, errorWarning.toString()));
    }

    if (selectedSequences.length == 1 || !assSeqOpt.getName()
            .equalsIgnoreCase("-Select Associated Seq-"))
    {
      txt_search.setEnabled(true);
      if (isValidPBDEntry)
      {
        btn_add.setEnabled(true);
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
  protected void validateAssociationFromFile()
  {
    AssociateSeqOptions assSeqOpt = (AssociateSeqOptions) fileChooserAssSeqPanel
            .getCmb_assSeq().getSelectedItem();
    lbl_fromFileStatus.setIcon(errorImage);
    if (selectedSequences.length == 1 || (assSeqOpt != null && !assSeqOpt
            .getName().equalsIgnoreCase("-Select Associated Seq-")))
    {
      btn_pdbFromFile.setEnabled(true);
      if (selectedPdbFileName != null && selectedPdbFileName.length() > 0)
      {
        btn_add.setEnabled(true);
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
  protected void cmbAssSeqStateChanged()
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
   * select structures for viewing by their PDB IDs
   * 
   * @param pdbids
   * @return true if structures were found and marked as selected
   */
  public boolean selectStructure(String... pdbids)
  {
    boolean found = false;

    FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
            .getSelectedItem());
    String currentView = selectedFilterOpt.getView();
    JTable restable = (currentView == VIEWS_FILTER) ? getResultTable()
            : (currentView == VIEWS_LOCAL_PDB) ? tbl_local_pdb : null;

    if (restable == null)
    {
      // can't select (enter PDB ID, or load file - need to also select which
      // sequence to associate with)
      return false;
    }

    int pdbIdColIndex = restable.getColumn("PDB Id").getModelIndex();
    for (int r = 0; r < restable.getRowCount(); r++)
    {
      for (int p = 0; p < pdbids.length; p++)
      {
        if (String.valueOf(restable.getValueAt(r, pdbIdColIndex))
                .equalsIgnoreCase(pdbids[p]))
        {
          restable.setRowSelectionInterval(r, r);
          found = true;
        }
      }
    }
    return found;
  }
  
  /**
   * Handles the 'New View' action
   */
  @Override
  protected void newView_ActionPerformed()
  {
    targetView.setSelectedItem(null);
    showStructures(false);
  }

  /**
   * Handles the 'Add to existing viewer' action
   */
  @Override
  protected void add_ActionPerformed()
  {
    showStructures(false);
  }

  /**
   * structure viewer opened by this dialog, or null
   */
  private StructureViewer sViewer = null;

  public void showStructures(boolean waitUntilFinished)
  {

    final StructureSelectionManager ssm = ap.getStructureSelectionManager();

    final int preferredHeight = pnl_filter.getHeight();

    Runnable viewStruc = new Runnable()
    {
      @Override
      public void run()
      {
        FilterOption selectedFilterOpt = ((FilterOption) cmb_filterOption
                .getSelectedItem());
        String currentView = selectedFilterOpt.getView();
        JTable restable = (currentView == VIEWS_FILTER) ? getResultTable()
                : tbl_local_pdb;

        if (currentView == VIEWS_FILTER)
        {
          int pdbIdColIndex = restable.getColumn("PDB Id")
                  .getModelIndex();
          int refSeqColIndex = restable.getColumn("Ref Sequence")
                  .getModelIndex();
          int[] selectedRows = restable.getSelectedRows();
          PDBEntry[] pdbEntriesToView = new PDBEntry[selectedRows.length];
          int count = 0;
          List<SequenceI> selectedSeqsToView = new ArrayList<>();
          for (int row : selectedRows)
          {
            String pdbIdStr = restable
                    .getValueAt(row, pdbIdColIndex).toString();
            SequenceI selectedSeq = (SequenceI) restable
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
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  selectedSeqs);
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
          List<SequenceI> selectedSeqsToView = new ArrayList<>();
          for (int row : selectedRows)
          {
            PDBEntry pdbEntry = (PDBEntry) tbl_local_pdb.getValueAt(row,
                    pdbIdColIndex);
            pdbEntriesToView[count++] = pdbEntry;
            SequenceI selectedSeq = (SequenceI) tbl_local_pdb
                    .getValueAt(row, refSeqColIndex);
            selectedSeqsToView.add(selectedSeq);
          }
          SequenceI[] selectedSeqs = selectedSeqsToView
                  .toArray(new SequenceI[selectedSeqsToView.size()]);
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  selectedSeqs);
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
          sViewer = launchStructureViewer(ssm, pdbEntriesToView, ap,
                  new SequenceI[]
                  { selectedSequence });
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
                          DataSourceType.FILE, selectedSequence, true,
                          Desktop.instance);

          sViewer = launchStructureViewer(
                  ssm, new PDBEntry[]
                  { fileEntry }, ap,
                  new SequenceI[]
                  { selectedSequence });
        }
        SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            closeAction(preferredHeight);
            mainFrame.dispose();
          }
        });
      }
    };
    Thread runner = new Thread(viewStruc);
    runner.start();
    if (waitUntilFinished)
    {
      while (sViewer == null ? runner.isAlive()
              : (sViewer.sview == null ? true
                      : !sViewer.sview.hasMapping()))
      {
        try
        {
          Thread.sleep(300);
        } catch (InterruptedException ie)
        {

        }
      }
    }
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

  /**
   * Answers a structure viewer (new or existing) configured to superimpose
   * added structures or not according to the user's choice
   * 
   * @param ssm
   * @return
   */
  StructureViewer getTargetedStructureViewer(
          StructureSelectionManager ssm)
  {
    Object sv = targetView.getSelectedItem();

    return sv == null ? new StructureViewer(ssm) : (StructureViewer) sv;
  }

  /**
   * Adds PDB structures to a new or existing structure viewer
   * 
   * @param ssm
   * @param pdbEntriesToView
   * @param alignPanel
   * @param sequences
   * @return
   */
  private StructureViewer launchStructureViewer(
          StructureSelectionManager ssm,
          final PDBEntry[] pdbEntriesToView,
          final AlignmentPanel alignPanel, SequenceI[] sequences)
  {
    long progressId = sequences.hashCode();
    setProgressBar(MessageManager
            .getString("status.launching_3d_structure_viewer"), progressId);
    final StructureViewer theViewer = getTargetedStructureViewer(ssm);
    boolean superimpose = chk_superpose.isSelected();
    theViewer.setSuperpose(superimpose);

    /*
     * remember user's choice of superimpose or not
     */
    Cache.setProperty(AUTOSUPERIMPOSE,
            Boolean.valueOf(superimpose).toString());

    setProgressBar(null, progressId);
    if (SiftsSettings.isMapWithSifts())
    {
      List<SequenceI> seqsWithoutSourceDBRef = new ArrayList<>();
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
        if (seq.getPrimaryDBRefs().isEmpty())
        {
          seqsWithoutSourceDBRef.add(seq);
          continue;
        }
      }
      if (!seqsWithoutSourceDBRef.isEmpty())
      {
        int y = seqsWithoutSourceDBRef.size();
        setProgressBar(MessageManager.formatMessage(
                "status.fetching_dbrefs_for_sequences_without_valid_refs",
                y), progressId);
        SequenceI[] seqWithoutSrcDBRef = seqsWithoutSourceDBRef
                .toArray(new SequenceI[y]);
        DBRefFetcher dbRefFetcher = new DBRefFetcher(seqWithoutSrcDBRef);
        dbRefFetcher.fetchDBRefs(true);

        setProgressBar("Fetch complete.", progressId); // todo i18n
      }
    }
    if (pdbEntriesToView.length > 1)
    {
      setProgressBar(MessageManager.getString(
              "status.fetching_3d_structures_for_selected_entries"),
              progressId);
      theViewer.viewStructures(pdbEntriesToView, sequences, alignPanel);
    }
    else
    {
      setProgressBar(MessageManager.formatMessage(
              "status.fetching_3d_structures_for",
              pdbEntriesToView[0].getId()),progressId);
      theViewer.viewStructures(pdbEntriesToView[0], sequences, alignPanel);
    }
    setProgressBar(null, progressId);
    // remember the last viewer we used...
    lastTargetedView = theViewer;
    return theViewer;
  }

  /**
   * Populates the combo-box used in associating manually fetched structures to
   * a unique sequence when more than one sequence selection is made.
   */
  @Override
  protected void populateCmbAssociateSeqOptions(
          JComboBox<AssociateSeqOptions> cmb_assSeq,
          JLabel lbl_associateSeq)
  {
    cmb_assSeq.removeAllItems();
    cmb_assSeq.addItem(
            new AssociateSeqOptions("-Select Associated Seq-", null));
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

  protected boolean isStructuresDiscovered()
  {
    return discoveredStructuresSet != null
            && !discoveredStructuresSet.isEmpty();
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
          List<FTSDataColumnI> wantedFields = new ArrayList<>();
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
  protected void tabRefresh()
  {
    if (selectedSequences != null)
    {
      Thread refreshThread = new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          fetchStructuresMetaData();
          filterResultSet(
                  ((FilterOption) cmb_filterOption.getSelectedItem())
                          .getValue());
        }
      });
      refreshThread.start();
    }
  }

  public class PDBEntryTableModel extends AbstractTableModel
  {
    String[] columns = { "Ref Sequence", "PDB Id", "Chain", "Type",
        "File" };

    private List<CachedPDB> pdbEntries;

    public PDBEntryTableModel(List<CachedPDB> pdbEntries)
    {
      this.pdbEntries = new ArrayList<>(pdbEntries);
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
        value = entry.getPdbEntry().getChainCode() == null ? "_"
                : entry.getPdbEntry().getChainCode();
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

  public JalviewStructureDisplayI getOpenedStructureViewer()
  {
    return sViewer == null ? null : sViewer.sview;
  }
}
