/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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
import jalview.bin.Cache;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentOrder;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.HiddenColumns;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.SplitFrame;
import jalview.gui.WebserviceInfo;
import jalview.util.MessageManager;
import jalview.ws.AWsJob;
import jalview.ws.JobStateSummary;
import jalview.ws.WSClientI;
import jalview.ws.jws2.dm.JabaWsParamSet;
import jalview.ws.params.WsParamSetI;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JInternalFrame;

import compbio.data.msa.MsaWS;
import compbio.metadata.Argument;
import compbio.metadata.ChunkHolder;
import compbio.metadata.JobStatus;
import compbio.metadata.Preset;

class MsaWSThread extends AWS2Thread implements WSClientI
{
  boolean submitGaps = false; // pass sequences including gaps to alignment

  // service

  boolean preserveOrder = true; // and always store and recover sequence

  // order

  class MsaWSJob extends JWs2Job
  {
    long lastChunk = 0;

    WsParamSetI preset = null;

    List<Argument> arguments = null;

    /**
     * input
     */
    ArrayList<compbio.data.sequence.FastaSequence> seqs = new ArrayList<compbio.data.sequence.FastaSequence>();

    /**
     * output
     */
    compbio.data.sequence.Alignment alignment;

    // set if the job didn't get run - then the input is simply returned to the
    // user
    private boolean returnInput = false;

    /**
     * MsaWSJob
     * 
     * @param jobNum
     *          int
     * @param jobId
     *          String
     */
    public MsaWSJob(int jobNum, SequenceI[] inSeqs)
    {
      this.jobnum = jobNum;
      if (!prepareInput(inSeqs, 2))
      {
        submitted = true;
        subjobComplete = true;
        returnInput = true;
      }

    }

    Hashtable<String, Map> SeqNames = new Hashtable();

    Vector<String[]> emptySeqs = new Vector();

    /**
     * prepare input sequences for MsaWS service
     * 
     * @param seqs
     *          jalview sequences to be prepared
     * @param minlen
     *          minimum number of residues required for this MsaWS service
     * @return true if seqs contains sequences to be submitted to service.
     */
    // TODO: return compbio.seqs list or nothing to indicate validity.
    private boolean prepareInput(SequenceI[] seqs, int minlen)
    {
      int nseqs = 0;
      if (minlen < 0)
      {
        throw new Error(MessageManager.getString(
                "error.implementation_error_minlen_must_be_greater_zero"));
      }
      for (int i = 0; i < seqs.length; i++)
      {
        if (seqs[i].getEnd() - seqs[i].getStart() > minlen - 1)
        {
          nseqs++;
        }
      }
      boolean valid = nseqs > 1; // need at least two seqs
      compbio.data.sequence.FastaSequence seq;
      for (int i = 0, n = 0; i < seqs.length; i++)
      {

        String newname = jalview.analysis.SeqsetUtils.unique_name(i); // same
        // for
        // any
        // subjob
        SeqNames.put(newname,
                jalview.analysis.SeqsetUtils.SeqCharacterHash(seqs[i]));
        if (valid && seqs[i].getEnd() - seqs[i].getStart() > minlen - 1)
        {
          // make new input sequence with or without gaps
          seq = new compbio.data.sequence.FastaSequence(newname,
                  (submitGaps) ? seqs[i].getSequenceAsString()
                          : AlignSeq.extractGaps(
                                  jalview.util.Comparison.GapChars,
                                  seqs[i].getSequenceAsString()));
          this.seqs.add(seq);
        }
        else
        {
          String empty = null;
          if (seqs[i].getEnd() >= seqs[i].getStart())
          {
            empty = (submitGaps) ? seqs[i].getSequenceAsString()
                    : AlignSeq.extractGaps(jalview.util.Comparison.GapChars,
                            seqs[i].getSequenceAsString());
          }
          emptySeqs.add(new String[] { newname, empty });
        }
      }
      return valid;
    }

