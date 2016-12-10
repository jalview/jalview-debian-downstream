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
package jalview.ws.jws2;

import jalview.analysis.AlignSeq;
import jalview.analysis.SeqsetUtils;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AnnotatedCollectionI;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.IProgressIndicator;
import jalview.workers.AlignCalcWorker;
import jalview.ws.jws2.dm.AAConSettings;
import jalview.ws.jws2.dm.JabaWsParamSet;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.WsParamSetI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import compbio.data.sequence.FastaSequence;
import compbio.metadata.Argument;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobStatus;
import compbio.metadata.JobSubmissionException;
import compbio.metadata.Option;
import compbio.metadata.ResultNotAvailableException;

public abstract class AbstractJabaCalcWorker extends AlignCalcWorker
{

  protected Jws2Instance service;

  protected WsParamSetI preset;

  protected List<Argument> arguments;

  protected IProgressIndicator guiProgress;

  protected boolean submitGaps = true;

  /**
   * Recover any existing parameters for this service
   */
  protected void initViewportParams()
  {
    if (getCalcId() != null)
    {
      ((jalview.gui.AlignViewport) alignViewport).setCalcIdSettingsFor(
              getCalcId(),
              new AAConSettings(true, service, this.preset,
                      (arguments != null) ? JabaParamStore
                              .getJwsArgsfromJaba(arguments) : null), true);
    }
  }

  /**
   * 
   * @return null or a string used to recover all annotation generated by this
   *         worker
   */
  public abstract String getCalcId();

  public WsParamSetI getPreset()
  {
    return preset;
  }

  public List<Argument> getArguments()
  {
    return arguments;
  }

  /**
   * reconfigure and restart the AAConClient. This method will spawn a new
   * thread that will wait until any current jobs are finished, modify the
   * parameters and restart the conservation calculation with the new values.
   * 
   * @param newpreset
   * @param newarguments
   */
  public void updateParameters(final WsParamSetI newpreset,
          final List<Argument> newarguments)
  {
    preset = newpreset;
    arguments = newarguments;
    calcMan.startWorker(this);
    initViewportParams();
  }

  public List<Option> getJabaArguments()
  {
    List<Option> newargs = new ArrayList<Option>();
    if (preset != null && preset instanceof JabaWsParamSet)
    {
      newargs.addAll(((JabaWsParamSet) preset).getjabaArguments());
    }
    if (arguments != null && arguments.size() > 0)
    {
      for (Argument rg : arguments)
      {
        if (Option.class.isAssignableFrom(rg.getClass()))
        {
          newargs.add((Option) rg);
        }
      }
    }
    return newargs;
  }

  protected boolean alignedSeqs = true;

  protected boolean nucleotidesAllowed = false;

  protected boolean proteinAllowed = false;

  /**
   * record sequences for mapping result back to afterwards
   */
  protected boolean bySequence = false;

  protected Map<String, SequenceI> seqNames;

  protected boolean[] gapMap;

  int realw;

  protected int start;

  int end;

  public AbstractJabaCalcWorker(AlignViewportI alignViewport,
          AlignmentViewPanel alignPanel)
  {
    super(alignViewport, alignPanel);
  }

  public AbstractJabaCalcWorker(Jws2Instance service,
          AlignFrame alignFrame, WsParamSetI preset, List<Argument> paramset)
  {
    this(alignFrame.getCurrentView(), alignFrame.alignPanel);
    this.guiProgress = alignFrame;
    this.preset = preset;
    this.arguments = paramset;
    this.service = service;
  }

  /**
   * 
   * @return true if the submission thread should attempt to submit data
   */
  abstract boolean hasService();

  volatile String rslt = "JOB NOT DEFINED";

