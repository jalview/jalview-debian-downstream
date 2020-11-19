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
package jalview.fts.service.pdb;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.gui.JvOptionPane;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class PDBFTSRestClientTest
{

  @BeforeClass(alwaysRun = true)
  public void setUpJvOptionPane()
  {
    JvOptionPane.setInteractiveMode(false);
    JvOptionPane.setMockResponse(JvOptionPane.CANCEL_OPTION);
  }

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception
  {
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception
  {
  }

  @Test(groups = { "External", "Network" })
  public void executeRequestTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setResponseSize(100);
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc");
    request.setWantedFields(wantedFields);

    FTSRestResponse response;
    try
    {
      response = PDBFTSRestClient.getInstance().executeRequest(request);
    } catch (Exception e)
    {
      e.printStackTrace();
      Assert.fail("Couldn't execute webservice call!");
      return;
    }
    assertTrue(response.getNumberOfItemsFound() > 99);
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getSearchSummary().size() > 99);
  }

  @Test(groups = { "Functional" })
  public void getPDBDocFieldsAsCommaDelimitedStringTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    String expectedResult = "molecule_type,pdb_id,genus,gene_name,title";
    String actualResult = PDBFTSRestClient.getInstance()
            .getDataColumnsFieldsAsCommaDelimitedString(wantedFields);

    assertEquals("", expectedResult, actualResult);
  }

  @Test(groups = { "External, Network" })
  public void parsePDBJsonExceptionStringTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setResponseSize(100);
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc");
    request.setWantedFields(wantedFields);

    String jsonErrorResponse = "";
    try
    {
      jsonErrorResponse = readJsonStringFromFile("test/jalview/io/pdb_request_json_error.txt");
    } catch (IOException e)
    {
      e.printStackTrace();
    }

    String parsedErrorResponse = PDBFTSRestClient
            .parseJsonExceptionString(jsonErrorResponse);

    String expectedErrorMsg = "\n============= PDB Rest Client RunTime error =============\n"
            + "Status: 400\n"
            + "Message: org.apache.solr.search.SyntaxError: Cannot parse 'text:abc OR text:go:abc AND molecule_sequence:['' TO *]': Encountered \" \":\" \": \"\" at line 1, column 19.\n"
            + "query: text:abc OR text:go:abc AND molecule_sequence:['' TO *]\n"
            + "fl: pdb_id\n";

    assertEquals(expectedErrorMsg, parsedErrorResponse);
  }

  @Test(groups = { "External" }, expectedExceptions = Exception.class)
  public void testForExpectedRuntimeException() throws Exception
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    wantedFields.add(PDBFTSRestClient.getInstance()
            .getDataColumnByNameOrCode("pdb_id"));

    FTSRestRequest request = new FTSRestRequest();
    request.setFieldToSearchBy("text:");
    request.setSearchTerm("abc OR text:go:abc");
    request.setWantedFields(wantedFields);
    PDBFTSRestClient.getInstance().executeRequest(request);
  }

  // JBP: Is this actually external ? Looks like it is mocked
  @Test(groups = { "External" })
  public void parsePDBJsonResponseTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
    } catch (Exception e1)
    {
      e1.printStackTrace();
    }

    FTSRestRequest request = new FTSRestRequest();
    request.setAllowEmptySeq(false);
    request.setWantedFields(wantedFields);

    String jsonString = "";
    try
    {
      jsonString = readJsonStringFromFile("test/jalview/io/pdb_response_json.txt");
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    FTSRestResponse response = PDBFTSRestClient.parsePDBJsonResponse(
            jsonString, request);
    assertTrue(response.getSearchSummary() != null);
    assertTrue(response.getNumberOfItemsFound() == 931);
    assertTrue(response.getSearchSummary().size() == 14);
  }

  @Test(groups = { "Functional" })
  public void getPDBIdColumIndexTest()
  {
    List<FTSDataColumnI> wantedFields = new ArrayList<FTSDataColumnI>();
    try
    {
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("molecule_type"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("genus"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("gene_name"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("title"));
      wantedFields.add(PDBFTSRestClient.getInstance()
              .getDataColumnByNameOrCode("pdb_id"));
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    try
    {
      assertEquals(5, PDBFTSRestClient.getInstance()
              .getPrimaryKeyColumIndex(wantedFields, true));
      assertEquals(4, PDBFTSRestClient.getInstance()
              .getPrimaryKeyColumIndex(wantedFields, false));
    } catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Test(groups = { "External" })
  public void externalServiceIntegrationTest()
  {
    ClientConfig clientConfig = new DefaultClientConfig();
    Client client = Client.create(clientConfig);

    // Build request parameters for the REST Request
    WebResource webResource = client
            .resource(PDBFTSRestClient.PDB_SEARCH_ENDPOINT)
            .queryParam("wt", "json").queryParam("rows", String.valueOf(1))
            .queryParam("q", "text:abc AND molecule_sequence:['' TO *]");

    // Execute the REST request
    ClientResponse clientResponse = webResource.accept(
            MediaType.APPLICATION_JSON).get(ClientResponse.class);

    // Get the JSON string from the response object
    String pdbJsonResponseString = clientResponse.getEntity(String.class);

    // Check the response status and report exception if one occurs
    if (clientResponse.getStatus() != 200)
    {
      Assert.fail("Webservice call failed!!!");
    }
    else
    {
      try
      {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObj = (JSONObject) jsonParser
                .parse(pdbJsonResponseString);
        JSONObject pdbResponse = (JSONObject) jsonObj.get("response");
        String queryTime = ((JSONObject) jsonObj.get("responseHeader"))
                .get("QTime").toString();
        String numFound = pdbResponse.get("numFound").toString();
        JSONArray docs = (JSONArray) pdbResponse.get("docs");
        Iterator<JSONObject> docIter = docs.iterator();

        assertTrue("Couldn't Retrieve 'response' object",
                pdbResponse != null);
        assertTrue("Couldn't Retrieve 'QTime' value", queryTime != null);
        assertTrue("Couldn't Retrieve 'numFound' value", numFound != null);
        assertTrue("Couldn't Retrieve 'docs' object", docs != null
                || !docIter.hasNext());

        JSONObject pdbJsonDoc = docIter.next();

        for (FTSDataColumnI field : PDBFTSRestClient.getInstance()
                .getAllFTSDataColumns())
        {
          if (field.getName().equalsIgnoreCase("ALL"))
          {
            continue;
          }
          if (pdbJsonDoc.get(field.getCode()) == null)
          {
            // System.out.println(">>>\t" + field.getCode());
            assertTrue(field.getCode()
                    + " has been removed from PDB doc Entity",
                    !pdbJsonResponseString.contains(field.getCode()));
          }
        }
      } catch (ParseException e)
      {
        Assert.fail(">>>  Test failed due to exception while parsing pdb response json !!!");
        e.printStackTrace();
      }
    }
  }

  public String readJsonStringFromFile(String filePath) throws IOException
  {
    String fileContent;
    BufferedReader br = new BufferedReader(new FileReader(filePath));
    try
    {
      StringBuilder sb = new StringBuilder();
      String line = br.readLine();

      while (line != null)
      {
        sb.append(line);
        sb.append(System.lineSeparator());
        line = br.readLine();
      }
      fileContent = sb.toString();
    } finally
    {
      br.close();
    }
    return fileContent;
  }

}