    /**
     * 
     * @return true if getAlignment will return a valid alignment result.
     */
    @Override
    public boolean hasResults()
    {
      if (subjobComplete && isFinished() && (alignment != null
              || (emptySeqs != null && emptySeqs.size() > 0)))
      {
        return true;
      }
      return false;
    }

    /**
     * 
     * get the alignment including any empty sequences in the original order
     * with original ids. Caller must access the alignment.getMetadata() object
     * to annotate the final result passsed to the user.
     * 
     * @return { SequenceI[], AlignmentOrder }
     */
    public Object[] getAlignment()
    {
      // is this a generic subjob or a Jws2 specific Object[] return signature
      if (hasResults())
      {
        SequenceI[] alseqs = null;
        char alseq_gapchar = '-';
        int alseq_l = 0;
        if (alignment.getSequences().size() > 0)
        {
          alseqs = new SequenceI[alignment.getSequences().size()];
          for (compbio.data.sequence.FastaSequence seq : alignment
                  .getSequences())
          {
            alseqs[alseq_l++] = new Sequence(seq.getId(),
                    seq.getSequence());
          }
          alseq_gapchar = alignment.getMetadata().getGapchar();

        }
        // add in the empty seqs.
        if (emptySeqs.size() > 0)
        {
          SequenceI[] t_alseqs = new SequenceI[alseq_l + emptySeqs.size()];
          // get width
          int i, w = 0;
          if (alseq_l > 0)
          {
            for (i = 0, w = alseqs[0].getLength(); i < alseq_l; i++)
            {
              if (w < alseqs[i].getLength())
              {
                w = alseqs[i].getLength();
              }
              t_alseqs[i] = alseqs[i];
              alseqs[i] = null;
            }
          }
          // check that aligned width is at least as wide as emptySeqs width.
          int ow = w, nw = w;
          for (i = 0, w = emptySeqs.size(); i < w; i++)
          {
            String[] es = emptySeqs.get(i);
            if (es != null && es[1] != null)
            {
              int sw = es[1].length();
              if (nw < sw)
              {
                nw = sw;
              }
            }
          }
          // make a gapped string.
          StringBuffer insbuff = new StringBuffer(w);
          for (i = 0; i < nw; i++)
          {
            insbuff.append(alseq_gapchar);
          }
          if (ow < nw)
          {
            for (i = 0; i < alseq_l; i++)
            {
              int sw = t_alseqs[i].getLength();
              if (nw > sw)
              {
                // pad at end
                alseqs[i].setSequence(t_alseqs[i].getSequenceAsString()
                        + insbuff.substring(0, sw - nw));
              }
            }
          }
          for (i = 0, w = emptySeqs.size(); i < w; i++)
          {
            String[] es = emptySeqs.get(i);
            if (es[1] == null)
            {
              t_alseqs[i + alseq_l] = new jalview.datamodel.Sequence(es[0],
                      insbuff.toString(), 1, 0);
            }
            else
            {
              if (es[1].length() < nw)
              {
                t_alseqs[i + alseq_l] = new jalview.datamodel.Sequence(
                        es[0],
                        es[1] + insbuff.substring(0, nw - es[1].length()),
                        1, 1 + es[1].length());
              }
              else
              {
                t_alseqs[i + alseq_l] = new jalview.datamodel.Sequence(
                        es[0], es[1]);
              }
            }
          }
          alseqs = t_alseqs;
        }
        AlignmentOrder msaorder = new AlignmentOrder(alseqs);
        // always recover the order - makes parseResult()'s life easier.
        jalview.analysis.AlignmentSorter.recoverOrder(alseqs);
        // account for any missing sequences
        jalview.analysis.SeqsetUtils.deuniquify(SeqNames, alseqs);
        return new Object[] { alseqs, msaorder };
      }
      return null;
    }

    /**
     * mark subjob as cancelled and set result object appropriatly
     */
    void cancel()
    {
      cancelled = true;
      subjobComplete = true;
      alignment = null;
    }

