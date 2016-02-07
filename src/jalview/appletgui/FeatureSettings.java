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
package jalview.appletgui;

import jalview.api.FeatureSettingsControllerI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.GraduatedColor;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class FeatureSettings extends Panel implements ItemListener,
        MouseListener, MouseMotionListener, ActionListener,
        AdjustmentListener, FeatureSettingsControllerI
{
  FeatureRenderer fr;

  AlignmentPanel ap;

  AlignViewport av;

  Frame frame;

  Panel groupPanel;

  Panel featurePanel = new Panel();

  ScrollPane scrollPane;

  Image linkImage;

  Scrollbar transparency;

  public FeatureSettings(final AlignmentPanel ap)
  {
    this.ap = ap;
    this.av = ap.av;
    ap.av.featureSettings = this;
    fr = ap.seqPanel.seqCanvas.getFeatureRenderer();

    transparency = new Scrollbar(Scrollbar.HORIZONTAL,
            100 - (int) (fr.getTransparency() * 100), 1, 1, 100);

    if (fr.isTransparencyAvailable())
    {
      transparency.addAdjustmentListener(this);
    }
    else
    {
      transparency.setEnabled(false);
    }

    java.net.URL url = getClass().getResource("/images/link.gif");
    if (url != null)
    {
      linkImage = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }

    if (av.isShowSequenceFeatures() || !fr.hasRenderOrder())
    {
      fr.findAllFeatures(true); // was default - now true to make all visible
    }

    discoverAllFeatureData();

    this.setLayout(new BorderLayout());
    scrollPane = new ScrollPane();
    scrollPane.add(featurePanel);
    if (fr.getAllFeatureColours() != null
            && fr.getAllFeatureColours().size() > 0)
    {
      add(scrollPane, BorderLayout.CENTER);
    }

    Button invert = new Button("Invert Selection");
    invert.addActionListener(this);

    Panel lowerPanel = new Panel(new GridLayout(2, 1, 5, 10));
    lowerPanel.add(invert);

    Panel tPanel = new Panel(new BorderLayout());

    if (fr.isTransparencyAvailable())
    {
      tPanel.add(transparency, BorderLayout.CENTER);
      tPanel.add(new Label("Transparency"), BorderLayout.EAST);
    }
    else
    {
      tPanel.add(
              new Label("Transparency not available in this web browser"),
              BorderLayout.CENTER);
    }

    lowerPanel.add(tPanel, BorderLayout.SOUTH);

    add(lowerPanel, BorderLayout.SOUTH);

    if (groupPanel != null)
    {
      groupPanel.setLayout(new GridLayout(
              (fr.getFeatureGroupsSize()) / 4 + 1, 4)); // JBPNote - this was
                                                        // scaled on number of
                                                        // visible groups. seems
                                                        // broken
      groupPanel.validate();

      add(groupPanel, BorderLayout.NORTH);
    }
    frame = new Frame();
    frame.add(this);
    final FeatureSettings me = this;
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        if (me.av.featureSettings == me)
        {
          me.av.featureSettings = null;
          me.ap = null;
          me.av = null;
        }
      }
    });
    int height = featurePanel.getComponentCount() * 50 + 60;

    height = Math.max(200, height);
    height = Math.min(400, height);
    int width = 300;
    jalview.bin.JalviewLite.addFrame(frame,
            MessageManager.getString("label.sequence_feature_settings"),
            width, height);
  }

  public void paint(Graphics g)
  {
    g.setColor(Color.black);
    g.drawString(MessageManager
            .getString("label.no_features_added_to_this_alignment"), 10, 20);
    g.drawString(MessageManager
            .getString("label.features_can_be_added_from_searches_1"), 10,
            40);
    g.drawString(MessageManager
            .getString("label.features_can_be_added_from_searches_2"), 10,
            60);
  }

  protected void popupSort(final MyCheckbox check, final Hashtable minmax,
          int x, int y)
  {
    final String type = check.type;
    final Object typeCol = fr.getFeatureStyle(type);
    java.awt.PopupMenu men = new PopupMenu(MessageManager.formatMessage(
            "label.settings_for_type", new String[] { type }));
    java.awt.MenuItem scr = new MenuItem(
            MessageManager.getString("label.sort_by_score"));
    men.add(scr);
    final FeatureSettings me = this;
    scr.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc
                .sortAlignmentByFeatureScore(new String[] { type });
      }

    });
    MenuItem dens = new MenuItem(
            MessageManager.getString("label.sort_by_density"));
    dens.addActionListener(new ActionListener()
    {

      public void actionPerformed(ActionEvent e)
      {
        me.ap.alignFrame.avc
                .sortAlignmentByFeatureDensity(new String[] { type });
      }

    });
    men.add(dens);
    if (minmax != null)
    {
      final Object typeMinMax = minmax.get(type);
      /*
       * final java.awt.CheckboxMenuItem chb = new
       * java.awt.CheckboxMenuItem("Vary Height"); // this is broken at the
       * moment chb.setState(minmax.get(type) != null);
       * chb.addActionListener(new ActionListener() {
       * 
       * public void actionPerformed(ActionEvent e) {
       * chb.setState(chb.getState()); if (chb.getState()) { minmax.put(type,
       * null); } else { minmax.put(type, typeMinMax); } }
       * 
       * }); men.add(chb);
       */
      if (typeMinMax != null && ((float[][]) typeMinMax)[0] != null)
      {
        // graduated colourschemes for those where minmax exists for the
        // positional features
        MenuItem mxcol = new MenuItem(
                (typeCol instanceof Color) ? "Graduated Colour"
                        : "Single Colour");
        men.add(mxcol);
        mxcol.addActionListener(new ActionListener()
        {

          public void actionPerformed(ActionEvent e)
          {
            if (typeCol instanceof Color)
            {
              new FeatureColourChooser(me, type);
              // write back the current colour object to update the table
              check.updateColor(fr.getFeatureStyle(type));
            }
            else
            {
              new UserDefinedColours(me, check.type,
                      ((GraduatedColor) typeCol));
            }
          }

        });
      }
    }
    this.featurePanel.add(men);
    men.show(this.featurePanel, x, y);
  }

  @Override
  public void discoverAllFeatureData()
  {
    if (fr.getAllFeatureColours() != null
            && fr.getAllFeatureColours().size() > 0)
    {
      rebuildGroups();

    }
    resetTable(false);
  }

  /**
   * rebuilds the group panel
   */
  public void rebuildGroups()
  {
    boolean rdrw = false;
    if (groupPanel == null)
    {
      groupPanel = new Panel();
    }
    else
    {
      rdrw = true;
      groupPanel.removeAll();
    }
    // TODO: JAL-964 - smoothly incorporate new group entries if panel already
    // displayed and new groups present
    for (String group : fr.getFeatureGroups())
    {
      boolean vis = fr.checkGroupVisibility(group, false);
      Checkbox check = new MyCheckbox(group, vis,
              (fr.featureLinks != null && fr.featureLinks
                      .containsKey(group)));
      check.addMouseListener(this);
      check.setFont(new Font("Serif", Font.BOLD, 12));
      check.addItemListener(groupItemListener);
      // note - visibility seems to correlate with displayed. ???wtf ??
      check.setVisible(vis);
      groupPanel.add(check);
    }
    if (rdrw)
    {
      groupPanel.validate();
    }
  }

  // This routine adds and removes checkboxes depending on
  // Group selection states
  void resetTable(boolean groupsChanged)
  {
    SequenceFeature[] tmpfeatures;
    String group = null, type;
    Vector visibleChecks = new Vector();
    AlignmentI alignment = av.getAlignment();
    for (int i = 0; i < alignment.getHeight(); i++)
    {
      if (alignment.getSequenceAt(i).getSequenceFeatures() == null)
      {
        continue;
      }

      tmpfeatures = alignment.getSequenceAt(i).getSequenceFeatures();
      int index = 0;
      while (index < tmpfeatures.length)
      {
        group = tmpfeatures[index].featureGroup;

        if (group == null || fr.checkGroupVisibility(group, true))
        {
          type = tmpfeatures[index].getType();
          if (!visibleChecks.contains(type))
          {
            visibleChecks.addElement(type);
          }
        }
        index++;
      }
    }

    Component[] comps;
    int cSize = featurePanel.getComponentCount();
    MyCheckbox check;
    // This will remove any checkboxes which shouldn't be
    // visible
    for (int i = 0; i < cSize; i++)
    {
      comps = featurePanel.getComponents();
      check = (MyCheckbox) comps[i];
      if (!visibleChecks.contains(check.type))
      {
        featurePanel.remove(i);
        cSize--;
        i--;
      }
    }

    if (fr.getRenderOrder() != null)
    {
      // First add the checks in the previous render order,
      // in case the window has been closed and reopened
      List<String> rol = fr.getRenderOrder();
      for (int ro = rol.size() - 1; ro > -1; ro--)
      {
        String item = rol.get(ro);

        if (!visibleChecks.contains(item))
        {
          continue;
        }

        visibleChecks.removeElement(item);

        addCheck(false, item);
      }
    }

    // now add checkboxes which should be visible,
    // if they have not already been added
    Enumeration en = visibleChecks.elements();

    while (en.hasMoreElements())
    {
      addCheck(groupsChanged, en.nextElement().toString());
    }

    featurePanel.setLayout(new GridLayout(featurePanel.getComponentCount(),
            1, 10, 5));
    featurePanel.validate();

    if (scrollPane != null)
    {
      scrollPane.validate();
    }

    itemStateChanged(null);
  }

  /**
   * update the checklist of feature types with the given type
   * 
   * @param groupsChanged
   *          true means if the type is not in the display list then it will be
   *          added and displayed
   * @param type
   *          feature type to be checked for in the list.
   */
  void addCheck(boolean groupsChanged, String type)
  {
    boolean addCheck;
    Component[] comps = featurePanel.getComponents();
    MyCheckbox check;
    addCheck = true;
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      check = (MyCheckbox) comps[i];
      if (check.type.equals(type))
      {
        addCheck = false;
        break;
      }
    }

    if (addCheck)
    {
      boolean selected = false;
      if (groupsChanged || av.getFeaturesDisplayed().isVisible(type))
      {
        selected = true;
      }

      check = new MyCheckbox(
              type,
              selected,
              (fr.featureLinks != null && fr.featureLinks.containsKey(type)),
              fr.getFeatureStyle(type));

      check.addMouseListener(this);
      check.addMouseMotionListener(this);
      check.addItemListener(this);
      if (groupsChanged)
      {
        // add at beginning of stack.
        featurePanel.add(check, 0);
      }
      else
      {
        // add at end of stack.
        featurePanel.add(check);
      }
    }
  }

  public void actionPerformed(ActionEvent evt)
  {
    for (int i = 0; i < featurePanel.getComponentCount(); i++)
    {
      Checkbox check = (Checkbox) featurePanel.getComponent(i);
      check.setState(!check.getState());
    }
    selectionChanged();
  }

  private ItemListener groupItemListener = new ItemListener()
  {
    public void itemStateChanged(ItemEvent evt)
    {
      Checkbox source = (Checkbox) evt.getSource();
      fr.setGroupVisibility(source.getLabel(), source.getState());
      ap.seqPanel.seqCanvas.repaint();
      if (ap.overviewPanel != null)
      {
        ap.overviewPanel.updateOverviewImage();
      }
      resetTable(true);
      return;
    };
  };

  public void itemStateChanged(ItemEvent evt)
  {
    selectionChanged();
  }

  void selectionChanged()
  {
    Component[] comps = featurePanel.getComponents();
    int cSize = comps.length;

    Object[][] tmp = new Object[cSize][3];
    int tmpSize = 0;
    for (int i = 0; i < cSize; i++)
    {
      MyCheckbox check = (MyCheckbox) comps[i];
      tmp[tmpSize][0] = check.type;
      tmp[tmpSize][1] = fr.getFeatureStyle(check.type);
      tmp[tmpSize][2] = new Boolean(check.getState());
      tmpSize++;
    }

    Object[][] data = new Object[tmpSize][3];
    System.arraycopy(tmp, 0, data, 0, tmpSize);

    fr.setFeaturePriority(data);

    ap.paintAlignment(true);
  }

  MyCheckbox selectedCheck;

  boolean dragging = false;

  public void mousePressed(MouseEvent evt)
  {

    selectedCheck = (MyCheckbox) evt.getSource();

    if (fr.featureLinks != null
            && fr.featureLinks.containsKey(selectedCheck.type))
    {
      if (evt.getX() > selectedCheck.stringWidth + 20)
      {
        evt.consume();
      }
    }

  }

  public void mouseDragged(MouseEvent evt)
  {
    if (((Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }
    dragging = true;
  }

  public void mouseReleased(MouseEvent evt)
  {
    if (((Component) evt.getSource()).getParent() != featurePanel)
    {
      return;
    }

    Component comp = null;
    Checkbox target = null;

    int height = evt.getY() + evt.getComponent().getLocation().y;

    if (height > featurePanel.getSize().height)
    {

      comp = featurePanel
              .getComponent(featurePanel.getComponentCount() - 1);
    }
    else if (height < 0)
    {
      comp = featurePanel.getComponent(0);
    }
    else
    {
      comp = featurePanel.getComponentAt(evt.getX(), evt.getY()
              + evt.getComponent().getLocation().y);
    }

    if (comp != null && comp instanceof Checkbox)
    {
      target = (Checkbox) comp;
    }

    if (selectedCheck != null && target != null && selectedCheck != target)
    {
      int targetIndex = -1;
      for (int i = 0; i < featurePanel.getComponentCount(); i++)
      {
        if (target == featurePanel.getComponent(i))
        {
          targetIndex = i;
          break;
        }
      }

      featurePanel.remove(selectedCheck);
      featurePanel.add(selectedCheck, targetIndex);
      featurePanel.validate();
      itemStateChanged(null);
    }
  }

  public void setUserColour(String feature, Object originalColour)
  {
    if (originalColour instanceof Color
            || originalColour instanceof GraduatedColor)
    {
      fr.setColour(feature, originalColour);
    }
    else
    {
      throw new Error(
              MessageManager
                      .getString("error.implementation_error_unsupported_feature_colour_object"));
    }
    refreshTable();
  }

  public void refreshTable()
  {
    featurePanel.removeAll();
    resetTable(false);
    ap.paintAlignment(true);
  }

  public void mouseEntered(MouseEvent evt)
  {
  }

  public void mouseExited(MouseEvent evt)
  {
  }

  public void mouseClicked(MouseEvent evt)
  {
    MyCheckbox check = (MyCheckbox) evt.getSource();
    if ((evt.getModifiers() & InputEvent.BUTTON3_MASK) != 0)
    {
      this.popupSort(check, fr.getMinMax(), evt.getX(), evt.getY());
    }
    if (fr.featureLinks != null && fr.featureLinks.containsKey(check.type))
    {
      if (evt.getX() > check.stringWidth + 20)
      {
        evt.consume();
        String link = fr.featureLinks.get(check.type).toString();
        ap.alignFrame.showURL(link.substring(link.indexOf("|") + 1),
                link.substring(0, link.indexOf("|")));
      }
    }

    if (check.getParent() != featurePanel)
    {
      return;
    }

    if (evt.getClickCount() > 1)
    {
      Object fcol = fr.getFeatureStyle(check.type);
      if (fcol instanceof Color)
      {
        new UserDefinedColours(this, check.type, (Color) fcol);
      }
      else
      {
        new FeatureColourChooser(this, check.type);
        // write back the current colour object to update the table
        check.updateColor(fr.getFeatureStyle(check.type));
      }
    }
  }

  public void mouseMoved(MouseEvent evt)
  {
  }

  public void adjustmentValueChanged(AdjustmentEvent evt)
  {
    fr.setTransparency((100 - transparency.getValue()) / 100f);
    ap.seqPanel.seqCanvas.repaint();

  }

  class MyCheckbox extends Checkbox
  {
    public String type;

    public int stringWidth;

    boolean hasLink;

    GraduatedColor gcol;

    Color col;

    public void updateColor(Object newcol)
    {
      if (newcol instanceof Color)
      {
        col = (Color) newcol;
        gcol = null;
      }
      else if (newcol instanceof GraduatedColor)
      {
        gcol = (GraduatedColor) newcol;
        col = null;
      }
      else
      {
        throw new Error(
                MessageManager
                        .getString("error.invalid_colour_for_mycheckbox"));
      }
      if (col != null)
      {
        setBackground(col);
      }
      else
      {
        String vlabel = type;
        if (gcol.getThreshType() != AnnotationColourGradient.NO_THRESHOLD)
        {
          vlabel += " "
                  + ((gcol.getThreshType() == AnnotationColourGradient.ABOVE_THRESHOLD) ? "(>)"
                          : "(<)");
        }
        if (gcol.isColourByLabel())
        {
          setBackground(Color.white);
          vlabel += " (by Label)";
        }
        else
        {
          setBackground(gcol.getMinColor());
        }
        this.setLabel(vlabel);
      }
      repaint();
    }

    public MyCheckbox(String label, boolean checked, boolean haslink)
    {
      super(label, checked);
      type = label;
      FontMetrics fm = av.nullFrame.getFontMetrics(av.nullFrame.getFont());
      stringWidth = fm.stringWidth(label);
      this.hasLink = haslink;
    }

    public MyCheckbox(String type, boolean selected, boolean b,
            Object featureStyle)
    {
      this(type, selected, b);
      updateColor(featureStyle);
    }

    public void paint(Graphics g)
    {
      Dimension d = getSize();
      if (gcol != null)
      {
        if (gcol.isColourByLabel())
        {
          g.setColor(Color.white);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);
          /*
           * g.setColor(Color.black); Font f=g.getFont().deriveFont(9);
           * g.setFont(f);
           * 
           * // g.setFont(g.getFont().deriveFont( //
           * AffineTransform.getScaleInstance( //
           * width/g.getFontMetrics().stringWidth("Label"), //
           * height/g.getFontMetrics().getHeight()))); g.drawString("Label",
           * width/2, 0);
           */

        }
        else
        {
          Color maxCol = gcol.getMaxColor();
          g.setColor(maxCol);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);

        }
      }

      if (hasLink)
      {
        g.drawImage(linkImage, stringWidth + 25,
                (getSize().height - linkImage.getHeight(this)) / 2, this);
      }
    }
  }

}
