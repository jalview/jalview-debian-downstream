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

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.util.UrlLink;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * generate HTML reports for a sequence
 * 
 * @author jimp
 */
public class SequenceAnnotationReport
{
  final String linkImageURL;

  public SequenceAnnotationReport(String linkImageURL)
  {
    this.linkImageURL = linkImageURL;
  }

  /**
   * appends the features at rpos to the given stringbuffer ready for display in
   * a tooltip
   * 
   * @param tooltipText2
   * @param linkImageURL
   * @param rpos
   * @param features
   *          TODO refactor to Jalview 'utilities' somehow.
   */
  public void appendFeatures(final StringBuffer tooltipText2, int rpos,
          List<SequenceFeature> features)
  {
    appendFeatures(tooltipText2, rpos, features, null);
  }

  public void appendFeatures(final StringBuffer tooltipText2, int rpos,
          List<SequenceFeature> features, Hashtable minmax)
  {
    String tmpString;
    if (features != null)
    {
      for (SequenceFeature feature : features)
      {
        if (feature.getType().equals("disulfide bond"))
        {
          if (feature.getBegin() == rpos || feature.getEnd() == rpos)
          {
            if (tooltipText2.length() > 6)
            {
              tooltipText2.append("<br>");
            }
            tooltipText2.append("disulfide bond " + feature.getBegin()
                    + ":" + feature.getEnd());
          }
        }
        else
        {
          if (tooltipText2.length() > 6)
          {
            tooltipText2.append("<br>");
          }
          // TODO: remove this hack to display link only features
          boolean linkOnly = feature.getValue("linkonly") != null;
          if (!linkOnly)
          {
            tooltipText2.append(feature.getType() + " ");
            if (rpos != 0)
            {
              // we are marking a positional feature
              tooltipText2.append(feature.begin);
            }
            if (feature.begin != feature.end)
            {
              tooltipText2.append(" " + feature.end);
            }

            if (feature.getDescription() != null
                    && !feature.description.equals(feature.getType()))
            {
              tmpString = feature.getDescription();
              String tmp2up = tmpString.toUpperCase();
              int startTag = tmp2up.indexOf("<HTML>");
              if (startTag > -1)
              {
                tmpString = tmpString.substring(startTag + 6);
                tmp2up = tmp2up.substring(startTag + 6);
              }
              int endTag = tmp2up.indexOf("</BODY>");
              if (endTag > -1)
              {
                tmpString = tmpString.substring(0, endTag);
                tmp2up = tmp2up.substring(0, endTag);
              }
              endTag = tmp2up.indexOf("</HTML>");
              if (endTag > -1)
              {
                tmpString = tmpString.substring(0, endTag);
              }

              if (startTag > -1)
              {
                tooltipText2.append("; " + tmpString);
              }
              else
              {
                if (tmpString.indexOf("<") > -1
                        || tmpString.indexOf(">") > -1)
                {
                  // The description does not specify html is to
                  // be used, so we must remove < > symbols
                  tmpString = tmpString.replaceAll("<", "&lt;");
                  tmpString = tmpString.replaceAll(">", "&gt;");

                  tooltipText2.append("; ");
                  tooltipText2.append(tmpString);

                }
                else
                {
                  tooltipText2.append("; " + tmpString);
                }
              }
            }
            // check score should be shown
            if (!Float.isNaN(feature.getScore()))
            {
              float[][] rng = (minmax == null) ? null : ((float[][]) minmax
                      .get(feature.getType()));
              if (rng != null && rng[0] != null && rng[0][0] != rng[0][1])
              {
                tooltipText2.append(" Score=" + feature.getScore());
              }
            }
            if (feature.getValue("status") != null)
            {
              String status = feature.getValue("status").toString();
              if (status.length() > 0)
              {
                tooltipText2.append("; (" + feature.getValue("status")
                        + ")");
              }
            }
          }
        }
        if (feature.links != null)
        {
          if (linkImageURL != null)
          {
            tooltipText2.append(" <img src=\"" + linkImageURL + "\">");
          }
          else
          {
            for (String urlstring : feature.links)
            {
              try
              {
                for (String[] urllink : createLinksFrom(null, urlstring))
                {
                  tooltipText2.append("<br/> <a href=\""
                          + urllink[3]
                          + "\" target=\""
                          + urllink[0]
                          + "\">"
                          + (urllink[0].toLowerCase().equals(
                                  urllink[1].toLowerCase()) ? urllink[0]
                                  : (urllink[0] + ":" + urllink[1]))
                          + "</a></br>");
                }
              } catch (Exception x)
              {
                System.err.println("problem when creating links from "
                        + urlstring);
                x.printStackTrace();
              }
            }
          }

        }
      }
    }
  }

