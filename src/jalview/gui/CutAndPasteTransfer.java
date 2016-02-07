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

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.ComplexAlignFile;
import jalview.api.FeaturesDisplayedI;
import jalview.bin.Jalview;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceI;
import jalview.io.AppletFormatAdapter;
import jalview.io.FileParse;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GCutAndPasteTransfer;
import jalview.schemes.ColourSchemeI;
import jalview.util.MessageManager;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

/**
 * Cut'n'paste files into the desktop See JAL-1105
 * 
 * @author $author$
 * @version $Revision$
 */
public class CutAndPasteTransfer extends GCutAndPasteTransfer
{

  AlignmentViewPanel alignpanel;

  AlignViewportI viewport;

  FileParse source = null;

  public CutAndPasteTransfer()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        textarea.requestFocus();
      }
    });

  }

  /**
   * DOCUMENT ME!
   */
  public void setForInput(AlignmentViewPanel viewpanel)
  {
    this.alignpanel = viewpanel;
    if (alignpanel != null)
    {
      this.viewport = alignpanel.getAlignViewport();
    }
    if (viewport != null)
    {
      ok.setText(MessageManager.getString("action.add"));
    }

    getContentPane().add(inputButtonPanel, java.awt.BorderLayout.SOUTH);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getText()
  {
    return textarea.getText();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param text
   *          DOCUMENT ME!
   */
  public void setText(String text)
  {
    textarea.setText(text);
  }

  public void appendText(String text)
  {
    textarea.append(text);
  }

  public void save_actionPerformed(ActionEvent e)
  {
    JalviewFileChooser chooser = new JalviewFileChooser(
            jalview.bin.Cache.getProperty("LAST_DIRECTORY"));

    chooser.setAcceptAllFileFilterUsed(false);
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager
            .getString("label.save_text_to_file"));
    chooser.setToolTipText(MessageManager.getString("action.save"));

    int value = chooser.showSaveDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      try
      {
        java.io.PrintWriter out = new java.io.PrintWriter(
                new java.io.FileWriter(chooser.getSelectedFile()));

        out.print(getText());
        out.close();
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }

    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void copyItem_actionPerformed(ActionEvent e)
  {
    textarea.getSelectedText();
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    c.setContents(new StringSelection(textarea.getSelectedText()), null);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void pasteMenu_actionPerformed(ActionEvent e)
  {
    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
    Transferable contents = c.getContents(this);

    if (contents == null)
    {
      return;
    }

    try
    {
      textarea.append((String) contents
              .getTransferData(DataFlavor.stringFlavor));
    } catch (Exception ex)
    {
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void ok_actionPerformed(ActionEvent e)
  {
    String text = getText();
    if (text.trim().length() < 1)
    {
      return;
    }

    String format = new IdentifyFile().Identify(text, "Paste");
    if (format == null || format.equalsIgnoreCase("EMPTY DATA FILE"))
    {
      System.err.println(MessageManager
              .getString("label.couldnt_read_data"));
      if (!Jalview.isHeadlessMode())
      {
        javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                AppletFormatAdapter.SUPPORTED_FORMATS,
                MessageManager.getString("label.couldnt_read_data"),
                JOptionPane.WARNING_MESSAGE);
      }
      return;
    }

    // TODO: identify feature, annotation or tree file and parse appropriately.
    AlignmentI al = null;

    if (FormatAdapter.isValidFormat(format))
    {
      try
      {
        FormatAdapter fa = new FormatAdapter(alignpanel);
        al = fa.readFile(getText(), "Paste", format);
        source = fa.getAlignFile();

      } catch (java.io.IOException ex)
      {
        JOptionPane.showInternalMessageDialog(Desktop.desktop,
                MessageManager.formatMessage(
                        "label.couldnt_read_pasted_text",
                        new String[] { ex.toString() }), MessageManager
                        .getString("label.error_parsing_text"),
                JOptionPane.WARNING_MESSAGE);
      }
    }

    if (al != null && al.hasValidSequence())
    {
      String title = MessageManager.formatMessage(
              "label.input_cut_paste_params", new String[] { format });
      if (viewport != null)
      {
        ((AlignViewport) viewport).addAlignment(al, title);
      }
      else
      {

        AlignFrame af;
        if (source instanceof ComplexAlignFile)
        {
          ColumnSelection colSel = ((ComplexAlignFile) source)
                  .getColumnSelection();
          SequenceI[] hiddenSeqs = ((ComplexAlignFile) source)
                  .getHiddenSequences();
          boolean showSeqFeatures = ((ComplexAlignFile) source)
                  .isShowSeqFeatures();
          ColourSchemeI cs = ((ComplexAlignFile) source).getColourScheme();
          FeaturesDisplayedI fd = ((ComplexAlignFile) source)
                  .getDisplayedFeatures();
          af = new AlignFrame(al, hiddenSeqs, colSel,
                  AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
          af.getViewport().setShowSequenceFeatures(showSeqFeatures);
          af.getViewport().setFeaturesDisplayed(fd);
          af.changeColour(cs);
        }
        else
        {
          af = new AlignFrame(al, AlignFrame.DEFAULT_WIDTH,
                  AlignFrame.DEFAULT_HEIGHT);
        }

        af.currentFileFormat = format;
        Desktop.addInternalFrame(af, title, AlignFrame.DEFAULT_WIDTH,
                AlignFrame.DEFAULT_HEIGHT);
        af.statusBar.setText(MessageManager
                .getString("label.successfully_pasted_alignment_file"));

        try
        {
          af.setMaximum(jalview.bin.Cache.getDefault("SHOW_FULLSCREEN",
                  false));
        } catch (Exception ex)
        {
        }
      }
    }
    else
    {
      System.err.println(MessageManager
              .getString("label.couldnt_read_data"));
      if (!Jalview.isHeadlessMode())
      {
        javax.swing.JOptionPane.showInternalMessageDialog(Desktop.desktop,
                AppletFormatAdapter.SUPPORTED_FORMATS,
                MessageManager.getString("label.couldnt_read_data"),
                JOptionPane.WARNING_MESSAGE);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void cancel_actionPerformed(ActionEvent e)
  {
    try
    {
      this.setClosed(true);
    } catch (Exception ex)
    {
    }
  }

  public void textarea_mousePressed(MouseEvent e)
  {
    if (SwingUtilities.isRightMouseButton(e))
    {
      JPopupMenu popup = new JPopupMenu(
              MessageManager.getString("action.edit"));
      JMenuItem item = new JMenuItem(
              MessageManager.getString("action.copy"));
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          copyItem_actionPerformed(e);
        }
      });
      popup.add(item);
      item = new JMenuItem(MessageManager.getString("action.paste"));
      item.addActionListener(new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          pasteMenu_actionPerformed(e);
        }
      });
      popup.add(item);
      popup.show(this, e.getX() + 10, e.getY() + textarea.getY() + 40);

    }
  }

}
