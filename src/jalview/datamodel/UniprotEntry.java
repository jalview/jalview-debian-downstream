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
package jalview.datamodel;

import java.util.Vector;

/**
 * Data model for an entry returned from a Uniprot query
 * 
 * @see uniprot_mapping.xml
 */
public class UniprotEntry
{

  UniprotSequence sequence;

  Vector<String> name;

  Vector<String> accession;

  Vector<SequenceFeature> feature;

  Vector<PDBEntry> dbrefs;

  UniprotProteinName protName;

  public void setAccession(Vector<String> items)
  {
    accession = items;
  }

  public void setFeature(Vector<SequenceFeature> items)
  {
    feature = items;
  }

  public Vector<SequenceFeature> getFeature()
  {
    return feature;
  }

  public Vector<String> getAccession()
  {
    return accession;
  }

  public void setProtein(UniprotProteinName names)
  {
    protName = names;
  }

  public UniprotProteinName getProtein()
  {
    return protName;
  }

  public void setName(Vector<String> na)
  {
    name = na;
  }

  public Vector<String> getName()
  {
    return name;
  }

  public UniprotSequence getUniprotSequence()
  {
    return sequence;
  }

  public void setUniprotSequence(UniprotSequence seq)
  {
    sequence = seq;
  }

  public Vector<PDBEntry> getDbReference()
  {
    return dbrefs;
  }

  public void setDbReference(Vector<PDBEntry> dbref)
  {
    this.dbrefs = dbref;
  }

}
