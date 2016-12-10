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

import jalview.datamodel.AlignmentI;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * A client for the Ensembl lookup REST endpoint; used to find the Parent gene
 * identifier given a transcript identifier.
 * 
 * @author gmcarstairs
 *
 */
public class EnsemblLookup extends EnsemblRestClient
{

  /**
   * Default constructor (to use rest.ensembl.org)
   */
  public EnsemblLookup()
  {
    super();
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param
   */
  public EnsemblLookup(String d)
  {
    super(d);
  }

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
    String identifier = ids.get(0);
    return getUrl(identifier);
  }

  /**
   * @param identifier
   * @return
   */
  protected URL getUrl(String identifier)
  {
    String url = getDomain() + "/lookup/id/" + identifier
            + "?content-type=application/json";
    try
    {
      return new URL(url);
    } catch (MalformedURLException e)
    {
      return null;
    }
  }

  @Override
  protected boolean useGetRequest()
  {
    return true;
  }

  @Override
  protected String getRequestMimeType(boolean multipleIds)
  {
    return "application/json";
  }

  @Override
  protected String getResponseMimeType()
  {
    return "application/json";
  }

  /**
   * Calls the Ensembl lookup REST endpoint and retrieves the 'Parent' for the
   * given identifier, or null if not found
   * 
   * @param identifier
   * @return
   */
  public String getParent(String identifier)
  {
    List<String> ids = Arrays.asList(new String[] { identifier });

    BufferedReader br = null;
    try
    {
      URL url = getUrl(identifier);
      if (url != null)
      {
        br = getHttpResponse(url, ids);
      }
      return (parseResponse(br));
    } catch (IOException e)
    {
      // ignore
      return null;
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
   * Parses "Parent" from the JSON response and returns the value, or null if
   * not found
   * 
   * @param br
   * @return
   * @throws IOException
   */
  protected String parseResponse(BufferedReader br) throws IOException
  {
    String parent = null;
    JSONParser jp = new JSONParser();
    try
    {
      JSONObject val = (JSONObject) jp.parse(br);
      parent = val.get("Parent").toString();
    } catch (ParseException e)
    {
      // ignore
    }
    return parent;
  }

}
