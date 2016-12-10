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

import static jalview.util.UrlConstants.DB_ACCESSION;
import static jalview.util.UrlConstants.SEQUENCE_ID;

import jalview.datamodel.DBRefEntry;
import jalview.datamodel.SequenceI;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class UrlLink
{
  /**
   * helper class to parse URL Link strings taken from applet parameters or
   * jalview properties file using the com.stevesoft.pat.Regex implementation.
   * Jalview 2.4 extension allows regular expressions to be used to parse ID
   * strings and replace the result in the URL. Regex's operate on the whole ID
   * string given to the matchURL method, if no regex is supplied, then only
   * text following the first pipe symbol will be substituted. Usage
   * documentation todo.
   */

  // Internal constants
  private static final String SEP = "|";

  private static final String DELIM = "$";

  private String urlSuffix;

  private String urlPrefix;

  private String target;

  private String label;

  private String regexReplace;

  private boolean dynamic = false;

  private boolean usesDBaccession = false;

  private String invalidMessage = null;

  /**
   * parse the given linkString of the form '<label>SEP<url>' into parts url may
   * contain a string $SEQUENCE_ID<=optional regex=>$ where <=optional regex=>
   * must be of the form =/<perl style regex>/=$
   * 
   * @param link
   */
  public UrlLink(String link)
  {
    int sep = link.indexOf(SEP);
    int psqid = link.indexOf(DELIM + DB_ACCESSION);
    int nsqid = link.indexOf(DELIM + SEQUENCE_ID);
    if (psqid > -1)
    {
      dynamic = true;
      usesDBaccession = true;

      sep = parseTargetAndLabel(sep, psqid, link);

      parseUrl(link, DB_ACCESSION, psqid, sep);
    }
    else if (nsqid > -1)
    {
      dynamic = true;
      sep = parseTargetAndLabel(sep, nsqid, link);

      parseUrl(link, SEQUENCE_ID, nsqid, sep);
    }
    else
    {
      target = link.substring(0, sep);
      sep = link.lastIndexOf(SEP);
      label = link.substring(0, sep);
      urlPrefix = link.substring(sep + 1).trim();
      regexReplace = null; // implies we trim any prefix if necessary //
      urlSuffix = null;
    }

    label = label.trim();
    target = target.trim();
  }

  /**
   * @return the url_suffix
   */
  public String getUrl_suffix()
  {
    return urlSuffix;
  }

  /**
   * @return the url_prefix
   */
  public String getUrl_prefix()
  {
    return urlPrefix;
  }

  /**
   * @return the target
   */
  public String getTarget()
  {
    return target;
  }

  /**
   * @return the label
   */
  public String getLabel()
  {
    return label;
  }

  /**
   * @return the regexReplace
   */
  public String getRegexReplace()
  {
    return regexReplace;
  }

  /**
   * @return the invalidMessage
   */
  public String getInvalidMessage()
  {
    return invalidMessage;
  }

  /**
   * Check if URL string was parsed properly.
   * 
   * @return boolean - if false then <code>getInvalidMessage</code> returns an
   *         error message
   */
  public boolean isValid()
  {
    return invalidMessage == null;
  }

  /**
   * 
   * @return whether link is dynamic
   */
  public boolean isDynamic()
  {
    return dynamic;
  }

  /**
   * 
   * @return whether link uses DB Accession id
   */
  public boolean usesDBAccession()
  {
    return usesDBaccession;
  }

  /**
   * Set the label
   * 
   * @param newlabel
   */
  public void setLabel(String newlabel)
  {
    this.label = newlabel;
  }

  /**
   * return one or more URL strings by applying regex to the given idstring
   * 
   * @param idstring
   * @param onlyIfMatches
   *          - when true url strings are only made if regex is defined and
   *          matches
   * @return String[] { part of idstring substituted, full substituted url , ..
   *         next part, next url..}
   */
  public String[] makeUrls(String idstring, boolean onlyIfMatches)
  {
    if (dynamic)
    {
      if (regexReplace != null)
      {
        com.stevesoft.pat.Regex rg = com.stevesoft.pat.Regex.perlCode("/"
                + regexReplace + "/");
        if (rg.search(idstring))
        {
          int ns = rg.numSubs();
          if (ns == 0)
          {
            // take whole regex
            return new String[] { rg.stringMatched(),
                urlPrefix + rg.stringMatched() + urlSuffix };
          } /*
             * else if (ns==1) { // take only subgroup match return new String[]
             * { rg.stringMatched(1), url_prefix+rg.stringMatched(1)+url_suffix
             * }; }
             */
          else
          {
            // debug
            for (int s = 0; s <= rg.numSubs(); s++)
            {
              System.err.println("Sub " + s + " : " + rg.matchedFrom(s)
                      + " : " + rg.matchedTo(s) + " : '"
                      + rg.stringMatched(s) + "'");
            }
            // try to collate subgroup matches
            Vector subs = new Vector();
            // have to loop through submatches, collating them at top level
            // match
            int s = 0; // 1;
            while (s <= ns)
            {
              if (s + 1 <= ns && rg.matchedTo(s) > -1
                      && rg.matchedTo(s + 1) > -1
                      && rg.matchedTo(s + 1) < rg.matchedTo(s))
              {
                // s is top level submatch. search for submatches enclosed by
                // this one
                int r = s + 1;
                String mtch = "";
                while (r <= ns && rg.matchedTo(r) <= rg.matchedTo(s))
                {
                  if (rg.matchedFrom(r) > -1)
                  {
                    mtch += rg.stringMatched(r);
                  }
                  r++;
                }
                if (mtch.length() > 0)
                {
                  subs.addElement(mtch);
                  subs.addElement(urlPrefix + mtch + urlSuffix);
                }
                s = r;
              }
              else
              {
                if (rg.matchedFrom(s) > -1)
                {
                  subs.addElement(rg.stringMatched(s));
                  subs.addElement(urlPrefix + rg.stringMatched(s)
                          + urlSuffix);
                }
                s++;
              }
            }

            String[] res = new String[subs.size()];
            for (int r = 0, rs = subs.size(); r < rs; r++)
            {
              res[r] = (String) subs.elementAt(r);
            }
            subs.removeAllElements();
            return res;
          }
        }
        if (onlyIfMatches)
        {
          return null;
        }
      }
      /* Otherwise - trim off any 'prefix' - pre 2.4 Jalview behaviour */
      if (idstring.indexOf(SEP) > -1)
      {
        idstring = idstring.substring(idstring.lastIndexOf(SEP) + 1);
      }

      // just return simple url substitution.
      return new String[] { idstring, urlPrefix + idstring + urlSuffix };
    }
    else
    {
      return new String[] { "", urlPrefix };
    }
  }

  @Override
  public String toString()
  {
    String var = (usesDBaccession ? DB_ACCESSION : SEQUENCE_ID);

    return label
            + SEP
            + urlPrefix
            + (dynamic ? (DELIM + var + ((regexReplace != null) ? "="
                    + regexReplace + "=" + DELIM : DELIM)) : "")
            + ((urlSuffix == null) ? "" : urlSuffix);
  }

  /**
   * 
   * @param firstSep
   *          Location of first occurrence of separator in link string
   * @param psqid
   *          Position of sequence id or name in link string
   * @param link
   *          Link string containing database name and url
   * @return Position of last separator symbol prior to any regex symbols
   */
  protected int parseTargetAndLabel(int firstSep, int psqid, String link)
  {
    int p = firstSep;
    int sep = firstSep;
    do
    {
      sep = p;
      p = link.indexOf(SEP, sep + 1);
    } while (p > sep && p < psqid);
    // Assuming that the URL itself does not contain any SEP symbols
    // sep now contains last pipe symbol position prior to any regex symbols
    label = link.substring(0, sep);
    if (label.indexOf(SEP) > -1)
    {
      // SEP terminated database name / www target at start of Label
      target = label.substring(0, label.indexOf(SEP));
    }
    else if (label.indexOf(" ") > 2)
    {
      // space separated Label - matches database name
      target = label.substring(0, label.indexOf(" "));
    }
    else
    {
      target = label;
    }
    return sep;
  }

  /**
   * Parse the URL part of the link string
   * 
   * @param link
   *          Link string containing database name and url
   * @param varName
   *          Name of variable in url string (e.g. SEQUENCE_ID, SEQUENCE_NAME)
   * @param sqidPos
   *          Position of id or name in link string
   * @param sep
   *          Position of separator in link string
   */
  protected void parseUrl(String link, String varName, int sqidPos, int sep)
  {
    urlPrefix = link.substring(sep + 1, sqidPos).trim();

    // delimiter at start of regex: e.g. $SEQUENCE_ID=/
    String startDelimiter = DELIM + varName + "=/";

    // delimiter at end of regex: /=$
    String endDelimiter = "/=" + DELIM;

    int startLength = startDelimiter.length();

    // Parse URL : Whole URL string first
    int p = link.indexOf(endDelimiter, sqidPos + startLength);

    if (link.indexOf(startDelimiter) == sqidPos
            && (p > sqidPos + startLength))
    {
      // Extract Regex and suffix
      urlSuffix = link.substring(p + endDelimiter.length());
      regexReplace = link.substring(sqidPos + startLength, p);
      try
      {
        com.stevesoft.pat.Regex rg = com.stevesoft.pat.Regex.perlCode("/"
                + regexReplace + "/");
        if (rg == null)
        {
          invalidMessage = "Invalid Regular Expression : '" + regexReplace
                  + "'\n";
        }
      } catch (Exception e)
      {
        invalidMessage = "Invalid Regular Expression : '" + regexReplace
                + "'\n";
      }
    }
    else
    {
      // no regex
      regexReplace = null;
      // verify format is really correct.
      if (link.indexOf(DELIM + varName + DELIM) == sqidPos)
      {
        urlSuffix = link.substring(sqidPos + startLength - 1);
        regexReplace = null;
      }
      else
      {
        invalidMessage = "Warning: invalid regex structure for URL link : "
                + link;
      }
    }
  }

  /**
   * Create a set of URL links for a sequence
   * 
   * @param seq
   *          The sequence to create links for
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  public void createLinksFromSeq(final SequenceI seq,
          Map<String, List<String>> linkset)
  {
    if (seq != null && dynamic)
    {
      createDynamicLinks(seq, linkset);
    }
    else
    {
      createStaticLink(linkset);
    }
  }

  /**
   * Create a static URL link
   * 
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  protected void createStaticLink(Map<String, List<String>> linkset)
  {
    if (!linkset.containsKey(label + SEP + getUrl_prefix()))
    {
      // Add a non-dynamic link
      linkset.put(label + SEP + getUrl_prefix(),
              Arrays.asList(target, label, null, getUrl_prefix()));
    }
  }

  /**
   * Create dynamic URL links
   * 
   * @param seq
   *          The sequence to create links for
   * @param linkset
   *          Map of links: key = id + SEP + link, value = [target, label, id,
   *          link]
   */
  protected void createDynamicLinks(final SequenceI seq,
          Map<String, List<String>> linkset)
  {
    // collect id string too
    String id = seq.getName();
    String descr = seq.getDescription();
    if (descr != null && descr.length() < 1)
    {
      descr = null;
    }

    if (usesDBAccession()) // link is ID
    {
      // collect matching db-refs
      DBRefEntry[] dbr = DBRefUtils.selectRefs(seq.getDBRefs(),
              new String[] { target });

      // if there are any dbrefs which match up with the link
      if (dbr != null)
      {
        for (int r = 0; r < dbr.length; r++)
        {
          // create Bare ID link for this URL
          createBareURLLink(dbr[r].getAccessionId(), true, linkset);
        }
      }
    }
    else if (!usesDBAccession() && id != null) // link is name
    {
      // create Bare ID link for this URL
      createBareURLLink(id, false, linkset);
    }

    // Create urls from description but only for URL links which are regex
    // links
    if (descr != null && getRegexReplace() != null)
    {
      // create link for this URL from description where regex matches
      createBareURLLink(descr, false, linkset);
    }
  }

  /*
   * Create a bare URL Link
   * Returns map where key = id + SEP + link, and value = [target, label, id, link]
   */
  protected void createBareURLLink(String id, Boolean combineLabel,
          Map<String, List<String>> linkset)
  {
    String[] urls = makeUrls(id, true);
    if (urls != null)
    {
      for (int u = 0; u < urls.length; u += 2)
      {
        if (!linkset.containsKey(urls[u] + SEP + urls[u + 1]))
        {
          String thisLabel = label;
          if (combineLabel)
          {
            // incorporate label with idstring
            thisLabel = label + SEP + urls[u];
          }

          linkset.put(urls[u] + SEP + urls[u + 1],
                  Arrays.asList(target, thisLabel, urls[u], urls[u + 1]));
        }
      }
    }
  }

  private static void testUrls(UrlLink ul, String idstring, String[] urls)
  {

    if (urls == null)
    {
      System.out.println("Created NO urls.");
    }
    else
    {
      System.out.println("Created " + (urls.length / 2) + " Urls.");
      for (int uls = 0; uls < urls.length; uls += 2)
      {
        System.out.println("URL Replacement text : " + urls[uls]
                + " : URL : " + urls[uls + 1]);
      }
    }
  }

  public static void main(String argv[])
  {
    String[] links = new String[] {
    /*
     * "AlinkT|Target|http://foo.foo.soo/",
     * "myUrl1|http://$SEQUENCE_ID=/[0-9]+/=$.someserver.org/foo",
     * "myUrl2|http://$SEQUENCE_ID=/(([0-9]+).+([A-Za-z]+))/=$.someserver.org/foo"
     * ,
     * "myUrl3|http://$SEQUENCE_ID=/([0-9]+).+([A-Za-z]+)/=$.someserver.org/foo"
     * , "myUrl4|target|http://$SEQUENCE_ID$.someserver.org/foo|too",
     * "PF1|http://us.expasy.org/cgi-bin/niceprot.pl?$SEQUENCE_ID=/(?:PFAM:)?(.+)/=$"
     * ,
     * "PF2|http://us.expasy.org/cgi-bin/niceprot.pl?$SEQUENCE_ID=/(PFAM:)?(.+)/=$"
     * ,
     * "PF3|http://us.expasy.org/cgi-bin/niceprot.pl?$SEQUENCE_ID=/PFAM:(.+)/=$"
     * , "NOTFER|http://notfer.org/$SEQUENCE_ID=/(?<!\\s)(.+)/=$",
     */
    "NESTED|http://nested/$" + DB_ACCESSION
            + "=/^(?:Label:)?(?:(?:gi\\|(\\d+))|([^:]+))/=$/nested" };
    String[] idstrings = new String[] {
    /*
     * //"LGUL_human", //"QWIQW_123123", "uniprot|why_do+_12313_foo",
     * //"123123312", "123123 ABCDE foo", "PFAM:PF23943",
     */
    "Label:gi|9234|pdb|102L|A" };
    // TODO: test the setLabel method.
    for (int i = 0; i < links.length; i++)
    {
      UrlLink ul = new UrlLink(links[i]);
      if (ul.isValid())
      {
        System.out.println("\n\n\n");
        System.out.println("Link " + i + " " + links[i] + " : "
                + ul.toString());
        System.out.println(" pref : "
                + ul.getUrl_prefix()
                + "\n suf : "
                + ul.getUrl_suffix()
                + "\n : "
                + ((ul.getRegexReplace() != null) ? ul.getRegexReplace()
                        : ""));
        for (int ids = 0; ids < idstrings.length; ids++)
        {
          System.out.println("ID String : " + idstrings[ids]
                  + "\nWithout onlyIfMatches:");
          String[] urls = ul.makeUrls(idstrings[ids], false);
          testUrls(ul, idstrings[ids], urls);
          System.out.println("With onlyIfMatches set.");
          urls = ul.makeUrls(idstrings[ids], true);
          testUrls(ul, idstrings[ids], urls);
        }
      }
      else
      {
        System.err.println("Invalid URLLink : " + links[i] + " : "
                + ul.getInvalidMessage());
      }
    }
  }
}
