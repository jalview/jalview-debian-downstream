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

package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.api.ComplexAlignFile;
import jalview.api.FeatureRenderer;
import jalview.api.FeaturesDisplayedI;
import jalview.bin.BuildDetails;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.Annotation;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.HiddenSequences;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.json.binding.biojson.v1.AlignmentAnnotationPojo;
import jalview.json.binding.biojson.v1.AlignmentPojo;
import jalview.json.binding.biojson.v1.AnnotationPojo;
import jalview.json.binding.biojson.v1.JalviewBioJsColorSchemeMapper;
import jalview.json.binding.biojson.v1.SequenceFeaturesPojo;
import jalview.json.binding.biojson.v1.SequenceGrpPojo;
import jalview.json.binding.biojson.v1.SequencePojo;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.viewmodel.seqfeatures.FeaturesDisplayed;

import java.awt.Color;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONFile extends AlignFile implements ComplexAlignFile
{
  private ColourSchemeI colourScheme;

  private static String version = new BuildDetails().getVersion();

  private String webstartUrl = "http://www.jalview.org/services/launchApp";

  private String application = "Jalview";

  public static final String FILE_EXT = "json";

  public static final String FILE_DESC = "JSON";

  private String globalColorScheme;

  private boolean showSeqFeatures;

  private Hashtable<String, Sequence> seqMap;

  private FeaturesDisplayedI displayedFeatures;

  private FeatureRenderer fr;

  private List<int[]> hiddenColumns;

  private ColumnSelection columnSelection;

  private List<String> hiddenSeqRefs;

  private ArrayList<SequenceI> hiddenSequences;

  public JSONFile()
  {
    super();
  }

  public JSONFile(FileParse source) throws IOException
  {
    super(source);
  }

  public JSONFile(String inFile, String type) throws IOException
  {
    super(inFile, type);
  }

  @Override
  public void parse() throws IOException
  {
    parse(getReader());

  }

  @Override
  public String print()
  {
    String jsonOutput = null;
    try
    {
      AlignmentPojo jsonAlignmentPojo = new AlignmentPojo();
      AlignExportSettingI exportSettings = getExportSettings();

      // if no export settings were supplied use the following with all values
      // defaulting to true
      if (exportSettings == null)
      {
        exportSettings = new AlignExportSettingI()
        {
          @Override
          public boolean isExportHiddenSequences()
          {
            return true;
          }

          @Override
          public boolean isExportHiddenColumns()
          {
            return true;
          }

          @Override
          public boolean isExportGroups()
          {
            return true;
          }

          @Override
          public boolean isExportFeatures()
          {
            return true;
          }

          @Override
          public boolean isExportAnnotations()
          {
            return true;
          }

          @Override
          public boolean isCancelled()
          {
            return false;
          }
        };
      }

      int count = 0;
      for (SequenceI seq : seqs)
      {
        StringBuilder name = new StringBuilder();
        name.append(seq.getName()).append("/").append(seq.getStart())
                .append("-").append(seq.getEnd());
        SequencePojo jsonSeqPojo = new SequencePojo();
        jsonSeqPojo.setId(String.valueOf(seq.hashCode()));
        jsonSeqPojo.setOrder(++count);
        jsonSeqPojo.setEnd(seq.getEnd());
        jsonSeqPojo.setStart(seq.getStart());
        jsonSeqPojo.setName(name.toString());
        jsonSeqPojo.setSeq(seq.getSequenceAsString());
        jsonAlignmentPojo.getSeqs().add(jsonSeqPojo);
      }
      jsonAlignmentPojo.setGlobalColorScheme(globalColorScheme);
      jsonAlignmentPojo.getAppSettings().put("application", application);
      jsonAlignmentPojo.getAppSettings().put("version", version);
      jsonAlignmentPojo.getAppSettings().put("webStartUrl", webstartUrl);
      jsonAlignmentPojo.getAppSettings().put("showSeqFeatures",
              String.valueOf(showSeqFeatures));

      String[] hiddenSections = getHiddenSections();
      if (hiddenSections != null)
      {
        if (hiddenSections[0] != null
                && exportSettings.isExportHiddenColumns())
        {
          jsonAlignmentPojo.getAppSettings().put("hiddenCols",
                  String.valueOf(hiddenSections[0]));
        }
        if (hiddenSections[1] != null
                && exportSettings.isExportHiddenSequences())
        {
          jsonAlignmentPojo.getAppSettings().put("hiddenSeqs",
                  String.valueOf(hiddenSections[1]));
        }
      }

      if (exportSettings.isExportAnnotations())
      {
        jsonAlignmentPojo
                .setAlignAnnotation(annotationToJsonPojo(annotations));
      }

      if (exportSettings.isExportFeatures())
      {
        jsonAlignmentPojo
                .setSeqFeatures(sequenceFeatureToJsonPojo(seqs, fr));
      }

      if (exportSettings.isExportGroups() && seqGroups != null
              && seqGroups.size() > 0)
      {
        for (SequenceGroup seqGrp : seqGroups)
        {
          SequenceGrpPojo seqGrpPojo = new SequenceGrpPojo();
          seqGrpPojo.setGroupName(seqGrp.getName());
          seqGrpPojo.setColourScheme(ColourSchemeProperty
                  .getColourName(seqGrp.cs));
          seqGrpPojo.setColourText(seqGrp.getColourText());
          seqGrpPojo.setDescription(seqGrp.getDescription());
          seqGrpPojo.setDisplayBoxes(seqGrp.getDisplayBoxes());
          seqGrpPojo.setDisplayText(seqGrp.getDisplayText());
          seqGrpPojo.setEndRes(seqGrp.getEndRes());
          seqGrpPojo.setStartRes(seqGrp.getStartRes());
          seqGrpPojo.setShowNonconserved(seqGrp.getShowNonconserved());
          for (SequenceI seq : seqGrp.getSequences())
          {
            seqGrpPojo.getSequenceRefs()
                    .add(String.valueOf(seq.hashCode()));
          }
          jsonAlignmentPojo.getSeqGroups().add(seqGrpPojo);
        }
      }
      org.json.JSONObject generatedJSon = new org.json.JSONObject(
              jsonAlignmentPojo);
      jsonOutput = generatedJSon.toString();
      return jsonOutput.replaceAll("xstart", "xStart").replaceAll("xend",
              "xEnd");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return jsonOutput;
  }

  public String[] getHiddenSections()
  {
    String[] hiddenSections = new String[2];
    if (getViewport() == null)
    {
      return null;
    }

    // hidden column business
    if (getViewport().hasHiddenColumns())
    {
      List<int[]> hiddenCols = getViewport().getColumnSelection()
              .getHiddenColumns();
      StringBuilder hiddenColsBuilder = new StringBuilder();
      for (int[] range : hiddenCols)
      {
        hiddenColsBuilder.append(";").append(range[0]).append("-")
                .append(range[1]);
      }

      hiddenColsBuilder.deleteCharAt(0);
      hiddenSections[0] = hiddenColsBuilder.toString();
    }

    // hidden rows/seqs business
    HiddenSequences hiddenSeqsObj = getViewport().getAlignment()
            .getHiddenSequences();
    if (hiddenSeqsObj == null || hiddenSeqsObj.hiddenSequences == null)
    {
      return hiddenSections;
    }

    SequenceI[] hiddenSeqs = hiddenSeqsObj.hiddenSequences;
    StringBuilder hiddenSeqsBuilder = new StringBuilder();
    for (SequenceI hiddenSeq : hiddenSeqs)
    {
      if (hiddenSeq != null)
      {
        hiddenSeqsBuilder.append(";").append(hiddenSeq.hashCode());
      }
    }
    if (hiddenSeqsBuilder.length() > 0)
    {
      hiddenSeqsBuilder.deleteCharAt(0);
    }
    hiddenSections[1] = hiddenSeqsBuilder.toString();

    return hiddenSections;
  }

  public List<SequenceFeaturesPojo> sequenceFeatureToJsonPojo(
          List<SequenceI> seqs, FeatureRenderer fr)
  {
    displayedFeatures = (fr == null) ? null : fr.getFeaturesDisplayed();
    List<SequenceFeaturesPojo> sequenceFeaturesPojo = new ArrayList<SequenceFeaturesPojo>();
    for (SequenceI seq : seqs)
    {
      SequenceI dataSetSequence = seq.getDatasetSequence();
      SequenceFeature[] seqFeatures = (dataSetSequence == null) ? null
              : seq.getDatasetSequence().getSequenceFeatures();

      seqFeatures = (seqFeatures == null) ? seq.getSequenceFeatures()
              : seqFeatures;
      if (seqFeatures == null)
      {
        continue;
      }

      for (SequenceFeature sf : seqFeatures)
      {
        if (displayedFeatures != null
                && displayedFeatures.isVisible(sf.getType()))
        {
          SequenceFeaturesPojo jsonFeature = new SequenceFeaturesPojo(
                  String.valueOf(seq.hashCode()));

          String featureColour = (fr == null) ? null : jalview.util.Format
                  .getHexString(fr.findFeatureColour(Color.white, seq,
                          seq.findIndex(sf.getBegin())));
          jsonFeature.setXstart(seq.findIndex(sf.getBegin()) - 1);
          jsonFeature.setXend(seq.findIndex(sf.getEnd()));
          jsonFeature.setType(sf.getType());
          jsonFeature.setDescription(sf.getDescription());
          jsonFeature.setLinks(sf.links);
          jsonFeature.setOtherDetails(sf.otherDetails);
          jsonFeature.setScore(sf.getScore());
          jsonFeature.setFillColor(featureColour);
          jsonFeature.setFeatureGroup(sf.getFeatureGroup());
          sequenceFeaturesPojo.add(jsonFeature);
        }
      }
    }
    return sequenceFeaturesPojo;
  }

  public static List<AlignmentAnnotationPojo> annotationToJsonPojo(
          Vector<AlignmentAnnotation> annotations)
  {
    List<AlignmentAnnotationPojo> jsonAnnotations = new ArrayList<AlignmentAnnotationPojo>();
    if (annotations == null)
    {
      return jsonAnnotations;
    }
    for (AlignmentAnnotation annot : annotations)
    {
      AlignmentAnnotationPojo alignAnnotPojo = new AlignmentAnnotationPojo();
      alignAnnotPojo.setDescription(annot.description);
      alignAnnotPojo.setLabel(annot.label);
      for (Annotation annotation : annot.annotations)
      {
        AnnotationPojo annotationPojo = new AnnotationPojo();
        if (annotation != null)
        {
          annotationPojo.setDescription(annotation.description);
          annotationPojo.setValue(annotation.value);
          annotationPojo
                  .setSecondaryStructure(annotation.secondaryStructure);
          annotationPojo.setDisplayCharacter(annotation.displayCharacter);
          alignAnnotPojo.getAnnotations().add(annotationPojo);
        }
        else
        {
          alignAnnotPojo.getAnnotations().add(annotationPojo);
        }
      }
      jsonAnnotations.add(alignAnnotPojo);
    }
    return jsonAnnotations;
  }

  @SuppressWarnings("unchecked")
  public JSONFile parse(Reader jsonAlignmentString)
  {
    try
    {
      JSONParser jsonParser = new JSONParser();
      JSONObject alignmentJsonObj = (JSONObject) jsonParser
              .parse(jsonAlignmentString);
      JSONArray seqJsonArray = (JSONArray) alignmentJsonObj.get("seqs");
      JSONArray alAnnotJsonArray = (JSONArray) alignmentJsonObj
              .get("alignAnnotation");
      JSONArray jsonSeqArray = (JSONArray) alignmentJsonObj
              .get("seqFeatures");
      JSONArray seqGrpJsonArray = (JSONArray) alignmentJsonObj
              .get("seqGroups");
      JSONObject jvSettingsJsonObj = (JSONObject) alignmentJsonObj
              .get("appSettings");

      if (jvSettingsJsonObj != null)
      {
        String jsColourScheme = (String) jvSettingsJsonObj
                .get("globalColorScheme");
        Boolean showFeatures = Boolean.valueOf(jvSettingsJsonObj.get(
                "showSeqFeatures").toString());
        setColourScheme(getJalviewColorScheme(jsColourScheme));
        setShowSeqFeatures(showFeatures);
        parseHiddenSeqRefsAsList(jvSettingsJsonObj);
        parseHiddenCols(jvSettingsJsonObj);
      }

      hiddenSequences = new ArrayList<SequenceI>();
      seqMap = new Hashtable<String, Sequence>();
      for (Iterator<JSONObject> sequenceIter = seqJsonArray.iterator(); sequenceIter
              .hasNext();)
      {
        JSONObject sequence = sequenceIter.next();
        String sequcenceString = sequence.get("seq").toString();
        String sequenceName = sequence.get("name").toString();
        String seqUniqueId = sequence.get("id").toString();
        int start = Integer.valueOf(sequence.get("start").toString());
        int end = Integer.valueOf(sequence.get("end").toString());
        Sequence seq = new Sequence(sequenceName, sequcenceString, start,
                end);
        if (hiddenSeqRefs != null && hiddenSeqRefs.contains(seqUniqueId))
        {
          hiddenSequences.add(seq);
        }
        seqs.add(seq);
        seqMap.put(seqUniqueId, seq);
      }
      parseFeatures(jsonSeqArray);

      for (Iterator<JSONObject> seqGrpIter = seqGrpJsonArray.iterator(); seqGrpIter
              .hasNext();)
      {
        JSONObject seqGrpObj = seqGrpIter.next();
        String grpName = seqGrpObj.get("groupName").toString();
        String colourScheme = seqGrpObj.get("colourScheme").toString();
        String description = (seqGrpObj.get("description") == null) ? null
                : seqGrpObj.get("description").toString();
        boolean displayBoxes = Boolean.valueOf(seqGrpObj
                .get("displayBoxes").toString());
        boolean displayText = Boolean.valueOf(seqGrpObj.get("displayText")
                .toString());
        boolean colourText = Boolean.valueOf(seqGrpObj.get("colourText")
                .toString());
        boolean showNonconserved = Boolean.valueOf(seqGrpObj.get(
                "showNonconserved").toString());
        int startRes = Integer
                .valueOf(seqGrpObj.get("startRes").toString());
        int endRes = Integer.valueOf(seqGrpObj.get("endRes").toString());
        JSONArray sequenceRefs = (JSONArray) seqGrpObj.get("sequenceRefs");

        ArrayList<SequenceI> grpSeqs = new ArrayList<SequenceI>();
        if (sequenceRefs.size() > 0)
        {
          Iterator<String> seqHashIter = sequenceRefs.iterator();
          while (seqHashIter.hasNext())
          {
            String seqHash = seqHashIter.next();
            Sequence sequence = seqMap.get(seqHash);
            if (sequence != null)
            {
              grpSeqs.add(sequence);
            }
          }
        }
        ColourSchemeI grpColourScheme = getJalviewColorScheme(colourScheme);
        SequenceGroup seqGrp = new SequenceGroup(grpSeqs, grpName,
                grpColourScheme, displayBoxes, displayText, colourText,
                startRes, endRes);
        seqGrp.setShowNonconserved(showNonconserved);
        seqGrp.setDescription(description);
        this.seqGroups.add(seqGrp);

      }

      for (Iterator<JSONObject> alAnnotIter = alAnnotJsonArray.iterator(); alAnnotIter
              .hasNext();)
      {
        JSONObject alAnnot = alAnnotIter.next();
        JSONArray annotJsonArray = (JSONArray) alAnnot.get("annotations");
        Annotation[] annotations = new Annotation[annotJsonArray.size()];
        int count = 0;
        for (Iterator<JSONObject> annotIter = annotJsonArray.iterator(); annotIter
                .hasNext();)
        {
          JSONObject annot = annotIter.next();
          if (annot == null)
          {
            annotations[count] = null;
          }
          else
          {
            float val = annot.get("value") == null ? null : Float
                    .valueOf(annot.get("value").toString());
            String desc = annot.get("description") == null ? null : annot
                    .get("description").toString();

            char ss = annot.get("secondaryStructure") == null ? ' ' : annot
                    .get("secondaryStructure").toString().charAt(0);
            String displayChar = annot.get("displayCharacter") == null ? ""
                    : annot.get("displayCharacter").toString();

            annotations[count] = new Annotation(displayChar, desc, ss, val);
          }
          ++count;
        }

        AlignmentAnnotation alignAnnot = new AlignmentAnnotation(alAnnot
                .get("label").toString(), alAnnot.get("description")
                .toString(), annotations);
        this.annotations.add(alignAnnot);
      }

    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return this;
  }

  public void parseHiddenSeqRefsAsList(JSONObject jvSettingsJson)
  {
    hiddenSeqRefs = new ArrayList<String>();
    String hiddenSeqs = (String) jvSettingsJson.get("hiddenSeqs");
    if (hiddenSeqs != null && !hiddenSeqs.isEmpty())
    {
      String[] seqRefs = hiddenSeqs.split(";");
      for (String seqRef : seqRefs)
      {
        hiddenSeqRefs.add(seqRef);
      }
    }
  }

  public void parseHiddenCols(JSONObject jvSettingsJson)
  {
    String hiddenCols = (String) jvSettingsJson.get("hiddenCols");
    if (hiddenCols != null && !hiddenCols.isEmpty())
    {
      columnSelection = new ColumnSelection();
      String[] rangeStrings = hiddenCols.split(";");
      for (String rangeString : rangeStrings)
      {
        String[] range = rangeString.split("-");
        columnSelection.hideColumns(Integer.valueOf(range[0]),
                Integer.valueOf(range[1]));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void parseFeatures(JSONArray jsonSeqFeatures)
  {
    if (jsonSeqFeatures != null)
    {
      displayedFeatures = new FeaturesDisplayed();
      for (Iterator<JSONObject> seqFeatureItr = jsonSeqFeatures.iterator(); seqFeatureItr
              .hasNext();)
      {
        JSONObject jsonFeature = seqFeatureItr.next();
        Long begin = (Long) jsonFeature.get("xStart");
        Long end = (Long) jsonFeature.get("xEnd");
        String type = (String) jsonFeature.get("type");
        String featureGrp = (String) jsonFeature.get("featureGroup");
        String descripiton = (String) jsonFeature.get("description");
        String seqRef = (String) jsonFeature.get("sequenceRef");
        Float score = Float.valueOf(jsonFeature.get("score").toString());

        Sequence seq = seqMap.get(seqRef);
        SequenceFeature sequenceFeature = new SequenceFeature();
        JSONArray linksJsonArray = (JSONArray) jsonFeature.get("links");
        if (linksJsonArray != null && linksJsonArray.size() > 0)
        {
          Iterator<String> linkList = linksJsonArray.iterator();
          while (linkList.hasNext())
          {
            String link = linkList.next();
            sequenceFeature.addLink(link);
          }
        }
        sequenceFeature.setFeatureGroup(featureGrp);
        sequenceFeature.setScore(score);
        sequenceFeature.setDescription(descripiton);
        sequenceFeature.setType(type);
        sequenceFeature.setBegin(seq.findPosition(begin.intValue()));
        sequenceFeature.setEnd(seq.findPosition(end.intValue()) - 1);
        seq.addSequenceFeature(sequenceFeature);
        displayedFeatures.setVisible(type);
      }
    }
  }

  public static ColourSchemeI getJalviewColorScheme(
          String bioJsColourSchemeName)
  {
    ColourSchemeI jalviewColor = null;
    for (JalviewBioJsColorSchemeMapper cs : JalviewBioJsColorSchemeMapper
            .values())
    {
      if (cs.getBioJsName().equalsIgnoreCase(bioJsColourSchemeName))
      {
        jalviewColor = cs.getJvColourScheme();
        break;
      }
    }
    return jalviewColor;
  }

  public String getGlobalColorScheme()
  {
    return globalColorScheme;
  }

  public void setGlobalColorScheme(String globalColorScheme)
  {
    this.globalColorScheme = globalColorScheme;
  }

  public ColourSchemeI getColourScheme()
  {
    return colourScheme;
  }

  public void setColourScheme(ColourSchemeI colourScheme)
  {
    this.colourScheme = colourScheme;
  }

  @Override
  public FeaturesDisplayedI getDisplayedFeatures()
  {
    return displayedFeatures;
  }

  public void setDisplayedFeatures(FeaturesDisplayedI displayedFeatures)
  {
    this.displayedFeatures = displayedFeatures;
  }

  public void configureForView(AlignmentViewPanel avpanel)
  {
    super.configureForView(avpanel);
    AlignViewportI viewport = avpanel.getAlignViewport();
    AlignmentI alignment = viewport.getAlignment();
    AlignmentAnnotation[] annots = alignment.getAlignmentAnnotation();

    seqGroups = alignment.getGroups();
    fr = avpanel.cloneFeatureRenderer();

    // Add non auto calculated annotation to AlignFile
    for (AlignmentAnnotation annot : annots)
    {
      if (annot != null && !annot.autoCalculated)
      {
        if (!annot.visible)
        {
          continue;
        }
        annotations.add(annot);
      }
    }
    globalColorScheme = ColourSchemeProperty.getColourName(viewport
            .getGlobalColourScheme());
    setDisplayedFeatures(viewport.getFeaturesDisplayed());
    showSeqFeatures = viewport.isShowSequenceFeatures();

  }

  public boolean isShowSeqFeatures()
  {
    return showSeqFeatures;
  }

  public void setShowSeqFeatures(boolean showSeqFeatures)
  {
    this.showSeqFeatures = showSeqFeatures;
  }

  public Vector<AlignmentAnnotation> getAnnotations()
  {
    return annotations;
  }

  public List<int[]> getHiddenColumns()
  {
    return hiddenColumns;
  }

  public ColumnSelection getColumnSelection()
  {
    return columnSelection;
  }

  public void setColumnSelection(ColumnSelection columnSelection)
  {
    this.columnSelection = columnSelection;
  }

  public SequenceI[] getHiddenSequences()
  {
    if (hiddenSequences == null || hiddenSequences.isEmpty())
    {
      return new SequenceI[] {};
    }
    synchronized (hiddenSequences)
    {
      return hiddenSequences.toArray(new SequenceI[hiddenSequences.size()]);
    }
  }

  public void setHiddenSequences(ArrayList<SequenceI> hiddenSequences)
  {
    this.hiddenSequences = hiddenSequences;
  }

  public class JSONExportSettings
  {
    private boolean exportSequence;

    private boolean exportSequenceFeatures;

    private boolean exportAnnotations;

    private boolean exportGroups;

    private boolean exportJalviewSettings;

    public boolean isExportSequence()
    {
      return exportSequence;
    }

    public void setExportSequence(boolean exportSequence)
    {
      this.exportSequence = exportSequence;
    }

    public boolean isExportSequenceFeatures()
    {
      return exportSequenceFeatures;
    }

    public void setExportSequenceFeatures(boolean exportSequenceFeatures)
    {
      this.exportSequenceFeatures = exportSequenceFeatures;
    }

    public boolean isExportAnnotations()
    {
      return exportAnnotations;
    }

    public void setExportAnnotations(boolean exportAnnotations)
    {
      this.exportAnnotations = exportAnnotations;
    }

    public boolean isExportGroups()
    {
      return exportGroups;
    }

    public void setExportGroups(boolean exportGroups)
    {
      this.exportGroups = exportGroups;
    }

    public boolean isExportJalviewSettings()
    {
      return exportJalviewSettings;
    }

    public void setExportJalviewSettings(boolean exportJalviewSettings)
    {
      this.exportJalviewSettings = exportJalviewSettings;
    }
  }
}