  @Override
  public void run()
  {
    if (!hasService())
    {
      return;
    }
    long progressId = -1;

    int serverErrorsLeft = 3;

    StringBuffer msg = new StringBuffer();
    try
    {
      if (checkDone())
      {
        return;
      }
      List<compbio.data.sequence.FastaSequence> seqs = getInputSequences(
              alignViewport.getAlignment(),
              bySequence ? alignViewport.getSelectionGroup() : null);

      if (seqs == null || !checkValidInputSeqs(true, seqs))
      {
        calcMan.workerComplete(this);
        return;
      }

      AlignmentAnnotation[] aa = alignViewport.getAlignment()
              .getAlignmentAnnotation();
      if (guiProgress != null)
      {
        guiProgress.setProgressBar("JABA " + getServiceActionText(),
                progressId = System.currentTimeMillis());
      }
      rslt = submitToService(seqs);

      boolean finished = false;
      long rpos = 0;
      do
      {
        JobStatus status = getJobStatus(rslt);
        if (status.equals(JobStatus.FINISHED))
        {
          finished = true;
        }
        if (calcMan.isPending(this) && isInteractiveUpdate())
        {
          finished = true;
          // cancel this job and yield to the new job
          try
          {
            if (cancelJob(rslt))
            {
              System.err.println("Cancelled AACon job: " + rslt);
            }
            else
            {
              System.err.println("FAILED TO CANCEL AACon job: " + rslt);
            }

          } catch (Exception x)
          {

          }
          rslt = "CANCELLED JOB";
          return;
        }
        long cpos;
        ChunkHolder stats = null;
        do
        {
          cpos = rpos;
          boolean retry = false;
          do
          {
            try
            {
              stats = pullExecStatistics(rslt, rpos);
            } catch (Exception x)
            {

              if (x.getMessage().contains(
                      "Position in a file could not be negative!"))
              {
                // squash index out of bounds exception- seems to happen for
                // disorder predictors which don't (apparently) produce any
                // progress information and JABA server throws an exception
                // because progress length is -1.
                stats = null;
              }
              else
              {
                if (--serverErrorsLeft > 0)
                {
                  retry = true;
                  try
                  {
                    Thread.sleep(200);
                  } catch (InterruptedException q)
                  {
                  }
                  ;
                }
                else
                {
                  throw x;
                }
              }
            }
          } while (retry);
          if (stats != null)
          {
            System.out.print(stats.getChunk());
            msg.append(stats);
            rpos = stats.getNextPosition();
          }
        } while (stats != null && rpos > cpos);

        if (!finished && status.equals(JobStatus.FAILED))
        {
          try
          {
            Thread.sleep(200);
          } catch (InterruptedException x)
          {
          }
          ;
        }
      } while (!finished);
      if (serverErrorsLeft > 0)
      {
        try
        {
          Thread.sleep(200);
        } catch (InterruptedException x)
        {
        }
        if (collectAnnotationResultsFor(rslt))
        {
          jalview.bin.Cache.log
                  .debug("Updating result annotation from Job " + rslt
                          + " at " + service.getUri());
          updateResultAnnotation(true);
          ap.adjustAnnotationHeight();
        }
      }
    }

    catch (JobSubmissionException x)
    {

      System.err.println("submission error with " + getServiceActionText()
              + " :");
      x.printStackTrace();
      calcMan.disableWorker(this);
    } catch (ResultNotAvailableException x)
    {
      System.err.println("collection error:\nJob ID: " + rslt);
      x.printStackTrace();
      calcMan.disableWorker(this);

    } catch (OutOfMemoryError error)
    {
      calcMan.disableWorker(this);

      // consensus = null;
      // hconsensus = null;
      ap.raiseOOMWarning(getServiceActionText(), error);
    } catch (Exception x)
    {
      calcMan.disableWorker(this);

      // consensus = null;
      // hconsensus = null;
      System.err
              .println("Blacklisting worker due to unexpected exception:");
      x.printStackTrace();
    } finally
    {

      calcMan.workerComplete(this);
      if (ap != null)
      {
        calcMan.workerComplete(this);
        if (guiProgress != null && progressId != -1)
        {
          guiProgress.setProgressBar("", progressId);
        }
        ap.paintAlignment(true);
      }
      if (msg.length() > 0)
      {
        // TODO: stash message somewhere in annotation or alignment view.
        // code below shows result in a text box popup
        /*
         * jalview.gui.CutAndPasteTransfer cap = new
         * jalview.gui.CutAndPasteTransfer(); cap.setText(msg.toString());
         * jalview.gui.Desktop.addInternalFrame(cap,
         * "Job Status for "+getServiceActionText(), 600, 400);
         */
      }
    }

  }

  /**
   * validate input for dynamic/non-dynamic update context
   * 
   * @param dynamic
   * @param seqs
   * @return true if input is valid
   */
  abstract boolean checkValidInputSeqs(boolean dynamic,
          List<FastaSequence> seqs);

  abstract String submitToService(
          List<compbio.data.sequence.FastaSequence> seqs)
          throws JobSubmissionException;

  abstract boolean cancelJob(String rslt) throws Exception;

  abstract JobStatus getJobStatus(String rslt) throws Exception;

  abstract ChunkHolder pullExecStatistics(String rslt, long rpos);

  abstract boolean collectAnnotationResultsFor(String rslt)
          throws ResultNotAvailableException;

  public void cancelCurrentJob()
  {
    try
    {
      String id = rslt;
      if (cancelJob(rslt))
      {
        System.err.println("Cancelled job " + id);
      }
      else
      {
        System.err.println("Job " + id + " couldn't be cancelled.");
      }
    } catch (Exception q)
    {
      q.printStackTrace();
    }
  }

  /**
   * Interactive updating. Analysis calculations that work on the currently
   * displayed alignment data should cancel existing jobs when the input data
   * has changed.
   * 
   * @return true if a running job should be cancelled because new input data is
   *         available for analysis
   */
  abstract boolean isInteractiveUpdate();