    /**
     * 
     * @return boolean true if job can be submitted.
     */
    @Override
    public boolean hasValidInput()
    {
      // TODO: get attributes for this MsaWS instance to check if it can do two
      // sequence alignment.
      if (seqs != null && seqs.size() >= 2) // two or more sequences is valid ?
      {
        return true;
      }
      return false;
    }

    StringBuffer jobProgress = new StringBuffer();

    public void setStatus(String string)
    {
      jobProgress.setLength(0);
      jobProgress.append(string);
    }

    @Override
    public String getStatus()
    {
      return jobProgress.toString();
    }

    @Override
    public boolean hasStatus()
    {
      return jobProgress != null;
    }

    /**
     * @return the lastChunk
     */
    public long getLastChunk()
    {
      return lastChunk;
    }

    /**
     * @param lastChunk
     *          the lastChunk to set
     */
    public void setLastChunk(long lastChunk)
    {
      this.lastChunk = lastChunk;
    }

    String alignmentProgram = null;

    public String getAlignmentProgram()
    {
      return alignmentProgram;
    }

    public boolean hasArguments()
    {
      return (arguments != null && arguments.size() > 0)
              || (preset != null && preset instanceof JabaWsParamSet);
    }

    public List<Argument> getJabaArguments()
    {
      List<Argument> newargs = new ArrayList<Argument>();
      if (preset != null && preset instanceof JabaWsParamSet)
      {
        newargs.addAll(((JabaWsParamSet) preset).getjabaArguments());
      }
      if (arguments != null && arguments.size() > 0)
      {
        newargs.addAll(arguments);
      }
      return newargs;
    }

    /**
     * add a progess header to status string containing presets/args used
     */
    public void addInitialStatus()
    {
      if (preset != null)
      {
        jobProgress.append("Using "
                + (preset instanceof JabaPreset ? "Server" : "User")
                + "Preset: " + preset.getName());
        if (preset instanceof JabaWsParamSet)
        {
          for (Argument opt : ((JabaWsParamSet) preset).getjabaArguments())
          {
            jobProgress.append(
                    opt.getName() + " " + opt.getDefaultValue() + "\n");
          }
        }
      }
      if (arguments != null && arguments.size() > 0)
      {
        jobProgress.append("With custom parameters : \n");
        // merge arguments with preset's own arguments.
        for (Argument opt : arguments)
        {
          jobProgress.append(
                  opt.getName() + " " + opt.getDefaultValue() + "\n");
        }
      }
      jobProgress.append("\nJob Output:\n");
    }

    public boolean isPresetJob()
    {
      return preset != null && preset instanceof JabaPreset;
    }

    public Preset getServerPreset()
    {
      return (isPresetJob()) ? ((JabaPreset) preset).p : null;
    }
  }

  String alTitle; // name which will be used to form new alignment window.

  AlignmentI dataset; // dataset to which the new alignment will be

  // associated.

  @SuppressWarnings("unchecked")
  MsaWS server = null;

  /**
   * set basic options for this (group) of Msa jobs
   * 
   * @param subgaps
   *          boolean
   * @param presorder
   *          boolean
   */
  private MsaWSThread(MsaWS server, String wsUrl, WebserviceInfo wsinfo,
          jalview.gui.AlignFrame alFrame, AlignmentView alview,
          String wsname, boolean subgaps, boolean presorder)
  {
    super(alFrame, wsinfo, alview, wsname, wsUrl);
    this.server = server;
    this.submitGaps = subgaps;
    this.preserveOrder = presorder;
  }

