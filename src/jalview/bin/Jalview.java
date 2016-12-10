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
package jalview.bin;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;

import jalview.ext.so.SequenceOntology;
import jalview.gui.AlignFrame;
import jalview.gui.Desktop;
import jalview.gui.PromptUserConfig;
import jalview.io.AppletFormatAdapter;
import jalview.io.BioJsHTMLOutput;
import jalview.io.FileLoader;
import jalview.io.FormatAdapter;
import jalview.io.HtmlSvgOutput;
import jalview.io.IdentifyFile;
import jalview.io.NewickFile;
import jalview.io.gff.SequenceOntologyFactory;
import jalview.schemes.ColourSchemeI;
import jalview.schemes.ColourSchemeProperty;
import jalview.schemes.UserColourScheme;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.ws.jws2.Jws2Discoverer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.UIManager;

/**
 * Main class for Jalview Application <br>
 * <br>
 * start with java -Djava.ext.dirs=$PATH_TO_LIB$ jalview.bin.Jalview
 * 
 * @author $author$
 * @version $Revision$
 */
public class Jalview
{
  /*
   * singleton instance of this class
   */
  private static Jalview instance;

  private Desktop desktop;

  public static AlignFrame currentAlignFrame;

  static
  {
    // grab all the rights we can the JVM
    Policy.setPolicy(new Policy()
    {
      @Override
      public PermissionCollection getPermissions(CodeSource codesource)
      {
        Permissions perms = new Permissions();
        perms.add(new AllPermission());
        return (perms);
      }

      @Override
      public void refresh()
      {
      }
    });
  }

  /**
   * keep track of feature fetching tasks.
   * 
   * @author JimP
   * 
   */
  class FeatureFetcher
  {
    /*
     * TODO: generalise to track all jalview events to orchestrate batch
     * processing events.
     */

    private int queued = 0;

    private int running = 0;

    public FeatureFetcher()
    {

    }

    public void addFetcher(final AlignFrame af,
            final Vector<String> dasSources)
    {
      final long id = System.currentTimeMillis();
      queued++;
      final FeatureFetcher us = this;
      new Thread(new Runnable()
      {

        @Override
        public void run()
        {
          synchronized (us)
          {
            queued--;
            running++;
          }

          af.setProgressBar(MessageManager
                  .getString("status.das_features_being_retrived"), id);
          af.featureSettings_actionPerformed(null);
          af.featureSettings.fetchDasFeatures(dasSources, true);
          af.setProgressBar(null, id);
          synchronized (us)
          {
            running--;
          }
        }
      }).start();
    }

    public synchronized boolean allFinished()
    {
      return queued == 0 && running == 0;
    }

  }

  public static Jalview getInstance()
  {
    return instance;
  }

  /**
   * main class for Jalview application
   * 
   * @param args
   *          open <em>filename</em>
   */
  public static void main(String[] args)
  {
    instance = new Jalview();
    instance.doMain(args);
  }

