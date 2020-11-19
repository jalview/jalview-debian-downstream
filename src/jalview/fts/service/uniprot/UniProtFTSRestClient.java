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

package jalview.fts.service.uniprot;

import jalview.bin.Cache;
import jalview.fts.api.FTSData;
import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSRestClientI;
import jalview.fts.core.FTSRestClient;
import jalview.fts.core.FTSRestRequest;
import jalview.fts.core.FTSRestResponse;
import jalview.util.MessageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class UniProtFTSRestClient extends FTSRestClient
{
  private static final String DEFAULT_UNIPROT_DOMAIN = "https://www.uniprot.org";

  private static FTSRestClientI instance = null;

  public final String uniprotSearchEndpoint;

  public UniProtFTSRestClient()
  {
    super();
    uniprotSearchEndpoint = Cache.getDefault("UNIPROT_DOMAIN",
            DEFAULT_UNIPROT_DOMAIN) + "/uniprot/?";
  }

  @Override
  public FTSRestResponse executeRequest(FTSRestRequest uniportRestRequest)
          throws Exception
  {
    try
    {
      ClientConfig clientConfig = new DefaultClientConfig();
      Client client = Client.create(clientConfig);

      String wantedFields = getDataColumnsFieldsAsCommaDelimitedString(
              uniportRestRequest.getWantedFields());
      int responseSize = (uniportRestRequest.getResponseSize() == 0)
              ? getDefaultResponsePageSize()
              : uniportRestRequest.getResponseSize();

      int offSet = uniportRestRequest.getOffSet();
      String query;
      if (isAdvancedQuery(uniportRestRequest.getSearchTerm()))
      {
        query = uniportRestRequest.getSearchTerm();
      }
      else
      {
        query = uniportRestRequest.getFieldToSearchBy()
                .equalsIgnoreCase("Search All")
                        ? uniportRestRequest.getSearchTerm()
                                + " or mnemonic:"
                                + uniportRestRequest.getSearchTerm()
                        : uniportRestRequest.getFieldToSearchBy() + ":"
                                + uniportRestRequest.getSearchTerm();
      }

      WebResource webResource = null;
      webResource = client.resource(uniprotSearchEndpoint)
              .queryParam("format", "tab")
              .queryParam("columns", wantedFields)
              .queryParam("limit", String.valueOf(responseSize))
              .queryParam("offset", String.valueOf(offSet))
              .queryParam("sort", "score").queryParam("query", query);
      // Execute the REST request
      ClientResponse clientResponse = webResource
              .accept(MediaType.TEXT_PLAIN).get(ClientResponse.class);
      String uniProtTabDelimittedResponseString = clientResponse
              .getEntity(String.class);
      // Make redundant objects eligible for garbage collection to conserve
      // memory
      // System.out.println(">>>>> response : "
      // + uniProtTabDelimittedResponseString);
      if (clientResponse.getStatus() != 200)
      {
        String errorMessage = getMessageByHTTPStatusCode(
                clientResponse.getStatus(), "Uniprot");
        throw new Exception(errorMessage);

      }
      int xTotalResults = Integer.valueOf(
              clientResponse.getHeaders().get("X-Total-Results").get(0));
      clientResponse = null;
      client = null;
      return parseUniprotResponse(uniProtTabDelimittedResponseString,
              uniportRestRequest, xTotalResults);
    } catch (Exception e)
    {
      String exceptionMsg = e.getMessage();
      if (exceptionMsg.contains("SocketException"))
      {
        // No internet connection
        throw new Exception(MessageManager.getString(
                "exception.unable_to_detect_internet_connection"));
      }
      else if (exceptionMsg.contains("UnknownHostException"))
      {
        // The server 'http://www.uniprot.org' is unreachable
        throw new Exception(MessageManager.formatMessage(
                "exception.fts_server_unreachable", "Uniprot"));
      }
      else
      {
        throw e;
      }
    }
  }

  public boolean isAdvancedQuery(String query)
  {
    if (query.contains(" AND ") || query.contains(" OR ")
            || query.contains(" NOT ") || query.contains(" ! ")
            || query.contains(" || ") || query.contains(" && ")
            || query.contains(":") || query.contains("-"))
    {
      return true;
    }
    return false;
  }

  public FTSRestResponse parseUniprotResponse(
          String uniProtTabDelimittedResponseString,
          FTSRestRequest uniprotRestRequest, int xTotalResults)
  {
    FTSRestResponse searchResult = new FTSRestResponse();
    List<FTSData> result = null;
    if (uniProtTabDelimittedResponseString == null
            || uniProtTabDelimittedResponseString.trim().isEmpty())
    {
      searchResult.setNumberOfItemsFound(0);
      return searchResult;
    }
    String[] foundDataRow = uniProtTabDelimittedResponseString.split("\n");
    if (foundDataRow != null && foundDataRow.length > 0)
    {
      result = new ArrayList<>();
      boolean firstRow = true;
      for (String dataRow : foundDataRow)
      {
        // The first data row is usually the header data. This should be
        // filtered out from the rest of the data See: JAL-2485
        if (firstRow)
        {
          firstRow = false;
          continue;
        }
        // System.out.println(dataRow);
        result.add(getFTSData(dataRow, uniprotRestRequest));
      }
      searchResult.setNumberOfItemsFound(xTotalResults);
      searchResult.setSearchSummary(result);
    }
    return searchResult;
  }

  /**
   * Takes a collection of FTSDataColumnI and converts its 'code' values into a
   * tab delimited string.
   * 
   * @param dataColumnFields
   *          the collection of FTSDataColumnI to process
   * @return the generated comma delimited string from the supplied
   *         FTSDataColumnI collection
   */
  private String getDataColumnsFieldsAsTabDelimitedString(
          Collection<FTSDataColumnI> dataColumnFields)
  {
    String result = "";
    if (dataColumnFields != null && !dataColumnFields.isEmpty())
    {
      StringBuilder returnedFields = new StringBuilder();
      for (FTSDataColumnI field : dataColumnFields)
      {
        if (field.getName().equalsIgnoreCase("Uniprot Id"))
        {
          returnedFields.append("\t").append("Entry");
        }
        else
        {
          returnedFields.append("\t").append(field.getName());
        }
      }
      returnedFields.deleteCharAt(0);
      result = returnedFields.toString();
    }
    return result;
  }

  public static FTSData getFTSData(String tabDelimittedDataStr,
          FTSRestRequest request)
  {
    String primaryKey = null;

    Object[] summaryRowData;

    Collection<FTSDataColumnI> diplayFields = request.getWantedFields();
    int colCounter = 0;
    summaryRowData = new Object[diplayFields.size()];
    String[] columns = tabDelimittedDataStr.split("\t");
    for (FTSDataColumnI field : diplayFields)
    {
      try
      {
        String fieldData = columns[colCounter];
        if (field.isPrimaryKeyColumn())
        {
          primaryKey = fieldData;
          summaryRowData[colCounter++] = primaryKey;
        }
        else if (fieldData == null || fieldData.isEmpty())
        {
          summaryRowData[colCounter++] = null;
        }
        else
        {
          try
          {
            summaryRowData[colCounter++] = (field.getDataType()
                    .getDataTypeClass() == Integer.class)
                            ? Integer.valueOf(fieldData.replace(",", ""))
                            : (field.getDataType()
                                    .getDataTypeClass() == Double.class)
                                            ? Double.valueOf(fieldData)
                                            : fieldData;
          } catch (Exception e)
          {
            e.printStackTrace();
            System.out.println("offending value:" + fieldData);
          }
        }
      } catch (Exception e)
      {
        // e.printStackTrace();
      }
    }

    final String primaryKey1 = primaryKey;

    final Object[] summaryRowData1 = summaryRowData;
    return new FTSData()
    {
      @Override
      public Object[] getSummaryData()
      {
        return summaryRowData1;
      }

      @Override
      public Object getPrimaryKey()
      {
        return primaryKey1;
      }

      /**
       * Returns a string representation of this object;
       */
      @Override
      public String toString()
      {
        StringBuilder summaryFieldValues = new StringBuilder();
        for (Object summaryField : summaryRowData1)
        {
          summaryFieldValues.append(
                  summaryField == null ? " " : summaryField.toString())
                  .append("\t");
        }
        return summaryFieldValues.toString();
      }

      /**
       * Returns hash code value for this object
       */
      @Override
      public int hashCode()
      {
        return Objects.hash(primaryKey1, this.toString());
      }

      @Override
      public boolean equals(Object that)
      {
        return this.toString().equals(that.toString());
      }
    };
  }

  public static FTSRestClientI getInstance()
  {
    if (instance == null)
    {
      instance = new UniProtFTSRestClient();
    }
    return instance;
  }

  @Override
  public String getColumnDataConfigFileName()
  {
    return "/fts/uniprot_data_columns.txt";
  }

}
