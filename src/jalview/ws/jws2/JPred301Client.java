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

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.gui.AlignFrame;
import jalview.ws.jws2.jabaws2.Jws2Instance;
import jalview.ws.params.ArgumentI;
import jalview.ws.params.OptionI;
import jalview.ws.params.WsParamSetI;
import jalview.ws.uimodel.AlignAnalysisUIText;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compbio.data.sequence.FastaSequence;
import compbio.data.sequence.JpredAlignment;
import compbio.metadata.Argument;

public class JPred301Client extends JabawsMsaInterfaceAlignCalcWorker
{
  /**
   * 
   * @return default args for this service when run as dynamic web service
   */
  public List<Argument> selectDefaultArgs()
  {
    List<ArgumentI> rgs = new ArrayList<ArgumentI>();
    for (ArgumentI argi : service.getParamStore().getServiceParameters())
    {
      if (argi instanceof OptionI)
      {
        List<String> o = ((OptionI) argi).getPossibleValues();
        if (o.contains("-pred-nohits"))
        {
          OptionI cpy = ((OptionI) argi).copy();
          cpy.setValue("-pred-nohits");
          rgs.add(cpy);
        }
      }
    }
    return JabaParamStore.getJabafromJwsArgs(rgs);
  }

  public JPred301Client(Jws2Instance service, AlignFrame alignFrame,
          WsParamSetI preset, List<Argument> paramset)
  {
    super(service, alignFrame, preset, paramset);
    submitGaps = true;
    alignedSeqs = true;
    nucleotidesAllowed = false;
    proteinAllowed = true;
    gapMap = new boolean[0];
    updateParameters(null, selectDefaultArgs());
  }

  @Override
  boolean checkValidInputSeqs(boolean dynamic, List<FastaSequence> seqs)
  {
    return (seqs.size() > 1);
  }

  @Override
  public String getServiceActionText()
  {
    return "calculating consensus secondary structure prediction using JPred service";
  }

  private static Map<String, String[]> jpredRowLabels = new HashMap<String, String[]>();

  private static final Set<String> jpredRes_graph;

  private static final Set<String> jpredRes_ssonly;
  static
  {
    jpredRes_ssonly = new HashSet<String>();
    jpredRes_ssonly.add("jnetpred".toLowerCase());
    jpredRes_ssonly.add("jnetpssm".toLowerCase());
    jpredRes_ssonly.add("jnethmm".toLowerCase());
    jpredRes_graph = new HashSet<String>();
    jpredRes_graph.add("jnetconf".toLowerCase());
    jpredRes_graph.add("jnet burial".toLowerCase());
  }

  /**
   * update the consensus annotation from the sequence profile data using
   * current visualization settings.
   */
  @Override
  public void updateResultAnnotation(boolean immediate)
  {
    if (immediate || !calcMan.isWorking(this) && msascoreset != null)
    {
      if (msascoreset instanceof compbio.data.sequence.JpredAlignment)
      {
        JpredAlignment jpres = (JpredAlignment) msascoreset;
        int alWidth = alignViewport.getAlignment().getWidth();
        ArrayList<AlignmentAnnotation> ourAnnot = new ArrayList<AlignmentAnnotation>();
        char[] sol = new char[jpres.getJpredSequences().get(0).getLength()];
        boolean firstsol = true;
        for (FastaSequence fsq : jpres.getJpredSequences())
        {
          String[] k = jpredRowLabels.get(fsq.getId());
          if (k == null)
          {
            k = new String[] { fsq.getId(), "JNet Output" };
          }
          if (fsq.getId().startsWith("JNETSOL"))
          {
            char amnt = (fsq.getId().endsWith("25") ? "3" : fsq.getId()
                    .endsWith("5") ? "6" : "9").charAt(0);
            char[] vseq = fsq.getSequence().toCharArray();
            for (int spos = 0, sposL = fsq.getLength(); spos < sposL; spos++)
            {
              if (firstsol)
              {
                sol[spos] = '0';
              }
              if (vseq[spos] == 'B'
                      && (sol[spos] == '0' || sol[spos] < amnt))
              {
                sol[spos] = amnt;
              }
            }
            firstsol = false;
          }
          else
          {
            createAnnotationRowFromString(
                    ourAnnot,
                    getCalcId(),
                    alWidth,
                    k[0],
                    k[1],
                    jpredRes_graph.contains(fsq.getId()) ? AlignmentAnnotation.BAR_GRAPH
                            : AlignmentAnnotation.NO_GRAPH, 0f, 9f,
                    fsq.getSequence());
          }

        }
        createAnnotationRowFromString(
                ourAnnot,
                getCalcId(),
                alWidth,
                "Jnet Burial",
                "<html>Prediction of Solvent Accessibility<br/>levels are<ul><li>0 - Exposed</li><li>3 - 25% or more S.A. accessible</li><li>6 - 5% or more S.A. accessible</li><li>9 - Buried (<5% exposed)</li></ul>",
                AlignmentAnnotation.BAR_GRAPH, 0f, 9f, new String(sol));
        for (FastaSequence fsq : jpres.getSequences())
        {
          if (fsq.getId().equalsIgnoreCase("QUERY"))
          {
            createAnnotationRowFromString(ourAnnot, getCalcId(), alWidth,
                    "Query", "JPred Reference Sequence",
                    AlignmentAnnotation.NO_GRAPH, 0f, 0f, fsq.getSequence());
          }
        }
        if (ourAnnot.size() > 0)
        {
          updateOurAnnots(ourAnnot);
        }
      }
    }
  }