  /**
   * @param args
   */
  void doMain(String[] args)
  {
    System.setSecurityManager(null);
    System.out.println("Java version: "
            + System.getProperty("java.version"));
    System.out.println(System.getProperty("os.arch") + " "
            + System.getProperty("os.name") + " "
            + System.getProperty("os.version"));

    ArgsParser aparser = new ArgsParser(args);
    boolean headless = false;

    if (aparser.contains("help") || aparser.contains("h"))
    {
      showUsage();
      System.exit(0);
    }
    if (aparser.contains("nodisplay") || aparser.contains("nogui")
            || aparser.contains("headless"))
    {
      System.setProperty("java.awt.headless", "true");
      headless = true;
    }
    String usrPropsFile = aparser.getValue("props");
    Cache.loadProperties(usrPropsFile); // must do this before
    if (usrPropsFile != null)
    {
      System.out.println("CMD [-props " + usrPropsFile
              + "] executed successfully!");
    }

    // anything else!

    final String jabawsUrl = aparser.getValue("jabaws");
    if (jabawsUrl != null)
    {
      try
      {
        Jws2Discoverer.getDiscoverer().setPreferredUrl(jabawsUrl);
        System.out.println("CMD [-jabaws " + jabawsUrl
                + "] executed successfully!");
      } catch (MalformedURLException e)
      {
        System.err.println("Invalid jabaws parameter: " + jabawsUrl
                + " ignored");
      }
    }

    String defs = aparser.getValue("setprop");
    while (defs != null)
    {
      int p = defs.indexOf('=');
      if (p == -1)
      {
        System.err.println("Ignoring invalid setprop argument : " + defs);
      }
      else
      {
        System.out.println("Executing setprop argument: " + defs);
        // DISABLED FOR SECURITY REASONS
        // TODO: add a property to allow properties to be overriden by cli args
        // Cache.setProperty(defs.substring(0,p), defs.substring(p+1));
      }
      defs = aparser.getValue("setprop");
    }
    if (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true"))
    {
      headless = true;
    }
    System.setProperty("http.agent",
            "Jalview Desktop/" + Cache.getDefault("VERSION", "Unknown"));
    try
    {
      Cache.initLogger();
    } catch (NoClassDefFoundError error)
    {
      error.printStackTrace();
      System.out
              .println("\nEssential logging libraries not found."
                      + "\nUse: java -Djava.ext.dirs=$PATH_TO_LIB$ jalview.bin.Jalview");
      System.exit(0);
    }

    desktop = null;

    try
    {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception ex)
    {
    }
    if (Platform.isAMac())
    {
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
              "Jalview");
      System.setProperty("apple.laf.useScreenMenuBar", "true");
      try
      {
        UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager
                .getLookAndFeel());
      } catch (Throwable e)
      {
        System.err.println("Failed to set QuaQua look and feel: "
                + e.toString());
      }
    }

    /*
     * configure 'full' SO model if preferences say to, 
     * else use the default (SO Lite)
     */
    if (Cache.getDefault("USE_FULL_SO", false))
    {
      SequenceOntologyFactory.setInstance(new SequenceOntology());
    }

    if (!headless)
    {
      desktop = new Desktop();
      desktop.setInBatchMode(true); // indicate we are starting up
      desktop.setVisible(true);
      desktop.startServiceDiscovery();
      if (!aparser.contains("nousagestats"))
      {
        startUsageStats(desktop);
      }
      else
      {
        System.err.println("CMD [-nousagestats] executed successfully!");
      }

      if (!aparser.contains("noquestionnaire"))
      {
        String url = aparser.getValue("questionnaire");
        if (url != null)
        {
          // Start the desktop questionnaire prompter with the specified
          // questionnaire
          Cache.log.debug("Starting questionnaire url at " + url);
          desktop.checkForQuestionnaire(url);
          System.out.println("CMD questionnaire[-" + url
                  + "] executed successfully!");
        }
        else
        {
          if (Cache.getProperty("NOQUESTIONNAIRES") == null)
          {
            // Start the desktop questionnaire prompter with the specified
            // questionnaire
            // String defurl =
            // "http://anaplog.compbio.dundee.ac.uk/cgi-bin/questionnaire.pl";
            // //
            String defurl = "http://www.jalview.org/cgi-bin/questionnaire.pl";
            Cache.log.debug("Starting questionnaire with default url: "
                    + defurl);
            desktop.checkForQuestionnaire(defurl);
          }
        }
      }
      else
      {
        System.err.println("CMD [-noquestionnaire] executed successfully!");
      }

      if (!aparser.contains("nonews"))
      {
        desktop.checkForNews();
      }

      BioJsHTMLOutput.updateBioJS();
    }

    String file = null, protocol = null, format = null, data = null;
    FileLoader fileLoader = new FileLoader(!headless);
    Vector<String> getFeatures = null; // vector of das source nicknames to
                                       // fetch
    // features from
    // loading is done.
    String groovyscript = null; // script to execute after all loading is
    // completed one way or another
    // extract groovy argument and execute if necessary
    groovyscript = aparser.getValue("groovy", true);
    file = aparser.getValue("open", true);

