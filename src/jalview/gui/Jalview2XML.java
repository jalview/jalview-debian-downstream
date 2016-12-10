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

import jalview.analysis.Conservation;
import jalview.api.FeatureColourI;
import jalview.api.ViewStyleI;
import jalview.api.structures.JalviewStructureDisplayI;
import jalview.bin.Cache;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.RnaViewerModel;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.StructureViewerModel;
import jalview.datamodel.StructureViewerModel.StructureData;
import jalview.ext.varna.RnaModel;
import jalview.gui.StructureViewer.ViewerType;
import jalview.schemabinding.version2.AlcodMap;
import jalview.schemabinding.version2.AlcodonFrame;
import jalview.schemabinding.version2.Annotation;
import jalview.schemabinding.version2.AnnotationColours;
import jalview.schemabinding.version2.AnnotationElement;
import jalview.schemabinding.version2.CalcIdParam;
import jalview.schemabinding.version2.DBRef;
import jalview.schemabinding.version2.Features;
import jalview.schemabinding.version2.Group;
import jalview.schemabinding.version2.HiddenColumns;
import jalview.schemabinding.version2.JGroup;
import jalview.schemabinding.version2.JSeq;
import jalview.schemabinding.version2.JalviewModel;
import jalview.schemabinding.version2.JalviewModelSequence;
import jalview.schemabinding.version2.MapListFrom;
import jalview.schemabinding.version2.MapListTo;
import jalview.schemabinding.version2.Mapping;
import jalview.schemabinding.version2.MappingChoice;
import jalview.schemabinding.version2.OtherData;
import jalview.schemabinding.version2.PdbentryItem;
import jalview.schemabinding.version2.Pdbids;
import jalview.schemabinding.version2.Property;
import jalview.schemabinding.version2.RnaViewer;
import jalview.schemabinding.version2.SecondaryStructure;
import jalview.schemabinding.version2.Sequence;
import jalview.schemabinding.version2.SequenceSet;
import jalview.schemabinding.version2.SequenceSetProperties;
import jalview.schemabinding.version2.Setting;
import jalview.schemabinding.version2.StructureState;
import jalview.schemabinding.version2.ThresholdLine;
import jalview.schemabinding.version2.Tree;
import jalview.schemabinding.version2.UserColours;
import jalview.schemabinding.version2.Viewport;
import jalview.schemes.AnnotationColourGradient;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.FeatureColour;
import jalview.schemes.ResidueColourScheme;
import jalview.schemes.ResidueProperties;
import jalview.schemes.UserColourScheme;
import jalview.structure.StructureSelectionManager;
import jalview.structures.models.AAStructureBindingModel;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.StringUtils;
import jalview.util.jarInputStreamProvider;
import jalview.viewmodel.AlignmentViewport;
import jalview.viewmodel.seqfeatures.FeatureRendererSettings;
import jalview.viewmodel.seqfeatures.FeaturesDisplayed;
import jalview.ws.jws2.Jws2Discoverer;
import jalview.ws.jws2.dm.AAConSettings;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.AutoCalcSetting;
import jalview.ws.params.WsParamSetI;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.Unmarshaller;

/**
 * Write out the current jalview desktop state as a Jalview XML stream.
 * 
 * Note: the vamsas objects referred to here are primitive versions of the
 * VAMSAS project schema elements - they are not the same and most likely never
 * will be :)
 * 
 * @author $author$
 * @version $Revision: 1.134 $
 */
public class Jalview2XML
{
  private static final String VIEWER_PREFIX = "viewer_";

  private static final String RNA_PREFIX = "rna_";

  private static final String UTF_8 = "UTF-8";

  // use this with nextCounter() to make unique names for entities
  private int counter = 0;

  /*
   * SequenceI reference -> XML ID string in jalview XML. Populated as XML reps
   * of sequence objects are created.
   */
  IdentityHashMap<SequenceI, String> seqsToIds = null;

  /**
   * jalview XML Sequence ID to jalview sequence object reference (both dataset
   * and alignment sequences. Populated as XML reps of sequence objects are
   * created.)
   */
  Map<String, SequenceI> seqRefIds = null;

  Map<String, SequenceI> incompleteSeqs = null;

  List<SeqFref> frefedSequence = null;

  boolean raiseGUI = true; // whether errors are raised in dialog boxes or not

  /*
   * Map of reconstructed AlignFrame objects that appear to have come from
   * SplitFrame objects (have a dna/protein complement view).
   */
  private Map<Viewport, AlignFrame> splitFrameCandidates = new HashMap<Viewport, AlignFrame>();

  /*
   * Map from displayed rna structure models to their saved session state jar
   * entry names
   */
  private Map<RnaModel, String> rnaSessions = new HashMap<RnaModel, String>();

  /**
   * create/return unique hash string for sq
   * 
   * @param sq
   * @return new or existing unique string for sq
   */
  String seqHash(SequenceI sq)
  {
    if (seqsToIds == null)
    {
      initSeqRefs();
    }
    if (seqsToIds.containsKey(sq))
    {
      return seqsToIds.get(sq);
    }
    else
    {
      // create sequential key
      String key = "sq" + (seqsToIds.size() + 1);
      key = makeHashCode(sq, key); // check we don't have an external reference
      // for it already.
      seqsToIds.put(sq, key);
      return key;
    }
  }

  void clearSeqRefs()
  {
    if (_cleartables)
    {
      if (seqRefIds != null)
      {
        seqRefIds.clear();
      }
      if (seqsToIds != null)
      {
        seqsToIds.clear();
      }
      if (incompleteSeqs != null)
      {
        incompleteSeqs.clear();
      }
      // seqRefIds = null;
      // seqsToIds = null;
    }
    else
    {
      // do nothing
      warn("clearSeqRefs called when _cleartables was not set. Doing nothing.");
      // seqRefIds = new Hashtable();
      // seqsToIds = new IdentityHashMap();
    }
  }

  void initSeqRefs()
  {
    if (seqsToIds == null)
    {
      seqsToIds = new IdentityHashMap<SequenceI, String>();
    }
    if (seqRefIds == null)
    {
      seqRefIds = new HashMap<String, SequenceI>();
    }
    if (incompleteSeqs == null)
    {
      incompleteSeqs = new HashMap<String, SequenceI>();
    }
    if (frefedSequence == null)
    {
      frefedSequence = new ArrayList<SeqFref>();
    }
  }

  public Jalview2XML()
  {
  }

  public Jalview2XML(boolean raiseGUI)
  {
    this.raiseGUI = raiseGUI;
  }

  /**
   * base class for resolving forward references to sequences by their ID
   * 
   * @author jprocter
   *
   */
  abstract class SeqFref
  {
    String sref;

    String type;

    public SeqFref(String _sref, String type)
    {
      sref = _sref;
      this.type = type;
    }

    public String getSref()
    {
      return sref;
    }

    public SequenceI getSrefSeq()
    {
      return seqRefIds.get(sref);
    }

    public boolean isResolvable()
    {
      return seqRefIds.get(sref) != null;
    }

    public SequenceI getSrefDatasetSeq()
    {
      SequenceI sq = seqRefIds.get(sref);
      if (sq != null)
      {
        while (sq.getDatasetSequence() != null)
        {
          sq = sq.getDatasetSequence();
        }
      }
      return sq;
    }

    /**
     * @return true if the forward reference was fully resolved
     */
    abstract boolean resolve();

    @Override
    public String toString()
    {
      return type + " reference to " + sref;
    }
  }

  /**
   * create forward reference for a mapping
   * 
   * @param sref
   * @param _jmap
   * @return
   */
  public SeqFref newMappingRef(final String sref,
          final jalview.datamodel.Mapping _jmap)
  {
    SeqFref fref = new SeqFref(sref, "Mapping")
    {
      public jalview.datamodel.Mapping jmap = _jmap;

      @Override
      boolean resolve()
      {
        SequenceI seq = getSrefDatasetSeq();
        if (seq == null)
        {
          return false;
        }
        jmap.setTo(seq);
        return true;
      }
    };
    return fref;
  }

  public SeqFref newAlcodMapRef(final String sref,
          final AlignedCodonFrame _cf, final jalview.datamodel.Mapping _jmap)
  {

    SeqFref fref = new SeqFref(sref, "Codon Frame")
    {
      AlignedCodonFrame cf = _cf;

      public jalview.datamodel.Mapping mp = _jmap;

      @Override
      public boolean isResolvable()
      {
        return super.isResolvable() && mp.getTo() != null;
      };

      @Override
      boolean resolve()
      {
        SequenceI seq = getSrefDatasetSeq();
        if (seq == null)
        {
          return false;
        }
        cf.addMap(seq, mp.getTo(), mp.getMap());
        return true;
      }
    };
    return fref;
  }

  public void resolveFrefedSequences()
  {
    Iterator<SeqFref> nextFref = frefedSequence.iterator();
    int toresolve = frefedSequence.size();
    int unresolved = 0, failedtoresolve = 0;
    while (nextFref.hasNext())
    {
      SeqFref ref = nextFref.next();
      if (ref.isResolvable())
      {
        try
        {
          if (ref.resolve())
          {
            nextFref.remove();
          }
          else
          {
            failedtoresolve++;
          }
        } catch (Exception x)
        {
          System.err
                  .println("IMPLEMENTATION ERROR: Failed to resolve forward reference for sequence "
                          + ref.getSref());
          x.printStackTrace();
          failedtoresolve++;
        }
      }
      else
      {
        unresolved++;
      }
    }
    if (unresolved > 0)
    {
      System.err.println("Jalview Project Import: There were " + unresolved
              + " forward references left unresolved on the stack.");
    }
    if (failedtoresolve > 0)
    {
      System.err.println("SERIOUS! " + failedtoresolve
              + " resolvable forward references failed to resolve.");
    }
    if (incompleteSeqs != null && incompleteSeqs.size() > 0)
    {
      System.err.println("Jalview Project Import: There are "
              + incompleteSeqs.size()
              + " sequences which may have incomplete metadata.");
      if (incompleteSeqs.size() < 10)
      {
        for (SequenceI s : incompleteSeqs.values())
        {
          System.err.println(s.toString());
        }
      }
      else
      {
        System.err
                .println("Too many to report. Skipping output of incomplete sequences.");
      }
    }
  }

  /**
   * This maintains a map of viewports, the key being the seqSetId. Important to
   * set historyItem and redoList for multiple views
   */
  Map<String, AlignViewport> viewportsAdded = new HashMap<String, AlignViewport>();

  Map<String, AlignmentAnnotation> annotationIds = new HashMap<String, AlignmentAnnotation>();

  String uniqueSetSuffix = "";

  /**
   * List of pdbfiles added to Jar
   */
  List<String> pdbfiles = null;

