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
package jalview.commands;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.ReverseListIterator;
import jalview.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * 
 * <p>
 * Title: EditCommmand
 * </p>
 * 
 * <p>
 * Description: Essential information for performing undo and redo for cut/paste
 * insert/delete gap which can be stored in the HistoryList
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * 
 * <p>
 * Company: Dundee University
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class EditCommand implements CommandI
{
  public enum Action
  {
    INSERT_GAP
    {
      @Override
      public Action getUndoAction()
      {
        return DELETE_GAP;
      }
    },
    DELETE_GAP
    {
      @Override
      public Action getUndoAction()
      {
        return INSERT_GAP;
      }
    },
    CUT
    {
      @Override
      public Action getUndoAction()
      {
        return PASTE;
      }
    },
    PASTE
    {
      @Override
      public Action getUndoAction()
      {
        return CUT;
      }
    },
    REPLACE
    {
      @Override
      public Action getUndoAction()
      {
        return REPLACE;
      }
    },
    INSERT_NUC
    {
      @Override
      public Action getUndoAction()
      {
        return null;
      }
    };
    public abstract Action getUndoAction();
  };

  private List<Edit> edits = new ArrayList<Edit>();

  String description;

  public EditCommand()
  {
  }

  public EditCommand(String description)
  {
    this.description = description;
  }

  public EditCommand(String description, Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al)
  {
    this.description = description;
    if (command == Action.CUT || command == Action.PASTE)
    {
      setEdit(new Edit(command, seqs, position, number, al));
    }

    performEdit(0, null);
  }

  public EditCommand(String description, Action command, String replace,
          SequenceI[] seqs, int position, int number, AlignmentI al)
  {
    this.description = description;
    if (command == Action.REPLACE)
    {
      setEdit(new Edit(command, seqs, position, number, al, replace));
    }

    performEdit(0, null);
  }

  /**
   * Set the list of edits to the specified item (only).
   * 
   * @param e
   */
  protected void setEdit(Edit e)
  {
    edits.clear();
    edits.add(e);
  }

  /**
   * Add the given edit command to the stored list of commands. If simply
   * expanding the range of the last command added, then modify it instead of
   * adding a new command.
   * 
   * @param e
   */
  public void addEdit(Edit e)
  {
    if (!expandEdit(edits, e))
    {
      edits.add(e);
    }
  }

  /**
   * Returns true if the new edit is incorporated by updating (expanding the
   * range of) the last edit on the list, else false. We can 'expand' the last
   * edit if the new one is the same action, on the same sequences, and acts on
   * a contiguous range. This is the case where a mouse drag generates a series
   * of contiguous gap insertions or deletions.
   * 
   * @param edits
   * @param e
   * @return
   */
  protected static boolean expandEdit(List<Edit> edits, Edit e)
  {
    if (edits == null || edits.isEmpty())
    {
      return false;
    }
    Edit lastEdit = edits.get(edits.size() - 1);
    Action action = e.command;
    if (lastEdit.command != action)
    {
      return false;
    }

    /*
     * Both commands must act on the same sequences - compare the underlying
     * dataset sequences, rather than the aligned sequences, which change as
     * they are edited.
     */
    if (lastEdit.seqs.length != e.seqs.length)
    {
      return false;
    }
    for (int i = 0; i < e.seqs.length; i++)
    {
      if (lastEdit.seqs[i].getDatasetSequence() != e.seqs[i]
              .getDatasetSequence())
      {
        return false;
      }
    }

    /**
     * Check a contiguous edit; either
     * <ul>
     * <li>a new Insert <n> positions to the right of the last <insert n>, or</li>
     * <li>a new Delete <n> gaps which is <n> positions to the left of the last
     * delete.</li>
     * </ul>
     */
    boolean contiguous = (action == Action.INSERT_GAP && e.position == lastEdit.position
            + lastEdit.number)
            || (action == Action.DELETE_GAP && e.position + e.number == lastEdit.position);
    if (contiguous)
    {
      /*
       * We are just expanding the range of the last edit. For delete gap, also
       * moving the start position left.
       */
      lastEdit.number += e.number;
      lastEdit.seqs = e.seqs;
      if (action == Action.DELETE_GAP)
      {
        lastEdit.position--;
      }
      return true;
    }
    return false;
  }

  /**
   * Clear the list of stored edit commands.
   * 
   */
  protected void clearEdits()
  {
    edits.clear();
  }

  /**
   * Returns the i'th stored Edit command.
   * 
   * @param i
   * @return
   */
  protected Edit getEdit(int i)
  {
    if (i >= 0 && i < edits.size())
    {
      return edits.get(i);
    }
    return null;
  }

  @Override
  final public String getDescription()
  {
    return description;
  }

  @Override
  public int getSize()
  {
    return edits.size();
  }

  /**
   * Return the alignment for the first edit (or null if no edit).
   * 
   * @return
   */
  final public AlignmentI getAlignment()
  {
    return (edits.isEmpty() ? null : edits.get(0).al);
  }

  /**
   * append a new editCommand Note. this shouldn't be called if the edit is an
   * operation affects more alignment objects than the one referenced in al (for
   * example, cut or pasting whole sequences). Use the form with an additional
   * AlignmentI[] views parameter.
   * 
   * @param command
   * @param seqs
   * @param position
   * @param number
   * @param al
   * @param performEdit
   */
  final public void appendEdit(Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al, boolean performEdit)
  {
    appendEdit(command, seqs, position, number, al, performEdit, null);
  }

  /**
   * append a new edit command with a set of alignment views that may be
   * operated on
   * 
   * @param command
   * @param seqs
   * @param position
   * @param number
   * @param al
   * @param performEdit
   * @param views
   */
  final public void appendEdit(Action command, SequenceI[] seqs,
          int position, int number, AlignmentI al, boolean performEdit,
          AlignmentI[] views)
  {
    Edit edit = new Edit(command, seqs, position, number,
            al.getGapCharacter());
    if (al.getHeight() == seqs.length)
    {
      edit.al = al;
      edit.fullAlignmentHeight = true;
    }

    addEdit(edit);

    if (performEdit)
    {
      performEdit(edit, views);
    }
  }

  /**
   * Overloaded method that accepts an Edit object with additional parameters.
   * 
   * @param edit
   * @param al
   * @param performEdit
   * @param views
   */
  final public void appendEdit(Edit edit, AlignmentI al,
          boolean performEdit, AlignmentI[] views)
  {
    if (al.getHeight() == edit.seqs.length)
    {
      edit.al = al;
      edit.fullAlignmentHeight = true;
    }

    addEdit(edit);

    if (performEdit)
    {
      performEdit(edit, views);
    }
  }

  /**
   * Execute all the edit commands, starting at the given commandIndex
   * 
   * @param commandIndex
   * @param views
   */
  public final void performEdit(int commandIndex, AlignmentI[] views)
  {
    ListIterator<Edit> iterator = edits.listIterator(commandIndex);
    while (iterator.hasNext())
    {
      Edit edit = iterator.next();
      performEdit(edit, views);
    }
  }

  /**
   * Execute one edit command in all the specified alignment views
   * 
   * @param edit
   * @param views
   */
  protected static void performEdit(Edit edit, AlignmentI[] views)
  {
    switch (edit.command)
    {
    case INSERT_GAP:
      insertGap(edit);
      break;
    case DELETE_GAP:
      deleteGap(edit);
      break;
    case CUT:
      cut(edit, views);
      break;
    case PASTE:
      paste(edit, views);
      break;
    case REPLACE:
      replace(edit);
      break;
    case INSERT_NUC:
      // TODO:add deleteNuc for UNDO
      // case INSERT_NUC:
      // insertNuc(edits[e]);
      break;
    default:
      break;
    }
  }

  @Override
  final public void doCommand(AlignmentI[] views)
  {
    performEdit(0, views);
  }

  /**
   * Undo the stored list of commands, in reverse order.
   */
  @Override
  final public void undoCommand(AlignmentI[] views)
  {
    ListIterator<Edit> iterator = edits.listIterator(edits.size());
    while (iterator.hasPrevious())
    {
      Edit e = iterator.previous();
      switch (e.command)
      {
      case INSERT_GAP:
        deleteGap(e);
        break;
      case DELETE_GAP:
        insertGap(e);
        break;
      case CUT:
        paste(e, views);
        break;
      case PASTE:
        cut(e, views);
        break;
      case REPLACE:
        replace(e);
        break;
      case INSERT_NUC:
        // not implemented
        break;
      default:
        break;
      }
    }
  }

  /**
   * Insert gap(s) in sequences as specified by the command, and adjust
   * annotations.
   * 
   * @param command
   */
  final private static void insertGap(Edit command)
  {

    for (int s = 0; s < command.seqs.length; s++)
    {
      command.seqs[s].insertCharAt(command.position, command.number,
              command.gapChar);
      // System.out.println("pos: "+command.position+" number: "+command.number);
    }

    adjustAnnotations(command, true, false, null);
  }

  //
  // final void insertNuc(Edit command)
  // {
  //
  // for (int s = 0; s < command.seqs.length; s++)
  // {
  // System.out.println("pos: "+command.position+" number: "+command.number);
  // command.seqs[s].insertCharAt(command.position, command.number,'A');
  // }
  //
  // adjustAnnotations(command, true, false, null);
  // }

  /**
   * Delete gap(s) in sequences as specified by the command, and adjust
   * annotations.
   * 
   * @param command
   */
  final static private void deleteGap(Edit command)
  {
    for (int s = 0; s < command.seqs.length; s++)
    {
      command.seqs[s].deleteChars(command.position, command.position
              + command.number);
    }

    adjustAnnotations(command, false, false, null);
  }

  /**
   * Carry out a Cut action. The cut characters are saved in case Undo is
   * requested.
   * 
   * @param command
   * @param views
   */
  static void cut(Edit command, AlignmentI[] views)
  {
    boolean seqDeleted = false;
    command.string = new char[command.seqs.length][];

    for (int i = 0; i < command.seqs.length; i++)
    {
      final SequenceI sequence = command.seqs[i];
      if (sequence.getLength() > command.position)
      {
        command.string[i] = sequence.getSequence(command.position,
                command.position + command.number);
        SequenceI oldds = sequence.getDatasetSequence();
        if (command.oldds != null && command.oldds[i] != null)
        {
          // we are redoing an undone cut.
          sequence.setDatasetSequence(null);
        }
        sequence.deleteChars(command.position, command.position
                + command.number);
        if (command.oldds != null && command.oldds[i] != null)
        {
          // oldds entry contains the cut dataset sequence.
          sequence.setDatasetSequence(command.oldds[i]);
          command.oldds[i] = oldds;
        }
        else
        {
          // modify the oldds if necessary
          if (oldds != sequence.getDatasetSequence()
                  || sequence.getSequenceFeatures() != null)
          {
            if (command.oldds == null)
            {
              command.oldds = new SequenceI[command.seqs.length];
            }
            command.oldds[i] = oldds;
            adjustFeatures(
                    command,
                    i,
                    sequence.findPosition(command.position),
                    sequence.findPosition(command.position + command.number),
                    false);
          }
        }
      }

      if (sequence.getLength() < 1)
      {
        command.al.deleteSequence(sequence);
        seqDeleted = true;
      }
    }

    adjustAnnotations(command, false, seqDeleted, views);
  }

  /**
   * Perform the given Paste command. This may be to add cut or copied sequences
   * to an alignment, or to undo a 'Cut' action on a region of the alignment.
   * 
   * @param command
   * @param views
   */
  static void paste(Edit command, AlignmentI[] views)
  {
    StringBuffer tmp;
    boolean newDSNeeded;
    boolean newDSWasNeeded;
    int newstart, newend;
    boolean seqWasDeleted = false;
    int start = 0, end = 0;

    for (int i = 0; i < command.seqs.length; i++)
    {
      newDSNeeded = false;
      newDSWasNeeded = command.oldds != null && command.oldds[i] != null;
      if (command.seqs[i].getLength() < 1)
      {
        // ie this sequence was deleted, we need to
        // readd it to the alignment
        if (command.alIndex[i] < command.al.getHeight())
        {
          List<SequenceI> sequences;
          synchronized (sequences = command.al.getSequences())
          {
            if (!(command.alIndex[i] < 0))
            {
              sequences.add(command.alIndex[i], command.seqs[i]);
            }
          }
        }
        else
        {
          command.al.addSequence(command.seqs[i]);
        }
        seqWasDeleted = true;
      }
      newstart = command.seqs[i].getStart();
      newend = command.seqs[i].getEnd();

      tmp = new StringBuffer();
      tmp.append(command.seqs[i].getSequence());
      // Undo of a delete does not replace original dataset sequence on to
      // alignment sequence.

      if (command.string != null && command.string[i] != null)
      {
        if (command.position >= tmp.length())
        {
          // This occurs if padding is on, and residues
          // are removed from end of alignment
          int length = command.position - tmp.length();
          while (length > 0)
          {
            tmp.append(command.gapChar);
            length--;
          }
        }
        tmp.insert(command.position, command.string[i]);
        for (int s = 0; s < command.string[i].length; s++)
        {
          if (jalview.schemes.ResidueProperties.aaIndex[command.string[i][s]] != 23)
          {
            if (!newDSNeeded)
            {
              newDSNeeded = true;
              start = command.seqs[i].findPosition(command.position);
              end = command.seqs[i].findPosition(command.position
                      + command.number);
            }
            if (command.seqs[i].getStart() == start)
            {
              newstart--;
            }
            else
            {
              newend++;
            }
          }
        }
        command.string[i] = null;
      }

      command.seqs[i].setSequence(tmp.toString());
      command.seqs[i].setStart(newstart);
      command.seqs[i].setEnd(newend);
      if (newDSNeeded)
      {
        if (command.seqs[i].getDatasetSequence() != null)
        {
          SequenceI ds;
          if (newDSWasNeeded)
          {
            ds = command.oldds[i];
          }
          else
          {
            // make a new DS sequence
            // use new ds mechanism here
            ds = new Sequence(command.seqs[i].getName(),
                    jalview.analysis.AlignSeq.extractGaps(
                            jalview.util.Comparison.GapChars,
                            command.seqs[i].getSequenceAsString()),
                    command.seqs[i].getStart(), command.seqs[i].getEnd());
            ds.setDescription(command.seqs[i].getDescription());
          }
          if (command.oldds == null)
          {
            command.oldds = new SequenceI[command.seqs.length];
          }
          command.oldds[i] = command.seqs[i].getDatasetSequence();
          command.seqs[i].setDatasetSequence(ds);
        }
        adjustFeatures(command, i, start, end, true);
      }
    }
    adjustAnnotations(command, true, seqWasDeleted, views);

    command.string = null;
  }

  static void replace(Edit command)
  {
    StringBuffer tmp;
    String oldstring;
    int start = command.position;
    int end = command.number;
    // TODO TUTORIAL - Fix for replacement with different length of sequence (or
    // whole sequence)
    // TODO Jalview 2.4 bugfix change to an aggregate command - original
    // sequence string is cut, new string is pasted in.
    command.number = start + command.string[0].length;
    for (int i = 0; i < command.seqs.length; i++)
    {
      boolean newDSWasNeeded = command.oldds != null
              && command.oldds[i] != null;

      /**
       * cut addHistoryItem(new EditCommand("Cut Sequences", EditCommand.CUT,
       * cut, sg.getStartRes(), sg.getEndRes()-sg.getStartRes()+1,
       * viewport.alignment));
       * 
       */
      /**
       * then addHistoryItem(new EditCommand( "Add sequences",
       * EditCommand.PASTE, sequences, 0, alignment.getWidth(), alignment) );
       * 
       */
      oldstring = command.seqs[i].getSequenceAsString();
      tmp = new StringBuffer(oldstring.substring(0, start));
      tmp.append(command.string[i]);
      String nogaprep = jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, new String(
                      command.string[i]));
      int ipos = command.seqs[i].findPosition(start)
              - command.seqs[i].getStart();
      tmp.append(oldstring.substring(end));
      command.seqs[i].setSequence(tmp.toString());
      command.string[i] = oldstring.substring(start, end).toCharArray();
      String nogapold = jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, new String(
                      command.string[i]));
      if (!nogaprep.toLowerCase().equals(nogapold.toLowerCase()))
      {
        if (newDSWasNeeded)
        {
          SequenceI oldds = command.seqs[i].getDatasetSequence();
          command.seqs[i].setDatasetSequence(command.oldds[i]);
          command.oldds[i] = oldds;
        }
        else
        {
          if (command.oldds == null)
          {
            command.oldds = new SequenceI[command.seqs.length];
          }
          command.oldds[i] = command.seqs[i].getDatasetSequence();
          SequenceI newds = new Sequence(
                  command.seqs[i].getDatasetSequence());
          String fullseq, osp = newds.getSequenceAsString();
          fullseq = osp.substring(0, ipos) + nogaprep
                  + osp.substring(ipos + nogaprep.length());
          newds.setSequence(fullseq.toUpperCase());
          // TODO: JAL-1131 ensure newly created dataset sequence is added to
          // the set of
          // dataset sequences associated with the alignment.
          // TODO: JAL-1131 fix up any annotation associated with new dataset
          // sequence to ensure that original sequence/annotation relationships
          // are preserved.
          command.seqs[i].setDatasetSequence(newds);

        }
      }
      tmp = null;
      oldstring = null;
    }
  }

  final static void adjustAnnotations(Edit command, boolean insert,
          boolean modifyVisibility, AlignmentI[] views)
  {
    AlignmentAnnotation[] annotations = null;

    if (modifyVisibility && !insert)
    {
      // only occurs if a sequence was added or deleted.
      command.deletedAnnotationRows = new Hashtable<SequenceI, AlignmentAnnotation[]>();
    }
    if (command.fullAlignmentHeight)
    {
      annotations = command.al.getAlignmentAnnotation();
    }
    else
    {
      int aSize = 0;
      AlignmentAnnotation[] tmp;
      for (int s = 0; s < command.seqs.length; s++)
      {
        if (modifyVisibility)
        {
          // Rows are only removed or added to sequence object.
          if (!insert)
          {
            // remove rows
            tmp = command.seqs[s].getAnnotation();
            if (tmp != null)
            {
              int alen = tmp.length;
              for (int aa = 0; aa < tmp.length; aa++)
              {
                if (!command.al.deleteAnnotation(tmp[aa]))
                {
                  // strip out annotation not in the current al (will be put
                  // back on insert in all views)
                  tmp[aa] = null;
                  alen--;
                }
              }
              command.seqs[s].setAlignmentAnnotation(null);
              if (alen != tmp.length)
              {
                // save the non-null annotation references only
                AlignmentAnnotation[] saved = new AlignmentAnnotation[alen];
                for (int aa = 0, aapos = 0; aa < tmp.length; aa++)
                {
                  if (tmp[aa] != null)
                  {
                    saved[aapos++] = tmp[aa];
                    tmp[aa] = null;
                  }
                }
                tmp = saved;
                command.deletedAnnotationRows.put(command.seqs[s], saved);
                // and then remove any annotation in the other views
                for (int alview = 0; views != null && alview < views.length; alview++)
                {
                  if (views[alview] != command.al)
                  {
                    AlignmentAnnotation[] toremove = views[alview]
                            .getAlignmentAnnotation();
                    if (toremove == null || toremove.length == 0)
                    {
                      continue;
                    }
                    // remove any alignment annotation on this sequence that's
                    // on that alignment view.
                    for (int aa = 0; aa < toremove.length; aa++)
                    {
                      if (toremove[aa].sequenceRef == command.seqs[s])
                      {
                        views[alview].deleteAnnotation(toremove[aa]);
                      }
                    }
                  }
                }
              }
              else
              {
                // save all the annotation
                command.deletedAnnotationRows.put(command.seqs[s], tmp);
              }
            }
          }
          else
          {
            // recover rows
            if (command.deletedAnnotationRows != null
                    && command.deletedAnnotationRows
                            .containsKey(command.seqs[s]))
            {
              AlignmentAnnotation[] revealed = command.deletedAnnotationRows
                      .get(command.seqs[s]);
              command.seqs[s].setAlignmentAnnotation(revealed);
              if (revealed != null)
              {
                for (int aa = 0; aa < revealed.length; aa++)
                {
                  // iterate through al adding original annotation
                  command.al.addAnnotation(revealed[aa]);
                }
                for (int aa = 0; aa < revealed.length; aa++)
                {
                  command.al.setAnnotationIndex(revealed[aa], aa);
                }
                // and then duplicate added annotation on every other alignment
                // view
                for (int vnum = 0; views != null && vnum < views.length; vnum++)
                {
                  if (views[vnum] != command.al)
                  {
                    int avwidth = views[vnum].getWidth() + 1;
                    // duplicate in this view
                    for (int a = 0; a < revealed.length; a++)
                    {
                      AlignmentAnnotation newann = new AlignmentAnnotation(
                              revealed[a]);
                      command.seqs[s].addAlignmentAnnotation(newann);
                      newann.padAnnotation(avwidth);
                      views[vnum].addAnnotation(newann);
                      views[vnum].setAnnotationIndex(newann, a);
                    }
                  }
                }
              }
            }
          }
          continue;
        }

        if (command.seqs[s].getAnnotation() == null)
        {
          continue;
        }

        if (aSize == 0)
        {
          annotations = command.seqs[s].getAnnotation();
        }
        else
        {
          tmp = new AlignmentAnnotation[aSize
                  + command.seqs[s].getAnnotation().length];

          System.arraycopy(annotations, 0, tmp, 0, aSize);

          System.arraycopy(command.seqs[s].getAnnotation(), 0, tmp, aSize,
                  command.seqs[s].getAnnotation().length);

          annotations = tmp;
        }
        aSize = annotations.length;
      }
    }

    if (annotations == null)
    {
      return;
    }

    if (!insert)
    {
      command.deletedAnnotations = new Hashtable<String, Annotation[]>();
    }

    int aSize;
    Annotation[] temp;
    for (int a = 0; a < annotations.length; a++)
    {
      if (annotations[a].autoCalculated
              || annotations[a].annotations == null)
      {
        continue;
      }

      int tSize = 0;

      aSize = annotations[a].annotations.length;
      if (insert)
      {
        temp = new Annotation[aSize + command.number];
        if (annotations[a].padGaps)
        {
          for (int aa = 0; aa < temp.length; aa++)
          {
            temp[aa] = new Annotation(command.gapChar + "", null, ' ', 0);
          }
        }
      }
      else
      {
        if (command.position < aSize)
        {
          if (command.position + command.number >= aSize)
          {
            tSize = aSize;
          }
          else
          {
            tSize = aSize - command.number;
          }
        }
        else
        {
          tSize = aSize;
        }

        if (tSize < 0)
        {
          tSize = aSize;
        }
        temp = new Annotation[tSize];
      }

      if (insert)
      {
        if (command.position < annotations[a].annotations.length)
        {
          System.arraycopy(annotations[a].annotations, 0, temp, 0,
                  command.position);

          if (command.deletedAnnotations != null
                  && command.deletedAnnotations
                          .containsKey(annotations[a].annotationId))
          {
            Annotation[] restore = command.deletedAnnotations
                    .get(annotations[a].annotationId);

            System.arraycopy(restore, 0, temp, command.position,
                    command.number);

          }

          System.arraycopy(annotations[a].annotations, command.position,
                  temp, command.position + command.number, aSize
                          - command.position);
        }
        else
        {
          if (command.deletedAnnotations != null
                  && command.deletedAnnotations
                          .containsKey(annotations[a].annotationId))
          {
            Annotation[] restore = command.deletedAnnotations
                    .get(annotations[a].annotationId);

            temp = new Annotation[annotations[a].annotations.length
                    + restore.length];
            System.arraycopy(annotations[a].annotations, 0, temp, 0,
                    annotations[a].annotations.length);
            System.arraycopy(restore, 0, temp,
                    annotations[a].annotations.length, restore.length);
          }
          else
          {
            temp = annotations[a].annotations;
          }
        }
      }
      else
      {
        if (tSize != aSize || command.position < 2)
        {
          int copylen = Math.min(command.position,
                  annotations[a].annotations.length);
          if (copylen > 0)
          {
            System.arraycopy(annotations[a].annotations, 0, temp, 0,
                    copylen); // command.position);
          }

          Annotation[] deleted = new Annotation[command.number];
          if (copylen >= command.position)
          {
            copylen = Math.min(command.number,
                    annotations[a].annotations.length - command.position);
            if (copylen > 0)
            {
              System.arraycopy(annotations[a].annotations,
                      command.position, deleted, 0, copylen); // command.number);
            }
          }

          command.deletedAnnotations.put(annotations[a].annotationId,
                  deleted);
          if (annotations[a].annotations.length > command.position
                  + command.number)
          {
            System.arraycopy(annotations[a].annotations, command.position
                    + command.number, temp, command.position,
                    annotations[a].annotations.length - command.position
                            - command.number); // aSize
          }
        }
        else
        {
          int dSize = aSize - command.position;

          if (dSize > 0)
          {
            Annotation[] deleted = new Annotation[command.number];
            System.arraycopy(annotations[a].annotations, command.position,
                    deleted, 0, dSize);

            command.deletedAnnotations.put(annotations[a].annotationId,
                    deleted);

            tSize = Math.min(annotations[a].annotations.length,
                    command.position);
            temp = new Annotation[tSize];
            System.arraycopy(annotations[a].annotations, 0, temp, 0, tSize);
          }
          else
          {
            temp = annotations[a].annotations;
          }
        }
      }

      annotations[a].annotations = temp;
    }
  }

  final static void adjustFeatures(Edit command, int index, int i, int j,
          boolean insert)
  {
    SequenceI seq = command.seqs[index];
    SequenceI sequence = seq.getDatasetSequence();
    if (sequence == null)
    {
      sequence = seq;
    }

    if (insert)
    {
      if (command.editedFeatures != null
              && command.editedFeatures.containsKey(seq))
      {
        sequence.setSequenceFeatures(command.editedFeatures.get(seq));
      }

      return;
    }

    SequenceFeature[] sf = sequence.getSequenceFeatures();

    if (sf == null)
    {
      return;
    }

    SequenceFeature[] oldsf = new SequenceFeature[sf.length];

    int cSize = j - i;

    for (int s = 0; s < sf.length; s++)
    {
      SequenceFeature copy = new SequenceFeature(sf[s]);

      oldsf[s] = copy;

      if (sf[s].getEnd() < i)
      {
        continue;
      }

      if (sf[s].getBegin() > j)
      {
        sf[s].setBegin(copy.getBegin() - cSize);
        sf[s].setEnd(copy.getEnd() - cSize);
        continue;
      }

      if (sf[s].getBegin() >= i)
      {
        sf[s].setBegin(i);
      }

      if (sf[s].getEnd() < j)
      {
        sf[s].setEnd(j - 1);
      }

      sf[s].setEnd(sf[s].getEnd() - (cSize));

      if (sf[s].getBegin() > sf[s].getEnd())
      {
        sequence.deleteFeature(sf[s]);
      }
    }

    if (command.editedFeatures == null)
    {
      command.editedFeatures = new Hashtable<SequenceI, SequenceFeature[]>();
    }

    command.editedFeatures.put(seq, oldsf);

  }

  /**
   * Returns the list of edit commands wrapped by this object.
   * 
   * @return
   */
  public List<Edit> getEdits()
  {
    return this.edits;
  }

  /**
   * Returns a map whose keys are the dataset sequences, and values their
   * aligned sequences before the command edit list was applied. The aligned
   * sequences are copies, which may be updated without affecting the originals.
   * 
   * The command holds references to the aligned sequences (after editing). If
   * the command is an 'undo',then the prior state is simply the aligned state.
   * Otherwise, we have to derive the prior state by working backwards through
   * the edit list to infer the aligned sequences before editing.
   * 
   * Note: an alternative solution would be to cache the 'before' state of each
   * edit, but this would be expensive in space in the common case that the
   * original is never needed (edits are not mirrored).
   * 
   * @return
   * @throws IllegalStateException
   *           on detecting an edit command of a type that can't be unwound
   */
  public Map<SequenceI, SequenceI> priorState(boolean forUndo)
  {
    Map<SequenceI, SequenceI> result = new HashMap<SequenceI, SequenceI>();
    if (getEdits() == null)
    {
      return result;
    }
    if (forUndo)
    {
      for (Edit e : getEdits())
      {
        for (SequenceI seq : e.getSequences())
        {
          SequenceI ds = seq.getDatasetSequence();
          SequenceI preEdit = result.get(ds);
          if (preEdit == null)
          {
            preEdit = new Sequence("", seq.getSequenceAsString());
            preEdit.setDatasetSequence(ds);
            result.put(ds, preEdit);
          }
        }
      }
      return result;
    }

    /*
     * Work backwards through the edit list, deriving the sequences before each
     * was applied. The final result is the sequence set before any edits.
     */
    Iterator<Edit> edits = new ReverseListIterator<Edit>(getEdits());
    while (edits.hasNext())
    {
      Edit oldEdit = edits.next();
      Action action = oldEdit.getAction();
      int position = oldEdit.getPosition();
      int number = oldEdit.getNumber();
      final char gap = oldEdit.getGapCharacter();
      for (SequenceI seq : oldEdit.getSequences())
      {
        SequenceI ds = seq.getDatasetSequence();
        SequenceI preEdit = result.get(ds);
        if (preEdit == null)
        {
          preEdit = new Sequence("", seq.getSequenceAsString());
          preEdit.setDatasetSequence(ds);
          result.put(ds, preEdit);
        }
        /*
         * 'Undo' this edit action on the sequence (updating the value in the
         * map).
         */
        if (ds != null)
        {
          if (action == Action.DELETE_GAP)
          {
            preEdit.setSequence(new String(StringUtils.insertCharAt(
                    preEdit.getSequence(), position, number, gap)));
          }
          else if (action == Action.INSERT_GAP)
          {
            preEdit.setSequence(new String(StringUtils.deleteChars(
                    preEdit.getSequence(), position, position + number)));
          }
          else
          {
            System.err.println("Can't undo edit action " + action);
            // throw new IllegalStateException("Can't undo edit action " +
            // action);
          }
        }
      }
    }
    return result;
  }

  public class Edit
  {
    public SequenceI[] oldds;

    boolean fullAlignmentHeight = false;

    Hashtable<SequenceI, AlignmentAnnotation[]> deletedAnnotationRows;

    Hashtable<String, Annotation[]> deletedAnnotations;

    Hashtable<SequenceI, SequenceFeature[]> editedFeatures;

    AlignmentI al;

    Action command;

    char[][] string;

    SequenceI[] seqs;

    int[] alIndex;

    int position, number;

    char gapChar;

    public Edit(Action command, SequenceI[] seqs, int position, int number,
            char gapChar)
    {
      this.command = command;
      this.seqs = seqs;
      this.position = position;
      this.number = number;
      this.gapChar = gapChar;
    }

    Edit(Action command, SequenceI[] seqs, int position, int number,
            AlignmentI al)
    {
      this.gapChar = al.getGapCharacter();
      this.command = command;
      this.seqs = seqs;
      this.position = position;
      this.number = number;
      this.al = al;

      alIndex = new int[seqs.length];
      for (int i = 0; i < seqs.length; i++)
      {
        alIndex[i] = al.findIndex(seqs[i]);
      }

      fullAlignmentHeight = (al.getHeight() == seqs.length);
    }

    Edit(Action command, SequenceI[] seqs, int position, int number,
            AlignmentI al, String replace)
    {
      this.command = command;
      this.seqs = seqs;
      this.position = position;
      this.number = number;
      this.al = al;
      this.gapChar = al.getGapCharacter();
      string = new char[seqs.length][];
      for (int i = 0; i < seqs.length; i++)
      {
        string[i] = replace.toCharArray();
      }

      fullAlignmentHeight = (al.getHeight() == seqs.length);
    }

    public SequenceI[] getSequences()
    {
      return seqs;
    }

    public int getPosition()
    {
      return position;
    }

    public Action getAction()
    {
      return command;
    }

    public int getNumber()
    {
      return number;
    }

    public char getGapCharacter()
    {
      return gapChar;
    }
  }

  /**
   * Returns an iterator over the list of edit commands which traverses the list
   * either forwards or backwards.
   * 
   * @param forwards
   * @return
   */
  public Iterator<Edit> getEditIterator(boolean forwards)
  {
    if (forwards)
    {
      return getEdits().iterator();
    }
    else
    {
      return new ReverseListIterator<Edit>(getEdits());
    }
  }
}
