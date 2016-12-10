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
package jalview.json.binding.biojs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class BioJSRepositoryPojo
{

  private String description;

  private String latestReleaseVersion;

  private Collection<BioJSReleasePojo> releases = new ArrayList<BioJSReleasePojo>();

  public BioJSRepositoryPojo()
  {
  }

  public BioJSRepositoryPojo(String jsonString)
  {
    try
    {
      parse(jsonString);
    } catch (ParseException e)
    {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private void parse(String jsonString) throws ParseException
  {
    Objects.requireNonNull(jsonString,
            "Supplied jsonString must not be null");
    JSONParser jsonParser = new JSONParser();
    JSONObject JsonObj = (JSONObject) jsonParser.parse(jsonString);
    this.description = (String) JsonObj.get("description");
    this.latestReleaseVersion = (String) JsonObj
            .get("latestReleaseVersion");

    JSONArray repositoriesJsonArray = (JSONArray) JsonObj.get("releases");
    for (Iterator<JSONObject> repoIter = repositoriesJsonArray.iterator(); repoIter
            .hasNext();)
    {
      JSONObject repoObj = repoIter.next();
      BioJSReleasePojo repo = new BioJSReleasePojo();
      repo.setType((String) repoObj.get("type"));
      repo.setUrl((String) repoObj.get("url"));
      repo.setVersion((String) repoObj.get("version"));
      this.getReleases().add(repo);
    }
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getLatestReleaseVersion()
  {
    return latestReleaseVersion;
  }

  public void setLatestReleaseVersion(String latestReleaseVersion)
  {
    this.latestReleaseVersion = latestReleaseVersion;
  }

  public Collection<BioJSReleasePojo> getReleases()
  {
    return releases;
  }

  public void setReleases(Collection<BioJSReleasePojo> releases)
  {
    this.releases = releases;
  }

}
