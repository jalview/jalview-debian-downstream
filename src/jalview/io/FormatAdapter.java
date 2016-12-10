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
package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;

/**
 * Additional formatting methods used by the application in a number of places.
 * 
 * @author $author$
 * @version $Revision$
 */
public class FormatAdapter extends AppletFormatAdapter
{
  public FormatAdapter(AlignmentViewPanel viewpanel)
  {
    super(viewpanel);
    init();
  }

  public FormatAdapter()
  {
    super();
    init();
  }

  public FormatAdapter(AlignmentViewPanel alignPanel,
          AlignExportSettingI settings)
  {
    super(alignPanel, settings);
  }

  private void init()
  {
    if (jalview.bin.Cache.getDefault("STRUCT_FROM_PDB", true))
    {
      annotFromStructure = jalview.bin.Cache.getDefault("ADD_TEMPFACT_ANN",
              true);
      localSecondaryStruct = jalview.bin.Cache.getDefault("ADD_SS_ANN",
              true);
      serviceSecondaryStruct = jalview.bin.Cache.getDefault("USE_RNAVIEW",
              true);
    }
    else
    {
      // disable all PDB annotation options
      annotFromStructure = false;
      localSecondaryStruct = false;
      serviceSecondaryStruct = false;
    }
  }

  public String formatSequences(String format, SequenceI[] seqs,
          String[] omitHiddenColumns, int[] exportRange)
  {

    return formatSequences(format,
            replaceStrings(seqs, omitHiddenColumns, exportRange));
  }

  /**
   * create sequences with each sequence string replaced with the one given in
   * omitHiddenCOlumns
   * 
   * @param seqs
   * @param omitHiddenColumns
   * @return new sequences
   */
  public SequenceI[] replaceStrings(SequenceI[] seqs,
          String[] omitHiddenColumns, int[] startEnd)
  {
    if (omitHiddenColumns != null)
    {
      SequenceI[] tmp = new SequenceI[seqs.length];

      int startRes;
      int endRes;
      int startIndex;
      int endIndex;
      for (int i = 0; i < seqs.length; i++)
      {
        startRes = seqs[i].getStart();
        endRes = seqs[i].getEnd();
        if (startEnd != null)
        {
          startIndex = startEnd[0];
          endIndex = startEnd[1];
          // get first non-gaped residue start position
          while (jalview.util.Comparison.isGap(seqs[i]
                  .getCharAt(startIndex)) && startIndex < endIndex)
          {
            startIndex++;
          }

          // get last non-gaped residue end position
          while (jalview.util.Comparison.isGap(seqs[i].getCharAt(endIndex))
                  && endIndex > startIndex)
          {
            endIndex--;
          }

          startRes = seqs[i].findPosition(startIndex);
          endRes = seqs[i].findPosition(endIndex);
        }

        tmp[i] = new Sequence(seqs[i].getName(), omitHiddenColumns[i],
                startRes, endRes);
        tmp[i].setDescription(seqs[i].getDescription());
      }
      seqs = tmp;
    }
    return seqs;
  }

