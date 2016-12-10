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
package jalview.ext.ensembl;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A client for the Ensembl xrefs/symbol REST service;
 * 
 * @see http://rest.ensembl.org/documentation/info/xref_external
 * @author gmcarstairs
 *
 */
public class EnsemblSymbol extends EnsemblXref
{
  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param domain
   * @param dbName
   * @param dbVersion
   */
  public EnsemblSymbol(String domain, String dbName, String dbVersion)
  {
    super(domain, dbName, dbVersion);
  }

  /**
   * Returns the first "id" value in gene identifier format from the JSON
   * response, or null if none found
   * 
   * @param br
   * @return
   * @throws IOException
   */
  protected String parseSymbolResponse(BufferedReader br)
          throws IOException
  {
    JSONParser jp = new JSONParser();
    String result = null;
    try
    {
      JSONArray responses = (JSONArray) jp.parse(br);
      Iterator rvals = responses.iterator();
      while (rvals.hasNext())
      {
        JSONObject val = (JSONObject) rvals.next();
        String id = val.get("id").toString();
        if (id != null && isGeneIdentifier(id))
        {
          result = id;
          break;
        }
      }
    } catch (ParseException e)
    {
      // ignore
    }
    return result;
  }

  protected URL getUrl(String id, Species species)
  {
    String url = getDomain() + "/xrefs/symbol/" + species.toString() + "/"
            + id + "?content-type=application/json";
    try
    {
      return new URL(url);
    } catch (MalformedURLException e)
    {
      return null;
    }
  }

  /**
   * Calls the Ensembl xrefs REST 'symbol' endpoint and retrieves any gene ids
   * for the given identifier, for any known model organisms
   * 
   * @param identifier
   * @return
   */
  public List<String> getIds(String identifier)
  {
    List<String> result = new ArrayList<String>();
    List<String> ids = new ArrayList<String>();
    ids.add(identifier);

    String[] queries = identifier.split(getAccessionSeparator());
    BufferedReader br = null;
    try
    {
      for (String query : queries)
      {
        for (Species taxon : Species.values())
        {
          if (taxon.isModelOrganism())
          {
            URL url = getUrl(query, taxon);
            if (url != null)
            {
              br = getHttpResponse(url, ids);
            }
            String geneId = parseSymbolResponse(br);
            if (geneId != null)
            {
              result.add(geneId);
            }
          }
        }
      }
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
    return result;
  }

}