  /**
   * create one or more Msa jobs to align visible seuqences in _msa
   * 
   * @param title
   *          String
   * @param _msa
   *          AlignmentView
   * @param subgaps
   *          boolean
   * @param presorder
   *          boolean
   * @param seqset
   *          Alignment
   */
  MsaWSThread(MsaWS server2, WsParamSetI preset, List<Argument> paramset,
          String wsUrl, WebserviceInfo wsinfo,
          jalview.gui.AlignFrame alFrame, String wsname, String title,
          AlignmentView _msa, boolean subgaps, boolean presorder,
          AlignmentI seqset)
  {
    this(server2, wsUrl, wsinfo, alFrame, _msa, wsname, subgaps, presorder);
    OutputHeader = wsInfo.getProgressText();
    alTitle = title;
    dataset = seqset;

    SequenceI[][] conmsa = _msa.getVisibleContigs('-');
    if (conmsa != null)
    {
      int nvalid = 0, njobs = conmsa.length;
      jobs = new MsaWSJob[njobs];
      for (int j = 0; j < njobs; j++)
      {
        if (j != 0)
        {
          jobs[j] = new MsaWSJob(wsinfo.addJobPane(), conmsa[j]);
        }
        else
        {
          jobs[j] = new MsaWSJob(0, conmsa[j]);
        }
        if (((MsaWSJob) jobs[j]).hasValidInput())
        {
          nvalid++;
        }
        ((MsaWSJob) jobs[j]).preset = preset;
        ((MsaWSJob) jobs[j]).arguments = paramset;
        ((MsaWSJob) jobs[j]).alignmentProgram = wsname;
        if (njobs > 0)
        {
          wsinfo.setProgressName("region " + jobs[j].getJobnum(),
                  jobs[j].getJobnum());
        }
        wsinfo.setProgressText(jobs[j].getJobnum(), OutputHeader);
      }
      validInput = nvalid > 0;
    }
  }

  boolean validInput = false;

  /**
   * 
   * @return true if the thread will perform a calculation
   */
  public boolean hasValidInput()
  {
    return validInput;
  }

  @Override
  public boolean isCancellable()
  {
    return true;
  }

  @Override
  public void cancelJob()
  {
    if (!jobComplete && jobs != null)
    {
      boolean cancelled = true;
      for (int job = 0; job < jobs.length; job++)
      {
        if (jobs[job].isSubmitted() && !jobs[job].isSubjobComplete())
        {
          String cancelledMessage = "";
          try
          {
            boolean cancelledJob = server.cancelJob(jobs[job].getJobId());
            if (true) // cancelledJob || true)
            {
              // CANCELLED_JOB
              // if the Jaba server indicates the job can't be cancelled, its
              // because its running on the server's local execution engine
              // so we just close the window anyway.
              cancelledMessage = "Job cancelled.";
              ((MsaWSJob) jobs[job]).cancel(); // TODO: refactor to avoid this
                                               // ugliness -
              wsInfo.setStatus(jobs[job].getJobnum(),
                      WebserviceInfo.STATE_CANCELLED_OK);
            }
            else
            {
              // VALID UNSTOPPABLE JOB
              cancelledMessage += "Server cannot cancel this job. just close the window.\n";
              cancelled = false;
              // wsInfo.setStatus(jobs[job].jobnum,
              // WebserviceInfo.STATE_RUNNING);
            }
          } catch (Exception exc)
          {
            cancelledMessage += ("\nProblems cancelling the job : Exception received...\n"
                    + exc + "\n");
            Cache.log.warn(
                    "Exception whilst cancelling " + jobs[job].getJobId(),
                    exc);
          }
          wsInfo.setProgressText(jobs[job].getJobnum(),
                  OutputHeader + cancelledMessage + "\n");
        }
        else
        {
          // if we hadn't submitted then just mark the job as cancelled.
          jobs[job].setSubjobComplete(true);
          wsInfo.setStatus(jobs[job].getJobnum(),
                  WebserviceInfo.STATE_CANCELLED_OK);

        }
      }
      if (cancelled)
      {
        wsInfo.setStatus(WebserviceInfo.STATE_CANCELLED_OK);
        jobComplete = true;
      }
      this.interrupt(); // kick thread to update job states.
    }
    else
    {
      if (!jobComplete)
      {
        wsInfo.setProgressText(OutputHeader
                + "Server cannot cancel this job because it has not been submitted properly. just close the window.\n");
      }
    }
  }