    if (file == null && desktop == null)
    {
      System.out.println("No files to open!");
      System.exit(1);
    }
    String vamsasImport = aparser.getValue("vdoc");
    String vamsasSession = aparser.getValue("vsess");
    if (vamsasImport != null || vamsasSession != null)
    {
      if (desktop == null || headless)
      {
        System.out
                .println("Headless vamsas sessions not yet supported. Sorry.");
        System.exit(1);
      }
      // if we have a file, start a new session and import it.
      boolean inSession = false;
      if (vamsasImport != null)
      {
        try
        {
          String viprotocol = AppletFormatAdapter
                  .checkProtocol(vamsasImport);
          if (viprotocol == jalview.io.FormatAdapter.FILE)
          {
            inSession = desktop.vamsasImport(new File(vamsasImport));
          }
          else if (viprotocol == FormatAdapter.URL)
          {
            inSession = desktop.vamsasImport(new URL(vamsasImport));
          }

        } catch (Exception e)
        {
          System.err.println("Exeption when importing " + vamsasImport
                  + " as a vamsas document.");
          e.printStackTrace();
        }
        if (!inSession)
        {
          System.err.println("Failed to import " + vamsasImport
                  + " as a vamsas document.");
        }
        else
        {
          System.out.println("Imported Successfully into new session "
                  + desktop.getVamsasApplication().getCurrentSession());
        }
      }
      if (vamsasSession != null)
      {
        if (vamsasImport != null)
        {
          // close the newly imported session and import the Jalview specific
          // remnants into the new session later on.
          desktop.vamsasStop_actionPerformed(null);
        }
        // now join the new session
        try
        {
          if (desktop.joinVamsasSession(vamsasSession))
          {
            System.out.println("Successfully joined vamsas session "
                    + vamsasSession);
          }
          else
          {
            System.err.println("WARNING: Failed to join vamsas session "
                    + vamsasSession);
          }
        } catch (Exception e)
        {
          System.err.println("ERROR: Failed to join vamsas session "
                  + vamsasSession);
          e.printStackTrace();
        }
        if (vamsasImport != null)
        {
          // the Jalview specific remnants can now be imported into the new
          // session at the user's leisure.
          Cache.log
                  .info("Skipping Push for import of data into existing vamsas session."); // TODO:
          // enable
          // this
          // when
          // debugged
          // desktop.getVamsasApplication().push_update();
        }
      }
    }
    long progress = -1;
    // Finally, deal with the remaining input data.
    if (file != null)
    {
      if (!headless)
      {
        desktop.setProgressBar(MessageManager
                .getString("status.processing_commandline_args"),
                progress = System.currentTimeMillis());
      }
      System.out.println("CMD [-open " + file + "] executed successfully!");

      if (!file.startsWith("http://"))
      {
        if (!(new File(file)).exists())
        {
          System.out.println("Can't find " + file);
          if (headless)
          {
            System.exit(1);
          }
        }
      }

      protocol = AppletFormatAdapter.checkProtocol(file);

      format = new IdentifyFile().identify(file, protocol);

      AlignFrame af = fileLoader.LoadFileWaitTillLoaded(file, protocol,
              format);
      if (af == null)
      {
        System.out.println("error");
      }
      else
      {
        setCurrentAlignFrame(af);
        data = aparser.getValue("colour", true);
        if (data != null)
        {
          data.replaceAll("%20", " ");

          ColourSchemeI cs = ColourSchemeProperty.getColour(af
                  .getViewport().getAlignment(), data);

          if (cs == null)
          {
            UserColourScheme ucs = new UserColourScheme("white");
            ucs.parseAppletParameter(data);
            cs = ucs;
          }
          else
          {
            System.out.println("CMD [-color " + data
                    + "] executed successfully!");
          }
          af.changeColour(cs);
        }

        // Must maintain ability to use the groups flag
        data = aparser.getValue("groups", true);
        if (data != null)
        {
          af.parseFeaturesFile(data,
                  AppletFormatAdapter.checkProtocol(data));
          // System.out.println("Added " + data);
          System.out.println("CMD groups[-" + data
                  + "]  executed successfully!");
        }
        data = aparser.getValue("features", true);
        if (data != null)
        {
          af.parseFeaturesFile(data,
                  AppletFormatAdapter.checkProtocol(data));
          // System.out.println("Added " + data);
          System.out.println("CMD [-features " + data
                  + "]  executed successfully!");
        }

        data = aparser.getValue("annotations", true);
        if (data != null)
        {
          af.loadJalviewDataFile(data, null, null, null);
          // System.out.println("Added " + data);
          System.out.println("CMD [-annotations " + data
                  + "] executed successfully!");
        }
        // set or clear the sortbytree flag.
        if (aparser.contains("sortbytree"))
        {
          af.getViewport().setSortByTree(true);
          if (af.getViewport().getSortByTree())
          {
            System.out.println("CMD [-sortbytree] executed successfully!");
          }
        }
        if (aparser.contains("no-annotation"))
        {
          af.getViewport().setShowAnnotation(false);
          if (!af.getViewport().isShowAnnotation())
          {
            System.out.println("CMD no-annotation executed successfully!");
          }
        }
        if (aparser.contains("nosortbytree"))
        {
          af.getViewport().setSortByTree(false);
          if (!af.getViewport().getSortByTree())
          {
            System.out
                    .println("CMD [-nosortbytree] executed successfully!");
          }
        }
        data = aparser.getValue("tree", true);
        if (data != null)
        {
          jalview.io.NewickFile fin = null;
          try
          {
            System.out.println("CMD [-tree " + data
                    + "] executed successfully!");
            fin = new NewickFile(data,
                    AppletFormatAdapter.checkProtocol(data));
            if (fin != null)
            {
              af.getViewport().setCurrentTree(
                      af.ShowNewickTree(fin, data).getTree());
            }
          } catch (IOException ex)
          {
            System.err.println("Couldn't add tree " + data);
            ex.printStackTrace(System.err);
          }
        }
        // TODO - load PDB structure(s) to alignment JAL-629
        // (associate with identical sequence in alignment, or a specified
        // sequence)

        getFeatures = checkDasArguments(aparser);
        if (af != null && getFeatures != null)
        {
          FeatureFetcher ff = startFeatureFetching(getFeatures);
          if (ff != null)
          {
            while (!ff.allFinished() || af.operationInProgress())
            {
              // wait around until fetching is finished.
              try
              {
                Thread.sleep(100);
              } catch (Exception e)
              {

              }
            }
          }
          getFeatures = null; // have retrieved features - forget them now.
        }
        if (groovyscript != null)
        {
          // Execute the groovy script after we've done all the rendering stuff
          // and before any images or figures are generated.
          System.out.println("Executing script " + groovyscript);
          executeGroovyScript(groovyscript, af);
          System.out.println("CMD groovy[" + groovyscript
                  + "] executed successfully!");
          groovyscript = null;
        }
        String imageName = "unnamed.png";
        while (aparser.getSize() > 1)
        {
          format = aparser.nextValue();
          file = aparser.nextValue();

          if (format.equalsIgnoreCase("png"))
          {
            af.createPNG(new File(file));
            imageName = (new File(file)).getName();
            System.out.println("Creating PNG image: " + file);
            continue;
          }
          else if (format.equalsIgnoreCase("svg"))
          {
            File imageFile = new File(file);
            imageName = imageFile.getName();
            af.createSVG(imageFile);
            System.out.println("Creating SVG image: " + file);
            continue;
          }
          else if (format.equalsIgnoreCase("html"))
          {
            File imageFile = new File(file);
            imageName = imageFile.getName();
            HtmlSvgOutput htmlSVG = new HtmlSvgOutput(af.alignPanel);
            htmlSVG.exportHTML(file);

            System.out.println("Creating HTML image: " + file);
            continue;
          }
          else if (format.equalsIgnoreCase("biojsmsa"))
          {
            if (file == null)
            {
              System.err.println("The output html file must not be null");
              return;
            }
            try
            {
              BioJsHTMLOutput
                      .refreshVersionInfo(BioJsHTMLOutput.BJS_TEMPLATES_LOCAL_DIRECTORY);
            } catch (URISyntaxException e)
            {
              e.printStackTrace();
            }
            BioJsHTMLOutput bjs = new BioJsHTMLOutput(af.alignPanel);
            bjs.exportHTML(file);
            System.out.println("Creating BioJS MSA Viwer HTML file: "
                    + file);
            continue;
          }
          else if (format.equalsIgnoreCase("imgMap"))
          {
            af.createImageMap(new File(file), imageName);
            System.out.println("Creating image map: " + file);
            continue;
          }
          else if (format.equalsIgnoreCase("eps"))
          {
            File outputFile = new File(file);
            System.out.println("Creating EPS file: "
                    + outputFile.getAbsolutePath());
            af.createEPS(outputFile);
            continue;
          }

          if (af.saveAlignment(file, format))
          {
            System.out.println("Written alignment in " + format
                    + " format to " + file);
          }
          else
          {
            System.out.println("Error writing file " + file + " in "
                    + format + " format!!");
          }

        }

        while (aparser.getSize() > 0)
        {
          System.out.println("Unknown arg: " + aparser.nextValue());
        }
      }
    }
    AlignFrame startUpAlframe = null;
    // We'll only open the default file if the desktop is visible.
    // And the user
    // ////////////////////