  /**
   * 
   * @param seq
   * @param link
   * @return String[][] { String[] { link target, link label, dynamic component
   *         inserted (if any), url }}
   */
  public String[][] createLinksFrom(SequenceI seq, String link)
  {
    ArrayList<String[]> urlSets = new ArrayList<String[]>();
    ArrayList<String> uniques = new ArrayList<String>();
    UrlLink urlLink = new UrlLink(link);
    if (!urlLink.isValid())
    {
      System.err.println(urlLink.getInvalidMessage());
      return null;
    }
    final String target = urlLink.getTarget(); // link.substring(0,
    // link.indexOf("|"));
    final String label = urlLink.getLabel();
    if (seq != null && urlLink.isDynamic())
    {

      // collect matching db-refs
      DBRefEntry[] dbr = jalview.util.DBRefUtils.selectRefs(seq.getDBRef(),
              new String[] { target });
      // collect id string too
      String id = seq.getName();
      String descr = seq.getDescription();
      if (descr != null && descr.length() < 1)
      {
        descr = null;
      }
      if (dbr != null)
      {
        for (int r = 0; r < dbr.length; r++)
        {
          if (id != null && dbr[r].getAccessionId().equals(id))
          {
            // suppress duplicate link creation for the bare sequence ID
            // string with this link
            id = null;
          }
          // create Bare ID link for this RUL
          String[] urls = urlLink.makeUrls(dbr[r].getAccessionId(), true);
          if (urls != null)
          {
            for (int u = 0; u < urls.length; u += 2)
            {
              String unq = urls[u] + "|" + urls[u + 1];
              if (!uniques.contains(unq))
              {
                urlSets.add(new String[] { target, label, urls[u],
                    urls[u + 1] });
                uniques.add(unq);
              }
            }
          }
        }
      }
      if (id != null)
      {
        // create Bare ID link for this RUL
        String[] urls = urlLink.makeUrls(id, true);
        if (urls != null)
        {
          for (int u = 0; u < urls.length; u += 2)
          {
            String unq = urls[u] + "|" + urls[u + 1];
            if (!uniques.contains(unq))
            {
              urlSets.add(new String[] { target, label, urls[u],
                  urls[u + 1] });
              uniques.add(unq);
            }
          }
        }
      }
      if (descr != null && urlLink.getRegexReplace() != null)
      {
        // create link for this URL from description only if regex matches
        String[] urls = urlLink.makeUrls(descr, true);
        if (urls != null)
        {
          for (int u = 0; u < urls.length; u += 2)
          {
            String unq = urls[u] + "|" + urls[u + 1];
            if (!uniques.contains(unq))
            {
              urlSets.add(new String[] { target, label, urls[u],
                  urls[u + 1] });
              uniques.add(unq);
            }
          }
        }
      }

    }
    else
    {
      String unq = label + "|" + urlLink.getUrl_prefix();
      if (!uniques.contains(unq))
      {
        uniques.add(unq);
        // Add a non-dynamic link
        urlSets.add(new String[] { target, label, null,
            urlLink.getUrl_prefix() });
      }
    }

    return urlSets.toArray(new String[][] {});
  }

  public void createSequenceAnnotationReport(final StringBuffer tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          Hashtable minmax)
  {
    createSequenceAnnotationReport(tip, sequence, showDbRefs, showNpFeats,
            true, minmax);
  }

  public void createSequenceAnnotationReport(final StringBuffer tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          boolean tableWrap, Hashtable minmax)
  {
    String tmp;
    tip.append("<i>");

    int maxWidth = 0;
    if (sequence.getDescription() != null)
    {
      tmp = sequence.getDescription();
      tip.append("<br>" + tmp);
      maxWidth = Math.max(maxWidth, tmp.length());
    }
    SequenceI ds = sequence;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }
    DBRefEntry[] dbrefs = ds.getDBRef();
    if (showDbRefs && dbrefs != null)
    {
      for (int i = 0; i < dbrefs.length; i++)
      {
        tip.append("<br>");
        tmp = dbrefs[i].getSource() + " " + dbrefs[i].getAccessionId();
        tip.append(tmp);
        maxWidth = Math.max(maxWidth, tmp.length());
      }
    }

    // ADD NON POSITIONAL SEQUENCE INFO
    SequenceFeature[] features = sequence.getSequenceFeatures();
    if (showNpFeats && features != null)
    {
      for (int i = 0; i < features.length; i++)
      {
        if (features[i].begin == 0 && features[i].end == 0)
        {
          int sz = -tip.length();
          List<SequenceFeature> tfeat = new ArrayList<SequenceFeature>();
          tfeat.add(features[i]);
          appendFeatures(tip, 0, tfeat, minmax);
          sz += tip.length();
          maxWidth = Math.max(maxWidth, sz);
        }
      }
    }

    if (tableWrap && maxWidth > 60)
    {
      tip.insert(0, "<table width=350 border=0><tr><td><i>");
      tip.append("</i></td></tr></table>");
    }

  }
}
