/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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

import jalview.api.FeatureSettingsControllerI;
import jalview.bin.Cache;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.Help.HelpId;
import jalview.io.JalviewFileChooser;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.GraduatedColor;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import jalview.ws.dbsources.das.api.jalviewSourceI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.help.HelpSetException;
import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class FeatureSettings extends JPanel implements
        FeatureSettingsControllerI
{
  DasSourceBrowser dassourceBrowser;

  jalview.ws.DasSequenceFeatureFetcher dasFeatureFetcher;

  JPanel settingsPane = new JPanel();

  JPanel dasSettingsPane = new JPanel();

  final FeatureRenderer fr;

  public final AlignFrame af;

  Object[][] originalData;

  private float originalTransparency;

  final JInternalFrame frame;

  JScrollPane scrollPane = new JScrollPane();

  JTable table;

  JPanel groupPanel;

  JSlider transparency = new JSlider();

  JPanel transPanel = new JPanel(new GridLayout(1, 2));

  public FeatureSettings(AlignFrame af)
  {
    this.af = af;
    fr = af.getFeatureRenderer();
    // allow transparency to be recovered
    transparency.setMaximum(100 - (int) ((originalTransparency = fr
            .getTransparency()) * 100));

    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    table = new JTable()
    {
      @Override
      public String getToolTipText(MouseEvent e)
      {
        if (table.columnAtPoint(e.getPoint()) == 0)
        {
          /*
           * Tooltip for feature name only
           */
          return JvSwingUtils.wrapTooltip(true, MessageManager
                  .getString("label.feature_settings_click_drag"));
        }
        return null;
      }
    };
    table.getTableHeader().setFont(new Font("Verdana", Font.PLAIN, 12));
    table.setFont(new Font("Verdana", Font.PLAIN, 12));
    table.setDefaultRenderer(Color.class, new ColorRenderer());

    table.setDefaultEditor(Color.class, new ColorEditor(this));

    table.setDefaultEditor(GraduatedColor.class, new ColorEditor(this));
    table.setDefaultRenderer(GraduatedColor.class, new ColorRenderer());
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    table.addMouseListener(new MouseAdapter()
    {
      public void mousePressed(MouseEvent evt)
      {
        selectedRow = table.rowAtPoint(evt.getPoint());
        if (SwingUtilities.isRightMouseButton(evt))
        {
          popupSort(selectedRow, (String) table.getValueAt(selectedRow, 0),
                  table.getValueAt(selectedRow, 1), fr.getMinMax(),
                  evt.getX(), evt.getY());
        }
        else if (evt.getClickCount() == 2)
        {
          fr.ap.alignFrame.avc.markColumnsContainingFeatures(
                  evt.isAltDown(), evt.isShiftDown() || evt.isMetaDown(),
                  evt.isMetaDown(),
                  (String) table.getValueAt(selectedRow, 0));
        }
      }

      // isPopupTrigger fires on mouseReleased on Mac
      @Override
      public void mouseReleased(MouseEvent evt)
      {
        selectedRow = table.rowAtPoint(evt.getPoint());
        if (evt.isPopupTrigger())
        {
          popupSort(selectedRow, (String) table.getValueAt(selectedRow, 0),
                  table.getValueAt(selectedRow, 1), fr.getMinMax(),
                  evt.getX(), evt.getY());
        }
      }
    });

    table.addMouseMotionListener(new MouseMotionAdapter()
    {
      public void mouseDragged(MouseEvent evt)
      {
        int newRow = table.rowAtPoint(evt.getPoint());
        if (newRow != selectedRow && selectedRow != -1 && newRow != -1)
        {
          Object[] temp = new Object[3];
          temp[0] = table.getValueAt(selectedRow, 0);
          temp[1] = table.getValueAt(selectedRow, 1);
          temp[2] = table.getValueAt(selectedRow, 2);

          table.setValueAt(table.getValueAt(newRow, 0), selectedRow, 0);
          table.setValueAt(table.getValueAt(newRow, 1), selectedRow, 1);
          table.setValueAt(table.getValueAt(newRow, 2), selectedRow, 2);

          table.setValueAt(temp[0], newRow, 0);
          table.setValueAt(temp[1], newRow, 1);
          table.setValueAt(temp[2], newRow, 2);

          selectedRow = newRow;
        }
      }
    });
    // table.setToolTipText(JvSwingUtils.wrapTooltip(true,
    // MessageManager.getString("label.feature_settings_click_drag")));
    scrollPane.setViewportView(table);

    dassourceBrowser = new DasSourceBrowser(this);
    dasSettingsPane.add(dassourceBrowser, BorderLayout.CENTER);

    if (af.getViewport().isShowSequenceFeatures() || !fr.hasRenderOrder())
    {
      fr.findAllFeatures(true); // display everything!
    }

    discoverAllFeatureData();
    final PropertyChangeListener change;
    final FeatureSettings fs = this;
    fr.addPropertyChangeListener(change = new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent evt)
      {
        if (!fs.resettingTable && !fs.handlingUpdate)
        {
          fs.handlingUpdate = true;
          fs.resetTable(null); // new groups may be added with new seuqence
          // feature types only
          fs.handlingUpdate = false;
        }
      }

    });

    frame = new JInternalFrame();
    frame.setContentPane(this);
    if (new jalview.util.Platform().isAMac())
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("label.sequence_feature_settings"),
              475, 480);
    }
    else
    {
      Desktop.addInternalFrame(frame,
              MessageManager.getString("label.sequence_feature_settings"),
              400, 450);
    }

    frame.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter()
    {
      public void internalFrameClosed(
              javax.swing.event.InternalFrameEvent evt)
      {
        fr.removePropertyChangeListener(change);
        dassourceBrowser.fs = null;
      };
    });
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
  }

  protected void popupSort(final int selectedRow, final String type,
          final Object typeCol, final Hashtable minmax, int x, int y)
  {
    JPopupMenu men = new JPopupMenu(MessageManager.formatMessage(
            "label.settings_for_param", new String[] { type }));
    JMenuItem scr = new JMenuItem(
            MessageManager.getString("label.sort_by_score"));
    men.add(scr);
    final FeatureSettings me = this;
    scr.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        me.af.avc.sortAlignmentByFeatureScore(new String[] { type });
      }

    });
    JMenuItem dens = new JMenuItem(
            MessageManager.getString("label.sort_by_density"));
    dens.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        me.af.avc.sortAlignmentByFeatureDensity(new String[] { type });
      }

    });
    men.add(dens);
    if (minmax != null)
    {
      final Object typeMinMax = minmax.get(type);
      /*
       * final JCheckBoxMenuItem chb = new JCheckBoxMenuItem("Vary Height"); //
       * this is broken at the moment and isn't that useful anyway!
       * chb.setSelected(minmax.get(type) != null); chb.addActionListener(new
       * ActionListener() {
       * 
       * public void actionPerformed(ActionEvent e) {
       * chb.setState(chb.getState()); if (chb.getState()) { minmax.put(type,
       * null); } else { minmax.put(type, typeMinMax); } }
       * 
       * });
       * 
       * men.add(chb);
       */
      if (typeMinMax != null && ((float[][]) typeMinMax)[0] != null)
      {
        // if (table.getValueAt(row, column));
        // graduated colourschemes for those where minmax exists for the
        // positional features
        final JCheckBoxMenuItem mxcol = new JCheckBoxMenuItem(
                "Graduated Colour");
        mxcol.setSelected(!(typeCol instanceof Color));
        men.add(mxcol);
        mxcol.addActionListener(new ActionListener()
        {
          JColorChooser colorChooser;

          public void actionPerformed(ActionEvent e)
          {
            if (e.getSource() == mxcol)
            {
              if (typeCol instanceof Color)
              {
                FeatureColourChooser fc = new FeatureColourChooser(me.fr,
                        type);
                fc.addActionListener(this);
              }
              else
              {
                // bring up simple color chooser
                colorChooser = new JColorChooser();
                JDialog dialog = JColorChooser.createDialog(me,
                        "Select new Colour", true, // modal
                        colorChooser, this, // OK button handler
                        null); // no CANCEL button handler
                colorChooser.setColor(((GraduatedColor) typeCol)
                        .getMaxColor());
                dialog.setVisible(true);
              }
            }
            else
            {
              if (e.getSource() instanceof FeatureColourChooser)
              {
                FeatureColourChooser fc = (FeatureColourChooser) e
                        .getSource();
                table.setValueAt(fc.getLastColour(), selectedRow, 1);
                table.validate();
              }
              else
              {
                // probably the color chooser!
                table.setValueAt(colorChooser.getColor(), selectedRow, 1);
                table.validate();
                me.updateFeatureRenderer(
                        ((FeatureTableModel) table.getModel()).getData(),
                        false);
              }
            }
          }

        });
      }
    }
    JMenuItem selCols = new JMenuItem(
            MessageManager.getString("label.select_columns_containing"));
    selCols.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.avc.markColumnsContainingFeatures(false, false,
                false, type);
      }
    });
    JMenuItem clearCols = new JMenuItem(
            MessageManager.getString("label.select_columns_not_containing"));
    clearCols.addActionListener(new ActionListener()
    {

      @Override
      public void actionPerformed(ActionEvent arg0)
      {
        fr.ap.alignFrame.avc.markColumnsContainingFeatures(true, false,
                false, type);
      }
    });
    men.add(selCols);
    men.add(clearCols);
    men.show(table, x, y);
  }

  /**
   * true when Feature Settings are updating from feature renderer
   */
  private boolean handlingUpdate = false;

  /**
   * contains a float[3] for each feature type string. created by setTableData
   */
  Hashtable typeWidth = null;

  @Override
  synchronized public void discoverAllFeatureData()
  {
    Vector allFeatures = new Vector();
    Vector allGroups = new Vector();
    SequenceFeature[] tmpfeatures;
    String group;
    for (int i = 0; i < af.getViewport().getAlignment().getHeight(); i++)
    {
      tmpfeatures = af.getViewport().getAlignment().getSequenceAt(i)
              .getSequenceFeatures();
      if (tmpfeatures == null)
      {
        continue;
      }

      int index = 0;
      while (index < tmpfeatures.length)
      {
        if (tmpfeatures[index].begin == 0 && tmpfeatures[index].end == 0)
        {
          index++;
          continue;
        }

        if (tmpfeatures[index].getFeatureGroup() != null)
        {
          group = tmpfeatures[index].featureGroup;
          if (!allGroups.contains(group))
          {
            allGroups.addElement(group);
            checkGroupState(group);
          }
        }

        if (!allFeatures.contains(tmpfeatures[index].getType()))
        {
          allFeatures.addElement(tmpfeatures[index].getType());
        }
        index++;
      }
    }

    resetTable(null);

    validate();
  }

  /**
   * Synchronise gui group list and check visibility of group
   * 
   * @param group
   * @return true if group is visible
   */
  private boolean checkGroupState(String group)
  {
    boolean visible = fr.checkGroupVisibility(group, true);

    if (groupPanel == null)
    {
      groupPanel = new JPanel();
    }

    boolean alreadyAdded = false;
    for (int g = 0; g < groupPanel.getComponentCount(); g++)
    {
      if (((JCheckBox) groupPanel.getComponent(g)).getText().equals(group))
      {
        alreadyAdded = true;
        ((JCheckBox) groupPanel.getComponent(g)).setSelected(visible);
        break;
      }
    }

    if (alreadyAdded)
    {

      return visible;
    }
    final String grp = group;
    final JCheckBox check = new JCheckBox(group, visible);
    check.setFont(new Font("Serif", Font.BOLD, 12));
    check.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent evt)
      {
        fr.setGroupVisibility(check.getText(), check.isSelected());
        af.alignPanel.getSeqPanel().seqCanvas.repaint();
        if (af.alignPanel.overviewPanel != null)
        {
          af.alignPanel.overviewPanel.updateOverviewImage();
        }

        resetTable(new String[] { grp });
      }
    });
    groupPanel.add(check);
    return visible;
  }

  boolean resettingTable = false;

  synchronized void resetTable(String[] groupChanged)
  {
    if (resettingTable == true)
    {
      return;
    }
    resettingTable = true;
    typeWidth = new Hashtable();
    // TODO: change avWidth calculation to 'per-sequence' average and use long
    // rather than float
    float[] avWidth = null;
    SequenceFeature[] tmpfeatures;
    String group = null, type;
    Vector visibleChecks = new Vector();

    // Find out which features should be visible depending on which groups
    // are selected / deselected
    // and recompute average width ordering
    for (int i = 0; i < af.getViewport().getAlignment().getHeight(); i++)
    {

      tmpfeatures = af.getViewport().getAlignment().getSequenceAt(i)
              .getSequenceFeatures();
      if (tmpfeatures == null)
      {
        continue;
      }

      int index = 0;
      while (index < tmpfeatures.length)
      {
        group = tmpfeatures[index].featureGroup;

        if (tmpfeatures[index].begin == 0 && tmpfeatures[index].end == 0)
        {
          index++;
          continue;
        }

        if (group == null || checkGroupState(group))
        {
          type = tmpfeatures[index].getType();
          if (!visibleChecks.contains(type))
          {
            visibleChecks.addElement(type);
          }
        }
        if (!typeWidth.containsKey(tmpfeatures[index].getType()))
        {
          typeWidth.put(tmpfeatures[index].getType(),
                  avWidth = new float[3]);
        }
        else
        {
          avWidth = (float[]) typeWidth.get(tmpfeatures[index].getType());
        }
        avWidth[0]++;
        if (tmpfeatures[index].getBegin() > tmpfeatures[index].getEnd())
        {
          avWidth[1] += 1 + tmpfeatures[index].getBegin()
                  - tmpfeatures[index].getEnd();
        }
        else
        {
          avWidth[1] += 1 + tmpfeatures[index].getEnd()
                  - tmpfeatures[index].getBegin();
        }
        index++;
      }
    }

    int fSize = visibleChecks.size();
    Object[][] data = new Object[fSize][3];
    int dataIndex = 0;

    if (fr.hasRenderOrder())
    {
      if (!handlingUpdate)
      {
        fr.findAllFeatures(groupChanged != null); // prod to update
        // colourschemes. but don't
        // affect display
        // First add the checks in the previous render order,
        // in case the window has been closed and reopened
      }
      List<String> frl = fr.getRenderOrder();
      for (int ro = frl.size() - 1; ro > -1; ro--)
      {
        type = frl.get(ro);

        if (!visibleChecks.contains(type))
        {
          continue;
        }

        data[dataIndex][0] = type;
        data[dataIndex][1] = fr.getFeatureStyle(type);
        data[dataIndex][2] = new Boolean(af.getViewport()
                .getFeaturesDisplayed().isVisible(type));
        dataIndex++;
        visibleChecks.removeElement(type);
      }
    }

    fSize = visibleChecks.size();
    for (int i = 0; i < fSize; i++)
    {
      // These must be extra features belonging to the group
      // which was just selected
      type = visibleChecks.elementAt(i).toString();
      data[dataIndex][0] = type;

      data[dataIndex][1] = fr.getFeatureStyle(type);
      if (data[dataIndex][1] == null)
      {
        // "Colour has been updated in another view!!"
        fr.clearRenderOrder();
        return;
      }

      data[dataIndex][2] = new Boolean(true);
      dataIndex++;
    }

    if (originalData == null)
    {
      originalData = new Object[data.length][3];
      for (int i = 0; i < data.length; i++)
      {
        System.arraycopy(data[i], 0, originalData[i], 0, 3);
      }
    }

    table.setModel(new FeatureTableModel(data));
    table.getColumnModel().getColumn(0).setPreferredWidth(200);

    if (groupPanel != null)
    {
      groupPanel.setLayout(new GridLayout(
              fr.getFeatureGroupsSize() / 4 + 1, 4));

      groupPanel.validate();
      bigPanel.add(groupPanel, BorderLayout.NORTH);
    }

    updateFeatureRenderer(data, groupChanged != null);
    resettingTable = false;
  }

  /**
   * reorder data based on the featureRenderers global priority list.
   * 
   * @param data
   */
  private void ensureOrder(Object[][] data)
  {
    boolean sort = false;
    float[] order = new float[data.length];
    for (int i = 0; i < order.length; i++)
    {
      order[i] = fr.getOrder(data[i][0].toString());
      if (order[i] < 0)
      {
        order[i] = fr.setOrder(data[i][0].toString(), i / order.length);
      }
      if (i > 1)
      {
        sort = sort || order[i - 1] > order[i];
      }
    }
    if (sort)
    {
      jalview.util.QuickSort.sort(order, data);
    }
  }

  void load()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "fc" },
            new String[] { "Sequence Feature Colours" },
            "Sequence Feature Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.load_feature_colours"));
    chooser.setToolTipText(MessageManager.getString("action.load"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      File file = chooser.getSelectedFile();

      try
      {
        InputStreamReader in = new InputStreamReader(new FileInputStream(
                file), "UTF-8");

        jalview.schemabinding.version2.JalviewUserColours jucs = new jalview.schemabinding.version2.JalviewUserColours();
        jucs = jucs.unmarshal(in);

        for (int i = jucs.getColourCount() - 1; i >= 0; i--)
        {
          String name;
          jalview.schemabinding.version2.Colour newcol = jucs.getColour(i);
          if (newcol.hasMax())
          {
            Color mincol = null, maxcol = null;
            try
            {
              mincol = new Color(Integer.parseInt(newcol.getMinRGB(), 16));
              maxcol = new Color(Integer.parseInt(newcol.getRGB(), 16));

            } catch (Exception e)
            {
              Cache.log.warn("Couldn't parse out graduated feature color.",
                      e);
            }
            GraduatedColor gcol = new GraduatedColor(mincol, maxcol,
                    newcol.getMin(), newcol.getMax());
            if (newcol.hasAutoScale())
            {
              gcol.setAutoScaled(newcol.getAutoScale());
            }
            if (newcol.hasColourByLabel())
            {
              gcol.setColourByLabel(newcol.getColourByLabel());
            }
            if (newcol.hasThreshold())
            {
              gcol.setThresh(newcol.getThreshold());
              gcol.setThreshType(AnnotationColourGradient.NO_THRESHOLD); // default
            }
            if (newcol.getThreshType().length() > 0)
            {
              String ttyp = newcol.getThreshType();
              if (ttyp.equalsIgnoreCase("NONE"))
              {
                gcol.setThreshType(AnnotationColourGradient.NO_THRESHOLD);
              }
              if (ttyp.equalsIgnoreCase("ABOVE"))
              {
                gcol.setThreshType(AnnotationColourGradient.ABOVE_THRESHOLD);
              }
              if (ttyp.equalsIgnoreCase("BELOW"))
              {
                gcol.setThreshType(AnnotationColourGradient.BELOW_THRESHOLD);
              }
            }
            fr.setColour(name = newcol.getName(), gcol);
          }
          else
          {
            fr.setColour(name = jucs.getColour(i).getName(), new Color(
                    Integer.parseInt(jucs.getColour(i).getRGB(), 16)));
          }
          fr.setOrder(name, (i == 0) ? 0 : i / jucs.getColourCount());
        }
        if (table != null)
        {
          resetTable(null);
          Object[][] data = ((FeatureTableModel) table.getModel())
                  .getData();
          ensureOrder(data);
          updateFeatureRenderer(data, false);
          table.repaint();
        }
      } catch (Exception ex)
      {
        System.out.println("Error loading User Colour File\n" + ex);
      }
    }
  }

  void save()
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"),
            new String[] { "fc" },
            new String[] { "Sequence Feature Colours" },
            "Sequence Feature Colours");
    chooser.setFileView(new jalview.io.JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.save_feature_colours"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      jalview.schemabinding.version2.JalviewUserColours ucs = new jalview.schemabinding.version2.JalviewUserColours();
      ucs.setSchemeName("Sequence Features");
      try
      {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(choice), "UTF-8"));

        Set fr_colours = fr.getAllFeatureColours();
        Iterator e = fr_colours.iterator();
        float[] sortOrder = new float[fr_colours.size()];
        String[] sortTypes = new String[fr_colours.size()];
        int i = 0;
        while (e.hasNext())
        {
          sortTypes[i] = e.next().toString();
          sortOrder[i] = fr.getOrder(sortTypes[i]);
          i++;
        }
        jalview.util.QuickSort.sort(sortOrder, sortTypes);
        sortOrder = null;
        Object fcol;
        GraduatedColor gcol;
        for (i = 0; i < sortTypes.length; i++)
        {
          jalview.schemabinding.version2.Colour col = new jalview.schemabinding.version2.Colour();
          col.setName(sortTypes[i]);
          col.setRGB(jalview.util.Format.getHexString(fr.getColour(col
                  .getName())));
          fcol = fr.getFeatureStyle(sortTypes[i]);
          if (fcol instanceof GraduatedColor)
          {
            gcol = (GraduatedColor) fcol;
            col.setMin(gcol.getMin());
            col.setMax(gcol.getMax());
            col.setMinRGB(jalview.util.Format.getHexString(gcol
                    .getMinColor()));
            col.setAutoScale(gcol.isAutoScale());
            col.setThreshold(gcol.getThresh());
            col.setColourByLabel(gcol.isColourByLabel());
            switch (gcol.getThreshType())
            {
            case AnnotationColourGradient.NO_THRESHOLD:
              col.setThreshType("NONE");
              break;
            case AnnotationColourGradient.ABOVE_THRESHOLD:
              col.setThreshType("ABOVE");
              break;
            case AnnotationColourGradient.BELOW_THRESHOLD:
              col.setThreshType("BELOW");
              break;
            }
          }
          ucs.addColour(col);
        }
        ucs.marshal(out);
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public void invertSelection()
  {
    for (int i = 0; i < table.getRowCount(); i++)
    {
      Boolean value = (Boolean) table.getValueAt(i, 2);

      table.setValueAt(new Boolean(!value.booleanValue()), i, 2);
    }
  }

  public void orderByAvWidth()
  {
    if (table == null || table.getModel() == null)
    {
      return;
    }
    Object[][] data = ((FeatureTableModel) table.getModel()).getData();
    float[] width = new float[data.length];
    float[] awidth;
    float max = 0;
    int num = 0;
    for (int i = 0; i < data.length; i++)
    {
      awidth = (float[]) typeWidth.get(data[i][0]);
      if (awidth[0] > 0)
      {
        width[i] = awidth[1] / awidth[0];// *awidth[0]*awidth[2]; - better
        // weight - but have to make per
        // sequence, too (awidth[2])
        // if (width[i]==1) // hack to distinguish single width sequences.
        num++;
      }
      else
      {
        width[i] = 0;
      }
      if (max < width[i])
      {
        max = width[i];
      }
    }
    boolean sort = false;
    for (int i = 0; i < width.length; i++)
    {
      // awidth = (float[]) typeWidth.get(data[i][0]);
      if (width[i] == 0)
      {
        width[i] = fr.getOrder(data[i][0].toString());
        if (width[i] < 0)
        {
          width[i] = fr.setOrder(data[i][0].toString(), i / data.length);
        }
      }
      else
      {
        width[i] /= max; // normalize
        fr.setOrder(data[i][0].toString(), width[i]); // store for later
      }
      if (i > 0)
      {
        sort = sort || width[i - 1] > width[i];
      }
    }
    if (sort)
    {
      jalview.util.QuickSort.sort(width, data);
      // update global priority order
    }

    updateFeatureRenderer(data, false);
    table.repaint();
  }

  public void close()
  {
    try
    {
      frame.setClosed(true);
    } catch (Exception exe)
    {
    }

  }

  public void updateFeatureRenderer(Object[][] data)
  {
    updateFeatureRenderer(data, true);
  }

  private void updateFeatureRenderer(Object[][] data, boolean visibleNew)
  {
    fr.setFeaturePriority(data, visibleNew);
    af.alignPanel.paintAlignment(true);
  }

  int selectedRow = -1;

  JTabbedPane tabbedPane = new JTabbedPane();

  BorderLayout borderLayout1 = new BorderLayout();

  BorderLayout borderLayout2 = new BorderLayout();

  BorderLayout borderLayout3 = new BorderLayout();

  JPanel bigPanel = new JPanel();

  BorderLayout borderLayout4 = new BorderLayout();

  JButton invert = new JButton();

  JPanel buttonPanel = new JPanel();

  JButton cancel = new JButton();

  JButton ok = new JButton();

  JButton loadColours = new JButton();

  JButton saveColours = new JButton();

  JPanel dasButtonPanel = new JPanel();

  JButton fetchDAS = new JButton();

  JButton saveDAS = new JButton();

  JButton cancelDAS = new JButton();

  JButton optimizeOrder = new JButton();

  JButton sortByScore = new JButton();

  JButton sortByDens = new JButton();

  JButton help = new JButton();

  JPanel transbuttons = new JPanel(new GridLayout(5, 1));

  private void jbInit() throws Exception
  {
    this.setLayout(borderLayout1);
    settingsPane.setLayout(borderLayout2);
    dasSettingsPane.setLayout(borderLayout3);
    bigPanel.setLayout(borderLayout4);
    invert.setFont(JvSwingUtils.getLabelFont());
    invert.setText(MessageManager.getString("label.invert_selection"));
    invert.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        invertSelection();
      }
    });
    optimizeOrder.setFont(JvSwingUtils.getLabelFont());
    optimizeOrder.setText(MessageManager.getString("label.optimise_order"));
    optimizeOrder.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        orderByAvWidth();
      }
    });
    sortByScore.setFont(JvSwingUtils.getLabelFont());
    sortByScore
            .setText(MessageManager.getString("label.seq_sort_by_score"));
    sortByScore.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        af.avc.sortAlignmentByFeatureScore(null);
      }
    });
    sortByDens.setFont(JvSwingUtils.getLabelFont());
    sortByDens.setText(MessageManager
            .getString("label.sequence_sort_by_density"));
    sortByDens.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        af.avc.sortAlignmentByFeatureDensity(null);
      }
    });
    help.setFont(JvSwingUtils.getLabelFont());
    help.setText(MessageManager.getString("action.help"));
    help.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          Help.showHelpWindow(HelpId.SequenceFeatureSettings);
        } catch (HelpSetException e1)
        {
          e1.printStackTrace();
        }
      }
    });
    help.setFont(JvSwingUtils.getLabelFont());
    help.setText(MessageManager.getString("action.help"));
    help.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          Help.showHelpWindow(HelpId.SequenceFeatureSettings);
        } catch (HelpSetException e1)
        {
          e1.printStackTrace();
        }
      }
    });
    cancel.setFont(JvSwingUtils.getLabelFont());
    cancel.setText(MessageManager.getString("action.cancel"));
    cancel.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fr.setTransparency(originalTransparency);
        updateFeatureRenderer(originalData);
        close();
      }
    });
    ok.setFont(JvSwingUtils.getLabelFont());
    ok.setText(MessageManager.getString("action.ok"));
    ok.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        close();
      }
    });
    loadColours.setFont(JvSwingUtils.getLabelFont());
    loadColours.setText(MessageManager.getString("label.load_colours"));
    loadColours.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        load();
      }
    });
    saveColours.setFont(JvSwingUtils.getLabelFont());
    saveColours.setText(MessageManager.getString("label.save_colours"));
    saveColours.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        save();
      }
    });
    transparency.addChangeListener(new ChangeListener()
    {
      public void stateChanged(ChangeEvent evt)
      {
        fr.setTransparency((100 - transparency.getValue()) / 100f);
        af.alignPanel.paintAlignment(true);
      }
    });

    transparency.setMaximum(70);
    transparency.setToolTipText(MessageManager
            .getString("label.transparency_tip"));
    fetchDAS.setText(MessageManager.getString("label.fetch_das_features"));
    fetchDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        fetchDAS_actionPerformed(e);
      }
    });
    saveDAS.setText(MessageManager.getString("action.save_as_default"));
    saveDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveDAS_actionPerformed(e);
      }
    });
    dasButtonPanel.setBorder(BorderFactory.createEtchedBorder());
    dasSettingsPane.setBorder(null);
    cancelDAS.setEnabled(false);
    cancelDAS.setText(MessageManager.getString("action.cancel_fetch"));
    cancelDAS.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        cancelDAS_actionPerformed(e);
      }
    });
    this.add(tabbedPane, java.awt.BorderLayout.CENTER);
    tabbedPane.addTab(MessageManager.getString("label.feature_settings"),
            settingsPane);
    tabbedPane.addTab(MessageManager.getString("label.das_settings"),
            dasSettingsPane);
    bigPanel.add(transPanel, java.awt.BorderLayout.SOUTH);
    transbuttons.add(optimizeOrder);
    transbuttons.add(invert);
    transbuttons.add(sortByScore);
    transbuttons.add(sortByDens);
    transbuttons.add(help);
    JPanel sliderPanel = new JPanel();
    sliderPanel.add(transparency);
    transPanel.add(transparency);
    transPanel.add(transbuttons);
    buttonPanel.add(ok);
    buttonPanel.add(cancel);
    buttonPanel.add(loadColours);
    buttonPanel.add(saveColours);
    bigPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
    dasSettingsPane.add(dasButtonPanel, java.awt.BorderLayout.SOUTH);
    dasButtonPanel.add(fetchDAS);
    dasButtonPanel.add(cancelDAS);
    dasButtonPanel.add(saveDAS);
    settingsPane.add(bigPanel, java.awt.BorderLayout.CENTER);
    settingsPane.add(buttonPanel, java.awt.BorderLayout.SOUTH);
  }

  public void fetchDAS_actionPerformed(ActionEvent e)
  {
    fetchDAS.setEnabled(false);
    cancelDAS.setEnabled(true);
    dassourceBrowser.setGuiEnabled(false);
    Vector selectedSources = dassourceBrowser.getSelectedSources();
    doDasFeatureFetch(selectedSources, true, true);
  }

  /**
   * get the features from selectedSources for all or the current selection
   * 
   * @param selectedSources
   * @param checkDbRefs
   * @param promptFetchDbRefs
   */
  private void doDasFeatureFetch(List<jalviewSourceI> selectedSources,
          boolean checkDbRefs, boolean promptFetchDbRefs)
  {
    SequenceI[] dataset, seqs;
    int iSize;
    AlignmentViewport vp = af.getViewport();
    if (vp.getSelectionGroup() != null
            && vp.getSelectionGroup().getSize() > 0)
    {
      iSize = vp.getSelectionGroup().getSize();
      dataset = new SequenceI[iSize];
      seqs = vp.getSelectionGroup().getSequencesInOrder(vp.getAlignment());
    }
    else
    {
      iSize = vp.getAlignment().getHeight();
      seqs = vp.getAlignment().getSequencesArray();
    }

    dataset = new SequenceI[iSize];
    for (int i = 0; i < iSize; i++)
    {
      dataset[i] = seqs[i].getDatasetSequence();
    }

    cancelDAS.setEnabled(true);
    dasFeatureFetcher = new jalview.ws.DasSequenceFeatureFetcher(dataset,
            this, selectedSources, checkDbRefs, promptFetchDbRefs);
    af.getViewport().setShowSequenceFeatures(true);
    af.showSeqFeatures.setSelected(true);
  }

  /**
   * blocking call to initialise the das source browser
   */
  public void initDasSources()
  {
    dassourceBrowser.initDasSources();
  }

  /**
   * examine the current list of das sources and return any matching the given
   * nicknames in sources
   * 
   * @param sources
   *          Vector of Strings to resolve to DAS source nicknames.
   * @return sources that are present in source list.
   */
  public List<jalviewSourceI> resolveSourceNicknames(Vector sources)
  {
    return dassourceBrowser.sourceRegistry.resolveSourceNicknames(sources);
  }

  /**
   * get currently selected das sources. ensure you have called initDasSources
   * before calling this.
   * 
   * @return vector of selected das source nicknames
   */
  public Vector getSelectedSources()
  {
    return dassourceBrowser.getSelectedSources();
  }

  /**
   * properly initialise DAS fetcher and then initiate a new thread to fetch
   * features from the named sources (rather than any turned on by default)
   * 
   * @param sources
   * @param block
   *          if true then runs in same thread, otherwise passes to the Swing
   *          executor
   */
  public void fetchDasFeatures(Vector sources, boolean block)
  {
    initDasSources();
    List<jalviewSourceI> resolved = dassourceBrowser.sourceRegistry
            .resolveSourceNicknames(sources);
    if (resolved.size() == 0)
    {
      resolved = dassourceBrowser.getSelectedSources();
    }
    if (resolved.size() > 0)
    {
      final List<jalviewSourceI> dassources = resolved;
      fetchDAS.setEnabled(false);
      // cancelDAS.setEnabled(true); doDasFetch does this.
      Runnable fetcher = new Runnable()
      {

        public void run()
        {
          doDasFeatureFetch(dassources, true, false);

        }
      };
      if (block)
      {
        fetcher.run();
      }
      else
      {
        SwingUtilities.invokeLater(fetcher);
      }
    }
  }

  public void saveDAS_actionPerformed(ActionEvent e)
  {
    dassourceBrowser
            .saveProperties(jalview.bin.Cache.applicationProperties);
  }

  public void complete()
  {
    fetchDAS.setEnabled(true);
    cancelDAS.setEnabled(false);
    dassourceBrowser.setGuiEnabled(true);

  }

  public void cancelDAS_actionPerformed(ActionEvent e)
  {
    if (dasFeatureFetcher != null)
    {
      dasFeatureFetcher.cancel();
    }
    complete();
  }

  public void noDasSourceActive()
  {
    complete();
    JOptionPane
            .showInternalConfirmDialog(
                    Desktop.desktop,
                    MessageManager
                            .getString("label.no_das_sources_selected_warn"),
                    MessageManager
                            .getString("label.no_das_sources_selected_title"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE);
  }

  // ///////////////////////////////////////////////////////////////////////
  // http://java.sun.com/docs/books/tutorial/uiswing/components/table.html
  // ///////////////////////////////////////////////////////////////////////
  class FeatureTableModel extends AbstractTableModel
  {
    FeatureTableModel(Object[][] data)
    {
      this.data = data;
    }

    private String[] columnNames = {
        MessageManager.getString("label.feature_type"),
        MessageManager.getString("action.colour"),
        MessageManager.getString("label.display") };

    private Object[][] data;

    public Object[][] getData()
    {
      return data;
    }

    public void setData(Object[][] data)
    {
      this.data = data;
    }

    public int getColumnCount()
    {
      return columnNames.length;
    }

    public Object[] getRow(int row)
    {
      return data[row];
    }

    public int getRowCount()
    {
      return data.length;
    }

    public String getColumnName(int col)
    {
      return columnNames[col];
    }

    public Object getValueAt(int row, int col)
    {
      return data[row][col];
    }

    public Class getColumnClass(int c)
    {
      return getValueAt(0, c).getClass();
    }

    public boolean isCellEditable(int row, int col)
    {
      return col == 0 ? false : true;
    }

    public void setValueAt(Object value, int row, int col)
    {
      data[row][col] = value;
      fireTableCellUpdated(row, col);
      updateFeatureRenderer(data);
    }

  }

  class ColorRenderer extends JLabel implements TableCellRenderer
  {
    javax.swing.border.Border unselectedBorder = null;

    javax.swing.border.Border selectedBorder = null;

    final String baseTT = "Click to edit, right/apple click for menu.";

    public ColorRenderer()
    {
      setOpaque(true); // MUST do this for background to show up.
      setHorizontalTextPosition(SwingConstants.CENTER);
      setVerticalTextPosition(SwingConstants.CENTER);
    }

    public Component getTableCellRendererComponent(JTable table,
            Object color, boolean isSelected, boolean hasFocus, int row,
            int column)
    {
      // JLabel comp = new JLabel();
      // comp.
      setOpaque(true);
      // comp.
      // setBounds(getBounds());
      Color newColor;
      setToolTipText(baseTT);
      setBackground(table.getBackground());
      if (color instanceof GraduatedColor)
      {
        Rectangle cr = table.getCellRect(row, column, false);
        FeatureSettings.renderGraduatedColor(this, (GraduatedColor) color,
                (int) cr.getWidth(), (int) cr.getHeight());

      }
      else
      {
        this.setText("");
        this.setIcon(null);
        newColor = (Color) color;
        // comp.
        setBackground(newColor);
        // comp.setToolTipText("RGB value: " + newColor.getRed() + ", "
        // + newColor.getGreen() + ", " + newColor.getBlue());
      }
      if (isSelected)
      {
        if (selectedBorder == null)
        {
          selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  table.getSelectionBackground());
        }
        // comp.
        setBorder(selectedBorder);
      }
      else
      {
        if (unselectedBorder == null)
        {
          unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5,
                  table.getBackground());
        }
        // comp.
        setBorder(unselectedBorder);
      }

      return this;
    }
  }

  /**
   * update comp using rendering settings from gcol
   * 
   * @param comp
   * @param gcol
   */
  public static void renderGraduatedColor(JLabel comp, GraduatedColor gcol)
  {
    int w = comp.getWidth(), h = comp.getHeight();
    if (w < 20)
    {
      w = (int) comp.getPreferredSize().getWidth();
      h = (int) comp.getPreferredSize().getHeight();
      if (w < 20)
      {
        w = 80;
        h = 12;
      }
    }
    renderGraduatedColor(comp, gcol, w, h);
  }

  public static void renderGraduatedColor(JLabel comp, GraduatedColor gcol,
          int w, int h)
  {
    boolean thr = false;
    String tt = "";
    String tx = "";
    if (gcol.getThreshType() == AnnotationColourGradient.ABOVE_THRESHOLD)
    {
      thr = true;
      tx += ">";
      tt += "Thresholded (Above " + gcol.getThresh() + ") ";
    }
    if (gcol.getThreshType() == AnnotationColourGradient.BELOW_THRESHOLD)
    {
      thr = true;
      tx += "<";
      tt += "Thresholded (Below " + gcol.getThresh() + ") ";
    }
    if (gcol.isColourByLabel())
    {
      tt = "Coloured by label text. " + tt;
      if (thr)
      {
        tx += " ";
      }
      tx += "Label";
      comp.setIcon(null);
    }
    else
    {
      Color newColor = gcol.getMaxColor();
      comp.setBackground(newColor);
      // System.err.println("Width is " + w / 2);
      Icon ficon = new FeatureIcon(gcol, comp.getBackground(), w, h, thr);
      comp.setIcon(ficon);
      // tt+="RGB value: Max (" + newColor.getRed() + ", "
      // + newColor.getGreen() + ", " + newColor.getBlue()
      // + ")\nMin (" + minCol.getRed() + ", " + minCol.getGreen()
      // + ", " + minCol.getBlue() + ")");
    }
    comp.setHorizontalAlignment(SwingConstants.CENTER);
    comp.setText(tx);
    if (tt.length() > 0)
    {
      if (comp.getToolTipText() == null)
      {
        comp.setToolTipText(tt);
      }
      else
      {
        comp.setToolTipText(tt + " " + comp.getToolTipText());
      }
    }
  }
}

