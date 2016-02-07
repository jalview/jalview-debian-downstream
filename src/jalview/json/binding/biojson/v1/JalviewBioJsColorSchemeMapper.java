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
package jalview.json.binding.biojson.v1;

import jalview.schemes.Blosum62ColourScheme;
import jalview.schemes.BuriedColourScheme;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.HelixColourScheme;
import jalview.schemes.HydrophobicColourScheme;
import jalview.schemes.NucleotideColourScheme;
import jalview.schemes.PIDColourScheme;
import jalview.schemes.PurinePyrimidineColourScheme;
import jalview.schemes.RNAInteractionColourScheme;
import jalview.schemes.StrandColourScheme;
import jalview.schemes.TaylorColourScheme;
import jalview.schemes.TurnColourScheme;
import jalview.schemes.ZappoColourScheme;

public enum JalviewBioJsColorSchemeMapper
{

  USER_DEFINED("User Defined", "user defined", null), NONE("None", "foo",
          null), CLUSTAL("Clustal", "clustal", null), ZAPPO("Zappo",
          "zappo", new ZappoColourScheme()), TAYLOR("Taylor", "taylor",
          new TaylorColourScheme()), NUCLEOTIDE("Nucleotide", "nucleotide",
          new NucleotideColourScheme()), PURINE_PYRIMIDINE(
          "Purine/Pyrimidine", "purine", new PurinePyrimidineColourScheme()), HELIX_PROPENSITY(
          "Helix Propensity", "helix", new HelixColourScheme()), TURN_PROPENSITY(
          "Turn Propensity", "turn", new TurnColourScheme()), STRAND_PROPENSITY(
          "Strand Propensity", "strand", new StrandColourScheme()), BURIED_INDEX(
          "Buried Index", "buried", new BuriedColourScheme()), HYDROPHOBIC(
          "Hydrophobic", "hydro", new HydrophobicColourScheme()),

  // The color types below are not yet supported by BioJs MSA viewer
  T_COFFE_SCORES("T-Coffee Scores", "T-Coffee Scores", null), RNA_INT_TYPE(
          "RNA Interaction type", "RNA Interaction type",
          new RNAInteractionColourScheme()), BLOSUM62("Blosum62",
          "Blosum62", new Blosum62ColourScheme()), RNA_HELICES(
          "RNA Helices", "RNA Helices", null), PERCENTAGE_IDENTITY(
          "% Identity", "pid", new PIDColourScheme());

  private String jalviewName;

  private String bioJsName;

  private ColourSchemeI jvColourScheme;

  private JalviewBioJsColorSchemeMapper(String jalviewName,
          String bioJsName, ColourSchemeI jvColourScheme)
  {
    this.jalviewName = jalviewName;
    this.bioJsName = bioJsName;
    this.setJvColourScheme(jvColourScheme);
  }

  public String getJalviewName()
  {
    return jalviewName;
  }

  public String getBioJsName()
  {
    return bioJsName;
  }

  public ColourSchemeI getJvColourScheme()
  {
    return jvColourScheme;
  }

  public void setJvColourScheme(ColourSchemeI jvColourScheme)
  {
    this.jvColourScheme = jvColourScheme;
  }

}
