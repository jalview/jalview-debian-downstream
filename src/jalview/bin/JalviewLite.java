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
package jalview.bin;

import jalview.analysis.AlignmentUtils;
import jalview.api.StructureSelectionManagerProvider;
import jalview.appletgui.AlignFrame;
import jalview.appletgui.AlignViewport;
import jalview.appletgui.EmbmenuFrame;
import jalview.appletgui.FeatureSettings;
import jalview.appletgui.SplitFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.AnnotationFile;
import jalview.io.AppletFormatAdapter;
import jalview.io.FileParse;
import jalview.io.IdentifyFile;
import jalview.io.JPredFile;
import jalview.io.JnetAnnotationMaker;
import jalview.io.NewickFile;
import jalview.javascript.JSFunctionExec;
import jalview.javascript.JalviewLiteJsApi;
import jalview.javascript.JsCallBack;
import jalview.structure.SelectionListener;
import jalview.structure.StructureSelectionManager;
import jalview.util.MessageManager;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import netscape.javascript.JSObject;

/**
 * Jalview Applet. Runs in Java 1.18 runtime
 * 
 * @author $author$
 * @version $Revision: 1.92 $
 */
public class JalviewLite extends Applet implements
        StructureSelectionManagerProvider, JalviewLiteJsApi
{

  private static final String TRUE = "true";

  private static final String FALSE = "false";

  public StructureSelectionManager getStructureSelectionManager()
  {
    return StructureSelectionManager.getStructureSelectionManager(this);
  }

  // /////////////////////////////////////////
  // The following public methods maybe called
  // externally, eg via javascript in HTML page
  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getSelectedSequences()
   */
  public String getSelectedSequences()
  {
    return getSelectedSequencesFrom(getDefaultTargetFrame());
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getSelectedSequences(java.lang.String)
   */
  public String getSelectedSequences(String sep)
  {
    return getSelectedSequencesFrom(getDefaultTargetFrame(), sep);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getSelectedSequencesFrom(jalview.appletgui
   * .AlignFrame)
   */
  public String getSelectedSequencesFrom(AlignFrame alf)
  {
    return getSelectedSequencesFrom(alf, separator); // ""+0x00AC);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getSelectedSequencesFrom(jalview.appletgui
   * .AlignFrame, java.lang.String)
   */
  public String getSelectedSequencesFrom(AlignFrame alf, String sep)
  {
    StringBuffer result = new StringBuffer("");
    if (sep == null || sep.length() == 0)
    {
      sep = separator; // "+0x00AC;
    }
    if (alf.viewport.getSelectionGroup() != null)
    {
      SequenceI[] seqs = alf.viewport.getSelectionGroup()
              .getSequencesInOrder(alf.viewport.getAlignment());

      for (int i = 0; i < seqs.length; i++)
      {
        result.append(seqs[i].getName());
        result.append(sep);
      }
    }

    return result.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#highlight(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void highlight(String sequenceId, String position,
          String alignedPosition)
  {
    highlightIn(getDefaultTargetFrame(), sequenceId, position,
            alignedPosition);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#highlightIn(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public void highlightIn(final AlignFrame alf, final String sequenceId,
          final String position, final String alignedPosition)
  {
    // TODO: could try to highlight in all alignments if alf==null
    jalview.analysis.SequenceIdMatcher matcher = new jalview.analysis.SequenceIdMatcher(
            alf.viewport.getAlignment().getSequencesArray());
    final SequenceI sq = matcher.findIdMatch(sequenceId);
    if (sq != null)
    {
      int apos = -1;
      try
      {
        apos = new Integer(position).intValue();
        apos--;
      } catch (NumberFormatException ex)
      {
        return;
      }
      final StructureSelectionManagerProvider me = this;
      final int pos = apos;
      // use vamsas listener to broadcast to all listeners in scope
      if (alignedPosition != null
              && (alignedPosition.trim().length() == 0 || alignedPosition
                      .toLowerCase().indexOf("false") > -1))
      {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            StructureSelectionManager.getStructureSelectionManager(me)
                    .mouseOverVamsasSequence(sq, sq.findIndex(pos), null);
          }
        });
      }
      else
      {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            StructureSelectionManager.getStructureSelectionManager(me)
                    .mouseOverVamsasSequence(sq, pos, null);
          }
        });
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#select(java.lang.String,
   * java.lang.String)
   */
  public void select(String sequenceIds, String columns)
  {
    selectIn(getDefaultTargetFrame(), sequenceIds, columns, separator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#select(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void select(String sequenceIds, String columns, String sep)
  {
    selectIn(getDefaultTargetFrame(), sequenceIds, columns, sep);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#selectIn(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String)
   */
  public void selectIn(AlignFrame alf, String sequenceIds, String columns)
  {
    selectIn(alf, sequenceIds, columns, separator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#selectIn(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public void selectIn(final AlignFrame alf, String sequenceIds,
          String columns, String sep)
  {
    if (sep == null || sep.length() == 0)
    {
      sep = separator;
    }
    else
    {
      if (debug)
      {
        System.err.println("Selecting region using separator string '"
                + separator + "'");
      }
    }
    // deparse fields
    String[] ids = separatorListToArray(sequenceIds, sep);
    String[] cols = separatorListToArray(columns, sep);
    final SequenceGroup sel = new SequenceGroup();
    final ColumnSelection csel = new ColumnSelection();
    AlignmentI al = alf.viewport.getAlignment();
    jalview.analysis.SequenceIdMatcher matcher = new jalview.analysis.SequenceIdMatcher(
            alf.viewport.getAlignment().getSequencesArray());
    int start = 0, end = al.getWidth(), alw = al.getWidth();
    boolean seqsfound = true;
    if (ids != null && ids.length > 0)
    {
      seqsfound = false;
      for (int i = 0; i < ids.length; i++)
      {
        if (ids[i].trim().length() == 0)
        {
          continue;
        }
        SequenceI sq = matcher.findIdMatch(ids[i]);
        if (sq != null)
        {
          seqsfound = true;
          sel.addSequence(sq, false);
        }
      }
    }
    boolean inseqpos = false;
    if (cols != null && cols.length > 0)
    {
      boolean seset = false;
      for (int i = 0; i < cols.length; i++)
      {
        String cl = cols[i].trim();
        if (cl.length() == 0)
        {
          continue;
        }
        int p;
        if ((p = cl.indexOf("-")) > -1)
        {
          int from = -1, to = -1;
          try
          {
            from = new Integer(cl.substring(0, p)).intValue();
            from--;
          } catch (NumberFormatException ex)
          {
            System.err
                    .println("ERROR: Couldn't parse first integer in range element column selection string '"
                            + cl + "' - format is 'from-to'");
            return;
          }
          try
          {
            to = new Integer(cl.substring(p + 1)).intValue();
            to--;
          } catch (NumberFormatException ex)
          {
            System.err
                    .println("ERROR: Couldn't parse second integer in range element column selection string '"
                            + cl + "' - format is 'from-to'");
            return;
          }
          if (from >= 0 && to >= 0)
          {
            // valid range
            if (from < to)
            {
              int t = to;
              to = from;
              to = t;
            }
            if (!seset)
            {
              start = from;
              end = to;
              seset = true;
            }
            else
            {
              // comment to prevent range extension
              if (start > from)
              {
                start = from;
              }
              if (end < to)
              {
                end = to;
              }
            }
            for (int r = from; r <= to; r++)
            {
              if (r >= 0 && r < alw)
              {
                csel.addElement(r);
              }
            }
            if (debug)
            {
              System.err.println("Range '" + cl + "' deparsed as [" + from
                      + "," + to + "]");
            }
          }
          else
          {
            System.err.println("ERROR: Invalid Range '" + cl
                    + "' deparsed as [" + from + "," + to + "]");
          }
        }
        else
        {
          int r = -1;
          try
          {
            r = new Integer(cl).intValue();
            r--;
          } catch (NumberFormatException ex)
          {
            if (cl.toLowerCase().equals("sequence"))
            {
              // we are in the dataset sequence's coordinate frame.
              inseqpos = true;
            }
            else
            {
              System.err
                      .println("ERROR: Couldn't parse integer from point selection element of column selection string '"
                              + cl + "'");
              return;
            }
          }
          if (r >= 0 && r <= alw)
          {
            if (!seset)
            {
              start = r;
              end = r;
              seset = true;
            }
            else
            {
              // comment to prevent range extension
              if (start > r)
              {
                start = r;
              }
              if (end < r)
              {
                end = r;
              }
            }
            csel.addElement(r);
            if (debug)
            {
              System.err.println("Point selection '" + cl
                      + "' deparsed as [" + r + "]");
            }
          }
          else
          {
            System.err.println("ERROR: Invalid Point selection '" + cl
                    + "' deparsed as [" + r + "]");
          }
        }
      }
    }
    if (seqsfound)
    {
      // we only propagate the selection when it was the null selection, or the
      // given sequences were found in the alignment.
      if (inseqpos && sel.getSize() > 0)
      {
        // assume first sequence provides reference frame ?
        SequenceI rs = sel.getSequenceAt(0);
        start = rs.findIndex(start);
        end = rs.findIndex(end);
        if (csel != null)
        {
          List<Integer> cs = csel.getSelected();
          csel.clear();
          for (Integer selectedCol : cs)
          {
            csel.addElement(rs.findIndex(selectedCol));
          }
        }
      }
      sel.setStartRes(start);
      sel.setEndRes(end);
      EventQueue.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          alf.select(sel, csel);
        }
      });
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getSelectedSequencesAsAlignment(java.lang.
   * String, java.lang.String)
   */
  public String getSelectedSequencesAsAlignment(String format, String suffix)
  {
    return getSelectedSequencesAsAlignmentFrom(getDefaultTargetFrame(),
            format, suffix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getSelectedSequencesAsAlignmentFrom(jalview
   * .appletgui.AlignFrame, java.lang.String, java.lang.String)
   */
  public String getSelectedSequencesAsAlignmentFrom(AlignFrame alf,
          String format, String suffix)
  {
    try
    {
      boolean seqlimits = suffix.equalsIgnoreCase(TRUE);
      if (alf.viewport.getSelectionGroup() != null)
      {
        // JBPNote: getSelectionAsNewSequence behaviour has changed - this
        // method now returns a full copy of sequence data
        // TODO consider using getSequenceSelection instead here
        String reply = new AppletFormatAdapter().formatSequences(format,
                new Alignment(alf.viewport.getSelectionAsNewSequence()),
                seqlimits);
        return reply;
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
      return "Error retrieving alignment in " + format + " format. ";
    }
    return "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getAlignmentOrder()
   */
  public String getAlignmentOrder()
  {
    return getAlignmentOrderFrom(getDefaultTargetFrame());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getAlignmentOrderFrom(jalview.appletgui.AlignFrame
   * )
   */
  public String getAlignmentOrderFrom(AlignFrame alf)
  {
    return getAlignmentOrderFrom(alf, separator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getAlignmentOrderFrom(jalview.appletgui.AlignFrame
   * , java.lang.String)
   */
  public String getAlignmentOrderFrom(AlignFrame alf, String sep)
  {
    AlignmentI alorder = alf.getAlignViewport().getAlignment();
    String[] order = new String[alorder.getHeight()];
    for (int i = 0; i < order.length; i++)
    {
      order[i] = alorder.getSequenceAt(i).getName();
    }
    return arrayToSeparatorList(order);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#orderBy(java.lang.String,
   * java.lang.String)
   */
  public String orderBy(String order, String undoName)
  {
    return orderBy(order, undoName, separator);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#orderBy(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public String orderBy(String order, String undoName, String sep)
  {
    return orderAlignmentBy(getDefaultTargetFrame(), order, undoName, sep);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#orderAlignmentBy(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public String orderAlignmentBy(AlignFrame alf, String order,
          String undoName, String sep)
  {
    String[] ids = separatorListToArray(order, sep);
    SequenceI[] sqs = null;
    if (ids != null && ids.length > 0)
    {
      jalview.analysis.SequenceIdMatcher matcher = new jalview.analysis.SequenceIdMatcher(
              alf.viewport.getAlignment().getSequencesArray());
      int s = 0;
      sqs = new SequenceI[ids.length];
      for (int i = 0; i < ids.length; i++)
      {
        if (ids[i].trim().length() == 0)
        {
          continue;
        }
        SequenceI sq = matcher.findIdMatch(ids[i]);
        if (sq != null)
        {
          sqs[s++] = sq;
        }
      }
      if (s > 0)
      {
        SequenceI[] sqq = new SequenceI[s];
        System.arraycopy(sqs, 0, sqq, 0, s);
        sqs = sqq;
      }
      else
      {
        sqs = null;
      }
    }
    if (sqs == null)
    {
      return "";
    }
    ;
    final AlignmentOrder aorder = new AlignmentOrder(sqs);

    if (undoName != null && undoName.trim().length() == 0)
    {
      undoName = null;
    }
    final String _undoName = undoName;
    // TODO: deal with synchronization here: cannot raise any events until after
    // this has returned.
    return alf.sortBy(aorder, _undoName) ? TRUE : "";
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getAlignment(java.lang.String)
   */
  public String getAlignment(String format)
  {
    return getAlignmentFrom(getDefaultTargetFrame(), format, TRUE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getAlignmentFrom(jalview.appletgui.AlignFrame,
   * java.lang.String)
   */
  public String getAlignmentFrom(AlignFrame alf, String format)
  {
    return getAlignmentFrom(alf, format, TRUE);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getAlignment(java.lang.String,
   * java.lang.String)
   */
  public String getAlignment(String format, String suffix)
  {
    return getAlignmentFrom(getDefaultTargetFrame(), format, suffix);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getAlignmentFrom(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String)
   */
  public String getAlignmentFrom(AlignFrame alf, String format,
          String suffix)
  {
    try
    {
      boolean seqlimits = suffix.equalsIgnoreCase(TRUE);

      String reply = new AppletFormatAdapter().formatSequences(format,
              alf.viewport.getAlignment(), seqlimits);
      return reply;
    } catch (Exception ex)
    {
      ex.printStackTrace();
      return "Error retrieving alignment in " + format + " format. ";
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#loadAnnotation(java.lang.String)
   */
  public void loadAnnotation(String annotation)
  {
    loadAnnotationFrom(getDefaultTargetFrame(), annotation);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#loadAnnotationFrom(jalview.appletgui.AlignFrame
   * , java.lang.String)
   */
  public void loadAnnotationFrom(AlignFrame alf, String annotation)
  {
    if (new AnnotationFile().annotateAlignmentView(alf.getAlignViewport(),
            annotation, AppletFormatAdapter.PASTE))
    {
      alf.alignPanel.fontChanged();
      alf.alignPanel.setScrollValues(0, 0);
    }
    else
    {
      alf.parseFeaturesFile(annotation, AppletFormatAdapter.PASTE);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#loadAnnotation(java.lang.String)
   */
  public void loadFeatures(String features, boolean autoenabledisplay)
  {
    loadFeaturesFrom(getDefaultTargetFrame(), features, autoenabledisplay);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#loadAnnotationFrom(jalview.appletgui.AlignFrame
   * , java.lang.String)
   */
  public boolean loadFeaturesFrom(AlignFrame alf, String features,
          boolean autoenabledisplay)
  {
    return alf.parseFeaturesFile(features, AppletFormatAdapter.PASTE,
            autoenabledisplay);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getFeatures(java.lang.String)
   */
  public String getFeatures(String format)
  {
    return getFeaturesFrom(getDefaultTargetFrame(), format);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getFeaturesFrom(jalview.appletgui.AlignFrame,
   * java.lang.String)
   */
  public String getFeaturesFrom(AlignFrame alf, String format)
  {
    return alf.outputFeatures(false, format);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getAnnotation()
   */
  public String getAnnotation()
  {
    return getAnnotationFrom(getDefaultTargetFrame());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getAnnotationFrom(jalview.appletgui.AlignFrame
   * )
   */
  public String getAnnotationFrom(AlignFrame alf)
  {
    return alf.outputAnnotations(false);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#newView()
   */
  public AlignFrame newView()
  {
    return newViewFrom(getDefaultTargetFrame());
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#newView(java.lang.String)
   */
  public AlignFrame newView(String name)
  {
    return newViewFrom(getDefaultTargetFrame(), name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#newViewFrom(jalview.appletgui.AlignFrame)
   */
  public AlignFrame newViewFrom(AlignFrame alf)
  {
    return alf.newView(null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#newViewFrom(jalview.appletgui.AlignFrame,
   * java.lang.String)
   */
  public AlignFrame newViewFrom(AlignFrame alf, String name)
  {
    return alf.newView(name);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#loadAlignment(java.lang.String,
   * java.lang.String)
   */
  public AlignFrame loadAlignment(String text, String title)
  {
    AlignmentI al = null;

    String format = new IdentifyFile().Identify(text,
            AppletFormatAdapter.PASTE);
    try
    {
      al = new AppletFormatAdapter().readFile(text,
              AppletFormatAdapter.PASTE, format);
      if (al.getHeight() > 0)
      {
        return new AlignFrame(al, this, title, false);
      }
    } catch (java.io.IOException ex)
    {
      ex.printStackTrace();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setMouseoverListener(java.lang.String)
   */
  public void setMouseoverListener(String listener)
  {
    setMouseoverListener(currentAlignFrame, listener);
  }

  private Vector<jalview.javascript.JSFunctionExec> javascriptListeners = new Vector<jalview.javascript.JSFunctionExec>();

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#setMouseoverListener(jalview.appletgui.AlignFrame
   * , java.lang.String)
   */
  public void setMouseoverListener(AlignFrame af, String listener)
  {
    if (listener != null)
    {
      listener = listener.trim();
      if (listener.length() == 0)
      {
        System.err
                .println("jalview Javascript error: Ignoring empty function for mouseover listener.");
        return;
      }
    }
    jalview.javascript.MouseOverListener mol = new jalview.javascript.MouseOverListener(
            this, af, listener);
    javascriptListeners.addElement(mol);
    StructureSelectionManager.getStructureSelectionManager(this)
            .addStructureViewerListener(mol);
    if (debug)
    {
      System.err.println("Added a mouseover listener for "
              + ((af == null) ? "All frames" : "Just views for "
                      + af.getAlignViewport().getSequenceSetId()));
      System.err.println("There are now " + javascriptListeners.size()
              + " listeners in total.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setSelectionListener(java.lang.String)
   */
  public void setSelectionListener(String listener)
  {
    setSelectionListener(null, listener);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#setSelectionListener(jalview.appletgui.AlignFrame
   * , java.lang.String)
   */
  public void setSelectionListener(AlignFrame af, String listener)
  {
    if (listener != null)
    {
      listener = listener.trim();
      if (listener.length() == 0)
      {
        System.err
                .println("jalview Javascript error: Ignoring empty function for selection listener.");
        return;
      }
    }
    jalview.javascript.JsSelectionSender mol = new jalview.javascript.JsSelectionSender(
            this, af, listener);
    javascriptListeners.addElement(mol);
    StructureSelectionManager.getStructureSelectionManager(this)
            .addSelectionListener(mol);
    if (debug)
    {
      System.err.println("Added a selection listener for "
              + ((af == null) ? "All frames" : "Just views for "
                      + af.getAlignViewport().getSequenceSetId()));
      System.err.println("There are now " + javascriptListeners.size()
              + " listeners in total.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setStructureListener(java.lang.String,
   * java.lang.String)
   */
  public void setStructureListener(String listener, String modelSet)
  {
    if (listener != null)
    {
      listener = listener.trim();
      if (listener.length() == 0)
      {
        System.err
                .println("jalview Javascript error: Ignoring empty function for selection listener.");
        return;
      }
    }
    jalview.javascript.MouseOverStructureListener mol = new jalview.javascript.MouseOverStructureListener(
            this, listener, separatorListToArray(modelSet));
    javascriptListeners.addElement(mol);
    StructureSelectionManager.getStructureSelectionManager(this)
            .addStructureViewerListener(mol);
    if (debug)
    {
      System.err.println("Added a javascript structure viewer listener '"
              + listener + "'");
      System.err.println("There are now " + javascriptListeners.size()
              + " listeners in total.");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#removeJavascriptListener(jalview.appletgui
   * .AlignFrame, java.lang.String)
   */
  public void removeJavascriptListener(AlignFrame af, String listener)
  {
    if (listener != null)
    {
      listener = listener.trim();
      if (listener.length() == 0)
      {
        listener = null;
      }
    }
    boolean rprt = false;
    for (int ms = 0, msSize = javascriptListeners.size(); ms < msSize;)
    {
      Object lstn = javascriptListeners.elementAt(ms);
      JsCallBack lstner = (JsCallBack) lstn;
      if ((af == null || lstner.getAlignFrame() == af)
              && (listener == null || lstner.getListenerFunction().equals(
                      listener)))
      {
        javascriptListeners.removeElement(lstner);
        msSize--;
        if (lstner instanceof SelectionListener)
        {
          StructureSelectionManager.getStructureSelectionManager(this)
                  .removeSelectionListener((SelectionListener) lstner);
        }
        else
        {
          StructureSelectionManager.getStructureSelectionManager(this)
                  .removeStructureViewerListener(lstner, null);
        }
        rprt = debug;
        if (debug)
        {
          System.err.println("Removed listener '" + listener + "'");
        }
      }
      else
      {
        ms++;
      }
    }
    if (rprt)
    {
      System.err.println("There are now " + javascriptListeners.size()
              + " listeners in total.");
    }
  }

  public void stop()
  {
    System.err.println("Applet " + getName() + " stop().");
    tidyUp();
  }

  public void destroy()
  {
    System.err.println("Applet " + getName() + " destroy().");
    tidyUp();
  }

  private void tidyUp()
  {
    removeAll();
    if (currentAlignFrame != null && currentAlignFrame.viewport != null
            && currentAlignFrame.viewport.applet != null)
    {
      AlignViewport av = currentAlignFrame.viewport;
      currentAlignFrame.closeMenuItem_actionPerformed();
      av.applet = null;
      currentAlignFrame = null;
    }
    if (javascriptListeners != null)
    {
      while (javascriptListeners.size() > 0)
      {
        jalview.javascript.JSFunctionExec mol = javascriptListeners
                .elementAt(0);
        javascriptListeners.removeElement(mol);
        if (mol instanceof SelectionListener)
        {
          StructureSelectionManager.getStructureSelectionManager(this)
                  .removeSelectionListener((SelectionListener) mol);
        }
        else
        {
          StructureSelectionManager.getStructureSelectionManager(this)
                  .removeStructureViewerListener(mol, null);
        }
        mol.jvlite = null;
      }
    }
    if (jsFunctionExec != null)
    {
      jsFunctionExec.stopQueue();
      jsFunctionExec.jvlite = null;
    }
    initialAlignFrame = null;
    jsFunctionExec = null;
    javascriptListeners = null;
    StructureSelectionManager.release(this);
  }

  private jalview.javascript.JSFunctionExec jsFunctionExec;

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#mouseOverStructure(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void mouseOverStructure(final String pdbResNum,
          final String chain, final String pdbfile)
  {
    final StructureSelectionManagerProvider me = this;
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          StructureSelectionManager.getStructureSelectionManager(me)
                  .mouseOverStructure(new Integer(pdbResNum).intValue(),
                          chain, pdbfile);
          if (debug)
          {
            System.err.println("mouseOver for '" + pdbResNum
                    + "' in chain '" + chain + "' in structure '" + pdbfile
                    + "'");
          }
        } catch (NumberFormatException e)
        {
          System.err.println("Ignoring invalid residue number string '"
                  + pdbResNum + "'");
        }

      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#scrollViewToIn(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String)
   */
  public void scrollViewToIn(final AlignFrame alf, final String topRow,
          final String leftHandColumn)
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          alf.scrollTo(new Integer(topRow).intValue(), new Integer(
                  leftHandColumn).intValue());

        } catch (Exception ex)
        {
          System.err.println("Couldn't parse integer arguments (topRow='"
                  + topRow + "' and leftHandColumn='" + leftHandColumn
                  + "')");
          ex.printStackTrace();
        }
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.javascript.JalviewLiteJsApi#scrollViewToRowIn(jalview.appletgui
   * .AlignFrame, java.lang.String)
   */
  @Override
  public void scrollViewToRowIn(final AlignFrame alf, final String topRow)
  {

    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          alf.scrollToRow(new Integer(topRow).intValue());

        } catch (Exception ex)
        {
          System.err.println("Couldn't parse integer arguments (topRow='"
                  + topRow + "')");
          ex.printStackTrace();
        }

      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.javascript.JalviewLiteJsApi#scrollViewToColumnIn(jalview.appletgui
   * .AlignFrame, java.lang.String)
   */
  @Override
  public void scrollViewToColumnIn(final AlignFrame alf,
          final String leftHandColumn)
  {
    java.awt.EventQueue.invokeLater(new Runnable()
    {

      @Override
      public void run()
      {
        try
        {
          alf.scrollToColumn(new Integer(leftHandColumn).intValue());

        } catch (Exception ex)
        {
          System.err
                  .println("Couldn't parse integer arguments (leftHandColumn='"
                          + leftHandColumn + "')");
          ex.printStackTrace();
        }
      }
    });

  }

  // //////////////////////////////////////////////
  // //////////////////////////////////////////////

  public static int lastFrameX = 200;

  public static int lastFrameY = 200;

  boolean fileFound = true;

  String file = "No file";

  String file2 = null;

  Button launcher = new Button(
          MessageManager.getString("label.start_jalview"));

  /**
   * The currentAlignFrame is static, it will change if and when the user
   * selects a new window. Note that it will *never* point back to the embedded
   * AlignFrame if the applet is started as embedded on the page and then
   * afterwards a new view is created.
   */
  public AlignFrame currentAlignFrame = null;

  /**
   * This is the first frame to be displayed, and does not change. API calls
   * will default to this instance if currentAlignFrame is null.
   */
  AlignFrame initialAlignFrame = null;

  boolean embedded = false;

  private boolean checkForJmol = true;

  private boolean checkedForJmol = false; // ensure we don't check for jmol

  // every time the app is re-inited

  public boolean jmolAvailable = false;

  private boolean alignPdbStructures = false;

  /**
   * use an external structure viewer exclusively (no jmols or MCViews will be
   * opened by JalviewLite itself)
   */
  public boolean useXtrnalSviewer = false;

  public static boolean debug = false;

  static String builddate = null, version = null, installation = null;

  private static void initBuildDetails()
  {
    if (builddate == null)
    {
      builddate = "unknown";
      version = "test";
      installation = "applet";
      java.net.URL url = JalviewLite.class
              .getResource("/.build_properties");
      if (url != null)
      {
        try
        {
          BufferedReader reader = new BufferedReader(new InputStreamReader(
                  url.openStream()));
          String line;
          while ((line = reader.readLine()) != null)
          {
            if (line.indexOf("VERSION") > -1)
            {
              version = line.substring(line.indexOf("=") + 1);
            }
            if (line.indexOf("BUILD_DATE") > -1)
            {
              builddate = line.substring(line.indexOf("=") + 1);
            }
            if (line.indexOf("INSTALLATION") > -1)
            {
              installation = line.substring(line.indexOf("=") + 1);
            }
          }
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }
  }

  public static String getBuildDate()
  {
    initBuildDetails();
    return builddate;
  }

  public static String getInstallation()
  {
    initBuildDetails();
    return installation;
  }

  public static String getVersion()
  {
    initBuildDetails();
    return version;
  }

  // public JSObject scriptObject = null;

  /**
   * init method for Jalview Applet
   */
  public void init()
  {
    // remove any handlers that might be hanging around from an earlier instance
    try
    {
      if (debug)
      {
        System.err.println("Applet context is '"
                + getAppletContext().getClass().toString() + "'");
      }
      JSObject scriptObject = JSObject.getWindow(this);
      if (debug && scriptObject != null)
      {
        System.err.println("Applet has Javascript callback support.");
      }

    } catch (Exception ex)
    {
      System.err
              .println("Warning: No JalviewLite javascript callbacks available.");
      if (debug)
      {
        ex.printStackTrace();
      }
    }
    /**
     * turn on extra applet debugging
     */
    debug = TRUE.equalsIgnoreCase(getParameter("debug"));
    if (debug)
    {

      System.err.println("JalviewLite Version " + getVersion());
      System.err.println("Build Date : " + getBuildDate());
      System.err.println("Installation : " + getInstallation());

    }
    String externalsviewer = getParameter("externalstructureviewer");
    if (externalsviewer != null)
    {
      useXtrnalSviewer = externalsviewer.trim().toLowerCase().equals(TRUE);
    }
    /**
     * if true disable the check for jmol
     */
    String chkforJmol = getParameter("nojmol");
    if (chkforJmol != null)
    {
      checkForJmol = !chkforJmol.equals(TRUE);
    }
    /**
     * get the separator parameter if present
     */
    String sep = getParameter("separator");
    if (sep != null)
    {
      if (sep.length() > 0)
      {
        separator = sep;
        if (debug)
        {
          System.err.println("Separator set to '" + separator + "'");
        }
      }
      else
      {
        throw new Error(
                MessageManager
                        .getString("error.invalid_separator_parameter"));
      }
    }
    int r = 255;
    int g = 255;
    int b = 255;
    String param = getParameter("RGB");

    if (param != null)
    {
      try
      {
        r = Integer.parseInt(param.substring(0, 2), 16);
        g = Integer.parseInt(param.substring(2, 4), 16);
        b = Integer.parseInt(param.substring(4, 6), 16);
      } catch (Exception ex)
      {
        r = 255;
        g = 255;
        b = 255;
      }
    }
    param = getParameter("label");
    if (param != null)
    {
      launcher.setLabel(param);
    }

    setBackground(new Color(r, g, b));

    file = getParameter("file");

    if (file == null)
    {
      // Maybe the sequences are added as parameters
      StringBuffer data = new StringBuffer("PASTE");
      int i = 1;
      while ((file = getParameter("sequence" + i)) != null)
      {
        data.append(file.toString() + "\n");
        i++;
      }
      if (data.length() > 5)
      {
        file = data.toString();
      }
    }
    if (getDefaultParameter("enableSplitFrame", true))
    {
      file2 = getParameter("file2");
    }

    embedded = TRUE.equalsIgnoreCase(getParameter("embedded"));
    if (embedded)
    {
      LoadingThread loader = new LoadingThread(file, file2, this);
      loader.start();
    }
    else if (file != null)
    {
      /*
       * Start the applet immediately or show a button to start it
       */
      if (FALSE.equalsIgnoreCase(getParameter("showbutton")))
      {
        LoadingThread loader = new LoadingThread(file, file2, this);
        loader.start();
      }
      else
      {
        add(launcher);
        launcher.addActionListener(new java.awt.event.ActionListener()
        {
          public void actionPerformed(ActionEvent e)
          {
            LoadingThread loader = new LoadingThread(file, file2,
                    JalviewLite.this);
            loader.start();
          }
        });
      }
    }
    else
    {
      // jalview initialisation with no alignment. loadAlignment() method can
      // still be called to open new alignments.
      file = "NO FILE";
      fileFound = false;
      callInitCallback();
    }
  }

  private void initLiveConnect()
  {
    // try really hard to get the liveConnect thing working
    boolean notFailed = false;
    int tries = 0;
    while (!notFailed && tries < 10)
    {
      if (tries > 0)
      {
        System.err.println("LiveConnect request thread going to sleep.");
      }
      try
      {
        Thread.sleep(700 * (1 + tries));
      } catch (InterruptedException q)
      {
      }
      ;
      if (tries++ > 0)
      {
        System.err.println("LiveConnect request thread woken up.");
      }
      try
      {
        JSObject scriptObject = JSObject.getWindow(this);
        if (scriptObject.eval("navigator") != null)
        {
          notFailed = true;
        }
      } catch (Exception jsex)
      {
        System.err.println("Attempt " + tries
                + " to access LiveConnect javascript failed.");
      }
    }
  }

  private void callInitCallback()
  {
    String initjscallback = getParameter("oninit");
    if (initjscallback == null)
    {
      return;
    }
    initjscallback = initjscallback.trim();
    if (initjscallback.length() > 0)
    {
      JSObject scriptObject = null;
      try
      {
        scriptObject = JSObject.getWindow(this);
      } catch (Exception ex)
      {
      }
      ;
      // try really hard to let the browser plugin know we want liveconnect
      initLiveConnect();

      if (scriptObject != null)
      {
        try
        {
          // do onInit with the JS executor thread
          new JSFunctionExec(this).executeJavascriptFunction(true,
                  initjscallback, null, "Calling oninit callback '"
                          + initjscallback + "'.");
        } catch (Exception e)
        {
          System.err.println("Exception when executing _oninit callback '"
                  + initjscallback + "'.");
          e.printStackTrace();
        }
      }
      else
      {
        System.err.println("Not executing _oninit callback '"
                + initjscallback + "' - no scripting allowed.");
      }
    }
  }

  /**
   * Initialises and displays a new java.awt.Frame
   * 
   * @param frame
   *          java.awt.Frame to be displayed
   * @param title
   *          title of new frame
   * @param width
   *          width if new frame
   * @param height
   *          height of new frame
   */
  public static void addFrame(final Frame frame, String title, int width,
          int height)
  {
    frame.setLocation(lastFrameX, lastFrameY);
    lastFrameX += 40;
    lastFrameY += 40;
    frame.setSize(width, height);
    frame.setTitle(title);
    frame.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        if (frame instanceof AlignFrame)
        {
          AlignViewport vp = ((AlignFrame) frame).viewport;
          ((AlignFrame) frame).closeMenuItem_actionPerformed();
          if (vp.applet.currentAlignFrame == frame)
          {
            vp.applet.currentAlignFrame = null;
          }
          vp.applet = null;
          vp = null;

        }
        lastFrameX -= 40;
        lastFrameY -= 40;
        if (frame instanceof EmbmenuFrame)
        {
          ((EmbmenuFrame) frame).destroyMenus();
        }
        frame.setMenuBar(null);
        frame.dispose();
      }

      public void windowActivated(WindowEvent e)
      {
        if (frame instanceof AlignFrame)
        {
          ((AlignFrame) frame).viewport.applet.currentAlignFrame = (AlignFrame) frame;
          if (debug)
          {
            System.err.println("Activated window " + frame);
          }
        }
        // be good.
        super.windowActivated(e);
      }
      /*
       * Probably not necessary to do this - see TODO above. (non-Javadoc)
       * 
       * @see
       * java.awt.event.WindowAdapter#windowDeactivated(java.awt.event.WindowEvent
       * )
       * 
       * public void windowDeactivated(WindowEvent e) { if (currentAlignFrame ==
       * frame) { currentAlignFrame = null; if (debug) {
       * System.err.println("Deactivated window "+frame); } }
       * super.windowDeactivated(e); }
       */
    });
    frame.setVisible(true);
  }

  /**
   * This paints the background surrounding the "Launch Jalview button" <br>
   * <br>
   * If file given in parameter not found, displays error message
   * 
   * @param g
   *          graphics context
   */
  public void paint(Graphics g)
  {
    if (!fileFound)
    {
      g.setColor(new Color(200, 200, 200));
      g.setColor(Color.cyan);
      g.fillRect(0, 0, getSize().width, getSize().height);
      g.setColor(Color.red);
      g.drawString(
              MessageManager.getString("label.jalview_cannot_open_file"),
              5, 15);
      g.drawString("\"" + file + "\"", 5, 30);
    }
    else if (embedded)
    {
      g.setColor(Color.black);
      g.setFont(new Font("Arial", Font.BOLD, 24));
      g.drawString(MessageManager.getString("label.jalview_applet"), 50,
              getSize().height / 2 - 30);
      g.drawString(MessageManager.getString("label.loading_data") + "...",
              50, getSize().height / 2);
    }
  }

  /**
   * get all components associated with the applet of the given type
   * 
   * @param class1
   * @return
   */
  public Vector getAppletWindow(Class class1)
  {
    Vector wnds = new Vector();
    Component[] cmp = getComponents();
    if (cmp != null)
    {
      for (int i = 0; i < cmp.length; i++)
      {
        if (class1.isAssignableFrom(cmp[i].getClass()))
        {
          wnds.addElement(cmp);
        }
      }
    }
    return wnds;
  }

  class LoadJmolThread extends Thread
  {
    private boolean running = false;

    public void run()
    {
      if (running || checkedForJmol)
      {
        return;
      }
      running = true;
      if (checkForJmol)
      {
        try
        {
          if (!System.getProperty("java.version").startsWith("1.1"))
          {
            Class.forName("org.jmol.adapter.smarter.SmarterJmolAdapter");
            jmolAvailable = true;
          }
          if (!jmolAvailable)
          {
            System.out
                    .println("Jmol not available - Using MCview for structures");
          }
        } catch (java.lang.ClassNotFoundException ex)
        {
        }
      }
      else
      {
        jmolAvailable = false;
        if (debug)
        {
          System.err
                  .println("Skipping Jmol check. Will use MCView (probably)");
        }
      }
      checkedForJmol = true;
      running = false;
    }

    public boolean notFinished()
    {
      return running || !checkedForJmol;
    }
  }

  class LoadingThread extends Thread
  {
    /**
     * State variable: protocol for access to file source
     */
    String protocol;

    String _file; // alignment file or URL spec

    String _file2; // second alignment file or URL spec

    JalviewLite applet;

    private void dbgMsg(String msg)
    {
      if (JalviewLite.debug)
      {
        System.err.println(msg);
      }
    }

    /**
     * update the protocol state variable for accessing the datasource located
     * by file.
     * 
     * @param file
     * @return possibly updated datasource string
     */
    public String setProtocolState(String file)
    {
      if (file.startsWith("PASTE"))
      {
        file = file.substring(5);
        protocol = AppletFormatAdapter.PASTE;
      }
      else if (inArchive(file))
      {
        protocol = AppletFormatAdapter.CLASSLOADER;
      }
      else
      {
        file = addProtocol(file);
        protocol = AppletFormatAdapter.URL;
      }
      dbgMsg("Protocol identified as '" + protocol + "'");
      return file;
    }

    public LoadingThread(String file, String file2, JalviewLite _applet)
    {
      this._file = file;
      this._file2 = file2;
      applet = _applet;
    }

    public void run()
    {
      LoadJmolThread jmolchecker = new LoadJmolThread();
      jmolchecker.start();
      while (jmolchecker.notFinished())
      {
        // wait around until the Jmol check is complete.
        try
        {
          Thread.sleep(2);
        } catch (Exception e)
        {
        }
      }
      startLoading();
      // applet.callInitCallback();
    }

    /**
     * Load the alignment and any related files as specified by applet
     * parameters
     */
    private void startLoading()
    {
      dbgMsg("Loading thread started with:\n>>file\n" + _file + ">>endfile");

      dbgMsg("Loading started.");

      AlignFrame newAlignFrame = readAlignment(_file);
      AlignFrame newAlignFrame2 = readAlignment(_file2);
      if (newAlignFrame != null)
      {
        addToDisplay(newAlignFrame, newAlignFrame2);
        loadTree(newAlignFrame);

        loadScoreFile(newAlignFrame);

        loadFeatures(newAlignFrame);

        loadAnnotations(newAlignFrame);

        loadJnetFile(newAlignFrame);

        loadPdbFiles(newAlignFrame);
      }
      else
      {
        fileFound = false;
        applet.remove(launcher);
        applet.repaint();
      }
      callInitCallback();
    }

    /**
     * Add an AlignFrame to the display; or if two are provided, a SplitFrame.
     * 
     * @param af
     * @param af2
     */
    public void addToDisplay(AlignFrame af, AlignFrame af2)
    {
      if (af2 != null)
      {
        AlignmentI al1 = af.viewport.getAlignment();
        AlignmentI al2 = af2.viewport.getAlignment();
        if (AlignmentUtils.isMappable(al1, al2))
        {
          SplitFrame sf = new SplitFrame(af, af2);
          sf.addToDisplay(embedded, JalviewLite.this);
          return;
        }
        else
        {
          String msg = "Could not map any sequence in " + af2.getTitle()
                  + " as "
                  + (al1.isNucleotide() ? "protein product" : "cDNA")
                  + " for " + af.getTitle();
          System.err.println(msg);
        }
      }

      af.addToDisplay(embedded);
    }

    /**
     * Read the alignment file (from URL, text 'paste', or archive by
     * classloader).
     * 
     * @return
     */
    protected AlignFrame readAlignment(String fileParam)
    {
      if (fileParam == null)
      {
        return null;
      }
      String resolvedFile = setProtocolState(fileParam);
      String format = new IdentifyFile().Identify(resolvedFile, protocol);
      dbgMsg("File identified as '" + format + "'");
      AlignmentI al = null;
      try
      {
        al = new AppletFormatAdapter().readFile(resolvedFile, protocol,
                format);
        if ((al != null) && (al.getHeight() > 0))
        {
          dbgMsg("Successfully loaded file.");
          al.setDataset(null);
          AlignFrame newAlignFrame = new AlignFrame(al, applet,
                  resolvedFile, embedded, false);
          newAlignFrame.setTitle(resolvedFile);
          if (initialAlignFrame == null)
          {
            initialAlignFrame = newAlignFrame;
          }
          // update the focus.
          currentAlignFrame = newAlignFrame;

          if (protocol == AppletFormatAdapter.PASTE)
          {
            newAlignFrame.setTitle(MessageManager.formatMessage(
                    "label.sequences_from", new Object[] { applet
                            .getDocumentBase().toString() }));
          }

          newAlignFrame.statusBar.setText(MessageManager.formatMessage(
                  "label.successfully_loaded_file",
                  new Object[] { resolvedFile }));

          return newAlignFrame;
        }
      } catch (java.io.IOException ex)
      {
        dbgMsg("File load exception.");
        ex.printStackTrace();
        if (debug)
        {
          try
          {
            FileParse fp = new FileParse(resolvedFile, protocol);
            String ln = null;
            dbgMsg(">>>Dumping contents of '" + resolvedFile + "' " + "("
                    + protocol + ")");
            while ((ln = fp.nextLine()) != null)
            {
              dbgMsg(ln);
            }
            dbgMsg(">>>Dump finished.");
          } catch (Exception e)
          {
            System.err
                    .println("Exception when trying to dump the content of the file parameter.");
            e.printStackTrace();
          }
        }
      }
      return null;
    }

    /**
     * Load PDBFiles if any specified by parameter(s). Returns true if loaded,
     * else false.
     * 
     * @param alignFrame
     * @return
     */
    protected boolean loadPdbFiles(AlignFrame alignFrame)
    {
      boolean result = false;
      /*
       * <param name="alignpdbfiles" value="false/true"/> Undocumented for 2.6 -
       * related to JAL-434
       */

      applet.setAlignPdbStructures(getDefaultParameter("alignpdbfiles",
              false));
      /*
       * <param name="PDBfile" value="1gaq.txt PDB|1GAQ|1GAQ|A PDB|1GAQ|1GAQ|B
       * PDB|1GAQ|1GAQ|C">
       * 
       * <param name="PDBfile2" value="1gaq.txt A=SEQA B=SEQB C=SEQB">
       * 
       * <param name="PDBfile3" value="1q0o Q45135_9MICO">
       */

      int pdbFileCount = 0;
      // Accumulate pdbs here if they are heading for the same view (if
      // alignPdbStructures is true)
      Vector pdbs = new Vector();
      // create a lazy matcher if we're asked to
      jalview.analysis.SequenceIdMatcher matcher = (applet
              .getDefaultParameter("relaxedidmatch", false)) ? new jalview.analysis.SequenceIdMatcher(
              alignFrame.getAlignViewport().getAlignment()
                      .getSequencesArray()) : null;

      String param;
      do
      {
        if (pdbFileCount > 0)
        {
          param = applet.getParameter("PDBFILE" + pdbFileCount);
        }
        else
        {
          param = applet.getParameter("PDBFILE");
        }

        if (param != null)
        {
          PDBEntry pdb = new PDBEntry();

          String seqstring;
          SequenceI[] seqs = null;
          String[] chains = null;

          StringTokenizer st = new StringTokenizer(param, " ");

          if (st.countTokens() < 2)
          {
            String sequence = applet.getParameter("PDBSEQ");
            if (sequence != null)
            {
              seqs = new SequenceI[] { matcher == null ? (Sequence) alignFrame
                      .getAlignViewport().getAlignment().findName(sequence)
                      : matcher.findIdMatch(sequence) };
            }

          }
          else
          {
            param = st.nextToken();
            Vector tmp = new Vector();
            Vector tmp2 = new Vector();

            while (st.hasMoreTokens())
            {
              seqstring = st.nextToken();
              StringTokenizer st2 = new StringTokenizer(seqstring, "=");
              if (st2.countTokens() > 1)
              {
                // This is the chain
                tmp2.addElement(st2.nextToken());
                seqstring = st2.nextToken();
              }
              tmp.addElement(matcher == null ? (Sequence) alignFrame
                      .getAlignViewport().getAlignment()
                      .findName(seqstring) : matcher.findIdMatch(seqstring));
            }

            seqs = new SequenceI[tmp.size()];
            tmp.copyInto(seqs);
            if (tmp2.size() == tmp.size())
            {
              chains = new String[tmp2.size()];
              tmp2.copyInto(chains);
            }
          }
          param = setProtocolState(param);

          if (// !jmolAvailable
          // &&
          protocol == AppletFormatAdapter.CLASSLOADER && !useXtrnalSviewer)
          {
            // Re: JAL-357 : the bug isn't a problem if we are using an
            // external viewer!
            // TODO: verify this Re:
            // https://mantis.lifesci.dundee.ac.uk/view.php?id=36605
            // This exception preserves the current behaviour where, even if
            // the local pdb file was identified in the class loader
            protocol = AppletFormatAdapter.URL; // this is probably NOT
            // CORRECT!
            param = addProtocol(param); //
          }

          pdb.setFile(param);

          if (seqs != null)
          {
            for (int i = 0; i < seqs.length; i++)
            {
              if (seqs[i] != null)
              {
                ((Sequence) seqs[i]).addPDBId(pdb);
                StructureSelectionManager.getStructureSelectionManager(
                        applet).registerPDBEntry(pdb);
              }
              else
              {
                if (JalviewLite.debug)
                {
                  // this may not really be a problem but we give a warning
                  // anyway
                  System.err
                          .println("Warning: Possible input parsing error: Null sequence for attachment of PDB (sequence "
                                  + i + ")");
                }
              }
            }

            if (!alignPdbStructures)
            {
              alignFrame.newStructureView(applet, pdb, seqs, chains,
                      protocol);
            }
            else
            {
              pdbs.addElement(new Object[] { pdb, seqs, chains,
                  new String(protocol) });
            }
          }
        }

        pdbFileCount++;
      } while (param != null || pdbFileCount < 10);
      if (pdbs.size() > 0)
      {
        SequenceI[][] seqs = new SequenceI[pdbs.size()][];
        PDBEntry[] pdb = new PDBEntry[pdbs.size()];
        String[][] chains = new String[pdbs.size()][];
        String[] protocols = new String[pdbs.size()];
        for (int pdbsi = 0, pdbsiSize = pdbs.size(); pdbsi < pdbsiSize; pdbsi++)
        {
          Object[] o = (Object[]) pdbs.elementAt(pdbsi);
          pdb[pdbsi] = (PDBEntry) o[0];
          seqs[pdbsi] = (SequenceI[]) o[1];
          chains[pdbsi] = (String[]) o[2];
          protocols[pdbsi] = (String) o[3];
        }
        alignFrame.alignedStructureView(applet, pdb, seqs, chains,
                protocols);
        result = true;
      }
      return result;
    }

    /**
     * Load in a Jnetfile if specified by parameter. Returns true if loaded,
     * else false.
     * 
     * @param alignFrame
     * @return
     */
    protected boolean loadJnetFile(AlignFrame alignFrame)
    {
      boolean result = false;
      String param = applet.getParameter("jnetfile");
      if (param != null)
      {
        try
        {
          param = setProtocolState(param);
          JPredFile predictions = new JPredFile(param, protocol);
          JnetAnnotationMaker.add_annotation(predictions,
                  alignFrame.viewport.getAlignment(), 0, false);
          // false == do not add sequence profile from concise output
          SequenceI repseq = alignFrame.viewport.getAlignment()
                  .getSequenceAt(0);
          alignFrame.viewport.getAlignment().setSeqrep(repseq);
          ColumnSelection cs = new ColumnSelection();
          cs.hideInsertionsFor(repseq);
          alignFrame.viewport.setColumnSelection(cs);
          alignFrame.alignPanel.fontChanged();
          alignFrame.alignPanel.setScrollValues(0, 0);
          result = true;
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      return result;
    }

    /**
     * Load annotations if specified by parameter. Returns true if loaded, else
     * false.
     * 
     * @param alignFrame
     * @return
     */
    protected boolean loadAnnotations(AlignFrame alignFrame)
    {
      boolean result = false;
      String param = applet.getParameter("annotations");
      if (param != null)
      {
        param = setProtocolState(param);

        if (new AnnotationFile().annotateAlignmentView(alignFrame.viewport,
                param, protocol))
        {
          alignFrame.alignPanel.fontChanged();
          alignFrame.alignPanel.setScrollValues(0, 0);
          result = true;
        }
        else
        {
          System.err
                  .println("Annotations were not added from annotation file '"
                          + param + "'");
        }
      }
      return result;
    }

    /**
     * Load features file and view settings as specified by parameters. Returns
     * true if features were loaded, else false.
     * 
     * @param alignFrame
     * @return
     */
    protected boolean loadFeatures(AlignFrame alignFrame)
    {
      boolean result = false;
      // ///////////////////////////
      // modify display of features
      // we do this before any features have been loaded, ensuring any hidden
      // groups are hidden when features first displayed
      //
      // hide specific groups
      //
      String param = applet.getParameter("hidefeaturegroups");
      if (param != null)
      {
        alignFrame.setFeatureGroupState(separatorListToArray(param), false);
        // applet.setFeatureGroupStateOn(newAlignFrame, param, false);
      }
      // show specific groups
      param = applet.getParameter("showfeaturegroups");
      if (param != null)
      {
        alignFrame.setFeatureGroupState(separatorListToArray(param), true);
        // applet.setFeatureGroupStateOn(newAlignFrame, param, true);
      }
      // and now load features
      param = applet.getParameter("features");
      if (param != null)
      {
        param = setProtocolState(param);

        result = alignFrame.parseFeaturesFile(param, protocol);
      }

      param = applet.getParameter("showFeatureSettings");
      if (param != null && param.equalsIgnoreCase(TRUE))
      {
        alignFrame.viewport.setShowSequenceFeatures(true);
        new FeatureSettings(alignFrame.alignPanel);
      }
      return result;
    }

    /**
     * Load a score file if specified by parameter. Returns true if file was
     * loaded, else false.
     * 
     * @param alignFrame
     */
    protected boolean loadScoreFile(AlignFrame alignFrame)
    {
      boolean result = false;
      String sScoreFile = applet.getParameter("scoreFile");
      if (sScoreFile != null && !"".equals(sScoreFile))
      {
        try
        {
          if (debug)
          {
            System.err
                    .println("Attempting to load T-COFFEE score file from the scoreFile parameter");
          }
          result = alignFrame.loadScoreFile(sScoreFile);
          if (!result)
          {
            System.err
                    .println("Failed to parse T-COFFEE parameter as a valid score file ('"
                            + sScoreFile + "')");
          }
        } catch (Exception e)
        {
          System.err.printf("Cannot read score file: '%s'. Cause: %s \n",
                  sScoreFile, e.getMessage());
        }
      }
      return result;
    }

    /**
     * Load a tree for the alignment if specified by parameter. Returns true if
     * a tree was loaded, else false.
     * 
     * @param alignFrame
     * @return
     */
    protected boolean loadTree(AlignFrame alignFrame)
    {
      boolean result = false;
      String treeFile = applet.getParameter("tree");
      if (treeFile == null)
      {
        treeFile = applet.getParameter("treeFile");
      }

      if (treeFile != null)
      {
        try
        {
          treeFile = setProtocolState(treeFile);
          NewickFile fin = new NewickFile(treeFile, protocol);
          fin.parse();

          if (fin.getTree() != null)
          {
            alignFrame.loadTree(fin, treeFile);
            result = true;
            dbgMsg("Successfully imported tree.");
          }
          else
          {
            dbgMsg("Tree parameter did not resolve to a valid tree.");
          }
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      return result;
    }

    /**
     * Discovers whether the given file is in the Applet Archive
     * 
     * @param file
     *          String
     * @return boolean
     */
    boolean inArchive(String file)
    {
      // This might throw a security exception in certain browsers
      // Netscape Communicator for instance.
      try
      {
        boolean rtn = (getClass().getResourceAsStream("/" + file) != null);
        if (debug)
        {
          System.err.println("Resource '" + file + "' was "
                  + (rtn ? "" : "not") + " located by classloader.");
        }
        return rtn;
      } catch (Exception ex)
      {
        System.out.println("Exception checking resources: " + file + " "
                + ex);
        return false;
      }
    }

    /**
     * If the file is not already in URL format, tries to locate it by resolving
     * as a URL.
     * 
     * @param f
     * @return
     */
    String addProtocol(final String f)
    {
      if (f.indexOf("://") != -1)
      {
        // already has URL format
        return f;
      }

      /*
       * Try relative to document base
       */
      URL documentBase = getDocumentBase();
      String url = applet.resolveUrlForLocalOrAbsolute(f, documentBase);
      if (urlExists(url))
      {
        if (debug)
        {
          System.err.println("Prepended document base '" + documentBase
                  + "' to make: '" + url + "'");
        }
        return url;
      }

      /*
       * Try relative to codebase
       */
      URL codeBase = getCodeBase();
      url = applet.resolveUrlForLocalOrAbsolute(f, codeBase);
      if (urlExists(url))
      {
        if (debug)
        {
          System.err.println("Prepended codebase '" + codeBase
                  + "' to make: '" + url + "'");
        }
        return url;
      }

      return f;
    }

    /**
     * Returns true if an input stream can be opened on the specified URL, else
     * false.
     * 
     * @param url
     * @return
     */
    private boolean urlExists(String url)
    {
      InputStream is = null;
      try
      {
        is = new URL(url).openStream();
        if (is != null)
        {
          return true;
        }
      } catch (Exception x)
      {
        // ignore
      } finally
      {
        if (is != null)
        {
          try
          {
            is.close();
          } catch (IOException e)
          {
            // ignore
          }
        }
      }
      return false;
    }
  }

  /**
   * @return the default alignFrame acted on by the public applet methods. May
   *         return null with an error message on System.err indicating the
   *         fact.
   */
  public AlignFrame getDefaultTargetFrame()
  {
    if (currentAlignFrame != null)
    {
      return currentAlignFrame;
    }
    if (initialAlignFrame != null)
    {
      return initialAlignFrame;
    }
    System.err
            .println("Implementation error: Jalview Applet API cannot work out which AlignFrame to use.");
    return null;
  }

  /**
   * separator used for separatorList
   */
  protected String separator = "" + ((char) 0x00AC); // the default used to be
                                                     // '|' but many sequence
                                                     // IDS include pipes.

  /**
   * set to enable the URL based javascript execution mechanism
   */
  public boolean jsfallbackEnabled = false;

  /**
   * parse the string into a list
   * 
   * @param list
   * @return elements separated by separator
   */
  public String[] separatorListToArray(String list)
  {
    return separatorListToArray(list, separator);
  }

  /**
   * parse the string into a list
   * 
   * @param list
   * @param separator
   * @return elements separated by separator
   */
  public String[] separatorListToArray(String list, String separator)
  {
    int seplen = separator.length();
    if (list == null || list.equals("") || list.equals(separator))
    {
      return null;
    }
    java.util.Vector jv = new Vector();
    int cp = 0, pos;
    while ((pos = list.indexOf(separator, cp)) > cp)
    {
      jv.addElement(list.substring(cp, pos));
      cp = pos + seplen;
    }
    if (cp < list.length())
    {
      String c = list.substring(cp);
      if (!c.equals(separator))
      {
        jv.addElement(c);
      }
    }
    if (jv.size() > 0)
    {
      String[] v = new String[jv.size()];
      for (int i = 0; i < v.length; i++)
      {
        v[i] = (String) jv.elementAt(i);
      }
      jv.removeAllElements();
      if (debug)
      {
        System.err.println("Array from '" + separator
                + "' separated List:\n" + v.length);
        for (int i = 0; i < v.length; i++)
        {
          System.err.println("item " + i + " '" + v[i] + "'");
        }
      }
      return v;
    }
    if (debug)
    {
      System.err.println("Empty Array from '" + separator
              + "' separated List");
    }
    return null;
  }

  /**
   * concatenate the list with separator
   * 
   * @param list
   * @return concatenated string
   */
  public String arrayToSeparatorList(String[] list)
  {
    return arrayToSeparatorList(list, separator);
  }

  /**
   * concatenate the list with separator
   * 
   * @param list
   * @param separator
   * @return concatenated string
   */
  public String arrayToSeparatorList(String[] list, String separator)
  {
    StringBuffer v = new StringBuffer();
    if (list != null && list.length > 0)
    {
      for (int i = 0, iSize = list.length; i < iSize; i++)
      {
        if (list[i] != null)
        {
          if (i > 0)
          {
            v.append(separator);
          }
          v.append(list[i]);
        }
      }
      if (debug)
      {
        System.err.println("Returning '" + separator
                + "' separated List:\n");
        System.err.println(v);
      }
      return v.toString();
    }
    if (debug)
    {
      System.err.println("Returning empty '" + separator
              + "' separated List\n");
    }
    return "" + separator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getFeatureGroups()
   */
  public String getFeatureGroups()
  {
    String lst = arrayToSeparatorList(getDefaultTargetFrame()
            .getFeatureGroups());
    return lst;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getFeatureGroupsOn(jalview.appletgui.AlignFrame
   * )
   */
  public String getFeatureGroupsOn(AlignFrame alf)
  {
    String lst = arrayToSeparatorList(alf.getFeatureGroups());
    return lst;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getFeatureGroupsOfState(boolean)
   */
  public String getFeatureGroupsOfState(boolean visible)
  {
    return arrayToSeparatorList(getDefaultTargetFrame()
            .getFeatureGroupsOfState(visible));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.bin.JalviewLiteJsApi#getFeatureGroupsOfStateOn(jalview.appletgui
   * .AlignFrame, boolean)
   */
  public String getFeatureGroupsOfStateOn(AlignFrame alf, boolean visible)
  {
    return arrayToSeparatorList(alf.getFeatureGroupsOfState(visible));
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setFeatureGroupStateOn(jalview.appletgui.
   * AlignFrame, java.lang.String, boolean)
   */
  public void setFeatureGroupStateOn(final AlignFrame alf,
          final String groups, boolean state)
  {
    final boolean st = state;// !(state==null || state.equals("") ||
    // state.toLowerCase().equals("false"));
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        alf.setFeatureGroupState(separatorListToArray(groups), st);
      }
    });
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setFeatureGroupState(java.lang.String,
   * boolean)
   */
  public void setFeatureGroupState(String groups, boolean state)
  {
    setFeatureGroupStateOn(getDefaultTargetFrame(), groups, state);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getSeparator()
   */
  public String getSeparator()
  {
    return separator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#setSeparator(java.lang.String)
   */
  public void setSeparator(String separator)
  {
    if (separator == null || separator.length() < 1)
    {
      // reset to default
      separator = "" + ((char) 0x00AC);
    }
    this.separator = separator;
    if (debug)
    {
      System.err.println("Default Separator now: '" + separator + "'");
    }
  }

  /**
   * get boolean value of applet parameter 'name' and return default if
   * parameter is not set
   * 
   * @param name
   *          name of paremeter
   * @param def
   *          the value to return otherwise
   * @return true or false
   */
  public boolean getDefaultParameter(String name, boolean def)
  {
    String stn;
    if ((stn = getParameter(name)) == null)
    {
      return def;
    }
    if (TRUE.equalsIgnoreCase(stn))
    {
      return true;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#addPdbFile(jalview.appletgui.AlignFrame,
   * java.lang.String, java.lang.String, java.lang.String)
   */
  public boolean addPdbFile(AlignFrame alFrame, String sequenceId,
          String pdbEntryString, String pdbFile)
  {
    return alFrame.addPdbFile(sequenceId, pdbEntryString, pdbFile);
  }

  protected void setAlignPdbStructures(boolean alignPdbStructures)
  {
    this.alignPdbStructures = alignPdbStructures;
  }

  public boolean isAlignPdbStructures()
  {
    return alignPdbStructures;
  }

  public void start()
  {
    // callInitCallback();
  }

  private Hashtable<String, long[]> jshashes = new Hashtable<String, long[]>();

  private Hashtable<String, Hashtable<String, String[]>> jsmessages = new Hashtable<String, Hashtable<String, String[]>>();

  public void setJsMessageSet(String messageclass, String viewId,
          String[] colcommands)
  {
    Hashtable<String, String[]> msgset = jsmessages.get(messageclass);
    if (msgset == null)
    {
      msgset = new Hashtable<String, String[]>();
      jsmessages.put(messageclass, msgset);
    }
    msgset.put(viewId, colcommands);
    long[] l = new long[colcommands.length];
    for (int i = 0; i < colcommands.length; i++)
    {
      l[i] = colcommands[i].hashCode();
    }
    jshashes.put(messageclass + "|" + viewId, l);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.bin.JalviewLiteJsApi#getJsMessage(java.lang.String,
   * java.lang.String)
   */
  public String getJsMessage(String messageclass, String viewId)
  {
    Hashtable<String, String[]> msgset = jsmessages.get(messageclass);
    if (msgset != null)
    {
      String[] msgs = msgset.get(viewId);
      if (msgs != null)
      {
        for (int i = 0; i < msgs.length; i++)
        {
          if (msgs[i] != null)
          {
            String m = msgs[i];
            msgs[i] = null;
            return m;
          }
        }
      }
    }
    return "";
  }

  public boolean isJsMessageSetChanged(String string, String string2,
          String[] colcommands)
  {
    long[] l = jshashes.get(string + "|" + string2);
    if (l == null && colcommands != null)
    {
      return true;
    }
    for (int i = 0; i < colcommands.length; i++)
    {
      if (l[i] != colcommands[i].hashCode())
      {
        return true;
      }
    }
    return false;
  }

  private Vector jsExecQueue = new Vector();

  public Vector getJsExecQueue()
  {
    return jsExecQueue;
  }

  public void setExecutor(JSFunctionExec jsFunctionExec2)
  {
    jsFunctionExec = jsFunctionExec2;
  }

  /**
   * return the given colour value parameter or the given default if parameter
   * not given
   * 
   * @param colparam
   * @param defcolour
   * @return
   */
  public Color getDefaultColourParameter(String colparam, Color defcolour)
  {
    String colprop = getParameter(colparam);
    if (colprop == null || colprop.trim().length() == 0)
    {
      return defcolour;
    }
    Color col = jalview.schemes.ColourSchemeProperty
            .getAWTColorFromName(colprop);
    if (col == null)
    {
      try
      {
        col = new jalview.schemes.UserColourScheme(colprop).findColour('A');
      } catch (Exception ex)
      {
        System.err.println("Couldn't parse '" + colprop
                + "' as a colour for " + colparam);
        col = null;
      }
    }
    return (col == null) ? defcolour : col;

  }

  public void openJalviewHelpUrl()
  {
    String helpUrl = getParameter("jalviewhelpurl");
    if (helpUrl == null || helpUrl.trim().length() < 5)
    {
      helpUrl = "http://www.jalview.org/help.html";
    }
    showURL(helpUrl, "HELP");
  }

  /**
   * form a complete URL given a path to a resource and a reference location on
   * the same server
   * 
   * @param url
   *          - an absolute path on the same server as localref or a document
   *          located relative to localref
   * @param localref
   *          - a URL on the same server as url
   * @return a complete URL for the resource located by url
   */
  private String resolveUrlForLocalOrAbsolute(String url, URL localref)
  {
    String codebase = localref.toString();
    if (url.indexOf("/") == 0)
    {
      String localfile = localref.getFile();
      url = codebase.substring(0, codebase.length() - localfile.length())
              + url;
    }
    else
    {
      url = localref + url;
    }
    if (debug)
    {
      System.err.println("URL: " + localref.toString());
      System.err.println("URL.getFile: " + localref.getFile());
      System.err.println("URL.getPath: " + localref.getPath());
      System.err.println("URL.getQuery: " + localref.getQuery());
      System.err.println("returning " + url);
    }
    return url;
  }

  /**
   * open a URL in the browser - resolving it according to relative refs and
   * coping with javascript: protocol if necessary.
   * 
   * @param url
   * @param target
   */
  public void showURL(String url, String target)
  {
    try
    {
      if (url.indexOf(":") == -1)
      {
        // TODO: verify (Bas Vroling bug) prepend codebase or server URL to
        // form valid URL
        // Should really use docbase, not codebase.
        URL prepend;
        url = resolveUrlForLocalOrAbsolute(
                url,
                prepend = getDefaultParameter("resolvetocodebase", false) ? getDocumentBase()
                        : getCodeBase());
        if (debug)
        {
          System.err
                  .println("Show url (prepended "
                          + prepend
                          + " - toggle resolvetocodebase if code/docbase resolution is wrong): "
                          + url);
        }
      }
      else
      {
        if (debug)
        {
          System.err.println("Show url: " + url);
        }
      }
      if (url.indexOf("javascript:") == 0)
      {
        // no target for the javascript context
        getAppletContext().showDocument(new java.net.URL(url));
      }
      else
      {
        getAppletContext().showDocument(new java.net.URL(url), target);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * bind structures in a viewer to any matching sequences in an alignFrame (use
   * sequenceIds to limit scope of search to specific sequences)
   * 
   * @param alFrame
   * @param viewer
   * @param sequenceIds
   * @return TODO: consider making an exception structure for indicating when
   *         binding fails public SequenceStructureBinding
   *         addStructureViewInstance( AlignFrame alFrame, Object viewer, String
   *         sequenceIds) {
   * 
   *         if (sequenceIds != null && sequenceIds.length() > 0) { return
   *         alFrame.addStructureViewInstance(viewer,
   *         separatorListToArray(sequenceIds)); } else { return
   *         alFrame.addStructureViewInstance(viewer, null); } // return null; }
   */
}
