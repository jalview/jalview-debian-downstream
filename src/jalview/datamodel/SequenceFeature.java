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
package jalview.datamodel;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class SequenceFeature
{
  private static final String STATUS = "status";

  private static final String STRAND = "STRAND";

  // private key for Phase designed not to conflict with real GFF data
  private static final String PHASE = "!Phase";

  // private key for ENA location designed not to conflict with real GFF data
  private static final String LOCATION = "!Location";

  /*
   * ATTRIBUTES is reserved for the GFF 'column 9' data, formatted as
   * name1=value1;name2=value2,value3;...etc
   */
  private static final String ATTRIBUTES = "ATTRIBUTES";

  public int begin;

  public int end;

  public float score;

  public String type;

  public String description;

  /*
   * a map of key-value pairs; may be populated from GFF 'column 9' data,
   * other data sources (e.g. GenBank file), or programmatically
   */
  public Map<String, Object> otherDetails;

  public Vector<String> links;

  // Feature group can be set from a features file
  // as a group of features between STARTGROUP and ENDGROUP markers
  public String featureGroup;

  public SequenceFeature()
  {
  }

  /**
   * Constructs a duplicate feature. Note: Uses makes a shallow copy of the
   * otherDetails map, so the new and original SequenceFeature may reference the
   * same objects in the map.
   * 
   * @param cpy
   */
  public SequenceFeature(SequenceFeature cpy)
  {
    if (cpy != null)
    {
      begin = cpy.begin;
      end = cpy.end;
      score = cpy.score;
      if (cpy.type != null)
      {
        type = new String(cpy.type);
      }
      if (cpy.description != null)
      {
        description = new String(cpy.description);
      }
      if (cpy.featureGroup != null)
      {
        featureGroup = new String(cpy.featureGroup);
      }
      if (cpy.otherDetails != null)
      {
        try
        {
          otherDetails = (Map<String, Object>) ((HashMap<String, Object>) cpy.otherDetails)
                  .clone();
        } catch (Exception e)
        {
          // ignore
        }
      }
      if (cpy.links != null && cpy.links.size() > 0)
      {
        links = new Vector<String>();
        for (int i = 0, iSize = cpy.links.size(); i < iSize; i++)
        {
          links.addElement(cpy.links.elementAt(i));
        }
      }
    }
  }

  /**
   * Constructor including a Status value
   * 
   * @param type
   * @param desc
   * @param status
   * @param begin
   * @param end
   * @param featureGroup
   */
  public SequenceFeature(String type, String desc, String status,
          int begin, int end, String featureGroup)
  {
    this(type, desc, begin, end, featureGroup);
    setStatus(status);
  }

  /**
   * Constructor
   * 
   * @param type
   * @param desc
   * @param begin
   * @param end
   * @param featureGroup
   */
  SequenceFeature(String type, String desc, int begin, int end,
          String featureGroup)
  {
    this.type = type;
    this.description = desc;
    this.begin = begin;
    this.end = end;
    this.featureGroup = featureGroup;
  }

  /**
   * Constructor including a score value
   * 
   * @param type
   * @param desc
   * @param begin
   * @param end
   * @param score
   * @param featureGroup
   */
  public SequenceFeature(String type, String desc, int begin, int end,
          float score, String featureGroup)
  {
    this(type, desc, begin, end, featureGroup);
    this.score = score;
  }

  /**
   * Two features are considered equal if they have the same type, group,
   * description, start, end, phase, strand, and (if present) 'Name', ID' and
   * 'Parent' attributes.
   * 
   * Note we need to check Parent to distinguish the same exon occurring in
   * different transcripts (in Ensembl GFF). This allows assembly of transcript
   * sequences from their component exon regions.
   */
  @Override
  public boolean equals(Object o)
  {
    return equals(o, false);
  }

  /**
   * Overloaded method allows the equality test to optionally ignore the
   * 'Parent' attribute of a feature. This supports avoiding adding many
   * superficially duplicate 'exon' or CDS features to genomic or protein
   * sequence.
   * 
   * @param o
   * @param ignoreParent
   * @return
   */
  public boolean equals(Object o, boolean ignoreParent)
  {
    if (o == null || !(o instanceof SequenceFeature))
    {
      return false;
    }

    SequenceFeature sf = (SequenceFeature) o;
    boolean sameScore = Float.isNaN(score) ? Float.isNaN(sf.score)
            : score == sf.score;
    if (begin != sf.begin || end != sf.end || !sameScore)
    {
      return false;
    }

    if (getStrand() != sf.getStrand())
    {
      return false;
    }

    if (!(type + description + featureGroup + getPhase()).equals(sf.type
            + sf.description + sf.featureGroup + sf.getPhase()))
    {
      return false;
    }
    if (!equalAttribute(getValue("ID"), sf.getValue("ID")))
    {
      return false;
    }
    if (!equalAttribute(getValue("Name"), sf.getValue("Name")))
    {
      return false;
    }
    if (!ignoreParent)
    {
      if (!equalAttribute(getValue("Parent"), sf.getValue("Parent")))
      {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns true if both values are null, are both non-null and equal
   * 
   * @param att1
   * @param att2
   * @return
   */
  protected static boolean equalAttribute(Object att1, Object att2)
  {
    if (att1 == null && att2 == null)
    {
      return true;
    }
    if (att1 != null)
    {
      return att1.equals(att2);
    }
    return att2.equals(att1);
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getBegin()
  {
    return begin;
  }

  public void setBegin(int start)
  {
    this.begin = start;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public int getEnd()
  {
    return end;
  }

  public void setEnd(int end)
  {
    this.end = end;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String getDescription()
  {
    return description;
  }

  public void setDescription(String desc)
  {
    description = desc;
  }

  public String getFeatureGroup()
  {
    return featureGroup;
  }

  public void setFeatureGroup(String featureGroup)
  {
    this.featureGroup = featureGroup;
  }

  public void addLink(String labelLink)
  {
    if (links == null)
    {
      links = new Vector<String>();
    }

    links.insertElementAt(labelLink, 0);
  }

  public float getScore()
  {
    return score;
  }

  public void setScore(float value)
  {
    score = value;
  }

  /**
   * Used for getting values which are not in the basic set. eg STRAND, PHASE
   * for GFF file
   * 
   * @param key
   *          String
   */
  public Object getValue(String key)
  {
    if (otherDetails == null)
    {
      return null;
    }
    else
    {
      return otherDetails.get(key);
    }
  }

  /**
   * Returns a property value for the given key if known, else the specified
   * default value
   * 
   * @param key
   * @param defaultValue
   * @return
   */
  public Object getValue(String key, Object defaultValue)
  {
    Object value = getValue(key);
    return value == null ? defaultValue : value;
  }

  /**
   * Used for setting values which are not in the basic set. eg STRAND, FRAME
   * for GFF file
   * 
   * @param key
   *          eg STRAND
   * @param value
   *          eg +
   */
  public void setValue(String key, Object value)
  {
    if (value != null)
    {
      if (otherDetails == null)
      {
        otherDetails = new HashMap<String, Object>();
      }

      otherDetails.put(key, value);
    }
  }

  /*
   * The following methods are added to maintain the castor Uniprot mapping file
   * for the moment.
   */
  public void setStatus(String status)
  {
    setValue(STATUS, status);
  }

  public String getStatus()
  {
    return (String) getValue(STATUS);
  }

  public void setAttributes(String attr)
  {
    setValue(ATTRIBUTES, attr);
  }

  public String getAttributes()
  {
    return (String) getValue(ATTRIBUTES);
  }

  public void setPosition(int pos)
  {
    begin = pos;
    end = pos;
  }

  public int getPosition()
  {
    return begin;
  }

  /**
   * Return 1 for forward strand ('+' in GFF), -1 for reverse strand ('-' in
   * GFF), and 0 for unknown or not (validly) specified
   * 
   * @return
   */
  public int getStrand()
  {
    int strand = 0;
    if (otherDetails != null)
    {
      Object str = otherDetails.get(STRAND);
      if ("-".equals(str))
      {
        strand = -1;
      }
      else if ("+".equals(str))
      {
        strand = 1;
      }
    }
    return strand;
  }

  /**
   * Set the value of strand
   * 
   * @param strand
   *          should be "+" for forward, or "-" for reverse
   */
  public void setStrand(String strand)
  {
    setValue(STRAND, strand);
  }

  public void setPhase(String phase)
  {
    setValue(PHASE, phase);
  }

  public String getPhase()
  {
    return (String) getValue(PHASE);
  }

  /**
   * Sets the 'raw' ENA format location specifier e.g. join(12..45,89..121)
   * 
   * @param loc
   */
  public void setEnaLocation(String loc)
  {
    setValue(LOCATION, loc);
  }

  /**
   * Gets the 'raw' ENA format location specifier e.g. join(12..45,89..121)
   * 
   * @param loc
   */
  public String getEnaLocation()
  {
    return (String) getValue(LOCATION);
  }

  /**
   * Readable representation, for debug only, not guaranteed not to change
   * between versions
   */
  @Override
  public String toString()
  {
    return String.format("%d %d %s %s", getBegin(), getEnd(), getType(),
            getDescription());
  }

  /**
   * Overridden to ensure that whenever two objects are equal, they have the
   * same hashCode
   */
  @Override
  public int hashCode()
  {
    String s = getType() + getDescription() + getFeatureGroup()
            + getValue("ID") + getValue("Name") + getValue("Parent")
            + getPhase();
    return s.hashCode() + getBegin() + getEnd() + (int) getScore()
            + getStrand();
  }

  /**
   * Answers true if the feature's start/end values represent two related
   * positions, rather than ends of a range. Such features may be visualised or
   * reported differently to features on a range.
   */
  public boolean isContactFeature()
  {
    // TODO abstract one day to a FeatureType class
    if ("disulfide bond".equalsIgnoreCase(type)
            || "disulphide bond".equalsIgnoreCase(type))
    {
      return true;
    }
    return false;
  }
}
