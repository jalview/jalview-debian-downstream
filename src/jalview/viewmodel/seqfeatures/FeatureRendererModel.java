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
package jalview.viewmodel.seqfeatures;

import jalview.api.AlignViewportI;
import jalview.api.FeatureColourI;
import jalview.api.FeaturesDisplayedI;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.renderer.seqfeatures.FeatureRenderer;
import jalview.schemes.FeatureColour;
import jalview.schemes.UserColourScheme;

import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FeatureRendererModel implements
        jalview.api.FeatureRenderer
{

  /**
   * global transparency for feature
   */
  protected float transparency = 1.0f;

  protected Map<String, FeatureColourI> featureColours = new ConcurrentHashMap<String, FeatureColourI>();

  protected Map<String, Boolean> featureGroups = new ConcurrentHashMap<String, Boolean>();

  protected String[] renderOrder;

  Map<String, Float> featureOrder = null;

  protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(
          this);

  protected AlignViewportI av;

  @Override
  public AlignViewportI getViewport()
  {
    return av;
  }

  public FeatureRendererSettings getSettings()
  {
    return new FeatureRendererSettings(this);
  }

  public void transferSettings(FeatureRendererSettings fr)
  {
    this.renderOrder = fr.renderOrder;
    this.featureGroups = fr.featureGroups;
    this.featureColours = fr.featureColours;
    this.transparency = fr.transparency;
    this.featureOrder = fr.featureOrder;
  }

  /**
   * update from another feature renderer
   * 
   * @param fr
   *          settings to copy
   */
  public void transferSettings(jalview.api.FeatureRenderer _fr)
  {
    FeatureRenderer fr = (FeatureRenderer) _fr;
    FeatureRendererSettings frs = new FeatureRendererSettings(fr);
    this.renderOrder = frs.renderOrder;
    this.featureGroups = frs.featureGroups;
    this.featureColours = frs.featureColours;
    this.transparency = frs.transparency;
    this.featureOrder = frs.featureOrder;
    if (av != null && av != fr.getViewport())
    {
      // copy over the displayed feature settings
      if (_fr.getFeaturesDisplayed() != null)
      {
        FeaturesDisplayedI fd = getFeaturesDisplayed();
        if (fd == null)
        {
          setFeaturesDisplayedFrom(_fr.getFeaturesDisplayed());
        }
        else
        {
          synchronized (fd)
          {
            fd.clear();
            java.util.Iterator<String> fdisp = _fr.getFeaturesDisplayed()
                    .getVisibleFeatures();
            while (fdisp.hasNext())
            {
              fd.setVisible(fdisp.next());
            }
          }
        }
      }
    }
  }

  public void setFeaturesDisplayedFrom(FeaturesDisplayedI featuresDisplayed)
  {
    av.setFeaturesDisplayed(new FeaturesDisplayed(featuresDisplayed));
  }

  @Override
  public void setVisible(String featureType)
  {
    FeaturesDisplayedI fdi = av.getFeaturesDisplayed();
    if (fdi == null)
    {
      av.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
    }
    if (!fdi.isRegistered(featureType))
    {
      pushFeatureType(Arrays.asList(new String[] { featureType }));
    }
    fdi.setVisible(featureType);
  }

  @Override
  public void setAllVisible(List<String> featureTypes)
  {
    FeaturesDisplayedI fdi = av.getFeaturesDisplayed();
    if (fdi == null)
    {
      av.setFeaturesDisplayed(fdi = new FeaturesDisplayed());
    }
    List<String> nft = new ArrayList<String>();
    for (String featureType : featureTypes)
    {
      if (!fdi.isRegistered(featureType))
      {
        nft.add(featureType);
      }
    }
    if (nft.size() > 0)
    {
      pushFeatureType(nft);
    }
    fdi.setAllVisible(featureTypes);
  }

  /**
   * push a set of new types onto the render order stack. Note - this is a
   * direct mechanism rather than the one employed in updateRenderOrder
   * 
   * @param types
   */
  private void pushFeatureType(List<String> types)
  {

    int ts = types.size();
    String neworder[] = new String[(renderOrder == null ? 0
            : renderOrder.length) + ts];
    types.toArray(neworder);
    if (renderOrder != null)
    {
      System.arraycopy(neworder, 0, neworder, renderOrder.length, ts);
      System.arraycopy(renderOrder, 0, neworder, 0, renderOrder.length);
    }
    renderOrder = neworder;
  }

  protected Map<String, float[][]> minmax = new Hashtable<String, float[][]>();

  public Map<String, float[][]> getMinMax()
  {
    return minmax;
  }

  /**
   * normalise a score against the max/min bounds for the feature type.
   * 
   * @param sequenceFeature
   * @return byte[] { signed, normalised signed (-127 to 127) or unsigned
   *         (0-255) value.
   */
  protected final byte[] normaliseScore(SequenceFeature sequenceFeature)
  {
    float[] mm = minmax.get(sequenceFeature.type)[0];
    final byte[] r = new byte[] { 0, (byte) 255 };
    if (mm != null)
    {
      if (r[0] != 0 || mm[0] < 0.0)
      {
        r[0] = 1;
        r[1] = (byte) ((int) 128.0 + 127.0 * (sequenceFeature.score / mm[1]));
      }
      else
      {
        r[1] = (byte) ((int) 255.0 * (sequenceFeature.score / mm[1]));
      }
    }
    return r;
  }

  boolean newFeatureAdded = false;

  boolean findingFeatures = false;

  protected boolean updateFeatures()
  {
    if (av.getFeaturesDisplayed() == null || renderOrder == null
            || newFeatureAdded)
    {
      findAllFeatures();
      if (av.getFeaturesDisplayed().getVisibleFeatureCount() < 1)
      {
        return false;
      }
    }
    // TODO: decide if we should check for the visible feature count first
    return true;
  }

  /**
   * search the alignment for all new features, give them a colour and display
   * them. Then fires a PropertyChangeEvent on the changeSupport object.
   * 
   */
  protected void findAllFeatures()
  {
    synchronized (firing)
    {
      if (firing.equals(Boolean.FALSE))
      {
        firing = Boolean.TRUE;
        findAllFeatures(true); // add all new features as visible
        changeSupport.firePropertyChange("changeSupport", null, null);
        firing = Boolean.FALSE;
      }
    }
  }

  @Override
  public List<SequenceFeature> findFeaturesAtRes(SequenceI sequence, int res)
  {
    ArrayList<SequenceFeature> tmp = new ArrayList<SequenceFeature>();
    SequenceFeature[] features = sequence.getSequenceFeatures();

    if (features != null)
    {
      for (int i = 0; i < features.length; i++)
      {
        if (!av.areFeaturesDisplayed()
                || !av.getFeaturesDisplayed().isVisible(
                        features[i].getType()))
        {
          continue;
        }

        if (features[i].featureGroup != null
                && featureGroups != null
                && featureGroups.containsKey(features[i].featureGroup)
                && !featureGroups.get(features[i].featureGroup)
                        .booleanValue())
        {
          continue;
        }

        // check if start/end are at res, and if not a contact feature, that res
        // lies between start and end
        if ((features[i].getBegin() == res || features[i].getEnd() == res)
                || (!features[i].isContactFeature()
                        && (features[i].getBegin() < res) && (features[i]
                        .getEnd() >= res)))
        {
          tmp.add(features[i]);
        }
      }
    }
    return tmp;
  }

  /**
   * Searches alignment for all features and updates colours
   * 
   * @param newMadeVisible
   *          if true newly added feature types will be rendered immediatly
   *          TODO: check to see if this method should actually be proxied so
   *          repaint events can be propagated by the renderer code
   */
  @Override
  public synchronized void findAllFeatures(boolean newMadeVisible)
  {
    newFeatureAdded = false;

    if (findingFeatures)
    {
      newFeatureAdded = true;
      return;
    }

    findingFeatures = true;
    if (av.getFeaturesDisplayed() == null)
    {
      av.setFeaturesDisplayed(new FeaturesDisplayed());
    }
    FeaturesDisplayedI featuresDisplayed = av.getFeaturesDisplayed();

    ArrayList<String> allfeatures = new ArrayList<String>();
    ArrayList<String> oldfeatures = new ArrayList<String>();
    if (renderOrder != null)
    {
      for (int i = 0; i < renderOrder.length; i++)
      {
        if (renderOrder[i] != null)
        {
          oldfeatures.add(renderOrder[i]);
        }
      }
    }
    if (minmax == null)
    {
      minmax = new Hashtable<String, float[][]>();
    }
    AlignmentI alignment = av.getAlignment();
    for (int i = 0; i < alignment.getHeight(); i++)
    {
      SequenceI asq = alignment.getSequenceAt(i);
      SequenceFeature[] features = asq.getSequenceFeatures();

      if (features == null)
      {
        continue;
      }

      int index = 0;
      while (index < features.length)
      {
        if (!featuresDisplayed.isRegistered(features[index].getType()))
        {
          String fgrp = features[index].getFeatureGroup();
          if (fgrp != null)
          {
            Boolean groupDisplayed = featureGroups.get(fgrp);
            if (groupDisplayed == null)
            {
              groupDisplayed = Boolean.valueOf(newMadeVisible);
              featureGroups.put(fgrp, groupDisplayed);
            }
            if (!groupDisplayed.booleanValue())
            {
              index++;
              continue;
            }
          }
          if (!(features[index].begin == 0 && features[index].end == 0))
          {
            // If beginning and end are 0, the feature is for the whole sequence
            // and we don't want to render the feature in the normal way

            if (newMadeVisible
                    && !oldfeatures.contains(features[index].getType()))
            {
              // this is a new feature type on the alignment. Mark it for
              // display.
              featuresDisplayed.setVisible(features[index].getType());
              setOrder(features[index].getType(), 0);
            }
          }
        }
        if (!allfeatures.contains(features[index].getType()))
        {
          allfeatures.add(features[index].getType());
        }
        if (!Float.isNaN(features[index].score))
        {
          int nonpos = features[index].getBegin() >= 1 ? 0 : 1;
          float[][] mm = minmax.get(features[index].getType());
          if (mm == null)
          {
            mm = new float[][] { null, null };
            minmax.put(features[index].getType(), mm);
          }
          if (mm[nonpos] == null)
          {
            mm[nonpos] = new float[] { features[index].score,
                features[index].score };

          }
          else
          {
            if (mm[nonpos][0] > features[index].score)
            {
              mm[nonpos][0] = features[index].score;
            }
            if (mm[nonpos][1] < features[index].score)
            {
              mm[nonpos][1] = features[index].score;
            }
          }
        }
        index++;
      }
    }
    updateRenderOrder(allfeatures);
    findingFeatures = false;
  }

  protected Boolean firing = Boolean.FALSE;

  /**
   * replaces the current renderOrder with the unordered features in
   * allfeatures. The ordering of any types in both renderOrder and allfeatures
   * is preserved, and all new feature types are rendered on top of the existing
   * types, in the order given by getOrder or the order given in allFeatures.
   * Note. this operates directly on the featureOrder hash for efficiency. TODO:
   * eliminate the float storage for computing/recalling the persistent ordering
   * New Cability: updates min/max for colourscheme range if its dynamic
   * 
   * @param allFeatures
   */
  private void updateRenderOrder(List<String> allFeatures)
  {
    List<String> allfeatures = new ArrayList<String>(allFeatures);
    String[] oldRender = renderOrder;
    renderOrder = new String[allfeatures.size()];
    boolean initOrders = (featureOrder == null);
    int opos = 0;
    if (oldRender != null && oldRender.length > 0)
    {
      for (int j = 0; j < oldRender.length; j++)
      {
        if (oldRender[j] != null)
        {
          if (initOrders)
          {
            setOrder(oldRender[j], (1 - (1 + (float) j) / oldRender.length));
          }
          if (allfeatures.contains(oldRender[j]))
          {
            renderOrder[opos++] = oldRender[j]; // existing features always
            // appear below new features
            allfeatures.remove(oldRender[j]);
            if (minmax != null)
            {
              float[][] mmrange = minmax.get(oldRender[j]);
              if (mmrange != null)
              {
                FeatureColourI fc = featureColours.get(oldRender[j]);
                if (fc != null && !fc.isSimpleColour() && fc.isAutoScaled())
                {
                  fc.updateBounds(mmrange[0][0], mmrange[0][1]);
                }
              }
            }
          }
        }
      }
    }
    if (allfeatures.size() == 0)
    {
      // no new features - leave order unchanged.
      return;
    }
    int i = allfeatures.size() - 1;
    int iSize = i;
    boolean sort = false;
    String[] newf = new String[allfeatures.size()];
    float[] sortOrder = new float[allfeatures.size()];
    for (String newfeat : allfeatures)
    {
      newf[i] = newfeat;
      if (minmax != null)
      {
        // update from new features minmax if necessary
        float[][] mmrange = minmax.get(newf[i]);
        if (mmrange != null)
        {
          FeatureColourI fc = featureColours.get(newf[i]);
          if (fc != null && !fc.isSimpleColour() && fc.isAutoScaled())
          {
            fc.updateBounds(mmrange[0][0], mmrange[0][1]);
          }
        }
      }
      if (initOrders || !featureOrder.containsKey(newf[i]))
      {
        int denom = initOrders ? allfeatures.size() : featureOrder.size();
        // new unordered feature - compute persistent ordering at head of
        // existing features.
        setOrder(newf[i], i / (float) denom);
      }
      // set order from newly found feature from persisted ordering.
      sortOrder[i] = 2 - featureOrder.get(newf[i]).floatValue();
      if (i < iSize)
      {
        // only sort if we need to
        sort = sort || sortOrder[i] > sortOrder[i + 1];
      }
      i--;
    }
    if (iSize > 1 && sort)
    {
      jalview.util.QuickSort.sort(sortOrder, newf);
    }
    sortOrder = null;
    System.arraycopy(newf, 0, renderOrder, opos, newf.length);
  }

  /**
   * get a feature style object for the given type string. Creates a
   * java.awt.Color for a featureType with no existing colourscheme.
   * 
   * @param featureType
   * @return
   */
  @Override
  public FeatureColourI getFeatureStyle(String featureType)
  {
    FeatureColourI fc = featureColours.get(featureType);
    if (fc == null)
    {
      Color col = UserColourScheme.createColourFromName(featureType);
      fc = new FeatureColour(col);
      featureColours.put(featureType, fc);
    }
    return fc;
  }

  /**
   * calculate the render colour for a specific feature using current feature
   * settings.
   * 
   * @param feature
   * @return render colour for the given feature
   */
  public Color getColour(SequenceFeature feature)
  {
    FeatureColourI fc = getFeatureStyle(feature.getType());
    return fc.getColor(feature);
  }

  protected boolean showFeature(SequenceFeature sequenceFeature)
  {
    FeatureColourI fc = getFeatureStyle(sequenceFeature.type);
    return fc.isColored(sequenceFeature);
  }

  /**
   * Answers true if the feature type is currently selected to be displayed,
   * else false
   * 
   * @param type
   * @return
   */
  protected boolean showFeatureOfType(String type)
  {
    return type == null ? false : av.getFeaturesDisplayed().isVisible(type);
  }

  @Override
  public void setColour(String featureType, FeatureColourI col)
  {
    featureColours.put(featureType, col);
  }

  public void setTransparency(float value)
  {
    transparency = value;
  }

  public float getTransparency()
  {
    return transparency;
  }

  /**
   * analogous to colour - store a normalized ordering for all feature types in
   * this rendering context.
   * 
   * @param type
   *          Feature type string
   * @param position
   *          normalized priority - 0 means always appears on top, 1 means
   *          always last.
   */
  public float setOrder(String type, float position)
  {
    if (featureOrder == null)
    {
      featureOrder = new Hashtable<String, Float>();
    }
    featureOrder.put(type, new Float(position));
    return position;
  }

  /**
   * get the global priority (0 (top) to 1 (bottom))
   * 
   * @param type
   * @return [0,1] or -1 for a type without a priority
   */
  public float getOrder(String type)
  {
    if (featureOrder != null)
    {
      if (featureOrder.containsKey(type))
      {
        return featureOrder.get(type).floatValue();
      }
    }
    return -1;
  }

  @Override
  public Map<String, FeatureColourI> getFeatureColours()
  {
    return featureColours;
  }

  /**
   * Replace current ordering with new ordering
   * 
   * @param data
   *          { String(Type), Colour(Type), Boolean(Displayed) }
   * @return true if any visible features have been reordered, else false
   */
  public boolean setFeaturePriority(Object[][] data)
  {
    return setFeaturePriority(data, true);
  }

  /**
   * Sets the priority order for features
   * 
   * @param data
   *          { String(Type), Colour(Type), Boolean(Displayed) }
   * @param visibleNew
   *          when true current featureDisplay list will be cleared
   * @return true if any visible features have been reordered or recoloured,
   *         else false (i.e. no need to repaint)
   */
  public boolean setFeaturePriority(Object[][] data, boolean visibleNew)
  {
    /*
     * note visible feature ordering and colours before update
     */
    List<String> visibleFeatures = getDisplayedFeatureTypes();
    Map<String, FeatureColourI> visibleColours = new HashMap<String, FeatureColourI>(
            getFeatureColours());

    FeaturesDisplayedI av_featuresdisplayed = null;
    if (visibleNew)
    {
      if ((av_featuresdisplayed = av.getFeaturesDisplayed()) != null)
      {
        av.getFeaturesDisplayed().clear();
      }
      else
      {
        av.setFeaturesDisplayed(av_featuresdisplayed = new FeaturesDisplayed());
      }
    }
    else
    {
      av_featuresdisplayed = av.getFeaturesDisplayed();
    }
    if (data == null)
    {
      return false;
    }
    // The feature table will display high priority
    // features at the top, but these are the ones
    // we need to render last, so invert the data
    renderOrder = new String[data.length];

    if (data.length > 0)
    {
      for (int i = 0; i < data.length; i++)
      {
        String type = data[i][0].toString();
        setColour(type, (FeatureColourI) data[i][1]);
        if (((Boolean) data[i][2]).booleanValue())
        {
          av_featuresdisplayed.setVisible(type);
        }

        renderOrder[data.length - i - 1] = type;
      }
    }

    /*
     * get the new visible ordering and return true if it has changed
     * order or any colour has changed
     */
    List<String> reorderedVisibleFeatures = getDisplayedFeatureTypes();
    if (!visibleFeatures.equals(reorderedVisibleFeatures))
    {
      /*
       * the list of ordered visible features has changed
       */
      return true;
    }

    /*
     * return true if any feature colour has changed
     */
    for (String feature : visibleFeatures)
    {
      if (visibleColours.get(feature) != getFeatureStyle(feature))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#addPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addPropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * @param listener
   * @see java.beans.PropertyChangeSupport#removePropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void removePropertyChangeListener(PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  public Set<String> getAllFeatureColours()
  {
    return featureColours.keySet();
  }

  public void clearRenderOrder()
  {
    renderOrder = null;
  }

  public boolean hasRenderOrder()
  {
    return renderOrder != null;
  }

  /**
   * Returns feature types in ordering of rendering, where last means on top
   */
  public List<String> getRenderOrder()
  {
    if (renderOrder == null)
    {
      return Arrays.asList(new String[] {});
    }
    return Arrays.asList(renderOrder);
  }

  public int getFeatureGroupsSize()
  {
    return featureGroups != null ? 0 : featureGroups.size();
  }

  @Override
  public List<String> getFeatureGroups()
  {
    // conflict between applet and desktop - featureGroups returns the map in
    // the desktop featureRenderer
    return (featureGroups == null) ? Arrays.asList(new String[0]) : Arrays
            .asList(featureGroups.keySet().toArray(new String[0]));
  }

  public boolean checkGroupVisibility(String group, boolean newGroupsVisible)
  {
    if (featureGroups == null)
    {
      // then an exception happens next..
    }
    if (featureGroups.containsKey(group))
    {
      return featureGroups.get(group).booleanValue();
    }
    if (newGroupsVisible)
    {
      featureGroups.put(group, new Boolean(true));
      return true;
    }
    return false;
  }

  /**
   * get visible or invisible groups
   * 
   * @param visible
   *          true to return visible groups, false to return hidden ones.
   * @return list of groups
   */
  @Override
  public List getGroups(boolean visible)
  {
    if (featureGroups != null)
    {
      List<String> gp = new ArrayList<String>();

      for (String grp : featureGroups.keySet())
      {
        Boolean state = featureGroups.get(grp);
        if (state.booleanValue() == visible)
        {
          gp.add(grp);
        }
      }
      return gp;
    }
    return null;
  }

  @Override
  public void setGroupVisibility(String group, boolean visible)
  {
    featureGroups.put(group, new Boolean(visible));
  }

  @Override
  public void setGroupVisibility(List<String> toset, boolean visible)
  {
    if (toset != null && toset.size() > 0 && featureGroups != null)
    {
      boolean rdrw = false;
      for (String gst : toset)
      {
        Boolean st = featureGroups.get(gst);
        featureGroups.put(gst, new Boolean(visible));
        if (st != null)
        {
          rdrw = rdrw || (visible != st.booleanValue());
        }
      }
      if (rdrw)
      {
        // set local flag indicating redraw needed ?
      }
    }
  }

  @Override
  public Map<String, FeatureColourI> getDisplayedFeatureCols()
  {
    Map<String, FeatureColourI> fcols = new Hashtable<String, FeatureColourI>();
    if (getViewport().getFeaturesDisplayed() == null)
    {
      return fcols;
    }
    Iterator<String> features = getViewport().getFeaturesDisplayed()
            .getVisibleFeatures();
    while (features.hasNext())
    {
      String feature = features.next();
      fcols.put(feature, getFeatureStyle(feature));
    }
    return fcols;
  }

  @Override
  public FeaturesDisplayedI getFeaturesDisplayed()
  {
    return av.getFeaturesDisplayed();
  }

  /**
   * Returns a (possibly empty) list of visible feature types, in render order
   * (last is on top)
   */
  @Override
  public List<String> getDisplayedFeatureTypes()
  {
    List<String> typ = getRenderOrder();
    List<String> displayed = new ArrayList<String>();
    FeaturesDisplayedI feature_disp = av.getFeaturesDisplayed();
    if (feature_disp != null)
    {
      synchronized (feature_disp)
      {
        for (String type : typ)
        {
          if (feature_disp.isVisible(type))
          {
            displayed.add(type);
          }
        }
      }
    }
    return displayed;
  }

  @Override
  public List<String> getDisplayedFeatureGroups()
  {
    List<String> _gps = new ArrayList<String>();
    boolean valid = false;
    for (String gp : getFeatureGroups())
    {
      if (checkGroupVisibility(gp, false))
      {
        valid = true;
        _gps.add(gp);
      }
      if (!valid)
      {
        return null;
      }
      else
      {
        // gps = new String[_gps.size()];
        // _gps.toArray(gps);
      }
    }
    return _gps;
  }

}
