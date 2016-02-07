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

import jalview.api.AlignViewportI;
import jalview.datamodel.AlignedCodonFrame;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceI;

import java.io.IOException;
import java.util.List;

/**
 * A GFF3 File parsing wrapper for the tangled mess that is FeaturesFile.
 * 
 * This class implements the methods relied on by FileLoader/FormatAdapter in
 * order to allow them to load alignments directly from GFF2 and GFF3 files that
 * contain sequence data and alignment information.
 * 
 * Major issues:
 * 
 * 1. GFF3 files commonly include mappings between DNA, RNA and Protein - so
 * this class needs a dataset AlignmentI context to create alignment codon
 * mappings.
 * 
 * 2. A single GFF3 file can generate many distinct alignments. Support will be
 * needed to allow several AlignmentI instances to be generated from a single
 * file.
 * 
 * 
 * @author jprocter
 *
 */
public class Gff3File extends FeaturesFile
{

  /**
   * 
   */
  public Gff3File()
  {
    super();
  }

  /**
   * @param source
   * @throws IOException
   */
  public Gff3File(FileParse source) throws IOException
  {
    super(source);
  }

  /**
   * @param inFile
   * @param type
   * @throws IOException
   */
  public Gff3File(String inFile, String type) throws IOException
  {
    super(inFile, type);
  }

  /**
   * @param parseImmediately
   * @param source
   * @throws IOException
   */
  public Gff3File(boolean parseImmediately, FileParse source)
          throws IOException
  {
    super(parseImmediately, source);
  }

  /**
   * @param parseImmediately
   * @param inFile
   * @param type
   * @throws IOException
   */
  public Gff3File(boolean parseImmediately, String inFile, String type)
          throws IOException
  {
    super(parseImmediately, inFile, type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.io.FeaturesFile#print()
   */
  @Override
  public String print()
  {
    // TODO GFF3 writer with sensible defaults for writing alignment data

    // return super.printGFFFormat(seqs, visible);
    return ("Not yet implemented.");
  }

  AlignmentI dataset;

  List<AlignmentI> alignments;

  @Override
  public void parse()
  {
    AlignViewportI av = getViewport();
    if (av != null)
    {
      if (av.getAlignment() != null)
      {
        dataset = av.getAlignment().getDataset();
      }
      if (dataset == null)
      {
        // working in the applet context ?
        dataset = av.getAlignment();
      }
    }
    else
    {
      dataset = new Alignment(new SequenceI[] {});
    }

    boolean parseResult = parse(dataset, null, null, false, true);
    if (!parseResult)
    {
      // pass error up somehow
    }
    if (av != null)
    {
      // update viewport with the dataset data ?
    }
    else
    {
      setSeqs(dataset.getSequencesArray());
    }

  }

  @Override
  public void addProperties(AlignmentI al)
  {
    super.addProperties(al);
    if (dataset.getCodonFrames() != null)
    {
      AlignmentI ds = (al.getDataset() == null) ? al : al.getDataset();
      for (AlignedCodonFrame codons : dataset.getCodonFrames())
      {
        ds.addCodonFrame(codons);
      }
    }
  }
}