  @Override
  public void pollJob(AWsJob job) throws Exception
  {
    // TODO: investigate if we still need to cast here in J1.6
    MsaWSJob j = ((MsaWSJob) job);
    // this is standard code, but since the interface doesn't comprise of a
    // basic one that implements (getJobStatus, pullExecStatistics) we have to
    // repeat the code for all jw2s services.
    j.setjobStatus(server.getJobStatus(job.getJobId()));
    updateJobProgress(j);
  }

  /**
   * 
   * @param j
   * @return true if more job progress data was available
   * @throws Exception
   */
  protected boolean updateJobProgress(MsaWSJob j) throws Exception
  {
    StringBuffer response = j.jobProgress;
    long lastchunk = j.getLastChunk();
    boolean changed = false;
    do
    {
      j.setLastChunk(lastchunk);
      ChunkHolder chunk = server.pullExecStatistics(j.getJobId(),
              lastchunk);
      if (chunk != null)
      {
        changed |= chunk.getChunk().length() > 0;
        response.append(chunk.getChunk());
        lastchunk = chunk.getNextPosition();
        try
        {
          Thread.sleep(50);
        } catch (InterruptedException x)
        {
        }
        ;
      }
      ;
    } while (lastchunk >= 0 && j.getLastChunk() != lastchunk);
    return changed;
  }