class FeatureIcon implements Icon
{
  GraduatedColor gcol;

  Color backg;

  boolean midspace = false;

  int width = 50, height = 20;

  int s1, e1; // start and end of midpoint band for thresholded symbol

  Color mpcolour = Color.white;

  FeatureIcon(GraduatedColor gfc, Color bg, int w, int h, boolean mspace)
  {
    gcol = gfc;
    backg = bg;
    width = w;
    height = h;
    midspace = mspace;
    if (midspace)
    {
      s1 = width / 3;
      e1 = s1 * 2;
    }
    else
    {
      s1 = width / 2;
      e1 = s1;
    }
  }

  public int getIconWidth()
  {
    return width;
  }

  public int getIconHeight()
  {
    return height;
  }

  public void paintIcon(Component c, Graphics g, int x, int y)
  {

    if (gcol.isColourByLabel())
    {
      g.setColor(backg);
      g.fillRect(0, 0, width, height);
      // need an icon here.
      g.setColor(gcol.getMaxColor());

      g.setFont(new Font("Verdana", Font.PLAIN, 9));

      // g.setFont(g.getFont().deriveFont(
      // AffineTransform.getScaleInstance(
      // width/g.getFontMetrics().stringWidth("Label"),
      // height/g.getFontMetrics().getHeight())));

      g.drawString(MessageManager.getString("label.label"), 0, 0);

    }
    else
    {
      Color minCol = gcol.getMinColor();
      g.setColor(minCol);
      g.fillRect(0, 0, s1, height);
      if (midspace)
      {
        g.setColor(Color.white);
        g.fillRect(s1, 0, e1 - s1, height);
      }
      g.setColor(gcol.getMaxColor());
      g.fillRect(0, e1, width - e1, height);
    }
  }
}

