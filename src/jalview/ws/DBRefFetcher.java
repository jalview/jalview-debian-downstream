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
package jalview.ws;

import jalview.analysis.AlignSeq;
import jalview.bin.Cache;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.Mapping;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.gui.CutAndPasteTransfer;
import jalview.gui.DasSourceBrowser;
import jalview.gui.Desktop;
import jalview.gui.FeatureSettings;
import jalview.gui.IProgressIndicator;
import jalview.gui.OOMWarning;
import jalview.util.DBRefUtils;
import jalview.util.MessageManager;
import jalview.ws.dbsources.das.api.jalviewSourceI;
import jalview.ws.dbsources.das.datamodel.DasSequenceSource;
import jalview.ws.seqfetcher.DbSourceProxy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import uk.ac.ebi.picr.model.UPEntry;
import uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperServiceLocator;

/**
 * Implements a runnable for validating a sequence against external databases
 * and then propagating references and features onto the sequence(s)
 * 
 * @author $author$
 * @version $Revision$
 */
public class DBRefFetcher implements Runnable
{
  private static final String NEWLINE = System.lineSeparator();

  public interface FetchFinishedListenerI
  {
    void finished();
  }

  SequenceI[] dataset;

  IProgressIndicator progressWindow;

  CutAndPasteTransfer output = new CutAndPasteTransfer();

  boolean running = false;

  /**
   * picr client instance
   */
  uk.ac.ebi.www.picr.AccessionMappingService.AccessionMapperInterface picrClient = null;

  // This will be a collection of Vectors of sequenceI refs.
  // The key will be the seq name or accession id of the seq
  Hashtable<String, Vector<SequenceI>> seqRefs;

  DbSourceProxy[] dbSources;

  SequenceFetcher sfetcher;

  private List<FetchFinishedListenerI> listeners;

  private SequenceI[] alseqs;

  /*
   * when true - retrieved sequences will be trimmed to cover longest derived
   * alignment sequence
   */
  private boolean trimDsSeqs = true;

  /**
   * Creates a new DBRefFetcher object and fetches from the currently selected
   * set of databases, if this is null then it fetches based on feature settings
   * 
   * @param seqs
   *          fetch references for these SequenceI array
   * @param progressIndicatorFrame
   *          the frame for progress bar monitoring
   * @param sources
   *          array of DbSourceProxy to query references form
   * @param featureSettings
   *          FeatureSettings to get alternative DbSourceProxy from
   * @param isNucleotide
   *          indicates if the array of SequenceI are Nucleotides or not
   */
  public DBRefFetcher(SequenceI[] seqs,
          IProgressIndicator progressIndicatorFrame,
          DbSourceProxy[] sources, FeatureSettings featureSettings,
          boolean isNucleotide)
  {
    listeners = new ArrayList<FetchFinishedListenerI>();
    this.progressWindow = progressIndicatorFrame;
    alseqs = new SequenceI[seqs.length];
    SequenceI[] ds = new SequenceI[seqs.length];
    for (int i = 0; i < seqs.length; i++)
    {
      alseqs[i] = seqs[i];
      if (seqs[i].getDatasetSequence() != null)
      {
        ds[i] = seqs[i].getDatasetSequence();
      }
      else
      {
        ds[i] = seqs[i];
      }
    }
    this.dataset = ds;
    // TODO Jalview 2.5 lots of this code should be in the gui package!
    sfetcher = jalview.gui.SequenceFetcher
            .getSequenceFetcherSingleton(progressIndicatorFrame);
    // set default behaviour for transferring excess sequence data to the
    // dataset
    trimDsSeqs = Cache.getDefault("TRIM_FETCHED_DATASET_SEQS", true);
    if (sources == null)
    {
      setDatabaseSources(featureSettings, isNucleotide);
    }
    else
    {
      // we assume the caller knows what they're doing and ensured that all the
      // db source names are valid
      dbSources = sources;
    }
  }