  public List<FastaSequence> getInputSequences(AlignmentI alignment,
          AnnotatedCollectionI inputSeqs)
  {
    if (alignment == null || alignment.getWidth() <= 0
            || alignment.getSequences() == null || alignment.isNucleotide() ? !nucleotidesAllowed
            : !proteinAllowed)
    {
      return null;
    }
    if (inputSeqs == null || inputSeqs.getWidth() <= 0
            || inputSeqs.getSequences() == null
            || inputSeqs.getSequences().size() < 1)
    {
      inputSeqs = alignment;
    }

    List<compbio.data.sequence.FastaSequence> seqs = new ArrayList<compbio.data.sequence.FastaSequence>();

    int minlen = 10;
    int ln = -1;
    if (bySequence)
    {
      seqNames = new HashMap<String, SequenceI>();
    }
    gapMap = new boolean[0];
    start = inputSeqs.getStartRes();
    end = inputSeqs.getEndRes();

    for (SequenceI sq : ((List<SequenceI>) inputSeqs.getSequences()))
    {
      if (bySequence ? sq.findPosition(end + 1)
              - sq.findPosition(start + 1) > minlen - 1 : sq.getEnd()
              - sq.getStart() > minlen - 1)
      {
        String newname = SeqsetUtils.unique_name(seqs.size() + 1);
        // make new input sequence with or without gaps
        if (seqNames != null)
        {
          seqNames.put(newname, sq);
        }
        FastaSequence seq;
        if (submitGaps)
        {
          seqs.add(seq = new compbio.data.sequence.FastaSequence(newname,
                  sq.getSequenceAsString()));
          if (gapMap == null || gapMap.length < seq.getSequence().length())
          {
            boolean[] tg = gapMap;
            gapMap = new boolean[seq.getLength()];
            System.arraycopy(tg, 0, gapMap, 0, tg.length);
            for (int p = tg.length; p < gapMap.length; p++)
            {
              gapMap[p] = false; // init as a gap
            }
          }
          for (int apos : sq.gapMap())
          {
            gapMap[apos] = true; // aligned.
          }
        }
        else
        {
          seqs.add(seq = new compbio.data.sequence.FastaSequence(newname,
                  AlignSeq.extractGaps(jalview.util.Comparison.GapChars,
                          sq.getSequenceAsString(start, end + 1))));
        }
        if (seq.getSequence().length() > ln)
        {
          ln = seq.getSequence().length();
        }
      }
    }
    if (alignedSeqs && submitGaps)
    {
      realw = 0;
      for (int i = 0; i < gapMap.length; i++)
      {
        if (gapMap[i])
        {
          realw++;
        }
      }
      // try real hard to return something submittable
      // TODO: some of AAcon measures need a minimum of two or three amino
      // acids at each position, and AAcon doesn't gracefully degrade.
      for (int p = 0; p < seqs.size(); p++)
      {
        FastaSequence sq = seqs.get(p);
        int l = sq.getSequence().length();
        // strip gapped columns
        char[] padded = new char[realw], orig = sq.getSequence()
                .toCharArray();
        for (int i = 0, pp = 0; i < realw; pp++)
        {
          if (gapMap[pp])
          {
            if (orig.length > pp)
            {
              padded[i++] = orig[pp];
            }
            else
            {
              padded[i++] = '-';
            }
          }
        }
        seqs.set(p, new compbio.data.sequence.FastaSequence(sq.getId(),
                new String(padded)));
      }
    }
    return seqs;
  }

  @Override
  public void updateAnnotation()
  {
    updateResultAnnotation(false);
  }

  public abstract void updateResultAnnotation(boolean immediate);

  public abstract String getServiceActionText();

  /**
   * notify manager that we have started, and wait for a free calculation slot
   * 
   * @return true if slot is obtained and work still valid, false if another
   *         thread has done our work for us.
   */
  protected boolean checkDone()
  {
    calcMan.notifyStart(this);
    ap.paintAlignment(false);
    while (!calcMan.notifyWorking(this))
    {
      if (calcMan.isWorking(this))
      {
        return true;
      }
      try
      {
        if (ap != null)
        {
          ap.paintAlignment(false);
        }

        Thread.sleep(200);
      } catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
    if (alignViewport.isClosed())
    {
      abortAndDestroy();
      return true;
    }
    return false;
  }

  protected void updateOurAnnots(List<AlignmentAnnotation> ourAnnot)
  {
    List<AlignmentAnnotation> our = ourAnnots;
    ourAnnots = ourAnnot;
    AlignmentI alignment = alignViewport.getAlignment();
    if (our != null)
    {
      if (our.size() > 0)
      {
        for (AlignmentAnnotation an : our)
        {
          if (!ourAnnots.contains(an))
          {
            // remove the old annotation
            alignment.deleteAnnotation(an);
          }
        }
      }
      our.clear();

      ap.adjustAnnotationHeight();
    }
  }

}