class ColorEditor extends AbstractCellEditor implements TableCellEditor,
        ActionListener
{
  FeatureSettings me;

  GraduatedColor currentGColor;

  FeatureColourChooser chooser;

  String type;

  Color currentColor;

  JButton button;

  JColorChooser colorChooser;

  JDialog dialog;

  protected static final String EDIT = "edit";

  int selectedRow = 0;

  public ColorEditor(FeatureSettings me)
  {
    this.me = me;
    // Set up the editor (from the table's point of view),
    // which is a button.
    // This button brings up the color chooser dialog,
    // which is the editor from the user's point of view.
    button = new JButton();
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    button.setBorderPainted(false);
    // Set up the dialog that the button brings up.
    colorChooser = new JColorChooser();
    dialog = JColorChooser.createDialog(button, "Select new Colour", true, // modal
            colorChooser, this, // OK button handler
            null); // no CANCEL button handler
  }

  /**
   * Handles events from the editor button and from the dialog's OK button.
   */
  public void actionPerformed(ActionEvent e)
  {

    if (EDIT.equals(e.getActionCommand()))
    {
      // The user has clicked the cell, so
      // bring up the dialog.
      if (currentColor != null)
      {
        // bring up simple color chooser
        button.setBackground(currentColor);
        colorChooser.setColor(currentColor);
        dialog.setVisible(true);
      }
      else
      {
        // bring up graduated chooser.
        chooser = new FeatureColourChooser(me.fr, type);
        chooser.setRequestFocusEnabled(true);
        chooser.requestFocus();
        chooser.addActionListener(this);
      }
      // Make the renderer reappear.
      fireEditingStopped();

    }
    else
    { // User pressed dialog's "OK" button.
      if (currentColor != null)
      {
        currentColor = colorChooser.getColor();
      }
      else
      {
        // class cast exceptions may be raised if the chooser created on a
        // non-graduated color
        currentGColor = (GraduatedColor) chooser.getLastColour();
      }
      me.table.setValueAt(getCellEditorValue(), selectedRow, 1);
      fireEditingStopped();
      me.table.validate();
    }
  }

  // Implement the one CellEditor method that AbstractCellEditor doesn't.
  public Object getCellEditorValue()
  {
    if (currentColor == null)
    {
      return currentGColor;
    }
    return currentColor;
  }

  // Implement the one method defined by TableCellEditor.
  public Component getTableCellEditorComponent(JTable table, Object value,
          boolean isSelected, int row, int column)
  {
    currentGColor = null;
    currentColor = null;
    this.selectedRow = row;
    type = me.table.getValueAt(row, 0).toString();
    button.setOpaque(true);
    button.setBackground(me.getBackground());
    if (value instanceof GraduatedColor)
    {
      currentGColor = (GraduatedColor) value;
      JLabel btn = new JLabel();
      btn.setSize(button.getSize());
      FeatureSettings.renderGraduatedColor(btn, currentGColor);
      button.setBackground(btn.getBackground());
      button.setIcon(btn.getIcon());
      button.setText(btn.getText());
    }
    else
    {
      button.setText("");
      button.setIcon(null);
      currentColor = (Color) value;
      button.setBackground(currentColor);
    }
    return button;
  }
}