  /**
   * Helper method to configure the list of database sources to query
   * 
   * @param featureSettings
   * @param forNucleotide
   */
  void setDatabaseSources(FeatureSettings featureSettings,
          boolean forNucleotide)
  {
    // af.featureSettings_actionPerformed(null);
    String[] defdb = null;
    List<DbSourceProxy> selsources = new ArrayList<DbSourceProxy>();
    Vector<jalviewSourceI> dasselsrc = (featureSettings != null) ? featureSettings
            .getSelectedSources() : new DasSourceBrowser()
            .getSelectedSources();

    for (jalviewSourceI src : dasselsrc)
    {
      List<DbSourceProxy> sp = src.getSequenceSourceProxies();
      if (sp != null)
      {
        selsources.addAll(sp);
        if (sp.size() > 1)
        {
          Cache.log.debug("Added many Db Sources for :" + src.getTitle());
        }
      }
    }
    // select appropriate databases based on alignFrame context.
    if (forNucleotide)
    {
      defdb = DBRefSource.DNACODINGDBS;
    }
    else
    {
      defdb = DBRefSource.PROTEINDBS;
    }
    List<DbSourceProxy> srces = new ArrayList<DbSourceProxy>();
    for (String ddb : defdb)
    {
      List<DbSourceProxy> srcesfordb = sfetcher.getSourceProxy(ddb);
      if (srcesfordb != null)
      {
        for (DbSourceProxy src : srcesfordb)
        {
          if (!srces.contains(src))
          {
            srces.addAll(srcesfordb);
          }
        }
      }
    }
    // append the PDB data source, since it is 'special', catering for both
    // nucleotide and protein
    // srces.addAll(sfetcher.getSourceProxy(DBRefSource.PDB));

    srces.addAll(selsources);
    dbSources = srces.toArray(new DbSourceProxy[srces.size()]);
  }

  /**
   * Constructor with only sequences provided
   * 
   * @param sequences
   */
  public DBRefFetcher(SequenceI[] sequences)
  {
    this(sequences, null, null, null, false);
  }

  /**
   * Add a listener to be notified when sequence fetching is complete
   * 
   * @param l
   */
  public void addListener(FetchFinishedListenerI l)
  {
    listeners.add(l);
  }

  /**
   * retrieve all the das sequence sources and add them to the list of db
   * sources to retrieve from
   */
  public void appendAllDasSources()
  {
    if (dbSources == null)
    {
      dbSources = new DbSourceProxy[0];
    }
    // append additional sources
    DbSourceProxy[] otherdb = sfetcher
            .getDbSourceProxyInstances(DasSequenceSource.class);
    if (otherdb != null && otherdb.length > 0)
    {
      DbSourceProxy[] newsrc = new DbSourceProxy[dbSources.length
              + otherdb.length];
      System.arraycopy(dbSources, 0, newsrc, 0, dbSources.length);
      System.arraycopy(otherdb, 0, newsrc, dbSources.length, otherdb.length);
      dbSources = newsrc;
    }
  }

  /**
   * start the fetcher thread
   * 
   * @param waitTillFinished
   *          true to block until the fetcher has finished
   */
  public void fetchDBRefs(boolean waitTillFinished)
  {
    // TODO can we not simply write
    // if (waitTillFinished) { run(); } else { new Thread(this).start(); }

    Thread thread = new Thread(this);
    thread.start();
    running = true;

    if (waitTillFinished)
    {
      while (running)
      {
        try
        {
          Thread.sleep(500);
        } catch (Exception ex)
        {
        }
      }
    }
  }

  /**
   * The sequence will be added to a vector of sequences belonging to key which
   * could be either seq name or dbref id
   * 
   * @param seq
   *          SequenceI
   * @param key
   *          String
   */
  void addSeqId(SequenceI seq, String key)
  {
    key = key.toUpperCase();

    Vector<SequenceI> seqs;
    if (seqRefs.containsKey(key))
    {
      seqs = seqRefs.get(key);

      if (seqs != null && !seqs.contains(seq))
      {
        seqs.addElement(seq);
      }
      else if (seqs == null)
      {
        seqs = new Vector<SequenceI>();
        seqs.addElement(seq);
      }

    }
    else
    {
      seqs = new Vector<SequenceI>();
      seqs.addElement(seq);
    }

    seqRefs.put(key, seqs);
  }

