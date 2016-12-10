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
package jalview.appletgui;

import jalview.api.FeatureColourI;
import jalview.datamodel.SearchResults;
import jalview.datamodel.SearchResultsI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.FeaturesFile;
import jalview.schemes.FeatureColour;
import jalview.schemes.UserColourScheme;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class FeatureRenderer extends
        jalview.renderer.seqfeatures.FeatureRenderer
{

  // Holds web links for feature groups and feature types
  // in the form label|link
  Hashtable featureLinks = null;

  /**
   * Creates a new FeatureRenderer object.
   * 
   * @param av
   */
  public FeatureRenderer(AlignmentViewport av)
  {
    super(av);

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
    public void updateColor(FeatureColourI newcol)
    {
      Color bg = null;
      String vlabel = "";
      if (newcol.isSimpleColour())
      {
        bg = newcol.getColour();
        setBackground(bg);
      }
      else
      {
        if (newcol.isAboveThreshold())
        {
          vlabel += " (>)";
        }
        else if (newcol.isBelowThreshold())
        {
          vlabel += " (<)";
        }

        if (isColourByLabel = newcol.isColourByLabel())
        {
          setBackground(bg = Color.white);
          vlabel += " (by Label)";
        }
        else
        {
          setBackground(bg = newcol.getMinColour());
          maxCol = newcol.getMaxColour();
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

    @Override
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
          g.drawString(MessageManager.getString("label.label"), 0, 0);
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
        {
          item += " (" + features[i].getFeatureGroup() + ")";
        }

        overlaps.addItem(item);
      }

      tmp.add(overlaps);

      overlaps.addItemListener(new java.awt.event.ItemListener()
      {
        @Override
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

            SearchResultsI highlight = new SearchResults();
            highlight.addResult(sequences[0], features[index].getBegin(),
                    features[index].getEnd());

            ap.seqPanel.seqCanvas.highlightSearchResults(highlight);

          }
          FeatureColourI col = getFeatureStyle(name.getText());
          if (col == null)
          {
            Color generatedColour = UserColourScheme
                    .createColourFromName(name.getText());
            col = new FeatureColour(generatedColour);
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
    tmp.add(new Label(MessageManager.getString("label.name:"), Label.RIGHT));
    tmp.add(name);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label(MessageManager.getString("label.group:"), Label.RIGHT));
    tmp.add(source);

    tmp = new Panel();
    panel.add(tmp);
    tmp.add(new Label(MessageManager.getString("label.colour"), Label.RIGHT));
    tmp.add(colourPanel);

    bigPanel.add(panel, BorderLayout.NORTH);

    panel = new Panel();
    panel.add(new Label(MessageManager.getString("label.description:"),
            Label.RIGHT));
    panel.add(new ScrollPane().add(description));

    if (!newFeatures)
    {
      bigPanel.add(panel, BorderLayout.SOUTH);

      panel = new Panel();
      panel.add(new Label(MessageManager.getString("label.start"),
              Label.RIGHT));
      panel.add(start);
      panel.add(new Label(MessageManager.getString("label.end"),
              Label.RIGHT));
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

    String title = newFeatures ? MessageManager
            .getString("label.create_new_sequence_features")
            : MessageManager.formatMessage("label.amend_delete_features",
                    new String[] { sequences[0].getName() });

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
      dialog.ok.setLabel(MessageManager.getString("label.amend"));
      dialog.buttonPanel.add(deleteButton, 1);
      deleteButton.addActionListener(new ActionListener()
      {
        @Override
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
    // lookup (or generate) the feature colour
    FeatureColourI fcol = getFeatureStyle(name.getText());
    // simply display the feature color in a box
    colourPanel.updateColor(fcol);
    dialog.setResizable(true);
    // TODO: render the graduated color in the box.
    colourPanel.addMouseListener(new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (!colourPanel.isGcol)
        {
          new UserDefinedColours(fr, ap.alignFrame);
        }
        else
        {
          new FeatureColourChooser(ap.alignFrame, name.getText());
          dialog.transferFocus();
        }
      }
    });
    dialog.setVisible(true);

    FeaturesFile ffile = new FeaturesFile();

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
    {
      lastFeatureGroupAdded = null;
    }

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
          setColour(sf.type, new FeatureColour(colourPanel.getBackground()));
        }
        try
        {
          sf.begin = Integer.parseInt(start.getText());
          sf.end = Integer.parseInt(end.getText());
        } catch (NumberFormatException ex)
        {
        }

        ffile.parseDescriptionHTML(sf, false);
        setVisible(lastFeatureAdded); // if user edited name then make sure new
                                      // type is visible
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

        Color newColour = colourPanel.getBackground();
        // setColour(lastFeatureAdded, fcol);

        if (lastFeatureGroupAdded != null)
        {
          setGroupVisibility(lastFeatureGroupAdded, true);
        }
        setColour(lastFeatureAdded, new FeatureColour(newColour)); // was fcol
        setVisible(lastFeatureAdded);
        findAllFeatures(false); // different to original applet behaviour ?
        // findAllFeatures();
      }
      else
      {
        // no update to the alignment
        return false;
      }
    }
    // refresh the alignment and the feature settings dialog
    if (((jalview.appletgui.AlignViewport) av).featureSettings != null)
    {
      ((jalview.appletgui.AlignViewport) av).featureSettings.refreshTable();
    }
    // findAllFeatures();

    ap.paintAlignment(true);

    return true;
  }
}
