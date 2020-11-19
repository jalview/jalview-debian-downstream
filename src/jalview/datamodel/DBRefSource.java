/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.11.1.3)
 * Copyright (C) 2020 The Jalview Authors
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Defines internal constants for unambiguous annotation of DbRefEntry source
 * strings and describing the data retrieved from external database sources (see
 * jalview.ws.DbSourcProxy) <br/>
 * TODO: replace with ontology to allow recognition of particular attributes
 * (e.g. protein coding, alignment (ortholog db, paralog db, domain db),
 * genomic, transcriptomic, 3D structure providing (PDB, MODBASE, etc) ..).
 * 
 * @author JimP
 * 
 */
public class DBRefSource
{
  /**
   * UNIPROT Accession Number
   */
  public static final String UNIPROT = "UNIPROT";

  /**
   * UNIPROT Entry Name
   */
  public static final String UP_NAME = "UNIPROT_NAME".toUpperCase();

  /**
   * Uniprot Knowledgebase/TrEMBL as served from EMBL protein products.
   */
  public static final String UNIPROTKB = "UniProtKB/TrEMBL".toUpperCase();

  public static final String EMBLCDSProduct = "EMBLCDSProtein"
          .toUpperCase();

  /**
   * PDB Entry Code
   */
  public static final String PDB = "PDB";

  /**
   * EMBL ID
   */
  public static final String EMBL = "EMBL";

  /**
   * EMBLCDS ID
   */
  public static final String EMBLCDS = "EMBLCDS";

  /**
   * PFAM ID
   */
  public static final String PFAM = "PFAM";

  /**
   * RFAM ID
   */
  public static final String RFAM = "RFAM";

  /**
   * GeneDB ID
   */
  public static final String GENEDB = "GeneDB".toUpperCase();

  /**
   * Ensembl
   */
  public static final String ENSEMBL = "ENSEMBL";

  public static final String ENSEMBLGENOMES = "ENSEMBLGENOMES";

  /**
   * List of databases whose sequences might have coding regions annotated
   */
  public static final String[] DNACODINGDBS = { EMBL, EMBLCDS, GENEDB,
      ENSEMBL, ENSEMBLGENOMES };

  public static final String[] CODINGDBS = { EMBLCDS, GENEDB, ENSEMBL };

  public static final String[] PROTEINDBS = { UNIPROT, UNIPROTKB,
      EMBLCDSProduct, ENSEMBL }; // Ensembl ENSP* entries are protein

  public static String[] allSources()
  {
    List<String> src = new ArrayList<>();
    for (Field f : DBRefSource.class.getFields())
    {
      if (String.class.equals(f.getType()))
      {
        try
        {
          src.add((String) f.get(null));
        } catch (Exception x)
        {
          x.printStackTrace();
        }
      }
    }
    return src.toArray(new String[0]);
  }
}