  /**
   * DOCUMENT ME!
   */
  @Override
  public void run()
  {
    if (dbSources == null)
    {
      throw new Error(
              MessageManager
                      .getString("error.implementation_error_must_init_dbsources"));
    }
    running = true;
    long startTime = System.currentTimeMillis();
    if (progressWindow != null)
    {
      progressWindow.setProgressBar(
              MessageManager.getString("status.fetching_db_refs"),
              startTime);
    }
    try
    {
      if (Cache.getDefault("DBREFFETCH_USEPICR", false))
      {
        picrClient = new AccessionMapperServiceLocator()
                .getAccessionMapperPort();
      }
    } catch (Exception e)
    {
      System.err.println("Couldn't locate PICR service instance.\n");
      e.printStackTrace();
    }

    Vector<SequenceI> sdataset = new Vector<SequenceI>(
            Arrays.asList(dataset));
    List<String> warningMessages = new ArrayList<String>();

    int db = 0;
    while (sdataset.size() > 0 && db < dbSources.length)
    {
      int maxqlen = 1; // default number of queries made at one time
      System.out.println("Verifying against " + dbSources[db].getDbName());

      // iterate through db for each remaining un-verified sequence
      SequenceI[] currSeqs = new SequenceI[sdataset.size()];
      sdataset.copyInto(currSeqs);// seqs that are to be validated against
      // dbSources[db]
      Vector<String> queries = new Vector<String>(); // generated queries curSeq
      seqRefs = new Hashtable<String, Vector<SequenceI>>();

      int seqIndex = 0;

      DbSourceProxy dbsource = dbSources[db];
      // for moment, we dumbly iterate over all retrieval sources for a
      // particular database
      // TODO: introduce multithread multisource queries and logic to remove a
      // query from other sources if any source for a database returns a
      // record
      maxqlen = dbsource.getMaximumQueryCount();

      while (queries.size() > 0 || seqIndex < currSeqs.length)
      {
        if (queries.size() > 0)
        {
          // Still queries to make for current seqIndex
          StringBuffer queryString = new StringBuffer("");
          int numq = 0;
          int nqSize = (maxqlen > queries.size()) ? queries.size()
                  : maxqlen;

          while (queries.size() > 0 && numq < nqSize)
          {
            String query = queries.elementAt(0);
            if (dbsource.isValidReference(query))
            {
              queryString.append((numq == 0) ? "" : dbsource
                      .getAccessionSeparator());
              queryString.append(query);
              numq++;
            }
            // remove the extracted query string
            queries.removeElementAt(0);
          }
          // make the queries and process the response
          AlignmentI retrieved = null;
          try
          {
            if (Cache.log.isDebugEnabled())
            {
              Cache.log.debug("Querying " + dbsource.getDbName()
                      + " with : '" + queryString.toString() + "'");
            }
            retrieved = dbsource.getSequenceRecords(queryString.toString());
          } catch (Exception ex)
          {
            ex.printStackTrace();
          } catch (OutOfMemoryError err)
          {
            new OOMWarning("retrieving database references ("
                    + queryString.toString() + ")", err);
          }
          if (retrieved != null)
          {
            transferReferences(sdataset, dbsource.getDbSource(), retrieved,
                    trimDsSeqs, warningMessages);
          }
        }
        else
        {
          // make some more strings for use as queries
          for (int i = 0; (seqIndex < dataset.length) && (i < 50); seqIndex++, i++)
          {
            SequenceI sequence = dataset[seqIndex];
            DBRefEntry[] uprefs = DBRefUtils.selectRefs(
                    sequence.getDBRefs(),
                    new String[] { dbsource.getDbSource() }); // jalview.datamodel.DBRefSource.UNIPROT
            // });
            // check for existing dbrefs to use
            if (uprefs != null && uprefs.length > 0)
            {
              for (int j = 0; j < uprefs.length; j++)
              {
                addSeqId(sequence, uprefs[j].getAccessionId());
                queries.addElement(uprefs[j].getAccessionId().toUpperCase());
              }
            }
            else
            {
              // generate queries from sequence ID string
              StringTokenizer st = new StringTokenizer(sequence.getName(),
                      "|");
              while (st.hasMoreTokens())
              {
                String token = st.nextToken();
                UPEntry[] presp = null;
                if (picrClient != null)
                {
                  // resolve the string against PICR to recover valid IDs
                  try
                  {
                    presp = picrClient
                            .getUPIForAccession(token, null,
                                    picrClient.getMappedDatabaseNames(),
                                    null, true);
                  } catch (Exception e)
                  {
                    System.err.println("Exception with Picr for '" + token
                            + "'\n");
                    e.printStackTrace();
                  }
                }
                if (presp != null && presp.length > 0)
                {
                  for (int id = 0; id < presp.length; id++)
                  {
                    // construct sequences from response if sequences are
                    // present, and do a transferReferences
                    // otherwise transfer non sequence x-references directly.
                  }
                  System.out
                          .println("Validated ID against PICR... (for what its worth):"
                                  + token);
                  addSeqId(sequence, token);
                  queries.addElement(token.toUpperCase());
                }
                else
                {
                  // if ()
                  // System.out.println("Not querying source with token="+token+"\n");
                  addSeqId(sequence, token);
                  queries.addElement(token.toUpperCase());
                }
              }
            }
          }
        }
      }
      // advance to next database
      db++;
    } // all databases have been queried
    if (!warningMessages.isEmpty())
    {
      StringBuilder sb = new StringBuilder(warningMessages.size() * 30);
      sb.append(MessageManager
              .getString("label.your_sequences_have_been_verified"));
      for (String msg : warningMessages)
      {
        sb.append(msg).append(NEWLINE);
      }
      output.setText(sb.toString());

      Desktop.addInternalFrame(output,
              MessageManager.getString("label.sequences_updated"), 600, 300);
      // The above is the dataset, we must now find out the index
      // of the viewed sequence

    }
    if (progressWindow != null)
    {
      progressWindow.setProgressBar(
              MessageManager.getString("label.dbref_search_completed"),
              startTime);
    }

    for (FetchFinishedListenerI listener : listeners)
    {
      listener.finished();
    }
    running = false;
  }

