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
package jalview.util;

/**
 * A class to hold constants relating to Url links used in Jalview
 */
public class UrlConstants
{

  /*
   * Sequence ID string
   */
  public static final String DB_ACCESSION = "DB_ACCESSION";

  /*
   * Sequence Name string
   */
  public static final String SEQUENCE_ID = "SEQUENCE_ID";

  /*
   * Default sequence URL link string for EMBL-EBI search
   */
  public static final String EMBLEBI_STRING = "EMBL-EBI Search|http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$";

  /*
   * Default sequence URL link string for SRS 
   */
  public static final String SRS_STRING = "SRS|http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-newId+(([uniprot-all:$SEQUENCE_ID$]))+-view+SwissEntry";

  /*
   * not instantiable
   */
  private UrlConstants()
  {
  }
}