  @Override
  public void StartJob(AWsJob job)
  {
    Exception lex = null;
    // boiler plate template
    if (!(job instanceof MsaWSJob))
    {
      throw new Error(MessageManager.formatMessage(
              "error.implementation_error_msawbjob_called", new String[]
              { job.getClass().toString() }));
    }
    MsaWSJob j = (MsaWSJob) job;
    if (j.isSubmitted())
    {
      if (Cache.log.isDebugEnabled())
      {
        Cache.log.debug(
                "Tried to submit an already submitted job " + j.getJobId());
      }
      return;
    }
    // end boilerplate

    if (j.seqs == null || j.seqs.size() == 0)
    {
      // special case - selection consisted entirely of empty sequences...
      j.setjobStatus(JobStatus.FINISHED);
      j.setStatus(MessageManager.getString("label.empty_alignment_job"));
    }
    try
    {
      j.addInitialStatus(); // list the presets/parameters used for the job in
                            // status
      if (j.isPresetJob())
      {
        j.setJobId(server.presetAlign(j.seqs, j.getServerPreset()));
      }
      else if (j.hasArguments())
      {
        j.setJobId(server.customAlign(j.seqs, j.getJabaArguments()));
      }
      else
      {
        j.setJobId(server.align(j.seqs));
      }

      if (j.getJobId() != null)
      {
        j.setSubmitted(true);
        j.setSubjobComplete(false);
        // System.out.println(WsURL + " Job Id '" + jobId + "'");
        return;
      }
      else
      {
        throw new Exception(MessageManager.formatMessage(
                "exception.web_service_returned_null_try_later",
                new String[]
                { WsUrl }));
      }
    } catch (compbio.metadata.UnsupportedRuntimeException _lex)
    {
      lex = _lex;
      wsInfo.appendProgressText(MessageManager.formatMessage(
              "info.job_couldnt_be_run_server_doesnt_support_program",
              new String[]
              { _lex.getMessage() }));
      wsInfo.warnUser(_lex.getMessage(),
              MessageManager.getString("warn.service_not_supported"));
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
      wsInfo.setStatus(j.getJobnum(),
              WebserviceInfo.STATE_STOPPED_SERVERERROR);
    } catch (compbio.metadata.LimitExceededException _lex)
    {
      lex = _lex;
      wsInfo.appendProgressText(MessageManager.formatMessage(
              "info.job_couldnt_be_run_exceeded_hard_limit", new String[]
              { _lex.getMessage() }));
      wsInfo.warnUser(_lex.getMessage(),
              MessageManager.getString("warn.input_is_too_big"));
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
      wsInfo.setStatus(j.getJobnum(), WebserviceInfo.STATE_STOPPED_ERROR);
    } catch (compbio.metadata.WrongParameterException _lex)
    {
      lex = _lex;
      wsInfo.warnUser(_lex.getMessage(),
              MessageManager.getString("warn.invalid_job_param_set"));
      wsInfo.appendProgressText(MessageManager.formatMessage(
              "info.job_couldnt_be_run_incorrect_param_setting",
              new String[]
              { _lex.getMessage() }));
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
      wsInfo.setStatus(j.getJobnum(), WebserviceInfo.STATE_STOPPED_ERROR);
    } catch (Error e)
    {
      // For unexpected errors
      System.err.println(WebServiceName
              + "Client: Failed to submit the sequences for alignment (probably a server side problem)\n"
              + "When contacting Server:" + WsUrl + "\n");
      e.printStackTrace(System.err);
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
      wsInfo.setStatus(j.getJobnum(),
              WebserviceInfo.STATE_STOPPED_SERVERERROR);
    } catch (Exception e)
    {
      // For unexpected errors
      System.err.println(WebServiceName
              + "Client: Failed to submit the sequences for alignment (probably a server side problem)\n"
              + "When contacting Server:" + WsUrl + "\n");
      e.printStackTrace(System.err);
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_SERVERERROR);
      wsInfo.setStatus(j.getJobnum(),
              WebserviceInfo.STATE_STOPPED_SERVERERROR);
    } finally
    {
      if (!j.isSubmitted())
      {
        // Boilerplate code here
        // TODO: JBPNote catch timeout or other fault types explicitly

        j.setAllowedServerExceptions(0);
        wsInfo.appendProgressText(j.getJobnum(), MessageManager.getString(
                "info.failed_to_submit_sequences_for_alignment"));
      }
    }
  }

  @Override
  public void parseResult()
  {
    long progbar = System.currentTimeMillis();
    wsInfo.setProgressBar(
            MessageManager.getString("status.collecting_job_results"),
            progbar);
    int results = 0; // number of result sets received
    JobStateSummary finalState = new JobStateSummary();
    try
    {
      for (int j = 0; j < jobs.length; j++)
      {
        MsaWSJob msjob = ((MsaWSJob) jobs[j]);
        if (jobs[j].isFinished() && msjob.alignment == null)
        {
          int nunchanged = 3, nexcept = 3;
          boolean jpchanged = false, jpex = false;
          do
          {
            try
            {
              jpchanged = updateJobProgress(msjob);
              jpex = false;
              if (jpchanged)
              {
                nexcept = 3;
              }
            } catch (Exception e)
            {

              Cache.log.warn(
                      "Exception when retrieving remaining Job progress data for job "
                              + msjob.getJobId() + " on server " + WsUrl);
              e.printStackTrace();
              nexcept--;
              nunchanged = 3;
              // set flag remember that we've had an exception.
              jpex = true;
              jpchanged = false;
            }
            if (!jpchanged)
            {
              try
              {
                Thread.sleep(jpex ? 2400 : 1200); // wait a bit longer if we
                                                  // experienced an exception.
              } catch (Exception ex)
              {
              }
              ;
              nunchanged--;
            }
          } while (nunchanged > 0 && nexcept > 0);

          if (Cache.log.isDebugEnabled())
          {
            System.out.println("Job Execution file for job: "
                    + msjob.getJobId() + " on server " + WsUrl);
            System.out.println(msjob.getStatus());
            System.out.println("*** End of status");

          }
          try
          {
            msjob.alignment = server.getResult(msjob.getJobId());
          } catch (compbio.metadata.ResultNotAvailableException e)
          {
            // job has failed for some reason - probably due to invalid
            // parameters
            Cache.log.debug(
                    "Results not available for finished job - marking as broken job.",
                    e);
            msjob.jobProgress.append(
                    "\nResult not available. Probably due to invalid input or parameter settings. Server error message below:\n\n"
                            + e.getLocalizedMessage());
            msjob.setjobStatus(JobStatus.FAILED);
          } catch (Exception e)
          {
            Cache.log.error("Couldn't get Alignment for job.", e);
            // TODO: Increment count and retry ?
            msjob.setjobStatus(JobStatus.UNDEFINED);
          }
        }
        finalState.updateJobPanelState(wsInfo, OutputHeader, jobs[j]);
        if (jobs[j].isSubmitted() && jobs[j].isSubjobComplete()
                && jobs[j].hasResults())
        {
          results++;
          compbio.data.sequence.Alignment alignment = ((MsaWSJob) jobs[j]).alignment;
          if (alignment != null)
          {
            // server.close(jobs[j].getJobnum());
            // wsInfo.appendProgressText(jobs[j].getJobnum(),
            // "\nAlignment Object Method Notes\n");
            // wsInfo.appendProgressText(jobs[j].getJobnum(),
            // "Calculated with
            // "+alignment.getMetadata().getProgram().toString());
            // JBPNote The returned files from a webservice could be
            // hidden behind icons in the monitor window that,
            // when clicked, pop up their corresponding data
          }
        }
      }
    } catch (Exception ex)
    {

      Cache.log.error(
              "Unexpected exception when processing results for " + alTitle,
              ex);
      wsInfo.setStatus(WebserviceInfo.STATE_STOPPED_ERROR);
    }
    if (results > 0)
    {
      wsInfo.showResultsNewFrame
              .addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                  displayResults(true);
                }
              });
      wsInfo.mergeResults
              .addActionListener(new java.awt.event.ActionListener()
              {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt)
                {
                  displayResults(false);
                }
              });
      wsInfo.setResultsReady();
    }
    else
    {
      wsInfo.setFinishedNoResults();
    }
    updateGlobalStatus(finalState);
    wsInfo.setProgressBar(null, progbar);
  }

  /**
   * Display alignment results in a new frame (or - not currently supported -
   * added to an existing alignment).
   * 
   * @param newFrame
   */
  void displayResults(boolean newFrame)
  {
    // view input or result data for each block
    List<AlignmentOrder> alorders = new ArrayList<AlignmentOrder>();
    SequenceI[][] results = new SequenceI[jobs.length][];
    AlignmentOrder[] orders = new AlignmentOrder[jobs.length];
    String lastProgram = null;
    MsaWSJob msjob;
    for (int j = 0; j < jobs.length; j++)
    {
      if (jobs[j].hasResults())
      {
        msjob = (MsaWSJob) jobs[j];
        Object[] res = msjob.getAlignment();
        lastProgram = msjob.getAlignmentProgram();
        alorders.add((AlignmentOrder) res[1]);
        results[j] = (SequenceI[]) res[0];
        orders[j] = (AlignmentOrder) res[1];

        // SequenceI[] alignment = input.getUpdated
      }
      else
      {
        results[j] = null;
      }
    }
    Object[] newview = input.getUpdatedView(results, orders, getGapChar());
    // trash references to original result data
    for (int j = 0; j < jobs.length; j++)
    {
      results[j] = null;
      orders[j] = null;
    }
    SequenceI[] alignment = (SequenceI[]) newview[0];
    HiddenColumns hidden = (HiddenColumns) newview[1];
    Alignment al = new Alignment(alignment);
    // TODO: add 'provenance' property to alignment from the method notes
    if (lastProgram != null)
    {
      al.setProperty("Alignment Program", lastProgram);
    }
    // accompanying each subjob
    if (dataset != null)
    {
      al.setDataset(dataset);
    }
    propagateDatasetMappings(al);
    Alignment complement=null;
    if (input.hasComplementView())
    {
      Object[] newcompl = input
              .getComplementView()
              .getAlignmentAndHiddenColumns(getRequestingAlignFrame()
                      .getViewport().getCodingComplement().getGapCharacter());
      complement = new Alignment((SequenceI[])newcompl[0]);
      complement.setHiddenColumns((HiddenColumns) newcompl[1]);
      complement.setDataset(dataset);
      complement.alignAs(al);
    }
    // JBNote- TODO: warn user if a block is input rather than aligned data ?

    if (newFrame)
    {
      displayInNewFrame(al, alorders, hidden, complement);

    }
    else
    {
      // TODO 2.9.x feature
      System.out.println("MERGE WITH OLD FRAME");
      // TODO: modify alignment in original frame, replacing old for new
      // alignment using the commands.EditCommand model to ensure the update can
      // be undone
    }
  }

  /**
   * Display the alignment result in a new frame.
   * 
   * @param al
   * @param alorders
   * @param complement2 
   * @param columnselection
   */
  protected void displayInNewFrame(AlignmentI al,
          List<AlignmentOrder> alorders, HiddenColumns hidden, AlignmentI complement)
  {
    AlignFrame af = new AlignFrame(al, hidden, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);

    // initialise with same renderer settings as in parent alignframe.
    af.getFeatureRenderer().transferSettings(this.featureSettings);

    if (alorders.size() > 0)
    {
      addSortByMenuItems(af, alorders);
    }

    // TODO: refactor retrieve and show as new splitFrame as Desktop method

    /*
     * If alignment was requested from one half of a SplitFrame, show in a
     * SplitFrame with the other pane similarly aligned.
     */
    AlignFrame requestedBy = getRequestingAlignFrame();
    if (complement!=null && requestedBy != null && requestedBy.getSplitViewContainer() != null
            && requestedBy.getSplitViewContainer()
                    .getComplement(requestedBy) != null)
    {
      String complementTitle = requestedBy.getSplitViewContainer()
              .getComplementTitle(requestedBy);
      // becomes null if the alignment window was closed before the alignment
      // job finished.
      if (complement.getHeight() > 0)
      {
        af.setTitle(alTitle);
        AlignFrame af2 = new AlignFrame(complement,
                AlignFrame.DEFAULT_WIDTH, AlignFrame.DEFAULT_HEIGHT);
        af2.setTitle(complementTitle);
        String linkedTitle = MessageManager
                .getString("label.linked_view_title");
        JInternalFrame splitFrame = new SplitFrame(
                al.isNucleotide() ? af : af2, al.isNucleotide() ? af2 : af);
        Desktop.addInternalFrame(splitFrame, linkedTitle, -1, -1);
        return;
      }
    }

    /*
     * Not from SplitFrame, or failed to created a complementary alignment
     */
    Desktop.addInternalFrame(af, alTitle, AlignFrame.DEFAULT_WIDTH,
            AlignFrame.DEFAULT_HEIGHT);
  }

  /**
   * Add sort order options to the AlignFrame menus.
   * 
   * @param af
   * @param alorders
   */
  protected void addSortByMenuItems(AlignFrame af,
          List<AlignmentOrder> alorders)
  {
    // update orders
    if (alorders.size() == 1)
    {
      af.addSortByOrderMenuItem(WebServiceName + " Ordering",
              alorders.get(0));
    }
    else
    {
      // construct a non-redundant ordering set
      List<String> names = new ArrayList<String>();
      for (int i = 0, l = alorders.size(); i < l; i++)
      {
        String orderName = " Region " + i;
        int j = i + 1;

        while (j < l)
        {
          if (alorders.get(i).equals(alorders.get(j)))
          {
            alorders.remove(j);
            l--;
            orderName += "," + j;
          }
          else
          {
            j++;
          }
        }

        if (i == 0 && j == 1)
        {
          names.add("");
        }
        else
        {
          names.add(orderName);
        }
      }
      for (int i = 0, l = alorders.size(); i < l; i++)
      {
        af.addSortByOrderMenuItem(
                WebServiceName + (names.get(i)) + " Ordering",
                alorders.get(i));
      }
    }
  }

  @Override
  public boolean canMergeResults()
  {
    return false;
  }
}
