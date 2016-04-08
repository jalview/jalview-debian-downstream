/*
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
 */
package jalview.ws.jws2;

import javax.swing.JMenu;

import jalview.gui.AlignFrame;
import jalview.gui.WebserviceInfo;
import jalview.ws.jws2.Jws2Discoverer.Jws2Instance;

/**
 * provides metadata for a jws2 service instance - resolves names, etc.
 * 
 * @author JimP
 * 
 */
public abstract class Jws2Client extends jalview.ws.WSClient
{
  protected WebserviceInfo setWebService(Jws2Instance serv, boolean b)
  {
    // serviceHandle = serv;
    String serviceInstance = serv.service.getClass().getName();
    WebServiceName = serv.serviceType;
    WebServiceJobTitle = serv.getActionText();
    WsURL = serv.hosturl;
    if (!b)
    {
      return new WebserviceInfo(WebServiceJobTitle, WebServiceJobTitle
              + " using service hosted at " + serv.hosturl);
    }
    return null;
  }

  /*
   * Jws2Instance serviceHandle; (non-Javadoc)
   * 
   * @see jalview.ws.WSMenuEntryProviderI#attachWSMenuEntry(javax.swing.JMenu,
   * jalview.gui.AlignFrame)
   * 
   * @Override public void attachWSMenuEntry(JMenu wsmenu, AlignFrame
   * alignFrame) { if (serviceHandle==null) { throw new
   * Error("Implementation error: No service handle for this Jws2 service."); }
   * attachWSMenuEntry(wsmenu, serviceHandle, alignFrame); }
   */
  /**
   * add the menu item for a particular jws2 service instance
   * 
   * @param wsmenu
   * @param service
   * @param alignFrame
   */
  abstract void attachWSMenuEntry(JMenu wsmenu, final Jws2Instance service,
          final AlignFrame alignFrame);

}