    if (!headless && file == null && vamsasImport == null
            && jalview.bin.Cache.getDefault("SHOW_STARTUP_FILE", true))
    {
      file = jalview.bin.Cache.getDefault(
              "STARTUP_FILE",
              jalview.bin.Cache.getDefault("www.jalview.org",
                      "http://www.jalview.org")
                      + "/examples/exampleFile_2_7.jar");
      if (file.equals("http://www.jalview.org/examples/exampleFile_2_3.jar"))
      {
        // hardwire upgrade of the startup file
        file.replace("_2_3.jar", "_2_7.jar");
        // and remove the stale setting
        jalview.bin.Cache.removeProperty("STARTUP_FILE");
      }

      protocol = "File";

      if (file.indexOf("http:") > -1)
      {
        protocol = "URL";
      }

      if (file.endsWith(".jar"))
      {
        format = "Jalview";
      }
      else
      {
        format = new IdentifyFile().identify(file, protocol);
      }

      startUpAlframe = fileLoader.LoadFileWaitTillLoaded(file, protocol,
              format);
      getFeatures = checkDasArguments(aparser);
      // extract groovy arguments before anything else.
    }
    // If the user has specified features to be retrieved,
    // or a groovy script to be executed, do them if they
    // haven't been done already
    // fetch features for the default alignment
    if (getFeatures != null)
    {
      if (startUpAlframe != null)
      {
        startFeatureFetching(getFeatures);
      }
    }
    // Once all other stuff is done, execute any groovy scripts (in order)
    if (groovyscript != null)
    {
      if (Cache.groovyJarsPresent())
      {
        System.out.println("Executing script " + groovyscript);
        executeGroovyScript(groovyscript, startUpAlframe);
      }
      else
      {
        System.err
                .println("Sorry. Groovy Support is not available, so ignoring the provided groovy script "
                        + groovyscript);
      }
    }
    // and finally, turn off batch mode indicator - if the desktop still exists
    if (desktop != null)
    {
      if (progress != -1)
      {
        desktop.setProgressBar(null, progress);
      }
      desktop.setInBatchMode(false);
    }
  }

  private static void showUsage()
  {
    System.out
            .println("Usage: jalview -open [FILE] [OUTPUT_FORMAT] [OUTPUT_FILE]\n\n"
                    + "-nodisplay\tRun Jalview without User Interface.\n"
                    + "-props FILE\tUse the given Jalview properties file instead of users default.\n"
                    + "-colour COLOURSCHEME\tThe colourscheme to be applied to the alignment\n"
                    + "-annotations FILE\tAdd precalculated annotations to the alignment.\n"
                    + "-tree FILE\tLoad the given newick format tree file onto the alignment\n"
                    + "-features FILE\tUse the given file to mark features on the alignment.\n"
                    + "-fasta FILE\tCreate alignment file FILE in Fasta format.\n"
                    + "-clustal FILE\tCreate alignment file FILE in Clustal format.\n"
                    + "-pfam FILE\tCreate alignment file FILE in PFAM format.\n"
                    + "-msf FILE\tCreate alignment file FILE in MSF format.\n"
                    + "-pileup FILE\tCreate alignment file FILE in Pileup format\n"
                    + "-pir FILE\tCreate alignment file FILE in PIR format.\n"
                    + "-blc FILE\tCreate alignment file FILE in BLC format.\n"
                    + "-json FILE\tCreate alignment file FILE in JSON format.\n"
                    + "-jalview FILE\tCreate alignment file FILE in Jalview format.\n"
                    + "-png FILE\tCreate PNG image FILE from alignment.\n"
                    + "-svg FILE\tCreate SVG image FILE from alignment.\n"
                    + "-html FILE\tCreate HTML file from alignment.\n"
                    + "-biojsMSA FILE\tCreate BioJS MSA Viewer HTML file from alignment.\n"
                    + "-imgMap FILE\tCreate HTML file FILE with image map of PNG image.\n"
                    + "-eps FILE\tCreate EPS file FILE from alignment.\n"
                    + "-questionnaire URL\tQueries the given URL for information about any Jalview user questionnaires.\n"
                    + "-noquestionnaire\tTurn off questionnaire check.\n"
                    + "-nonews\tTurn off check for Jalview news.\n"
                    + "-nousagestats\tTurn off google analytics tracking for this session.\n"
                    + "-sortbytree OR -nosortbytree\tEnable or disable sorting of the given alignment by the given tree\n"
                    // +
                    // "-setprop PROPERTY=VALUE\tSet the given Jalview property, after all other properties files have been read\n\t (quote the 'PROPERTY=VALUE' pair to ensure spaces are passed in correctly)"
                    + "-jabaws URL\tSpecify URL for Jabaws services (e.g. for a local installation).\n"
                    + "-dasserver nickname=URL\tAdd and enable a das server with given nickname\n\t\t\t(alphanumeric or underscores only) for retrieval of features for all alignments.\n"
                    + "\t\t\tSources that also support the sequence command may be specified by prepending the URL with sequence:\n"
                    + "\t\t\t e.g. sequence:http://localdas.somewhere.org/das/source)\n"
                    + "-fetchfrom nickname\tQuery nickname for features for the alignments and display them.\n"
                    // +
                    // "-vdoc vamsas-document\tImport vamsas document into new session or join existing session with same URN\n"
                    // + "-vses vamsas-session\tJoin session with given URN\n"
                    + "-groovy FILE\tExecute groovy script in FILE, after all other arguments have been processed (if FILE is the text 'STDIN' then the file will be read from STDIN)\n"
                    + "\n~Read documentation in Application or visit http://www.jalview.org for description of Features and Annotations file~\n\n");
  }

  private static void startUsageStats(final Desktop desktop)
  {
    /**
     * start a User Config prompt asking if we can log usage statistics.
     */
    PromptUserConfig prompter = new PromptUserConfig(
            Desktop.desktop,
            "USAGESTATS",
            "Jalview Usage Statistics",
            "Do you want to help make Jalview better by enabling "
                    + "the collection of usage statistics with Google Analytics ?"
                    + "\n\n(you can enable or disable usage tracking in the preferences)",
            new Runnable()
            {
              @Override
              public void run()
              {
                Cache.log
                        .debug("Initialising googletracker for usage stats.");
                Cache.initGoogleTracker();
                Cache.log.debug("Tracking enabled.");
              }
            }, new Runnable()
            {
              @Override
              public void run()
              {
                Cache.log.debug("Not enabling Google Tracking.");
              }
            }, null, true);
    desktop.addDialogThread(prompter);
  }

  /**
   * Locate the given string as a file and pass it to the groovy interpreter.
   * 
   * @param groovyscript
   *          the script to execute
   * @param jalviewContext
   *          the Jalview Desktop object passed in to the groovy binding as the
   *          'Jalview' object.
   */
  private void executeGroovyScript(String groovyscript, AlignFrame af)
  {
    /**
     * for scripts contained in files
     */
    File tfile = null;
    /**
     * script's URI
     */
    URL sfile = null;
    if (groovyscript.trim().equals("STDIN"))
    {
      // read from stdin into a tempfile and execute it
      try
      {
        tfile = File.createTempFile("jalview", "groovy");
        PrintWriter outfile = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(tfile)));
        BufferedReader br = new BufferedReader(new InputStreamReader(
                System.in));
        String line = null;
        while ((line = br.readLine()) != null)
        {
          outfile.write(line + "\n");
        }
        br.close();
        outfile.flush();
        outfile.close();

      } catch (Exception ex)
      {
        System.err.println("Failed to read from STDIN into tempfile "
                + ((tfile == null) ? "(tempfile wasn't created)" : tfile
                        .toString()));
        ex.printStackTrace();
        return;
      }
      try
      {
        sfile = tfile.toURI().toURL();
      } catch (Exception x)
      {
        System.err
                .println("Unexpected Malformed URL Exception for temporary file created from STDIN: "
                        + tfile.toURI());
        x.printStackTrace();
        return;
      }
    }
    else
    {
      try
      {
        sfile = new URI(groovyscript).toURL();
      } catch (Exception x)
      {
        tfile = new File(groovyscript);
        if (!tfile.exists())
        {
          System.err.println("File '" + groovyscript + "' does not exist.");
          return;
        }
        if (!tfile.canRead())
        {
          System.err.println("File '" + groovyscript + "' cannot be read.");
          return;
        }
        if (tfile.length() < 1)
        {
          System.err.println("File '" + groovyscript + "' is empty.");
          return;
        }
        try
        {
          sfile = tfile.getAbsoluteFile().toURI().toURL();
        } catch (Exception ex)
        {
          System.err.println("Failed to create a file URL for "
                  + tfile.getAbsoluteFile());
          return;
        }
      }
    }
    try
    {
      Map<String, Object> vbinding = new HashMap<String, Object>();
      vbinding.put("Jalview", this);
      if (af != null)
      {
        vbinding.put("currentAlFrame", af);
      }
      Binding gbinding = new Binding(vbinding);
      GroovyScriptEngine gse = new GroovyScriptEngine(new URL[] { sfile });
      gse.run(sfile.toString(), gbinding);
      if ("STDIN".equals(groovyscript))
      {
        // delete temp file that we made -
        // only if it was successfully executed
        tfile.delete();
      }
    } catch (Exception e)
    {
      System.err.println("Exception Whilst trying to execute file " + sfile
              + " as a groovy script.");
      e.printStackTrace(System.err);

    }
  }

  /**
   * Check commandline for any das server definitions or any fetchfrom switches
   * 
   * @return vector of DAS source nicknames to retrieve from
   */
  private static Vector<String> checkDasArguments(ArgsParser aparser)
  {
    Vector<String> source = null;
    String data;
    String locsources = Cache.getProperty(Cache.DAS_LOCAL_SOURCE);
    while ((data = aparser.getValue("dasserver", true)) != null)
    {
      String nickname = null;
      String url = null;
      int pos = data.indexOf('=');
      // determine capabilities
      if (pos > 0)
      {
        nickname = data.substring(0, pos);
      }
      url = data.substring(pos + 1);
      if (url != null
              && (url.startsWith("http:") || url
                      .startsWith("sequence:http:")))
      {
        if (nickname == null)
        {
          nickname = url;
        }
        if (locsources == null)
        {
          locsources = "";
        }
        else
        {
          locsources += "\t";
        }
        locsources = locsources + nickname + "|" + url;
        System.err
                .println("NOTE! dasserver parameter not yet really supported (got args of "
                        + nickname + "|" + url);
        if (source == null)
        {
          source = new Vector<String>();
        }
        source.addElement(nickname);
      }
      System.out.println("CMD [-dasserver " + data
              + "] executed successfully!");
    } // loop until no more server entries are found.
    if (locsources != null && locsources.indexOf('|') > -1)
    {
      Cache.log.debug("Setting local source list in properties file to:\n"
              + locsources);
      Cache.setProperty(Cache.DAS_LOCAL_SOURCE, locsources);
    }
    while ((data = aparser.getValue("fetchfrom", true)) != null)
    {
      System.out.println("adding source '" + data + "'");
      if (source == null)
      {
        source = new Vector<String>();
      }
      source.addElement(data);
    }
    return source;
  }

  /**
   * start a feature fetcher for every alignment frame
   * 
   * @param dasSources
   */
  private FeatureFetcher startFeatureFetching(
          final Vector<String> dasSources)
  {
    FeatureFetcher ff = new FeatureFetcher();
    AlignFrame afs[] = Desktop.getAlignFrames();
    if (afs == null || afs.length == 0)
    {
      return null;
    }
    for (int i = 0; i < afs.length; i++)
    {
      ff.addFetcher(afs[i], dasSources);
    }
    return ff;
  }

  public static boolean isHeadlessMode()
  {
    String isheadless = System.getProperty("java.awt.headless");
    if (isheadless != null && isheadless.equalsIgnoreCase("true"))
    {
      return true;
    }
    return false;
  }

  public AlignFrame[] getAlignFrames()
  {
    return desktop == null ? new AlignFrame[] { getCurrentAlignFrame() }
            : Desktop.getAlignFrames();

  }

  /**
   * Quit method delegates to Desktop.quit - unless running in headless mode
   * when it just ends the JVM
   */
  public void quit()
  {
    if (desktop != null)
    {
      desktop.quit();
    }
    else
    {
      System.exit(0);
    }
  }

  public static AlignFrame getCurrentAlignFrame()
  {
    return Jalview.currentAlignFrame;
  }

  public static void setCurrentAlignFrame(AlignFrame currentAlignFrame)
  {
    Jalview.currentAlignFrame = currentAlignFrame;
  }
}
