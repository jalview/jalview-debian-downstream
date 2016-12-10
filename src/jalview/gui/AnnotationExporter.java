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

import jalview.api.FeatureColourI;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.SequenceI;
import jalview.io.AnnotationFile;
import jalview.io.FeaturesFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.util.MessageManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

/**
 * 
 * GUI dialog for exporting features or alignment annotations depending upon
 * which method is called.
 * 
 * @author AMW
 * 
 */
public class AnnotationExporter extends JPanel
{
  JInternalFrame frame;

  AlignmentPanel ap;

  boolean features = true;

  private AlignmentAnnotation[] annotations;

  private boolean wholeView;

  public AnnotationExporter()
  {
    try
    {
      jbInit();
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }

    frame = new JInternalFrame();
    frame.setContentPane(this);
    frame.setLayer(JLayeredPane.PALETTE_LAYER);
    Desktop.addInternalFrame(frame, "", frame.getPreferredSize().width,
            frame.getPreferredSize().height);
  }

  public void exportFeatures(AlignmentPanel ap)
  {
    this.ap = ap;
    features = true;
    CSVFormat.setVisible(false);
    frame.setTitle(MessageManager.getString("label.export_features"));
  }

  public void exportAnnotations(AlignmentPanel ap)
  {
    this.ap = ap;
    annotations = ap.av.isShowAnnotation() ? null : ap.av.getAlignment()
            .getAlignmentAnnotation();
    wholeView = true;
    startExportAnnotation();
  }

  public void exportAnnotations(AlignmentPanel alp,
          AlignmentAnnotation[] toExport)
  {
    ap = alp;
    annotations = toExport;
    wholeView = false;
    startExportAnnotation();
  }

  private void startExportAnnotation()
  {
    features = false;
    GFFFormat.setVisible(false);
    CSVFormat.setVisible(true);
    frame.setTitle(MessageManager.getString("label.export_annotations"));
  }

  public void toFile_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(features ? MessageManager
            .getString("label.save_features_to_file") : MessageManager
            .getString("label.save_annotation_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String text = getFileContents();

      try
      {
        java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.FileWriter(chooser.getSelectedFile()));

        out.print(text);
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }

    close_actionPerformed(null);
  }

  private String getFileContents()
  {
    String text = MessageManager
            .getString("label.no_features_on_alignment");
    if (features)
    {
      Map<String, FeatureColourI> displayedFeatureColours = ap
              .getFeatureRenderer().getDisplayedFeatureCols();
      FeaturesFile formatter = new FeaturesFile();
      SequenceI[] sequences = ap.av.getAlignment().getSequencesArray();
      Map<String, FeatureColourI> featureColours = ap.getFeatureRenderer()
              .getDisplayedFeatureCols();
      boolean includeNonPositional = ap.av.isShowNPFeats();
      if (GFFFormat.isSelected())
      {
        text = new FeaturesFile().printGffFormat(ap.av.getAlignment()
                .getDataset().getSequencesArray(), displayedFeatureColours,
                true, ap.av.isShowNPFeats());
        text = formatter.printGffFormat(sequences, featureColours, true,
                includeNonPositional);
      }
      else
      {
        text = new FeaturesFile().printJalviewFormat(ap.av.getAlignment()
                .getDataset().getSequencesArray(), displayedFeatureColours,
                true, ap.av.isShowNPFeats()); // ap.av.featuresDisplayed);
        text = formatter.printJalviewFormat(sequences, featureColours,
                true, includeNonPositional);
      }
    }
    else
    {
      if (CSVFormat.isSelected())
      {
        text = new AnnotationFile().printCSVAnnotations(annotations);
      }
      else
      {
        if (wholeView)
        {
          text = new AnnotationFile().printAnnotationsForView(ap.av);
        }
        else
        {
          text = new AnnotationFile().printAnnotations(annotations, null,
                  null);
        }
      }
    }
    return text;
  }

  public void toTextbox_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();

    try
    {
      String text = getFileContents();
      cap.setText(text);
      Desktop.addInternalFrame(
              cap,
              (features ? MessageManager.formatMessage(
                      "label.features_for_params",
                      new String[] { ap.alignFrame.getTitle() })
                      : MessageManager.formatMessage(
                              "label.annotations_for_params",
                              new String[] { ap.alignFrame.getTitle() })),
              600, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning((features ? MessageManager.formatMessage(
              "label.generating_features_for_params",
              new String[] { ap.alignFrame.getTitle() })
              : MessageManager.formatMessage(
                      "label.generating_annotations_for_params",
                      new String[] { ap.alignFrame.getTitle() })), oom);
      cap.dispose();
    }

    close_actionPerformed(null);
  }

  public void close_actionPerformed(ActionEvent e)
  {
    try
    {
      frame.setClosed(true);
    } catch (java.beans.PropertyVetoException ex)
    {
    }
  }

  private void jbInit() throws Exception
  {
    this.setLayout(new BorderLayout());

    toFile.setText(MessageManager.getString("label.to_file"));
    toFile.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        toFile_actionPerformed(e);
      }
    });
    toTextbox.setText(MessageManager.getString("label.to_textbox"));
    toTextbox.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        toTextbox_actionPerformed(e);
      }
    });
    close.setText(MessageManager.getString("action.close"));
    close.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        close_actionPerformed(e);
      }
    });
    jalviewFormat.setOpaque(false);
    jalviewFormat.setSelected(true);
    jalviewFormat.setText("Jalview");
    GFFFormat.setOpaque(false);
    GFFFormat.setText("GFF");
    CSVFormat.setOpaque(false);
    CSVFormat.setText(MessageManager.getString("label.csv_spreadsheet"));
    jLabel1.setHorizontalAlignment(SwingConstants.TRAILING);
    jLabel1.setText(MessageManager.getString("action.format") + " ");
    this.setBackground(Color.white);
    jPanel3.setBorder(BorderFactory.createEtchedBorder());
    jPanel3.setOpaque(false);
    jPanel1.setOpaque(false);
    jPanel1.add(toFile);
    jPanel1.add(toTextbox);
    jPanel1.add(close);
    jPanel3.add(jLabel1);
    jPanel3.add(jalviewFormat);
    jPanel3.add(GFFFormat);
    jPanel3.add(CSVFormat);
    buttonGroup.add(jalviewFormat);
    buttonGroup.add(GFFFormat);
    buttonGroup.add(CSVFormat);
    this.add(jPanel3, BorderLayout.CENTER);
    this.add(jPanel1, BorderLayout.SOUTH);
  }

  JPanel jPanel1 = new JPanel();

  JButton toFile = new JButton();

  JButton toTextbox = new JButton();

  JButton close = new JButton();

  ButtonGroup buttonGroup = new ButtonGroup();

  JRadioButton jalviewFormat = new JRadioButton();

  JRadioButton GFFFormat = new JRadioButton();

  JRadioButton CSVFormat = new JRadioButton();

  JLabel jLabel1 = new JLabel();

  JPanel jPanel3 = new JPanel();

  FlowLayout flowLayout1 = new FlowLayout();
}
