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

package jalview.fts.service.uniprot;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.fts.core.GFTSPanel;
import jalview.gui.Help;
import jalview.gui.Help.HelpId;
import jalview.gui.SequenceFetcher;
import jalview.util.MessageManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.help.HelpSetException;

@SuppressWarnings("serial")
public class UniprotFTSPanel extends GFTSPanel
{

  private static String defaultFTSFrameTitle = MessageManager
          .getString("label.uniprot_sequence_fetcher");

  private static Map<String, Integer> tempUserPrefs = new HashMap<>();

  private static final String UNIPROT_FTS_CACHE_KEY = "CACHE.UNIPROT_FTS";

  private static final String UNIPROT_AUTOSEARCH = "FTS.UNIPROT.AUTOSEARCH";

  public UniprotFTSPanel(SequenceFetcher fetcher)
  {
    super(fetcher);
    pageLimit = UniProtFTSRestClient.getInstance()
            .getDefaultResponsePageSize();
    this.seqFetcher = fetcher;
    this.progressIndicator = (fetcher == null) ? null
            : fetcher.getProgressIndicator();
  }

  @Override
  public void searchAction(boolean isFreshSearch)
  {
    mainFrame.requestFocusInWindow();
    if (isFreshSearch)
    {
      offSet = 0;
    }
    new Thread()
    {
      @Override
      public void run()
      {
        reset();
        String searchInput = getTypedText();
        if (searchInput.length() > 0)
        {
          setSearchInProgress(true);
          long startTime = System.currentTimeMillis();
          searchInput = getTypedText();
          String searchTarget = ((FTSDataColumnI) cmb_searchTarget
                  .getSelectedItem()).getAltCode();
          wantedFields = UniProtFTSRestClient.getInstance()
                  .getAllDefaultDisplayedFTSDataColumns();
          String searchTerm = decodeSearchTerm(searchInput, searchTarget);

          FTSRestRequest request = new FTSRestRequest();
          request.setFieldToSearchBy(searchTarget);
          request.setSearchTerm(searchTerm);
          request.setOffSet(offSet);
          request.setWantedFields(wantedFields);
          FTSRestClientI uniProtRestClient = UniProtFTSRestClient
                  .getInstance();
          FTSRestResponse resultList;
          try
          {
            resultList = uniProtRestClient.executeRequest(request);
          } catch (Exception e)
          {
            setErrorMessage(e.getMessage());
            checkForErrors();
            setSearchInProgress(false);
            return;
          }

          if (resultList.getSearchSummary() != null
                  && resultList.getSearchSummary().size() > 0)
          {
            getResultTable().setModel(FTSRestResponse.getTableModel(request,
                    resultList.getSearchSummary()));
            FTSRestResponse.configureTableColumn(getResultTable(),
                    wantedFields, tempUserPrefs);
            getResultTable().setVisible(true);
          }

          long endTime = System.currentTimeMillis();
          totalResultSetCount = resultList.getNumberOfItemsFound();
          resultSetCount = resultList.getSearchSummary() == null ? 0
                  : resultList.getSearchSummary().size();
          String result = (resultSetCount > 0)
                  ? MessageManager.getString("label.results")
                  : MessageManager.getString("label.result");
          if (isPaginationEnabled() && resultSetCount > 0)
          {
            updateSearchFrameTitle(defaultFTSFrameTitle + " - " + result
                    + " "
                    + totalNumberformatter.format((Number) (offSet + 1))
                    + " to "
                    + totalNumberformatter
                            .format((Number) (offSet + resultSetCount))
                    + " of "
                    + totalNumberformatter
                            .format((Number) totalResultSetCount)
                    + " " + " (" + (endTime - startTime) + " milli secs)");
          }
          else
          {
            updateSearchFrameTitle(defaultFTSFrameTitle + " - "
                    + resultSetCount + " " + result + " ("
                    + (endTime - startTime) + " milli secs)");
          }
          setSearchInProgress(false);
          refreshPaginatorState();
          updateSummaryTableSelections();
        }
        txt_search.updateCache();
      }
    }.start();

  }

  public String decodeSearchTerm(String enteredText, String targetField)
  {
    int searchTargetLength = targetField.equalsIgnoreCase("Search All") ? 0
            : targetField.length() + 1;
    String searchTarget = targetField.equalsIgnoreCase("Search All") ? ""
            : targetField + ":";
    String foundSearchTerms = enteredText;
    StringBuilder foundSearchTermsBuilder = new StringBuilder();
    if (enteredText.contains(";"))
    {
      String[] searchTerms = enteredText.split(";");
      for (String searchTerm : searchTerms)
      {
        foundSearchTermsBuilder.append(searchTarget).append(searchTerm)
                .append(" OR ");
      }
      int endIndex = foundSearchTermsBuilder.lastIndexOf(" OR ");
      foundSearchTerms = foundSearchTermsBuilder.toString();
      if (foundSearchTerms.contains(" OR "))
      {
        foundSearchTerms = foundSearchTerms.substring(searchTargetLength,
                endIndex);
      }
    }
    return foundSearchTerms;
  }

  @Override
  public boolean isPaginationEnabled()
  {
    return true;
  }

  @Override
  public void okAction()
  {
    disableActionButtons();
    StringBuilder selectedIds = new StringBuilder();
    HashSet<String> selectedIdsSet = new HashSet<>();
    int primaryKeyColIndex = 0;
    try
    {
      primaryKeyColIndex = getFTSRestClient()
              .getPrimaryKeyColumIndex(wantedFields, false);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    int[] selectedRows = getResultTable().getSelectedRows();
    for (int summaryRow : selectedRows)
    {
      String idStr = getResultTable()
              .getValueAt(summaryRow, primaryKeyColIndex).toString();
      selectedIdsSet.add(idStr);
    }
    selectedIdsSet.addAll(paginatorCart);
    for (String selectedId : selectedIdsSet)
    {
      selectedIds.append(selectedId).append(";");
    }

    String ids = selectedIds.toString();
    // System.out.println(">>>>>>>>>>>>>>>> selected Ids: " + ids);
    seqFetcher.getTextArea().setText(ids);
    Thread worker = new Thread(seqFetcher);
    worker.start();
    delayAndEnableActionButtons();
  }

  @Override
  public FTSRestClientI getFTSRestClient()
  {
    return UniProtFTSRestClient.getInstance();
  }

  @Override
  public String getFTSFrameTitle()
  {
    return defaultFTSFrameTitle;
  }

  @Override
  public Map<String, Integer> getTempUserPrefs()
  {
    return tempUserPrefs;
  }

  @Override
  public String getCacheKey()
  {
    return UNIPROT_FTS_CACHE_KEY;
  }

  @Override
  public String getAutosearchPreference()
  {
    return UNIPROT_AUTOSEARCH;
  }

  @Override
  protected void showHelp()
  {
    try
    {
      Help.showHelpWindow(HelpId.UniprotFts);
    } catch (HelpSetException e1)
    {
      e1.printStackTrace();
    }
  }
}
