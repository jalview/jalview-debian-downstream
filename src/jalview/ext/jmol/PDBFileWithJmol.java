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
package jalview.ext.jmol;

import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.Annotation;
import jalview.datamodel.PDBEntry;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.io.AlignFile;
import jalview.io.FileParse;
import jalview.schemes.ResidueProperties;
import jalview.util.MessageManager;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import javajs.awt.Dimension;

import org.jmol.api.JmolStatusListener;
import org.jmol.api.JmolViewer;
import org.jmol.c.CBK;
import org.jmol.modelset.Group;
import org.jmol.modelset.Model;
import org.jmol.modelset.ModelSet;
import org.jmol.modelsetbio.BioModel;
import org.jmol.modelsetbio.BioPolymer;
import org.jmol.viewer.Viewer;

/**
 * Import and process PDB files with Jmol
 * 
 * @author jprocter
 * 
 */
public class PDBFileWithJmol extends AlignFile implements
        JmolStatusListener
{
  Viewer viewer = null;

  public PDBFileWithJmol(String inFile, String type) throws IOException
  {
    super(inFile, type);
  }

  public PDBFileWithJmol(FileParse fp) throws IOException
  {
    super(fp);
  }

  public PDBFileWithJmol()
  {
  }

  /**
   * create a headless jmol instance for dataprocessing
   * 
   * @return
   */
  private Viewer getJmolData()
  {
    if (viewer == null)
    {
      try
      {
        viewer = (Viewer) JmolViewer.allocateViewer(null, null, null, null,
                null, "-x -o -n", this);
      } catch (ClassCastException x)
      {
        throw new Error(MessageManager.formatMessage(
                "error.jmol_version_not_compatible_with_jalview_version",
                new String[] { JmolViewer.getJmolVersion() }), x);
      }
    }
    return viewer;
  }

  private void waitForScript(Viewer jmd)
  {
    while (jmd.isScriptExecuting())
    {
      try
      {
        Thread.sleep(50);

      } catch (InterruptedException x)
      {
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.io.AlignFile#parse()
   */
  @Override
  public void parse() throws IOException
  {
    Viewer jmd = getJmolData();
    jmd.openReader(getDataName(), getDataName(), getReader());
    waitForScript(jmd);

    if (jmd.ms.mc > 0)
    {
      ModelSet ms = jmd.ms;
      // Jmol 14.2 added third argument doReport = false
      ms.calculateStructures(null, true, false, false, true);
      // System.out.println("Structs\n"+structs);
      Group group = null;
      int modelIndex = -1;
      for (Model model : ms.am)
      {
        modelIndex++;
        for (BioPolymer bp : ((BioModel) model).bioPolymers)
        {
          int lastChainId = 0; // int value of character e.g. 65 for A
          String lastChainIdAlpha = "";

          int[] groups = bp.getLeadAtomIndices();
          char seq[] = new char[groups.length], secstr[] = new char[groups.length], secstrcode[] = new char[groups.length];
          int groupc = 0, len = 0, firstrnum = 1, lastrnum = 0;

          do
          {
            if (groupc >= groups.length
                    || ms.at[groups[groupc]].group.chain.chainID != lastChainId)
            {
              /*
               * on change of chain (or at end), construct the sequence and
               * secondary structure annotation for the last chain
               */
              if (len > 0)
              {
                boolean isNa = bp.isNucleic();
                // normalise sequence from Jmol to jalview
                int[] cinds = isNa ? ResidueProperties.nucleotideIndex
                        : ResidueProperties.aaIndex;
                int nonGap = isNa ? ResidueProperties.maxNucleotideIndex
                        : ResidueProperties.maxProteinIndex;
                char ngc = 'X';
                char newseq[] = new char[len];
                Annotation asecstr[] = new Annotation[len + firstrnum - 1];
                for (int p = 0; p < len; p++)
                {
                  newseq[p] = cinds[seq[p]] == nonGap ? ngc : seq[p];
                  if (secstr[p] >= 'A' && secstr[p] <= 'z')
                  {
                    try
                    {
                      asecstr[p] = new Annotation("" + secstr[p], null,
                              secstrcode[p], Float.NaN);
                    } catch (ArrayIndexOutOfBoundsException e)
                    {
                      // skip - patch for JAL-1836
                    }
                  }
                }
                String modelTitle = (String) ms
                        .getInfo(modelIndex, "title");
                SequenceI sq = new Sequence("" + getDataName() + "|"
                        + modelTitle + "|" + lastChainIdAlpha, newseq,
                        firstrnum, lastrnum);
                PDBEntry pdbe = new PDBEntry();
                pdbe.setFile(getDataName());
                pdbe.setId(getDataName());
                pdbe.setProperty(new Hashtable());
                // pdbe.getProperty().put("CHAIN", "" + _lastChainId);
                pdbe.setChainCode(lastChainIdAlpha);
                sq.addPDBId(pdbe);
                // JAL-1533
                // Need to put the number of models for this polymer somewhere
                // for Chimera/others to grab
                // pdbe.getProperty().put("PDBMODELS", biopoly.)
                seqs.add(sq);
                if (!isNa)
                {
                  String mt = modelTitle == null ? getDataName()
                          : modelTitle;
                  if (lastChainId >= ' ')
                  {
                    mt += lastChainIdAlpha;
                  }
                  AlignmentAnnotation ann = new AlignmentAnnotation(
                          "Secondary Structure", "Secondary Structure for "
                                  + mt, asecstr);
                  ann.belowAlignment = true;
                  ann.visible = true;
                  ann.autoCalculated = false;
                  ann.setCalcId(getClass().getName());
                  sq.addAlignmentAnnotation(ann);
                  ann.adjustForAlignment();
                  ann.validateRangeAndDisplay();
                  annotations.add(ann);
                }
              }
              len = 0;
              firstrnum = 1;
              lastrnum = 0;
            }
            if (groupc < groups.length)
            {
              group = ms.at[groups[groupc]].group;
              if (len == 0)
              {
                firstrnum = group.getResno();
                lastChainId = group.chain.chainID;
                lastChainIdAlpha = group.chain.getIDStr();
              }
              else
              {
                lastrnum = group.getResno();
              }
              seq[len] = group.getGroup1();

              /*
               * JAL-1828 replace a modified amino acid with its standard
               * equivalent (e.g. MSE with MET->M) to maximise sequence matching
               */
              String threeLetterCode = group.getGroup3();
              String canonical = ResidueProperties
                      .getCanonicalAminoAcid(threeLetterCode);
              if (canonical != null
                      && !canonical.equalsIgnoreCase(threeLetterCode))
              {
                seq[len] = ResidueProperties
                        .getSingleCharacterCode(canonical);
              }
              switch (group.getProteinStructureSubType())
              {
              case HELIX310:
                if (secstr[len] == 0)
                {
                  secstr[len] = '3';
                }
              case HELIXALPHA:
                if (secstr[len] == 0)
                {
                  secstr[len] = 'H';
                }
              case HELIXPI:
                if (secstr[len] == 0)
                {
                  secstr[len] = 'P';
                }
              case HELIX:
                if (secstr[len] == 0)
                {
                  secstr[len] = 'H';
                }
                secstrcode[len] = 'H';
                break;
              case SHEET:
                secstr[len] = 'E';
                secstrcode[len] = 'E';
                break;
              default:
                secstr[len] = 0;
                secstrcode[len] = 0;
              }
              len++;
            }
          } while (groupc++ < groups.length);
        }
      }

      /*
       * lastScriptTermination = -9465; String dsspOut =
       * jmd.evalString("calculate STRUCTURE"); if (dsspOut.equals("pending")) {
       * while (lastScriptTermination == -9465) { try { Thread.sleep(50); }
       * catch (Exception x) { } ; } } System.out.println(lastConsoleEcho);
       */
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.io.AlignFile#print()
   */
  @Override
  public String print()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setCallbackFunction(String callbackType,
          String callbackFunction)
  {
    // TODO Auto-generated method stub

  }

  /*
   * @Override public void notifyCallback(EnumCallback type, Object[] data) {
   * try { switch (type) { case ERROR: case SCRIPT:
   * notifyScriptTermination((String) data[2], ((Integer) data[3]).intValue());
   * break; case MESSAGE: sendConsoleMessage((data == null) ? ((String) null) :
   * (String) data[1]); break; case LOADSTRUCT: notifyFileLoaded((String)
   * data[1], (String) data[2], (String) data[3], (String) data[4], ((Integer)
   * data[5]).intValue());
   * 
   * break; default: // System.err.println("Unhandled callback " + type + " " //
   * + data[1].toString()); break; } } catch (Exception e) {
   * System.err.println("Squashed Jmol callback handler error:");
   * e.printStackTrace(); } }
   */
  public void notifyCallback(CBK type, Object[] data)
  {
    String strInfo = (data == null || data[1] == null ? null : data[1]
            .toString());
    switch (type)
    {
    case ECHO:
      sendConsoleEcho(strInfo);
      break;
    case SCRIPT:
      notifyScriptTermination((String) data[2],
              ((Integer) data[3]).intValue());
      break;
    case MEASURE:
      String mystatus = (String) data[3];
      if (mystatus.indexOf("Picked") >= 0
              || mystatus.indexOf("Sequence") >= 0)
      {
        // Picking mode
        sendConsoleMessage(strInfo);
      }
      else if (mystatus.indexOf("Completed") >= 0)
      {
        sendConsoleEcho(strInfo.substring(strInfo.lastIndexOf(",") + 2,
                strInfo.length() - 1));
      }
      break;
    case MESSAGE:
      sendConsoleMessage(data == null ? null : strInfo);
      break;
    case PICK:
      sendConsoleMessage(strInfo);
      break;
    default:
      break;
    }
  }

  String lastConsoleEcho = "";

  private void sendConsoleEcho(String string)
  {
    lastConsoleEcho += string;
    lastConsoleEcho += "\n";
  }

  String lastConsoleMessage = "";

  private void sendConsoleMessage(String string)
  {
    lastConsoleMessage += string;
    lastConsoleMessage += "\n";
  }

  int lastScriptTermination = -1;

  String lastScriptMessage = "";

  private void notifyScriptTermination(String string, int intValue)
  {
    lastScriptMessage += string;
    lastScriptMessage += "\n";
    lastScriptTermination = intValue;
  }

  @Override
  public boolean notifyEnabled(CBK callbackPick)
  {
    switch (callbackPick)
    {
    case MESSAGE:
    case SCRIPT:
    case ECHO:
    case LOADSTRUCT:
    case ERROR:
      return true;
    default:
      return false;
    }
  }

  @Override
  public String eval(String strEval)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public float[][] functionXY(String functionName, int x, int y)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public float[][][] functionXYZ(String functionName, int nx, int ny, int nz)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String createImage(String fileName, String type,
          Object text_or_bytes, int quality)
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map<String, Object> getRegistryInfo()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void showUrl(String url)
  {
    // TODO Auto-generated method stub

  }

  @Override
  public Dimension resizeInnerPanel(String data)
  {
    return null;
  }

  @Override
  public Map<String, Object> getJSpecViewProperty(String arg0)
  {
    return null;
  }

}
