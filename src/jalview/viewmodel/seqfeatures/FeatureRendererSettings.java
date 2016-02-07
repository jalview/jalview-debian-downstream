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
package jalview.viewmodel.seqfeatures;

import jalview.schemes.GraduatedColor;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeatureRendererSettings implements Cloneable
{
  String[] renderOrder;

  Map featureGroups;

  Map featureColours;

  float transparency;

  Map featureOrder;

  public FeatureRendererSettings(String[] renderOrder,
          Hashtable featureGroups, Hashtable featureColours,
          float transparency, Hashtable featureOrder)
  {
    super();
    this.renderOrder = Arrays.copyOf(renderOrder, renderOrder.length);
    this.featureGroups = new ConcurrentHashMap(featureGroups);
    this.featureColours = new ConcurrentHashMap(featureColours);
    this.transparency = transparency;
    this.featureOrder = new ConcurrentHashMap(featureOrder);
  }

  /**
   * create an independent instance of the feature renderer settings
   * 
   * @param fr
   */
  public FeatureRendererSettings(
          jalview.viewmodel.seqfeatures.FeatureRendererModel fr)
  {
    renderOrder = null;
    featureGroups = new ConcurrentHashMap();
    featureColours = new ConcurrentHashMap();
    featureOrder = new ConcurrentHashMap();
    if (fr.renderOrder != null)
    {
      this.renderOrder = new String[fr.renderOrder.length];
      System.arraycopy(fr.renderOrder, 0, renderOrder, 0,
              fr.renderOrder.length);
    }
    if (fr.featureGroups != null)
    {
      this.featureGroups = new ConcurrentHashMap(fr.featureGroups);
    }
    if (fr.featureColours != null)
    {
      this.featureColours = new ConcurrentHashMap(fr.featureColours);
    }
    Iterator en = fr.featureColours.keySet().iterator();
    while (en.hasNext())
    {
      Object next = en.next();
      Object val = featureColours.get(next);
      if (val instanceof GraduatedColor)
      {
        featureColours.put(next, new GraduatedColor((GraduatedColor) val));
      }
    }
    this.transparency = fr.transparency;
    if (fr.featureOrder != null)
    {
      this.featureOrder = new ConcurrentHashMap(fr.featureOrder);
    }
  }
}
