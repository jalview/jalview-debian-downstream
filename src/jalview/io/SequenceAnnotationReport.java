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
package jalview.io;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.DBRefSource;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceI;
import jalview.io.gff.GffConstants;
import jalview.util.MessageManager;
import jalview.util.UrlLink;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * generate HTML reports for a sequence
 * 
 * @author jimp
 */
public class SequenceAnnotationReport
{
  private static final String COMMA = ",";

  private static final String ELLIPSIS = "...";

  private static final int MAX_REFS_PER_SOURCE = 4;

  private static final int MAX_SOURCES = 40;

  private static final String[][] PRIMARY_SOURCES = new String[][] {
      DBRefSource.CODINGDBS, DBRefSource.DNACODINGDBS,
      DBRefSource.PROTEINDBS };

  final String linkImageURL;

  /*
   * Comparator to order DBRefEntry by Source + accession id (case-insensitive)
   */
  private static Comparator<DBRefEntry> comparator = new Comparator<DBRefEntry>()
  {

    @Override
    public int compare(DBRefEntry ref1, DBRefEntry ref2)
    {
      String s1 = ref1.getSource();
      String s2 = ref2.getSource();
      boolean s1Primary = isPrimarySource(s1);
      boolean s2Primary = isPrimarySource(s2);
      if (s1Primary && !s2Primary)
      {
        return -1;
      }
      if (!s1Primary && s2Primary)
      {
        return 1;
      }
      int comp = s1 == null ? -1 : (s2 == null ? 1 : s1
              .compareToIgnoreCase(s2));
      if (comp == 0)
      {
        String a1 = ref1.getAccessionId();
        String a2 = ref2.getAccessionId();
        comp = a1 == null ? -1 : (a2 == null ? 1 : a1
                .compareToIgnoreCase(a2));
      }
      return comp;
    }

    private boolean isPrimarySource(String source)
    {
      for (String[] primary : PRIMARY_SOURCES)
      {
        for (String s : primary)
        {
          if (source.equals(s))
          {
            return true;
          }
        }
      }
      return false;
    }
  };

  public SequenceAnnotationReport(String linkImageURL)
  {
    this.linkImageURL = linkImageURL;
  }

  /**
   * Append text for the list of features to the tooltip
   * 
   * @param sb
   * @param rpos
   * @param features
   * @param minmax
   */
  public void appendFeatures(final StringBuilder sb, int rpos,
          List<SequenceFeature> features, Map<String, float[][]> minmax)
  {
    if (features != null)
    {
      for (SequenceFeature feature : features)
      {
        appendFeature(sb, rpos, minmax, feature);
      }
    }
  }

