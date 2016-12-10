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

package jalview.jbgui;

import jalview.datamodel.SequenceI;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSDataColumnPreferences;
import jalview.fts.core.FTSDataColumnPreferences.PreferenceSource;
import jalview.fts.service.pdb.PDBFTSRestClient;
import jalview.gui.AlignmentPanel;
import jalview.gui.Desktop;
import jalview.gui.JvSwingUtils;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.InternalFrameEvent;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
/**
 * GUI layout for structure chooser 
 * @author tcnofoegbu
 *
 */
public abstract class GStructureChooser extends JPanel implements
        ItemListener
{
  protected JPanel statusPanel = new JPanel();

  public JLabel statusBar = new JLabel();

  private JPanel pnl_actionsAndStatus = new JPanel(new BorderLayout());

  protected String frameTitle = MessageManager
          .getString("label.structure_chooser");

  protected JInternalFrame mainFrame = new JInternalFrame(frameTitle);

  protected JComboBox<FilterOption> cmb_filterOption = new JComboBox<FilterOption>();

  protected AlignmentPanel ap;

  protected StringBuilder errorWarning = new StringBuilder();

  protected JLabel lbl_result = new JLabel(
          MessageManager.getString("label.select"));

  protected JButton btn_view = new JButton();

  protected JButton btn_cancel = new JButton();

  protected JButton btn_pdbFromFile = new JButton();

  protected JTextField txt_search = new JTextField(14);

  private JPanel pnl_actions = new JPanel();

  private JPanel pnl_main = new JPanel();

  private JPanel pnl_idInput = new JPanel(new FlowLayout());

  private JPanel pnl_fileChooser = new JPanel(new FlowLayout());

  private JPanel pnl_idInputBL = new JPanel(new BorderLayout());

  private JPanel pnl_fileChooserBL = new JPanel(new BorderLayout());

  private JPanel pnl_locPDB = new JPanel(new BorderLayout());

  protected JPanel pnl_switchableViews = new JPanel(new CardLayout());

  protected CardLayout layout_switchableViews = (CardLayout) (pnl_switchableViews
          .getLayout());

  private BorderLayout mainLayout = new BorderLayout();

  protected JCheckBox chk_rememberSettings = new JCheckBox(
          MessageManager.getString("label.dont_ask_me_again"));

  protected JCheckBox chk_invertFilter = new JCheckBox(
          MessageManager.getString("label.invert"));

  protected ImageIcon loadingImage = new ImageIcon(getClass().getResource(
          "/images/loading.gif"));

  protected ImageIcon goodImage = new ImageIcon(getClass().getResource(
          "/images/good.png"));

  protected ImageIcon errorImage = new ImageIcon(getClass().getResource(
          "/images/error.png"));

  protected ImageIcon warningImage = new ImageIcon(getClass().getResource(
          "/images/warning.gif"));

  protected JLabel lbl_warning = new JLabel(warningImage);

  protected JLabel lbl_loading = new JLabel(loadingImage);

  protected JLabel lbl_pdbManualFetchStatus = new JLabel(errorImage);

  protected JLabel lbl_fromFileStatus = new JLabel(errorImage);

  protected AssciateSeqPanel idInputAssSeqPanel = new AssciateSeqPanel();

  protected AssciateSeqPanel fileChooserAssSeqPanel = new AssciateSeqPanel();

  protected static final String VIEWS_FILTER = "VIEWS_FILTER";

  protected static final String VIEWS_FROM_FILE = "VIEWS_FROM_FILE";

  protected static final String VIEWS_ENTER_ID = "VIEWS_ENTER_ID";

  protected static final String VIEWS_LOCAL_PDB = "VIEWS_LOCAL_PDB";

  protected JTable tbl_local_pdb = new JTable();

  protected JScrollPane scrl_localPDB = new JScrollPane(tbl_local_pdb);

  protected JTabbedPane pnl_filter = new JTabbedPane();

  protected FTSDataColumnPreferences pdbDocFieldPrefs = new FTSDataColumnPreferences(
          PreferenceSource.STRUCTURE_CHOOSER,
          PDBFTSRestClient.getInstance());

  protected FTSDataColumnI[] previousWantedFields;

  protected static Map<String, Integer> tempUserPrefs = new HashMap<String, Integer>();

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
        tempUserPrefs.put(colHeader, resizingColumn.getWidth());
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
        // e.printStackTrace();
      }
      toolTipText = (toolTipText == null ? null
              : (toolTipText.length() > 500 ? JvSwingUtils.wrapTooltip(
                      true, "\"" + toolTipText.subSequence(0, 500)
                              + "...\"") : JvSwingUtils.wrapTooltip(true,
                      toolTipText)));
      return toolTipText;
    }
  };

  protected JScrollPane scrl_foundStructures = new JScrollPane(tbl_summary);

  public GStructureChooser()
  {
    try
    {
      jbInit();
      mainFrame.setVisible(false);
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
    Integer width = tempUserPrefs.get("structureChooser.width") == null ? 800
            : tempUserPrefs.get("structureChooser.width");
    Integer height = tempUserPrefs.get("structureChooser.height") == null ? 400
            : tempUserPrefs.get("structureChooser.height");
    tbl_summary.setAutoCreateRowSorter(true);
    tbl_summary.getTableHeader().setReorderingAllowed(false);
    tbl_summary.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        validateSelections();
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        validateSelections();
      }
    });
    tbl_summary.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        validateSelections();
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_ESCAPE: // escape key
          mainFrame.dispose();
          break;
        case KeyEvent.VK_ENTER: // enter key
          if (btn_view.isEnabled())
          {
            ok_ActionPerformed();
          }
          break;
        case KeyEvent.VK_TAB: // tab key
          if (evt.isShiftDown())
          {
            pnl_filter.requestFocus();
          }
          else
          {
            btn_view.requestFocus();
          }
          evt.consume();
          break;
        default:
          return;
        }
      }
    });
    tbl_local_pdb.setAutoCreateRowSorter(true);
    tbl_local_pdb.getTableHeader().setReorderingAllowed(false);
    tbl_local_pdb.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mouseClicked(MouseEvent e)
      {
        validateSelections();
      }

      @Override
      public void mouseReleased(MouseEvent e)
      {
        validateSelections();
      }
    });
    tbl_local_pdb.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        validateSelections();
        switch (evt.getKeyCode())
        {
        case KeyEvent.VK_ESCAPE: // escape key
          mainFrame.dispose();
          break;
        case KeyEvent.VK_ENTER: // enter key
          if (btn_view.isEnabled())
          {
            ok_ActionPerformed();
          }
          break;
        case KeyEvent.VK_TAB: // tab key
          if (evt.isShiftDown())
          {
            cmb_filterOption.requestFocus();
          }
          else
          {
            if (btn_view.isEnabled())
            {
              btn_view.requestFocus();
            }
            else
            {
              btn_cancel.requestFocus();
            }
          }
          evt.consume();
          break;
        default:
          return;
        }
      }
    });
    btn_view.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_view.setText(MessageManager.getString("action.view"));
    btn_view.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        ok_ActionPerformed();
      }
    });
    btn_view.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          ok_ActionPerformed();
        }
      }
    });

    btn_cancel.setFont(new java.awt.Font("Verdana", 0, 12));
    btn_cancel.setText(MessageManager.getString("action.cancel"));
    btn_cancel.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        closeAction(pnl_filter.getHeight());
      }
    });
    btn_cancel.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          closeAction(pnl_filter.getHeight());
        }
      }
    });

    btn_pdbFromFile.setFont(new java.awt.Font("Verdana", 0, 12));
    String btn_title = MessageManager.getString("label.select_pdb_file");
    btn_pdbFromFile.setText(btn_title + "              ");
    btn_pdbFromFile.addActionListener(new java.awt.event.ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        pdbFromFile_actionPerformed();
      }
    });
    btn_pdbFromFile.addKeyListener(new KeyAdapter()
    {
      @Override
      public void keyPressed(KeyEvent evt)
      {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER)
        {
          pdbFromFile_actionPerformed();
        }
      }
    });

    scrl_foundStructures.setPreferredSize(new Dimension(width, height));

    scrl_localPDB.setPreferredSize(new Dimension(width, height));
    scrl_localPDB
            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    cmb_filterOption.setFont(new java.awt.Font("Verdana", 0, 12));
    chk_invertFilter.setFont(new java.awt.Font("Verdana", 0, 12));
    chk_rememberSettings.setFont(new java.awt.Font("Verdana", 0, 12));
    chk_rememberSettings.setVisible(false);
    txt_search.setToolTipText(JvSwingUtils.wrapTooltip(true,
            MessageManager.getString("label.enter_pdb_id")));
    cmb_filterOption.setToolTipText(MessageManager
            .getString("info.select_filter_option"));
    txt_search.getDocument().addDocumentListener(new DocumentListener()
    {
      @Override
      public void insertUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }

      @Override
      public void removeUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }

      @Override
      public void changedUpdate(DocumentEvent e)
      {
        txt_search_ActionPerformed();
      }
    });

    cmb_filterOption.addItemListener(this);
    chk_invertFilter.addItemListener(this);

    pnl_actions.add(chk_rememberSettings);
    pnl_actions.add(btn_view);
    pnl_actions.add(btn_cancel);

    // pnl_filter.add(lbl_result);
    pnl_main.add(cmb_filterOption);
    pnl_main.add(lbl_loading);
    pnl_main.add(chk_invertFilter);
    lbl_loading.setVisible(false);

    pnl_fileChooser.add(btn_pdbFromFile);
    pnl_fileChooser.add(lbl_fromFileStatus);
    pnl_fileChooserBL.add(fileChooserAssSeqPanel, BorderLayout.NORTH);
    pnl_fileChooserBL.add(pnl_fileChooser, BorderLayout.CENTER);

    pnl_idInput.add(txt_search);
    pnl_idInput.add(lbl_pdbManualFetchStatus);
    pnl_idInputBL.add(idInputAssSeqPanel, BorderLayout.NORTH);
    pnl_idInputBL.add(pnl_idInput, BorderLayout.CENTER);

    final String foundStructureSummary = MessageManager
            .getString("label.found_structures_summary");
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
        btn_view.setVisible(true);
        btn_cancel.setVisible(true);
        if (sourceTabbedPane.getTitleAt(index).equals(configureCols))
        {
          btn_view.setEnabled(false);
          btn_cancel.setEnabled(false);
          btn_view.setVisible(false);
          btn_cancel.setVisible(false);
          previousWantedFields = pdbDocFieldPrefs
                  .getStructureSummaryFields().toArray(
                          new FTSDataColumnI[0]);
        }
        if (sourceTabbedPane.getTitleAt(index)
                .equals(foundStructureSummary))
        {
          btn_cancel.setEnabled(true);
          if (wantedFieldsUpdated())
          {
            tabRefresh();
          }
          else
          {
            validateSelections();
          }
        }
      }
    };
    pnl_filter.addChangeListener(changeListener);
    pnl_filter.setPreferredSize(new Dimension(width, height));
    pnl_filter.add(foundStructureSummary, scrl_foundStructures);
    pnl_filter.add(configureCols, pdbDocFieldPrefs);

    pnl_locPDB.add(scrl_localPDB);

    pnl_switchableViews.add(pnl_fileChooserBL, VIEWS_FROM_FILE);
    pnl_switchableViews.add(pnl_idInputBL, VIEWS_ENTER_ID);
    pnl_switchableViews.add(pnl_filter, VIEWS_FILTER);
    pnl_switchableViews.add(pnl_locPDB, VIEWS_LOCAL_PDB);

    this.setLayout(mainLayout);
    this.add(pnl_main, java.awt.BorderLayout.NORTH);
    this.add(pnl_switchableViews, java.awt.BorderLayout.CENTER);
    // this.add(pnl_actions, java.awt.BorderLayout.SOUTH);
    statusPanel.setLayout(new GridLayout());
    pnl_actionsAndStatus.add(pnl_actions, BorderLayout.CENTER);
    pnl_actionsAndStatus.add(statusPanel, BorderLayout.SOUTH);
    statusPanel.add(statusBar, null);
    this.add(pnl_actionsAndStatus, java.awt.BorderLayout.SOUTH);

    mainFrame
            .addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
            {
              @Override
              public void internalFrameClosing(InternalFrameEvent e)
              {
                closeAction(pnl_filter.getHeight());
              }
            });
    mainFrame.setVisible(true);
    mainFrame.setContentPane(this);
    mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Integer x = tempUserPrefs.get("structureChooser.x");
    Integer y = tempUserPrefs.get("structureChooser.y");
    if (x != null && y != null)
    {
      mainFrame.setLocation(x, y);
    }
    Desktop.addInternalFrame(mainFrame, frameTitle, width, height);
  }

  protected void closeAction(int preferredHeight)
  {
    // System.out.println(">>>>>>>>>> closing internal frame!!!");
    // System.out.println("width : " + mainFrame.getWidth());
    // System.out.println("heigh : " + mainFrame.getHeight());
    // System.out.println("x : " + mainFrame.getX());
    // System.out.println("y : " + mainFrame.getY());
    tempUserPrefs.put("structureChooser.width", pnl_filter.getWidth());
    tempUserPrefs.put("structureChooser.height", preferredHeight);
    tempUserPrefs.put("structureChooser.x", mainFrame.getX());
    tempUserPrefs.put("structureChooser.y", mainFrame.getY());
    mainFrame.dispose();
  }

  public boolean wantedFieldsUpdated()
  {
    if (previousWantedFields == null)
    {
      return true;
    }

    FTSDataColumnI[] currentWantedFields = pdbDocFieldPrefs
            .getStructureSummaryFields().toArray(new FTSDataColumnI[0]);
    return Arrays.equals(currentWantedFields, previousWantedFields) ? false
            : true;

  }

  @Override
  /**
   * Event listener for the 'filter' combo-box and 'invert' check-box
   */
  public void itemStateChanged(ItemEvent e)
  {
    stateChanged(e);
  }

  /**
   * This inner class provides the data model for the structure filter combo-box
   * 
   * @author tcnofoegbu
   *
   */
  public class FilterOption
  {
    private String name;

    private String value;

    private String view;

    public FilterOption(String name, String value, String view)
    {
      this.name = name;
      this.value = value;
      this.view = view;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getValue()
    {
      return value;
    }

    public void setValue(String value)
    {
      this.value = value;
    }

    public String getView()
    {
      return view;
    }

    public void setView(String view)
    {
      this.view = view;
    }

    @Override
    public String toString()
    {
      return this.name;
    }
  }

  /**
   * This inner class provides the provides the data model for associate
   * sequence combo-box - cmb_assSeq
   * 
   * @author tcnofoegbu
   *
   */
  public class AssociateSeqOptions
  {
    private SequenceI sequence;

    private String name;

    public AssociateSeqOptions(SequenceI seq)
    {
      this.sequence = seq;
      this.name = (seq.getName().length() >= 23) ? seq.getName().substring(
              0, 23) : seq.getName();
    }

    public AssociateSeqOptions(String name, SequenceI seq)
    {
      this.name = name;
      this.sequence = seq;
    }

    @Override
    public String toString()
    {
      return name;
    }

    public String getName()
    {
      return name;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public SequenceI getSequence()
    {
      return sequence;
    }

    public void setSequence(SequenceI sequence)
    {
      this.sequence = sequence;
    }

  }

  /**
   * This inner class holds the Layout and configuration of the panel which
   * handles association of manually fetched structures to a unique sequence
   * when more than one sequence selection is made
   * 
   * @author tcnofoegbu
   *
   */
  public class AssciateSeqPanel extends JPanel implements ItemListener
  {
    private JComboBox<AssociateSeqOptions> cmb_assSeq = new JComboBox<AssociateSeqOptions>();

    private JLabel lbl_associateSeq = new JLabel();

    public AssciateSeqPanel()
    {
      this.setLayout(new FlowLayout());
      this.add(cmb_assSeq);
      this.add(lbl_associateSeq);
      cmb_assSeq.setToolTipText(MessageManager
              .getString("info.associate_wit_sequence"));
      cmb_assSeq.addItemListener(this);
    }

    public void loadCmbAssSeq()
    {
      populateCmbAssociateSeqOptions(cmb_assSeq, lbl_associateSeq);
    }

    public JComboBox<AssociateSeqOptions> getCmb_assSeq()
    {
      return cmb_assSeq;
    }

    public void setCmb_assSeq(JComboBox<AssociateSeqOptions> cmb_assSeq)
    {
      this.cmb_assSeq = cmb_assSeq;
    }

    @Override
    public void itemStateChanged(ItemEvent e)
    {
      if (e.getStateChange() == ItemEvent.SELECTED)
      {
        cmbAssSeqStateChanged();
      }
    }
  }

  public JTable getResultTable()
  {
    return tbl_summary;
  }

  public JComboBox<FilterOption> getCmbFilterOption()
  {
    return cmb_filterOption;
  }

  protected abstract void stateChanged(ItemEvent e);

  protected abstract void ok_ActionPerformed();

  protected abstract void pdbFromFile_actionPerformed();

  protected abstract void txt_search_ActionPerformed();

  public abstract void populateCmbAssociateSeqOptions(
          JComboBox<AssociateSeqOptions> cmb_assSeq, JLabel lbl_associateSeq);

  public abstract void cmbAssSeqStateChanged();

  public abstract void tabRefresh();

  public abstract void validateSelections();
}
