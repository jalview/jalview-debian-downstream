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
package jalview.ext.ensembl;

import jalview.datamodel.AlignmentI;
import jalview.datamodel.DBRefSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class EnsemblInfo extends EnsemblRestClient
{

  /*
   * cached results of REST /info/divisions service, currently
   * <pre>
   * { 
   *  { "ENSEMBLFUNGI", "http://rest.ensemblgenomes.org"},
   *    "ENSEMBLBACTERIA", "http://rest.ensemblgenomes.org"},
   *    "ENSEMBLPROTISTS", "http://rest.ensemblgenomes.org"},
   *    "ENSEMBLMETAZOA", "http://rest.ensemblgenomes.org"},
   *    "ENSEMBLPLANTS",  "http://rest.ensemblgenomes.org"},
   *    "ENSEMBL", "http://rest.ensembl.org" }
   *  }
   * </pre>
   * The values for EnsemblGenomes are retrieved by a REST call, that for
   * Ensembl is added programmatically for convenience of lookup
   */
  private static Map<String, String> divisions;

  @Override
  public String getDbName()
  {
    return "ENSEMBL";
  }

  @Override
  public AlignmentI getSequenceRecords(String queries) throws Exception
  {
    return null;
  }

  @Override
  protected URL getUrl(List<String> ids) throws MalformedURLException
  {
    return null;
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  /**
   * Answers the domain (http://rest.ensembl.org or
   * http://rest.ensemblgenomes.org) for the given division, or null if not
   * recognised by Ensembl.
   * 
   * @param division
   * @return
   */
  public String getDomain(String division)
  {
    if (divisions == null)
    {
      fetchDivisions();
    }
    return divisions.get(division.toUpperCase());
  }

  /**
   * On first request only, populate the lookup map by fetching the list of
   * divisions known to EnsemblGenomes.
   */
  void fetchDivisions()
  {
    divisions = new HashMap<>();

    /*
     * for convenience, pre-fill ensembl.org as the domain for "ENSEMBL"
     */
    divisions.put(DBRefSource.ENSEMBL.toUpperCase(), ensemblDomain);

    BufferedReader br = null;
    try
    {
      URL url = getDivisionsUrl(ensemblGenomesDomain);
      if (url != null)
      {
        br = getHttpResponse(url, null);
      }
      parseResponse(br, ensemblGenomesDomain);
    } catch (IOException e)
    {
      // ignore
    } finally
    {
      if (br != null)
      {
        try
        {
          br.close();
        } catch (IOException e)
        {
          // ignore
        }
      }
    }
  }

  /**
   * Parses the JSON response to /info/divisions, and add each to the lookup map
   * 
   * @param br
   * @param domain
   */
  void parseResponse(BufferedReader br, String domain)
  {
    JSONParser jp = new JSONParser();

    try
    {
      JSONArray parsed = (JSONArray) jp.parse(br);

      Iterator rvals = parsed.iterator();
      while (rvals.hasNext())
      {
        String division = rvals.next().toString();
        divisions.put(division.toUpperCase(), domain);
      }
    } catch (IOException | ParseException | NumberFormatException e)
    {
      // ignore
    }
  }

  /**
   * Constructs the URL for the EnsemblGenomes /info/divisions REST service
   * @param domain TODO
   * 
   * @return
   * @throws MalformedURLException
   */
  URL getDivisionsUrl(String domain) throws MalformedURLException
  {
    return new URL(domain
            + "/info/divisions?content-type=application/json");
  }

  /**
   * Returns the set of 'divisions' recognised by Ensembl or EnsemblGenomes
   * 
   * @return
   */
  public Set<String> getDivisions() {
    if (divisions == null)
    {
      fetchDivisions();
    }

    return divisions.keySet();
  }
}