  /**
   * Format a vector of sequences as a flat alignment file. TODO: allow caller
   * to detect errors and warnings encountered when generating output
   * 
   * 
   * @param format
   *          Format string as givien in the AppletFormatAdaptor list (exact
   *          match to name of class implementing file io for that format)
   * @param seqs
   *          vector of sequences to write
   * 
   * @return String containing sequences in desired format
   */
  public String formatSequences(String format, SequenceI[] seqs)
  {

    try
    {
      AlignFile afile = null;

      if (format.equalsIgnoreCase("FASTA"))
      {
        afile = new FastaFile();
        afile.addJVSuffix(jalview.bin.Cache.getDefault("FASTA_JVSUFFIX",
                true));
      }
      else if (format.equalsIgnoreCase("MSF"))
      {
        afile = new MSFfile();
        afile.addJVSuffix(jalview.bin.Cache
                .getDefault("MSF_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PileUp"))
      {
        afile = new PileUpfile();
        afile.addJVSuffix(jalview.bin.Cache.getDefault("PILEUP_JVSUFFIX",
                true));
      }
      else if (format.equalsIgnoreCase("CLUSTAL"))
      {
        afile = new ClustalFile();
        afile.addJVSuffix(jalview.bin.Cache.getDefault("CLUSTAL_JVSUFFIX",
                true));
      }
      else if (format.equalsIgnoreCase("BLC"))
      {
        afile = new BLCFile();
        afile.addJVSuffix(jalview.bin.Cache
                .getDefault("BLC_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PIR"))
      {
        afile = new PIRFile();
        afile.addJVSuffix(jalview.bin.Cache
                .getDefault("PIR_JVSUFFIX", true));
      }
      else if (format.equalsIgnoreCase("PFAM"))
      {
        afile = new PfamFile();
        afile.addJVSuffix(jalview.bin.Cache.getDefault("PFAM_JVSUFFIX",
                true));
      }
      /*
       * amsa is not supported by this function - it requires an alignment
       * rather than a sequence vector else if (format.equalsIgnoreCase("AMSA"))
       * { afile = new AMSAFile(); afile.addJVSuffix(
       * jalview.bin.Cache.getDefault("AMSA_JVSUFFIX", true)); }
       */

      afile.setSeqs(seqs);
      String afileresp = afile.print();
      if (afile.hasWarningMessage())
      {
        System.err.println("Warning raised when writing as " + format
                + " : " + afile.getWarningMessage());
      }
      return afileresp;
    } catch (Exception e)
    {
      System.err.println("Failed to write alignment as a '" + format
              + "' file\n");
      e.printStackTrace();
    }

    return null;
  }

  public boolean getCacheSuffixDefault(String format)
  {
    if (isValidFormat(format))
    {
      return jalview.bin.Cache.getDefault(format.toUpperCase()
              + "_JVSUFFIX", true);
    }
    return false;
  }

  public String formatSequences(String format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, ColumnSelection colSel)
  {
    return formatSequences(format, alignment, omitHidden, exportRange,
            getCacheSuffixDefault(format), colSel, null);
  }

  public String formatSequences(String format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, ColumnSelection colSel,
          SequenceGroup sgp)
  {
    return formatSequences(format, alignment, omitHidden, exportRange,
            getCacheSuffixDefault(format), colSel, sgp);
  }

  /**
   * hack function to replace seuqences with visible sequence strings before
   * generating a string of the alignment in the given format.
   * 
   * @param format
   * @param alignment
   * @param omitHidden
   *          sequence strings to write out in order of sequences in alignment
   * @param colSel
   *          defines hidden columns that are edited out of annotation
   * @return string representation of the alignment formatted as format
   */
  public String formatSequences(String format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, boolean suffix,
          ColumnSelection colSel)
  {
    return formatSequences(format, alignment, omitHidden, exportRange,
            suffix, colSel, null);
  }

  public String formatSequences(String format, AlignmentI alignment,
          String[] omitHidden, int[] exportRange, boolean suffix,
          ColumnSelection colSel, jalview.datamodel.SequenceGroup selgp)
  {
    if (omitHidden != null)
    {
      // TODO consider using AlignmentView to prune to visible region
      // TODO prune sequence annotation and groups to visible region
      // TODO: JAL-1486 - set start and end for output correctly. basically,
      // AlignmentView.getVisibleContigs does this.
      Alignment alv = new Alignment(replaceStrings(
              alignment.getSequencesArray(), omitHidden, exportRange));
      AlignmentAnnotation[] ala = alignment.getAlignmentAnnotation();
      if (ala != null)
      {
        for (int i = 0; i < ala.length; i++)
        {
          AlignmentAnnotation na = new AlignmentAnnotation(ala[i]);
          if (selgp != null)
          {
            colSel.makeVisibleAnnotation(selgp.getStartRes(),
                    selgp.getEndRes(), na);
          }
          else
          {
            colSel.makeVisibleAnnotation(na);
          }
          alv.addAnnotation(na);
        }
      }
      return this.formatSequences(format, alv, suffix);
    }
    return this.formatSequences(format, alignment, suffix);
  }

  /**
   * validate format is valid for IO in Application. This is basically the
   * AppletFormatAdapter.isValidFormat call with additional checks for
   * Application only formats like 'Jalview'.
   * 
   * @param format
   *          a format string to be compared with list of readable or writable
   *          formats (READABLE_FORMATS or WRITABLE_FORMATS)
   * @param forwriting
   *          when true, format is checked against list of writable formats.
   * @return true if format is valid
   */
  public static final boolean isValidIOFormat(String format,
          boolean forwriting)
  {
    if (format.equalsIgnoreCase("jalview"))
    {
      return true;
    }
    return AppletFormatAdapter.isValidFormat(format, forwriting);
  }

  /**
   * Create a flat file representation of a given view or selected region of a
   * view
   * 
   * @param format
   * @param ap
   *          alignment panel originating the view
   * @return String containing flat file
   */
  public String formatSequences(String format, AlignmentViewPanel ap,
          boolean selectedOnly)
  {
    return formatSequences(format, getCacheSuffixDefault(format), ap,
            selectedOnly);
  }

}
