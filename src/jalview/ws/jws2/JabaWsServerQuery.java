/*******************************************************************************
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.7)
 * Copyright (C) 2011 J Procter, AM Waterhouse, G Barton, M Clamp, S Searle
 *
 * This file is part of Jalview.
 *
 * Jalview is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Jalview is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty 
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Jalview.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
/**
 * 
 */
package jalview.ws.jws2;

import jalview.bin.Cache;

import compbio.data.msa.MsaWS;
import compbio.ws.client.Jws2Client;
import compbio.ws.client.Services;

/**
 * @author JimP
 *
 */
public class JabaWsServerQuery implements Runnable
{

  Jws2Discoverer jws2Discoverer=null;
  String jwsservers=null;
  boolean quit=false,
  running=false;
  /**
   * @return the running
   */
  public  boolean isRunning()
  {
    return running;
  }

  /**
   * @param quit the quit to set
   */
  public void setQuit(boolean quit)
  {
    this.quit = quit;
  }

  public JabaWsServerQuery(Jws2Discoverer jws2Discoverer, String jwsservers)
  {
    this.jws2Discoverer = jws2Discoverer;
    this.jwsservers=jwsservers;
  }

  /* (non-Javadoc)
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run()
  {
    running=true;
        try
    {
      if (Jws2Client.validURL(jwsservers))
      {
        boolean noservices=true;
        // look for services
        for (Services srv : Services.values())
        {
          if (quit)
          {
            running=false;
            return;
          }
          MsaWS service = null;
          try
          {
            service = Jws2Client.connect(jwsservers, srv);
          } catch (Exception e)
          {
            System.err.println("Jws2 Discoverer: Problem on "
                    + jwsservers + " with service " + srv + ":\n"
                    + e.getMessage());
            if (!(e instanceof javax.xml.ws.WebServiceException))
            {
              e.printStackTrace();
            }
            // For moment, report service as a problem.
            jws2Discoverer.addInvalidServiceUrl(jwsservers);
          }
          ;
          if (service != null)
          {
            noservices=false;
            jws2Discoverer.addService(jwsservers, srv, service);
          }
        }
        if (noservices)
        {
          jws2Discoverer.addUrlwithnoservices(jwsservers);
        }
      }
      else
      {
        jws2Discoverer.addInvalidServiceUrl(jwsservers);
        Cache.log.info("Ignoring invalid Jws2 service url " + jwsservers);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
      Cache.log.warn("Exception when discovering Jws2 services.", e);
      jws2Discoverer.addInvalidServiceUrl(jwsservers);
    } catch (Error e)
    {
      Cache.log.error("Exception when discovering Jws2 services.", e);
      jws2Discoverer.addInvalidServiceUrl(jwsservers);
    }
    running=false;
  }

}
