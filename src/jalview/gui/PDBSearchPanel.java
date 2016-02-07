/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.8.2)
 * Copyright (C) 2014 The Jalview Authors
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

import jalview.jbgui.GPDBSearchPanel;
import jalview.jbgui.PDBDocFieldPreferences;
import jalview.util.MessageManager;
import jalview.ws.dbsources.PDBRestClient;
import jalview.ws.dbsources.PDBRestClient.PDBDocField;
import jalview.ws.uimodel.PDBRestRequest;
import jalview.ws.uimodel.PDBRestResponse;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public class PDBSearchPanel extends GPDBSearchPanel
{
  private SequenceFetcher seqFetcher;

  private IProgressIndicator progressIdicator;

  private Collection<PDBDocField> wantedFields;

  public PDBSearchPanel(SequenceFetcher seqFetcher)
  {
    this.seqFetcher = seqFetcher;
    this.progressIdicator = (seqFetcher == null) ? null : seqFetcher
            .getProgressIndicator();
  }

  /**
   * Action performed when an input is detected on txt_search field.
   */
  @Override
  public void txt_search_ActionPerformed()
  {
    new Thread()
    {
      public void run()
      {
        lbl_loading.setVisible(false);
        errorWarning.setLength(0);
        lbl_warning.setVisible(false);
        btn_ok.setEnabled(false);
        boolean allowEmptySequence = false;
        mainFrame.setTitle(MessageManager
                .getString("label.pdb_sequence_getcher"));
        tbl_summary.setModel(new DefaultTableModel());
        if (txt_search.getText().trim().length() > 0)
        {
          lbl_loading.setVisible(true);
          long startTime = System.currentTimeMillis();

          String searchTarget = ((PDBDocField) cmb_searchTarget
                  .getSelectedItem()).getCode();

          wantedFields = PDBDocFieldPreferences.getSearchSummaryFields();

          String searchTerm = decodeSearchTerm(txt_search.getText(),
                  searchTarget);

          PDBRestRequest request = new PDBRestRequest();
          request.setAllowEmptySeq(allowEmptySequence);
          request.setResponseSize(100);
          request.setFieldToSearchBy("(" + searchTarget + ":");
          request.setSearchTerm(searchTerm + ")");
          request.setWantedFields(wantedFields);
          // System.out.println(">>>>>>>>>>>>>> " + request.getQuery());
          PDBRestClient pdbRestCleint = new PDBRestClient();
          PDBRestResponse resultList;
          try
          {
            resultList = pdbRestCleint.executeRequest(request);
          } catch (Exception e)
          {
            // e.printStackTrace();
            errorWarning.append(e.getMessage());
            checkForErrors();
            return;
          }

          if (resultList.getSearchSummary() != null)
          {
            tbl_summary.setModel(PDBRestResponse.getTableModel(request,
                    resultList.getSearchSummary()));
          }

          long endTime = System.currentTimeMillis();
          int resultSetCount = resultList.getNumberOfItemsFound();
          String result = (resultSetCount > 1) ? MessageManager
                  .getString("label.results") : MessageManager
                  .getString("label.result");
          mainFrame.setTitle(frameTitle + " - " + resultSetCount + " "
                  + result + " (" + (endTime - startTime) + " milli secs)");
          lbl_loading.setVisible(false);
        }
      }
    }.start();
  }

  public static String decodeSearchTerm(String enteredText,
          String targetField)
  {
    String foundSearchTerms = enteredText;
    StringBuilder foundSearchTermsBuilder = new StringBuilder();
    if (enteredText.contains(";"))
    {
      String[] searchTerms = enteredText.split(";");
      for (String searchTerm : searchTerms)
      {
        if (searchTerm.contains(":"))
        {
          foundSearchTermsBuilder.append(targetField).append(":")
                  .append(searchTerm.split(":")[0]).append(" OR ");
        }
        else
        {
          foundSearchTermsBuilder.append(targetField).append(":")
                  .append(searchTerm).append(" OR ");
        }
      }
      int endIndex = foundSearchTermsBuilder.lastIndexOf(" OR ");
      foundSearchTerms = foundSearchTermsBuilder.toString();
      if (foundSearchTerms.contains(" OR "))
      {
        foundSearchTerms = foundSearchTerms.substring(
                targetField.length() + 1, endIndex);
      }
    }
    else if (enteredText.contains(":"))
    {
      foundSearchTerms = foundSearchTerms.split(":")[0];
    }
    return foundSearchTerms;
  }

  @Override
  public void btn_ok_ActionPerformed()
  {
    loadSelectedPDBSequencesToAlignment();
  }

  @Override
  public void btn_back_ActionPerformed()
  {
    mainFrame.dispose();
    new SequenceFetcher(progressIdicator);
  }

  @Override
  public void btn_cancel_ActionPerformed()
  {
    mainFrame.dispose();
  }

  public void transferToSequenceFetcher(String ids)
  {
    // mainFrame.dispose();
    seqFetcher.textArea.setText(ids);
    Thread worker = new Thread(seqFetcher);
    worker.start();
  }

  /**
   * Add the discovered/selected sequences to a target alignment window
   */
  public void loadSelectedPDBSequencesToAlignment()
  {
    // mainFrame.dispose();
    disableActionButtons();
    StringBuilder selectedIds = new StringBuilder();
    HashSet<String> selectedIdsSet = new HashSet<String>();
    int pdbIdCol = PDBRestClient.getPDBIdColumIndex(wantedFields, false);
    int[] selectedRows = tbl_summary.getSelectedRows();
    for (int summaryRow : selectedRows)
    {
      String pdbIdStr = tbl_summary.getValueAt(summaryRow, pdbIdCol)
              .toString();
      String searchTerm = txt_search.getText();
      selectedIdsSet.add(getPDBIdwithSpecifiedChain(pdbIdStr, searchTerm));
    }

    for (String selectedId : selectedIdsSet)
    {
      selectedIds.append(selectedId).append(";");
    }

    String ids = selectedIds.toString();
    // System.out.println(">>>>>>>>>>>>>>>> selected Ids: " + ids);
    seqFetcher.textArea.setText(ids);
    Thread worker = new Thread(seqFetcher);
    worker.start();
    delayAndEnableActionButtons();

  }

  private void disableActionButtons()
  {
    btn_ok.setEnabled(false);
    btn_back.setEnabled(false);
    btn_cancel.setEnabled(false);
  }

  private void delayAndEnableActionButtons()
  {
    new Thread()
    {
      public void run()
      {
        try
        {
          Thread.sleep(1500);
        } catch (InterruptedException e)
        {
          e.printStackTrace();
        }
        btn_ok.setEnabled(true);
        btn_back.setEnabled(true);
        btn_cancel.setEnabled(true);
      }
    }.start();
  }

  public static String getPDBIdwithSpecifiedChain(String pdbId,
          String searchTerm)
  {
    String pdbIdWithChainCode = "";
    if (searchTerm.contains(";"))
    {
      String[] foundTerms = searchTerm.split(";");
      for (String foundTerm : foundTerms)
      {
        if (foundTerm.contains(pdbId))
        {
          pdbIdWithChainCode = foundTerm;
        }
      }
    }
    else if (searchTerm.contains(pdbId))
    {
      pdbIdWithChainCode = searchTerm;
    }
    else
    {
      pdbIdWithChainCode = pdbId;
    }
    return pdbIdWithChainCode;
  }

  /**
   * Populates search target combo-box options
   */
  public void populateCmbSearchTargetOptions()
  {
    List<PDBDocField> searchableTargets = new ArrayList<PDBDocField>();
    searchableTargets.add(PDBDocField.PDB_ID);
    searchableTargets.add(PDBDocField.PFAM_ACCESSION);
    searchableTargets.add(PDBDocField.MOLECULE_TYPE);
    searchableTargets.add(PDBDocField.MOLECULE_NAME);
    searchableTargets.add(PDBDocField.UNIPROT_ACCESSION);
    searchableTargets.add(PDBDocField.GENE_NAME);
    searchableTargets.add(PDBDocField.GENUS);
    searchableTargets.add(PDBDocField.ALL);

    Collections.sort(searchableTargets, new Comparator<PDBDocField>()
    {
      @Override
      public int compare(PDBDocField o1, PDBDocField o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (PDBDocField searchTarget : searchableTargets)
    {
      cmb_searchTarget.addItem(searchTarget);
    }
  }

  public void checkForErrors()
  {
    lbl_warning.setVisible(false);
    if (errorWarning.length() > 0)
    {
      lbl_loading.setVisible(false);
      lbl_warning.setToolTipText(JvSwingUtils.wrapTooltip(true,
              errorWarning.toString()));
      lbl_warning.setVisible(true);
    }
  }
}
