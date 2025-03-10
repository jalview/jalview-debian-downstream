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

import jalview.util.StringUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.HttpMethod;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Base class for Ensembl REST service clients
 * 
 * @author gmcarstairs
 */
abstract class EnsemblRestClient extends EnsemblSequenceFetcher
{
  private static final int DEFAULT_READ_TIMEOUT = 5 * 60 * 1000; // 5 minutes

  private static final int CONNECT_TIMEOUT_MS = 10 * 1000; // 10 seconds

  private static final int MAX_RETRIES = 3;

  private static final int HTTP_OK = 200;

  private static final int HTTP_OVERLOAD = 429;

  /*
   * update these constants when Jalview has been checked / updated for
   * changes to Ensembl REST API, and updated JAL-3018
   * @see https://github.com/Ensembl/ensembl-rest/wiki/Change-log
   * @see http://rest.ensembl.org/info/rest?content-type=application/json
   */
  private static final String LATEST_ENSEMBLGENOMES_REST_VERSION = "10.0";

  private static final String LATEST_ENSEMBL_REST_VERSION = "10.0";

  private static final String REST_CHANGE_LOG = "https://github.com/Ensembl/ensembl-rest/wiki/Change-log";

  private static Map<String, EnsemblData> domainData;

  private final static long AVAILABILITY_RETEST_INTERVAL = 10000L; // 10 seconds

  private final static long VERSION_RETEST_INTERVAL = 1000L * 3600; // 1 hr

  protected static final String CONTENT_TYPE_JSON = "?content-type=application/json";

  static
  {
    domainData = new HashMap<>();
    domainData.put(DEFAULT_ENSEMBL_BASEURL,
            new EnsemblData(DEFAULT_ENSEMBL_BASEURL, LATEST_ENSEMBL_REST_VERSION));
    domainData.put(DEFAULT_ENSEMBL_GENOMES_BASEURL, new EnsemblData(
            DEFAULT_ENSEMBL_GENOMES_BASEURL, LATEST_ENSEMBLGENOMES_REST_VERSION));
  }

  protected volatile boolean inProgress = false;

  /**
   * Default constructor to use rest.ensembl.org
   */
  public EnsemblRestClient()
  {
    super();

    /*
     * initialise domain info lazily
     */
    if (!domainData.containsKey(ensemblDomain))
    {
      domainData.put(ensemblDomain,
              new EnsemblData(ensemblDomain, LATEST_ENSEMBL_REST_VERSION));
    }
    if (!domainData.containsKey(ensemblGenomesDomain))
    {
      domainData.put(ensemblGenomesDomain, new EnsemblData(
              ensemblGenomesDomain, LATEST_ENSEMBLGENOMES_REST_VERSION));
    }
  }

  /**
   * Constructor given the target domain to fetch data from
   * 
   * @param d
   */
  public EnsemblRestClient(String d)
  {
    setDomain(d);
  }

  @Override
  public boolean queryInProgress()
  {
    return inProgress;
  }

  @Override
  public StringBuffer getRawRecords()
  {
    return null;
  }

  /**
   * Returns the URL for the client http request
   * 
   * @param ids
   * @return
   * @throws MalformedURLException
   */
  protected abstract URL getUrl(List<String> ids)
          throws MalformedURLException;

  /**
   * Returns true if client uses GET method, false if it uses POST
   * 
   * @return
   */
  protected abstract boolean useGetRequest();

  /**
   * Returns the desired value for the Content-Type request header. Default is
   * application/json, override if required to vary this.
   * 
   * @return
   * @see https://github.com/Ensembl/ensembl-rest/wiki/HTTP-Headers
   */
  protected String getRequestMimeType()
  {
    return "application/json";
  }

  /**
   * Return the desired value for the Accept request header. Default is
   * application/json, override if required to vary this.
   * 
   * @return
   * @see https://github.com/Ensembl/ensembl-rest/wiki/HTTP-Headers
   */
  protected String getResponseMimeType()
  {
    return "application/json";
  }