  /**
   * Verify local sequences in seqRefs against the retrieved sequence database
   * records. Returns true if any sequence was modified as a result (start/end
   * changed and/or sequence enlarged), else false.
   * 
   * @param sdataset
   *          dataset sequences we are retrieving for
   * @param dbSource
   *          database source we are retrieving from
   * @param retrievedAl
   *          retrieved sequences as alignment
   * @param trimDatasetSeqs
   *          if true, sequences will not be enlarged to match longer retrieved
   *          sequences, only their start/end adjusted
   * @param warningMessages
   *          a list of messages to add to
   */
  boolean transferReferences(Vector<SequenceI> sdataset, String dbSource,
          AlignmentI retrievedAl, boolean trimDatasetSeqs,
          List<String> warningMessages)
  {
    // System.out.println("trimming ? " + trimDatasetSeqs);
    if (retrievedAl == null || retrievedAl.getHeight() == 0)
    {
      return false;
    }

    boolean modified = false;
    SequenceI[] retrieved = recoverDbSequences(retrievedAl
            .getSequencesArray());
    SequenceI sequence = null;

    for (SequenceI retrievedSeq : retrieved)
    {
      // Work out which sequences this sequence matches,
      // taking into account all accessionIds and names in the file
      Vector<SequenceI> sequenceMatches = new Vector<SequenceI>();
      // look for corresponding accession ids
      DBRefEntry[] entryRefs = DBRefUtils.selectRefs(
              retrievedSeq.getDBRefs(), new String[] { dbSource });
      if (entryRefs == null)
      {
        System.err
                .println("Dud dbSource string ? no entryrefs selected for "
                        + dbSource + " on " + retrievedSeq.getName());
        continue;
      }
      for (int j = 0; j < entryRefs.length; j++)
      {
        String accessionId = entryRefs[j].getAccessionId();
        // match up on accessionId
        if (seqRefs.containsKey(accessionId.toUpperCase()))
        {
          Vector<SequenceI> seqs = seqRefs.get(accessionId);
          for (int jj = 0; jj < seqs.size(); jj++)
          {
            sequence = seqs.elementAt(jj);
            if (!sequenceMatches.contains(sequence))
            {
              sequenceMatches.addElement(sequence);
            }
          }
        }
      }
      if (sequenceMatches.isEmpty())
      {
        // failed to match directly on accessionId==query so just compare all
        // sequences to entry
        Enumeration<String> e = seqRefs.keys();
        while (e.hasMoreElements())
        {
          Vector<SequenceI> sqs = seqRefs.get(e.nextElement());
          if (sqs != null && sqs.size() > 0)
          {
            Enumeration<SequenceI> sqe = sqs.elements();
            while (sqe.hasMoreElements())
            {
              sequenceMatches.addElement(sqe.nextElement());
            }
          }
        }
      }
      // look for corresponding names
      // this is uniprot specific ?
      // could be useful to extend this so we try to find any 'significant'
      // information in common between two sequence objects.
      /*
       * DBRefEntry[] entryRefs =
       * jalview.util.DBRefUtils.selectRefs(entry.getDBRef(), new String[] {
       * dbSource }); for (int j = 0; j < entry.getName().size(); j++) { String
       * name = entry.getName().elementAt(j).toString(); if
       * (seqRefs.containsKey(name)) { Vector seqs = (Vector) seqRefs.get(name);
       * for (int jj = 0; jj < seqs.size(); jj++) { sequence = (SequenceI)
       * seqs.elementAt(jj); if (!sequenceMatches.contains(sequence)) {
       * sequenceMatches.addElement(sequence); } } } }
       */
      // sequenceMatches now contains the set of all sequences associated with
      // the returned db record
      final String retrievedSeqString = retrievedSeq.getSequenceAsString();
      String entrySeq = retrievedSeqString.toUpperCase();
      for (int m = 0; m < sequenceMatches.size(); m++)
      {
        sequence = sequenceMatches.elementAt(m);
        // only update start and end positions and shift features if there are
        // no existing references
        // TODO: test for legacy where uniprot or EMBL refs exist but no
        // mappings are made (but content matches retrieved set)
        boolean updateRefFrame = sequence.getDBRefs() == null
                || sequence.getDBRefs().length == 0;
        // TODO:
        // verify sequence against the entry sequence

        Mapping mp;
        final int sequenceStart = sequence.getStart();

        boolean remoteEnclosesLocal = false;
        String nonGapped = AlignSeq.extractGaps("-. ",
                sequence.getSequenceAsString()).toUpperCase();
        int absStart = entrySeq.indexOf(nonGapped);
        if (absStart == -1)
        {
          // couldn't find local sequence in sequence from database, so check if
          // the database sequence is a subsequence of local sequence
          absStart = nonGapped.indexOf(entrySeq);
          if (absStart == -1)
          {
            // verification failed. couldn't find any relationship between
            // entrySeq and local sequence
            // messages suppressed as many-to-many matches are confusing
            // String msg = sequence.getName()
            // + " Sequence not 100% match with "
            // + retrievedSeq.getName();
            // addWarningMessage(warningMessages, msg);
            continue;
          }
          /*
           * retrieved sequence is a proper subsequence of local sequence
           */
          String msg = sequence.getName() + " has " + absStart
                  + " prefixed residues compared to "
                  + retrievedSeq.getName();
          addWarningMessage(warningMessages, msg);

          /*
           * So create a mapping to the external entry from the matching region of 
           * the local sequence, and leave local start/end untouched. 
           */
          mp = new Mapping(null, new int[] { sequenceStart + absStart,
              sequenceStart + absStart + entrySeq.length() - 1 }, new int[]
          { retrievedSeq.getStart(),
              retrievedSeq.getStart() + entrySeq.length() - 1 }, 1, 1);
          updateRefFrame = false;
        }
        else
        {
          /*
           * local sequence is a subsequence of (or matches) retrieved sequence
           */
          remoteEnclosesLocal = true;
          mp = null;

          if (updateRefFrame)
          {
            SequenceFeature[] sfs = sequence.getSequenceFeatures();
            if (sfs != null)
            {
              /*
               * relocate existing sequence features by offset
               */
              int start = sequenceStart;
              int end = sequence.getEnd();
              int startShift = 1 - absStart - start;

              if (startShift != 0)
              {
                for (SequenceFeature sf : sfs)
                {
                  if (sf.getBegin() >= start && sf.getEnd() <= end)
                  {
                    sf.setBegin(sf.getBegin() + startShift);
                    sf.setEnd(sf.getEnd() + startShift);
                    modified = true;
                  }
                }
              }
            }
          }
        }

        System.out.println("Adding dbrefs to " + sequence.getName()
                + " from " + dbSource + " sequence : "
                + retrievedSeq.getName());
        sequence.transferAnnotation(retrievedSeq, mp);

        absStart += retrievedSeq.getStart();
        int absEnd = absStart + nonGapped.length() - 1;
        if (!trimDatasetSeqs)
        {
          /*
           * update start position and/or expand to longer retrieved sequence
           */
          if (!retrievedSeqString.equals(sequence.getSequenceAsString())
                  && remoteEnclosesLocal)
          {
            sequence.setSequence(retrievedSeqString);
            modified = true;
            addWarningMessage(warningMessages,
                    "Sequence for " + sequence.getName()
                            + " expanded from " + retrievedSeq.getName());
          }
          if (sequence.getStart() != retrievedSeq.getStart())
          {
            sequence.setStart(retrievedSeq.getStart());
            modified = true;
            if (absStart != sequenceStart)
            {
              addWarningMessage(warningMessages, "Start/end position for "
                      + sequence.getName() + " updated from "
                      + retrievedSeq.getName());
            }
          }
        }
        if (updateRefFrame)
        {
          // finally, update local sequence reference frame if we're allowed
          if (trimDatasetSeqs)
          {
            // just fix start/end
            if (sequence.getStart() != absStart
                    || sequence.getEnd() != absEnd)
            {
              sequence.setStart(absStart);
              sequence.setEnd(absEnd);
              modified = true;
              addWarningMessage(warningMessages, "Start/end for "
                      + sequence.getName() + " updated from "
                      + retrievedSeq.getName());
            }
          }
          // search for alignment sequences to update coordinate frame for
          for (int alsq = 0; alsq < alseqs.length; alsq++)
          {
            if (alseqs[alsq].getDatasetSequence() == sequence)
            {
              String ngAlsq = AlignSeq.extractGaps("-. ",
                      alseqs[alsq].getSequenceAsString()).toUpperCase();
              int oldstrt = alseqs[alsq].getStart();
              alseqs[alsq].setStart(sequence.getSequenceAsString()
                      .toUpperCase().indexOf(ngAlsq)
                      + sequence.getStart());
              if (oldstrt != alseqs[alsq].getStart())
              {
                alseqs[alsq].setEnd(ngAlsq.length()
                        + alseqs[alsq].getStart() - 1);
                modified = true;
              }
            }
          }
          // TODO: search for all other references to this dataset sequence, and
          // update start/end
          // TODO: update all AlCodonMappings which involve this alignment
          // sequence (e.g. Q30167 cdna translation from exon2 product (vamsas
          // demo)
        }
        // and remove it from the rest
        // TODO: decide if we should remove annotated sequence from set
        sdataset.remove(sequence);
        // TODO: should we make a note of sequences that have received new DB
        // ids, so we can query all enabled DAS servers for them ?
      }
    }
    return modified;
  }

  /**
   * Adds the message to the list unless it already contains it
   * 
   * @param messageList
   * @param msg
   */
  void addWarningMessage(List<String> messageList, String msg)
  {
    if (!messageList.contains(msg))
    {
      messageList.add(msg);
    }
  }

  /**
   * loop thru and collect additional sequences in Map.
   * 
   * @param sequencesArray
   * @return
   */
  private SequenceI[] recoverDbSequences(SequenceI[] sequencesArray)
  {
    Vector<SequenceI> nseq = new Vector<SequenceI>();
    for (int i = 0; sequencesArray != null && i < sequencesArray.length; i++)
    {
      nseq.addElement(sequencesArray[i]);
      DBRefEntry[] dbr = sequencesArray[i].getDBRefs();
      Mapping map = null;
      for (int r = 0; (dbr != null) && r < dbr.length; r++)
      {
        if ((map = dbr[r].getMap()) != null)
        {
          if (map.getTo() != null && !nseq.contains(map.getTo()))
          {
            nseq.addElement(map.getTo());
          }
        }
      }
    }
    if (nseq.size() > 0)
    {
      sequencesArray = new SequenceI[nseq.size()];
      nseq.toArray(sequencesArray);
    }
    return sequencesArray;
  }
}
