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

package jalview.fts.core;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.GFTSPanelI;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.gui.Desktop;
import jalview.gui.IProgressIndicator;
import jalview.gui.JvSwingUtils;
import jalview.gui.SequenceFetcher;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * This class provides the swing GUI layout for FTS Panel and implements most of
 * the contracts defined in GFSPanelI
 * 
 * @author tcnofoegbu
 *
 */

@SuppressWarnings("serial")
public abstract class GFTSPanel extends JPanel implements GFTSPanelI
{
  protected JInternalFrame mainFrame = new JInternalFrame(
          getFTSFrameTitle());

  protected IProgressIndicator progressIndicator;

  protected JComboBox<FTSDataColumnI> cmb_searchTarget = new JComboBox<FTSDataColumnI>();

  protected JButton btn_ok = new JButton();

  protected JButton btn_back = new JButton();

  protected JButton btn_cancel = new JButton();

  protected JTextField txt_search = new JTextField(30);

  protected SequenceFetcher seqFetcher;

  protected Collection<FTSDataColumnI> wantedFields;

  private String lastSearchTerm = "";

  protected JButton btn_next_page = new JButton();

  protected JButton btn_prev_page = new JButton();

  protected StringBuilder errorWarning = new StringBuilder();

  protected ImageIcon warningImage = new ImageIcon(getClass().getResource(
          "/images/warning.gif"));

  protected ImageIcon loadingImage = new ImageIcon(getClass().getResource(
          "/images/loading.gif"));

  protected ImageIcon balnkPlaceholderImage = new ImageIcon(getClass()
          .getResource("/images/blank_16x16_placeholder.png"));

  protected JLabel lbl_warning = new JLabel(warningImage);

  protected JLabel lbl_loading = new JLabel(loadingImage);

  protected JLabel lbl_blank = new JLabel(balnkPlaceholderImage);

  private JTabbedPane tabbedPane = new JTabbedPane();

  private JPanel pnl_actions = new JPanel();

  private JPanel pnl_results = new JPanel(new CardLayout());

  private JPanel pnl_inputs = new JPanel();

  private BorderLayout mainLayout = new BorderLayout();

  protected Object[] previousWantedFields;

  protected int resultSetCount;

  protected int totalResultSetCount;

  protected int offSet;

  protected int pageLimit;

  protected HashSet<String> paginatorCart = new HashSet<String>();

  protected static final DecimalFormat totalNumberformatter = new DecimalFormat(
          "###,###");

  private JTable tbl_summary = new JTable()
  {
    private boolean inLayout;

    @Override
    public boolean getScrollableTracksViewportWidth()
    {
      return hasExcessWidth();

    }

    @Override
    public void doLayout()
    {
      if (hasExcessWidth())
      {
        autoResizeMode = AUTO_RESIZE_SUBSEQUENT_COLUMNS;
      }
      inLayout = true;
      super.doLayout();
      inLayout = false;
      autoResizeMode = AUTO_RESIZE_OFF;
    }

    protected boolean hasExcessWidth()
    {
      return getPreferredSize().width < getParent().getWidth();
    }

    @Override
    public void columnMarginChanged(ChangeEvent e)
    {
      if (isEditing())
      {
        removeEditor();
      }
      TableColumn resizingColumn = getTableHeader().getResizingColumn();
      // Need to do this here, before the parent's
      // layout manager calls getPreferredSize().
      if (resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF
              && !inLayout)
      {
        resizingColumn.setPreferredWidth(resizingColumn.getWidth());
        String colHeader = resizingColumn.getHeaderValue().toString();
        getTempUserPrefs().put(colHeader, resizingColumn.getWidth());
      }
      resizeAndRepaint();
    }

    @Override
    public String getToolTipText(MouseEvent evt)
    {
      String toolTipText = null;
      java.awt.Point pnt = evt.getPoint();
      int rowIndex = rowAtPoint(pnt);
      int colIndex = columnAtPoint(pnt);

      try
      {
        if (getValueAt(rowIndex, colIndex) == null)
        {
          return null;
        }
        toolTipText = getValueAt(rowIndex, colIndex).toString();

      } catch (Exception e)
      {
        e.printStackTrace();
      }
      toolTipText = (toolTipText == null ? null
              : (toolTipText.length() > 500 ? JvSwingUtils.wrapTooltip(
                      true, toolTipText.subSequence(0, 500) + "...")
                      : JvSwingUtils.wrapTooltip(true, toolTipText)));

      return toolTipText;
    }
  };

