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
package jalview.datamodel.xdb.embl;

/**
 * Data model for a &lt;qualifier&gt; child element of a &lt;feature&gt; read
 * from an EMBL query reply
 * 
 * @see embl_mapping.xml
 */
public class Qualifier
{
  String name;

  String[] values;

  String[] evidence;

  /**
   * @return the name
   */
  public String getName()
  {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * @return the values
   */
  public String[] getValues()
  {
    return values;
  }

  /**
   * @param values
   *          the values to set
   */
  public void setValues(String[] values)
  {
    this.values = values;
  }

  public void addEvidence(String qevidence)
  {
    // TODO - not used? can remove?
    if (evidence == null)
    {
      evidence = new String[1];
    }
    else
    {
      String[] temp = new String[evidence.length + 1];
      System.arraycopy(evidence, 0, temp, 0, evidence.length);
      evidence = temp;
    }
    evidence[evidence.length - 1] = qevidence;
  }

  public void addValues(String value)
  {
    // TODO - not used? can remove?
    if (values == null)
    {
      values = new String[1];
    }
    else
    {
      String[] temp = new String[values.length + 1];
      System.arraycopy(values, 0, temp, 0, values.length);
      values = temp;
    }
    values[values.length - 1] = value;
  }

  /**
   * @return the evidence
   */
  public String[] getEvidence()
  {
    return evidence;
  }

  /**
   * @param evidence
   *          the evidence to set
   */
  public void setEvidence(String[] evidence)
  {
    this.evidence = evidence;
  }
}