  /**
   * Appends the feature at rpos to the given buffer
   * 
   * @param sb
   * @param rpos
   * @param minmax
   * @param feature
   */
  void appendFeature(final StringBuilder sb, int rpos,
          Map<String, float[][]> minmax, SequenceFeature feature)
  {
    if (feature.isContactFeature())
    {
      if (feature.getBegin() == rpos || feature.getEnd() == rpos)
      {
        if (sb.length() > 6)
        {
          sb.append("<br>");
        }
        sb.append(feature.getType()).append(" ").append(feature.getBegin())
                .append(":")
                .append(feature.getEnd());
      }
    }
    else
    {
      if (sb.length() > 6)
      {
        sb.append("<br>");
      }
      // TODO: remove this hack to display link only features
      boolean linkOnly = feature.getValue("linkonly") != null;
      if (!linkOnly)
      {
        sb.append(feature.getType()).append(" ");
        if (rpos != 0)
        {
          // we are marking a positional feature
          sb.append(feature.begin);
        }
        if (feature.begin != feature.end)
        {
          sb.append(" ").append(feature.end);
        }

        if (feature.getDescription() != null
                && !feature.description.equals(feature.getType()))
        {
          String tmpString = feature.getDescription();
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
            sb.append("; ").append(tmpString);
          }
          else
          {
            if (tmpString.indexOf("<") > -1 || tmpString.indexOf(">") > -1)
            {
              // The description does not specify html is to
              // be used, so we must remove < > symbols
              tmpString = tmpString.replaceAll("<", "&lt;");
              tmpString = tmpString.replaceAll(">", "&gt;");

              sb.append("; ");
              sb.append(tmpString);
            }
            else
            {
              sb.append("; ").append(tmpString);
            }
          }
        }
        // check score should be shown
        if (!Float.isNaN(feature.getScore()))
        {
          float[][] rng = (minmax == null) ? null : minmax.get(feature
                  .getType());
          if (rng != null && rng[0] != null && rng[0][0] != rng[0][1])
          {
            sb.append(" Score=").append(String.valueOf(feature.getScore()));
          }
        }
        String status = (String) feature.getValue("status");
        if (status != null && status.length() > 0)
        {
          sb.append("; (").append(status).append(")");
        }
        String clinSig = (String) feature
                .getValue(GffConstants.CLINICAL_SIGNIFICANCE);
        if (clinSig != null)
        {
          sb.append("; ").append(clinSig);
        }
      }
    }
  }

  /**
   * Format and appends any hyperlinks for the sequence feature to the string
   * buffer
   * 
   * @param sb
   * @param feature
   */
  void appendLinks(final StringBuffer sb, SequenceFeature feature)
  {
    if (feature.links != null)
    {
      if (linkImageURL != null)
      {
        sb.append(" <img src=\"" + linkImageURL + "\">");
      }
      else
      {
        for (String urlstring : feature.links)
        {
          try
          {
            for (List<String> urllink : createLinksFrom(null, urlstring))
            {
              sb.append("<br/> <a href=\""
                      + urllink.get(3)
                      + "\" target=\""
                      + urllink.get(0)
                      + "\">"
                      + (urllink.get(0).toLowerCase()
                              .equals(urllink.get(1).toLowerCase()) ? urllink
                              .get(0) : (urllink.get(0) + ":" + urllink
                              .get(1)))
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

  /**
   * 
   * @param seq
   * @param link
   * @return Collection< List<String> > { List<String> { link target, link
   *         label, dynamic component inserted (if any), url }}
   */
  Collection<List<String>> createLinksFrom(SequenceI seq, String link)
  {
    Map<String, List<String>> urlSets = new LinkedHashMap<String, List<String>>();
    UrlLink urlLink = new UrlLink(link);
    if (!urlLink.isValid())
    {
      System.err.println(urlLink.getInvalidMessage());
      return null;
    }

    urlLink.createLinksFromSeq(seq, urlSets);

    return urlSets.values();
  }

  public void createSequenceAnnotationReport(final StringBuilder tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          Map<String, float[][]> minmax)
  {
    createSequenceAnnotationReport(tip, sequence, showDbRefs, showNpFeats,
            minmax, false);
  }

  /**
   * Builds an html formatted report of sequence details and appends it to the
   * provided buffer.
   * 
   * @param sb
   *          buffer to append report to
   * @param sequence
   *          the sequence the report is for
   * @param showDbRefs
   *          whether to include database references for the sequence
   * @param showNpFeats
   *          whether to include non-positional sequence features
   * @param minmax
   * @param summary
   * @return
   */
  int createSequenceAnnotationReport(final StringBuilder sb,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          Map<String, float[][]> minmax, boolean summary)
  {
    String tmp;
    sb.append("<i>");

    int maxWidth = 0;
    if (sequence.getDescription() != null)
    {
      tmp = sequence.getDescription();
      sb.append("<br>").append(tmp);
      maxWidth = Math.max(maxWidth, tmp.length());
    }
    SequenceI ds = sequence;
    while (ds.getDatasetSequence() != null)
    {
      ds = ds.getDatasetSequence();
    }
    DBRefEntry[] dbrefs = ds.getDBRefs();
    if (showDbRefs && dbrefs != null)
    {
      // note this sorts the refs held on the sequence!
      Arrays.sort(dbrefs, comparator);
      boolean ellipsis = false;
      String source = null;
      String lastSource = null;
      int countForSource = 0;
      int sourceCount = 0;
      boolean moreSources = false;
      int lineLength = 0;

      for (DBRefEntry ref : dbrefs)
      {
        source = ref.getSource();
        if (source == null)
        {
          // shouldn't happen
          continue;
        }
        boolean sourceChanged = !source.equals(lastSource);
        if (sourceChanged)
        {
          lineLength = 0;
          countForSource = 0;
          sourceCount++;
        }
        if (sourceCount > MAX_SOURCES && summary)
        {
          ellipsis = true;
          moreSources = true;
          break;
        }
        lastSource = source;
        countForSource++;
        if (countForSource == 1 || !summary)
        {
          sb.append("<br>");
        }
        if (countForSource <= MAX_REFS_PER_SOURCE || !summary)
        {
          String accessionId = ref.getAccessionId();
          lineLength += accessionId.length() + 1;
          if (countForSource > 1 && summary)
          {
            sb.append(", ").append(accessionId);
            lineLength++;
          }
          else
          {
            sb.append(source).append(" ").append(accessionId);
            lineLength += source.length();
          }
          maxWidth = Math.max(maxWidth, lineLength);
        }
        if (countForSource == MAX_REFS_PER_SOURCE && summary)
        {
          sb.append(COMMA).append(ELLIPSIS);
          ellipsis = true;
        }
      }
      if (moreSources)
      {
        sb.append("<br>").append(ELLIPSIS).append(COMMA).append(source)
                .append(COMMA).append(ELLIPSIS);
      }
      if (ellipsis)
      {
        sb.append("<br>(");
        sb.append(MessageManager.getString("label.output_seq_details"));
        sb.append(")");
      }
    }

    /*
     * add non-positional features if wanted
     */
    SequenceFeature[] features = sequence.getSequenceFeatures();
    if (showNpFeats && features != null)
    {
      for (int i = 0; i < features.length; i++)
      {
        if (features[i].begin == 0 && features[i].end == 0)
        {
          int sz = -sb.length();
          appendFeature(sb, 0, minmax, features[i]);
          sz += sb.length();
          maxWidth = Math.max(maxWidth, sz);
        }
      }
    }
    sb.append("</i>");
    return maxWidth;
  }

  public void createTooltipAnnotationReport(final StringBuilder tip,
          SequenceI sequence, boolean showDbRefs, boolean showNpFeats,
          Map<String, float[][]> minmax)
  {
    int maxWidth = createSequenceAnnotationReport(tip, sequence,
            showDbRefs, showNpFeats, minmax, true);

    if (maxWidth > 60)
    {
      // ? not sure this serves any useful purpose
      // tip.insert(0, "<table width=350 border=0><tr><td>");
      // tip.append("</td></tr></table>");
    }
  }
}
