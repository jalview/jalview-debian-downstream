/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 * 
 * This file is part of Jalview.
 * 
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 */
package jalview.appletgui;

import java.util.*;

import java.awt.*;

import java.awt.event.*;

import jalview.appletgui.FeatureSettings.MyCheckbox;
import jalview.datamodel.*;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.GraduatedColor;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FeatureRenderer implements jalview.api.FeatureRenderer
{
  AlignViewport av;

  Hashtable featureColours = new Hashtable();

  // A higher level for grouping features of a
  // particular type
  Hashtable featureGroups = null;

  // Holds web links for feature groups and feature types
  // in the form label|link
  Hashtable featureLinks = null;

  // This is actually an Integer held in the hashtable,
  // Retrieved using the key feature type
  Object currentColour;

  String[] renderOrder;

  FontMetrics fm;

  int charOffset;

  float transparency = 1f;

  TransparencySetter transparencySetter = null;

  /**
   * Creates a new FeatureRenderer object.
   * 
   * @param av
   *          DOCUMENT ME!
   */
  public FeatureRenderer(AlignViewport av)
  {
    this.av = av;

    if (!System.getProperty("java.version").startsWith("1.1"))
    {
      transparencySetter = new TransparencySetter();
    }
  }

  public void transferSettings(FeatureRenderer fr)
  {
    renderOrder = fr.renderOrder;
    featureGroups = fr.featureGroups;
    featureColours = fr.featureColours;
    transparency = fr.transparency;
    if (av!=null && fr.av!=null && fr.av!=av)
    {
      if (fr.av.featuresDisplayed!=null)
      {
        if (av.featuresDisplayed==null)
        {
          av.featuresDisplayed = new Hashtable();
        } else {
          av.featuresDisplayed.clear();
        }
        Enumeration en=fr.av.featuresDisplayed.keys();
        while (en.hasMoreElements())
        {
          av.featuresDisplayed.put(en.nextElement(), Boolean.TRUE);
        }
      }
    }
  }

  static String lastFeatureAdded;

  static String lastFeatureGroupAdded;

  static String lastDescriptionAdded;

  int featureIndex = 0;

  boolean deleteFeature = false;

  FeatureColourPanel colourPanel;

  class FeatureColourPanel extends Panel
  {
    String label = "";

    private Color maxCol;

    private boolean isColourByLabel, isGcol;

    /**
     * render a feature style in the amend feature dialog box
     */
    public void updateColor(Object newcol)
    {

      Color bg, col = null;
      GraduatedColor gcol = null;
      String vlabel = "";
      if (newcol instanceof Color)
      {
        isGcol = false;
        col = (Color) newcol;
        gcol = null;
      }
      else if (newcol instanceof GraduatedColor)
      {
        isGcol = true;
        gcol = (GraduatedColor) newcol;
        col = null;
      }
      else
      {
        throw new Error("Invalid color for MyCheckBox");
      }
      if (col != null)
      {
        setBackground(bg = col);
      }
      else
      {
        if (gcol.getThreshType() != AnnotationColourGradient.NO_THRESHOLD)
        {
          vlabel += " "
                  + ((gcol.getThreshType() == AnnotationColourGradient.ABOVE_THRESHOLD) ? "(>)"
                          : "(<)");
        }
        if (isColourByLabel = gcol.isColourByLabel())
        {
          setBackground(bg = Color.white);
          vlabel += " (by Label)";
        }
        else
        {
          setBackground(bg = gcol.getMinColor());
          maxCol = gcol.getMaxColor();
        }
      }
      label = vlabel;
      setBackground(bg);
      repaint();
    }

    FeatureColourPanel()
    {
      super(null);
    }

    public void paint(Graphics g)
    {
      Dimension d = getSize();
      if (isGcol)
      {
        if (isColourByLabel)
        {
          g.setColor(Color.white);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);
          g.setColor(Color.black);
          Font f = new Font("Verdana", Font.PLAIN, 10);
          g.setFont(f);
          g.drawString("Label", 0, 0);
        }
        else
        {
          g.setColor(maxCol);
          g.fillRect(d.width / 2, 0, d.width / 2, d.height);

        }
      }
    }

  }

  boolean amendFeatures(final SequenceI[] sequences,
          final SequenceFeature[] features, boolean newFeatures,
          final AlignmentPanel ap)
  {
    Panel bigPanel = new Panel(new BorderLayout());
    final TextField name = new TextField(16);
    final TextField source = new TextField(16);
    final TextArea description = new TextArea(3, 35);
    final TextField start = new TextField(8);
    final TextField end = new TextField(8);
    final Choice overlaps;
    Button deleteButton = new Button("Delete");
    deleteFeature = false;

    colourPanel = new FeatureColourPanel();
    colourPanel.setSize(110, 15);
    final FeatureRenderer fr = this;

    Panel panel = new Panel(new GridLayout(3, 1));

    featureIndex = 0; // feature to be amended.
    Panel tmp;

    // /////////////////////////////////////
    // /MULTIPLE FEATURES AT SELECTED RESIDUE
    if (!newFeatures && features.length > 1)
    {
      panel = new Panel(new GridLayout(4, 1));
      tmp = new Panel();
      tmp.add(new Label("Select Feature: "));
      overlaps = new Choice();
      for (int i = 0; i < features.length; i++)
      {
        String item = features[i].getType() + "/" + features[i].getBegin()
                + "-" + features[i].getEnd();

        if (features[i].getFeatureGroup() != null)
          item += " (" + features[i].getFeatureGroup() + ")";

        overlaps.addItem(item);
      }

      tmp.add(overlaps);

      overlaps.addItemListener(new java.awt.event.ItemListener()
      {
        public void itemStateChanged(java.awt.event.ItemEvent e)
        {
          int index = overlaps.getSelectedIndex();
          if (index != -1)
          {
            featureIndex = index;
            name.setText(features[index].getType());
            description.setText(features[index].getDescription());
            source.setText(features[index].getFeatureGroup());
            start.setText(features[index].getBegin() + "");
            end.setText(features[index].getEnd() + "");

            SearchResults highlight = new SearchResults();
            highlight.addResult(sequences[0], features[index].getBegin(),
                    features[index].getEnd());

            ap.seqPanel.seqCanvas.highlightSearchResults(highlight);

          }
          Object col = getFeatureStyle(name.getText());
          if (col == null)
          {
            col = new jalview.schemes.UserColourScheme()
                    .createColourFromName(name.getText());
          }

          colourPanel.updateColor(col);
        }
      });

      panel.add(tmp);
    }
    // ////////
    // ////////////////////////////////////

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Name: ", Label.RIGHT));
    tmp.add(name);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Group: ", Label.RIGHT));
    tmp.add(source);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label("Colour: ", Label.RIGHT));
    tmp.add(colourPanel);

    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new Panel();
    panel.add(new Label("Description: ", Label.RIGHT));
    panel.add(new ScrollPane().add(description));

    if (!newFeatures)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new Panel();
      panel.add(new Label(" Start:", Label.RIGHT));
      panel.add(start);
      panel.add(new Label("  End:", Label.RIGHT));
      panel.add(end);
      bigPanel.add(panel, BorderLayout.CENTER);
    }
    else
    {
      bigPanel.add(panel, BorderLayout.CENTER);
    }

    if (lastFeatureAdded == null)
    {
      if (features[0].type != null)
      {
        lastFeatureAdded = features[0].type;
      }
      else
      {
        lastFeatureAdded = "feature_1";
      }
    }

    if (lastFeatureGroupAdded == null)
    {
      if (features[0].featureGroup != null)
      {
        lastFeatureGroupAdded = features[0].featureGroup;
      }
      else
      {
        lastFeatureAdded = "Jalview";
      }
    }

    String title = newFeatures ? "Create New Sequence Feature(s)"
            : "Amend/Delete Features for " + sequences[0].getName();

    final JVDialog dialog = new JVDialog(ap.alignFrame, title, true, 385,
            240);

    dialog.setMainPanel(bigPanel);

    if (newFeatures)
    {
      name.setText(lastFeatureAdded);
      source.setText(lastFeatureGroupAdded);
    }
    else
    {
      dialog.ok.setLabel("Amend");
      dialog.buttonPanel.add(deleteButton, 1);
      deleteButton.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          deleteFeature = true;
          dialog.setVisible(false);
        }
      });
      name.setText(features[0].getType());
      source.setText(features[0].getFeatureGroup());
    }

    start.setText(features[0].getBegin() + "");
    end.setText(features[0].getEnd() + "");
    description.setText(features[0].getDescription());
    Color col = getColour(name.getText());
    if (col == null)
    {
      col = new jalview.schemes.UserColourScheme()
              .createColourFromName(name.getText());
    }
    Object fcol = getFeatureStyle(name.getText());
    // simply display the feature color in a box
    colourPanel.updateColor(fcol);
    dialog.setResizable(true);
    // TODO: render the graduated color in the box.
    colourPanel.addMouseListener(new java.awt.event.MouseAdapter()
    {
      public void mousePressed(java.awt.event.MouseEvent evt)
      {
        if (!colourPanel.isGcol)
        {
          new UserDefinedColours(fr, ap.alignFrame);
        }
        else
        {
          FeatureColourChooser fcc = new FeatureColourChooser(
                  ap.alignFrame, name.getText());
          dialog.transferFocus();
        }
      }
    });
    dialog.setVisible(true);

    jalview.io.FeaturesFile ffile = new jalview.io.FeaturesFile();

    if (dialog.accept)
    {
      // This ensures that the last sequence
      // is refreshed and new features are rendered
      lastSeq = null;
      lastFeatureAdded = name.getText().trim();
      lastFeatureGroupAdded = source.getText().trim();
      lastDescriptionAdded = description.getText().replace('\n', ' ');
    }

    if (lastFeatureGroupAdded != null && lastFeatureGroupAdded.length() < 1)
      lastFeatureGroupAdded = null;

    if (!newFeatures)
    {

      SequenceFeature sf = features[featureIndex];
      if (dialog.accept)
      {
        sf.type = lastFeatureAdded;
        sf.featureGroup = lastFeatureGroupAdded;
        sf.description = lastDescriptionAdded;
        if (!colourPanel.isGcol)
        {
          // update colour - otherwise its already done.
          setColour(sf.type, colourPanel.getBackground());
        }
        try
        {
          sf.begin = Integer.parseInt(start.getText());
          sf.end = Integer.parseInt(end.getText());
        } catch (NumberFormatException ex)
        {
        }

        ffile.parseDescriptionHTML(sf, false);
      }
      if (deleteFeature)
      {
        sequences[0].deleteFeature(sf);
      }

    }
    else
    {
      if (dialog.accept && name.getText().length() > 0)
      {
        for (int i = 0; i < sequences.length; i++)
        {
          features[i].type = lastFeatureAdded;
          features[i].featureGroup = lastFeatureGroupAdded;
          features[i].description = lastDescriptionAdded;
          sequences[i].addSequenceFeature(features[i]);
          ffile.parseDescriptionHTML(features[i], false);
        }

        if (av.featuresDisplayed == null)
        {
          av.featuresDisplayed = new Hashtable();
        }

        if (featureGroups == null)
        {
          featureGroups = new Hashtable();
        }

        col = colourPanel.getBackground();
        // setColour(lastFeatureAdded, fcol);

        if (lastFeatureGroupAdded != null)
        {
          featureGroups.put(lastFeatureGroupAdded, new Boolean(true));
        }
        if (fcol instanceof Color)
        {
          setColour(lastFeatureAdded, fcol);
        }
        av.featuresDisplayed.put(lastFeatureAdded,
                getFeatureStyle(lastFeatureAdded));

        findAllFeatures();

        String[] tro = new String[renderOrder.length];
        tro[0] = renderOrder[renderOrder.length - 1];
        System.arraycopy(renderOrder, 0, tro, 1, renderOrder.length - 1);
        renderOrder = tro;
      }
      else
      {
        // no update to the alignment
        return false;
      }
    }
    // refresh the alignment and the feature settings dialog
    if (av.featureSettings != null)
    {
      av.featureSettings.refreshTable();
    }
    // findAllFeatures();

    ap.paintAlignment(true);

    return true;
  }

  public Color findFeatureColour(Color initialCol, SequenceI seq, int i)
  {
    overview = true;
    if (!av.showSequenceFeatures)
    {
      return initialCol;
    }

    lastSeq = seq;
    sequenceFeatures = lastSeq.getSequenceFeatures();
    if (sequenceFeatures == null)
    {
      return initialCol;
    }

    sfSize = sequenceFeatures.length;

    if (jalview.util.Comparison.isGap(lastSeq.getCharAt(i)))
    {
      return Color.white;
    }

    currentColour = null;

    drawSequence(null, lastSeq, lastSeq.findPosition(i), -1, -1);

    if (currentColour == null)
    {
      return initialCol;
    }

    return new Color(((Integer) currentColour).intValue());
  }

  /**
   * This is used by the Molecule Viewer to get the accurate colour of the
   * rendered sequence
   */
  boolean overview = false;

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param seq
   *          DOCUMENT ME!
   * @param sg
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   * @param x1
   *          DOCUMENT ME!
   * @param y1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  // String type;
  // SequenceFeature sf;
  SequenceI lastSeq;

  SequenceFeature[] sequenceFeatures;

  int sfSize, sfindex, spos, epos;

  synchronized public void drawSequence(Graphics g, SequenceI seq,
          int start, int end, int y1)
  {
    if (seq.getSequenceFeatures() == null
            || seq.getSequenceFeatures().length == 0)
    {
      return;
    }

    if (transparencySetter != null && g != null)
    {
      transparencySetter.setTransparency(g, transparency);
    }

    if (lastSeq == null || seq != lastSeq
            || sequenceFeatures != seq.getSequenceFeatures())
    {
      lastSeq = seq;
      sequenceFeatures = seq.getSequenceFeatures();
      sfSize = sequenceFeatures.length;
    }

    if (av.featuresDisplayed == null || renderOrder == null)
    {
      findAllFeatures();
      if (av.featuresDisplayed.size() < 1)
      {
        return;
      }

      sequenceFeatures = seq.getSequenceFeatures();
      sfSize = sequenceFeatures.length;
    }
    if (!overview)
    {
      spos = lastSeq.findPosition(start);
      epos = lastSeq.findPosition(end);
      if (g != null)
      {
        fm = g.getFontMetrics();
      }
    }
    String type;
    for (int renderIndex = 0; renderIndex < renderOrder.length; renderIndex++)
    {
      type = renderOrder[renderIndex];
      if (!av.featuresDisplayed.containsKey(type))
      {
        continue;
      }

      // loop through all features in sequence to find
      // current feature to render
      for (sfindex = 0; sfindex < sfSize; sfindex++)
      {
        if (!sequenceFeatures[sfindex].type.equals(type))
        {
          continue;
        }

        if (featureGroups != null
                && sequenceFeatures[sfindex].featureGroup != null
                && featureGroups
                        .containsKey(sequenceFeatures[sfindex].featureGroup)
                && !((Boolean) featureGroups
                        .get(sequenceFeatures[sfindex].featureGroup))
                        .booleanValue())
        {
          continue;
        }

        if (!overview
                && (sequenceFeatures[sfindex].getBegin() > epos || sequenceFeatures[sfindex]
                        .getEnd() < spos))
        {
          continue;
        }

        if (overview)
        {
          if (sequenceFeatures[sfindex].begin <= start
                  && sequenceFeatures[sfindex].end >= start)
          {
            currentColour = new Integer(
                    getColour(sequenceFeatures[sfindex]).getRGB());// av.featuresDisplayed
            // .get(sequenceFeatures[sfindex].type);
          }

        }
        else if (sequenceFeatures[sfindex].type.equals("disulfide bond"))
        {

          renderFeature(g, seq,
                  seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                  seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                  getColour(sequenceFeatures[sfindex])
                  // new Color(((Integer) av.featuresDisplayed
                  // .get(sequenceFeatures[sfindex].type)).intValue())
                  , start, end, y1);
          renderFeature(g, seq,
                  seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                  seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                  getColour(sequenceFeatures[sfindex])
                  // new Color(((Integer) av.featuresDisplayed
                  // .get(sequenceFeatures[sfindex].type)).intValue())
                  , start, end, y1);

        }
        else
        {
          if (showFeature(sequenceFeatures[sfindex]))
          {
            renderFeature(g, seq,
                    seq.findIndex(sequenceFeatures[sfindex].begin) - 1,
                    seq.findIndex(sequenceFeatures[sfindex].end) - 1,
                    getColour(sequenceFeatures[sfindex]), start, end, y1);
          }
        }

      }
    }

    if (transparencySetter != null && g != null)
    {
      transparencySetter.setTransparency(g, 1.0f);
    }
  }

  char s;

  int i;

  void renderFeature(Graphics g, SequenceI seq, int fstart, int fend,
          Color featureColour, int start, int end, int y1)
  {

    if (((fstart <= end) && (fend >= start)))
    {
      if (fstart < start)
      { // fix for if the feature we have starts before the sequence start,
        fstart = start; // but the feature end is still valid!!
      }

      if (fend >= end)
      {
        fend = end;
      }

      for (i = fstart; i <= fend; i++)
      {
        s = seq.getCharAt(i);

        if (jalview.util.Comparison.isGap(s))
        {
          continue;
        }

        g.setColor(featureColour);

        g.fillRect((i - start) * av.charWidth, y1, av.charWidth,
                av.charHeight);

        if (!av.validCharWidth)
        {
          continue;
        }

        g.setColor(Color.white);
        charOffset = (av.charWidth - fm.charWidth(s)) / 2;
        g.drawString(String.valueOf(s), charOffset
                + (av.charWidth * (i - start)), (y1 + av.charHeight)
                - av.charHeight / 5); // pady = height / 5;

      }
    }
  }

  Hashtable minmax = null;

  /**
   * Called when alignment in associated view has new/modified features to
   * discover and display.
   * 
   */
  public void featuresAdded()
  {
    lastSeq = null;
    findAllFeatures();
  }

  /**
   * find all features on the alignment
   */
  void findAllFeatures()
  {
    jalview.schemes.UserColourScheme ucs = new jalview.schemes.UserColourScheme();

    av.featuresDisplayed = new Hashtable();
    Vector allfeatures = new Vector();
    minmax = new Hashtable();

    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      SequenceFeature[] features = av.alignment.getSequenceAt(i)
              .getSequenceFeatures();

      if (features == null)
      {
        continue;
      }

      int index = 0;
      while (index < features.length)
      {
        if (features[index].begin == 0 && features[index].end == 0)
        {
          index++;
          continue;
        }
        if (!av.featuresDisplayed.containsKey(features[index].getType()))
        {
          if (getColour(features[index].getType()) == null)
          {
            featureColours.put(features[index].getType(),
                    ucs.createColourFromName(features[index].getType()));
          }

          av.featuresDisplayed.put(features[index].getType(), new Integer(
                  getColour(features[index].getType()).getRGB()));
          allfeatures.addElement(features[index].getType());
        }
        if (features[index].score != Float.NaN)
        {
          int nonpos = features[index].getBegin() >= 1 ? 0 : 1;
          float[][] mm = (float[][]) minmax.get(features[index].getType());
          if (mm == null)
          {
            mm = new float[][]
            { null, null };
            minmax.put(features[index].getType(), mm);
          }
          if (mm[nonpos] == null)
          {
            mm[nonpos] = new float[]
            { features[index].score, features[index].score };

          }
          else
          {
            if (mm[nonpos][0] > features[index].score)
            {
              mm[nonpos][0] = features[index].score;
            }
            if (mm[nonpos][1] < features[index].score)
            {
              mm[nonpos][1] = features[index].score;
            }
          }
        }

        index++;
      }
    }

    renderOrder = new String[allfeatures.size()];
    Enumeration en = allfeatures.elements();
    int i = allfeatures.size() - 1;
    while (en.hasMoreElements())
    {
      renderOrder[i] = en.nextElement().toString();
      i--;
    }
  }

  /**
   * get a feature style object for the given type string. Creates a
   * java.awt.Color for a featureType with no existing colourscheme. TODO:
   * replace return type with object implementing standard abstract colour/style
   * interface
   * 
   * @param featureType
   * @return java.awt.Color or GraduatedColor
   */
  public Object getFeatureStyle(String featureType)
  {
    Object fc = featureColours.get(featureType);
    if (fc == null)
    {
      jalview.schemes.UserColourScheme ucs = new jalview.schemes.UserColourScheme();
      Color col = ucs.createColourFromName(featureType);
      featureColours.put(featureType, fc = col);
    }
    return fc;
  }

  public Color getColour(String featureType)
  {
    Object fc = getFeatureStyle(featureType);

    if (fc instanceof Color)
    {
      return (Color) fc;
    }
    else
    {
      if (fc instanceof GraduatedColor)
      {
        return ((GraduatedColor) fc).getMaxColor();
      }
    }
    throw new Error("Implementation Error: Unrecognised render object "
            + fc.getClass() + " for features of type " + featureType);
  }

  /**
   * 
   * @param sequenceFeature
   * @return true if feature is visible.
   */
  private boolean showFeature(SequenceFeature sequenceFeature)
  {
    Object fc = getFeatureStyle(sequenceFeature.type);
    if (fc instanceof GraduatedColor)
    {
      return ((GraduatedColor) fc).isColored(sequenceFeature);
    }
    else
    {
      return true;
    }
  }

  /**
   * implement graduated colouring for features with scores
   * 
   * @param feature
   * @return render colour for the given feature
   */
  public Color getColour(SequenceFeature feature)
  {
    Object fc = getFeatureStyle(feature.getType());
    if (fc instanceof Color)
    {
      return (Color) fc;
    }
    else
    {
      if (fc instanceof GraduatedColor)
      {
        return ((GraduatedColor) fc).findColor(feature);
      }
    }
    throw new Error("Implementation Error: Unrecognised render object "
            + fc.getClass() + " for features of type " + feature.getType());
  }

  public void setColour(String featureType, Object col)
  {
    // overwrite
    // Color _col = (col instanceof Color) ? ((Color) col) : (col instanceof
    // GraduatedColor) ? ((GraduatedColor) col).getMaxColor() : null;
    // Object c = featureColours.get(featureType);
    // if (c == null || c instanceof Color || (c instanceof GraduatedColor &&
    // !((GraduatedColor)c).getMaxColor().equals(_col)))
    {
      featureColours.put(featureType, col);
    }
  }

  public void setFeaturePriority(Object[][] data)
  {
    // The feature table will display high priority
    // features at the top, but theses are the ones
    // we need to render last, so invert the data
    if (av.featuresDisplayed != null)
    {
      av.featuresDisplayed.clear();
    }

    /*
     * if (visibleNew) { if (av.featuresDisplayed != null) {
     * av.featuresDisplayed.clear(); } else { av.featuresDisplayed = new
     * Hashtable(); } } if (data == null) { return; }
     */

    renderOrder = new String[data.length];

    if (data.length > 0)
    {
      for (int i = 0; i < data.length; i++)
      {
        String type = data[i][0].toString();
        setColour(type, data[i][1]);
        if (((Boolean) data[i][2]).booleanValue())
        {
          av.featuresDisplayed.put(type, new Integer(getColour(type)
                  .getRGB()));
        }

        renderOrder[data.length - i - 1] = type;
      }
    }
  }

  /**
   * @return a simple list of feature group names or null
   */
  public String[] getGroups()
  {
    buildGroupHash();
    if (featureGroups != null)
    {
      String[] gps = new String[featureGroups.size()];
      Enumeration gn = featureGroups.keys();
      int i = 0;
      while (gn.hasMoreElements())
      {
        gps[i++] = (String) gn.nextElement();
      }
      return gps;
    }
    return null;
  }

  /**
   * get visible or invisible groups
   * 
   * @param visible
   *          true to return visible groups, false to return hidden ones.
   * @return list of groups
   */
  public String[] getGroups(boolean visible)
  {
    buildGroupHash();
    if (featureGroups != null)
    {
      Vector gp = new Vector();

      Enumeration gn = featureGroups.keys();
      while (gn.hasMoreElements())
      {
        String nm = (String) gn.nextElement();
        Boolean state = (Boolean) featureGroups.get(nm);
        if (state.booleanValue() == visible)
        {
          gp.addElement(nm);
        }
      }
      String[] gps = new String[gp.size()];
      gp.copyInto(gps);

      int i = 0;
      while (gn.hasMoreElements())
      {
        gps[i++] = (String) gn.nextElement();
      }
      return gps;
    }
    return null;
  }

  /**
   * set all feature groups in toset to be visible or invisible
   * 
   * @param toset
   *          group names
   * @param visible
   *          the state of the named groups to set
   */
  public void setGroupState(String[] toset, boolean visible)
  {
    buildGroupHash();
    if (toset != null && toset.length > 0 && featureGroups != null)
    {
      boolean rdrw = false;
      for (int i = 0; i < toset.length; i++)
      {
        Object st = featureGroups.get(toset[i]);
        if (st != null)
        {
          featureGroups.put(toset[i], new Boolean(visible));
          rdrw = rdrw || (visible != ((Boolean) st).booleanValue());
        }
      }
      if (rdrw)
      {
        if (this.av != null)
          if (this.av.featureSettings != null)
          {
            av.featureSettings.rebuildGroups();
            this.av.featureSettings.resetTable(true);
          }
          else
          {
            buildFeatureHash();
          }
        if (av != null)
        {
          av.alignmentChanged(null);
        }
      }
    }
  }

  /**
   * analyse alignment for groups and hash tables (used to be embedded in
   * FeatureSettings.setTableData)
   * 
   * @return true if features are on the alignment
   */
  public boolean buildGroupHash()
  {
    boolean alignmentHasFeatures = false;
    if (featureGroups == null)
    {
      featureGroups = new Hashtable();
    }
    Vector allFeatures = new Vector();
    Vector allGroups = new Vector();
    SequenceFeature[] tmpfeatures;
    String group;
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      if (av.alignment.getSequenceAt(i).getSequenceFeatures() == null)
      {
        continue;
      }

      alignmentHasFeatures = true;

      tmpfeatures = av.alignment.getSequenceAt(i).getSequenceFeatures();
      int index = 0;
      while (index < tmpfeatures.length)
      {
        if (tmpfeatures[index].getFeatureGroup() != null)
        {
          group = tmpfeatures[index].featureGroup;
          if (!allGroups.contains(group))
          {
            allGroups.addElement(group);

            boolean visible = true;
            if (featureGroups.containsKey(group))
            {
              visible = ((Boolean) featureGroups.get(group)).booleanValue();
            }
            else
            {
              featureGroups.put(group, new Boolean(visible));
            }
          }
        }

        if (!allFeatures.contains(tmpfeatures[index].getType()))
        {
          allFeatures.addElement(tmpfeatures[index].getType());
        }
        index++;
      }
    }

    return alignmentHasFeatures;
  }

  /**
   * rebuild the featuresDisplayed and renderorder list based on the
   * featureGroups hash and any existing display state and force a repaint if
   * necessary
   * 
   * @return true if alignment has visible features
   */
  public boolean buildFeatureHash()
  {
    boolean alignmentHasFeatures = false;
    if (featureGroups == null)
    {
      alignmentHasFeatures = buildGroupHash();
    }
    if (!alignmentHasFeatures)
      return false;
    Hashtable fdisp = av.featuresDisplayed;
    Vector allFeatures = new Vector();
    SequenceFeature[] tmpfeatures;
    String group;
    for (int i = 0; i < av.alignment.getHeight(); i++)
    {
      if (av.alignment.getSequenceAt(i).getSequenceFeatures() == null)
      {
        continue;
      }

      alignmentHasFeatures = true;

      tmpfeatures = av.alignment.getSequenceAt(i).getSequenceFeatures();
      int index = 0;
      while (index < tmpfeatures.length)
      {
        boolean visible = true;
        if (tmpfeatures[index].getFeatureGroup() != null)
        {
          group = tmpfeatures[index].featureGroup;
          if (featureGroups.containsKey(group))
          {
            visible = ((Boolean) featureGroups.get(group)).booleanValue();
          }
        }

        if (visible && !allFeatures.contains(tmpfeatures[index].getType()))
        {
          allFeatures.addElement(tmpfeatures[index].getType());
        }
        index++;
      }
    }
    if (allFeatures.size() > 0)
    {
      String[] neworder = new String[allFeatures.size()];
      int p = neworder.length - 1;
      for (int i = renderOrder.length - 1; i >= 0; i--)
      {
        if (allFeatures.contains(renderOrder[i]))
        {
          neworder[p--] = renderOrder[i];
          allFeatures.removeElement(renderOrder[i]);
        }
        else
        {
          av.featuresDisplayed.remove(renderOrder[i]);
        }
      }
      for (int i = allFeatures.size() - 1; i > 0; i++)
      {
        Object e = allFeatures.elementAt(i);
        if (e != null)
        {
          neworder[p--] = (String) e;
          av.featuresDisplayed.put(e, getColour((String) e));
        }
      }
      renderOrder = neworder;
      return true;
    }

    return alignmentHasFeatures;
  }

  /**
   * 
   * @return the displayed feature type as an array of strings
   */
  protected String[] getDisplayedFeatureTypes()
  {
    String[] typ = null;
    synchronized (renderOrder)
    {
      typ = new String[renderOrder.length];
      System.arraycopy(renderOrder, 0, typ, 0, typ.length);
      for (int i = 0; i < typ.length; i++)
      {
        if (av.featuresDisplayed.get(typ[i]) == null)
        {
          typ[i] = null;
        }
      }
    }
    return typ;
  }
}

class TransparencySetter
{
  void setTransparency(Graphics g, float value)
  {
    Graphics2D g2 = (Graphics2D) g;
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            value));
  }
}
