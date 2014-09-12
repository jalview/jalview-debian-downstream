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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Closeable;
import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.Level;

import jalview.bin.Cache;
import jalview.datamodel.AlignmentView;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.JalviewChangeSupport;
import jalview.gui.JvSwingUtils;
import jalview.ws.WSMenuEntryProviderI;
import jalview.ws.jws2.Jws2Discoverer.Jws2Instance;
import jalview.ws.params.ParamDatastoreI;
import compbio.data.msa.MsaWS;
import compbio.metadata.Option;
import compbio.metadata.Preset;
import compbio.metadata.PresetManager;
import compbio.metadata.RunnerConfig;
import compbio.ws.client.Jws2Client;
import compbio.ws.client.Services;

/**
 * discoverer for jws2 services. Follows the lightweight service discoverer
 * pattern (archetyped by EnfinEnvision2OneWay)
 * 
 * @author JimP
 * 
 */
public class Jws2Discoverer implements Runnable, WSMenuEntryProviderI
{
  private java.beans.PropertyChangeSupport changeSupport = new java.beans.PropertyChangeSupport(
          this);

  /**
   * change listeners are notified of "services" property changes
   * 
   * @param listener
   *          to be added that consumes new services Hashtable object.
   */
  public void addPropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.addPropertyChangeListener(listener);
  }

  /**
   * 
   * 
   * @param listener
   *          to be removed
   */
  public void removePropertyChangeListener(
          java.beans.PropertyChangeListener listener)
  {
    changeSupport.removePropertyChangeListener(listener);
  }

  boolean running = false, aborted = false;

  /**
   * @return the aborted
   */
  public boolean isAborted()
  {
    return aborted;
  }

  /**
   * @param aborted
   *          the aborted to set
   */
  public void setAborted(boolean aborted)
  {
    this.aborted = aborted;
  }

  Thread oldthread = null;

  public void run()
  {
    if (running && oldthread != null && oldthread.isAlive())
    {
      if (!aborted)
      {
        return;
      }
      while (running)
      {
        try
        {
          Cache.log
                  .debug("Waiting around for old discovery thread to finish.");
          // wait around until old discoverer dies
          Thread.sleep(100);
        } catch (Exception e)
        {
        }
      }
      Cache.log.debug("Old discovery thread has finished.");
    }
    running = true;
    oldthread = Thread.currentThread();
    try
    {
      Class foo = getClass().getClassLoader().loadClass(
              "compbio.ws.client.Jws2Client");
    } catch (ClassNotFoundException e)
    {
      System.err
              .println("Not enabling JABA Webservices : client jar is not available."
                      + "\nPlease check that your webstart JNLP file is up to date!");
      running = false;
      return;
    }
    // reinitialise records of good and bad service URLs
    if (services != null)
    {
      services.removeAllElements();
    }
    if (urlsWithoutServices != null)
    {
      urlsWithoutServices.removeAllElements();
    }
    if (invalidServiceUrls != null)
    {
      invalidServiceUrls.removeAllElements();
    }

    List<JabaWsServerQuery> qrys = new ArrayList<JabaWsServerQuery>();
    for (final String jwsservers : getServiceUrls())
    {
      JabaWsServerQuery squery = new JabaWsServerQuery(this, jwsservers);
      qrys.add(squery);
      new Thread(squery).start();
    }
    boolean finished = false;
    do
    {
      try
      {
        Thread.sleep(100);
      } catch (Exception e)
      {
      }
      ;
      for (JabaWsServerQuery squery : qrys)
      {
        finished |= !squery.isRunning();
      }
      if (aborted)
      {
        Cache.log.debug("Aborting " + qrys.size()
                + " JABAWS discovery threads.");
        for (JabaWsServerQuery squery : qrys)
        {
          squery.setQuit(true);
        }
      }
    } while (!aborted && !finished);
    oldthread = null;
    running = false;
    if (!aborted)
    {
      changeSupport.firePropertyChange("services", new Vector(), services);
    }
  }

  /**
   * record this service endpoint so we can use it
   * 
   * @param jwsservers
   * @param srv
   * @param service2
   */
  synchronized void addService(String jwsservers, Services srv,
          MsaWS service2)
  {
    if (services == null)
    {
      services = new Vector<Jws2Instance>();
    }
    System.out.println("Discovered service: " + jwsservers + " "
            + srv.toString());
    Jws2Instance service = new Jws2Instance(jwsservers, srv.toString(),
            service2);

    services.add(service);
    // retrieve the presets and parameter set and cache now
    service.getParamStore().getPresets();
    service.hasParameters();
  }

  public class Jws2Instance
  {
    public String hosturl;

    public String serviceType;

    public MsaWS service;

    public Jws2Instance(String hosturl, String serviceType, MsaWS service)
    {
      super();
      this.hosturl = hosturl;
      this.serviceType = serviceType;
      this.service = service;
    }

    PresetManager presets = null;

    public JabaParamStore paramStore = null;

    /**
     * non thread safe - gets the presets for this service (blocks whilst it
     * calls the service to get the preset set)
     * 
     * @return service presets or null if exceptions were raised.
     */
    public PresetManager getPresets()
    {
      if (presets == null)
      {
        try
        {
          presets = service.getPresets();
        } catch (Exception ex)
        {
          System.err
                  .println("Exception when retrieving presets for service "
                          + serviceType + " at " + hosturl);
        }
      }
      return presets;
    }

    public String getHost()
    {
      return hosturl;
      /*
       * try { URL serviceurl = new URL(hosturl); if (serviceurl.getPort()!=80)
       * { return serviceurl.getHost()+":"+serviceurl.getPort(); } return
       * serviceurl.getHost(); } catch (Exception e) {
       * System.err.println("Failed to parse service URL '" + hosturl +
       * "' as a valid URL!"); } return null;
       */
    }

    /**
     * @return short description of what the service will do
     */
    public String getActionText()
    {
      return "Align with " + serviceType;
    }

    /**
     * non-thread safe - blocks whilst accessing service to get complete set of
     * available options and parameters
     * 
     * @return
     */
    public RunnerConfig getRunnerConfig()
    {
      return service.getRunnerOptions();
    }

    @Override
    protected void finalize() throws Throwable
    {
      if (service != null)
      {
        try
        {
          Closeable svc = (Closeable) service;
          service = null;
          svc.close();
        } catch (Exception e)
        {
        }
        ;
      }
      super.finalize();
    }

    public ParamDatastoreI getParamStore()
    {
      if (paramStore == null)
      {
        try
        {
          paramStore = new JabaParamStore(this,
                  (Desktop.instance != null ? Desktop
                          .getUserParameterStore() : null));
        } catch (Exception ex)
        {
        }

      }
      return paramStore;
    }

    public String getUri()
    {
      // this is only valid for Jaba 1.0 - this formula might have to change!
      return hosturl
              + (hosturl.lastIndexOf("/") == (hosturl.length() - 1) ? ""
                      : "/") + serviceType;
    }

    private boolean hasParams = false, lookedForParams = false;

    public boolean hasParameters()
    {
      if (!lookedForParams)
      {
        lookedForParams = true;
        try
        {
          hasParams = (getRunnerConfig().getArguments().size() > 0);
        } catch (Exception e)
        {

        }
      }
      return hasParams;
    }
  };

  /**
   * holds list of services.
   */
  protected Vector<Jws2Instance> services;

  public void attachWSMenuEntry(JMenu wsmenu, final AlignFrame alignFrame)
  {
    // dynamically regenerate service list.
    final JMenu jws2al = wsmenu; // new JMenu("JABAWS Alignment");
    jws2al.addMenuListener(new MenuListener()
    {
      // TODO: future: add menu listener to parent menu - so submenus are
      // populated *before* they are selected.
      @Override
      public void menuSelected(MenuEvent e)
      {
        populateWSMenuEntry(jws2al, alignFrame);
      }

      @Override
      public void menuDeselected(MenuEvent e)
      {
        // TODO Auto-generated method stub

      }

      @Override
      public void menuCanceled(MenuEvent e)
      {
        // TODO Auto-generated method stub

      }

    });
    wsmenu.add(jws2al);

  }

  private void populateWSMenuEntry(JMenu jws2al, final AlignFrame alignFrame)
  {
    if (running || services == null || services.size() == 0)
    {
      return;
    }
    boolean byhost = Cache.getDefault("WSMENU_BYHOST", false), bytype = Cache
            .getDefault("WSMENU_BYTYPE", false);
    /**
     * eventually, JWS2 services will appear under the same align/etc submenus.
     * for moment we keep them separate.
     */
    JMenu atpoint;
    MsaWSClient msacl = new MsaWSClient();
    Vector hostLabels = new Vector();
    jws2al.removeAll();
    String lasthost = null;
    Hashtable<String, ArrayList<Jws2Instance>> hosts = new Hashtable<String, ArrayList<Jws2Instance>>();
    String[] sorton;
    for (Jws2Instance service : services)
    {
      ArrayList<Jws2Instance> hostservices = hosts.get(service.getHost());
      if (hostservices == null)
      {
        hosts.put(service.getHost(),
                hostservices = new ArrayList<Jws2Instance>());
      }
      hostservices.add(service);
    }
    sorton = hosts.keySet().toArray(new String[1]);
    String hostlist[] = sorton.clone();
    jalview.util.QuickSort.sort(sorton, hostlist);
    for (String host : hostlist)
    {
      Jws2Instance orderedsvcs[] = hosts.get(host).toArray(
              new Jws2Instance[1]);
      String sortbytype[] = new String[orderedsvcs.length];
      for (int i = 0; i < sortbytype.length; i++)
      {
        sortbytype[i] = orderedsvcs[i].serviceType;
      }
      jalview.util.QuickSort.sort(sortbytype, orderedsvcs);
      for (final Jws2Instance service : orderedsvcs)
      {
        atpoint = jws2al;
        String type = service.serviceType;
        if (byhost)
        {
          atpoint = JvSwingUtils.findOrCreateMenu(atpoint, host);
          if (atpoint.getToolTipText() == null)
          {
            atpoint.setToolTipText("Services at " + host);
          }
        }
        if (bytype)
        {
          atpoint = JvSwingUtils.findOrCreateMenu(atpoint, type);
          if (atpoint.getToolTipText() == null)
          {
            atpoint.setToolTipText(service.getActionText());
          }
        }
        if (!byhost
                && !hostLabels.contains(host + service.serviceType
                        + service.getActionText()))
        // !hostLabels.contains(host + (bytype ?
        // service.serviceType+service.getActionText() : "")))
        {
          // add a marker indicating where this service is hosted
          // relies on services from the same host being listed in a
          // contiguous
          // group
          JMenuItem hitm;
          atpoint.addSeparator();
          if (lasthost == null || !lasthost.equals(host))
          {
            atpoint.add(hitm = new JMenuItem(host));
            hitm.setForeground(Color.blue);
            hitm.addActionListener(new ActionListener()
            {

              @Override
              public void actionPerformed(ActionEvent e)
              {
                Desktop.showUrl(service.getHost());
              }
            });
            hitm.setToolTipText(JvSwingUtils
                    .wrapTooltip("Opens the JABAWS server's homepage in web browser"));
            lasthost = host;
          }
          hostLabels.addElement(host + service.serviceType
                  + service.getActionText());
          // hostLabels.addElement(host + (bytype ?
          // service.serviceType+service.getActionText() : ""));
        }
        msacl.attachWSMenuEntry(atpoint, service, alignFrame);
        /*
         * JMenuItem sitem = new JMenuItem(service.serviceType);
         * sitem.setToolTipText("Hosted at " + service.hosturl);
         * sitem.addActionListener(new ActionListener() {
         * 
         * @Override public void actionPerformed(ActionEvent e) { AlignmentView
         * msa = alignFrame.gatherSequencesForAlignment(); MsaWSClient client =
         * new MsaWSClient(service, "JWS2 Alignment of " +
         * alignFrame.getTitle(), msa, false, true,
         * alignFrame.getViewport().getAlignment().getDataset(), alignFrame); }
         * });
         */
      }
    }

  }

  public static void main(String[] args)
  {
    Thread runner = getDiscoverer().startDiscoverer(
            new PropertyChangeListener()
            {

              public void propertyChange(PropertyChangeEvent evt)
              {
                if (getDiscoverer().services != null)
                {
                  System.out.println("Changesupport: There are now "
                          + getDiscoverer().services.size() + " services");

                }
              }
            });
    while (runner.isAlive())
    {
      try
      {
        Thread.sleep(50);
      } catch (InterruptedException e)
      {
      }
      ;
    }
  }

  private static Jws2Discoverer discoverer;

  public static Jws2Discoverer getDiscoverer()
  {
    if (discoverer == null)
    {
      discoverer = new Jws2Discoverer();
    }
    return discoverer;
  }

  public boolean hasServices()
  {
    // TODO Auto-generated method stub
    return !running && services != null && services.size() > 0;
  }

  public boolean isRunning()
  {
    return running;
  }

  /**
   * the jalview .properties entry for JWS2 URLS
   */
  final static String JWS2HOSTURLS = "JWS2HOSTURLS";

  public static void setServiceUrls(Vector<String> urls)
  {
    if (urls != null)
    {
      StringBuffer urlbuffer = new StringBuffer();
      String sep = "";
      for (String url : urls)
      {
        urlbuffer.append(sep);
        urlbuffer.append(url);
        sep = ",";
      }
      Cache.setProperty(JWS2HOSTURLS, urlbuffer.toString());
    }
    else
    {
      Cache.removeProperty(JWS2HOSTURLS);
    }
  }

  public static Vector<String> getServiceUrls()
  {
    String surls = Cache.getDefault(JWS2HOSTURLS,
            "http://www.compbio.dundee.ac.uk/jabaws");
    Vector<String> urls = new Vector<String>();
    try
    {
      StringTokenizer st = new StringTokenizer(surls, ",");
      while (st.hasMoreElements())
      {
        String url = null;
        try
        {
          java.net.URL u = new java.net.URL(url = st.nextToken());
          if (!urls.contains(url))
          {
            urls.add(url);
          }
          else
          {
            jalview.bin.Cache.log.info("Ignoring duplicate url in "
                    + JWS2HOSTURLS + " list");
          }
        } catch (Exception ex)
        {
          jalview.bin.Cache.log
                  .warn("Problem whilst trying to make a URL from '"
                          + ((url != null) ? url : "<null>") + "'");
          jalview.bin.Cache.log
                  .warn("This was probably due to a malformed comma separated list"
                          + " in the "
                          + JWS2HOSTURLS
                          + " entry of $(HOME)/.jalview_properties)");
          jalview.bin.Cache.log.debug("Exception was ", ex);
        }
      }
    } catch (Exception ex)
    {
      jalview.bin.Cache.log.warn(
              "Error parsing comma separated list of urls in "
                      + JWS2HOSTURLS + " preference.", ex);
    }
    if (urls.size() >= 0)
    {
      return urls;
    }
    return null;
  }

  public Vector<Jws2Instance> getServices()
  {
    return (services == null) ? new Vector<Jws2Instance>()
            : new Vector<Jws2Instance>(services);
  }

  /**
   * test the given URL with the JabaWS test code
   * 
   * @param foo
   * @return
   */
  public static boolean testServiceUrl(URL foo)
  {
    try
    {
      compbio.ws.client.WSTester.main(new String[]
      { "-h=" + foo.toString() });
    } catch (Exception e)
    {
      e.printStackTrace();
      return false;
    } catch (OutOfMemoryError e)
    {
      e.printStackTrace();
      return false;
    } catch (Error e)
    {
      e.printStackTrace();
      return false;
    }

    return true;
  }

  /**
   * Start a fresh discovery thread and notify the given object when we're
   * finished. Any known existing threads will be killed before this one is
   * started.
   * 
   * @param changeSupport2
   * @return new thread
   */
  public Thread startDiscoverer(PropertyChangeListener changeSupport2)
  {
    if (isRunning())
    {
      setAborted(true);
    }
    addPropertyChangeListener(changeSupport2);
    Thread thr = new Thread(this);
    thr.start();
    return thr;
  }

  Vector<String> invalidServiceUrls = null, urlsWithoutServices = null;

  /**
   * @return the invalidServiceUrls
   */
  public Vector<String> getInvalidServiceUrls()
  {
    return invalidServiceUrls;
  }

  /**
   * @return the urlsWithoutServices
   */
  public Vector<String> getUrlsWithoutServices()
  {
    return urlsWithoutServices;
  }

  /**
   * add an 'empty' JABA server to the list. Only servers not already in the
   * 'bad URL' list will be added to this list.
   * 
   * @param jwsservers
   */
  public synchronized void addUrlwithnoservices(String jwsservers)
  {
    if (urlsWithoutServices == null)
    {
      urlsWithoutServices = new Vector<String>();
    }

    if ((invalidServiceUrls == null || !invalidServiceUrls
            .contains(jwsservers))
            && !urlsWithoutServices.contains(jwsservers))
    {
      urlsWithoutServices.add(jwsservers);
    }
  }

  /**
   * add a bad URL to the list
   * 
   * @param jwsservers
   */
  public synchronized void addInvalidServiceUrl(String jwsservers)
  {
    if (invalidServiceUrls == null)
    {
      invalidServiceUrls = new Vector<String>();
    }
    if (!invalidServiceUrls.contains(jwsservers))
    {
      invalidServiceUrls.add(jwsservers);
    }
  }

  /**
   * 
   * @return a human readable report of any problems with the service URLs used
   *         for discovery
   */
  public String getErrorMessages()
  {
    if (!isRunning() && !isAborted())
    {
      StringBuffer ermsg = new StringBuffer();
      boolean list = false;
      if (getInvalidServiceUrls() != null
              && getInvalidServiceUrls().size() > 0)
      {
        ermsg.append("URLs that could not be contacted: \n");
        for (String svcurl : getInvalidServiceUrls())
        {
          if (list)
          {
            ermsg.append(", ");
          }
          list = true;
          ermsg.append(svcurl);
        }
        ermsg.append("\n\n");
      }
      list = false;
      if (getUrlsWithoutServices() != null
              && getUrlsWithoutServices().size() > 0)
      {
        ermsg.append("URLs without any JABA Services : \n");
        for (String svcurl : getUrlsWithoutServices())
        {
          if (list)
          {
            ermsg.append(", ");
          }
          list = true;
          ermsg.append(svcurl);
        }
        ermsg.append("\n");
      }
      if (ermsg.length() > 1)
      {
        return ermsg.toString();
      }

    }
    return null;
  }

}