  private void createAnnotationRowFromString(
          ArrayList<AlignmentAnnotation> ourAnnot, String calcId,
          int alWidth, String label, String descr, int rowType, float min,
          float max, String jpredPrediction)
  {
    // simple annotation row
    AlignmentAnnotation annotation = alignViewport.getAlignment()
            .findOrCreateAnnotation(label, calcId, true, null, null);
    if (alWidth == gapMap.length) // scr.getScores().size())
    {
      annotation.label = new String(label);
      annotation.description = new String(descr);
      annotation.graph = rowType;
      annotation.graphMin = min;
      annotation.graphMax = max;
      if (constructAnnotationFromString(annotation, jpredPrediction,
              alWidth, rowType))
      {
        // created a valid annotation from the data
        ourAnnot.add(annotation);
        // annotation.validateRangeAndDisplay();
      }
    }
  }

  private boolean constructAnnotationFromString(
          AlignmentAnnotation annotation, String sourceData, int alWidth,
          int rowType)
  {
    if (sourceData.length() == 0 && alWidth > 0)
    {
      return false;
    }
    Annotation[] elm = new Annotation[alWidth];
    boolean ssOnly = jpredRes_ssonly.contains(annotation.label
            .toLowerCase());
    boolean graphOnly = rowType != AlignmentAnnotation.NO_GRAPH;
    if (!ssOnly && !graphOnly)
    {
      // for burial 'B'
      annotation.showAllColLabels = true;
    }

    for (int i = 0, iSize = sourceData.length(); i < iSize; i++)
    {
      char annot = sourceData.charAt(i);
      // if we're at a gapped column then skip to next ungapped position
      if (gapMap != null && gapMap.length > 0)
      {
        while (!gapMap[i])
        {
          elm[i++] = new Annotation("", "", ' ', Float.NaN);
        }
      }
      switch (rowType)
      {
      case AlignmentAnnotation.NO_GRAPH:
        elm[i] = ssOnly ? new Annotation("", "", annot, Float.NaN,
                colourSS(annot)) : new Annotation("" + annot, "" + annot,
                '\0', Float.NaN);
        break;
      default:
        try
        {
          elm[i] = new Annotation("" + annot, "" + annot, annot,
                  Integer.valueOf("" + annot));
        } catch (Exception x)
        {
          System.err.println("Expected numeric value in character '"
                  + annot + "'");
        }
      }
    }

    annotation.annotations = elm;
    annotation.belowAlignment = true;
    annotation.validateRangeAndDisplay();
    return true;
  }

  private Color colourSS(char annot)
  {
    switch (annot)
    {
    case 'H':
      return jalview.renderer.AnnotationRenderer.HELIX_COLOUR;
    case 'E':
      return jalview.renderer.AnnotationRenderer.SHEET_COLOUR;
    }
    return jalview.renderer.AnnotationRenderer.GLYPHLINE_COLOR;
  }

  @Override
  public String getCalcId()
  {
    return CALC_ID;
  }

  private static String CALC_ID = "jabaws21.JPred3Cons";

  public static AlignAnalysisUIText getAlignAnalysisUITest()
  {
    return new AlignAnalysisUIText(
            compbio.ws.client.Services.JpredWS.toString(),
            jalview.ws.jws2.JPred301Client.class, CALC_ID, false, true,
            true, "JPred Consensus",
            "When checked, JPred consensus is updated automatically.",
            "Change JPred Settings...",
            "Modify settings for JPred calculations.");
  }
}