  /**
   * Checks Ensembl's REST 'ping' endpoint, and returns true if response
   * indicates available, else false
   * 
   * @see http://rest.ensembl.org/documentation/info/ping
   * @return
   */
  boolean checkEnsembl()
  {
    BufferedReader br = null;
    String pingUrl = getDomain() + "/info/ping" + CONTENT_TYPE_JSON;
    try
    {
      // note this format works for both ensembl and ensemblgenomes
      // info/ping.json works for ensembl only (March 2016)
      URL ping = new URL(pingUrl);

      /*
       * expect {"ping":1} if ok
       * if ping takes more than 2 seconds to respond, treat as if unavailable
       */
      br = getHttpResponse(ping, null, 2 * 1000);
      if (br == null)
      {
        // error reponse status
        return false;
      }
      JSONParser jp = new JSONParser();
      JSONObject val = (JSONObject) jp.parse(br);
      String pingString = val.get("ping").toString();
      return pingString != null;
    } catch (Throwable t)
    {
      System.err.println(
              "Error connecting to " + pingUrl + ": " + t.getMessage());
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
    return false;
  }

  /**
   * Returns a reader to a (Json) response from the Ensembl sequence endpoint.
   * If the request failed the return value may be null.
   * 
   * @param ids
   * @return
   * @throws IOException
   */
  protected BufferedReader getSequenceReader(List<String> ids)
          throws IOException
  {
    URL url = getUrl(ids);

    BufferedReader reader = getHttpResponse(url, ids);
    return reader;
  }

  /**
   * Gets a reader to the HTTP response, using the default read timeout of 5
   * minutes
   * 
   * @param url
   * @param ids
   * @return
   * @throws IOException
   */
  protected BufferedReader getHttpResponse(URL url, List<String> ids)
          throws IOException
  {
    return getHttpResponse(url, ids, DEFAULT_READ_TIMEOUT);
  }

  /**
   * Sends the HTTP request and gets the response as a reader. Returns null if
   * the HTTP response code was not 200.
   * 
   * @param url
   * @param ids
   *          written as Json POST body if more than one
   * @param readTimeout
   *          in milliseconds
   * @return
   * @throws IOException
   */
  protected BufferedReader getHttpResponse(URL url, List<String> ids,
          int readTimeout) throws IOException
  {
    int retriesLeft = MAX_RETRIES;
    HttpURLConnection connection = null;
    int responseCode = 0;

    while (retriesLeft > 0)
    {
      connection = tryConnection(url, ids, readTimeout);
      responseCode = connection.getResponseCode();
      if (responseCode == HTTP_OVERLOAD) // 429
      {
        retriesLeft--;
        checkRetryAfter(connection);
      }
      else
      {
        retriesLeft = 0;
      }
    }
    if (responseCode != HTTP_OK) // 200
    {
      /*
       * note: a GET request for an invalid id returns an error code e.g. 415
       * but POST request returns 200 and an empty Fasta response 
       */
      System.err.println("Response code " + responseCode + " for " + url);
      return null;
    }

    InputStream response = connection.getInputStream();

    // System.out.println(getClass().getName() + " took "
    // + (System.currentTimeMillis() - now) + "ms to fetch");

    BufferedReader reader = null;
    reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
    return reader;
  }

  /**
   * @param url
   * @param ids
   * @param readTimeout
   * @return
   * @throws IOException
   * @throws ProtocolException
   */
  protected HttpURLConnection tryConnection(URL url, List<String> ids,
          int readTimeout) throws IOException, ProtocolException
  {
    // System.out.println(System.currentTimeMillis() + " " + url);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

    /*
     * POST method allows multiple queries in one request; it is supported for
     * sequence queries, but not for overlap
     */
    boolean multipleIds = ids != null && ids.size() > 1;
    connection.setRequestMethod(
            multipleIds ? HttpMethod.POST : HttpMethod.GET);
    connection.setRequestProperty("Content-Type", getRequestMimeType());
    connection.setRequestProperty("Accept", getResponseMimeType());

    connection.setUseCaches(false);
    connection.setDoInput(true);
    connection.setDoOutput(multipleIds);

    connection.setConnectTimeout(CONNECT_TIMEOUT_MS);
    connection.setReadTimeout(readTimeout);

    if (multipleIds)
    {
      writePostBody(connection, ids);
    }
    return connection;
  }

  /**
   * Inspects response headers for a 'retry-after' directive, and waits for the
   * directed period (if less than 10 seconds)
   * 
   * @see https://github.com/Ensembl/ensembl-rest/wiki/Rate-Limits
   * @param connection
   */
  void checkRetryAfter(HttpURLConnection connection)
  {
    String retryDelay = connection.getHeaderField("Retry-After");

    // to test:
    // retryDelay = "5";

    if (retryDelay != null)
    {
      try
      {
        int retrySecs = Integer.valueOf(retryDelay);
        if (retrySecs > 0 && retrySecs < 10)
        {
          System.err
                  .println("Ensembl REST service rate limit exceeded, waiting "
                          + retryDelay + " seconds before retrying");
          Thread.sleep(1000 * retrySecs);
        }
      } catch (NumberFormatException | InterruptedException e)
      {
        System.err.println("Error handling Retry-After: " + e.getMessage());
      }
    }
  }

  /**
   * Rechecks if Ensembl is responding, unless the last check was successful and
   * the retest interval has not yet elapsed. Returns true if Ensembl is up,
   * else false. Also retrieves and saves the current version of Ensembl data
   * and REST services at intervals.
   * 
   * @return
   */
  protected boolean isEnsemblAvailable()
  {
    EnsemblData info = domainData.get(getDomain());

    long now = System.currentTimeMillis();

    /*
     * recheck if Ensembl is up if it was down, or the recheck period has elapsed
     */
    boolean retestAvailability = (now
            - info.lastAvailableCheckTime) > AVAILABILITY_RETEST_INTERVAL;
    if (!info.restAvailable || retestAvailability)
    {
      info.restAvailable = checkEnsembl();
      info.lastAvailableCheckTime = now;
    }

    /*
     * refetch Ensembl versions if the recheck period has elapsed
     */
    boolean refetchVersion = (now
            - info.lastVersionCheckTime) > VERSION_RETEST_INTERVAL;
    if (refetchVersion)
    {
      checkEnsemblRestVersion();
      checkEnsemblDataVersion();
      info.lastVersionCheckTime = now;
    }

    return info.restAvailable;
  }

  /**
   * Constructs, writes and flushes the POST body of the request, containing the
   * query ids in JSON format
   * 
   * @param connection
   * @param ids
   * @throws IOException
   */
  protected void writePostBody(HttpURLConnection connection,
          List<String> ids) throws IOException
  {
    boolean first;
    StringBuilder postBody = new StringBuilder(64);
    postBody.append("{\"ids\":[");
    first = true;
    for (String id : ids)
    {
      if (!first)
      {
        postBody.append(",");
      }
      first = false;
      postBody.append("\"");
      postBody.append(id.trim());
      postBody.append("\"");
    }
    postBody.append("]}");
    byte[] thepostbody = postBody.toString().getBytes();
    connection.setRequestProperty("Content-Length",
            Integer.toString(thepostbody.length));
    DataOutputStream wr = new DataOutputStream(
            connection.getOutputStream());
    wr.write(thepostbody);
    wr.flush();
    wr.close();
  }

  /**
   * Fetches and checks Ensembl's REST version number
   * 
   * @return
   */
  private void checkEnsemblRestVersion()
  {
    EnsemblData info = domainData.get(getDomain());

    JSONParser jp = new JSONParser();
    URL url = null;
    try
    {
      url = new URL(getDomain() + "/info/rest" + CONTENT_TYPE_JSON);
      BufferedReader br = getHttpResponse(url, null);
      if (br == null)
      {
        return;
      }
      JSONObject val = (JSONObject) jp.parse(br);
      String version = val.get("release").toString();
      String majorVersion = version.substring(0, version.indexOf("."));
      String expected = info.expectedRestVersion;
      String expectedMajorVersion = expected.substring(0,
              expected.indexOf("."));
      info.restMajorVersionMismatch = false;
      try
      {
        /*
         * if actual REST major version is ahead of what we expect,
         * record this in case we want to warn the user
         */
        if (Float.valueOf(majorVersion) > Float
                .valueOf(expectedMajorVersion))
        {
          info.restMajorVersionMismatch = true;
        }
      } catch (NumberFormatException e)
      {
        System.err.println("Error in REST version: " + e.toString());
      }

      /*
       * check if REST version is later than what Jalview has tested against,
       * if so warn; we don't worry if it is earlier (this indicates Jalview has
       * been tested in advance against the next pending REST version)
       */
      boolean laterVersion = StringUtils.compareVersions(version,
              expected) == 1;
      if (laterVersion)
      {
        System.err.println(String.format(
                "EnsemblRestClient expected %s REST version %s but found %s, see %s",
                getDbSource(), expected, version, REST_CHANGE_LOG));
      }
      info.restVersion = version;
    } catch (Throwable t)
    {
      System.err.println(
              "Error checking Ensembl REST version: " + t.getMessage());
    }
  }

  public boolean isRestMajorVersionMismatch()
  {
    return domainData.get(getDomain()).restMajorVersionMismatch;
  }

  /**
   * Fetches and checks Ensembl's data version number
   * 
   * @return
   */
  private void checkEnsemblDataVersion()
  {
    JSONParser jp = new JSONParser();
    URL url = null;
    BufferedReader br = null;

    try
    {
      url = new URL(getDomain() + "/info/data" + CONTENT_TYPE_JSON);
      br = getHttpResponse(url, null);
      if (br != null)
      {
        JSONObject val = (JSONObject) jp.parse(br);
        JSONArray versions = (JSONArray) val.get("releases");
        domainData.get(getDomain()).dataVersion = versions.get(0)
                .toString();
      }
    } catch (Throwable t)
    {
      System.err.println(
              "Error checking Ensembl data version: " + t.getMessage());
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

  public String getEnsemblDataVersion()
  {
    return domainData.get(getDomain()).dataVersion;
  }

  @Override
  public String getDbVersion()
  {
    return getEnsemblDataVersion();
  }

}
