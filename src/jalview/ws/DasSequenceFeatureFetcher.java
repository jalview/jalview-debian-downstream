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
package jalview.ws;

import java.net.*;
import java.util.*;

import javax.swing.*;

import org.biojava.dasobert.das.*;
import org.biojava.dasobert.das2.*;
import org.biojava.dasobert.das2.io.*;
import org.biojava.dasobert.dasregistry.*;
import org.biojava.dasobert.eventmodel.*;
import jalview.bin.Cache;
import jalview.datamodel.*;
import jalview.gui.*;
import jalview.util.UrlLink;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class DasSequenceFeatureFetcher
{
  SequenceI[] sequences;

  AlignFrame af;

  FeatureSettings fsettings;

  StringBuffer sbuffer = new StringBuffer();

  Vector selectedSources;

  boolean cancelled = false;

  private void debug(String mesg)
  {
    debug(mesg, null);
  }

  private void debug(String mesg, Exception e)
  {
    if (Cache.log != null)
    {
      Cache.log.debug(mesg, e);
    }
    else
    {
      System.err.println(mesg);
      if (e != null)
      {
        e.printStackTrace();
      }
    }
  }

  long startTime;

  /**
   * Creates a new SequenceFeatureFetcher object. Uses default
   * 
   * @param align
   *          DOCUMENT ME!
   * @param ap
   *          DOCUMENT ME!
   */
  public DasSequenceFeatureFetcher(SequenceI[] sequences,
          FeatureSettings fsettings, Vector selectedSources)
  {
    this(sequences, fsettings, selectedSources, true, true);
  }

  public DasSequenceFeatureFetcher(SequenceI[] oursequences,
          FeatureSettings fsettings, Vector ourselectedSources,
          boolean checkDbrefs, boolean promptFetchDbrefs)
  {
    this.selectedSources = new Vector();
    Enumeration sources = ourselectedSources.elements();
    // filter both sequences and sources to eliminate duplicates
    while (sources.hasMoreElements())
    {
      Object src = sources.nextElement();
      if (!selectedSources.contains(src))
      {
        selectedSources.addElement(src);
      }
      ;
    }
    Vector sqs = new Vector();
    for (int i = 0; i < oursequences.length; i++)
    {
      if (!sqs.contains(oursequences[i]))
      {
        sqs.addElement(oursequences[i]);
      }
    }
    sequences = new SequenceI[sqs.size()];
    for (int i = 0; i < sequences.length; i++)
    {
      sequences[i] = (SequenceI) sqs.elementAt(i);
    }
    if (fsettings != null)
    {
      this.fsettings = fsettings;
      this.af = fsettings.af;
      af.setShowSeqFeatures(true);
    }
    int uniprotCount = 0;
    for (int i = 0; i < selectedSources.size(); i++)
    {
      DasSource source = (DasSource) selectedSources.elementAt(i);
      DasCoordinateSystem[] coords = source.getCoordinateSystem();
      for (int c = 0; c < coords.length; c++)
      {
        // TODO: match UniProt coord system canonically (?) - does
        // UniProt==uniprot==UNIPROT ?
        if (coords[c].getName().indexOf("UniProt") > -1)
        {
          uniprotCount++;
          break;
        }
      }
    }

    int refCount = 0;
    for (int i = 0; i < sequences.length; i++)
    {
      DBRefEntry[] dbref = sequences[i].getDBRef();
      if (dbref != null)
      {
        for (int j = 0; j < dbref.length; j++)
        {
          if (dbref[j].getSource().equals(
                  jalview.datamodel.DBRefSource.UNIPROT))
          {
            refCount++;
            break;
          }
        }
      }
    }

    if (checkDbrefs && refCount < sequences.length && uniprotCount > 0)
    {

      int reply = JOptionPane.YES_OPTION;
      if (promptFetchDbrefs)
      {
        reply = JOptionPane
                .showInternalConfirmDialog(
                        Desktop.desktop,
                        "Do you want Jalview to find\n"
                                + "Uniprot Accession ids for given sequence names?",
                        "Find Uniprot Accession Ids",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
      }

      if (reply == JOptionPane.YES_OPTION)
      {
        Thread thread = new Thread(new FetchDBRefs());
        thread.start();
      }
      else
      {
        startFetching();
      }
    }
    else
    {
      startFetching();
    }

  }

  class FetchDBRefs implements Runnable
  {
    public void run()
    {
      new DBRefFetcher(sequences, af).fetchDBRefs(true);
      startFetching();
    }
  }

  /**
   * Spawns a number of dasobert Fetcher threads to add features to sequences in
   * the dataset
   */
  void startFetching()
  {
    cancelled = false;
    startTime = System.currentTimeMillis();
    if (af != null)
    {
      af.setProgressBar("Fetching DAS Sequence Features", startTime);
    }

    if (selectedSources == null || selectedSources.size() == 0)
    {
      try
      {
        DasSource[] sources = new jalview.gui.DasSourceBrowser()
                .getDASSource();

        String active = jalview.bin.Cache.getDefault("DAS_ACTIVE_SOURCE",
                "uniprot");
        StringTokenizer st = new StringTokenizer(active, "\t");
        selectedSources = new Vector();
        String token;
        while (st.hasMoreTokens())
        {
          token = st.nextToken();
          for (int i = 0; i < sources.length; i++)
          {
            if (sources[i].getNickname().equals(token))
            {
              selectedSources.addElement(sources[i]);
              break;
            }
          }
        }
      } catch (Exception ex)
      {
        debug("Exception whilst setting default feature sources from registry and local preferences.",
                ex);
      }
    }

    if (selectedSources == null || selectedSources.size() == 0)
    {
      System.out.println("No DAS Sources active");
      cancelled = true;
      setGuiNoDassourceActive();
      return;
    }

    sourcesRemaining = selectedSources.size();
    // Now sending requests one at a time to each server
    for (int sourceIndex = 0; sourceIndex < selectedSources.size()
            && !cancelled; sourceIndex++)
    {
      DasSource dasSource = (DasSource) selectedSources
              .elementAt(sourceIndex);

      nextSequence(dasSource, sequences[0]);
    }
  }

  private void setGuiNoDassourceActive()
  {

    if (af != null)
    {
      af.setProgressBar("No DAS Sources Active", startTime);
    }
    if (getFeatSettings() != null)
    {
      fsettings.noDasSourceActive();
    }
  }

  /**
   * Update our fsettings dialog reference if we didn't have one when we were
   * first initialised.
   * 
   * @return fsettings
   */
  private FeatureSettings getFeatSettings()
  {
    if (fsettings == null)
    {
      if (af != null)
      {
        fsettings = af.featureSettings;
      }
    }
    return fsettings;
  }

  public void cancel()
  {
    if (af != null)
    {
      af.setProgressBar("DAS Feature Fetching Cancelled", startTime);
    }
    cancelled = true;
  }

  int sourcesRemaining = 0;

  void responseComplete(DasSource dasSource, SequenceI seq)
  {
    if (seq != null)
    {
      for (int seqIndex = 0; seqIndex < sequences.length - 1 && !cancelled; seqIndex++)
      {
        if (sequences[seqIndex] == seq)
        {
          nextSequence(dasSource, sequences[++seqIndex]);
          return;
        }
      }
    }

    sourcesRemaining--;

    if (sourcesRemaining == 0)
    {
      System.err.println("Fetching Complete.");
      setGuiFetchComplete();
    }

  }

  private void setGuiFetchComplete()
  {

    if (!cancelled && af != null)
    {
      // only update the progress bar if we've completed the fetch normally
      af.setProgressBar("DAS Feature Fetching Complete", startTime);
    }

    if (af != null && af.featureSettings != null)
    {
      af.featureSettings.setTableData();
    }

    if (getFeatSettings() != null)
    {
      fsettings.complete();
    }
  }

  void featuresAdded(SequenceI seq)
  {
    if (af == null)
    {
      // no gui to update with features.
      return;
    }
    af.getFeatureRenderer().featuresAdded();

    int start = af.getViewport().getStartSeq();
    int end = af.getViewport().getEndSeq();
    int index;
    for (index = start; index < end; index++)
    {
      if (seq == af.getViewport().getAlignment().getSequenceAt(index)
              .getDatasetSequence())
      {
        af.alignPanel.paintAlignment(true);
        break;
      }
    }
  }

  void nextSequence(DasSource dasSource, SequenceI seq)
  {
    if (cancelled)
      return;
    DBRefEntry[] uprefs = jalview.util.DBRefUtils.selectRefs(
            seq.getDBRef(), new String[]
            {
            // jalview.datamodel.DBRefSource.PDB,
            jalview.datamodel.DBRefSource.UNIPROT,
            // jalview.datamodel.DBRefSource.EMBL - not tested on any EMBL coord
            // sys sources
            });
    // TODO: minimal list of DAS queries to make by querying with untyped ID if
    // distinct from any typed IDs

    boolean dasCoordSysFound = false;

    if (uprefs != null)
    {
      // do any of these ids match the source's coordinate system ?
      for (int j = 0; !dasCoordSysFound && j < uprefs.length; j++)
      {
        DasCoordinateSystem cs[] = dasSource.getCoordinateSystem();

        for (int csIndex = 0; csIndex < cs.length && !dasCoordSysFound; csIndex++)
        {
          if (cs.length > 0
                  && jalview.util.DBRefUtils.isDasCoordinateSystem(
                          cs[csIndex].getName(), uprefs[j]))
          {
            debug("Launched fetcher for coordinate system "
                    + cs[0].getName());
            // Will have to pass any mapping information to the fetcher
            // - the start/end for the DBRefEntry may not be the same as the
            // sequence's start/end

            System.out.println(seq.getName() + " "
                    + (seq.getDatasetSequence() == null) + " "
                    + dasSource.getUrl());

            dasCoordSysFound = true; // break's out of the loop
            createFeatureFetcher(seq, dasSource, uprefs[j]);
          }
          else
            System.out.println("IGNORE " + cs[csIndex].getName());
        }
      }
    }

    if (!dasCoordSysFound)
    {
      String id = null;
      // try and use the name as the sequence id
      if (seq.getName().indexOf("|") > -1)
      {
        id = seq.getName().substring(seq.getName().lastIndexOf("|") + 1);
        if (id.trim().length() < 4)
        {
          // hack - we regard a significant ID as being at least 4
          // non-whitespace characters
          id = seq.getName().substring(0, seq.getName().lastIndexOf("|"));
          if (id.indexOf("|") > -1)
          {
            id = id.substring(id.lastIndexOf("|") + 1);
          }
        }
      }
      else
      {
        id = seq.getName();
      }
      if (id != null)
      {
        // Should try to call a general feature fetcher that
        // queries many sources with name to discover applicable ID references
        createFeatureFetcher(seq, dasSource, id);
      }
    }

  }

  /**
   * fetch and add das features to a sequence using the given source URL and
   * compatible DbRef id. new features are mapped using the DbRef mapping to the
   * local coordinate system.
   * 
   * @param seq
   * @param SourceUrl
   * @param dbref
   */
  protected void createFeatureFetcher(final SequenceI seq,
          final DasSource dasSource, final DBRefEntry dbref)
  {

    // ////////////
    // / fetch DAS features
    final Das1Source source = new Das1Source();
    source.setUrl(dasSource.getUrl());
    source.setNickname(dasSource.getNickname());
    if (dbref == null || dbref.getAccessionId() == null
            || dbref.getAccessionId().length() < 1)
    {
      responseComplete(dasSource, seq); // reduce thread count anyhow
      return;
    }
    debug("new Das Feature Fetcher for " + dbref.getSource() + ":"
            + dbref.getAccessionId() + " querying " + dasSource.getUrl());
    FeatureThread fetcher = new FeatureThread(dbref.getAccessionId()
    // + ":" + start + "," + end,
            , source);

    fetcher.addFeatureListener(new FeatureListener()
    {
      public void comeBackLater(FeatureEvent e)
      {
        responseComplete(dasSource, seq);
        debug("das source " + e.getSource().getNickname()
                + " asked us to come back in " + e.getComeBackLater()
                + " secs.");
      }

      public void newFeatures(FeatureEvent e)
      {

        Das1Source ds = e.getSource();

        Map[] features = e.getFeatures();
        // add features to sequence
        debug("das source " + ds.getUrl() + " returned " + features.length
                + " features");

        if (features.length > 0)
        {
          for (int i = 0; i < features.length; i++)
          {
            // standard DAS feature-> jalview sequence feature transformation
            SequenceFeature f = newSequenceFeature(features[i],
                    source.getNickname());
            if (!parseSeqFeature(seq, f, features[i], source))
            {
              if (dbref.getMap() != null && f.getBegin() > 0
                      && f.getEnd() > 0)
              {
                debug("mapping from " + f.getBegin() + " - " + f.getEnd());
                SequenceFeature vf[] = null;

                try
                {
                  vf = dbref.getMap().locateFeature(f);
                } catch (Exception ex)
                {
                  Cache.log
                          .info("Error in 'experimental' mapping of features. Please try to reproduce and then report info to jalview-discuss@jalview.org.");
                  Cache.log.info("Mapping feature from " + f.getBegin()
                          + " to " + f.getEnd() + " in dbref "
                          + dbref.getAccessionId() + " in "
                          + dbref.getSource());
                  Cache.log.info("using das Source " + ds.getUrl());
                  Cache.log.info("Exception", ex);
                }

                if (vf != null)
                {
                  for (int v = 0; v < vf.length; v++)
                  {
                    debug("mapping to " + v + ": " + vf[v].getBegin()
                            + " - " + vf[v].getEnd());
                    seq.addSequenceFeature(vf[v]);
                  }
                }
              }
              else
              {
                seq.addSequenceFeature(f);
              }
            }
          }
          featuresAdded(seq);
        }
        else
        {
          // System.out.println("No features found for " + seq.getName()
          // + " from: " + e.getDasSource().getNickname());
        }
        responseComplete(dasSource, seq);

      }
    }

    );

    fetcher.start();
  }

  protected void createFeatureFetcher(final SequenceI seq,
          final DasSource dasSource, String id)
  {
    // ////////////
    // / fetch DAS features
    final Das1Source source = new Das1Source();
    source.setUrl(dasSource.getUrl());
    source.setNickname(dasSource.getNickname());

    if (id != null)
    {
      id = id.trim();
    }
    if (id != null && id.length() > 0)
    {
      debug("new Das Feature Fetcher for " + id + " querying "
              + dasSource.getUrl());
      FeatureThread fetcher = new FeatureThread(id
      // + ":" + start + "," + end,
              , source);

      fetcher.addFeatureListener(new FeatureListener()
      {
        public void comeBackLater(FeatureEvent e)
        {
          responseComplete(dasSource, seq);
          debug("das source " + e.getSource().getNickname()
                  + " asked us to come back in " + e.getComeBackLater()
                  + " secs.");
        }

        public void newFeatures(FeatureEvent e)
        {

          Das1Source ds = e.getSource();

          Map[] features = e.getFeatures();
          // add features to sequence
          debug("das source " + ds.getUrl() + " returned "
                  + features.length + " features");

          if (features.length > 0)
          {
            for (int i = 0; i < features.length; i++)
            {
              // standard DAS feature-> jalview sequence feature transformation
              SequenceFeature f = newSequenceFeature(features[i],
                      source.getNickname());
              if (!parseSeqFeature(seq, f, features[i], source))
              {
                // just add as a simple sequence feature
                seq.addSequenceFeature(f);
              }
            }

            featuresAdded(seq);
          }
          else
          {
            // System.out.println("No features found for " + seq.getName()
            // + " from: " + e.getDasSource().getNickname());
          }
          responseComplete(dasSource, seq);

        }
      }

      );

      fetcher.start();
    }
    else
    {
      // invalid fetch - indicate it is finished.
      debug("Skipping empty ID for querying " + dasSource.getUrl());
      responseComplete(dasSource, seq);
    }
  }

  /**
   * examine the given sequence feature to determine if it should actually be
   * turned into sequence annotation or database cross references rather than a
   * simple sequence feature.
   * 
   * @param seq
   *          the sequence to annotate
   * @param f
   *          the jalview sequence feature generated from the DAS feature
   * @param map
   *          the sequence feature attributes
   * @param source
   *          the source that emitted the feature
   * @return true if feature was consumed as another kind of annotation.
   */
  protected boolean parseSeqFeature(SequenceI seq, SequenceFeature f,
          Map map, Das1Source source)
  {
    SequenceI mseq = seq;
    while (seq.getDatasetSequence() != null)
    {
      seq = seq.getDatasetSequence();
    }
    if (f.getType() != null)
    {
      String type = f.getType();
      if (type.equalsIgnoreCase("protein_name"))
      {
        // parse name onto the alignment sequence or the dataset sequence.
        if (seq.getDescription() == null
                || seq.getDescription().trim().length() == 0)
        {
          // could look at the note series to pick out the first long name, for
          // the moment just use the whole description string
          seq.setDescription(f.getDescription());
        }
        if (mseq.getDescription() == null
                || mseq.getDescription().trim().length() == 0)
        {
          // could look at the note series to pick out the first long name, for
          // the moment just use the whole description string
          mseq.setDescription(f.getDescription());
        }
        return true;
      }
      // check if source has biosapiens or other sequence ontology label
      if (type.equalsIgnoreCase("DBXREF") || type.equalsIgnoreCase("DBREF"))
      {
        // try to parse the accession out

        DBRefEntry dbr = new DBRefEntry();
        dbr.setVersion(source.getNickname());
        StringTokenizer st = new StringTokenizer(f.getDescription(), ":");
        if (st.hasMoreTokens())
        {
          dbr.setSource(st.nextToken());
        }
        if (st.hasMoreTokens())
        {
          dbr.setAccessionId(st.nextToken());
        }
        seq.addDBRef(dbr);

        if (f.links != null && f.links.size() > 0)
        {
          // feature is also appended to enable links to be seen.
          // TODO: consider extending dbrefs to have their own links ?
          // TODO: new feature: extract dbref links from DAS servers and add the
          // URL pattern to the list of DB name associated links in the user's
          // preferences ?
          // for the moment - just fix up the existing feature so it displays
          // correctly.
          // f.setType(dbr.getSource());
          // f.setDescription();
          f.setValue("linkonly", Boolean.TRUE);
          // f.setDescription("");
          Vector newlinks = new Vector();
          Enumeration it = f.links.elements();
          while (it.hasMoreElements())
          {
            String elm;
            UrlLink urllink = new UrlLink(elm = (String) it.nextElement());
            if (urllink.isValid())
            {
              urllink.setLabel(f.getDescription());
              newlinks.addElement(urllink.toString());
            }
            else
            {
              // couldn't parse the link properly. Keep it anyway - just in
              // case.
              debug("couldn't parse link string - " + elm);
              newlinks.addElement(elm);
            }
          }
          f.links = newlinks;
          seq.addSequenceFeature(f);
        }
        return true;
      }
    }
    return false;
  }

  /**
   * creates a jalview sequence feature from a das feature document
   * 
   * @param dasfeature
   * @return sequence feature object created using dasfeature information
   */
  SequenceFeature newSequenceFeature(Map dasfeature, String nickname)
  {
    if (dasfeature == null)
    {
      return null;
    }
    try
    {
      /**
       * Different qNames for a DAS Feature - are string keys to the HashMaps in
       * features "METHOD") || qName.equals("TYPE") || qName.equals("START") ||
       * qName.equals("END") || qName.equals("NOTE") || qName.equals("LINK") ||
       * qName.equals("SCORE")
       */
      String desc = new String();
      if (dasfeature.containsKey("NOTE"))
      {
        desc += (String) dasfeature.get("NOTE");
      }

      int start = 0, end = 0;
      float score = 0f;

      try
      {
        start = Integer.parseInt(dasfeature.get("START").toString());
      } catch (Exception ex)
      {
      }
      try
      {
        end = Integer.parseInt(dasfeature.get("END").toString());
      } catch (Exception ex)
      {
      }
      try
      {
        Object scr = dasfeature.get("SCORE");
        if (scr != null)
        {
          score = (float) Double.parseDouble(scr.toString());

        }
      } catch (Exception ex)
      {
      }

      SequenceFeature f = new SequenceFeature(
              (String) dasfeature.get("TYPE"), desc, start, end, score,
              nickname);

      if (dasfeature.containsKey("LINK"))
      {
        // Do not put feature extent in link text for non-positional features
        if (f.begin == 0 && f.end == 0)
        {
          f.addLink(f.getType() + "|" + dasfeature.get("LINK"));
        }
        else
        {
          f.addLink(f.getType() + " " + f.begin + "_" + f.end + "|"
                  + dasfeature.get("LINK"));
        }
      }

      return f;
    } catch (Exception e)
    {
      System.out.println("ERRR " + e);
      e.printStackTrace();
      System.out.println("############");
      debug("Failed to parse " + dasfeature.toString(), e);
      return null;
    }
  }

  /**
   * query the default DAS Source Registry for sources. Uses value of jalview
   * property DAS_REGISTRY_URL and the DasSourceBrowser.DEFAULT_REGISTRY if that
   * doesn't exist.
   * 
   * @return list of sources
   */
  public static DasSource[] getDASSources()
  {

    String registryURL = jalview.bin.Cache.getDefault("DAS_REGISTRY_URL",
            DasSourceBrowser.DEFAULT_REGISTRY);
    return getDASSources(registryURL);
  }

  /**
   * query the given URL for DasSources.
   * 
   * @param registryURL
   *          return sources from registryURL
   */
  public static DasSource[] getDASSources(String registryURL)
  {
    DasSourceReaderImpl reader = new DasSourceReaderImpl();

    try
    {
      URL url = new URL(registryURL);

      DasSource[] sources = reader.readDasSource(url);

      List das1sources = new ArrayList();
      for (int i = 0; i < sources.length; i++)
      {
        DasSource ds = sources[i];
        if (ds instanceof Das2Source)
        {
          Das2Source d2s = (Das2Source) ds;
          if (d2s.hasDas1Capabilities())
          {
            Das1Source d1s = DasSourceConverter.toDas1Source(d2s);
            das1sources.add(d1s);
          }

        }
        else if (ds instanceof Das1Source)
        {
          das1sources.add((Das1Source) ds);
        }
      }

      return (Das1Source[]) das1sources.toArray(new Das1Source[das1sources
              .size()]);
    } catch (Exception ex)
    {
      System.err.println("Failed to contact DAS1 registry at "
              + registryURL);
      ex.printStackTrace();
      return null;
    }
  }

}
