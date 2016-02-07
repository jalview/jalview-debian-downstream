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

import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceI;
import jalview.jbgui.GPCAPanel;
import jalview.schemes.ResidueProperties;
import jalview.util.MessageManager;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.PCAModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PCAPanel extends GPCAPanel implements Runnable,
        IProgressIndicator
{

  private IProgressIndicator progressBar;

  RotatableCanvas rc;

  AlignmentPanel ap;

  AlignmentViewport av;

  PCAModel pcaModel;

  int top = 0;

  /**
   * Creates a new PCAPanel object.
   * 
   * @param av
   *          DOCUMENT ME!
   * @param s
   *          DOCUMENT ME!
   */
  public PCAPanel(AlignmentPanel ap)
  {
    this.av = ap.av;
    this.ap = ap;

    progressBar = new ProgressBar(statusPanel, statusBar);

    boolean sameLength = true;
    boolean selected = av.getSelectionGroup() != null
            && av.getSelectionGroup().getSize() > 0;
    AlignmentView seqstrings = av.getAlignmentView(selected);
    boolean nucleotide = av.getAlignment().isNucleotide();
    SequenceI[] seqs;
    if (!selected)
    {
      seqs = av.getAlignment().getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.getAlignment());
    }
    SeqCigar sq[] = seqstrings.getSequences();
    int length = sq[0].getWidth();

    for (int i = 0; i < seqs.length; i++)
    {
      if (sq[i].getWidth() != length)
      {
        sameLength = false;
        break;
      }
    }

    if (!sameLength)
    {
      JOptionPane.showMessageDialog(Desktop.desktop,
              MessageManager.getString("label.pca_sequences_not_aligned"),
              MessageManager.getString("label.sequences_not_aligned"),
              JOptionPane.WARNING_MESSAGE);

      return;
    }
    pcaModel = new PCAModel(seqstrings, seqs, nucleotide);
    PaintRefresher.Register(this, av.getSequenceSetId());

    rc = new RotatableCanvas(ap);
    this.getContentPane().add(rc, BorderLayout.CENTER);
    Thread worker = new Thread(this);
    worker.start();
  }

  @Override
  protected void scoreMatrix_menuSelected()
  {
    scoreMatrixMenu.removeAll();
    for (final String sm : ResidueProperties.scoreMatrices.keySet())
    {
      if (ResidueProperties.getScoreMatrix(sm) != null)
      {
        // create an entry for this score matrix for use in PCA
        JCheckBoxMenuItem jm = new JCheckBoxMenuItem();
        jm.setText(MessageManager
                .getStringOrReturn("label.score_model", sm));
        jm.setSelected(pcaModel.getScore_matrix().equals(sm));
        if ((ResidueProperties.scoreMatrices.get(sm).isDNA() && ResidueProperties.scoreMatrices
                .get(sm).isProtein())
                || pcaModel.isNucleotide() == ResidueProperties.scoreMatrices
                        .get(sm).isDNA())
        {
          final PCAPanel us = this;
          jm.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              if (!pcaModel.getScore_matrix().equals(sm))
              {
                pcaModel.setScore_matrix(sm);
                Thread worker = new Thread(us);
                worker.start();
              }
            }
          });
          scoreMatrixMenu.add(jm);
        }
      }
    }
  }

  public void bgcolour_actionPerformed(ActionEvent e)
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_backgroud_colour"),
            rc.bgColour);

    if (col != null)
    {
      rc.bgColour = col;
    }
    rc.repaint();
  }

  /**
   * DOCUMENT ME!
   */
  public void run()
  {
    long progId = System.currentTimeMillis();
    IProgressIndicator progress = this;
    String message = MessageManager.getString("label.pca_recalculating");
    if (getParent() == null)
    {
      progress = ap.alignFrame;
      message = MessageManager.getString("label.pca_calculating");
    }
    progress.setProgressBar(message, progId);
    try
    {
      calcSettings.setEnabled(false);
      pcaModel.run();
      // ////////////////
      xCombobox.setSelectedIndex(0);
      yCombobox.setSelectedIndex(1);
      zCombobox.setSelectedIndex(2);

      pcaModel.updateRc(rc);
      // rc.invalidate();
      nuclSetting.setSelected(pcaModel.isNucleotide());
      protSetting.setSelected(!pcaModel.isNucleotide());
      jvVersionSetting.setSelected(pcaModel.isJvCalcMode());
      top = pcaModel.getTop();

    } catch (OutOfMemoryError er)
    {
      new OOMWarning("calculating PCA", er);
      return;
    } finally
    {
      progress.setProgressBar("", progId);
    }
    calcSettings.setEnabled(true);
    repaint();
    if (getParent() == null)
    {
      addKeyListener(rc);
      Desktop.addInternalFrame(this, MessageManager
              .getString("label.principal_component_analysis"), 475, 450);
    }
  }

  @Override
  protected void nuclSetting_actionPerfomed(ActionEvent arg0)
  {
    if (!pcaModel.isNucleotide())
    {
      pcaModel.setNucleotide(true);
      pcaModel.setScore_matrix("DNA");
      Thread worker = new Thread(this);
      worker.start();
    }

  }

  @Override
  protected void protSetting_actionPerfomed(ActionEvent arg0)
  {

    if (pcaModel.isNucleotide())
    {
      pcaModel.setNucleotide(false);
      pcaModel.setScore_matrix("BLOSUM62");
      Thread worker = new Thread(this);
      worker.start();
    }
  }

  @Override
  protected void jvVersionSetting_actionPerfomed(ActionEvent arg0)
  {
    pcaModel.setJvCalcMode(jvVersionSetting.isSelected());
    Thread worker = new Thread(this);
    worker.start();
  }

  /**
   * DOCUMENT ME!
   */
  void doDimensionChange()
  {
    if (top == 0)
    {
      return;
    }

    int dim1 = top - xCombobox.getSelectedIndex();
    int dim2 = top - yCombobox.getSelectedIndex();
    int dim3 = top - zCombobox.getSelectedIndex();
    pcaModel.updateRcView(dim1, dim2, dim3);
    rc.img = null;
    rc.rotmat.setIdentity();
    rc.initAxes();
    rc.paint(rc.getGraphics());
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void xCombobox_actionPerformed(ActionEvent e)
  {
    doDimensionChange();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void yCombobox_actionPerformed(ActionEvent e)
  {
    doDimensionChange();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  protected void zCombobox_actionPerformed(ActionEvent e)
  {
    doDimensionChange();
  }

  public void outputValues_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(pcaModel.getDetails());
      Desktop.addInternalFrame(cap,
              MessageManager.getString("label.pca_details"), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("opening PCA details", oom);
      cap.dispose();
    }
  }

  public void showLabels_actionPerformed(ActionEvent e)
  {
    rc.showLabels(showLabels.getState());
  }

  public void print_actionPerformed(ActionEvent e)
  {
    PCAPrinter printer = new PCAPrinter();
    printer.start();
  }

  public void originalSeqData_actionPerformed(ActionEvent e)
  {
    // this was cut'n'pasted from the equivalent TreePanel method - we should
    // make this an abstract function of all jalview analysis windows
    if (pcaModel.getSeqtrings() == null)
    {
      jalview.bin.Cache.log
              .info("Unexpected call to originalSeqData_actionPerformed - should have hidden this menu action.");
      return;
    }
    // decide if av alignment is sufficiently different to original data to
    // warrant a new window to be created
    // create new alignmnt window with hidden regions (unhiding hidden regions
    // yields unaligned seqs)
    // or create a selection box around columns in alignment view
    // test Alignment(SeqCigar[])
    char gc = '-';
    try
    {
      // we try to get the associated view's gap character
      // but this may fail if the view was closed...
      gc = av.getGapCharacter();
    } catch (Exception ex)
    {
    }
    ;
    Object[] alAndColsel = pcaModel.getSeqtrings()
            .getAlignmentAndColumnSelection(gc);

    if (alAndColsel != null && alAndColsel[0] != null)
    {
      // AlignmentOrder origorder = new AlignmentOrder(alAndColsel[0]);

      Alignment al = new Alignment((SequenceI[]) alAndColsel[0]);
      Alignment dataset = (av != null && av.getAlignment() != null) ? av
              .getAlignment().getDataset() : null;
      if (dataset != null)
      {
        al.setDataset(dataset);
      }

      if (true)
      {
        // make a new frame!
        AlignFrame af = new AlignFrame(al,
                (ColumnSelection) alAndColsel[1], AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);

        // >>>This is a fix for the moment, until a better solution is
        // found!!<<<
        // af.getFeatureRenderer().transferSettings(alignFrame.getFeatureRenderer());

        // af.addSortByOrderMenuItem(ServiceName + " Ordering",
        // msaorder);

        Desktop.addInternalFrame(af, MessageManager.formatMessage(
                "label.original_data_for_params",
                new String[] { this.title }), AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
      }
    }
    /*
     * CutAndPasteTransfer cap = new CutAndPasteTransfer(); for (int i = 0; i <
     * seqs.length; i++) { cap.appendText(new jalview.util.Format("%-" + 15 +
     * "s").form( seqs[i].getName())); cap.appendText(" " + seqstrings[i] +
     * "\n"); }
     * 
     * Desktop.addInternalFrame(cap, "Original Data", 400, 400);
     */
  }

  class PCAPrinter extends Thread implements Printable
  {
    public void run()
    {
      PrinterJob printJob = PrinterJob.getPrinterJob();
      PageFormat pf = printJob.pageDialog(printJob.defaultPage());

      printJob.setPrintable(this, pf);

      if (printJob.printDialog())
      {
        try
        {
          printJob.print();
        } catch (Exception PrintException)
        {
          PrintException.printStackTrace();
        }
      }
    }

    public int print(Graphics pg, PageFormat pf, int pi)
            throws PrinterException
    {
      pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

      rc.drawBackground(pg, rc.bgColour);
      rc.drawScene(pg);
      if (rc.drawAxes == true)
      {
        rc.drawAxes(pg);
      }

      if (pi == 0)
      {
        return Printable.PAGE_EXISTS;
      }
      else
      {
        return Printable.NO_SUCH_PAGE;
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void eps_actionPerformed(ActionEvent e)
  {
    makePCAImage(jalview.util.ImageMaker.TYPE.EPS);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void png_actionPerformed(ActionEvent e)
  {
    makePCAImage(jalview.util.ImageMaker.TYPE.PNG);
  }

  void makePCAImage(jalview.util.ImageMaker.TYPE type)
  {
    int width = rc.getWidth();
    int height = rc.getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.TYPE.PNG)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.PNG, "Make PNG image from PCA",
              width, height, null, null);
    }
    else if (type == jalview.util.ImageMaker.TYPE.EPS)
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.EPS, "Make EPS file from PCA",
              width, height, null, this.getTitle());
    }
    else
    {
      im = new jalview.util.ImageMaker(this,
              jalview.util.ImageMaker.TYPE.SVG, "Make SVG file from PCA",
              width, height, null, this.getTitle());

    }

    if (im.getGraphics() != null)
    {
      rc.drawBackground(im.getGraphics(), Color.black);
      rc.drawScene(im.getGraphics());
      if (rc.drawAxes == true)
      {
        rc.drawAxes(im.getGraphics());
      }
      im.writeImage();
    }
  }

  public void viewMenu_menuSelected()
  {
    buildAssociatedViewMenu();
  }

  void buildAssociatedViewMenu()
  {
    AlignmentPanel[] aps = PaintRefresher.getAssociatedPanels(av
            .getSequenceSetId());
    if (aps.length == 1 && rc.av == aps[0].av)
    {
      associateViewsMenu.setVisible(false);
      return;
    }

    associateViewsMenu.setVisible(true);

    if ((viewMenu.getItem(viewMenu.getItemCount() - 2) instanceof JMenuItem))
    {
      viewMenu.insertSeparator(viewMenu.getItemCount() - 1);
    }

    associateViewsMenu.removeAll();

    JRadioButtonMenuItem item;
    ButtonGroup buttonGroup = new ButtonGroup();
    int i, iSize = aps.length;
    final PCAPanel thisPCAPanel = this;
    for (i = 0; i < iSize; i++)
    {
      final AlignmentPanel ap = aps[i];
      item = new JRadioButtonMenuItem(ap.av.viewName, ap.av == rc.av);
      buttonGroup.add(item);
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent evt)
        {
          rc.applyToAllViews = false;
          rc.av = ap.av;
          rc.ap = ap;
          PaintRefresher.Register(thisPCAPanel, ap.av.getSequenceSetId());
        }
      });

      associateViewsMenu.add(item);
    }

    final JRadioButtonMenuItem itemf = new JRadioButtonMenuItem("All Views");

    buttonGroup.add(itemf);

    itemf.setSelected(rc.applyToAllViews);
    itemf.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent evt)
      {
        rc.applyToAllViews = itemf.isSelected();
      }
    });
    associateViewsMenu.add(itemf);

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPCAPanel#outputPoints_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  protected void outputPoints_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(pcaModel.getPointsasCsv(false,
              xCombobox.getSelectedIndex(), yCombobox.getSelectedIndex(),
              zCombobox.getSelectedIndex()));
      Desktop.addInternalFrame(cap, MessageManager.formatMessage(
              "label.points_for_params", new String[] { this.getTitle() }),
              500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting PCA points", oom);
      cap.dispose();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GPCAPanel#outputProjPoints_actionPerformed(java.awt.event
   * .ActionEvent)
   */
  protected void outputProjPoints_actionPerformed(ActionEvent e)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    try
    {
      cap.setText(pcaModel.getPointsasCsv(true,
              xCombobox.getSelectedIndex(), yCombobox.getSelectedIndex(),
              zCombobox.getSelectedIndex()));
      Desktop.addInternalFrame(cap, MessageManager.formatMessage(
              "label.transformed_points_for_params",
              new String[] { this.getTitle() }), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting transformed PCA points", oom);
      cap.dispose();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#setProgressBar(java.lang.String, long)
   */
  @Override
  public void setProgressBar(String message, long id)
  {
    progressBar.setProgressBar(message, id);
    // if (progressBars == null)
    // {
    // progressBars = new Hashtable();
    // progressBarHandlers = new Hashtable();
    // }
    //
    // JPanel progressPanel;
    // Long lId = new Long(id);
    // GridLayout layout = (GridLayout) statusPanel.getLayout();
    // if (progressBars.get(lId) != null)
    // {
    // progressPanel = (JPanel) progressBars.get(new Long(id));
    // statusPanel.remove(progressPanel);
    // progressBars.remove(lId);
    // progressPanel = null;
    // if (message != null)
    // {
    // statusBar.setText(message);
    // }
    // if (progressBarHandlers.contains(lId))
    // {
    // progressBarHandlers.remove(lId);
    // }
    // layout.setRows(layout.getRows() - 1);
    // }
    // else
    // {
    // progressPanel = new JPanel(new BorderLayout(10, 5));
    //
    // JProgressBar progressBar = new JProgressBar();
    // progressBar.setIndeterminate(true);
    //
    // progressPanel.add(new JLabel(message), BorderLayout.WEST);
    // progressPanel.add(progressBar, BorderLayout.CENTER);
    //
    // layout.setRows(layout.getRows() + 1);
    // statusPanel.add(progressPanel);
    //
    // progressBars.put(lId, progressPanel);
    // }
    // // update GUI
    // // setMenusForViewport();
    // validate();
  }

  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    progressBar.registerHandler(id, handler);
    // if (progressBarHandlers == null || !progressBars.contains(new Long(id)))
    // {
    // throw new
    // Error(MessageManager.getString("error.call_setprogressbar_before_registering_handler"));
    // }
    // progressBarHandlers.put(new Long(id), handler);
    // final JPanel progressPanel = (JPanel) progressBars.get(new Long(id));
    // if (handler.canCancel())
    // {
    // JButton cancel = new JButton(
    // MessageManager.getString("action.cancel"));
    // final IProgressIndicator us = this;
    // cancel.addActionListener(new ActionListener()
    // {
    //
    // @Override
    // public void actionPerformed(ActionEvent e)
    // {
    // handler.cancelActivity(id);
    // us.setProgressBar(MessageManager.formatMessage("label.cancelled_params",
    // new String[]{((JLabel) progressPanel.getComponent(0)).getText()}), id);
    // }
    // });
    // progressPanel.add(cancel, BorderLayout.EAST);
    // }
  }

  /**
   * 
   * @return true if any progress bars are still active
   */
  @Override
  public boolean operationInProgress()
  {
    return progressBar.operationInProgress();
  }

  @Override
  protected void resetButton_actionPerformed(ActionEvent e)
  {
    int t = top;
    top = 0; // ugly - prevents dimensionChanged events from being processed
    xCombobox.setSelectedIndex(0);
    yCombobox.setSelectedIndex(1);
    top = t;
    zCombobox.setSelectedIndex(2);
  }
}
