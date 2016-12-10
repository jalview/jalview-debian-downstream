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
package jalview.fts.core;

import jalview.fts.api.FTSDataColumnI;
import jalview.fts.api.FTSDataColumnI.FTSDataColumnGroupI;
import jalview.fts.api.FTSRestClientI;
import jalview.util.MessageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * Base class providing implementation for common methods defined in
 * FTSRestClientI
 * 
 * @author tcnofoegbu
 * 
 * @note implementations MUST be accessed as a singleton.
 */
public abstract class FTSRestClient implements FTSRestClientI
{
  protected Collection<FTSDataColumnI> dataColumns = new ArrayList<FTSDataColumnI>();

  protected Collection<FTSDataColumnGroupI> dataColumnGroups = new ArrayList<FTSDataColumnGroupI>();

  protected Collection<FTSDataColumnI> searchableDataColumns = new ArrayList<FTSDataColumnI>();

  protected Collection<FTSDataColumnI> defaulDisplayedDataColumns = new ArrayList<FTSDataColumnI>();

  protected FTSDataColumnI primaryKeyColumn;

  private String primaryKeyColumnCode = null;

  private int defaultResponsePageSize = 100;

  protected FTSRestClient()
  {

  }

  public void parseDataColumnsConfigFile()
  {
    String fileName = getColumnDataConfigFileName();

    InputStream in = getClass().getResourceAsStream(fileName);

    try (BufferedReader br = new BufferedReader(new InputStreamReader(in)))
    {
      String line;
      while ((line = br.readLine()) != null)
      {
        final String[] lineData = line.split(";");
        try
        {
          if (lineData.length == 2)
          {
            if (lineData[0].equalsIgnoreCase("_data_column.primary_key"))
            {
              primaryKeyColumnCode = lineData[1];
            }
            if (lineData[0]
                    .equalsIgnoreCase("_data_column.default_response_page_size"))
            {
              defaultResponsePageSize = Integer.valueOf(lineData[1]);
            }
          }
          else if (lineData.length == 3)
          {
            dataColumnGroups.add(new FTSDataColumnGroupI()
            {
              @Override
              public String getID()
              {
                return lineData[0];
              }

              @Override
              public String getName()
              {
                return lineData[1];
              }

              @Override
              public int getSortOrder()
              {
                return Integer.valueOf(lineData[2]);
              }

              @Override
              public String toString()
              {
                return lineData[1];
              }

              @Override
              public int hashCode()
              {
                return Objects.hash(this.getID(), this.getName(),
                        this.getSortOrder());
              }

              @Override
              public boolean equals(Object otherObject)
              {
                FTSDataColumnGroupI that = (FTSDataColumnGroupI) otherObject;
                return this.getID().equals(that.getID())
                        && this.getName().equals(that.getName())
                        && this.getSortOrder() == that.getSortOrder();
              }
            });
          }
          else if (lineData.length > 6)
          {
            FTSDataColumnI dataCol = new FTSDataColumnI()
            {
              @Override
              public String toString()
              {
                return lineData[0];
              }

              @Override
              public String getName()
              {
                return lineData[0];
              }

              @Override
              public String getCode()
              {
                return lineData[1].split("\\|")[0];
              }

              @Override
              public String getAltCode()
              {
                return lineData[1].split("\\|").length > 1 ? lineData[1]
                        .split("\\|")[1] : getCode();
              }

              @Override
              public DataTypeI getDataType()
              {
                final String[] dataTypeString = lineData[2].split("\\|");
                final String classString = dataTypeString[0].toUpperCase();

                return new DataTypeI()
                {

                  @Override
                  public boolean isFormtted()
                  {
                    if (dataTypeString.length > 1
                            && dataTypeString[1] != null)
                    {
                      switch (dataTypeString[1].toUpperCase())
                      {
                      case "T":
                      case "TRUE":
                        return true;
                      case "F":
                      case "False":
                      default:
                        return false;
                      }
                    }
                    return false;
                  }

                  @Override
                  public int getSignificantFigures()
                  {
                    if (dataTypeString.length > 2
                            && dataTypeString[2] != null)
                    {
                      return Integer.valueOf(dataTypeString[2]);
                    }
                    return 0;
                  }

                  @Override
                  public Class getDataTypeClass()
                  {
                    switch (classString)
                    {
                    case "INT":
                    case "INTEGER":
                      return Integer.class;
                    case "DOUBLE":
                      return Double.class;
                    case "STRING":
                    default:
                      return String.class;
                    }
                  }
                };

              }

              @Override
              public FTSDataColumnGroupI getGroup()
              {
                FTSDataColumnGroupI group = null;
                try
                {
                  group = getDataColumnGroupById(lineData[3]);
                } catch (Exception e)
                {
                  e.printStackTrace();
                }
                return group;
              }

              @Override
              public int getMinWidth()
              {
                return Integer.valueOf(lineData[4]);
              }

              @Override
              public int getMaxWidth()
              {
                return Integer.valueOf(lineData[5]);
              }

              @Override
              public int getPreferredWidth()
              {
                return Integer.valueOf(lineData[6]);
              }

              @Override
              public boolean isPrimaryKeyColumn()
              {
                return getName().equalsIgnoreCase(primaryKeyColumnCode)
                        || getCode().equalsIgnoreCase(primaryKeyColumnCode);
              }

              @Override
              public boolean isVisibleByDefault()
              {
                return Boolean.valueOf(lineData[7]);
              }

              @Override
              public boolean isSearchable()
              {
                return Boolean.valueOf(lineData[8]);
              }

              @Override
              public int hashCode()
              {
                return Objects.hash(this.getName(), this.getCode(),
                        this.getGroup());
              }

              @Override
              public boolean equals(Object otherObject)
              {
                FTSDataColumnI that = (FTSDataColumnI) otherObject;
                return this.getCode().equals(that.getCode())
                        && this.getName().equals(that.getName())
                        && this.getGroup().equals(that.getGroup());
              }

            };
            dataColumns.add(dataCol);

            if (dataCol.isSearchable())
            {
              searchableDataColumns.add(dataCol);
            }

            if (dataCol.isVisibleByDefault())
            {
              defaulDisplayedDataColumns.add(dataCol);
            }

          }
          else
          {
            continue;
          }
        } catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      try
      {
        this.primaryKeyColumn = getDataColumnByNameOrCode(primaryKeyColumnCode);
      } catch (Exception e)
      {
        e.printStackTrace();
      }
    } catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  @Override
  public int getPrimaryKeyColumIndex(
          Collection<FTSDataColumnI> wantedFields, boolean hasRefSeq)
          throws Exception
  {

    // If a reference sequence is attached then start counting from 1 else
    // start from zero
    int pdbFieldIndexCounter = hasRefSeq ? 1 : 0;

    for (FTSDataColumnI field : wantedFields)
    {
      if (field.isPrimaryKeyColumn())
      {
        break; // Once PDB Id index is determined exit iteration
      }
      ++pdbFieldIndexCounter;
    }
    return pdbFieldIndexCounter;
  }

  @Override
  public String getDataColumnsFieldsAsCommaDelimitedString(
          Collection<FTSDataColumnI> dataColumnFields)
  {
    String result = "";
    if (dataColumnFields != null && !dataColumnFields.isEmpty())
    {
      StringBuilder returnedFields = new StringBuilder();
      for (FTSDataColumnI field : dataColumnFields)
      {
        returnedFields.append(",").append(field.getCode());
      }
      returnedFields.deleteCharAt(0);
      result = returnedFields.toString();
    }
    return result;
  }

  @Override
  public Collection<FTSDataColumnI> getAllFTSDataColumns()
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return dataColumns;
  }

  @Override
  public Collection<FTSDataColumnI> getSearchableDataColumns()
  {
    if (searchableDataColumns == null || searchableDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return searchableDataColumns;
  }

  @Override
  public Collection<FTSDataColumnI> getAllDefaultDisplayedFTSDataColumns()
  {
    if (defaulDisplayedDataColumns == null
            || defaulDisplayedDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return defaulDisplayedDataColumns;
  }

  @Override
  public FTSDataColumnI getPrimaryKeyColumn()
  {
    if (defaulDisplayedDataColumns == null
            || defaulDisplayedDataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return primaryKeyColumn;
  }

  @Override
  public FTSDataColumnI getDataColumnByNameOrCode(String nameOrCode)
          throws Exception
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    for (FTSDataColumnI column : dataColumns)
    {
      if (column.getName().equalsIgnoreCase(nameOrCode)
              || column.getCode().equalsIgnoreCase(nameOrCode))
      {
        return column;
      }
    }
    throw new Exception("Couldn't find data column with name : "
            + nameOrCode);
  }

  @Override
  public FTSDataColumnGroupI getDataColumnGroupById(String id)
          throws Exception
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    for (FTSDataColumnGroupI columnGroup : dataColumnGroups)
    {
      if (columnGroup.getID().equalsIgnoreCase(id))
      {
        return columnGroup;
      }
    }
    throw new Exception("Couldn't find data column group with id : " + id);
  }

  public String getMessageByHTTPStatusCode(int code, String service)
  {
    String message = "";
    switch (code)
    {
    case 400:
      message = MessageManager.getString("exception.bad_request");
      break;

    case 410:
      message = MessageManager.formatMessage(
              "exception.fts_rest_service_no_longer_available", service);
      break;
    case 403:
    case 404:
      message = MessageManager.getString("exception.resource_not_be_found");
      break;
    case 408:
    case 409:
    case 500:
    case 501:
    case 502:
    case 504:
    case 505:
      message = MessageManager.formatMessage("exception.fts_server_error",
              service);
      break;
    case 503:
      message = MessageManager.getString("exception.service_not_available");
      break;
    default:
      break;
    }
    return message;
  }

  protected String getResourceFile(String fileName)
  {
    String result = "";
    try
    {
      result = getClass().getResource(fileName).getFile();
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    return result;

  }

  @Override
  public int getDefaultResponsePageSize()
  {
    if (dataColumns == null || dataColumns.isEmpty())
    {
      parseDataColumnsConfigFile();
    }
    return defaultResponsePageSize;
  }

}
