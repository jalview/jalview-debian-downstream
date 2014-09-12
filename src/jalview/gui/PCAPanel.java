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
package jalview.gui;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import javax.swing.*;

import jalview.analysis.*;
import jalview.datamodel.*;
import jalview.jbgui.*;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class PCAPanel extends GPCAPanel implements Runnable
{

  PCA pca;

  int top;

  RotatableCanvas rc;

  AlignmentPanel ap;

  AlignViewport av;

  AlignmentView seqstrings;

  SequenceI[] seqs;

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

    boolean sameLength = true;

    seqstrings = av.getAlignmentView(av.getSelectionGroup() != null);
    if (av.getSelectionGroup() == null)
    {
      seqs = av.alignment.getSequencesArray();
    }
    else
    {
      seqs = av.getSelectionGroup().getSequencesInOrder(av.alignment);
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
      JOptionPane
              .showMessageDialog(
                      Desktop.desktop,
                      "The sequences must be aligned before calculating PCA.\n"
                              + "Try using the Pad function in the edit menu,\n"
                              + "or one of the multiple sequence alignment web services.",
                      "Sequences not aligned", JOptionPane.WARNING_MESSAGE);

      return;
    }

    Desktop.addInternalFrame(this, "Principal component analysis", 400, 400);

    PaintRefresher.Register(this, av.getSequenceSetId());

    rc = new RotatableCanvas(ap);
    this.getContentPane().add(rc, BorderLayout.CENTER);
    Thread worker = new Thread(this);
    worker.start();
  }

  public void bgcolour_actionPerformed(ActionEvent e)
  {
    Color col = JColorChooser.showDialog(this, "Select Background Colour",
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
    try
    {
      pca = new PCA(seqstrings.getSequenceStrings(' '));
      pca.run();

      // Now find the component coordinates
      int ii = 0;

      while ((ii < seqs.length) && (seqs[ii] != null))
      {
        ii++;
      }

      double[][] comps = new double[ii][ii];

      for (int i = 0; i < ii; i++)
      {
        if (pca.getEigenvalue(i) > 1e-4)
        {
          comps[i] = pca.component(i);
        }
      }

      // ////////////////
      xCombobox.setSelectedIndex(0);
      yCombobox.setSelectedIndex(1);
      zCombobox.setSelectedIndex(2);

      top = pca.getM().rows - 1;

      Vector points = new Vector();
      float[][] scores = pca.getComponents(top - 1, top - 2, top - 3, 100);

      for (int i = 0; i < pca.getM().rows; i++)
      {
        SequencePoint sp = new SequencePoint(seqs[i], scores[i]);
        points.addElement(sp);
      }

      rc.setPoints(points, pca.getM().rows);
      rc.repaint();

      addKeyListener(rc);

    } catch (OutOfMemoryError er)
    {
      new OOMWarning("calculating PCA", er);

    }

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

    float[][] scores = pca.getComponents(dim1, dim2, dim3, 100);

    for (int i = 0; i < pca.getM().rows; i++)
    {
      ((SequencePoint) rc.points.elementAt(i)).coord = scores[i];
    }

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
      cap.setText(pca.getDetails());
      Desktop.addInternalFrame(cap, "PCA details", 500, 500);
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
    if (seqstrings == null)
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
    Object[] alAndColsel = seqstrings.getAlignmentAndColumnSelection(gc);

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

        Desktop.addInternalFrame(af, "Original Data for " + this.title,
                AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
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
    makePCAImage(jalview.util.ImageMaker.EPS);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void png_actionPerformed(ActionEvent e)
  {
    makePCAImage(jalview.util.ImageMaker.PNG);
  }

  void makePCAImage(int type)
  {
    int width = rc.getWidth();
    int height = rc.getHeight();

    jalview.util.ImageMaker im;

    if (type == jalview.util.ImageMaker.PNG)
    {
      im = new jalview.util.ImageMaker(this, jalview.util.ImageMaker.PNG,
              "Make PNG image from PCA", width, height, null, null);
    }
    else
    {
      im = new jalview.util.ImageMaker(this, jalview.util.ImageMaker.EPS,
              "Make EPS file from PCA", width, height, null,
              this.getTitle());
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
      cap.setText(getPointsasCsv(false));
      Desktop.addInternalFrame(cap, "Points for " + getTitle(), 500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting PCA points", oom);
      cap.dispose();
    }
  }

  private String getPointsasCsv(boolean transformed)
  {
    StringBuffer csv = new StringBuffer();
    csv.append("\"Sequence\"");
    if (transformed)
    {
      csv.append(",");
      csv.append(xCombobox.getSelectedIndex());
      csv.append(",");
      csv.append(yCombobox.getSelectedIndex());
      csv.append(",");
      csv.append(zCombobox.getSelectedIndex());
    }
    else
    {
      for (int d = 1, dmax = pca.component(1).length; d <= dmax; d++)
      {
        csv.append("," + d);
      }
    }
    csv.append("\n");
    for (int s = 0; s < seqs.length; s++)
    {
      csv.append("\"" + seqs[s].getName() + "\"");
      double fl[];
      if (!transformed)
      {
        // output pca in correct order
        fl = pca.component(s);
        for (int d = fl.length - 1; d >= 0; d--)
        {
          csv.append(",");
          csv.append(fl[d]);
        }
      }
      else
      {
        // output current x,y,z coords for points
        fl = rc.getPointPosition(s);
        for (int d = 0; d < fl.length; d++)
        {
          csv.append(",");
          csv.append(fl[d]);
        }
      }
      csv.append("\n");
    }
    return csv.toString();
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
      cap.setText(getPointsasCsv(true));
      Desktop.addInternalFrame(cap, "Transformed points for " + getTitle(),
              500, 500);
    } catch (OutOfMemoryError oom)
    {
      new OOMWarning("exporting transformed PCA points", oom);
      cap.dispose();
    }
  }

}