  protected JScrollPane scrl_searchResult = new JScrollPane(tbl_summary);

  public GFTSPanel()
  {
    try
    {
      jbInit();
      mainFrame.addFocusListener(new FocusAdapter()
      {
        @Override
        public void focusGained(FocusEvent e)
        {
          txt_search.requestFocusInWindow();
        }
      });
      mainFrame.invalidate();
      mainFrame.pack();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Initializes the GUI default properties
   * 
   * @throws Exception
   */
  private void jbInit() throws Exception
  {
    Integer width = getTempUserPrefs().get("FTSPanel.width") == null ? 800
            : getTempUserPrefs().get("FTSPanel.width");
    Integer height = getTempUserPrefs().get("FTSPanel.height") == null ? 400
            : getTempUserPrefs().get("FTSPanel.height");
    lbl_warning.setVisible(false);
    lbl_warning.setFont(new java.awt.Font("Verdana", 0, 12));
    lbl_loading.setVisible(false);
    lbl_loading.setFont(new java.awt.Font("Verdana", 0, 12));
    lbl_blank.setVisible(true);
    lbl_blank.setFont(new java.awt.Font("Verdana", 0, 12));

    tbl_summary.setAutoCreateRowSorter(true);
    tbl_summary.getTableHeader().setReorderingAllowed(false);
    tbl_summary.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        validateSelection();
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        validateSelection();
      }
    });
    tbl_summary.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        validateSelection();
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_ESCAPE: // escape key
          btn_back_ActionPerformed();
          break;
        case KeyEvent.VK_ENTER: // enter key
          if (btn_ok.isEnabled())
          {
            okAction();
          }
          evt.consume();
          break;
        case KeyEvent.VK_TAB: // tab key
          if (evt.isShiftDown())
          {
            tabbedPane.requestFocus();
          }
          else
          {
            btn_back.requestFocus();
          }
          evt.consume();
          break;
        default:
          return;
        }
      }
    });

    btn_back.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_back.setText(MessageManager.getString("action.back"));
    btn_back.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        btn_back_ActionPerformed();
      }
    });
    btn_back.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          btn_back_ActionPerformed();
        }
      }
    });

    btn_ok.setEnabled(false);
    btn_ok.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_ok.setText(MessageManager.getString("action.ok"));
    btn_ok.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        okAction();
      }
    });
    btn_ok.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          okAction();
        }
      }
    });
    btn_next_page.setEnabled(false);
    btn_next_page.setToolTipText(MessageManager
            .getString("label.next_page_tooltip"));
    btn_next_page.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_next_page.setText(MessageManager.getString("action.next_page"));
    btn_next_page.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        nextPageAction();
      }
    });
    btn_next_page.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          nextPageAction();
        }
      }
    });

    btn_prev_page.setEnabled(false);
    btn_prev_page.setToolTipText(MessageManager
            .getString("label.prev_page_tooltip"));
    btn_prev_page.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_prev_page.setText(MessageManager.getString("action.prev_page"));
    btn_prev_page.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        prevPageAction();
      }
    });
    btn_prev_page.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          prevPageAction();
        }
      }
    });

    if (isPaginationEnabled())
    {
      btn_prev_page.setVisible(true);
      btn_next_page.setVisible(true);
    }
    else
    {
      btn_prev_page.setVisible(false);
      btn_next_page.setVisible(false);
    }

    btn_cancel.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_cancel.setText(MessageManager.getString("action.cancel"));
    btn_cancel.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        btn_cancel_ActionPerformed();
      }
    });
    btn_cancel.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          btn_cancel_ActionPerformed();
        }
      }
    });
    scrl_searchResult.setPreferredSize(new Dimension(width, height));

    cmb_searchTarget.setFont(new java.awt.Font("Verdana", 0, 12));
    cmb_searchTarget.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        String tooltipText;
        if ("all".equalsIgnoreCase(getCmbSearchTarget().getSelectedItem()
                .toString()))
        {
          tooltipText = MessageManager.getString("label.search_all");
        }
        else if ("pdb id".equalsIgnoreCase(getCmbSearchTarget()
                .getSelectedItem().toString()))
        {
          tooltipText = MessageManager
                  .getString("label.separate_multiple_accession_ids");
        }
        else
        {
          tooltipText = MessageManager.formatMessage(
                  "label.separate_multiple_query_values",
                  new Object[] { getCmbSearchTarget().getSelectedItem()
                          .toString() });
        }
        txt_search.setToolTipText(JvSwingUtils.wrapTooltip(true,
                tooltipText));
        searchAction(true);
      }
    });

    populateCmbSearchTargetOptions();

    txt_search.setFont(new java.awt.Font("Verdana", 0, 12));

    txt_search.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent e)
      {
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          if (txt_search.getText() == null
                  || txt_search.getText().isEmpty())
          {
            return;
          }
          String primaryKeyName = getFTSRestClient().getPrimaryKeyColumn()
                  .getName();
          if (primaryKeyName.equalsIgnoreCase(getCmbSearchTarget()
                  .getSelectedItem().toString()))
          {
            transferToSequenceFetcher(txt_search.getText());
          }
        }
      }
    });

    final DeferredTextInputListener listener = new DeferredTextInputListener(
            1500, new ActionListener()
            {
              @Override
              public void actionPerformed(ActionEvent e)
              {
                if (!getTypedText().equalsIgnoreCase(lastSearchTerm))
                {
                  searchAction(true);
                  paginatorCart.clear();
                  lastSearchTerm = getTypedText();
                }
              }
            }, false);
    txt_search.getDocument().addDocumentListener(listener);
    txt_search.addFocusListener(new FocusListener()
    {
      @Override
      public void focusGained(FocusEvent e)
      {
        listener.start();
      }

      @Override
      public void focusLost(FocusEvent e)
      {
        // listener.stop();
      }
    });

    final String searchTabTitle = MessageManager
            .getString("label.search_result");
    final String configureCols = MessageManager
            .getString("label.configure_displayed_columns");
    ChangeListener changeListener = new ChangeListener()
    {
      @Override
      public void stateChanged(ChangeEvent changeEvent)
      {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent
                .getSource();
        int index = sourceTabbedPane.getSelectedIndex();

        btn_back.setVisible(true);
        btn_cancel.setVisible(true);
        btn_ok.setVisible(true);
        if (sourceTabbedPane.getTitleAt(index).equals(configureCols))
        {
          btn_back.setVisible(false);
          btn_cancel.setVisible(false);
          btn_ok.setVisible(false);
          btn_back.setEnabled(false);
          btn_cancel.setEnabled(false);
          btn_ok.setEnabled(false);
          btn_next_page.setEnabled(false);
          btn_prev_page.setEnabled(false);
          txt_search.setEnabled(false);
          cmb_searchTarget.setEnabled(false);
          previousWantedFields = getFTSRestClient()
                  .getAllDefaultDisplayedFTSDataColumns().toArray(
                          new Object[0]);
        }
        if (sourceTabbedPane.getTitleAt(index).equals(searchTabTitle))
        {
          btn_back.setEnabled(true);
          btn_cancel.setEnabled(true);
          refreshPaginatorState();
          txt_search.setEnabled(true);
          cmb_searchTarget.setEnabled(true);
          if (wantedFieldsUpdated())
          {
            searchAction(true);
            paginatorCart.clear();
          }
          else
          {
            validateSelection();
          }
        }
      }
    };
    tabbedPane.addChangeListener(changeListener);
    tabbedPane.setPreferredSize(new Dimension(width, height));
    tabbedPane.add(searchTabTitle, scrl_searchResult);
    tabbedPane.add(configureCols, new FTSDataColumnPreferences(
            PreferenceSource.SEARCH_SUMMARY, getFTSRestClient()));

    pnl_actions.add(btn_back);
    pnl_actions.add(btn_ok);
    pnl_actions.add(btn_cancel);

    pnl_results.add(tabbedPane);
    pnl_inputs.add(cmb_searchTarget);
    pnl_inputs.add(txt_search);
    pnl_inputs.add(lbl_loading);
    pnl_inputs.add(lbl_warning);
    pnl_inputs.add(lbl_blank);
    pnl_inputs.add(btn_prev_page);
    pnl_inputs.add(btn_next_page);

    this.setLayout(mainLayout);
    this.add(pnl_inputs, java.awt.BorderLayout.NORTH);
    this.add(pnl_results, java.awt.BorderLayout.CENTER);
    this.add(pnl_actions, java.awt.BorderLayout.SOUTH);
    mainFrame.setVisible(true);
    mainFrame.setContentPane(this);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    mainFrame
            .addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
            {
              @Override
              public void internalFrameClosing(InternalFrameEvent e)
              {
                closeAction();
              }
            });
    mainFrame.setVisible(true);
    mainFrame.setContentPane(this);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Integer x = getTempUserPrefs().get("FTSPanel.x");
    Integer y = getTempUserPrefs().get("FTSPanel.y");
    if (x != null && y != null)
    {
      mainFrame.setLocation(x, y);
    }
    Desktop.addInternalFrame(mainFrame, getFTSFrameTitle(), width, height);
  }

  protected void closeAction()
  {
    // System.out.println(">>>>>>>>>> closing internal frame!!!");
    // System.out.println("width : " + this.getWidth());
    // System.out.println("heigh : " + this.getHeight());
    // System.out.println("x : " + mainFrame.getX());
    // System.out.println("y : " + mainFrame.getY());
    getTempUserPrefs().put("FTSPanel.width", this.getWidth());
    getTempUserPrefs().put("FTSPanel.height", pnl_results.getHeight());
    getTempUserPrefs().put("FTSPanel.x", mainFrame.getX());
    getTempUserPrefs().put("FTSPanel.y", mainFrame.getY());
    mainFrame.dispose();
  }

  public class DeferredTextInputListener implements DocumentListener
  {
    private final Timer swingTimer;

    public DeferredTextInputListener(int timeOut, ActionListener listener,
            boolean repeats)
    {
      swingTimer = new Timer(timeOut, listener);
      swingTimer.setRepeats(repeats);
    }

    public void start()
    {
      swingTimer.start();
    }

    public void stop()
    {
      swingTimer.stop();
    }

    @Override
    public void insertUpdate(DocumentEvent e)
    {
      swingTimer.restart();
    }

    @Override
    public void removeUpdate(DocumentEvent e)
    {
      swingTimer.restart();
    }

    @Override
    public void changedUpdate(DocumentEvent e)
    {
      swingTimer.restart();
    }

  }

  public boolean wantedFieldsUpdated()
  {
    if (previousWantedFields == null)
    {
      return true;
    }

    return Arrays.equals(getFTSRestClient()
            .getAllDefaultDisplayedFTSDataColumns().toArray(new Object[0]),
            previousWantedFields) ? false : true;

  }

  public void validateSelection()
  {
    if (tbl_summary.getSelectedRows().length > 0
            || !paginatorCart.isEmpty())
    {
      btn_ok.setEnabled(true);
    }
    else
    {
      btn_ok.setEnabled(false);
    }
  }

  public JComboBox<FTSDataColumnI> getCmbSearchTarget()
  {
    return cmb_searchTarget;
  }

  public JTextField getTxtSearch()
  {
    return txt_search;
  }

  public JInternalFrame getMainFrame()
  {
    return mainFrame;
  }

  protected void delayAndEnableActionButtons()
  {
    new Thread()
    {
      @Override
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

  protected void checkForErrors()
  {
    lbl_warning.setVisible(false);
    lbl_blank.setVisible(true);
    if (errorWarning.length() > 0)
    {
      lbl_loading.setVisible(false);
      lbl_blank.setVisible(false);
      lbl_warning.setToolTipText(JvSwingUtils.wrapTooltip(true,
              errorWarning.toString()));
      lbl_warning.setVisible(true);
    }
  }

  protected void btn_back_ActionPerformed()
  {
    closeAction();
    new SequenceFetcher(progressIndicator);
  }

  protected void disableActionButtons()
  {
    btn_ok.setEnabled(false);
    btn_back.setEnabled(false);
    btn_cancel.setEnabled(false);
  }

  protected void btn_cancel_ActionPerformed()
  {
    closeAction();
  }

  /**
   * Populates search target combo-box options
   */
  public void populateCmbSearchTargetOptions()
  {
    List<FTSDataColumnI> searchableTargets = new ArrayList<FTSDataColumnI>();
    try
    {
      Collection<FTSDataColumnI> foundFTSTargets = getFTSRestClient()
              .getSearchableDataColumns();
      searchableTargets.addAll(foundFTSTargets);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    Collections.sort(searchableTargets, new Comparator<FTSDataColumnI>()
    {
      @Override
      public int compare(FTSDataColumnI o1, FTSDataColumnI o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });

    for (FTSDataColumnI searchTarget : searchableTargets)
    {
      cmb_searchTarget.addItem(searchTarget);
    }
  }

  public void transferToSequenceFetcher(String ids)
  {
    // mainFrame.dispose();
    seqFetcher.getTextArea().setText(ids);
    Thread worker = new Thread(seqFetcher);
    worker.start();
  }

  @Override
  public String getTypedText()
  {
    return txt_search.getText().trim();
  }

  @Override
  public JTable getResultTable()
  {
    return tbl_summary;
  }

  public void reset()
  {
    lbl_loading.setVisible(false);
    errorWarning.setLength(0);
    lbl_warning.setVisible(false);
    lbl_blank.setVisible(true);
    btn_ok.setEnabled(false);
    mainFrame.setTitle(getFTSFrameTitle());
    referesh();
    tbl_summary.setModel(new DefaultTableModel());
    tbl_summary.setVisible(false);
  }

  @Override
  public void setPrevPageButtonEnabled(boolean isEnabled)
  {
    btn_prev_page.setEnabled(isEnabled);
  }

  @Override
  public void setNextPageButtonEnabled(boolean isEnabled)
  {
    btn_next_page.setEnabled(isEnabled);
  }

  @Override
  public void setErrorMessage(String message)
  {
    errorWarning.append(message);
  }

  @Override
  public void updateSearchFrameTitle(String title)
  {
    mainFrame.setTitle(title);
  }

  @Override
  public void setSearchInProgress(Boolean isSearchInProgress)
  {
    lbl_blank.setVisible(!isSearchInProgress);
    lbl_loading.setVisible(isSearchInProgress);
  }

  @Override
  public void prevPageAction()
  {
    updatePaginatorCart();
    if (offSet >= pageLimit)
    {
      offSet = offSet - pageLimit;
      searchAction(false);
    }
    else
    {
      refreshPaginatorState();
    }
  }

  @Override
  public void nextPageAction()
  {
    updatePaginatorCart();
    offSet = offSet + pageLimit;
    searchAction(false);
  }

  public void updatePaginatorCart()
  {
    int primaryKeyColIndex = 0;
    JTable resultTable = getResultTable();
    int totalRows = resultTable.getRowCount();
    try
    {
      primaryKeyColIndex = getFTSRestClient().getPrimaryKeyColumIndex(
              wantedFields, false);
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    for (int row = 0; row < totalRows; row++)
    {
      String id = (String) resultTable.getValueAt(row, primaryKeyColIndex);
      if (paginatorCart.contains(id))
      {
        paginatorCart.remove(id);
      }
    }
    int[] selectedRows = resultTable.getSelectedRows();
    for (int summaryRow : selectedRows)
    {
      String idStr = resultTable.getValueAt(summaryRow, primaryKeyColIndex)
              .toString();
      paginatorCart.add(idStr);
    }
    // System.out.println("Paginator shopping cart size : "
    // + paginatorCart.size());
  }

  public void updateSummaryTableSelections()
  {
    JTable resultTable = getResultTable();
    if (paginatorCart.isEmpty())
    {
      return;
    }
    int primaryKeyColIndex = 0;
    try
    {
      primaryKeyColIndex = getFTSRestClient().getPrimaryKeyColumIndex(
              wantedFields, false);
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    // System.out.println(">>>>>> got here : 1");
    int totalRows = resultTable.getRowCount();
    // resultTable.clearSelection();
    for (int row = 0; row < totalRows; row++)
    {
      String id = (String) resultTable.getValueAt(row, primaryKeyColIndex);
      if (paginatorCart.contains(id))
      {
        resultTable.addRowSelectionInterval(row, row);
      }
    }
    validateSelection();
  }

  public void refreshPaginatorState()
  {
    // System.out.println("resultSet count : " + resultSetCount);
    // System.out.println("offSet : " + offSet);
    // System.out.println("page limit : " + pageLimit);
    setPrevPageButtonEnabled(false);
    setNextPageButtonEnabled(false);
    if (resultSetCount == 0 && pageLimit == 0)
    {
      return;
    }
    if (resultSetCount >= pageLimit)
    {
      setNextPageButtonEnabled(true);
    }
    if (offSet >= pageLimit)
    {
      setPrevPageButtonEnabled(true);
    }
  }

  public void referesh()
  {
    mainFrame.setTitle(getFTSFrameTitle());
  }

}
