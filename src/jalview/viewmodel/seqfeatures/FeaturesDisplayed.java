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

import jalview.api.FeaturesDisplayedI;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class FeaturesDisplayed implements FeaturesDisplayedI
{
  private HashSet<String> featuresDisplayed = new HashSet<String>();

  private HashSet<String> featuresRegistered = new HashSet<String>();

  public FeaturesDisplayed(FeaturesDisplayedI featuresDisplayed2)
  {
    Iterator<String> fdisp = featuresDisplayed2.getVisibleFeatures();
    String ftype;
    while (fdisp.hasNext())
    {
      ftype = fdisp.next();
      featuresDisplayed.add(ftype);
      featuresRegistered.add(ftype);
    }
  }

  public FeaturesDisplayed()
  {
    // TODO Auto-generated constructor stub
  }

  @Override
  public Iterator<String> getVisibleFeatures()
  {
    return featuresDisplayed.iterator();
  }

  @Override
  public boolean isVisible(String featureType)
  {
    return featuresDisplayed.contains(featureType);
  }

  @Override
  public boolean areVisible(Collection featureTypes)
  {
    return featuresDisplayed.containsAll(featureTypes);
  }

  @Override
  public void clear()
  {
    featuresDisplayed.clear();
    featuresRegistered.clear();
  }

  @Override
  public void setAllVisible(Collection makeVisible)
  {
    featuresDisplayed.addAll(makeVisible);
    featuresRegistered.addAll(makeVisible);
  }

  @Override
  public void setAllRegisteredVisible()
  {
    featuresDisplayed.addAll(featuresRegistered);
  }

  @Override
  public void setVisible(String featureType)
  {
    featuresDisplayed.add(featureType);
    featuresRegistered.add(featureType);
  }

  @Override
  public boolean isRegistered(String type)
  {
    return featuresRegistered.contains(type);
  }

  @Override
  public int getVisibleFeatureCount()
  {
    return featuresDisplayed.size();
  }

  @Override
  public int getRegisterdFeaturesCount()
  {
    return featuresRegistered.size();
  }
}