  // SAVES SEVERAL ALIGNMENT WINDOWS TO SAME JARFILE
  public void saveState(File statefile)
  {
    FileOutputStream fos = null;
    try
    {
      fos = new FileOutputStream(statefile);
      JarOutputStream jout = new JarOutputStream(fos);
      saveState(jout);

    } catch (Exception e)
    {
      // TODO: inform user of the problem - they need to know if their data was
      // not saved !
      if (errorMessage == null)
      {
        errorMessage = "Couldn't write Jalview Archive to output file '"
                + statefile + "' - See console error log for details";
      }
      else
      {
        errorMessage += "(output file was '" + statefile + "')";
      }
      e.printStackTrace();
    } finally
    {
      if (fos != null)
      {
        try
        {
          fos.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
    reportErrors();
  }

  /**
   * Writes a jalview project archive to the given Jar output stream.
   * 
   * @param jout
   */
  public void saveState(JarOutputStream jout)
  {
    AlignFrame[] frames = Desktop.getAlignFrames();

    if (frames == null)
    {
      return;
    }
    saveAllFrames(Arrays.asList(frames), jout);
  }

  /**
   * core method for storing state for a set of AlignFrames.
   * 
   * @param frames
   *          - frames involving all data to be exported (including containing
   *          splitframes)
   * @param jout
   *          - project output stream
   */
  private void saveAllFrames(List<AlignFrame> frames, JarOutputStream jout)
  {
    Hashtable<String, AlignFrame> dsses = new Hashtable<String, AlignFrame>();

    /*
     * ensure cached data is clear before starting
     */
    // todo tidy up seqRefIds, seqsToIds initialisation / reset
    rnaSessions.clear();
    splitFrameCandidates.clear();

    try
    {

      // NOTE UTF-8 MUST BE USED FOR WRITING UNICODE CHARS
      // //////////////////////////////////////////////////

      List<String> shortNames = new ArrayList<String>();
      List<String> viewIds = new ArrayList<String>();

      // REVERSE ORDER
      for (int i = frames.size() - 1; i > -1; i--)
      {
        AlignFrame af = frames.get(i);
        // skip ?
        if (skipList != null
                && skipList
                        .containsKey(af.getViewport().getSequenceSetId()))
        {
          continue;
        }

        String shortName = makeFilename(af, shortNames);

        int ap, apSize = af.alignPanels.size();

        for (ap = 0; ap < apSize; ap++)
        {
          AlignmentPanel apanel = af.alignPanels.get(ap);
          String fileName = apSize == 1 ? shortName : ap + shortName;
          if (!fileName.endsWith(".xml"))
          {
            fileName = fileName + ".xml";
          }

          saveState(apanel, fileName, jout, viewIds);

          String dssid = getDatasetIdRef(af.getViewport().getAlignment()
                  .getDataset());
          if (!dsses.containsKey(dssid))
          {
            dsses.put(dssid, af);
          }
        }
      }

      writeDatasetFor(dsses, "" + jout.hashCode() + " " + uniqueSetSuffix,
              jout);

      try
      {
        jout.flush();
      } catch (Exception foo)
      {
      }
      ;
      jout.close();
    } catch (Exception ex)
    {
      // TODO: inform user of the problem - they need to know if their data was
      // not saved !
      if (errorMessage == null)
      {
        errorMessage = "Couldn't write Jalview Archive - see error output for details";
      }
      ex.printStackTrace();
    }
  }

  /**
   * Generates a distinct file name, based on the title of the AlignFrame, by
   * appending _n for increasing n until an unused name is generated. The new
   * name (without its extension) is added to the list.
   * 
   * @param af
   * @param namesUsed
   * @return the generated name, with .xml extension
   */
  protected String makeFilename(AlignFrame af, List<String> namesUsed)
  {
    String shortName = af.getTitle();

    if (shortName.indexOf(File.separatorChar) > -1)
    {
      shortName = shortName.substring(shortName
              .lastIndexOf(File.separatorChar) + 1);
    }

    int count = 1;

    while (namesUsed.contains(shortName))
    {
      if (shortName.endsWith("_" + (count - 1)))
      {
        shortName = shortName.substring(0, shortName.lastIndexOf("_"));
      }

      shortName = shortName.concat("_" + count);
      count++;
    }

    namesUsed.add(shortName);

    if (!shortName.endsWith(".xml"))
    {
      shortName = shortName + ".xml";
    }
    return shortName;
  }

  // USE THIS METHOD TO SAVE A SINGLE ALIGNMENT WINDOW
  public boolean saveAlignment(AlignFrame af, String jarFile,
          String fileName)
  {
    try
    {
      FileOutputStream fos = new FileOutputStream(jarFile);
      JarOutputStream jout = new JarOutputStream(fos);
      List<AlignFrame> frames = new ArrayList<AlignFrame>();

      // resolve splitframes
      if (af.getViewport().getCodingComplement() != null)
      {
        frames = ((SplitFrame) af.getSplitViewContainer()).getAlignFrames();
      }
      else
      {
        frames.add(af);
      }
      saveAllFrames(frames, jout);
      try
      {
        jout.flush();
      } catch (Exception foo)
      {
      }
      ;
      jout.close();
      return true;
    } catch (Exception ex)
    {
      errorMessage = "Couldn't Write alignment view to Jalview Archive - see error output for details";
      ex.printStackTrace();
      return false;
    }
  }

  private void writeDatasetFor(Hashtable<String, AlignFrame> dsses,
          String fileName, JarOutputStream jout)
  {

    for (String dssids : dsses.keySet())
    {
      AlignFrame _af = dsses.get(dssids);
      String jfileName = fileName + " Dataset for " + _af.getTitle();
      if (!jfileName.endsWith(".xml"))
      {
        jfileName = jfileName + ".xml";
      }
      saveState(_af.alignPanel, jfileName, true, jout, null);
    }
  }

  /**
   * create a JalviewModel from an alignment view and marshall it to a
   * JarOutputStream
   * 
   * @param ap
   *          panel to create jalview model for
   * @param fileName
   *          name of alignment panel written to output stream
   * @param jout
   *          jar output stream
   * @param viewIds
   * @param out
   *          jar entry name
   */
  public JalviewModel saveState(AlignmentPanel ap, String fileName,
          JarOutputStream jout, List<String> viewIds)
  {
    return saveState(ap, fileName, false, jout, viewIds);
  }

  /**
   * create a JalviewModel from an alignment view and marshall it to a
   * JarOutputStream
   * 
   * @param ap
   *          panel to create jalview model for
   * @param fileName
   *          name of alignment panel written to output stream
   * @param storeDS
   *          when true, only write the dataset for the alignment, not the data
   *          associated with the view.
   * @param jout
   *          jar output stream
   * @param out
   *          jar entry name
   */
  public JalviewModel saveState(AlignmentPanel ap, String fileName,
          boolean storeDS, JarOutputStream jout, List<String> viewIds)
  {
    if (viewIds == null)
    {
      viewIds = new ArrayList<String>();
    }

    initSeqRefs();

    List<UserColourScheme> userColours = new ArrayList<UserColourScheme>();

    AlignViewport av = ap.av;

    JalviewModel object = new JalviewModel();
    object.setVamsasModel(new jalview.schemabinding.version2.VamsasModel());

    object.setCreationDate(new java.util.Date(System.currentTimeMillis()));
    object.setVersion(jalview.bin.Cache.getDefault("VERSION",
            "Development Build"));

    /**
     * rjal is full height alignment, jal is actual alignment with full metadata
     * but excludes hidden sequences.
     */
    jalview.datamodel.AlignmentI rjal = av.getAlignment(), jal = rjal;

    if (av.hasHiddenRows())
    {
      rjal = jal.getHiddenSequences().getFullAlignment();
    }

    SequenceSet vamsasSet = new SequenceSet();
    Sequence vamsasSeq;
    JalviewModelSequence jms = new JalviewModelSequence();

    vamsasSet.setGapChar(jal.getGapCharacter() + "");

    if (jal.getDataset() != null)
    {
      // dataset id is the dataset's hashcode
      vamsasSet.setDatasetId(getDatasetIdRef(jal.getDataset()));
      if (storeDS)
      {
        // switch jal and the dataset
        jal = jal.getDataset();
        rjal = jal;
      }
    }
    if (jal.getProperties() != null)
    {
      Enumeration en = jal.getProperties().keys();
      while (en.hasMoreElements())
      {
        String key = en.nextElement().toString();
        SequenceSetProperties ssp = new SequenceSetProperties();
        ssp.setKey(key);
        ssp.setValue(jal.getProperties().get(key).toString());
        vamsasSet.addSequenceSetProperties(ssp);
      }
    }

    JSeq jseq;
    Set<String> calcIdSet = new HashSet<String>();
    // record the set of vamsas sequence XML POJO we create.
    HashMap<String, Sequence> vamsasSetIds = new HashMap<String, Sequence>();
    // SAVE SEQUENCES
    for (final SequenceI jds : rjal.getSequences())
    {
      final SequenceI jdatasq = jds.getDatasetSequence() == null ? jds
              : jds.getDatasetSequence();
      String id = seqHash(jds);
      if (vamsasSetIds.get(id) == null)
      {
        if (seqRefIds.get(id) != null && !storeDS)
        {
          // This happens for two reasons: 1. multiple views are being
          // serialised.
          // 2. the hashCode has collided with another sequence's code. This
          // DOES
          // HAPPEN! (PF00072.15.stk does this)
          // JBPNote: Uncomment to debug writing out of files that do not read
          // back in due to ArrayOutOfBoundExceptions.
          // System.err.println("vamsasSeq backref: "+id+"");
          // System.err.println(jds.getName()+"
          // "+jds.getStart()+"-"+jds.getEnd()+" "+jds.getSequenceAsString());
          // System.err.println("Hashcode: "+seqHash(jds));
          // SequenceI rsq = (SequenceI) seqRefIds.get(id + "");
          // System.err.println(rsq.getName()+"
          // "+rsq.getStart()+"-"+rsq.getEnd()+" "+rsq.getSequenceAsString());
          // System.err.println("Hashcode: "+seqHash(rsq));
        }
        else
        {
          vamsasSeq = createVamsasSequence(id, jds);
          vamsasSet.addSequence(vamsasSeq);
          vamsasSetIds.put(id, vamsasSeq);
          seqRefIds.put(id, jds);
        }
      }
      jseq = new JSeq();
      jseq.setStart(jds.getStart());
      jseq.setEnd(jds.getEnd());
      jseq.setColour(av.getSequenceColour(jds).getRGB());

      jseq.setId(id); // jseq id should be a string not a number
      if (!storeDS)
      {
        // Store any sequences this sequence represents
        if (av.hasHiddenRows())
        {
          // use rjal, contains the full height alignment
          jseq.setHidden(av.getAlignment().getHiddenSequences()
                  .isHidden(jds));

          if (av.isHiddenRepSequence(jds))
          {
            jalview.datamodel.SequenceI[] reps = av
                    .getRepresentedSequences(jds).getSequencesInOrder(rjal);

            for (int h = 0; h < reps.length; h++)
            {
              if (reps[h] != jds)
              {
                jseq.addHiddenSequences(rjal.findIndex(reps[h]));
              }
            }
          }
        }
        // mark sequence as reference - if it is the reference for this view
        if (jal.hasSeqrep())
        {
          jseq.setViewreference(jds == jal.getSeqrep());
        }
      }

      // TODO: omit sequence features from each alignment view's XML dump if we
      // are storing dataset
      if (jds.getSequenceFeatures() != null)
      {
        jalview.datamodel.SequenceFeature[] sf = jds.getSequenceFeatures();
        int index = 0;
        while (index < sf.length)
        {
          Features features = new Features();

          features.setBegin(sf[index].getBegin());
          features.setEnd(sf[index].getEnd());
          features.setDescription(sf[index].getDescription());
          features.setType(sf[index].getType());
          features.setFeatureGroup(sf[index].getFeatureGroup());
          features.setScore(sf[index].getScore());
          if (sf[index].links != null)
          {
            for (int l = 0; l < sf[index].links.size(); l++)
            {
              OtherData keyValue = new OtherData();
              keyValue.setKey("LINK_" + l);
              keyValue.setValue(sf[index].links.elementAt(l).toString());
              features.addOtherData(keyValue);
            }
          }
          if (sf[index].otherDetails != null)
          {
            String key;
            Iterator<String> keys = sf[index].otherDetails.keySet()
                    .iterator();
            while (keys.hasNext())
            {
              key = keys.next();
              OtherData keyValue = new OtherData();
              keyValue.setKey(key);
              keyValue.setValue(sf[index].otherDetails.get(key).toString());
              features.addOtherData(keyValue);
            }
          }

          jseq.addFeatures(features);
          index++;
        }
      }

      if (jdatasq.getAllPDBEntries() != null)
      {
        Enumeration en = jdatasq.getAllPDBEntries().elements();
        while (en.hasMoreElements())
        {
          Pdbids pdb = new Pdbids();
          jalview.datamodel.PDBEntry entry = (jalview.datamodel.PDBEntry) en
                  .nextElement();

          String pdbId = entry.getId();
          pdb.setId(pdbId);
          pdb.setType(entry.getType());

          /*
           * Store any structure views associated with this sequence. This
           * section copes with duplicate entries in the project, so a dataset
           * only view *should* be coped with sensibly.
           */
          // This must have been loaded, is it still visible?
          JInternalFrame[] frames = Desktop.desktop.getAllFrames();
          String matchedFile = null;
          for (int f = frames.length - 1; f > -1; f--)
          {
            if (frames[f] instanceof StructureViewerBase)
            {
              StructureViewerBase viewFrame = (StructureViewerBase) frames[f];
              matchedFile = saveStructureState(ap, jds, pdb, entry,
                      viewIds, matchedFile, viewFrame);
              /*
               * Only store each structure viewer's state once in the project
               * jar. First time through only (storeDS==false)
               */
              String viewId = viewFrame.getViewId();
              if (!storeDS && !viewIds.contains(viewId))
              {
                viewIds.add(viewId);
                try
                {
                  String viewerState = viewFrame.getStateInfo();
                  writeJarEntry(jout, getViewerJarEntryName(viewId),
                          viewerState.getBytes());
                } catch (IOException e)
                {
                  System.err.println("Error saving viewer state: "
                          + e.getMessage());
                }
              }
            }
          }

          if (matchedFile != null || entry.getFile() != null)
          {
            if (entry.getFile() != null)
            {
              // use entry's file
              matchedFile = entry.getFile();
            }
            pdb.setFile(matchedFile); // entry.getFile());
            if (pdbfiles == null)
            {
              pdbfiles = new ArrayList<String>();
            }

            if (!pdbfiles.contains(pdbId))
            {
              pdbfiles.add(pdbId);
              copyFileToJar(jout, matchedFile, pdbId);
            }
          }

          Enumeration<String> props = entry.getProperties();
          if (props.hasMoreElements())
          {
            PdbentryItem item = new PdbentryItem();
            while (props.hasMoreElements())
            {
              Property prop = new Property();
              String key = props.nextElement();
              prop.setName(key);
              prop.setValue(entry.getProperty(key).toString());
              item.addProperty(prop);
            }
            pdb.addPdbentryItem(item);
          }

          jseq.addPdbids(pdb);
        }
      }

      saveRnaViewers(jout, jseq, jds, viewIds, ap, storeDS);

      jms.addJSeq(jseq);
    }

    if (!storeDS && av.hasHiddenRows())
    {
      jal = av.getAlignment();
    }
    // SAVE MAPPINGS
    // FOR DATASET
    if (storeDS && jal.getCodonFrames() != null)
    {
      List<AlignedCodonFrame> jac = jal.getCodonFrames();
      for (AlignedCodonFrame acf : jac)
      {
        AlcodonFrame alc = new AlcodonFrame();
        if (acf.getProtMappings() != null
                && acf.getProtMappings().length > 0)
        {
          boolean hasMap = false;
          SequenceI[] dnas = acf.getdnaSeqs();
          jalview.datamodel.Mapping[] pmaps = acf.getProtMappings();
          for (int m = 0; m < pmaps.length; m++)
          {
            AlcodMap alcmap = new AlcodMap();
            alcmap.setDnasq(seqHash(dnas[m]));
            alcmap.setMapping(createVamsasMapping(pmaps[m], dnas[m], null,
                    false));
            alc.addAlcodMap(alcmap);
            hasMap = true;
          }
          if (hasMap)
          {
            vamsasSet.addAlcodonFrame(alc);
          }
        }
        // TODO: delete this ? dead code from 2.8.3->2.9 ?
        // {
        // AlcodonFrame alc = new AlcodonFrame();
        // vamsasSet.addAlcodonFrame(alc);
        // for (int p = 0; p < acf.aaWidth; p++)
        // {
        // Alcodon cmap = new Alcodon();
        // if (acf.codons[p] != null)
        // {
        // // Null codons indicate a gapped column in the translated peptide
        // // alignment.
        // cmap.setPos1(acf.codons[p][0]);
        // cmap.setPos2(acf.codons[p][1]);
        // cmap.setPos3(acf.codons[p][2]);
        // }
        // alc.addAlcodon(cmap);
        // }
        // if (acf.getProtMappings() != null
        // && acf.getProtMappings().length > 0)
        // {
        // SequenceI[] dnas = acf.getdnaSeqs();
        // jalview.datamodel.Mapping[] pmaps = acf.getProtMappings();
        // for (int m = 0; m < pmaps.length; m++)
        // {
        // AlcodMap alcmap = new AlcodMap();
        // alcmap.setDnasq(seqHash(dnas[m]));
        // alcmap.setMapping(createVamsasMapping(pmaps[m], dnas[m], null,
        // false));
        // alc.addAlcodMap(alcmap);
        // }
        // }
      }
    }

    // SAVE TREES
    // /////////////////////////////////
    if (!storeDS && av.currentTree != null)
    {
      // FIND ANY ASSOCIATED TREES
      // NOT IMPLEMENTED FOR HEADLESS STATE AT PRESENT
      if (Desktop.desktop != null)
      {
        JInternalFrame[] frames = Desktop.desktop.getAllFrames();

        for (int t = 0; t < frames.length; t++)
        {
          if (frames[t] instanceof TreePanel)
          {
            TreePanel tp = (TreePanel) frames[t];

            if (tp.treeCanvas.av.getAlignment() == jal)
            {
              Tree tree = new Tree();
              tree.setTitle(tp.getTitle());
              tree.setCurrentTree((av.currentTree == tp.getTree()));
              tree.setNewick(tp.getTree().toString());
              tree.setThreshold(tp.treeCanvas.threshold);

              tree.setFitToWindow(tp.fitToWindow.getState());
              tree.setFontName(tp.getTreeFont().getName());
              tree.setFontSize(tp.getTreeFont().getSize());
              tree.setFontStyle(tp.getTreeFont().getStyle());
              tree.setMarkUnlinked(tp.placeholdersMenu.getState());

              tree.setShowBootstrap(tp.bootstrapMenu.getState());
              tree.setShowDistances(tp.distanceMenu.getState());

              tree.setHeight(tp.getHeight());
              tree.setWidth(tp.getWidth());
              tree.setXpos(tp.getX());
              tree.setYpos(tp.getY());
              tree.setId(makeHashCode(tp, null));
              jms.addTree(tree);
            }
          }
        }
      }
    }

    // SAVE ANNOTATIONS
    /**
     * store forward refs from an annotationRow to any groups
     */
    IdentityHashMap<SequenceGroup, String> groupRefs = new IdentityHashMap<SequenceGroup, String>();
    if (storeDS)
    {
      for (SequenceI sq : jal.getSequences())
      {
        // Store annotation on dataset sequences only
        AlignmentAnnotation[] aa = sq.getAnnotation();
        if (aa != null && aa.length > 0)
        {
          storeAlignmentAnnotation(aa, groupRefs, av, calcIdSet, storeDS,
                  vamsasSet);
        }
      }
    }
    else
    {
      if (jal.getAlignmentAnnotation() != null)
      {
        // Store the annotation shown on the alignment.
        AlignmentAnnotation[] aa = jal.getAlignmentAnnotation();
        storeAlignmentAnnotation(aa, groupRefs, av, calcIdSet, storeDS,
                vamsasSet);
      }
    }
    // SAVE GROUPS
    if (jal.getGroups() != null)
    {
      JGroup[] groups = new JGroup[jal.getGroups().size()];
      int i = -1;
      for (jalview.datamodel.SequenceGroup sg : jal.getGroups())
      {
        JGroup jGroup = new JGroup();
        groups[++i] = jGroup;

        jGroup.setStart(sg.getStartRes());
        jGroup.setEnd(sg.getEndRes());
        jGroup.setName(sg.getName());
        if (groupRefs.containsKey(sg))
        {
          // group has references so set its ID field
          jGroup.setId(groupRefs.get(sg));
        }
        if (sg.cs != null)
        {
          if (sg.cs.conservationApplied())
          {
            jGroup.setConsThreshold(sg.cs.getConservationInc());

            if (sg.cs instanceof jalview.schemes.UserColourScheme)
            {
              jGroup.setColour(setUserColourScheme(sg.cs, userColours, jms));
            }
            else
            {
              jGroup.setColour(ColourSchemeProperty.getColourName(sg.cs));
            }
          }
          else if (sg.cs instanceof jalview.schemes.AnnotationColourGradient)
          {
            jGroup.setColour("AnnotationColourGradient");
            jGroup.setAnnotationColours(constructAnnotationColours(
                    (jalview.schemes.AnnotationColourGradient) sg.cs,
                    userColours, jms));
          }
          else if (sg.cs instanceof jalview.schemes.UserColourScheme)
          {
            jGroup.setColour(setUserColourScheme(sg.cs, userColours, jms));
          }
          else
          {
            jGroup.setColour(ColourSchemeProperty.getColourName(sg.cs));
          }

          jGroup.setPidThreshold(sg.cs.getThreshold());
        }

        jGroup.setOutlineColour(sg.getOutlineColour().getRGB());
        jGroup.setDisplayBoxes(sg.getDisplayBoxes());
        jGroup.setDisplayText(sg.getDisplayText());
        jGroup.setColourText(sg.getColourText());
        jGroup.setTextCol1(sg.textColour.getRGB());
        jGroup.setTextCol2(sg.textColour2.getRGB());
        jGroup.setTextColThreshold(sg.thresholdTextColour);
        jGroup.setShowUnconserved(sg.getShowNonconserved());
        jGroup.setIgnoreGapsinConsensus(sg.getIgnoreGapsConsensus());
        jGroup.setShowConsensusHistogram(sg.isShowConsensusHistogram());
        jGroup.setShowSequenceLogo(sg.isShowSequenceLogo());
        jGroup.setNormaliseSequenceLogo(sg.isNormaliseSequenceLogo());
        for (SequenceI seq : sg.getSequences())
        {
          jGroup.addSeq(seqHash(seq));
        }
      }

      jms.setJGroup(groups);
    }
    if (!storeDS)
    {
      // /////////SAVE VIEWPORT
      Viewport view = new Viewport();
      view.setTitle(ap.alignFrame.getTitle());
      view.setSequenceSetId(makeHashCode(av.getSequenceSetId(),
              av.getSequenceSetId()));
      view.setId(av.getViewId());
      if (av.getCodingComplement() != null)
      {
        view.setComplementId(av.getCodingComplement().getViewId());
      }
      view.setViewName(av.viewName);
      view.setGatheredViews(av.isGatherViewsHere());

      Rectangle size = ap.av.getExplodedGeometry();
      Rectangle position = size;
      if (size == null)
      {
        size = ap.alignFrame.getBounds();
        if (av.getCodingComplement() != null)
        {
          position = ((SplitFrame) ap.alignFrame.getSplitViewContainer())
                  .getBounds();
        }
        else
        {
          position = size;
        }
      }
      view.setXpos(position.x);
      view.setYpos(position.y);

      view.setWidth(size.width);
      view.setHeight(size.height);

      view.setStartRes(av.startRes);
      view.setStartSeq(av.startSeq);

      if (av.getGlobalColourScheme() instanceof jalview.schemes.UserColourScheme)
      {
        view.setBgColour(setUserColourScheme(av.getGlobalColourScheme(),
                userColours, jms));
      }
      else if (av.getGlobalColourScheme() instanceof jalview.schemes.AnnotationColourGradient)
      {
        AnnotationColours ac = constructAnnotationColours(
                (jalview.schemes.AnnotationColourGradient) av
                        .getGlobalColourScheme(),
                userColours, jms);

        view.setAnnotationColours(ac);
        view.setBgColour("AnnotationColourGradient");
      }
      else
      {
        view.setBgColour(ColourSchemeProperty.getColourName(av
                .getGlobalColourScheme()));
      }

      ColourSchemeI cs = av.getGlobalColourScheme();

      if (cs != null)
      {
        if (cs.conservationApplied())
        {
          view.setConsThreshold(cs.getConservationInc());
          if (cs instanceof jalview.schemes.UserColourScheme)
          {
            view.setBgColour(setUserColourScheme(cs, userColours, jms));
          }
        }

        if (cs instanceof ResidueColourScheme)
        {
          view.setPidThreshold(cs.getThreshold());
        }
      }

      view.setConservationSelected(av.getConservationSelected());
      view.setPidSelected(av.getAbovePIDThreshold());
      view.setFontName(av.font.getName());
      view.setFontSize(av.font.getSize());
      view.setFontStyle(av.font.getStyle());
      view.setScaleProteinAsCdna(av.getViewStyle().isScaleProteinAsCdna());
      view.setRenderGaps(av.isRenderGaps());
      view.setShowAnnotation(av.isShowAnnotation());
      view.setShowBoxes(av.getShowBoxes());
      view.setShowColourText(av.getColourText());
      view.setShowFullId(av.getShowJVSuffix());
      view.setRightAlignIds(av.isRightAlignIds());
      view.setShowSequenceFeatures(av.isShowSequenceFeatures());
      view.setShowText(av.getShowText());
      view.setShowUnconserved(av.getShowUnconserved());
      view.setWrapAlignment(av.getWrapAlignment());
      view.setTextCol1(av.getTextColour().getRGB());
      view.setTextCol2(av.getTextColour2().getRGB());
      view.setTextColThreshold(av.getThresholdTextColour());
      view.setShowConsensusHistogram(av.isShowConsensusHistogram());
      view.setShowSequenceLogo(av.isShowSequenceLogo());
      view.setNormaliseSequenceLogo(av.isNormaliseSequenceLogo());
      view.setShowGroupConsensus(av.isShowGroupConsensus());
      view.setShowGroupConservation(av.isShowGroupConservation());
      view.setShowNPfeatureTooltip(av.isShowNPFeats());
      view.setShowDbRefTooltip(av.isShowDBRefs());
      view.setFollowHighlight(av.isFollowHighlight());
      view.setFollowSelection(av.followSelection);
      view.setIgnoreGapsinConsensus(av.isIgnoreGapsConsensus());
      if (av.getFeaturesDisplayed() != null)
      {
        jalview.schemabinding.version2.FeatureSettings fs = new jalview.schemabinding.version2.FeatureSettings();

        String[] renderOrder = ap.getSeqPanel().seqCanvas
                .getFeatureRenderer().getRenderOrder()
                .toArray(new String[0]);

        Vector<String> settingsAdded = new Vector<String>();
        if (renderOrder != null)
        {
          for (String featureType : renderOrder)
          {
            FeatureColourI fcol = ap.getSeqPanel().seqCanvas
                    .getFeatureRenderer().getFeatureStyle(featureType);
            Setting setting = new Setting();
            setting.setType(featureType);
            if (!fcol.isSimpleColour())
            {
              setting.setColour(fcol.getMaxColour().getRGB());
              setting.setMincolour(fcol.getMinColour().getRGB());
              setting.setMin(fcol.getMin());
              setting.setMax(fcol.getMax());
              setting.setColourByLabel(fcol.isColourByLabel());
              setting.setAutoScale(fcol.isAutoScaled());
              setting.setThreshold(fcol.getThreshold());
              // -1 = No threshold, 0 = Below, 1 = Above
              setting.setThreshstate(fcol.isAboveThreshold() ? 1 : (fcol
                      .isBelowThreshold() ? 0 : -1));
            }
            else
            {
              setting.setColour(fcol.getColour().getRGB());
            }

            setting.setDisplay(av.getFeaturesDisplayed().isVisible(
                    featureType));
            float rorder = ap.getSeqPanel().seqCanvas.getFeatureRenderer()
                    .getOrder(featureType);
            if (rorder > -1)
            {
              setting.setOrder(rorder);
            }
            fs.addSetting(setting);
            settingsAdded.addElement(featureType);
          }
        }

        // is groups actually supposed to be a map here ?
        Iterator<String> en = ap.getSeqPanel().seqCanvas
                .getFeatureRenderer().getFeatureGroups().iterator();
        Vector<String> groupsAdded = new Vector<String>();
        while (en.hasNext())
        {
          String grp = en.next();
          if (groupsAdded.contains(grp))
          {
            continue;
          }
          Group g = new Group();
          g.setName(grp);
          g.setDisplay(((Boolean) ap.getSeqPanel().seqCanvas
                  .getFeatureRenderer().checkGroupVisibility(grp, false))
                  .booleanValue());
          fs.addGroup(g);
          groupsAdded.addElement(grp);
        }
        jms.setFeatureSettings(fs);
      }

      if (av.hasHiddenColumns())
      {
        if (av.getColumnSelection() == null
                || av.getColumnSelection().getHiddenColumns() == null)
        {
          warn("REPORT BUG: avoided null columnselection bug (DMAM reported). Please contact Jim about this.");
        }
        else
        {
          for (int c = 0; c < av.getColumnSelection().getHiddenColumns()
                  .size(); c++)
          {
            int[] region = av.getColumnSelection().getHiddenColumns()
                    .get(c);
            HiddenColumns hc = new HiddenColumns();
            hc.setStart(region[0]);
            hc.setEnd(region[1]);
            view.addHiddenColumns(hc);
          }
        }
      }
      if (calcIdSet.size() > 0)
      {
        for (String calcId : calcIdSet)
        {
          if (calcId.trim().length() > 0)
          {
            CalcIdParam cidp = createCalcIdParam(calcId, av);
            // Some calcIds have no parameters.
            if (cidp != null)
            {
              view.addCalcIdParam(cidp);
            }
          }
        }
      }

      jms.addViewport(view);
    }
    object.setJalviewModelSequence(jms);
    object.getVamsasModel().addSequenceSet(vamsasSet);

    if (jout != null && fileName != null)
    {
      // We may not want to write the object to disk,
      // eg we can copy the alignViewport to a new view object
      // using save and then load
      try
      {
        System.out.println("Writing jar entry " + fileName);
        JarEntry entry = new JarEntry(fileName);
        jout.putNextEntry(entry);
        PrintWriter pout = new PrintWriter(new OutputStreamWriter(jout,
                UTF_8));
        Marshaller marshaller = new Marshaller(pout);
        marshaller.marshal(object);
        pout.flush();
        jout.closeEntry();
      } catch (Exception ex)
      {
        // TODO: raise error in GUI if marshalling failed.
        ex.printStackTrace();
      }
    }
    return object;
  }

  /**
   * Save any Varna viewers linked to this sequence. Writes an rnaViewer element
   * for each viewer, with
   * <ul>
   * <li>viewer geometry (position, size, split pane divider location)</li>
   * <li>index of the selected structure in the viewer (currently shows gapped
   * or ungapped)</li>
   * <li>the id of the annotation holding RNA secondary structure</li>
   * <li>(currently only one SS is shown per viewer, may be more in future)</li>
   * </ul>
   * Varna viewer state is also written out (in native Varna XML) to separate
   * project jar entries. A separate entry is written for each RNA structure
   * displayed, with the naming convention
   * <ul>
   * <li>rna_viewId_sequenceId_annotationId_[gapped|trimmed]</li>
   * </ul>
   * 
   * @param jout
   * @param jseq
   * @param jds
   * @param viewIds
   * @param ap
   * @param storeDataset
   */
  protected void saveRnaViewers(JarOutputStream jout, JSeq jseq,
          final SequenceI jds, List<String> viewIds, AlignmentPanel ap,
          boolean storeDataset)
  {
    if (Desktop.desktop == null)
    {
      return;
    }
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();
    for (int f = frames.length - 1; f > -1; f--)
    {
      if (frames[f] instanceof AppVarna)
      {
        AppVarna varna = (AppVarna) frames[f];
        /*
         * link the sequence to every viewer that is showing it and is linked to
         * its alignment panel
         */
        if (varna.isListeningFor(jds) && ap == varna.getAlignmentPanel())
        {
          String viewId = varna.getViewId();
          RnaViewer rna = new RnaViewer();
          rna.setViewId(viewId);
          rna.setTitle(varna.getTitle());
          rna.setXpos(varna.getX());
          rna.setYpos(varna.getY());
          rna.setWidth(varna.getWidth());
          rna.setHeight(varna.getHeight());
          rna.setDividerLocation(varna.getDividerLocation());
          rna.setSelectedRna(varna.getSelectedIndex());
          jseq.addRnaViewer(rna);

          /*
           * Store each Varna panel's state once in the project per sequence.
           * First time through only (storeDataset==false)
           */
          // boolean storeSessions = false;
          // String sequenceViewId = viewId + seqsToIds.get(jds);
          // if (!storeDataset && !viewIds.contains(sequenceViewId))
          // {
          // viewIds.add(sequenceViewId);
          // storeSessions = true;
          // }
          for (RnaModel model : varna.getModels())
          {
            if (model.seq == jds)
            {
              /*
               * VARNA saves each view (sequence or alignment secondary
               * structure, gapped or trimmed) as a separate XML file
               */
              String jarEntryName = rnaSessions.get(model);
              if (jarEntryName == null)
              {

                String varnaStateFile = varna.getStateInfo(model.rna);
                jarEntryName = RNA_PREFIX + viewId + "_" + nextCounter();
                copyFileToJar(jout, varnaStateFile, jarEntryName);
                rnaSessions.put(model, jarEntryName);
              }
              SecondaryStructure ss = new SecondaryStructure();
              String annotationId = varna.getAnnotation(jds).annotationId;
              ss.setAnnotationId(annotationId);
              ss.setViewerState(jarEntryName);
              ss.setGapped(model.gapped);
              ss.setTitle(model.title);
              rna.addSecondaryStructure(ss);
            }
          }
        }
      }
    }
  }

  /**
   * Copy the contents of a file to a new entry added to the output jar
   * 
   * @param jout
   * @param infilePath
   * @param jarEntryName
   */
  protected void copyFileToJar(JarOutputStream jout, String infilePath,
          String jarEntryName)
  {
    DataInputStream dis = null;
    try
    {
      File file = new File(infilePath);
      if (file.exists() && jout != null)
      {
        dis = new DataInputStream(new FileInputStream(file));
        byte[] data = new byte[(int) file.length()];
        dis.readFully(data);
        writeJarEntry(jout, jarEntryName, data);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (dis != null)
      {
        try
        {
          dis.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
  }

  /**
   * Write the data to a new entry of given name in the output jar file
   * 
   * @param jout
   * @param jarEntryName
   * @param data
   * @throws IOException
   */
  protected void writeJarEntry(JarOutputStream jout, String jarEntryName,
          byte[] data) throws IOException
  {
    if (jout != null)
    {
      System.out.println("Writing jar entry " + jarEntryName);
      jout.putNextEntry(new JarEntry(jarEntryName));
      DataOutputStream dout = new DataOutputStream(jout);
      dout.write(data, 0, data.length);
      dout.flush();
      jout.closeEntry();
    }
  }

  /**
   * Save the state of a structure viewer
   * 
   * @param ap
   * @param jds
   * @param pdb
   *          the archive XML element under which to save the state
   * @param entry
   * @param viewIds
   * @param matchedFile
   * @param viewFrame
   * @return
   */
  protected String saveStructureState(AlignmentPanel ap, SequenceI jds,
          Pdbids pdb, PDBEntry entry, List<String> viewIds,
          String matchedFile, StructureViewerBase viewFrame)
  {
    final AAStructureBindingModel bindingModel = viewFrame.getBinding();

    /*
     * Look for any bindings for this viewer to the PDB file of interest
     * (including part matches excluding chain id)
     */
    for (int peid = 0; peid < bindingModel.getPdbCount(); peid++)
    {
      final PDBEntry pdbentry = bindingModel.getPdbEntry(peid);
      final String pdbId = pdbentry.getId();
      if (!pdbId.equals(entry.getId())
              && !(entry.getId().length() > 4 && entry.getId()
                      .toLowerCase().startsWith(pdbId.toLowerCase())))
      {
        /*
         * not interested in a binding to a different PDB entry here
         */
        continue;
      }
      if (matchedFile == null)
      {
        matchedFile = pdbentry.getFile();
      }
      else if (!matchedFile.equals(pdbentry.getFile()))
      {
        Cache.log
                .warn("Probably lost some PDB-Sequence mappings for this structure file (which apparently has same PDB Entry code): "
                        + pdbentry.getFile());
      }
      // record the
      // file so we
      // can get at it if the ID
      // match is ambiguous (e.g.
      // 1QIP==1qipA)

      for (int smap = 0; smap < viewFrame.getBinding().getSequence()[peid].length; smap++)
      {
        // if (jal.findIndex(jmol.jmb.sequence[peid][smap]) > -1)
        if (jds == viewFrame.getBinding().getSequence()[peid][smap])
        {
          StructureState state = new StructureState();
          state.setVisible(true);
          state.setXpos(viewFrame.getX());
          state.setYpos(viewFrame.getY());
          state.setWidth(viewFrame.getWidth());
          state.setHeight(viewFrame.getHeight());
          final String viewId = viewFrame.getViewId();
          state.setViewId(viewId);
          state.setAlignwithAlignPanel(viewFrame.isUsedforaligment(ap));
          state.setColourwithAlignPanel(viewFrame.isUsedforcolourby(ap));
          state.setColourByJmol(viewFrame.isColouredByViewer());
          state.setType(viewFrame.getViewerType().toString());
          pdb.addStructureState(state);
        }
      }
    }
    return matchedFile;
  }

  private AnnotationColours constructAnnotationColours(
          AnnotationColourGradient acg, List<UserColourScheme> userColours,
          JalviewModelSequence jms)
  {
    AnnotationColours ac = new AnnotationColours();
    ac.setAboveThreshold(acg.getAboveThreshold());
    ac.setThreshold(acg.getAnnotationThreshold());
    ac.setAnnotation(acg.getAnnotation());
    if (acg.getBaseColour() instanceof jalview.schemes.UserColourScheme)
    {
      ac.setColourScheme(setUserColourScheme(acg.getBaseColour(),
              userColours, jms));
    }
    else
    {
      ac.setColourScheme(ColourSchemeProperty.getColourName(acg
              .getBaseColour()));
    }

    ac.setMaxColour(acg.getMaxColour().getRGB());
    ac.setMinColour(acg.getMinColour().getRGB());
    ac.setPerSequence(acg.isSeqAssociated());
    ac.setPredefinedColours(acg.isPredefinedColours());
    return ac;
  }

  private void storeAlignmentAnnotation(AlignmentAnnotation[] aa,
          IdentityHashMap<SequenceGroup, String> groupRefs,
          AlignmentViewport av, Set<String> calcIdSet, boolean storeDS,
          SequenceSet vamsasSet)
  {

    for (int i = 0; i < aa.length; i++)
    {
      Annotation an = new Annotation();

      AlignmentAnnotation annotation = aa[i];
      if (annotation.annotationId != null)
      {
        annotationIds.put(annotation.annotationId, annotation);
      }

      an.setId(annotation.annotationId);

      an.setVisible(annotation.visible);

      an.setDescription(annotation.description);

      if (annotation.sequenceRef != null)
      {
        // 2.9 JAL-1781 xref on sequence id rather than name
        an.setSequenceRef(seqsToIds.get(annotation.sequenceRef));
      }
      if (annotation.groupRef != null)
      {
        String groupIdr = groupRefs.get(annotation.groupRef);
        if (groupIdr == null)
        {
          // make a locally unique String
          groupRefs.put(
                  annotation.groupRef,
                  groupIdr = ("" + System.currentTimeMillis()
                          + annotation.groupRef.getName() + groupRefs
                          .size()));
        }
        an.setGroupRef(groupIdr.toString());
      }

      // store all visualization attributes for annotation
      an.setGraphHeight(annotation.graphHeight);
      an.setCentreColLabels(annotation.centreColLabels);
      an.setScaleColLabels(annotation.scaleColLabel);
      an.setShowAllColLabels(annotation.showAllColLabels);
      an.setBelowAlignment(annotation.belowAlignment);

      if (annotation.graph > 0)
      {
        an.setGraph(true);
        an.setGraphType(annotation.graph);
        an.setGraphGroup(annotation.graphGroup);
        if (annotation.getThreshold() != null)
        {
          ThresholdLine line = new ThresholdLine();
          line.setLabel(annotation.getThreshold().label);
          line.setValue(annotation.getThreshold().value);
          line.setColour(annotation.getThreshold().colour.getRGB());
          an.setThresholdLine(line);
        }
      }
      else
      {
        an.setGraph(false);
      }

      an.setLabel(annotation.label);

      if (annotation == av.getAlignmentQualityAnnot()
              || annotation == av.getAlignmentConservationAnnotation()
              || annotation == av.getAlignmentConsensusAnnotation()
              || annotation.autoCalculated)
      {
        // new way of indicating autocalculated annotation -
        an.setAutoCalculated(annotation.autoCalculated);
      }
      if (annotation.hasScore())
      {
        an.setScore(annotation.getScore());
      }

      if (annotation.getCalcId() != null)
      {
        calcIdSet.add(annotation.getCalcId());
        an.setCalcId(annotation.getCalcId());
      }
      if (annotation.hasProperties())
      {
        for (String pr : annotation.getProperties())
        {
          Property prop = new Property();
          prop.setName(pr);
          prop.setValue(annotation.getProperty(pr));
          an.addProperty(prop);
        }
      }

      AnnotationElement ae;
      if (annotation.annotations != null)
      {
        an.setScoreOnly(false);
        for (int a = 0; a < annotation.annotations.length; a++)
        {
          if ((annotation == null) || (annotation.annotations[a] == null))
          {
            continue;
          }

          ae = new AnnotationElement();
          if (annotation.annotations[a].description != null)
          {
            ae.setDescription(annotation.annotations[a].description);
          }
          if (annotation.annotations[a].displayCharacter != null)
          {
            ae.setDisplayCharacter(annotation.annotations[a].displayCharacter);
          }

          if (!Float.isNaN(annotation.annotations[a].value))
          {
            ae.setValue(annotation.annotations[a].value);
          }

          ae.setPosition(a);
          if (annotation.annotations[a].secondaryStructure > ' ')
          {
            ae.setSecondaryStructure(annotation.annotations[a].secondaryStructure
                    + "");
          }

          if (annotation.annotations[a].colour != null
                  && annotation.annotations[a].colour != java.awt.Color.black)
          {
            ae.setColour(annotation.annotations[a].colour.getRGB());
          }

          an.addAnnotationElement(ae);
          if (annotation.autoCalculated)
          {
            // only write one non-null entry into the annotation row -
            // sufficient to get the visualization attributes necessary to
            // display data
            continue;
          }
        }
      }
      else
      {
        an.setScoreOnly(true);
      }
      if (!storeDS || (storeDS && !annotation.autoCalculated))
      {
        // skip autocalculated annotation - these are only provided for
        // alignments
        vamsasSet.addAnnotation(an);
      }
    }

  }

  private CalcIdParam createCalcIdParam(String calcId, AlignViewport av)
  {
    AutoCalcSetting settings = av.getCalcIdSettingsFor(calcId);
    if (settings != null)
    {
      CalcIdParam vCalcIdParam = new CalcIdParam();
      vCalcIdParam.setCalcId(calcId);
      vCalcIdParam.addServiceURL(settings.getServiceURI());
      // generic URI allowing a third party to resolve another instance of the
      // service used for this calculation
      for (String urls : settings.getServiceURLs())
      {
        vCalcIdParam.addServiceURL(urls);
      }
      vCalcIdParam.setVersion("1.0");
      if (settings.getPreset() != null)
      {
        WsParamSetI setting = settings.getPreset();
        vCalcIdParam.setName(setting.getName());
        vCalcIdParam.setDescription(setting.getDescription());
      }
      else
      {
        vCalcIdParam.setName("");
        vCalcIdParam.setDescription("Last used parameters");
      }
      // need to be able to recover 1) settings 2) user-defined presets or
      // recreate settings from preset 3) predefined settings provided by
      // service - or settings that can be transferred (or discarded)
      vCalcIdParam.setParameters(settings.getWsParamFile().replace("\n",
              "|\\n|"));
      vCalcIdParam.setAutoUpdate(settings.isAutoUpdate());
      // todo - decide if updateImmediately is needed for any projects.

      return vCalcIdParam;
    }
    return null;
  }

  private boolean recoverCalcIdParam(CalcIdParam calcIdParam,
          AlignViewport av)
  {
    if (calcIdParam.getVersion().equals("1.0"))
    {
      Jws2Instance service = Jws2Discoverer.getDiscoverer()
              .getPreferredServiceFor(calcIdParam.getServiceURL());
      if (service != null)
      {
        WsParamSetI parmSet = null;
        try
        {
          parmSet = service.getParamStore().parseServiceParameterFile(
                  calcIdParam.getName(), calcIdParam.getDescription(),
                  calcIdParam.getServiceURL(),
                  calcIdParam.getParameters().replace("|\\n|", "\n"));
        } catch (IOException x)
        {
          warn("Couldn't parse parameter data for "
                  + calcIdParam.getCalcId(), x);
          return false;
        }
        List<ArgumentI> argList = null;
        if (calcIdParam.getName().length() > 0)
        {
          parmSet = service.getParamStore()
                  .getPreset(calcIdParam.getName());
          if (parmSet != null)
          {
            // TODO : check we have a good match with settings in AACon -
            // otherwise we'll need to create a new preset
          }
        }
        else
        {
          argList = parmSet.getArguments();
          parmSet = null;
        }
        AAConSettings settings = new AAConSettings(
                calcIdParam.isAutoUpdate(), service, parmSet, argList);
        av.setCalcIdSettingsFor(calcIdParam.getCalcId(), settings,
                calcIdParam.isNeedsUpdate());
        return true;
      }
      else
      {
        warn("Cannot resolve a service for the parameters used in this project. Try configuring a JABAWS server.");
        return false;
      }
    }
    throw new Error(MessageManager.formatMessage(
            "error.unsupported_version_calcIdparam",
            new Object[] { calcIdParam.toString() }));
  }

  /**
   * External mapping between jalview objects and objects yielding a valid and
   * unique object ID string. This is null for normal Jalview project IO, but
   * non-null when a jalview project is being read or written as part of a
   * vamsas session.
   */
  IdentityHashMap jv2vobj = null;

  /**
   * Construct a unique ID for jvobj using either existing bindings or if none
   * exist, the result of the hashcode call for the object.
   * 
   * @param jvobj
   *          jalview data object
   * @return unique ID for referring to jvobj
   */
  private String makeHashCode(Object jvobj, String altCode)
  {
    if (jv2vobj != null)
    {
      Object id = jv2vobj.get(jvobj);
      if (id != null)
      {
        return id.toString();
      }
      // check string ID mappings
      if (jvids2vobj != null && jvobj instanceof String)
      {
        id = jvids2vobj.get(jvobj);
      }
      if (id != null)
      {
        return id.toString();
      }
      // give up and warn that something has gone wrong
      warn("Cannot find ID for object in external mapping : " + jvobj);
    }
    return altCode;
  }

  /**
   * return local jalview object mapped to ID, if it exists
   * 
   * @param idcode
   *          (may be null)
   * @return null or object bound to idcode
   */
  private Object retrieveExistingObj(String idcode)
  {
    if (idcode != null && vobj2jv != null)
    {
      return vobj2jv.get(idcode);
    }
    return null;
  }

  /**
   * binding from ID strings from external mapping table to jalview data model
   * objects.
   */
  private Hashtable vobj2jv;

  private Sequence createVamsasSequence(String id, SequenceI jds)
  {
    return createVamsasSequence(true, id, jds, null);
  }

  private Sequence createVamsasSequence(boolean recurse, String id,
          SequenceI jds, SequenceI parentseq)
  {
    Sequence vamsasSeq = new Sequence();
    vamsasSeq.setId(id);
    vamsasSeq.setName(jds.getName());
    vamsasSeq.setSequence(jds.getSequenceAsString());
    vamsasSeq.setDescription(jds.getDescription());
    jalview.datamodel.DBRefEntry[] dbrefs = null;
    if (jds.getDatasetSequence() != null)
    {
      vamsasSeq.setDsseqid(seqHash(jds.getDatasetSequence()));
    }
    else
    {
      // seqId==dsseqid so we can tell which sequences really are
      // dataset sequences only
      vamsasSeq.setDsseqid(id);
      dbrefs = jds.getDBRefs();
      if (parentseq == null)
      {
        parentseq = jds;
      }
    }
    if (dbrefs != null)
    {
      for (int d = 0; d < dbrefs.length; d++)
      {
        DBRef dbref = new DBRef();
        dbref.setSource(dbrefs[d].getSource());
        dbref.setVersion(dbrefs[d].getVersion());
        dbref.setAccessionId(dbrefs[d].getAccessionId());
        if (dbrefs[d].hasMap())
        {
          Mapping mp = createVamsasMapping(dbrefs[d].getMap(), parentseq,
                  jds, recurse);
          dbref.setMapping(mp);
        }
        vamsasSeq.addDBRef(dbref);
      }
    }
    return vamsasSeq;
  }

  private Mapping createVamsasMapping(jalview.datamodel.Mapping jmp,
          SequenceI parentseq, SequenceI jds, boolean recurse)
  {
    Mapping mp = null;
    if (jmp.getMap() != null)
    {
      mp = new Mapping();

      jalview.util.MapList mlst = jmp.getMap();
      List<int[]> r = mlst.getFromRanges();
      for (int[] range : r)
      {
        MapListFrom mfrom = new MapListFrom();
        mfrom.setStart(range[0]);
        mfrom.setEnd(range[1]);
        mp.addMapListFrom(mfrom);
      }
      r = mlst.getToRanges();
      for (int[] range : r)
      {
        MapListTo mto = new MapListTo();
        mto.setStart(range[0]);
        mto.setEnd(range[1]);
        mp.addMapListTo(mto);
      }
      mp.setMapFromUnit(mlst.getFromRatio());
      mp.setMapToUnit(mlst.getToRatio());
      if (jmp.getTo() != null)
      {
        MappingChoice mpc = new MappingChoice();

        // check/create ID for the sequence referenced by getTo()

        String jmpid = "";
        SequenceI ps = null;
        if (parentseq != jmp.getTo()
                && parentseq.getDatasetSequence() != jmp.getTo())
        {
          // chaining dbref rather than a handshaking one
          jmpid = seqHash(ps = jmp.getTo());
        }
        else
        {
          jmpid = seqHash(ps = parentseq);
        }
        mpc.setDseqFor(jmpid);
        if (!seqRefIds.containsKey(mpc.getDseqFor()))
        {
          jalview.bin.Cache.log.debug("creatign new DseqFor ID");
          seqRefIds.put(mpc.getDseqFor(), ps);
        }
        else
        {
          jalview.bin.Cache.log.debug("reusing DseqFor ID");
        }

        mp.setMappingChoice(mpc);
      }
    }
    return mp;
  }

  String setUserColourScheme(jalview.schemes.ColourSchemeI cs,
          List<UserColourScheme> userColours, JalviewModelSequence jms)
  {
    String id = null;
    jalview.schemes.UserColourScheme ucs = (jalview.schemes.UserColourScheme) cs;
    boolean newucs = false;
    if (!userColours.contains(ucs))
    {
      userColours.add(ucs);
      newucs = true;
    }
    id = "ucs" + userColours.indexOf(ucs);
    if (newucs)
    {
      // actually create the scheme's entry in the XML model
      java.awt.Color[] colours = ucs.getColours();
      jalview.schemabinding.version2.UserColours uc = new jalview.schemabinding.version2.UserColours();
      jalview.schemabinding.version2.UserColourScheme jbucs = new jalview.schemabinding.version2.UserColourScheme();

      for (int i = 0; i < colours.length; i++)
      {
        jalview.schemabinding.version2.Colour col = new jalview.schemabinding.version2.Colour();
        col.setName(ResidueProperties.aa[i]);
        col.setRGB(jalview.util.Format.getHexString(colours[i]));
        jbucs.addColour(col);
      }
      if (ucs.getLowerCaseColours() != null)
      {
        colours = ucs.getLowerCaseColours();
        for (int i = 0; i < colours.length; i++)
        {
          jalview.schemabinding.version2.Colour col = new jalview.schemabinding.version2.Colour();
          col.setName(ResidueProperties.aa[i].toLowerCase());
          col.setRGB(jalview.util.Format.getHexString(colours[i]));
          jbucs.addColour(col);
        }
      }

      uc.setId(id);
      uc.setUserColourScheme(jbucs);
      jms.addUserColours(uc);
    }

    return id;
  }

  jalview.schemes.UserColourScheme getUserColourScheme(
          JalviewModelSequence jms, String id)
  {
    UserColours[] uc = jms.getUserColours();
    UserColours colours = null;

    for (int i = 0; i < uc.length; i++)
    {
      if (uc[i].getId().equals(id))
      {
        colours = uc[i];

        break;
      }
    }

    java.awt.Color[] newColours = new java.awt.Color[24];

    for (int i = 0; i < 24; i++)
    {
      newColours[i] = new java.awt.Color(Integer.parseInt(colours
              .getUserColourScheme().getColour(i).getRGB(), 16));
    }

    jalview.schemes.UserColourScheme ucs = new jalview.schemes.UserColourScheme(
            newColours);

    if (colours.getUserColourScheme().getColourCount() > 24)
    {
      newColours = new java.awt.Color[23];
      for (int i = 0; i < 23; i++)
      {
        newColours[i] = new java.awt.Color(Integer.parseInt(colours
                .getUserColourScheme().getColour(i + 24).getRGB(), 16));
      }
      ucs.setLowerCaseColours(newColours);
    }

    return ucs;
  }

  /**
   * contains last error message (if any) encountered by XML loader.
   */
  String errorMessage = null;

  /**
   * flag to control whether the Jalview2XML_V1 parser should be deferred to if
   * exceptions are raised during project XML parsing
   */
  public boolean attemptversion1parse = true;

  /**
   * Load a jalview project archive from a jar file
   * 
   * @param file
   *          - HTTP URL or filename
   */
  public AlignFrame loadJalviewAlign(final String file)
  {

    jalview.gui.AlignFrame af = null;

    try
    {
      // create list to store references for any new Jmol viewers created
      newStructureViewers = new Vector<JalviewStructureDisplayI>();
      // UNMARSHALLER SEEMS TO CLOSE JARINPUTSTREAM, MOST ANNOYING
      // Workaround is to make sure caller implements the JarInputStreamProvider
      // interface
      // so we can re-open the jar input stream for each entry.

      jarInputStreamProvider jprovider = createjarInputStreamProvider(file);
      af = loadJalviewAlign(jprovider);

    } catch (MalformedURLException e)
    {
      errorMessage = "Invalid URL format for '" + file + "'";
      reportErrors();
    } finally
    {
      try
      {
        SwingUtilities.invokeAndWait(new Runnable()
        {
          @Override
          public void run()
          {
            setLoadingFinishedForNewStructureViewers();
          };
        });
      } catch (Exception x)
      {
        System.err.println("Error loading alignment: " + x.getMessage());
      }
    }
    return af;
  }

  private jarInputStreamProvider createjarInputStreamProvider(
          final String file) throws MalformedURLException
  {
    URL url = null;
    errorMessage = null;
    uniqueSetSuffix = null;
    seqRefIds = null;
    viewportsAdded.clear();
    frefedSequence = null;

    if (file.startsWith("http://"))
    {
      url = new URL(file);
    }
    final URL _url = url;
    return new jarInputStreamProvider()
    {

      @Override
      public JarInputStream getJarInputStream() throws IOException
      {
        if (_url != null)
        {
          return new JarInputStream(_url.openStream());
        }
        else
        {
          return new JarInputStream(new FileInputStream(file));
        }
      }

      @Override
      public String getFilename()
      {
        return file;
      }
    };
  }

  /**
   * Recover jalview session from a jalview project archive. Caller may
   * initialise uniqueSetSuffix, seqRefIds, viewportsAdded and frefedSequence
   * themselves. Any null fields will be initialised with default values,
   * non-null fields are left alone.
   * 
   * @param jprovider
   * @return
   */
  public AlignFrame loadJalviewAlign(final jarInputStreamProvider jprovider)
  {
    errorMessage = null;
    if (uniqueSetSuffix == null)
    {
      uniqueSetSuffix = System.currentTimeMillis() % 100000 + "";
    }
    if (seqRefIds == null)
    {
      initSeqRefs();
    }
    AlignFrame af = null, _af = null;
    IdentityHashMap<AlignmentI, AlignmentI> importedDatasets = new IdentityHashMap<AlignmentI, AlignmentI>();
    Map<String, AlignFrame> gatherToThisFrame = new HashMap<String, AlignFrame>();
    final String file = jprovider.getFilename();
    try
    {
      JarInputStream jin = null;
      JarEntry jarentry = null;
      int entryCount = 1;

      do
      {
        jin = jprovider.getJarInputStream();
        for (int i = 0; i < entryCount; i++)
        {
          jarentry = jin.getNextJarEntry();
        }

        if (jarentry != null && jarentry.getName().endsWith(".xml"))
        {
          InputStreamReader in = new InputStreamReader(jin, UTF_8);
          JalviewModel object = new JalviewModel();

          Unmarshaller unmar = new Unmarshaller(object);
          unmar.setValidation(false);
          object = (JalviewModel) unmar.unmarshal(in);
          if (true) // !skipViewport(object))
          {
            _af = loadFromObject(object, file, true, jprovider);
            if (_af != null
                    && object.getJalviewModelSequence().getViewportCount() > 0)
            {
              if (af == null)
              {
                // store a reference to the first view
                af = _af;
              }
              if (_af.viewport.isGatherViewsHere())
              {
                // if this is a gathered view, keep its reference since
                // after gathering views, only this frame will remain
                af = _af;
                gatherToThisFrame.put(_af.viewport.getSequenceSetId(), _af);
              }
              // Save dataset to register mappings once all resolved
              importedDatasets.put(af.viewport.getAlignment().getDataset(),
                      af.viewport.getAlignment().getDataset());
            }
          }
          entryCount++;
        }
        else if (jarentry != null)
        {
          // Some other file here.
          entryCount++;
        }
      } while (jarentry != null);
      resolveFrefedSequences();
    } catch (IOException ex)
    {
      ex.printStackTrace();
      errorMessage = "Couldn't locate Jalview XML file : " + file;
      System.err.println("Exception whilst loading jalview XML file : "
              + ex + "\n");
    } catch (Exception ex)
    {
      System.err.println("Parsing as Jalview Version 2 file failed.");
      ex.printStackTrace(System.err);
      if (attemptversion1parse)
      {
        // Is Version 1 Jar file?
        try
        {
          af = new Jalview2XML_V1(raiseGUI).LoadJalviewAlign(jprovider);
        } catch (Exception ex2)
        {
          System.err.println("Exception whilst loading as jalviewXMLV1:");
          ex2.printStackTrace();
          af = null;
        }
      }
      if (Desktop.instance != null)
      {
        Desktop.instance.stopLoading();
      }
      if (af != null)
      {
        System.out.println("Successfully loaded archive file");
        return af;
      }
      ex.printStackTrace();

      System.err.println("Exception whilst loading jalview XML file : "
              + ex + "\n");
    } catch (OutOfMemoryError e)
    {
      // Don't use the OOM Window here
      errorMessage = "Out of memory loading jalview XML file";
      System.err.println("Out of memory whilst loading jalview XML file");
      e.printStackTrace();
    }

    /*
     * Regather multiple views (with the same sequence set id) to the frame (if
     * any) that is flagged as the one to gather to, i.e. convert them to tabbed
     * views instead of separate frames. Note this doesn't restore a state where
     * some expanded views in turn have tabbed views - the last "first tab" read
     * in will play the role of gatherer for all.
     */
    for (AlignFrame fr : gatherToThisFrame.values())
    {
      Desktop.instance.gatherViews(fr);
    }

    restoreSplitFrames();
    for (AlignmentI ds : importedDatasets.keySet())
    {
      if (ds.getCodonFrames() != null)
      {
        StructureSelectionManager.getStructureSelectionManager(
                Desktop.instance).registerMappings(ds.getCodonFrames());
      }
    }
    if (errorMessage != null)
    {
      reportErrors();
    }

    if (Desktop.instance != null)
    {
      Desktop.instance.stopLoading();
    }

    return af;
  }

  /**
   * Try to reconstruct and display SplitFrame windows, where each contains
   * complementary dna and protein alignments. Done by pairing up AlignFrame
   * objects (created earlier) which have complementary viewport ids associated.
   */
  protected void restoreSplitFrames()
  {
    List<SplitFrame> gatherTo = new ArrayList<SplitFrame>();
    List<AlignFrame> addedToSplitFrames = new ArrayList<AlignFrame>();
    Map<String, AlignFrame> dna = new HashMap<String, AlignFrame>();

    /*
     * Identify the DNA alignments
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (af.getViewport().getAlignment().isNucleotide())
      {
        dna.put(candidate.getKey().getId(), af);
      }
    }

    /*
     * Try to match up the protein complements
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (!af.getViewport().getAlignment().isNucleotide())
      {
        String complementId = candidate.getKey().getComplementId();
        // only non-null complements should be in the Map
        if (complementId != null && dna.containsKey(complementId))
        {
          final AlignFrame dnaFrame = dna.get(complementId);
          SplitFrame sf = createSplitFrame(dnaFrame, af);
          addedToSplitFrames.add(dnaFrame);
          addedToSplitFrames.add(af);
          dnaFrame.setMenusForViewport();
          af.setMenusForViewport();
          if (af.viewport.isGatherViewsHere())
          {
            gatherTo.add(sf);
          }
        }
      }
    }

    /*
     * Open any that we failed to pair up (which shouldn't happen!) as
     * standalone AlignFrame's.
     */
    for (Entry<Viewport, AlignFrame> candidate : splitFrameCandidates
            .entrySet())
    {
      AlignFrame af = candidate.getValue();
      if (!addedToSplitFrames.contains(af))
      {
        Viewport view = candidate.getKey();
        Desktop.addInternalFrame(af, view.getTitle(), view.getWidth(),
                view.getHeight());
        af.setMenusForViewport();
        System.err.println("Failed to restore view " + view.getTitle()
                + " to split frame");
      }
    }

    /*
     * Gather back into tabbed views as flagged.
     */
    for (SplitFrame sf : gatherTo)
    {
      Desktop.instance.gatherViews(sf);
    }

    splitFrameCandidates.clear();
  }

  /**
   * Construct and display one SplitFrame holding DNA and protein alignments.
   * 
   * @param dnaFrame
   * @param proteinFrame
   * @return
   */
  protected SplitFrame createSplitFrame(AlignFrame dnaFrame,
          AlignFrame proteinFrame)
  {
    SplitFrame splitFrame = new SplitFrame(dnaFrame, proteinFrame);
    String title = MessageManager.getString("label.linked_view_title");
    int width = (int) dnaFrame.getBounds().getWidth();
    int height = (int) (dnaFrame.getBounds().getHeight()
            + proteinFrame.getBounds().getHeight() + 50);

    /*
     * SplitFrame location is saved to both enclosed frames
     */
    splitFrame.setLocation(dnaFrame.getX(), dnaFrame.getY());
    Desktop.addInternalFrame(splitFrame, title, width, height);

    /*
     * And compute cDNA consensus (couldn't do earlier with consensus as
     * mappings were not yet present)
     */
    proteinFrame.viewport.alignmentChanged(proteinFrame.alignPanel);

    return splitFrame;
  }

  /**
   * check errorMessage for a valid error message and raise an error box in the
   * GUI or write the current errorMessage to stderr and then clear the error
   * state.
   */
  protected void reportErrors()
  {
    reportErrors(false);
  }

  protected void reportErrors(final boolean saving)
  {
    if (errorMessage != null)
    {
      final String finalErrorMessage = errorMessage;
      if (raiseGUI)
      {
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            JOptionPane.showInternalMessageDialog(Desktop.desktop,
                    finalErrorMessage, "Error "
                            + (saving ? "saving" : "loading")
                            + " Jalview file", JOptionPane.WARNING_MESSAGE);
          }
        });
      }
      else
      {
        System.err.println("Problem loading Jalview file: " + errorMessage);
      }
    }
    errorMessage = null;
  }

  Map<String, String> alreadyLoadedPDB = new HashMap<String, String>();

  /**
   * when set, local views will be updated from view stored in JalviewXML
   * Currently (28th Sep 2008) things will go horribly wrong in vamsas document
   * sync if this is set to true.
   */
  private final boolean updateLocalViews = false;

  /**
   * Returns the path to a temporary file holding the PDB file for the given PDB
   * id. The first time of asking, searches for a file of that name in the
   * Jalview project jar, and copies it to a new temporary file. Any repeat
   * requests just return the path to the file previously created.
   * 
   * @param jprovider
   * @param pdbId
   * @return
   */
  String loadPDBFile(jarInputStreamProvider jprovider, String pdbId,
          String origFile)
  {
    if (alreadyLoadedPDB.containsKey(pdbId))
    {
      return alreadyLoadedPDB.get(pdbId).toString();
    }

    String tempFile = copyJarEntry(jprovider, pdbId, "jalview_pdb",
            origFile);
    if (tempFile != null)
    {
      alreadyLoadedPDB.put(pdbId, tempFile);
    }
    return tempFile;
  }

  /**
   * Copies the jar entry of given name to a new temporary file and returns the
   * path to the file, or null if the entry is not found.
   * 
   * @param jprovider
   * @param jarEntryName
   * @param prefix
   *          a prefix for the temporary file name, must be at least three
   *          characters long
   * @param origFile
   *          null or original file - so new file can be given the same suffix
   *          as the old one
   * @return
   */
  protected String copyJarEntry(jarInputStreamProvider jprovider,
          String jarEntryName, String prefix, String origFile)
  {
    BufferedReader in = null;
    PrintWriter out = null;
    String suffix = ".tmp";
    if (origFile == null)
    {
      origFile = jarEntryName;
    }
    int sfpos = origFile.lastIndexOf(".");
    if (sfpos > -1 && sfpos < (origFile.length() - 3))
    {
      suffix = "." + origFile.substring(sfpos + 1);
    }
    try
    {
      JarInputStream jin = jprovider.getJarInputStream();
      /*
       * if (jprovider.startsWith("http://")) { jin = new JarInputStream(new
       * URL(jprovider).openStream()); } else { jin = new JarInputStream(new
       * FileInputStream(jprovider)); }
       */

      JarEntry entry = null;
      do
      {
        entry = jin.getNextJarEntry();
      } while (entry != null && !entry.getName().equals(jarEntryName));
      if (entry != null)
      {
        in = new BufferedReader(new InputStreamReader(jin, UTF_8));
        File outFile = File.createTempFile(prefix, suffix);
        outFile.deleteOnExit();
        out = new PrintWriter(new FileOutputStream(outFile));
        String data;

        while ((data = in.readLine()) != null)
        {
          out.println(data);
        }
        out.flush();
        String t = outFile.getAbsolutePath();
        return t;
      }
      else
      {
        warn("Couldn't find entry in Jalview Jar for " + jarEntryName);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
      if (out != null)
      {
        out.close();
      }
    }

    return null;
  }

  private class JvAnnotRow
  {
    public JvAnnotRow(int i, AlignmentAnnotation jaa)
    {
      order = i;
      template = jaa;
    }

    /**
     * persisted version of annotation row from which to take vis properties
     */
    public jalview.datamodel.AlignmentAnnotation template;

    /**
     * original position of the annotation row in the alignment
     */
    public int order;
  }

  /**
   * Load alignment frame from jalview XML DOM object
   * 
   * @param object
   *          DOM
   * @param file
   *          filename source string
   * @param loadTreesAndStructures
   *          when false only create Viewport
   * @param jprovider
   *          data source provider
   * @return alignment frame created from view stored in DOM
   */
  AlignFrame loadFromObject(JalviewModel object, String file,
          boolean loadTreesAndStructures, jarInputStreamProvider jprovider)
  {
    SequenceSet vamsasSet = object.getVamsasModel().getSequenceSet(0);
    Sequence[] vamsasSeq = vamsasSet.getSequence();

    JalviewModelSequence jms = object.getJalviewModelSequence();

    Viewport view = (jms.getViewportCount() > 0) ? jms.getViewport(0)
            : null;

    // ////////////////////////////////
    // LOAD SEQUENCES

    List<SequenceI> hiddenSeqs = null;

    List<SequenceI> tmpseqs = new ArrayList<SequenceI>();

    boolean multipleView = false;
    SequenceI referenceseqForView = null;
    JSeq[] jseqs = object.getJalviewModelSequence().getJSeq();
    int vi = 0; // counter in vamsasSeq array
    for (int i = 0; i < jseqs.length; i++)
    {
      String seqId = jseqs[i].getId();

      SequenceI tmpSeq = seqRefIds.get(seqId);
      if (tmpSeq != null)
      {
        if (!incompleteSeqs.containsKey(seqId))
        {
          // may not need this check, but keep it for at least 2.9,1 release
          if (tmpSeq.getStart() != jseqs[i].getStart()
                  || tmpSeq.getEnd() != jseqs[i].getEnd())
          {
            System.err
                    .println("Warning JAL-2154 regression: updating start/end for sequence "
                            + tmpSeq.toString() + " to " + jseqs[i]);
          }
        }
        else
        {
          incompleteSeqs.remove(seqId);
        }
        if (vamsasSeq.length > vi && vamsasSeq[vi].getId().equals(seqId))
        {
          // most likely we are reading a dataset XML document so
          // update from vamsasSeq section of XML for this sequence
          tmpSeq.setName(vamsasSeq[vi].getName());
          tmpSeq.setDescription(vamsasSeq[vi].getDescription());
          tmpSeq.setSequence(vamsasSeq[vi].getSequence());
          vi++;
        }
        else
        {
          // reading multiple views, so vamsasSeq set is a subset of JSeq
          multipleView = true;
        }
        tmpSeq.setStart(jseqs[i].getStart());
        tmpSeq.setEnd(jseqs[i].getEnd());
        tmpseqs.add(tmpSeq);
      }
      else
      {
        tmpSeq = new jalview.datamodel.Sequence(vamsasSeq[vi].getName(),
                vamsasSeq[vi].getSequence());
        tmpSeq.setDescription(vamsasSeq[vi].getDescription());
        tmpSeq.setStart(jseqs[i].getStart());
        tmpSeq.setEnd(jseqs[i].getEnd());
        tmpSeq.setVamsasId(uniqueSetSuffix + seqId);
        seqRefIds.put(vamsasSeq[vi].getId(), tmpSeq);
        tmpseqs.add(tmpSeq);
        vi++;
      }

      if (jseqs[i].hasViewreference() && jseqs[i].getViewreference())
      {
        referenceseqForView = tmpseqs.get(tmpseqs.size() - 1);
      }

      if (jseqs[i].getHidden())
      {
        if (hiddenSeqs == null)
        {
          hiddenSeqs = new ArrayList<SequenceI>();
        }

        hiddenSeqs.add(tmpSeq);
      }
    }

    // /
    // Create the alignment object from the sequence set
    // ///////////////////////////////
    SequenceI[] orderedSeqs = tmpseqs
            .toArray(new SequenceI[tmpseqs.size()]);

    AlignmentI al = null;
    // so we must create or recover the dataset alignment before going further
    // ///////////////////////////////
    if (vamsasSet.getDatasetId() == null || vamsasSet.getDatasetId() == "")
    {
      // older jalview projects do not have a dataset - so creat alignment and
      // dataset
      al = new Alignment(orderedSeqs);
      al.setDataset(null);
    }
    else
    {
      boolean isdsal = object.getJalviewModelSequence().getViewportCount() == 0;
      if (isdsal)
      {
        // we are importing a dataset record, so
        // recover reference to an alignment already materialsed as dataset
        al = getDatasetFor(vamsasSet.getDatasetId());
      }
      if (al == null)
      {
        // materialse the alignment
        al = new Alignment(orderedSeqs);
      }
      if (isdsal)
      {
        addDatasetRef(vamsasSet.getDatasetId(), al);
      }

      // finally, verify all data in vamsasSet is actually present in al
      // passing on flag indicating if it is actually a stored dataset
      recoverDatasetFor(vamsasSet, al, isdsal);
    }

    if (referenceseqForView != null)
    {
      al.setSeqrep(referenceseqForView);
    }
    // / Add the alignment properties
    for (int i = 0; i < vamsasSet.getSequenceSetPropertiesCount(); i++)
    {
      SequenceSetProperties ssp = vamsasSet.getSequenceSetProperties(i);
      al.setProperty(ssp.getKey(), ssp.getValue());
    }

    // ///////////////////////////////

    Hashtable pdbloaded = new Hashtable(); // TODO nothing writes to this??
    if (!multipleView)
    {
      // load sequence features, database references and any associated PDB
      // structures for the alignment
      //
      // prior to 2.10, this part would only be executed the first time a
      // sequence was encountered, but not afterwards.
      // now, for 2.10 projects, this is also done if the xml doc includes
      // dataset sequences not actually present in any particular view.
      //
      for (int i = 0; i < vamsasSeq.length; i++)
      {
        if (jseqs[i].getFeaturesCount() > 0)
        {
          Features[] features = jseqs[i].getFeatures();
          for (int f = 0; f < features.length; f++)
          {
            jalview.datamodel.SequenceFeature sf = new jalview.datamodel.SequenceFeature(
                    features[f].getType(), features[f].getDescription(),
                    features[f].getStatus(), features[f].getBegin(),
                    features[f].getEnd(), features[f].getFeatureGroup());

            sf.setScore(features[f].getScore());
            for (int od = 0; od < features[f].getOtherDataCount(); od++)
            {
              OtherData keyValue = features[f].getOtherData(od);
              if (keyValue.getKey().startsWith("LINK"))
              {
                sf.addLink(keyValue.getValue());
              }
              else
              {
                sf.setValue(keyValue.getKey(), keyValue.getValue());
              }

            }
            // adds feature to datasequence's feature set (since Jalview 2.10)
            al.getSequenceAt(i).addSequenceFeature(sf);
          }
        }
        if (vamsasSeq[i].getDBRefCount() > 0)
        {
          // adds dbrefs to datasequence's set (since Jalview 2.10)
          addDBRefs(
                  al.getSequenceAt(i).getDatasetSequence() == null ? al.getSequenceAt(i)
                          : al.getSequenceAt(i).getDatasetSequence(),
                  vamsasSeq[i]);
        }
        if (jseqs[i].getPdbidsCount() > 0)
        {
          Pdbids[] ids = jseqs[i].getPdbids();
          for (int p = 0; p < ids.length; p++)
          {
            jalview.datamodel.PDBEntry entry = new jalview.datamodel.PDBEntry();
            entry.setId(ids[p].getId());
            if (ids[p].getType() != null)
            {
              if (PDBEntry.Type.getType(ids[p].getType()) != null)
              {
                entry.setType(PDBEntry.Type.getType(ids[p].getType()));
              }
              else
              {
                entry.setType(PDBEntry.Type.FILE);
              }
            }
            // jprovider is null when executing 'New View'
            if (ids[p].getFile() != null && jprovider != null)
            {
              if (!pdbloaded.containsKey(ids[p].getFile()))
              {
                entry.setFile(loadPDBFile(jprovider, ids[p].getId(),
                        ids[p].getFile()));
              }
              else
              {
                entry.setFile(pdbloaded.get(ids[p].getId()).toString());
              }
            }
            if (ids[p].getPdbentryItem() != null)
            {
              for (PdbentryItem item : ids[p].getPdbentryItem())
              {
                for (Property pr : item.getProperty())
                {
                  entry.setProperty(pr.getName(), pr.getValue());
                }
              }
            }
            StructureSelectionManager.getStructureSelectionManager(
                    Desktop.instance).registerPDBEntry(entry);
            // adds PDBEntry to datasequence's set (since Jalview 2.10)
            if (al.getSequenceAt(i).getDatasetSequence() != null)
            {
              al.getSequenceAt(i).getDatasetSequence().addPDBId(entry);
            }
            else
            {
              al.getSequenceAt(i).addPDBId(entry);
            }
          }
        }
      }
    } // end !multipleview

    // ///////////////////////////////
    // LOAD SEQUENCE MAPPINGS

    if (vamsasSet.getAlcodonFrameCount() > 0)
    {
      // TODO Potentially this should only be done once for all views of an
      // alignment
      AlcodonFrame[] alc = vamsasSet.getAlcodonFrame();
      for (int i = 0; i < alc.length; i++)
      {
        AlignedCodonFrame cf = new AlignedCodonFrame();
        if (alc[i].getAlcodMapCount() > 0)
        {
          AlcodMap[] maps = alc[i].getAlcodMap();
          for (int m = 0; m < maps.length; m++)
          {
            SequenceI dnaseq = seqRefIds.get(maps[m].getDnasq());
            // Load Mapping
            jalview.datamodel.Mapping mapping = null;
            // attach to dna sequence reference.
            if (maps[m].getMapping() != null)
            {
              mapping = addMapping(maps[m].getMapping());
              if (dnaseq != null && mapping.getTo() != null)
              {
                cf.addMap(dnaseq, mapping.getTo(), mapping.getMap());
              }
              else
              {
                // defer to later
                frefedSequence.add(newAlcodMapRef(maps[m].getDnasq(), cf,
                        mapping));
              }
            }
          }
          al.addCodonFrame(cf);
        }
      }
    }

    // ////////////////////////////////
    // LOAD ANNOTATIONS
    List<JvAnnotRow> autoAlan = new ArrayList<JvAnnotRow>();

    /*
     * store any annotations which forward reference a group's ID
     */
    Map<String, List<AlignmentAnnotation>> groupAnnotRefs = new Hashtable<String, List<AlignmentAnnotation>>();

    if (vamsasSet.getAnnotationCount() > 0)
    {
      Annotation[] an = vamsasSet.getAnnotation();

      for (int i = 0; i < an.length; i++)
      {
        Annotation annotation = an[i];

        /**
         * test if annotation is automatically calculated for this view only
         */
        boolean autoForView = false;
        if (annotation.getLabel().equals("Quality")
                || annotation.getLabel().equals("Conservation")
                || annotation.getLabel().equals("Consensus"))
        {
          // Kludge for pre 2.5 projects which lacked the autocalculated flag
          autoForView = true;
          if (!annotation.hasAutoCalculated())
          {
            annotation.setAutoCalculated(true);
          }
        }
        if (autoForView
                || (annotation.hasAutoCalculated() && annotation
                        .isAutoCalculated()))
        {
          // remove ID - we don't recover annotation from other views for
          // view-specific annotation
          annotation.setId(null);
        }

        // set visiblity for other annotation in this view
        String annotationId = annotation.getId();
        if (annotationId != null && annotationIds.containsKey(annotationId))
        {
          AlignmentAnnotation jda = annotationIds.get(annotationId);
          // in principle Visible should always be true for annotation displayed
          // in multiple views
          if (annotation.hasVisible())
          {
            jda.visible = annotation.getVisible();
          }

          al.addAnnotation(jda);

          continue;
        }
        // Construct new annotation from model.
        AnnotationElement[] ae = annotation.getAnnotationElement();
        jalview.datamodel.Annotation[] anot = null;
        java.awt.Color firstColour = null;
        int anpos;
        if (!annotation.getScoreOnly())
        {
          anot = new jalview.datamodel.Annotation[al.getWidth()];
          for (int aa = 0; aa < ae.length && aa < anot.length; aa++)
          {
            anpos = ae[aa].getPosition();

            if (anpos >= anot.length)
            {
              continue;
            }

            anot[anpos] = new jalview.datamodel.Annotation(

            ae[aa].getDisplayCharacter(), ae[aa].getDescription(),
                    (ae[aa].getSecondaryStructure() == null || ae[aa]
                            .getSecondaryStructure().length() == 0) ? ' '
                            : ae[aa].getSecondaryStructure().charAt(0),
                    ae[aa].getValue()

            );
            // JBPNote: Consider verifying dataflow for IO of secondary
            // structure annotation read from Stockholm files
            // this was added to try to ensure that
            // if (anot[ae[aa].getPosition()].secondaryStructure>' ')
            // {
            // anot[ae[aa].getPosition()].displayCharacter = "";
            // }
            anot[anpos].colour = new java.awt.Color(ae[aa].getColour());
            if (firstColour == null)
            {
              firstColour = anot[anpos].colour;
            }
          }
        }
        jalview.datamodel.AlignmentAnnotation jaa = null;

        if (annotation.getGraph())
        {
          float llim = 0, hlim = 0;
          // if (autoForView || an[i].isAutoCalculated()) {
          // hlim=11f;
          // }
          jaa = new jalview.datamodel.AlignmentAnnotation(
                  annotation.getLabel(), annotation.getDescription(), anot,
                  llim, hlim, annotation.getGraphType());

          jaa.graphGroup = annotation.getGraphGroup();
          jaa._linecolour = firstColour;
          if (annotation.getThresholdLine() != null)
          {
            jaa.setThreshold(new jalview.datamodel.GraphLine(annotation
                    .getThresholdLine().getValue(), annotation
                    .getThresholdLine().getLabel(), new java.awt.Color(
                    annotation.getThresholdLine().getColour())));

          }
          if (autoForView || annotation.isAutoCalculated())
          {
            // Hardwire the symbol display line to ensure that labels for
            // histograms are displayed
            jaa.hasText = true;
          }
        }
        else
        {
          jaa = new jalview.datamodel.AlignmentAnnotation(an[i].getLabel(),
                  an[i].getDescription(), anot);
          jaa._linecolour = firstColour;
        }
        // register new annotation
        if (an[i].getId() != null)
        {
          annotationIds.put(an[i].getId(), jaa);
          jaa.annotationId = an[i].getId();
        }
        // recover sequence association
        String sequenceRef = an[i].getSequenceRef();
        if (sequenceRef != null)
        {
          // from 2.9 sequenceRef is to sequence id (JAL-1781)
          SequenceI sequence = seqRefIds.get(sequenceRef);
          if (sequence == null)
          {
            // in pre-2.9 projects sequence ref is to sequence name
            sequence = al.findName(sequenceRef);
          }
          if (sequence != null)
          {
            jaa.createSequenceMapping(sequence, 1, true);
            sequence.addAlignmentAnnotation(jaa);
          }
        }
        // and make a note of any group association
        if (an[i].getGroupRef() != null && an[i].getGroupRef().length() > 0)
        {
          List<jalview.datamodel.AlignmentAnnotation> aal = groupAnnotRefs
                  .get(an[i].getGroupRef());
          if (aal == null)
          {
            aal = new ArrayList<jalview.datamodel.AlignmentAnnotation>();
            groupAnnotRefs.put(an[i].getGroupRef(), aal);
          }
          aal.add(jaa);
        }

        if (an[i].hasScore())
        {
          jaa.setScore(an[i].getScore());
        }
        if (an[i].hasVisible())
        {
          jaa.visible = an[i].getVisible();
        }

        if (an[i].hasCentreColLabels())
        {
          jaa.centreColLabels = an[i].getCentreColLabels();
        }

        if (an[i].hasScaleColLabels())
        {
          jaa.scaleColLabel = an[i].getScaleColLabels();
        }
        if (an[i].hasAutoCalculated() && an[i].isAutoCalculated())
        {
          // newer files have an 'autoCalculated' flag and store calculation
          // state in viewport properties
          jaa.autoCalculated = true; // means annotation will be marked for
          // update at end of load.
        }
        if (an[i].hasGraphHeight())
        {
          jaa.graphHeight = an[i].getGraphHeight();
        }
        if (an[i].hasBelowAlignment())
        {
          jaa.belowAlignment = an[i].isBelowAlignment();
        }
        jaa.setCalcId(an[i].getCalcId());
        if (an[i].getPropertyCount() > 0)
        {
          for (jalview.schemabinding.version2.Property prop : an[i]
                  .getProperty())
          {
            jaa.setProperty(prop.getName(), prop.getValue());
          }
        }
        if (jaa.autoCalculated)
        {
          autoAlan.add(new JvAnnotRow(i, jaa));
        }
        else
        // if (!autoForView)
        {
          // add autocalculated group annotation and any user created annotation
          // for the view
          al.addAnnotation(jaa);
        }
      }
    }
    // ///////////////////////
    // LOAD GROUPS
    // Create alignment markup and styles for this view
    if (jms.getJGroupCount() > 0)
    {
      JGroup[] groups = jms.getJGroup();
      boolean addAnnotSchemeGroup = false;
      for (int i = 0; i < groups.length; i++)
      {
        JGroup jGroup = groups[i];
        ColourSchemeI cs = null;
        if (jGroup.getColour() != null)
        {
          if (jGroup.getColour().startsWith("ucs"))
          {
            cs = getUserColourScheme(jms, jGroup.getColour());
          }
          else if (jGroup.getColour().equals("AnnotationColourGradient")
                  && jGroup.getAnnotationColours() != null)
          {
            addAnnotSchemeGroup = true;
            cs = null;
          }
          else
          {
            cs = ColourSchemeProperty.getColour(al, jGroup.getColour());
          }

          if (cs != null)
          {
            cs.setThreshold(jGroup.getPidThreshold(), true);
          }
        }

        Vector<SequenceI> seqs = new Vector<SequenceI>();

        for (int s = 0; s < jGroup.getSeqCount(); s++)
        {
          String seqId = jGroup.getSeq(s) + "";
          SequenceI ts = seqRefIds.get(seqId);

          if (ts != null)
          {
            seqs.addElement(ts);
          }
        }

        if (seqs.size() < 1)
        {
          continue;
        }

        SequenceGroup sg = new SequenceGroup(seqs, jGroup.getName(), cs,
                jGroup.getDisplayBoxes(), jGroup.getDisplayText(),
                jGroup.getColourText(), jGroup.getStart(), jGroup.getEnd());

        sg.setOutlineColour(new java.awt.Color(jGroup.getOutlineColour()));

        sg.textColour = new java.awt.Color(jGroup.getTextCol1());
        sg.textColour2 = new java.awt.Color(jGroup.getTextCol2());
        sg.setShowNonconserved(jGroup.hasShowUnconserved() ? jGroup
                .isShowUnconserved() : false);
        sg.thresholdTextColour = jGroup.getTextColThreshold();
        if (jGroup.hasShowConsensusHistogram())
        {
          sg.setShowConsensusHistogram(jGroup.isShowConsensusHistogram());
        }
        ;
        if (jGroup.hasShowSequenceLogo())
        {
          sg.setshowSequenceLogo(jGroup.isShowSequenceLogo());
        }
        if (jGroup.hasNormaliseSequenceLogo())
        {
          sg.setNormaliseSequenceLogo(jGroup.isNormaliseSequenceLogo());
        }
        if (jGroup.hasIgnoreGapsinConsensus())
        {
          sg.setIgnoreGapsConsensus(jGroup.getIgnoreGapsinConsensus());
        }
        if (jGroup.getConsThreshold() != 0)
        {
          Conservation c = new Conservation("All", sg.getSequences(null),
                  0, sg.getWidth() - 1);
          c.calculate();
          c.verdict(false, 25);
          sg.cs.setConservation(c);
        }

        if (jGroup.getId() != null && groupAnnotRefs.size() > 0)
        {
          // re-instate unique group/annotation row reference
          List<AlignmentAnnotation> jaal = groupAnnotRefs.get(jGroup
                  .getId());
          if (jaal != null)
          {
            for (AlignmentAnnotation jaa : jaal)
            {
              jaa.groupRef = sg;
              if (jaa.autoCalculated)
              {
                // match up and try to set group autocalc alignment row for this
                // annotation
                if (jaa.label.startsWith("Consensus for "))
                {
                  sg.setConsensus(jaa);
                }
                // match up and try to set group autocalc alignment row for this
                // annotation
                if (jaa.label.startsWith("Conservation for "))
                {
                  sg.setConservationRow(jaa);
                }
              }
            }
          }
        }
        al.addGroup(sg);
        if (addAnnotSchemeGroup)
        {
          // reconstruct the annotation colourscheme
          sg.cs = constructAnnotationColour(jGroup.getAnnotationColours(),
                  null, al, jms, false);
        }
      }
    }
    if (view == null)
    {
      // only dataset in this model, so just return.
      return null;
    }
    // ///////////////////////////////
    // LOAD VIEWPORT

    // If we just load in the same jar file again, the sequenceSetId
    // will be the same, and we end up with multiple references
    // to the same sequenceSet. We must modify this id on load
    // so that each load of the file gives a unique id
    String uniqueSeqSetId = view.getSequenceSetId() + uniqueSetSuffix;
    String viewId = (view.getId() == null ? null : view.getId()
            + uniqueSetSuffix);
    AlignFrame af = null;
    AlignViewport av = null;
    // now check to see if we really need to create a new viewport.
    if (multipleView && viewportsAdded.size() == 0)
    {
      // We recovered an alignment for which a viewport already exists.
      // TODO: fix up any settings necessary for overlaying stored state onto
      // state recovered from another document. (may not be necessary).
      // we may need a binding from a viewport in memory to one recovered from
      // XML.
      // and then recover its containing af to allow the settings to be applied.
      // TODO: fix for vamsas demo
      System.err
              .println("About to recover a viewport for existing alignment: Sequence set ID is "
                      + uniqueSeqSetId);
      Object seqsetobj = retrieveExistingObj(uniqueSeqSetId);
      if (seqsetobj != null)
      {
        if (seqsetobj instanceof String)
        {
          uniqueSeqSetId = (String) seqsetobj;
          System.err
                  .println("Recovered extant sequence set ID mapping for ID : New Sequence set ID is "
                          + uniqueSeqSetId);
        }
        else
        {
          System.err
                  .println("Warning : Collision between sequence set ID string and existing jalview object mapping.");
        }

      }
    }
    /**
     * indicate that annotation colours are applied across all groups (pre
     * Jalview 2.8.1 behaviour)
     */
    boolean doGroupAnnColour = Jalview2XML.isVersionStringLaterThan(
            "2.8.1", object.getVersion());

    AlignmentPanel ap = null;
    boolean isnewview = true;
    if (viewId != null)
    {
      // Check to see if this alignment already has a view id == viewId
      jalview.gui.AlignmentPanel views[] = Desktop
              .getAlignmentPanels(uniqueSeqSetId);
      if (views != null && views.length > 0)
      {
        for (int v = 0; v < views.length; v++)
        {
          if (views[v].av.getViewId().equalsIgnoreCase(viewId))
          {
            // recover the existing alignpanel, alignframe, viewport
            af = views[v].alignFrame;
            av = views[v].av;
            ap = views[v];
            // TODO: could even skip resetting view settings if we don't want to
            // change the local settings from other jalview processes
            isnewview = false;
          }
        }
      }
    }

    if (isnewview)
    {
      af = loadViewport(file, jseqs, hiddenSeqs, al, jms, view,
              uniqueSeqSetId, viewId, autoAlan);
      av = af.viewport;
      ap = af.alignPanel;
    }

    /*
     * Load any trees, PDB structures and viewers
     * 
     * Not done if flag is false (when this method is used for New View)
     */
    if (loadTreesAndStructures)
    {
      loadTrees(jms, view, af, av, ap);
      loadPDBStructures(jprovider, jseqs, af, ap);
      loadRnaViewers(jprovider, jseqs, ap);
    }
    // and finally return.
    return af;
  }

  /**
   * Instantiate and link any saved RNA (Varna) viewers. The state of the Varna
   * panel is restored from separate jar entries, two (gapped and trimmed) per
   * sequence and secondary structure.
   * 
   * Currently each viewer shows just one sequence and structure (gapped and
   * trimmed), however this method is designed to support multiple sequences or
   * structures in viewers if wanted in future.
   * 
   * @param jprovider
   * @param jseqs
   * @param ap
   */
  private void loadRnaViewers(jarInputStreamProvider jprovider,
          JSeq[] jseqs, AlignmentPanel ap)
  {
    /*
     * scan the sequences for references to viewers; create each one the first
     * time it is referenced, add Rna models to existing viewers
     */
    for (JSeq jseq : jseqs)
    {
      for (int i = 0; i < jseq.getRnaViewerCount(); i++)
      {
        RnaViewer viewer = jseq.getRnaViewer(i);
        AppVarna appVarna = findOrCreateVarnaViewer(viewer,
                uniqueSetSuffix, ap);

        for (int j = 0; j < viewer.getSecondaryStructureCount(); j++)
        {
          SecondaryStructure ss = viewer.getSecondaryStructure(j);
          SequenceI seq = seqRefIds.get(jseq.getId());
          AlignmentAnnotation ann = this.annotationIds.get(ss
                  .getAnnotationId());

          /*
           * add the structure to the Varna display (with session state copied
           * from the jar to a temporary file)
           */
          boolean gapped = ss.isGapped();
          String rnaTitle = ss.getTitle();
          String sessionState = ss.getViewerState();
          String tempStateFile = copyJarEntry(jprovider, sessionState,
                  "varna", null);
          RnaModel rna = new RnaModel(rnaTitle, ann, seq, null, gapped);
          appVarna.addModelSession(rna, rnaTitle, tempStateFile);
        }
        appVarna.setInitialSelection(viewer.getSelectedRna());
      }
    }
  }

  /**
   * Locate and return an already instantiated matching AppVarna, or create one
   * if not found
   * 
   * @param viewer
   * @param viewIdSuffix
   * @param ap
   * @return
   */
  protected AppVarna findOrCreateVarnaViewer(RnaViewer viewer,
          String viewIdSuffix, AlignmentPanel ap)
  {
    /*
     * on each load a suffix is appended to the saved viewId, to avoid conflicts
     * if load is repeated
     */
    String postLoadId = viewer.getViewId() + viewIdSuffix;
    for (JInternalFrame frame : getAllFrames())
    {
      if (frame instanceof AppVarna)
      {
        AppVarna varna = (AppVarna) frame;
        if (postLoadId.equals(varna.getViewId()))
        {
          // this viewer is already instantiated
          // could in future here add ap as another 'parent' of the
          // AppVarna window; currently just 1-to-many
          return varna;
        }
      }
    }

    /*
     * viewer not found - make it
     */
    RnaViewerModel model = new RnaViewerModel(postLoadId,
            viewer.getTitle(), viewer.getXpos(), viewer.getYpos(),
            viewer.getWidth(), viewer.getHeight(),
            viewer.getDividerLocation());
    AppVarna varna = new AppVarna(model, ap);

    return varna;
  }

  /**
   * Load any saved trees
   * 
   * @param jms
   * @param view
   * @param af
   * @param av
   * @param ap
   */
  protected void loadTrees(JalviewModelSequence jms, Viewport view,
          AlignFrame af, AlignViewport av, AlignmentPanel ap)
  {
    // TODO result of automated refactoring - are all these parameters needed?
    try
    {
      for (int t = 0; t < jms.getTreeCount(); t++)
      {

        Tree tree = jms.getTree(t);

        TreePanel tp = (TreePanel) retrieveExistingObj(tree.getId());
        if (tp == null)
        {
          tp = af.ShowNewickTree(
                  new jalview.io.NewickFile(tree.getNewick()),
                  tree.getTitle(), tree.getWidth(), tree.getHeight(),
                  tree.getXpos(), tree.getYpos());
          if (tree.getId() != null)
          {
            // perhaps bind the tree id to something ?
          }
        }
        else
        {
          // update local tree attributes ?
          // TODO: should check if tp has been manipulated by user - if so its
          // settings shouldn't be modified
          tp.setTitle(tree.getTitle());
          tp.setBounds(new Rectangle(tree.getXpos(), tree.getYpos(), tree
                  .getWidth(), tree.getHeight()));
          tp.av = av; // af.viewport; // TODO: verify 'associate with all
          // views'
          // works still
          tp.treeCanvas.av = av; // af.viewport;
          tp.treeCanvas.ap = ap; // af.alignPanel;

        }
        if (tp == null)
        {
          warn("There was a problem recovering stored Newick tree: \n"
                  + tree.getNewick());
          continue;
        }

        tp.fitToWindow.setState(tree.getFitToWindow());
        tp.fitToWindow_actionPerformed(null);

        if (tree.getFontName() != null)
        {
          tp.setTreeFont(new java.awt.Font(tree.getFontName(), tree
                  .getFontStyle(), tree.getFontSize()));
        }
        else
        {
          tp.setTreeFont(new java.awt.Font(view.getFontName(), view
                  .getFontStyle(), tree.getFontSize()));
        }

        tp.showPlaceholders(tree.getMarkUnlinked());
        tp.showBootstrap(tree.getShowBootstrap());
        tp.showDistances(tree.getShowDistances());

        tp.treeCanvas.threshold = tree.getThreshold();

        if (tree.getCurrentTree())
        {
          af.viewport.setCurrentTree(tp.getTree());
        }
      }

    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  /**
   * Load and link any saved structure viewers.
   * 
   * @param jprovider
   * @param jseqs
   * @param af
   * @param ap
   */
  protected void loadPDBStructures(jarInputStreamProvider jprovider,
          JSeq[] jseqs, AlignFrame af, AlignmentPanel ap)
  {
    /*
     * Run through all PDB ids on the alignment, and collect mappings between
     * distinct view ids and all sequences referring to that view.
     */
    Map<String, StructureViewerModel> structureViewers = new LinkedHashMap<String, StructureViewerModel>();

    for (int i = 0; i < jseqs.length; i++)
    {
      if (jseqs[i].getPdbidsCount() > 0)
      {
        Pdbids[] ids = jseqs[i].getPdbids();
        for (int p = 0; p < ids.length; p++)
        {
          final int structureStateCount = ids[p].getStructureStateCount();
          for (int s = 0; s < structureStateCount; s++)
          {
            // check to see if we haven't already created this structure view
            final StructureState structureState = ids[p]
                    .getStructureState(s);
            String sviewid = (structureState.getViewId() == null) ? null
                    : structureState.getViewId() + uniqueSetSuffix;
            jalview.datamodel.PDBEntry jpdb = new jalview.datamodel.PDBEntry();
            // Originally : ids[p].getFile()
            // : TODO: verify external PDB file recovery still works in normal
            // jalview project load
            jpdb.setFile(loadPDBFile(jprovider, ids[p].getId(),
                    ids[p].getFile()));
            jpdb.setId(ids[p].getId());

            int x = structureState.getXpos();
            int y = structureState.getYpos();
            int width = structureState.getWidth();
            int height = structureState.getHeight();

            // Probably don't need to do this anymore...
            // Desktop.desktop.getComponentAt(x, y);
            // TODO: NOW: check that this recovers the PDB file correctly.
            String pdbFile = loadPDBFile(jprovider, ids[p].getId(),
                    ids[p].getFile());
            jalview.datamodel.SequenceI seq = seqRefIds.get(jseqs[i]
                    .getId() + "");
            if (sviewid == null)
            {
              sviewid = "_jalview_pre2_4_" + x + "," + y + "," + width
                      + "," + height;
            }
            if (!structureViewers.containsKey(sviewid))
            {
              structureViewers.put(sviewid,
                      new StructureViewerModel(x, y, width, height, false,
                              false, true, structureState.getViewId(),
                              structureState.getType()));
              // Legacy pre-2.7 conversion JAL-823 :
              // do not assume any view has to be linked for colour by
              // sequence
            }

            // assemble String[] { pdb files }, String[] { id for each
            // file }, orig_fileloc, SequenceI[][] {{ seqs_file 1 }, {
            // seqs_file 2}, boolean[] {
            // linkAlignPanel,superposeWithAlignpanel}} from hash
            StructureViewerModel jmoldat = structureViewers.get(sviewid);
            jmoldat.setAlignWithPanel(jmoldat.isAlignWithPanel()
                    | (structureState.hasAlignwithAlignPanel() ? structureState
                            .getAlignwithAlignPanel() : false));

            /*
             * Default colour by linked panel to false if not specified (e.g.
             * for pre-2.7 projects)
             */
            boolean colourWithAlignPanel = jmoldat.isColourWithAlignPanel();
            colourWithAlignPanel |= (structureState
                    .hasColourwithAlignPanel() ? structureState
                    .getColourwithAlignPanel() : false);
            jmoldat.setColourWithAlignPanel(colourWithAlignPanel);

            /*
             * Default colour by viewer to true if not specified (e.g. for
             * pre-2.7 projects)
             */
            boolean colourByViewer = jmoldat.isColourByViewer();
            colourByViewer &= structureState.hasColourByJmol() ? structureState
                    .getColourByJmol() : true;
            jmoldat.setColourByViewer(colourByViewer);

            if (jmoldat.getStateData().length() < structureState
                    .getContent().length())
            {
              {
                jmoldat.setStateData(structureState.getContent());
              }
            }
            if (ids[p].getFile() != null)
            {
              File mapkey = new File(ids[p].getFile());
              StructureData seqstrmaps = jmoldat.getFileData().get(mapkey);
              if (seqstrmaps == null)
              {
                jmoldat.getFileData().put(
                        mapkey,
                        seqstrmaps = jmoldat.new StructureData(pdbFile,
                                ids[p].getId()));
              }
              if (!seqstrmaps.getSeqList().contains(seq))
              {
                seqstrmaps.getSeqList().add(seq);
                // TODO and chains?
              }
            }
            else
            {
              errorMessage = ("The Jmol views in this project were imported\nfrom an older version of Jalview.\nPlease review the sequence colour associations\nin the Colour by section of the Jmol View menu.\n\nIn the case of problems, see note at\nhttp://issues.jalview.org/browse/JAL-747");
              warn(errorMessage);
            }
          }
        }
      }
    }
    // Instantiate the associated structure views
    for (Entry<String, StructureViewerModel> entry : structureViewers
            .entrySet())
    {
      try
      {
        createOrLinkStructureViewer(entry, af, ap, jprovider);
      } catch (Exception e)
      {
        System.err.println("Error loading structure viewer: "
                + e.getMessage());
        // failed - try the next one
      }
    }
  }

  /**
   * 
   * @param viewerData
   * @param af
   * @param ap
   * @param jprovider
   */
  protected void createOrLinkStructureViewer(
          Entry<String, StructureViewerModel> viewerData, AlignFrame af,
          AlignmentPanel ap, jarInputStreamProvider jprovider)
  {
    final StructureViewerModel stateData = viewerData.getValue();

    /*
     * Search for any viewer windows already open from other alignment views
     * that exactly match the stored structure state
     */
    StructureViewerBase comp = findMatchingViewer(viewerData);

    if (comp != null)
    {
      linkStructureViewer(ap, comp, stateData);
      return;
    }

    /*
     * From 2.9: stateData.type contains JMOL or CHIMERA, data is in jar entry
     * "viewer_"+stateData.viewId
     */
    if (ViewerType.CHIMERA.toString().equals(stateData.getType()))
    {
      createChimeraViewer(viewerData, af, jprovider);
    }
    else
    {
      /*
       * else Jmol (if pre-2.9, stateData contains JMOL state string)
       */
      createJmolViewer(viewerData, af, jprovider);
    }
  }

  /**
   * Create a new Chimera viewer.
   * 
   * @param data
   * @param af
   * @param jprovider
   */
  protected void createChimeraViewer(
          Entry<String, StructureViewerModel> viewerData, AlignFrame af,
          jarInputStreamProvider jprovider)
  {
    StructureViewerModel data = viewerData.getValue();
    String chimeraSessionFile = data.getStateData();

    /*
     * Copy Chimera session from jar entry "viewer_"+viewId to a temporary file
     * 
     * NB this is the 'saved' viewId as in the project file XML, _not_ the
     * 'uniquified' sviewid used to reconstruct the viewer here
     */
    String viewerJarEntryName = getViewerJarEntryName(data.getViewId());
    chimeraSessionFile = copyJarEntry(jprovider, viewerJarEntryName,
            "chimera", null);

    Set<Entry<File, StructureData>> fileData = data.getFileData()
            .entrySet();
    List<PDBEntry> pdbs = new ArrayList<PDBEntry>();
    List<SequenceI[]> allseqs = new ArrayList<SequenceI[]>();
    for (Entry<File, StructureData> pdb : fileData)
    {
      String filePath = pdb.getValue().getFilePath();
      String pdbId = pdb.getValue().getPdbId();
      // pdbs.add(new PDBEntry(filePath, pdbId));
      pdbs.add(new PDBEntry(pdbId, null, PDBEntry.Type.PDB, filePath));
      final List<SequenceI> seqList = pdb.getValue().getSeqList();
      SequenceI[] seqs = seqList.toArray(new SequenceI[seqList.size()]);
      allseqs.add(seqs);
    }

    boolean colourByChimera = data.isColourByViewer();
    boolean colourBySequence = data.isColourWithAlignPanel();

    // TODO use StructureViewer as a factory here, see JAL-1761
    final PDBEntry[] pdbArray = pdbs.toArray(new PDBEntry[pdbs.size()]);
    final SequenceI[][] seqsArray = allseqs.toArray(new SequenceI[allseqs
            .size()][]);
    String newViewId = viewerData.getKey();

    ChimeraViewFrame cvf = new ChimeraViewFrame(chimeraSessionFile,
            af.alignPanel, pdbArray, seqsArray, colourByChimera,
            colourBySequence, newViewId);
    cvf.setSize(data.getWidth(), data.getHeight());
    cvf.setLocation(data.getX(), data.getY());
  }

  /**
   * Create a new Jmol window. First parse the Jmol state to translate filenames
   * loaded into the view, and record the order in which files are shown in the
   * Jmol view, so we can add the sequence mappings in same order.
   * 
   * @param viewerData
   * @param af
   * @param jprovider
   */
  protected void createJmolViewer(
          final Entry<String, StructureViewerModel> viewerData,
          AlignFrame af, jarInputStreamProvider jprovider)
  {
    final StructureViewerModel svattrib = viewerData.getValue();
    String state = svattrib.getStateData();

    /*
     * Pre-2.9: state element value is the Jmol state string
     * 
     * 2.9+: @type is "JMOL", state data is in a Jar file member named "viewer_"
     * + viewId
     */
    if (ViewerType.JMOL.toString().equals(svattrib.getType()))
    {
      state = readJarEntry(jprovider,
              getViewerJarEntryName(svattrib.getViewId()));
    }

    List<String> pdbfilenames = new ArrayList<String>();
    List<SequenceI[]> seqmaps = new ArrayList<SequenceI[]>();
    List<String> pdbids = new ArrayList<String>();
    StringBuilder newFileLoc = new StringBuilder(64);
    int cp = 0, ncp, ecp;
    Map<File, StructureData> oldFiles = svattrib.getFileData();
    while ((ncp = state.indexOf("load ", cp)) > -1)
    {
      do
      {
        // look for next filename in load statement
        newFileLoc.append(state.substring(cp,
                ncp = (state.indexOf("\"", ncp + 1) + 1)));
        String oldfilenam = state.substring(ncp,
                ecp = state.indexOf("\"", ncp));
        // recover the new mapping data for this old filename
        // have to normalize filename - since Jmol and jalview do
        // filename
        // translation differently.
        StructureData filedat = oldFiles.get(new File(oldfilenam));
        if (filedat == null)
        {
          String reformatedOldFilename = oldfilenam.replaceAll("/", "\\\\");
          filedat = oldFiles.get(new File(reformatedOldFilename));
        }
        newFileLoc.append(Platform.escapeString(filedat.getFilePath()));
        pdbfilenames.add(filedat.getFilePath());
        pdbids.add(filedat.getPdbId());
        seqmaps.add(filedat.getSeqList().toArray(new SequenceI[0]));
        newFileLoc.append("\"");
        cp = ecp + 1; // advance beyond last \" and set cursor so we can
                      // look for next file statement.
      } while ((ncp = state.indexOf("/*file*/", cp)) > -1);
    }
    if (cp > 0)
    {
      // just append rest of state
      newFileLoc.append(state.substring(cp));
    }
    else
    {
      System.err.print("Ignoring incomplete Jmol state for PDB ids: ");
      newFileLoc = new StringBuilder(state);
      newFileLoc.append("; load append ");
      for (File id : oldFiles.keySet())
      {
        // add this and any other pdb files that should be present in
        // the viewer
        StructureData filedat = oldFiles.get(id);
        newFileLoc.append(filedat.getFilePath());
        pdbfilenames.add(filedat.getFilePath());
        pdbids.add(filedat.getPdbId());
        seqmaps.add(filedat.getSeqList().toArray(new SequenceI[0]));
        newFileLoc.append(" \"");
        newFileLoc.append(filedat.getFilePath());
        newFileLoc.append("\"");

      }
      newFileLoc.append(";");
    }

    if (newFileLoc.length() == 0)
    {
      return;
    }
    int histbug = newFileLoc.indexOf("history = ");
    if (histbug > -1)
    {
      /*
       * change "history = [true|false];" to "history = [1|0];"
       */
      histbug += 10;
      int diff = histbug == -1 ? -1 : newFileLoc.indexOf(";", histbug);
      String val = (diff == -1) ? null : newFileLoc
              .substring(histbug, diff);
      if (val != null && val.length() >= 4)
      {
        if (val.contains("e")) // eh? what can it be?
        {
          if (val.trim().equals("true"))
          {
            val = "1";
          }
          else
          {
            val = "0";
          }
          newFileLoc.replace(histbug, diff, val);
        }
      }
    }

    final String[] pdbf = pdbfilenames.toArray(new String[pdbfilenames
            .size()]);
    final String[] id = pdbids.toArray(new String[pdbids.size()]);
    final SequenceI[][] sq = seqmaps
            .toArray(new SequenceI[seqmaps.size()][]);
    final String fileloc = newFileLoc.toString();
    final String sviewid = viewerData.getKey();
    final AlignFrame alf = af;
    final Rectangle rect = new Rectangle(svattrib.getX(), svattrib.getY(),
            svattrib.getWidth(), svattrib.getHeight());
    try
    {
      javax.swing.SwingUtilities.invokeAndWait(new Runnable()
      {
        @Override
        public void run()
        {
          JalviewStructureDisplayI sview = null;
          try
          {
            sview = new StructureViewer(alf.alignPanel
                    .getStructureSelectionManager()).createView(
                    StructureViewer.ViewerType.JMOL, pdbf, id, sq,
                    alf.alignPanel, svattrib, fileloc, rect, sviewid);
            addNewStructureViewer(sview);
          } catch (OutOfMemoryError ex)
          {
            new OOMWarning("restoring structure view for PDB id " + id,
                    (OutOfMemoryError) ex.getCause());
            if (sview != null && sview.isVisible())
            {
              sview.closeViewer(false);
              sview.setVisible(false);
              sview.dispose();
            }
          }
        }
      });
    } catch (InvocationTargetException ex)
    {
      warn("Unexpected error when opening Jmol view.", ex);

    } catch (InterruptedException e)
    {
      // e.printStackTrace();
    }

  }

  /**
   * Generates a name for the entry in the project jar file to hold state
   * information for a structure viewer
   * 
   * @param viewId
   * @return
   */
  protected String getViewerJarEntryName(String viewId)
  {
    return VIEWER_PREFIX + viewId;
  }

  /**
   * Returns any open frame that matches given structure viewer data. The match
   * is based on the unique viewId, or (for older project versions) the frame's
   * geometry.
   * 
   * @param viewerData
   * @return
   */
  protected StructureViewerBase findMatchingViewer(
          Entry<String, StructureViewerModel> viewerData)
  {
    final String sviewid = viewerData.getKey();
    final StructureViewerModel svattrib = viewerData.getValue();
    StructureViewerBase comp = null;
    JInternalFrame[] frames = getAllFrames();
    for (JInternalFrame frame : frames)
    {
      if (frame instanceof StructureViewerBase)
      {
        /*
         * Post jalview 2.4 schema includes structure view id
         */
        if (sviewid != null
                && ((StructureViewerBase) frame).getViewId()
                        .equals(sviewid))
        {
          comp = (StructureViewerBase) frame;
          break; // break added in 2.9
        }
        /*
         * Otherwise test for matching position and size of viewer frame
         */
        else if (frame.getX() == svattrib.getX()
                && frame.getY() == svattrib.getY()
                && frame.getHeight() == svattrib.getHeight()
                && frame.getWidth() == svattrib.getWidth())
        {
          comp = (StructureViewerBase) frame;
          // no break in faint hope of an exact match on viewId
        }
      }
    }
    return comp;
  }

  /**
   * Link an AlignmentPanel to an existing structure viewer.
   * 
   * @param ap
   * @param viewer
   * @param oldFiles
   * @param useinViewerSuperpos
   * @param usetoColourbyseq
   * @param viewerColouring
   */
  protected void linkStructureViewer(AlignmentPanel ap,
          StructureViewerBase viewer, StructureViewerModel stateData)
  {
    // NOTE: if the jalview project is part of a shared session then
    // view synchronization should/could be done here.

    final boolean useinViewerSuperpos = stateData.isAlignWithPanel();
    final boolean usetoColourbyseq = stateData.isColourWithAlignPanel();
    final boolean viewerColouring = stateData.isColourByViewer();
    Map<File, StructureData> oldFiles = stateData.getFileData();

    /*
     * Add mapping for sequences in this view to an already open viewer
     */
    final AAStructureBindingModel binding = viewer.getBinding();
    for (File id : oldFiles.keySet())
    {
      // add this and any other pdb files that should be present in the
      // viewer
      StructureData filedat = oldFiles.get(id);
      String pdbFile = filedat.getFilePath();
      SequenceI[] seq = filedat.getSeqList().toArray(new SequenceI[0]);
      binding.getSsm().setMapping(seq, null, pdbFile,
              jalview.io.AppletFormatAdapter.FILE);
      binding.addSequenceForStructFile(pdbFile, seq);
    }
    // and add the AlignmentPanel's reference to the view panel
    viewer.addAlignmentPanel(ap);
    if (useinViewerSuperpos)
    {
      viewer.useAlignmentPanelForSuperposition(ap);
    }
    else
    {
      viewer.excludeAlignmentPanelForSuperposition(ap);
    }
    if (usetoColourbyseq)
    {
      viewer.useAlignmentPanelForColourbyseq(ap, !viewerColouring);
    }
    else
    {
      viewer.excludeAlignmentPanelForColourbyseq(ap);
    }
  }

  /**
   * Get all frames within the Desktop.
   * 
   * @return
   */
  protected JInternalFrame[] getAllFrames()
  {
    JInternalFrame[] frames = null;
    // TODO is this necessary - is it safe - risk of hanging?
    do
    {
      try
      {
        frames = Desktop.desktop.getAllFrames();
      } catch (ArrayIndexOutOfBoundsException e)
      {
        // occasional No such child exceptions are thrown here...
        try
        {
          Thread.sleep(10);
        } catch (InterruptedException f)
        {
        }
      }
    } while (frames == null);
    return frames;
  }

  /**
   * Answers true if 'version' is equal to or later than 'supported', where each
   * is formatted as major/minor versions like "2.8.3" or "2.3.4b1" for bugfix
   * changes. Development and test values for 'version' are leniently treated
   * i.e. answer true.
   * 
   * @param supported
   *          - minimum version we are comparing against
   * @param version
   *          - version of data being processsed
   * @return
   */
  public static boolean isVersionStringLaterThan(String supported,
          String version)
  {
    if (supported == null || version == null
            || version.equalsIgnoreCase("DEVELOPMENT BUILD")
            || version.equalsIgnoreCase("Test")
            || version.equalsIgnoreCase("AUTOMATED BUILD"))
    {
      System.err.println("Assuming project file with "
              + (version == null ? "null" : version)
              + " is compatible with Jalview version " + supported);
      return true;
    }
    else
    {
      return StringUtils.compareVersions(version, supported, "b") >= 0;
    }
  }

  Vector<JalviewStructureDisplayI> newStructureViewers = null;

  protected void addNewStructureViewer(JalviewStructureDisplayI sview)
  {
    if (newStructureViewers != null)
    {
      sview.getBinding().setFinishedLoadingFromArchive(false);
      newStructureViewers.add(sview);
    }
  }

  protected void setLoadingFinishedForNewStructureViewers()
  {
    if (newStructureViewers != null)
    {
      for (JalviewStructureDisplayI sview : newStructureViewers)
      {
        sview.getBinding().setFinishedLoadingFromArchive(true);
      }
      newStructureViewers.clear();
      newStructureViewers = null;
    }
  }

  AlignFrame loadViewport(String file, JSeq[] JSEQ,
          List<SequenceI> hiddenSeqs, AlignmentI al,
          JalviewModelSequence jms, Viewport view, String uniqueSeqSetId,
          String viewId, List<JvAnnotRow> autoAlan)
  {
    AlignFrame af = null;
    af = new AlignFrame(al, view.getWidth(), view.getHeight(),
            uniqueSeqSetId, viewId);

    af.setFileName(file, "Jalview");

    for (int i = 0; i < JSEQ.length; i++)
    {
      af.viewport.setSequenceColour(af.viewport.getAlignment()
              .getSequenceAt(i), new java.awt.Color(JSEQ[i].getColour()));
    }

    if (al.hasSeqrep())
    {
      af.getViewport().setColourByReferenceSeq(true);
      af.getViewport().setDisplayReferenceSeq(true);
    }

    af.viewport.setGatherViewsHere(view.getGatheredViews());

    if (view.getSequenceSetId() != null)
    {
      AlignmentViewport av = viewportsAdded.get(uniqueSeqSetId);

      af.viewport.setSequenceSetId(uniqueSeqSetId);
      if (av != null)
      {
        // propagate shared settings to this new view
        af.viewport.setHistoryList(av.getHistoryList());
        af.viewport.setRedoList(av.getRedoList());
      }
      else
      {
        viewportsAdded.put(uniqueSeqSetId, af.viewport);
      }
      // TODO: check if this method can be called repeatedly without
      // side-effects if alignpanel already registered.
      PaintRefresher.Register(af.alignPanel, uniqueSeqSetId);
    }
    // apply Hidden regions to view.
    if (hiddenSeqs != null)
    {
      for (int s = 0; s < JSEQ.length; s++)
      {
        SequenceGroup hidden = new SequenceGroup();
        boolean isRepresentative = false;
        for (int r = 0; r < JSEQ[s].getHiddenSequencesCount(); r++)
        {
          isRepresentative = true;
          SequenceI sequenceToHide = al.getSequenceAt(JSEQ[s]
                  .getHiddenSequences(r));
          hidden.addSequence(sequenceToHide, false);
          // remove from hiddenSeqs list so we don't try to hide it twice
          hiddenSeqs.remove(sequenceToHide);
        }
        if (isRepresentative)
        {
          SequenceI representativeSequence = al.getSequenceAt(s);
          hidden.addSequence(representativeSequence, false);
          af.viewport.hideRepSequences(representativeSequence, hidden);
        }
      }

      SequenceI[] hseqs = hiddenSeqs.toArray(new SequenceI[hiddenSeqs
              .size()]);
      af.viewport.hideSequence(hseqs);

    }
    // recover view properties and display parameters
    if (view.getViewName() != null)
    {
      af.viewport.viewName = view.getViewName();
      af.setInitialTabVisible();
    }
    af.setBounds(view.getXpos(), view.getYpos(), view.getWidth(),
            view.getHeight());

    af.viewport.setShowAnnotation(view.getShowAnnotation());
    af.viewport.setAbovePIDThreshold(view.getPidSelected());

    af.viewport.setColourText(view.getShowColourText());

    af.viewport.setConservationSelected(view.getConservationSelected());
    af.viewport.setShowJVSuffix(view.getShowFullId());
    af.viewport.setRightAlignIds(view.getRightAlignIds());
    af.viewport.setFont(
            new java.awt.Font(view.getFontName(), view.getFontStyle(), view
                    .getFontSize()), true);
    ViewStyleI vs = af.viewport.getViewStyle();
    vs.setScaleProteinAsCdna(view.isScaleProteinAsCdna());
    af.viewport.setViewStyle(vs);
    // TODO: allow custom charWidth/Heights to be restored by updating them
    // after setting font - which means set above to false
    af.viewport.setRenderGaps(view.getRenderGaps());
    af.viewport.setWrapAlignment(view.getWrapAlignment());
    af.viewport.setShowAnnotation(view.getShowAnnotation());

    af.viewport.setShowBoxes(view.getShowBoxes());

    af.viewport.setShowText(view.getShowText());

    af.viewport.setTextColour(new java.awt.Color(view.getTextCol1()));
    af.viewport.setTextColour2(new java.awt.Color(view.getTextCol2()));
    af.viewport.setThresholdTextColour(view.getTextColThreshold());
    af.viewport.setShowUnconserved(view.hasShowUnconserved() ? view
            .isShowUnconserved() : false);
    af.viewport.setStartRes(view.getStartRes());
    af.viewport.setStartSeq(view.getStartSeq());
    af.alignPanel.updateLayout();
    ColourSchemeI cs = null;
    // apply colourschemes
    if (view.getBgColour() != null)
    {
      if (view.getBgColour().startsWith("ucs"))
      {
        cs = getUserColourScheme(jms, view.getBgColour());
      }
      else if (view.getBgColour().startsWith("Annotation"))
      {
        AnnotationColours viewAnnColour = view.getAnnotationColours();
        cs = constructAnnotationColour(viewAnnColour, af, al, jms, true);

        // annpos

      }
      else
      {
        cs = ColourSchemeProperty.getColour(al, view.getBgColour());
      }

      if (cs != null)
      {
        cs.setThreshold(view.getPidThreshold(), true);
        cs.setConsensus(af.viewport.getSequenceConsensusHash());
      }
    }

    af.viewport.setGlobalColourScheme(cs);
    af.viewport.setColourAppliesToAllGroups(false);

    if (view.getConservationSelected() && cs != null)
    {
      cs.setConservationInc(view.getConsThreshold());
    }

    af.changeColour(cs);

    af.viewport.setColourAppliesToAllGroups(true);

    af.viewport.setShowSequenceFeatures(view.getShowSequenceFeatures());

    if (view.hasCentreColumnLabels())
    {
      af.viewport.setCentreColumnLabels(view.getCentreColumnLabels());
    }
    if (view.hasIgnoreGapsinConsensus())
    {
      af.viewport.setIgnoreGapsConsensus(view.getIgnoreGapsinConsensus(),
              null);
    }
    if (view.hasFollowHighlight())
    {
      af.viewport.setFollowHighlight(view.getFollowHighlight());
    }
    if (view.hasFollowSelection())
    {
      af.viewport.followSelection = view.getFollowSelection();
    }
    if (view.hasShowConsensusHistogram())
    {
      af.viewport.setShowConsensusHistogram(view
              .getShowConsensusHistogram());
    }
    else
    {
      af.viewport.setShowConsensusHistogram(true);
    }
    if (view.hasShowSequenceLogo())
    {
      af.viewport.setShowSequenceLogo(view.getShowSequenceLogo());
    }
    else
    {
      af.viewport.setShowSequenceLogo(false);
    }
    if (view.hasNormaliseSequenceLogo())
    {
      af.viewport.setNormaliseSequenceLogo(view.getNormaliseSequenceLogo());
    }
    if (view.hasShowDbRefTooltip())
    {
      af.viewport.setShowDBRefs(view.getShowDbRefTooltip());
    }
    if (view.hasShowNPfeatureTooltip())
    {
      af.viewport.setShowNPFeats(view.hasShowNPfeatureTooltip());
    }
    if (view.hasShowGroupConsensus())
    {
      af.viewport.setShowGroupConsensus(view.getShowGroupConsensus());
    }
    else
    {
      af.viewport.setShowGroupConsensus(false);
    }
    if (view.hasShowGroupConservation())
    {
      af.viewport.setShowGroupConservation(view.getShowGroupConservation());
    }
    else
    {
      af.viewport.setShowGroupConservation(false);
    }

    // recover featre settings
    if (jms.getFeatureSettings() != null)
    {
      FeaturesDisplayed fdi;
      af.viewport.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
      String[] renderOrder = new String[jms.getFeatureSettings()
              .getSettingCount()];
      Map<String, FeatureColourI> featureColours = new Hashtable<String, FeatureColourI>();
      Map<String, Float> featureOrder = new Hashtable<String, Float>();

      for (int fs = 0; fs < jms.getFeatureSettings().getSettingCount(); fs++)
      {
        Setting setting = jms.getFeatureSettings().getSetting(fs);
        if (setting.hasMincolour())
        {
          FeatureColourI gc = setting.hasMin() ? new FeatureColour(
                  new Color(setting.getMincolour()), new Color(
                          setting.getColour()), setting.getMin(),
                  setting.getMax()) : new FeatureColour(new Color(
                  setting.getMincolour()), new Color(setting.getColour()),
                  0, 1);
          if (setting.hasThreshold())
          {
            gc.setThreshold(setting.getThreshold());
            int threshstate = setting.getThreshstate();
            // -1 = None, 0 = Below, 1 = Above threshold
            if (threshstate == 0)
            {
              gc.setBelowThreshold(true);
            }
            else if (threshstate == 1)
            {
              gc.setAboveThreshold(true);
            }
          }
          gc.setAutoScaled(true); // default
          if (setting.hasAutoScale())
          {
            gc.setAutoScaled(setting.getAutoScale());
          }
          if (setting.hasColourByLabel())
          {
            gc.setColourByLabel(setting.getColourByLabel());
          }
          // and put in the feature colour table.
          featureColours.put(setting.getType(), gc);
        }
        else
        {
          featureColours.put(setting.getType(), new FeatureColour(
                  new Color(setting.getColour())));
        }
        renderOrder[fs] = setting.getType();
        if (setting.hasOrder())
        {
          featureOrder.put(setting.getType(), setting.getOrder());
        }
        else
        {
          featureOrder.put(setting.getType(), new Float(fs
                  / jms.getFeatureSettings().getSettingCount()));
        }
        if (setting.getDisplay())
        {
          fdi.setVisible(setting.getType());
        }
      }
      Map<String, Boolean> fgtable = new Hashtable<String, Boolean>();
      for (int gs = 0; gs < jms.getFeatureSettings().getGroupCount(); gs++)
      {
        Group grp = jms.getFeatureSettings().getGroup(gs);
        fgtable.put(grp.getName(), new Boolean(grp.getDisplay()));
      }
      // FeatureRendererSettings frs = new FeatureRendererSettings(renderOrder,
      // fgtable, featureColours, jms.getFeatureSettings().hasTransparency() ?
      // jms.getFeatureSettings().getTransparency() : 0.0, featureOrder);
      FeatureRendererSettings frs = new FeatureRendererSettings(
              renderOrder, fgtable, featureColours, 1.0f, featureOrder);
      af.alignPanel.getSeqPanel().seqCanvas.getFeatureRenderer()
              .transferSettings(frs);

    }

    if (view.getHiddenColumnsCount() > 0)
    {
      for (int c = 0; c < view.getHiddenColumnsCount(); c++)
      {
        af.viewport.hideColumns(view.getHiddenColumns(c).getStart(), view
                .getHiddenColumns(c).getEnd() // +1
                );
      }
    }
    if (view.getCalcIdParam() != null)
    {
      for (CalcIdParam calcIdParam : view.getCalcIdParam())
      {
        if (calcIdParam != null)
        {
          if (recoverCalcIdParam(calcIdParam, af.viewport))
          {
          }
          else
          {
            warn("Couldn't recover parameters for "
                    + calcIdParam.getCalcId());
          }
        }
      }
    }
    af.setMenusFromViewport(af.viewport);
    af.setTitle(view.getTitle());
    // TODO: we don't need to do this if the viewport is aready visible.
    /*
     * Add the AlignFrame to the desktop (it may be 'gathered' later), unless it
     * has a 'cdna/protein complement' view, in which case save it in order to
     * populate a SplitFrame once all views have been read in.
     */
    String complementaryViewId = view.getComplementId();
    if (complementaryViewId == null)
    {
      Desktop.addInternalFrame(af, view.getTitle(), view.getWidth(),
              view.getHeight());
      // recompute any autoannotation
      af.alignPanel.updateAnnotation(false, true);
      reorderAutoannotation(af, al, autoAlan);
      af.alignPanel.alignmentChanged();
    }
    else
    {
      splitFrameCandidates.put(view, af);
    }
    return af;
  }

  private ColourSchemeI constructAnnotationColour(
          AnnotationColours viewAnnColour, AlignFrame af, AlignmentI al,
          JalviewModelSequence jms, boolean checkGroupAnnColour)
  {
    boolean propagateAnnColour = false;
    ColourSchemeI cs = null;
    AlignmentI annAlignment = af != null ? af.viewport.getAlignment() : al;
    if (checkGroupAnnColour && al.getGroups() != null
            && al.getGroups().size() > 0)
    {
      // pre 2.8.1 behaviour
      // check to see if we should transfer annotation colours
      propagateAnnColour = true;
      for (jalview.datamodel.SequenceGroup sg : al.getGroups())
      {
        if (sg.cs instanceof AnnotationColourGradient)
        {
          propagateAnnColour = false;
        }
      }
    }
    // int find annotation
    if (annAlignment.getAlignmentAnnotation() != null)
    {
      for (int i = 0; i < annAlignment.getAlignmentAnnotation().length; i++)
      {
        if (annAlignment.getAlignmentAnnotation()[i].label
                .equals(viewAnnColour.getAnnotation()))
        {
          if (annAlignment.getAlignmentAnnotation()[i].getThreshold() == null)
          {
            annAlignment.getAlignmentAnnotation()[i]
                    .setThreshold(new jalview.datamodel.GraphLine(
                            viewAnnColour.getThreshold(), "Threshold",
                            java.awt.Color.black)

                    );
          }

          if (viewAnnColour.getColourScheme().equals("None"))
          {
            cs = new AnnotationColourGradient(
                    annAlignment.getAlignmentAnnotation()[i],
                    new java.awt.Color(viewAnnColour.getMinColour()),
                    new java.awt.Color(viewAnnColour.getMaxColour()),
                    viewAnnColour.getAboveThreshold());
          }
          else if (viewAnnColour.getColourScheme().startsWith("ucs"))
          {
            cs = new AnnotationColourGradient(
                    annAlignment.getAlignmentAnnotation()[i],
                    getUserColourScheme(jms,
                            viewAnnColour.getColourScheme()),
                    viewAnnColour.getAboveThreshold());
          }
          else
          {
            cs = new AnnotationColourGradient(
                    annAlignment.getAlignmentAnnotation()[i],
                    ColourSchemeProperty.getColour(al,
                            viewAnnColour.getColourScheme()),
                    viewAnnColour.getAboveThreshold());
          }
          if (viewAnnColour.hasPerSequence())
          {
            ((AnnotationColourGradient) cs).setSeqAssociated(viewAnnColour
                    .isPerSequence());
          }
          if (viewAnnColour.hasPredefinedColours())
          {
            ((AnnotationColourGradient) cs)
                    .setPredefinedColours(viewAnnColour
                            .isPredefinedColours());
          }
          if (propagateAnnColour && al.getGroups() != null)
          {
            // Also use these settings for all the groups
            for (int g = 0; g < al.getGroups().size(); g++)
            {
              jalview.datamodel.SequenceGroup sg = al.getGroups().get(g);

              if (sg.cs == null)
              {
                continue;
              }

              /*
               * if (viewAnnColour.getColourScheme().equals("None" )) { sg.cs =
               * new AnnotationColourGradient(
               * annAlignment.getAlignmentAnnotation()[i], new
               * java.awt.Color(viewAnnColour. getMinColour()), new
               * java.awt.Color(viewAnnColour. getMaxColour()),
               * viewAnnColour.getAboveThreshold()); } else
               */
              {
                sg.cs = new AnnotationColourGradient(
                        annAlignment.getAlignmentAnnotation()[i], sg.cs,
                        viewAnnColour.getAboveThreshold());
                if (cs instanceof AnnotationColourGradient)
                {
                  if (viewAnnColour.hasPerSequence())
                  {
                    ((AnnotationColourGradient) cs)
                            .setSeqAssociated(viewAnnColour.isPerSequence());
                  }
                  if (viewAnnColour.hasPredefinedColours())
                  {
                    ((AnnotationColourGradient) cs)
                            .setPredefinedColours(viewAnnColour
                                    .isPredefinedColours());
                  }
                }
              }

            }
          }

          break;
        }

      }
    }
    return cs;
  }

  private void reorderAutoannotation(AlignFrame af, AlignmentI al,
          List<JvAnnotRow> autoAlan)
  {
    // copy over visualization settings for autocalculated annotation in the
    // view
    if (al.getAlignmentAnnotation() != null)
    {
      /**
       * Kludge for magic autoannotation names (see JAL-811)
       */
      String[] magicNames = new String[] { "Consensus", "Quality",
          "Conservation" };
      JvAnnotRow nullAnnot = new JvAnnotRow(-1, null);
      Hashtable<String, JvAnnotRow> visan = new Hashtable<String, JvAnnotRow>();
      for (String nm : magicNames)
      {
        visan.put(nm, nullAnnot);
      }
      for (JvAnnotRow auan : autoAlan)
      {
        visan.put(auan.template.label
                + (auan.template.getCalcId() == null ? "" : "\t"
                        + auan.template.getCalcId()), auan);
      }
      int hSize = al.getAlignmentAnnotation().length;
      List<JvAnnotRow> reorder = new ArrayList<JvAnnotRow>();
      // work through any autoCalculated annotation already on the view
      // removing it if it should be placed in a different location on the
      // annotation panel.
      List<String> remains = new ArrayList<String>(visan.keySet());
      for (int h = 0; h < hSize; h++)
      {
        jalview.datamodel.AlignmentAnnotation jalan = al
                .getAlignmentAnnotation()[h];
        if (jalan.autoCalculated)
        {
          String k;
          JvAnnotRow valan = visan.get(k = jalan.label);
          if (jalan.getCalcId() != null)
          {
            valan = visan.get(k = jalan.label + "\t" + jalan.getCalcId());
          }

          if (valan != null)
          {
            // delete the auto calculated row from the alignment
            al.deleteAnnotation(jalan, false);
            remains.remove(k);
            hSize--;
            h--;
            if (valan != nullAnnot)
            {
              if (jalan != valan.template)
              {
                // newly created autoannotation row instance
                // so keep a reference to the visible annotation row
                // and copy over all relevant attributes
                if (valan.template.graphHeight >= 0)

                {
                  jalan.graphHeight = valan.template.graphHeight;
                }
                jalan.visible = valan.template.visible;
              }
              reorder.add(new JvAnnotRow(valan.order, jalan));
            }
          }
        }
      }
      // Add any (possibly stale) autocalculated rows that were not appended to
      // the view during construction
      for (String other : remains)
      {
        JvAnnotRow othera = visan.get(other);
        if (othera != nullAnnot && othera.template.getCalcId() != null
                && othera.template.getCalcId().length() > 0)
        {
          reorder.add(othera);
        }
      }
      // now put the automatic annotation in its correct place
      int s = 0, srt[] = new int[reorder.size()];
      JvAnnotRow[] rws = new JvAnnotRow[reorder.size()];
      for (JvAnnotRow jvar : reorder)
      {
        rws[s] = jvar;
        srt[s++] = jvar.order;
      }
      reorder.clear();
      jalview.util.QuickSort.sort(srt, rws);
      // and re-insert the annotation at its correct position
      for (JvAnnotRow jvar : rws)
      {
        al.addAnnotation(jvar.template, jvar.order);
      }
      af.alignPanel.adjustAnnotationHeight();
    }
  }

  Hashtable skipList = null;

  /**
   * TODO remove this method
   * 
   * @param view
   * @return AlignFrame bound to sequenceSetId from view, if one exists. private
   *         AlignFrame getSkippedFrame(Viewport view) { if (skipList==null) {
   *         throw new Error("Implementation Error. No skipList defined for this
   *         Jalview2XML instance."); } return (AlignFrame)
   *         skipList.get(view.getSequenceSetId()); }
   */

  /**
   * Check if the Jalview view contained in object should be skipped or not.
   * 
   * @param object
   * @return true if view's sequenceSetId is a key in skipList
   */
  private boolean skipViewport(JalviewModel object)
  {
    if (skipList == null)
    {
      return false;
    }
    String id;
    if (skipList.containsKey(id = object.getJalviewModelSequence()
            .getViewport()[0].getSequenceSetId()))
    {
      if (Cache.log != null && Cache.log.isDebugEnabled())
      {
        Cache.log.debug("Skipping seuqence set id " + id);
      }
      return true;
    }
    return false;
  }

  public void addToSkipList(AlignFrame af)
  {
    if (skipList == null)
    {
      skipList = new Hashtable();
    }
    skipList.put(af.getViewport().getSequenceSetId(), af);
  }

  public void clearSkipList()
  {
    if (skipList != null)
    {
      skipList.clear();
      skipList = null;
    }
  }

  private void recoverDatasetFor(SequenceSet vamsasSet, AlignmentI al,
          boolean ignoreUnrefed)
  {
    jalview.datamodel.AlignmentI ds = getDatasetFor(vamsasSet
            .getDatasetId());
    Vector dseqs = null;
    if (ds == null)
    {
      // create a list of new dataset sequences
      dseqs = new Vector();
    }
    for (int i = 0, iSize = vamsasSet.getSequenceCount(); i < iSize; i++)
    {
      Sequence vamsasSeq = vamsasSet.getSequence(i);
      ensureJalviewDatasetSequence(vamsasSeq, ds, dseqs, ignoreUnrefed, i);
    }
    // create a new dataset
    if (ds == null)
    {
      SequenceI[] dsseqs = new SequenceI[dseqs.size()];
      dseqs.copyInto(dsseqs);
      ds = new jalview.datamodel.Alignment(dsseqs);
      debug("Created new dataset " + vamsasSet.getDatasetId()
              + " for alignment " + System.identityHashCode(al));
      addDatasetRef(vamsasSet.getDatasetId(), ds);
    }
    // set the dataset for the newly imported alignment.
    if (al.getDataset() == null && !ignoreUnrefed)
    {
      al.setDataset(ds);
    }
  }

  /**
   * 
   * @param vamsasSeq
   *          sequence definition to create/merge dataset sequence for
   * @param ds
   *          dataset alignment
   * @param dseqs
   *          vector to add new dataset sequence to
   * @param ignoreUnrefed
   *          - when true, don't create new sequences from vamsasSeq if it's id
   *          doesn't already have an asssociated Jalview sequence.
   * @param vseqpos
   *          - used to reorder the sequence in the alignment according to the
   *          vamsasSeq array ordering, to preserve ordering of dataset
   */
  private void ensureJalviewDatasetSequence(Sequence vamsasSeq,
          AlignmentI ds, Vector dseqs, boolean ignoreUnrefed, int vseqpos)
  {
    // JBP TODO: Check this is called for AlCodonFrames to support recovery of
    // xRef Codon Maps
    SequenceI sq = seqRefIds.get(vamsasSeq.getId());
    boolean reorder = false;
    SequenceI dsq = null;
    if (sq != null && sq.getDatasetSequence() != null)
    {
      dsq = sq.getDatasetSequence();
    }
    else
    {
      reorder = true;
    }
    if (sq == null && ignoreUnrefed)
    {
      return;
    }
    String sqid = vamsasSeq.getDsseqid();
    if (dsq == null)
    {
      // need to create or add a new dataset sequence reference to this sequence
      if (sqid != null)
      {
        dsq = seqRefIds.get(sqid);
      }
      // check again
      if (dsq == null)
      {
        // make a new dataset sequence
        dsq = sq.createDatasetSequence();
        if (sqid == null)
        {
          // make up a new dataset reference for this sequence
          sqid = seqHash(dsq);
        }
        dsq.setVamsasId(uniqueSetSuffix + sqid);
        seqRefIds.put(sqid, dsq);
        if (ds == null)
        {
          if (dseqs != null)
          {
            dseqs.addElement(dsq);
          }
        }
        else
        {
          ds.addSequence(dsq);
        }
      }
      else
      {
        if (sq != dsq)
        { // make this dataset sequence sq's dataset sequence
          sq.setDatasetSequence(dsq);
          // and update the current dataset alignment
          if (ds == null)
          {
            if (dseqs != null)
            {
              if (!dseqs.contains(dsq))
              {
                dseqs.add(dsq);
              }
            }
            else
            {
              if (ds.findIndex(dsq) < 0)
              {
                ds.addSequence(dsq);
              }
            }
          }
        }
      }
    }
    // TODO: refactor this as a merge dataset sequence function
    // now check that sq (the dataset sequence) sequence really is the union of
    // all references to it
    // boolean pre = sq.getStart() < dsq.getStart();
    // boolean post = sq.getEnd() > dsq.getEnd();
    // if (pre || post)
    if (sq != dsq)
    {
      // StringBuffer sb = new StringBuffer();
      String newres = jalview.analysis.AlignSeq.extractGaps(
              jalview.util.Comparison.GapChars, sq.getSequenceAsString());
      if (!newres.equalsIgnoreCase(dsq.getSequenceAsString())
              && newres.length() > dsq.getLength())
      {
        // Update with the longer sequence.
        synchronized (dsq)
        {
          /*
           * if (pre) { sb.insert(0, newres .substring(0, dsq.getStart() -
           * sq.getStart())); dsq.setStart(sq.getStart()); } if (post) {
           * sb.append(newres.substring(newres.length() - sq.getEnd() -
           * dsq.getEnd())); dsq.setEnd(sq.getEnd()); }
           */
          dsq.setSequence(newres);
        }
        // TODO: merges will never happen if we 'know' we have the real dataset
        // sequence - this should be detected when id==dssid
        System.err
                .println("DEBUG Notice:  Merged dataset sequence (if you see this often, post at http://issues.jalview.org/browse/JAL-1474)"); // ("
        // + (pre ? "prepended" : "") + " "
        // + (post ? "appended" : ""));
      }
    }
    else
    {
      // sequence refs are identical. We may need to update the existing dataset
      // alignment with this one, though.
      if (ds != null && dseqs == null)
      {
        int opos = ds.findIndex(dsq);
        SequenceI tseq = null;
        if (opos != -1 && vseqpos != opos)
        {
          // remove from old position
          ds.deleteSequence(dsq);
        }
        if (vseqpos < ds.getHeight())
        {
          if (vseqpos != opos)
          {
            // save sequence at destination position
            tseq = ds.getSequenceAt(vseqpos);
            ds.replaceSequenceAt(vseqpos, dsq);
            ds.addSequence(tseq);
          }
        }
        else
        {
          ds.addSequence(dsq);
        }
      }
    }
  }

  /*
   * TODO use AlignmentI here and in related methods - needs
   * AlignmentI.getDataset() changed to return AlignmentI instead of Alignment
   */
  Hashtable<String, AlignmentI> datasetIds = null;

  IdentityHashMap<AlignmentI, String> dataset2Ids = null;

  private AlignmentI getDatasetFor(String datasetId)
  {
    if (datasetIds == null)
    {
      datasetIds = new Hashtable<String, AlignmentI>();
      return null;
    }
    if (datasetIds.containsKey(datasetId))
    {
      return datasetIds.get(datasetId);
    }
    return null;
  }

  private void addDatasetRef(String datasetId, AlignmentI dataset)
  {
    if (datasetIds == null)
    {
      datasetIds = new Hashtable<String, AlignmentI>();
    }
    datasetIds.put(datasetId, dataset);
  }

  /**
   * make a new dataset ID for this jalview dataset alignment
   * 
   * @param dataset
   * @return
   */
  private String getDatasetIdRef(AlignmentI dataset)
  {
    if (dataset.getDataset() != null)
    {
      warn("Serious issue!  Dataset Object passed to getDatasetIdRef is not a Jalview DATASET alignment...");
    }
    String datasetId = makeHashCode(dataset, null);
    if (datasetId == null)
    {
      // make a new datasetId and record it
      if (dataset2Ids == null)
      {
        dataset2Ids = new IdentityHashMap<AlignmentI, String>();
      }
      else
      {
        datasetId = dataset2Ids.get(dataset);
      }
      if (datasetId == null)
      {
        datasetId = "ds" + dataset2Ids.size() + 1;
        dataset2Ids.put(dataset, datasetId);
      }
    }
    return datasetId;
  }

  private void addDBRefs(SequenceI datasetSequence, Sequence sequence)
  {
    for (int d = 0; d < sequence.getDBRefCount(); d++)
    {
      DBRef dr = sequence.getDBRef(d);
      jalview.datamodel.DBRefEntry entry = new jalview.datamodel.DBRefEntry(
              sequence.getDBRef(d).getSource(), sequence.getDBRef(d)
                      .getVersion(), sequence.getDBRef(d).getAccessionId());
      if (dr.getMapping() != null)
      {
        entry.setMap(addMapping(dr.getMapping()));
      }
      datasetSequence.addDBRef(entry);
    }
  }

  private jalview.datamodel.Mapping addMapping(Mapping m)
  {
    SequenceI dsto = null;
    // Mapping m = dr.getMapping();
    int fr[] = new int[m.getMapListFromCount() * 2];
    Enumeration f = m.enumerateMapListFrom();
    for (int _i = 0; f.hasMoreElements(); _i += 2)
    {
      MapListFrom mf = (MapListFrom) f.nextElement();
      fr[_i] = mf.getStart();
      fr[_i + 1] = mf.getEnd();
    }
    int fto[] = new int[m.getMapListToCount() * 2];
    f = m.enumerateMapListTo();
    for (int _i = 0; f.hasMoreElements(); _i += 2)
    {
      MapListTo mf = (MapListTo) f.nextElement();
      fto[_i] = mf.getStart();
      fto[_i + 1] = mf.getEnd();
    }
    jalview.datamodel.Mapping jmap = new jalview.datamodel.Mapping(dsto,
            fr, fto, (int) m.getMapFromUnit(), (int) m.getMapToUnit());
    if (m.getMappingChoice() != null)
    {
      MappingChoice mc = m.getMappingChoice();
      if (mc.getDseqFor() != null)
      {
        String dsfor = "" + mc.getDseqFor();
        if (seqRefIds.containsKey(dsfor))
        {
          /**
           * recover from hash
           */
          jmap.setTo(seqRefIds.get(dsfor));
        }
        else
        {
          frefedSequence.add(newMappingRef(dsfor, jmap));
        }
      }
      else
      {
        /**
         * local sequence definition
         */
        Sequence ms = mc.getSequence();
        SequenceI djs = null;
        String sqid = ms.getDsseqid();
        if (sqid != null && sqid.length() > 0)
        {
          /*
           * recover dataset sequence
           */
          djs = seqRefIds.get(sqid);
        }
        else
        {
          System.err
                  .println("Warning - making up dataset sequence id for DbRef sequence map reference");
          sqid = ((Object) ms).toString(); // make up a new hascode for
          // undefined dataset sequence hash
          // (unlikely to happen)
        }

        if (djs == null)
        {
          /**
           * make a new dataset sequence and add it to refIds hash
           */
          djs = new jalview.datamodel.Sequence(ms.getName(),
                  ms.getSequence());
          djs.setStart(jmap.getMap().getToLowest());
          djs.setEnd(jmap.getMap().getToHighest());
          djs.setVamsasId(uniqueSetSuffix + sqid);
          jmap.setTo(djs);
          incompleteSeqs.put(sqid, djs);
          seqRefIds.put(sqid, djs);

        }
        jalview.bin.Cache.log.debug("about to recurse on addDBRefs.");
        addDBRefs(djs, ms);

      }
    }
    return (jmap);

  }

  public jalview.gui.AlignmentPanel copyAlignPanel(AlignmentPanel ap,
          boolean keepSeqRefs)
  {
    initSeqRefs();
    JalviewModel jm = saveState(ap, null, null, null);

    if (!keepSeqRefs)
    {
      clearSeqRefs();
      jm.getJalviewModelSequence().getViewport(0).setSequenceSetId(null);
    }
    else
    {
      uniqueSetSuffix = "";
      jm.getJalviewModelSequence().getViewport(0).setId(null); // we don't
      // overwrite the
      // view we just
      // copied
    }
    if (this.frefedSequence == null)
    {
      frefedSequence = new Vector();
    }

    viewportsAdded.clear();

    AlignFrame af = loadFromObject(jm, null, false, null);
    af.alignPanels.clear();
    af.closeMenuItem_actionPerformed(true);

    /*
     * if(ap.av.getAlignment().getAlignmentAnnotation()!=null) { for(int i=0;
     * i<ap.av.getAlignment().getAlignmentAnnotation().length; i++) {
     * if(!ap.av.getAlignment().getAlignmentAnnotation()[i].autoCalculated) {
     * af.alignPanel.av.getAlignment().getAlignmentAnnotation()[i] =
     * ap.av.getAlignment().getAlignmentAnnotation()[i]; } } }
     */

    return af.alignPanel;
  }

  /**
   * flag indicating if hashtables should be cleared on finalization TODO this
   * flag may not be necessary
   */
  private final boolean _cleartables = true;

  private Hashtable jvids2vobj;

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable
  {
    // really make sure we have no buried refs left.
    if (_cleartables)
    {
      clearSeqRefs();
    }
    this.seqRefIds = null;
    this.seqsToIds = null;
    super.finalize();
  }

  private void warn(String msg)
  {
    warn(msg, null);
  }

  private void warn(String msg, Exception e)
  {
    if (Cache.log != null)
    {
      if (e != null)
      {
        Cache.log.warn(msg, e);
      }
      else
      {
        Cache.log.warn(msg);
      }
    }
    else
    {
      System.err.println("Warning: " + msg);
      if (e != null)
      {
        e.printStackTrace();
      }
    }
  }

  private void debug(String string)
  {
    debug(string, null);
  }

  private void debug(String msg, Exception e)
  {
    if (Cache.log != null)
    {
      if (e != null)
      {
        Cache.log.debug(msg, e);
      }
      else
      {
        Cache.log.debug(msg);
      }
    }
    else
    {
      System.err.println("Warning: " + msg);
      if (e != null)
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * set the object to ID mapping tables used to write/recover objects and XML
   * ID strings for the jalview project. If external tables are provided then
   * finalize and clearSeqRefs will not clear the tables when the Jalview2XML
   * object goes out of scope. - also populates the datasetIds hashtable with
   * alignment objects containing dataset sequences
   * 
   * @param vobj2jv
   *          Map from ID strings to jalview datamodel
   * @param jv2vobj
   *          Map from jalview datamodel to ID strings
   * 
   * 
   */
  public void setObjectMappingTables(Hashtable vobj2jv,
          IdentityHashMap jv2vobj)
  {
    this.jv2vobj = jv2vobj;
    this.vobj2jv = vobj2jv;
    Iterator ds = jv2vobj.keySet().iterator();
    String id;
    while (ds.hasNext())
    {
      Object jvobj = ds.next();
      id = jv2vobj.get(jvobj).toString();
      if (jvobj instanceof jalview.datamodel.Alignment)
      {
        if (((jalview.datamodel.Alignment) jvobj).getDataset() == null)
        {
          addDatasetRef(id, (jalview.datamodel.Alignment) jvobj);
        }
      }
      else if (jvobj instanceof jalview.datamodel.Sequence)
      {
        // register sequence object so the XML parser can recover it.
        if (seqRefIds == null)
        {
          seqRefIds = new HashMap<String, SequenceI>();
        }
        if (seqsToIds == null)
        {
          seqsToIds = new IdentityHashMap<SequenceI, String>();
        }
        seqRefIds.put(jv2vobj.get(jvobj).toString(), (SequenceI) jvobj);
        seqsToIds.put((SequenceI) jvobj, id);
      }
      else if (jvobj instanceof jalview.datamodel.AlignmentAnnotation)
      {
        String anid;
        AlignmentAnnotation jvann = (AlignmentAnnotation) jvobj;
        annotationIds.put(anid = jv2vobj.get(jvobj).toString(), jvann);
        if (jvann.annotationId == null)
        {
          jvann.annotationId = anid;
        }
        if (!jvann.annotationId.equals(anid))
        {
          // TODO verify that this is the correct behaviour
          this.warn("Overriding Annotation ID for " + anid
                  + " from different id : " + jvann.annotationId);
          jvann.annotationId = anid;
        }
      }
      else if (jvobj instanceof String)
      {
        if (jvids2vobj == null)
        {
          jvids2vobj = new Hashtable();
          jvids2vobj.put(jvobj, jv2vobj.get(jvobj).toString());
        }
      }
      else
      {
        Cache.log.debug("Ignoring " + jvobj.getClass() + " (ID = " + id);
      }
    }
  }

  /**
   * set the uniqueSetSuffix used to prefix/suffix object IDs for jalview
   * objects created from the project archive. If string is null (default for
   * construction) then suffix will be set automatically.
   * 
   * @param string
   */
  public void setUniqueSetSuffix(String string)
  {
    uniqueSetSuffix = string;

  }

  /**
   * uses skipList2 as the skipList for skipping views on sequence sets
   * associated with keys in the skipList
   * 
   * @param skipList2
   */
  public void setSkipList(Hashtable skipList2)
  {
    skipList = skipList2;
  }

  /**
   * Reads the jar entry of given name and returns its contents, or null if the
   * entry is not found.
   * 
   * @param jprovider
   * @param jarEntryName
   * @return
   */
  protected String readJarEntry(jarInputStreamProvider jprovider,
          String jarEntryName)
  {
    String result = null;
    BufferedReader in = null;

    try
    {
      /*
       * Reopen the jar input stream and traverse its entries to find a matching
       * name
       */
      JarInputStream jin = jprovider.getJarInputStream();
      JarEntry entry = null;
      do
      {
        entry = jin.getNextJarEntry();
      } while (entry != null && !entry.getName().equals(jarEntryName));

      if (entry != null)
      {
        StringBuilder out = new StringBuilder(256);
        in = new BufferedReader(new InputStreamReader(jin, UTF_8));
        String data;

        while ((data = in.readLine()) != null)
        {
          out.append(data);
        }
        result = out.toString();
      }
      else
      {
        warn("Couldn't find entry in Jalview Jar for " + jarEntryName);
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
    } finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }

    return result;
  }

  /**
   * Returns an incrementing counter (0, 1, 2...)
   * 
   * @return
   */
  private synchronized int nextCounter()
  {
    return counter++;
  }
}
