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
package jalview.ws.ebi;

import jalview.datamodel.DBRefSource;
import jalview.util.MessageManager;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class EBIFetchClient
{

  /**
   * Creates a new EBIFetchClient object.
   */
  public EBIFetchClient()
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedDBs()
  {
    // TODO - implement rest call for dbfetch getSupportedDBs
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedFormats()
  {
    // TODO - implement rest call for dbfetch getSupportedFormats
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public String[] getSupportedStyles()
  {
    // TODO - implement rest call for dbfetch getSupportedStyles
    throw new Error(MessageManager.getString("error.not_yet_implemented"));
  }

  /**
   * Send an HTTP fetch request to EBI and save the reply in a temporary file.
   * 
   * @param ids
   *          the query formatted as db:query1;query2;query3
   * @param format
   *          the format wanted
   * @param extension
   *          for the temporary file to hold response
   * @return the file holding the response
   * @throws OutOfMemoryError
   */

  public File fetchDataAsFile(String ids, String format, String ext)
          throws OutOfMemoryError
  {
    File outFile = null;
    try
    {
      outFile = File.createTempFile("jalview", ext);
      outFile.deleteOnExit();
      fetchData(ids, format, outFile);
      if (outFile.length() == 0)
      {
        outFile.delete();
        return null;
      }
    } catch (Exception ex)
    {
    }
    return outFile;
  }

  /**
   * Fetches queries and either saves the response to a file or returns as
   * string data
   * 
   * @param ids
   * @param format
   * @param outFile
   * @return
   * @throws OutOfMemoryError
   */
  String[] fetchData(String ids, String format, File outFile)
          throws OutOfMemoryError
  {
    StringBuilder querystring = new StringBuilder(ids.length());
    String database = parseIds(ids, querystring);
    if (database == null)
    {
      System.err.println("Invalid Query string : '" + ids + "'");
      System.err.println("Should be of form 'dbname:q1;q2;q3;q4'");
      return null;
    }

    // note: outFile is currently always specified, so return value is null
    String[] rslt = fetchBatch(querystring.toString(), database, format,
            outFile);

    return (rslt != null && rslt.length > 0 ? rslt : null);
  }

  /**
   * Parses ids formatted as dbname:q1;q2;q3, returns the dbname and adds
   * queries as comma-separated items to the querystring. dbname must be
   * specified for at least one queryId. Returns null if a mixture of different
   * dbnames is found (ignoring case).
   * 
   * @param ids
   * @param queryString
   * @return
   */
  static String parseIds(String ids, StringBuilder queryString)
  {
    String database = null;
    StringTokenizer queries = new StringTokenizer(ids, ";");
    boolean appending = queryString.length() > 0;
    while (queries.hasMoreTokens())
    {
      String query = queries.nextToken();
      int p = query.indexOf(':');
      if (p > -1)
      {
        String db = query.substring(0, p);
        if (database != null && !db.equalsIgnoreCase(database))
        {
          /*
           * different databases mixed in together - invalid
           */
          return null;
        }
        database = db;
        query = query.substring(p + 1);
      }
      queryString.append(appending ? "," : "");
      queryString.append(query);
      appending = true;
    }
    return database;
  }

  /**
   * Fetches queries and either saves the response to a file or (if no file
   * specified) returns as string data
   * 
   * @param ids
   * @param database
   * @param format
   * @param outFile
   * @return
   * @throws OutOfMemoryError
   */
  String[] fetchBatch(String ids, String database, String format,
          File outFile) throws OutOfMemoryError
  {
    // long time = System.currentTimeMillis();
    String url = buildUrl(ids, database, format);

    try
    {
      URL rcall = new URL(url);

      InputStream is = new BufferedInputStream(rcall.openStream());
      if (outFile != null)
      {
        FileOutputStream fio = new FileOutputStream(outFile);
        byte[] bb = new byte[32 * 1024];
        int l;
        while ((l = is.read(bb)) > 0)
        {
          fio.write(bb, 0, l);
        }
        fio.close();
        is.close();
      }
      else
      {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String rtn;
        List<String> arl = new ArrayList<String>();
        while ((rtn = br.readLine()) != null)
        {
          arl.add(rtn);
        }
        return arl.toArray(new String[arl.size()]);
      }
    } catch (OutOfMemoryError er)
    {
      System.out.println("OUT OF MEMORY DOWNLOADING QUERY FROM " + database
              + ":\n" + ids);
      throw er;
    } catch (Exception ex)
    {
      if (ex.getMessage().startsWith(
              "uk.ac.ebi.jdbfetch.exceptions.DbfNoEntryFoundException"))
      {
        return null;
      }
      System.err.println("Unexpected exception when retrieving from "
              + database + "\nQuery was : '" + ids + "'");
      ex.printStackTrace(System.err);
      return null;
    } finally
    {
      // System.err.println("EBIFetch took " + (System.currentTimeMillis() -
      // time) + " ms");
    }
    return null;
  }

  /**
   * Constructs the URL to fetch from
   * 
   * @param ids
   * @param database
   * @param format
   * @return
   */
  static String buildUrl(String ids, String database, String format)
  {
    String url;
    if (database.equalsIgnoreCase(DBRefSource.EMBL)
            || database.equalsIgnoreCase(DBRefSource.EMBLCDS))
    {
      url = "http://www.ebi.ac.uk/ena/data/view/" + ids.toLowerCase()
              + (format != null ? "&" + format : "");
    }
    else
    {
      url = "http://www.ebi.ac.uk/Tools/dbfetch/dbfetch/"
              + database.toLowerCase() + "/" + ids.toLowerCase()
              + (format != null ? "/" + format : "");
    }
    return url;
  }
}
