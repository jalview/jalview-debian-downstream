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
package jalview.gui;

import jalview.api.AlignViewportI;
import jalview.api.AlignmentViewPanel;
import jalview.bin.Cache;
import jalview.bin.Jalview;
import jalview.io.BackupFiles;
import jalview.io.DataSourceType;
import jalview.io.FileFormat;
import jalview.io.FileFormatException;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FileLoader;
import jalview.io.FormatAdapter;
import jalview.io.IdentifyFile;
import jalview.io.JalviewFileChooser;
import jalview.io.JalviewFileView;
import jalview.jbgui.GSplitFrame;
import jalview.jbgui.GStructureViewer;
import jalview.project.Jalview2XML;
import jalview.structure.StructureSelectionManager;
import jalview.urls.IdOrgSettings;
import jalview.util.ImageMaker;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.UrlConstants;
import jalview.viewmodel.AlignmentViewport;
import jalview.ws.params.ParamManager;
import jalview.ws.utils.UrlDownloadClient;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultDesktopManager;
import javax.swing.DesktopManager;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.stackoverflowusers.file.WindowsShortcut;

/**
 * Jalview Desktop
 * 
 * 
 * @author $author$
 * @version $Revision: 1.155 $
 */
public class Desktop extends jalview.jbgui.GDesktop
        implements DropTargetListener, ClipboardOwner, IProgressIndicator,
        jalview.api.StructureSelectionManagerProvider
{
  private static int DEFAULT_MIN_WIDTH = 300;

  private static int DEFAULT_MIN_HEIGHT = 250;

  private static int ALIGN_FRAME_DEFAULT_MIN_WIDTH = 600;

  private static int ALIGN_FRAME_DEFAULT_MIN_HEIGHT = 70;

  private static final String EXPERIMENTAL_FEATURES = "EXPERIMENTAL_FEATURES";

  protected static final String CONFIRM_KEYBOARD_QUIT = "CONFIRM_KEYBOARD_QUIT";

  public static HashMap<String, FileWriter> savingFiles = new HashMap<String, FileWriter>();

  private JalviewChangeSupport changeSupport = new JalviewChangeSupport();

  /**
   * news reader - null if it was never started.
   */
  private BlogReader jvnews = null;

  private File projectFile;

  /**
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#addJalviewPropertyChangeListener(java.beans.PropertyChangeListener)
   */
  public void addJalviewPropertyChangeListener(
          PropertyChangeListener listener)
  {
    changeSupport.addJalviewPropertyChangeListener(listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#addJalviewPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void addJalviewPropertyChangeListener(String propertyName,
          PropertyChangeListener listener)
  {
    changeSupport.addJalviewPropertyChangeListener(propertyName, listener);
  }

  /**
   * @param propertyName
   * @param listener
   * @see jalview.gui.JalviewChangeSupport#removeJalviewPropertyChangeListener(java.lang.String,
   *      java.beans.PropertyChangeListener)
   */
  public void removeJalviewPropertyChangeListener(String propertyName,
          PropertyChangeListener listener)
  {
    changeSupport.removeJalviewPropertyChangeListener(propertyName,
            listener);
  }

  /** Singleton Desktop instance */
  public static Desktop instance;

  public static MyDesktopPane desktop;

  static int openFrameCount = 0;

  static final int xOffset = 30;

  static final int yOffset = 30;

  public static jalview.ws.jws1.Discoverer discoverer;

  public static Object[] jalviewClipboard;

  public static boolean internalCopy = false;

  static int fileLoadingCount = 0;

  class MyDesktopManager implements DesktopManager
  {

    private DesktopManager delegate;

    public MyDesktopManager(DesktopManager delegate)
    {
      this.delegate = delegate;
    }

    @Override
    public void activateFrame(JInternalFrame f)
    {
      try
      {
        delegate.activateFrame(f);
      } catch (NullPointerException npe)
      {
        Point p = getMousePosition();
        instance.showPasteMenu(p.x, p.y);
      }
    }

    @Override
    public void beginDraggingFrame(JComponent f)
    {
      delegate.beginDraggingFrame(f);
    }

    @Override
    public void beginResizingFrame(JComponent f, int direction)
    {
      delegate.beginResizingFrame(f, direction);
    }

    @Override
    public void closeFrame(JInternalFrame f)
    {
      delegate.closeFrame(f);
    }

    @Override
    public void deactivateFrame(JInternalFrame f)
    {
      delegate.deactivateFrame(f);
    }

    @Override
    public void deiconifyFrame(JInternalFrame f)
    {
      delegate.deiconifyFrame(f);
    }

    @Override
    public void dragFrame(JComponent f, int newX, int newY)
    {
      if (newY < 0)
      {
        newY = 0;
      }
      delegate.dragFrame(f, newX, newY);
    }

    @Override
    public void endDraggingFrame(JComponent f)
    {
      delegate.endDraggingFrame(f);
      desktop.repaint();
    }

    @Override
    public void endResizingFrame(JComponent f)
    {
      delegate.endResizingFrame(f);
      desktop.repaint();
    }

    @Override
    public void iconifyFrame(JInternalFrame f)
    {
      delegate.iconifyFrame(f);
    }

    @Override
    public void maximizeFrame(JInternalFrame f)
    {
      delegate.maximizeFrame(f);
    }

    @Override
    public void minimizeFrame(JInternalFrame f)
    {
      delegate.minimizeFrame(f);
    }

    @Override
    public void openFrame(JInternalFrame f)
    {
      delegate.openFrame(f);
    }

    @Override
    public void resizeFrame(JComponent f, int newX, int newY, int newWidth,
            int newHeight)
    {
      if (newY < 0)
      {
        newY = 0;
      }
      delegate.resizeFrame(f, newX, newY, newWidth, newHeight);
    }

    @Override
    public void setBoundsForFrame(JComponent f, int newX, int newY,
            int newWidth, int newHeight)
    {
      delegate.setBoundsForFrame(f, newX, newY, newWidth, newHeight);
    }

    // All other methods, simply delegate

  }

  /**
   * Creates a new Desktop object.
   */
  public Desktop()
  {
    /**
     * A note to implementors. It is ESSENTIAL that any activities that might
     * block are spawned off as threads rather than waited for during this
     * constructor.
     */
    instance = this;

    doConfigureStructurePrefs();
    setTitle("Jalview " + jalview.bin.Cache.getProperty("VERSION"));
    /*
    if (!Platform.isAMac())
    {
      // this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    else
    {
     this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    }
    */

    try
    {
      APQHandlers.setAPQHandlers(this);
    } catch (Exception e)
    {
      System.out.println("Cannot set APQHandlers");
      // e.printStackTrace();
    } catch (Throwable t)
    {
      System.out.println("Cannot set APQHandlers");
      // t.printStackTrace();
    }


    addWindowListener(new WindowAdapter()
    {

      @Override
      public void windowClosing(WindowEvent ev)
      {
        quit();
      }
    });

    boolean selmemusage = jalview.bin.Cache.getDefault("SHOW_MEMUSAGE",
            false);

    boolean showjconsole = jalview.bin.Cache.getDefault("SHOW_JAVA_CONSOLE",
            false);
    desktop = new MyDesktopPane(selmemusage);
    showMemusage.setSelected(selmemusage);
    desktop.setBackground(Color.white);

    getContentPane().setLayout(new BorderLayout());
    // alternate config - have scrollbars - see notes in JAL-153
    // JScrollPane sp = new JScrollPane();
    // sp.getViewport().setView(desktop);
    // getContentPane().add(sp, BorderLayout.CENTER);
    getContentPane().add(desktop, BorderLayout.CENTER);
    desktop.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

    // This line prevents Windows Look&Feel resizing all new windows to maximum
    // if previous window was maximised
    desktop.setDesktopManager(new MyDesktopManager(
            (Platform.isWindows() ? new DefaultDesktopManager()
                    : Platform.isAMac()
                            ? new AquaInternalFrameManager(
                                    desktop.getDesktopManager())
                            : desktop.getDesktopManager())));

    Rectangle dims = getLastKnownDimensions("");
    if (dims != null)
    {
      setBounds(dims);
    }
    else
    {
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setBounds((screenSize.width - 900) / 2, (screenSize.height - 650) / 2,
              900, 650);
    }
    jconsole = new Console(this, showjconsole);
    // add essential build information
    jconsole.setHeader(jalview.bin.Cache.getVersionDetailsForConsole());

    showConsole(showjconsole);

    showNews.setVisible(false);

    experimentalFeatures.setSelected(showExperimental());

    getIdentifiersOrgData();

    checkURLLinks();

    this.addWindowListener(new WindowAdapter()
    {
      @Override
      public void windowClosing(WindowEvent evt)
      {
        quit();
      }
    });

    MouseAdapter ma;
    this.addMouseListener(ma = new MouseAdapter()
    {
      @Override
      public void mousePressed(MouseEvent evt)
      {
        if (evt.isPopupTrigger()) // Mac
        {
          showPasteMenu(evt.getX(), evt.getY());
        }
      }

      @Override
      public void mouseReleased(MouseEvent evt)
      {
        if (evt.isPopupTrigger()) // Windows
        {
          showPasteMenu(evt.getX(), evt.getY());
        }
      }
    });
    desktop.addMouseListener(ma);

    this.setDropTarget(new java.awt.dnd.DropTarget(desktop, this));
    // Spawn a thread that shows the splashscreen
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        new SplashScreen();
      }
    });

    // Thread off a new instance of the file chooser - this reduces the time it
    // takes to open it later on.
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        Cache.log.debug("Filechooser init thread started.");
        String fileFormat = Cache.getProperty("DEFAULT_FILE_FORMAT");
        JalviewFileChooser.forRead(Cache.getProperty("LAST_DIRECTORY"),
                fileFormat);
        Cache.log.debug("Filechooser init thread finished.");
      }
    }).start();
    // Add the service change listener
    changeSupport.addJalviewPropertyChangeListener("services",
            new PropertyChangeListener()
            {

              @Override
              public void propertyChange(PropertyChangeEvent evt)
              {
                Cache.log.debug("Firing service changed event for "
                        + evt.getNewValue());
                JalviewServicesChanged(evt);
              }

            });
  }

  /**
   * Answers true if user preferences to enable experimental features is True
   * (on), else false
   * 
   * @return
   */
  public boolean showExperimental()
  {
    String experimental = Cache.getDefault(EXPERIMENTAL_FEATURES,
            Boolean.FALSE.toString());
    return Boolean.valueOf(experimental).booleanValue();
  }

  public void doConfigureStructurePrefs()
  {
    // configure services
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(this);
    if (jalview.bin.Cache.getDefault(Preferences.ADD_SS_ANN, true))
    {
      ssm.setAddTempFacAnnot(jalview.bin.Cache
              .getDefault(Preferences.ADD_TEMPFACT_ANN, true));
      ssm.setProcessSecondaryStructure(jalview.bin.Cache
              .getDefault(Preferences.STRUCT_FROM_PDB, true));
      ssm.setSecStructServices(
              jalview.bin.Cache.getDefault(Preferences.USE_RNAVIEW, true));
    }
    else
    {
      ssm.setAddTempFacAnnot(false);
      ssm.setProcessSecondaryStructure(false);
      ssm.setSecStructServices(false);
    }
  }

  public void checkForNews()
  {
    final Desktop me = this;
    // Thread off the news reader, in case there are connection problems.
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        Cache.log.debug("Starting news thread.");

        jvnews = new BlogReader(me);
        showNews.setVisible(true);
        Cache.log.debug("Completed news thread.");
      }
    }).start();
  }

  public void getIdentifiersOrgData()
  {
    // Thread off the identifiers fetcher
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        Cache.log.debug("Downloading data from identifiers.org");
        UrlDownloadClient client = new UrlDownloadClient();
        try
        {
          client.download(IdOrgSettings.getUrl(),
                  IdOrgSettings.getDownloadLocation());
        } catch (IOException e)
        {
          Cache.log.debug("Exception downloading identifiers.org data"
                  + e.getMessage());
        }
      }
    }).start();
    ;
  }

  @Override
  protected void showNews_actionPerformed(ActionEvent e)
  {
    showNews(showNews.isSelected());
  }

  void showNews(boolean visible)
  {
    {
      Cache.log.debug((visible ? "Showing" : "Hiding") + " news.");
      showNews.setSelected(visible);
      if (visible && !jvnews.isVisible())
      {
        new Thread(new Runnable()
        {
          @Override
          public void run()
          {
            long now = System.currentTimeMillis();
            Desktop.instance.setProgressBar(
                    MessageManager.getString("status.refreshing_news"),
                    now);
            jvnews.refreshNews();
            Desktop.instance.setProgressBar(null, now);
            jvnews.showNews();
          }
        }).start();
      }
    }
  }

  /**
   * recover the last known dimensions for a jalview window
   * 
   * @param windowName
   *          - empty string is desktop, all other windows have unique prefix
   * @return null or last known dimensions scaled to current geometry (if last
   *         window geom was known)
   */
  Rectangle getLastKnownDimensions(String windowName)
  {
    // TODO: lock aspect ratio for scaling desktop Bug #0058199
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    String x = jalview.bin.Cache.getProperty(windowName + "SCREEN_X");
    String y = jalview.bin.Cache.getProperty(windowName + "SCREEN_Y");
    String width = jalview.bin.Cache
            .getProperty(windowName + "SCREEN_WIDTH");
    String height = jalview.bin.Cache
            .getProperty(windowName + "SCREEN_HEIGHT");
    if ((x != null) && (y != null) && (width != null) && (height != null))
    {
      int ix = Integer.parseInt(x), iy = Integer.parseInt(y),
              iw = Integer.parseInt(width), ih = Integer.parseInt(height);
      if (jalview.bin.Cache.getProperty("SCREENGEOMETRY_WIDTH") != null)
      {
        // attempt #1 - try to cope with change in screen geometry - this
        // version doesn't preserve original jv aspect ratio.
        // take ratio of current screen size vs original screen size.
        double sw = ((1f * screenSize.width) / (1f * Integer.parseInt(
                jalview.bin.Cache.getProperty("SCREENGEOMETRY_WIDTH"))));
        double sh = ((1f * screenSize.height) / (1f * Integer.parseInt(
                jalview.bin.Cache.getProperty("SCREENGEOMETRY_HEIGHT"))));
        // rescale the bounds depending upon the current screen geometry.
        ix = (int) (ix * sw);
        iw = (int) (iw * sw);
        iy = (int) (iy * sh);
        ih = (int) (ih * sh);
        while (ix >= screenSize.width)
        {
          jalview.bin.Cache.log.debug(
                  "Window geometry location recall error: shifting horizontal to within screenbounds.");
          ix -= screenSize.width;
        }
        while (iy >= screenSize.height)
        {
          jalview.bin.Cache.log.debug(
                  "Window geometry location recall error: shifting vertical to within screenbounds.");
          iy -= screenSize.height;
        }
        jalview.bin.Cache.log.debug(
                "Got last known dimensions for " + windowName + ": x:" + ix
                        + " y:" + iy + " width:" + iw + " height:" + ih);
      }
      // return dimensions for new instance
      return new Rectangle(ix, iy, iw, ih);
    }
    return null;
  }

  void showPasteMenu(int x, int y)
  {
    JPopupMenu popup = new JPopupMenu();
    JMenuItem item = new JMenuItem(
            MessageManager.getString("label.paste_new_window"));
    item.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent evt)
      {
        paste();
      }
    });

    popup.add(item);
    popup.show(this, x, y);
  }

  public void paste()
  {
    try
    {
      Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
      Transferable contents = c.getContents(this);

      if (contents != null)
      {
        String file = (String) contents
                .getTransferData(DataFlavor.stringFlavor);

        FileFormatI format = new IdentifyFile().identify(file,
                DataSourceType.PASTE);

        new FileLoader().LoadFile(file, DataSourceType.PASTE, format);

      }
    } catch (Exception ex)
    {
      System.out.println(
              "Unable to paste alignment from system clipboard:\n" + ex);
    }
  }

  /**
   * Adds and opens the given frame to the desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param w
   *          width
   * @param h
   *          height
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, int w, int h)
  {
    addInternalFrame(frame, title, true, w, h, true, false);
  }

  /**
   * Add an internal frame to the Jalview desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param makeVisible
   *          When true, display frame immediately, otherwise, caller must call
   *          setVisible themselves.
   * @param w
   *          width
   * @param h
   *          height
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, boolean makeVisible,
          int w, int h)
  {
    addInternalFrame(frame, title, makeVisible, w, h, true, false);
  }

  /**
   * Add an internal frame to the Jalview desktop and make it visible
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param w
   *          width
   * @param h
   *          height
   * @param resizable
   *          Allow resize
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, int w, int h,
          boolean resizable)
  {
    addInternalFrame(frame, title, true, w, h, resizable, false);
  }

  /**
   * Add an internal frame to the Jalview desktop
   * 
   * @param frame
   *          Frame to show
   * @param title
   *          Visible Title
   * @param makeVisible
   *          When true, display frame immediately, otherwise, caller must call
   *          setVisible themselves.
   * @param w
   *          width
   * @param h
   *          height
   * @param resizable
   *          Allow resize
   * @param ignoreMinSize
   *          Do not set the default minimum size for frame
   */
  public static synchronized void addInternalFrame(
          final JInternalFrame frame, String title, boolean makeVisible,
          int w, int h, boolean resizable, boolean ignoreMinSize)
  {

    // TODO: allow callers to determine X and Y position of frame (eg. via
    // bounds object).
    // TODO: consider fixing method to update entries in the window submenu with
    // the current window title

    frame.setTitle(title);
    if (frame.getWidth() < 1 || frame.getHeight() < 1)
    {
      frame.setSize(w, h);
    }
    // THIS IS A PUBLIC STATIC METHOD, SO IT MAY BE CALLED EVEN IN
    // A HEADLESS STATE WHEN NO DESKTOP EXISTS. MUST RETURN
    // IF JALVIEW IS RUNNING HEADLESS
    // ///////////////////////////////////////////////
    if (instance == null || (System.getProperty("java.awt.headless") != null
            && System.getProperty("java.awt.headless").equals("true")))
    {
      return;
    }

    openFrameCount++;

    if (!ignoreMinSize)
    {
      frame.setMinimumSize(
              new Dimension(DEFAULT_MIN_WIDTH, DEFAULT_MIN_HEIGHT));

      // Set default dimension for Alignment Frame window.
      // The Alignment Frame window could be added from a number of places,
      // hence,
      // I did this here in order not to miss out on any Alignment frame.
      if (frame instanceof AlignFrame)
      {
        frame.setMinimumSize(new Dimension(ALIGN_FRAME_DEFAULT_MIN_WIDTH,
                ALIGN_FRAME_DEFAULT_MIN_HEIGHT));
      }
    }

    frame.setVisible(makeVisible);
    frame.setClosable(true);
    frame.setResizable(resizable);
    frame.setMaximizable(resizable);
    frame.setIconifiable(resizable);
    frame.setOpaque(false);

    if (frame.getX() < 1 && frame.getY() < 1)
    {
      frame.setLocation(xOffset * openFrameCount,
              yOffset * ((openFrameCount - 1) % 10) + yOffset);
    }

    /*
     * add an entry for the new frame in the Window menu 
     * (and remove it when the frame is closed)
     */
    final JMenuItem menuItem = new JMenuItem(title);
    frame.addInternalFrameListener(new InternalFrameAdapter()
    {
      @Override
      public void internalFrameActivated(InternalFrameEvent evt)
      {
        JInternalFrame itf = desktop.getSelectedFrame();
        if (itf != null)
        {
          if (itf instanceof AlignFrame)
          {
            Jalview.setCurrentAlignFrame((AlignFrame) itf);
          }
          itf.requestFocus();
        }
      }

      @Override
      public void internalFrameClosed(InternalFrameEvent evt)
      {
        PaintRefresher.RemoveComponent(frame);

        /*
         * defensive check to prevent frames being
         * added half off the window
         */
        if (openFrameCount > 0)
        {
          openFrameCount--;
        }

        /*
         * ensure no reference to alignFrame retained by menu item listener
         */
        if (menuItem.getActionListeners().length > 0)
        {
          menuItem.removeActionListener(menuItem.getActionListeners()[0]);
        }
        windowMenu.remove(menuItem);
      };
    });

    menuItem.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        try
        {
          frame.setSelected(true);
          frame.setIcon(false);
        } catch (java.beans.PropertyVetoException ex)
        {

        }
      }
    });

    setKeyBindings(frame);

    desktop.add(frame);

    windowMenu.add(menuItem);

    frame.toFront();
    try
    {
      frame.setSelected(true);
      frame.requestFocus();
    } catch (java.beans.PropertyVetoException ve)
    {
    } catch (java.lang.ClassCastException cex)
    {
      Cache.log.warn(
              "Squashed a possible GUI implementation error. If you can recreate this, please look at https://issues.jalview.org/browse/JAL-869",
              cex);
    }
  }

  /**
   * Add key bindings to a JInternalFrame so that Ctrl-W and Cmd-W will close
   * the window
   * 
   * @param frame
   */
  private static void setKeyBindings(JInternalFrame frame)
  {
    @SuppressWarnings("serial")
    final Action closeAction = new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        frame.dispose();
      }
    };

    /*
     * set up key bindings for Ctrl-W and Cmd-W, with the same (Close) action
     */
    KeyStroke ctrlWKey = KeyStroke.getKeyStroke(KeyEvent.VK_W,
            InputEvent.CTRL_DOWN_MASK);
    KeyStroke cmdWKey = KeyStroke.getKeyStroke(KeyEvent.VK_W,
            jalview.util.ShortcutKeyMaskExWrapper.getMenuShortcutKeyMaskEx());

    InputMap inputMap = frame
            .getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    String ctrlW = ctrlWKey.toString();
    inputMap.put(ctrlWKey, ctrlW);
    inputMap.put(cmdWKey, ctrlW);

    ActionMap actionMap = frame.getActionMap();
    actionMap.put(ctrlW, closeAction);
  }

  @Override
  public void lostOwnership(Clipboard clipboard, Transferable contents)
  {
    if (!internalCopy)
    {
      Desktop.jalviewClipboard = null;
    }

    internalCopy = false;
  }

  @Override
  public void dragEnter(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dragExit(DropTargetEvent evt)
  {
  }

  @Override
  public void dragOver(DropTargetDragEvent evt)
  {
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent evt)
  {
  }

  /**
   * DOCUMENT ME!
   * 
   * @param evt
   *          DOCUMENT ME!
   */
  @Override
  public void drop(DropTargetDropEvent evt)
  {
    boolean success = true;
    // JAL-1552 - acceptDrop required before getTransferable call for
    // Java's Transferable for native dnd
    evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
    Transferable t = evt.getTransferable();
    List<String> files = new ArrayList<>();
    List<DataSourceType> protocols = new ArrayList<>();

    try
    {
      Desktop.transferFromDropTarget(files, protocols, evt, t);
    } catch (Exception e)
    {
      e.printStackTrace();
      success = false;
    }

    if (files != null)
    {
      try
      {
        for (int i = 0; i < files.size(); i++)
        {
          String file = files.get(i).toString();
          DataSourceType protocol = (protocols == null)
                  ? DataSourceType.FILE
                  : protocols.get(i);
          FileFormatI format = null;

          if (file.endsWith(".jar"))
          {
            format = FileFormat.Jalview;

          }
          else
          {
            format = new IdentifyFile().identify(file, protocol);
          }

          new FileLoader().LoadFile(file, protocol, format);

        }
      } catch (Exception ex)
      {
        success = false;
      }
    }
    evt.dropComplete(success); // need this to ensure input focus is properly
                               // transfered to any new windows created
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void inputLocalFileMenuItem_actionPerformed(AlignViewport viewport)
  {
    String fileFormat = Cache.getProperty("DEFAULT_FILE_FORMAT");
    JalviewFileChooser chooser = JalviewFileChooser
            .forRead(Cache.getProperty("LAST_DIRECTORY"), fileFormat, BackupFiles.getEnabled());

    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(
            MessageManager.getString("label.open_local_file"));
    chooser.setToolTipText(MessageManager.getString("action.open"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      String choice = chooser.getSelectedFile().getPath();
      Cache.setProperty("LAST_DIRECTORY",
              chooser.getSelectedFile().getParent());

      FileFormatI format = chooser.getSelectedFormat();

      /*
       * Call IdentifyFile to verify the file contains what its extension implies.
       * Skip this step for dynamically added file formats, because
       * IdentifyFile does not know how to recognise them.
       */
      if (FileFormats.getInstance().isIdentifiable(format))
      {
        try
        {
          format = new IdentifyFile().identify(choice, DataSourceType.FILE);
        } catch (FileFormatException e)
        {
          // format = null; //??
        }
      }

      if (viewport != null)
      {
        new FileLoader().LoadFile(viewport, choice, DataSourceType.FILE,
                format);
      }
      else
      {
        new FileLoader().LoadFile(choice, DataSourceType.FILE, format);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void inputURLMenuItem_actionPerformed(AlignViewport viewport)
  {
    // This construct allows us to have a wider textfield
    // for viewing
    JLabel label = new JLabel(
            MessageManager.getString("label.input_file_url"));
    final JComboBox history = new JComboBox();

    JPanel panel = new JPanel(new GridLayout(2, 1));
    panel.add(label);
    panel.add(history);
    history.setPreferredSize(new Dimension(400, 20));
    history.setEditable(true);
    history.addItem("https://www.");

    String historyItems = jalview.bin.Cache.getProperty("RECENT_URL");

    StringTokenizer st;

    if (historyItems != null)
    {
      st = new StringTokenizer(historyItems, "\t");

      while (st.hasMoreTokens())
      {
        history.addItem(st.nextElement());
      }
    }

    int reply = JvOptionPane.showInternalConfirmDialog(desktop, panel,
            MessageManager.getString("label.input_alignment_from_url"),
            JvOptionPane.OK_CANCEL_OPTION);

    if (reply != JvOptionPane.OK_OPTION)
    {
      return;
    }

    String url = history.getSelectedItem().toString();

    if (url.toLowerCase().endsWith(".jar"))
    {
      if (viewport != null)
      {
        new FileLoader().LoadFile(viewport, url, DataSourceType.URL,
                FileFormat.Jalview);
      }
      else
      {
        new FileLoader().LoadFile(url, DataSourceType.URL,
                FileFormat.Jalview);
      }
    }
    else
    {
      FileFormatI format = null;
      try
      {
        format = new IdentifyFile().identify(url, DataSourceType.URL);
      } catch (FileFormatException e)
      {
        // TODO revise error handling, distinguish between
        // URL not found and response not valid
      }

      if (format == null)
      {
        JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                MessageManager.formatMessage("label.couldnt_locate",
                        new Object[]
                        { url }),
                MessageManager.getString("label.url_not_found"),
                JvOptionPane.WARNING_MESSAGE);

        return;
      }

      if (viewport != null)
      {
        new FileLoader().LoadFile(viewport, url, DataSourceType.URL,
                format);
      }
      else
      {
        new FileLoader().LoadFile(url, DataSourceType.URL, format);
      }
    }
  }

  /**
   * Opens the CutAndPaste window for the user to paste an alignment in to
   * 
   * @param viewPanel
   *          - if not null, the pasted alignment is added to the current
   *          alignment; if null, to a new alignment window
   */
  @Override
  public void inputTextboxMenuItem_actionPerformed(
          AlignmentViewPanel viewPanel)
  {
    CutAndPasteTransfer cap = new CutAndPasteTransfer();
    cap.setForInput(viewPanel);
    Desktop.addInternalFrame(cap,
            MessageManager.getString("label.cut_paste_alignmen_file"), true,
            600, 500);
  }

  /*
   * Exit the program
   */
  @Override
  public void quit()
  {
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    jalview.bin.Cache.setProperty("SCREENGEOMETRY_WIDTH",
            screen.width + "");
    jalview.bin.Cache.setProperty("SCREENGEOMETRY_HEIGHT",
            screen.height + "");
    storeLastKnownDimensions("", new Rectangle(getBounds().x, getBounds().y,
            getWidth(), getHeight()));

    if (jconsole != null)
    {
      storeLastKnownDimensions("JAVA_CONSOLE_", jconsole.getBounds());
      jconsole.stopConsole();
    }
    if (jvnews != null)
    {
      storeLastKnownDimensions("JALVIEW_RSS_WINDOW_", jvnews.getBounds());

    }
    if (dialogExecutor != null)
    {
      dialogExecutor.shutdownNow();
    }
    closeAll_actionPerformed(null);

    if (groovyConsole != null)
    {
      // suppress a possible repeat prompt to save script
      groovyConsole.setDirty(false);
      groovyConsole.exit();
    }
    System.exit(0);
  }

  private void storeLastKnownDimensions(String string, Rectangle jc)
  {
    jalview.bin.Cache.log.debug("Storing last known dimensions for "
            + string + ": x:" + jc.x + " y:" + jc.y + " width:" + jc.width
            + " height:" + jc.height);

    jalview.bin.Cache.setProperty(string + "SCREEN_X", jc.x + "");
    jalview.bin.Cache.setProperty(string + "SCREEN_Y", jc.y + "");
    jalview.bin.Cache.setProperty(string + "SCREEN_WIDTH", jc.width + "");
    jalview.bin.Cache.setProperty(string + "SCREEN_HEIGHT", jc.height + "");
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void aboutMenuItem_actionPerformed(ActionEvent e)
  {
    // StringBuffer message = getAboutMessage(false);
    // JvOptionPane.showInternalMessageDialog(Desktop.desktop,
    //
    // message.toString(), "About Jalview", JvOptionPane.INFORMATION_MESSAGE);
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        new SplashScreen(true);
      }
    }).start();
  }

  public StringBuffer getAboutMessage(boolean shortv)
  {
    StringBuffer message = new StringBuffer();
    message.append("<html>");
    if (shortv)
    {
      message.append("<h1><strong>Version: "
              + jalview.bin.Cache.getProperty("VERSION")
              + "</strong></h1>");
      message.append("<strong>Built: <em>"
              + jalview.bin.Cache.getDefault("BUILD_DATE", "unknown")
              + "</em> from " + jalview.bin.Cache.getBuildDetailsForSplash()
              + "</strong>");

    }
    else
    {

      message.append("<strong>Version "
              + jalview.bin.Cache.getProperty("VERSION")
              + "; last updated: "
              + jalview.bin.Cache.getDefault("BUILD_DATE", "unknown"));
    }

    if (jalview.bin.Cache.getDefault("LATEST_VERSION", "Checking")
            .equals("Checking"))
    {
      // JBP removed this message for 2.11: May be reinstated in future version
      // message.append("<br>...Checking latest version...</br>");
    }
    else if (!jalview.bin.Cache.getDefault("LATEST_VERSION", "Checking")
            .equals(jalview.bin.Cache.getProperty("VERSION")))
    {
      boolean red = false;
      if (jalview.bin.Cache.getProperty("VERSION").toLowerCase()
              .indexOf("automated build") == -1)
      {
        red = true;
        // Displayed when code version and jnlp version do not match and code
        // version is not a development build
        message.append("<div style=\"color: #FF0000;font-style: bold;\">");
      }

      message.append("<br>!! Version "
              + jalview.bin.Cache.getDefault("LATEST_VERSION",
                      "..Checking..")
              + " is available for download from "
              + jalview.bin.Cache.getDefault("www.jalview.org",
                      "https://www.jalview.org")
              + " !!");
      if (red)
      {
        message.append("</div>");
      }
    }
    message.append("<br>Authors:  " + jalview.bin.Cache.getDefault(
            "AUTHORFNAMES",
            "The Jalview Authors (See AUTHORS file for current list)")
            + "<br><br>Development managed by The Barton Group, University of Dundee, Scotland, UK.<br>"
            + "<br><br>For help, see the FAQ at <a href=\"https://www.jalview.org/faq\">www.jalview.org/faq</a> and/or join the jalview-discuss@jalview.org mailing list"
            + "<br><br>If  you use Jalview, please cite:"
            + "<br>Waterhouse, A.M., Procter, J.B., Martin, D.M.A, Clamp, M. and Barton, G. J. (2009)"
            + "<br>Jalview Version 2 - a multiple sequence alignment editor and analysis workbench"
            + "<br>Bioinformatics doi: 10.1093/bioinformatics/btp033"
            + "</html>");
    return message;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void documentationMenuItem_actionPerformed(ActionEvent e)
  {
    try
    {
      Help.showHelpWindow();
    } catch (Exception ex)
    {
    }
  }

  @Override
  public void closeAll_actionPerformed(ActionEvent e)
  {
    // TODO show a progress bar while closing?
    JInternalFrame[] frames = desktop.getAllFrames();
    for (int i = 0; i < frames.length; i++)
    {
      try
      {
        frames[i].setClosed(true);
      } catch (java.beans.PropertyVetoException ex)
      {
      }
    }
    Jalview.setCurrentAlignFrame(null);
    System.out.println("ALL CLOSED");

    /*
     * reset state of singleton objects as appropriate (clear down session state
     * when all windows are closed)
     */
    StructureSelectionManager ssm = StructureSelectionManager
            .getStructureSelectionManager(this);
    if (ssm != null)
    {
      ssm.resetAll();
    }
  }

  @Override
  public void raiseRelated_actionPerformed(ActionEvent e)
  {
    reorderAssociatedWindows(false, false);
  }

  @Override
  public void minimizeAssociated_actionPerformed(ActionEvent e)
  {
    reorderAssociatedWindows(true, false);
  }

  void closeAssociatedWindows()
  {
    reorderAssociatedWindows(false, true);
  }

  /*
   * (non-Javadoc)
   * 
   * @seejalview.jbgui.GDesktop#garbageCollect_actionPerformed(java.awt.event.
   * ActionEvent)
   */
  @Override
  protected void garbageCollect_actionPerformed(ActionEvent e)
  {
    // We simply collect the garbage
    jalview.bin.Cache.log.debug("Collecting garbage...");
    System.gc();
    jalview.bin.Cache.log.debug("Finished garbage collection.");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GDesktop#showMemusage_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  @Override
  protected void showMemusage_actionPerformed(ActionEvent e)
  {
    desktop.showMemoryUsage(showMemusage.isSelected());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * jalview.jbgui.GDesktop#showConsole_actionPerformed(java.awt.event.ActionEvent
   * )
   */
  @Override
  protected void showConsole_actionPerformed(ActionEvent e)
  {
    showConsole(showConsole.isSelected());
  }

  Console jconsole = null;

  /**
   * control whether the java console is visible or not
   * 
   * @param selected
   */
  void showConsole(boolean selected)
  {
    showConsole.setSelected(selected);
    // TODO: decide if we should update properties file
    Cache.setProperty("SHOW_JAVA_CONSOLE",
            Boolean.valueOf(selected).toString());
    jconsole.setVisible(selected);
  }

  void reorderAssociatedWindows(boolean minimize, boolean close)
  {
    JInternalFrame[] frames = desktop.getAllFrames();
    if (frames == null || frames.length < 1)
    {
      return;
    }

    AlignmentViewport source = null, target = null;
    if (frames[0] instanceof AlignFrame)
    {
      source = ((AlignFrame) frames[0]).getCurrentView();
    }
    else if (frames[0] instanceof TreePanel)
    {
      source = ((TreePanel) frames[0]).getViewPort();
    }
    else if (frames[0] instanceof PCAPanel)
    {
      source = ((PCAPanel) frames[0]).av;
    }
    else if (frames[0].getContentPane() instanceof PairwiseAlignPanel)
    {
      source = ((PairwiseAlignPanel) frames[0].getContentPane()).av;
    }

    if (source != null)
    {
      for (int i = 0; i < frames.length; i++)
      {
        target = null;
        if (frames[i] == null)
        {
          continue;
        }
        if (frames[i] instanceof AlignFrame)
        {
          target = ((AlignFrame) frames[i]).getCurrentView();
        }
        else if (frames[i] instanceof TreePanel)
        {
          target = ((TreePanel) frames[i]).getViewPort();
        }
        else if (frames[i] instanceof PCAPanel)
        {
          target = ((PCAPanel) frames[i]).av;
        }
        else if (frames[i].getContentPane() instanceof PairwiseAlignPanel)
        {
          target = ((PairwiseAlignPanel) frames[i].getContentPane()).av;
        }

        if (source == target)
        {
          try
          {
            if (close)
            {
              frames[i].setClosed(true);
            }
            else
            {
              frames[i].setIcon(minimize);
              if (!minimize)
              {
                frames[i].toFront();
              }
            }

          } catch (java.beans.PropertyVetoException ex)
          {
          }
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  protected void preferences_actionPerformed(ActionEvent e)
  {
    new Preferences();
  }

  /**
   * Shows a file chooser dialog and writes out the current session as a Jalview
   * project file
   */
  @Override
  public void saveState_actionPerformed()
  {
    saveState_actionPerformed(false);
  }

  public void saveState_actionPerformed(boolean saveAs)
  {
    java.io.File projectFile = getProjectFile();
    // autoSave indicates we already have a file and don't need to ask
    boolean autoSave = projectFile != null && !saveAs
            && BackupFiles.getEnabled();

    // System.out.println("autoSave="+autoSave+", projectFile='"+projectFile+"',
    // saveAs="+saveAs+", Backups
    // "+(BackupFiles.getEnabled()?"enabled":"disabled"));

    boolean approveSave = false;
    if (!autoSave)
    {
      JalviewFileChooser chooser = new JalviewFileChooser("jvp",
              "Jalview Project");

      chooser.setFileView(new JalviewFileView());
      chooser.setDialogTitle(MessageManager.getString("label.save_state"));

      int value = chooser.showSaveDialog(this);

      if (value == JalviewFileChooser.APPROVE_OPTION)
      {
        projectFile = chooser.getSelectedFile();
        setProjectFile(projectFile);
        approveSave = true;
      }
    }

    if (approveSave || autoSave)
    {
      final Desktop me = this;
      final java.io.File chosenFile = projectFile;
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          // TODO: refactor to Jalview desktop session controller action.
          setProgressBar(MessageManager.formatMessage(
                  "label.saving_jalview_project", new Object[]
                  { chosenFile.getName() }), chosenFile.hashCode());
          jalview.bin.Cache.setProperty("LAST_DIRECTORY",
                  chosenFile.getParent());
          // TODO catch and handle errors for savestate
          // TODO prevent user from messing with the Desktop whilst we're saving
          try
          {
            BackupFiles backupfiles = new BackupFiles(chosenFile);

            new Jalview2XML().saveState(backupfiles.getTempFile());

            backupfiles.setWriteSuccess(true);
            backupfiles.rollBackupsAndRenameTempFile();
          } catch (OutOfMemoryError oom)
          {
            new OOMWarning("Whilst saving current state to "
                    + chosenFile.getName(), oom);
          } catch (Exception ex)
          {
            Cache.log.error("Problems whilst trying to save to "
                    + chosenFile.getName(), ex);
            JvOptionPane.showMessageDialog(me,
                    MessageManager.formatMessage(
                            "label.error_whilst_saving_current_state_to",
                            new Object[]
                            { chosenFile.getName() }),
                    MessageManager.getString("label.couldnt_save_project"),
                    JvOptionPane.WARNING_MESSAGE);
          }
          setProgressBar(null, chosenFile.hashCode());
        }
      }).start();
    }
  }

  @Override
  public void saveAsState_actionPerformed(ActionEvent e)
  {
    saveState_actionPerformed(true);
  }

  private void setProjectFile(File choice)
  {
    this.projectFile = choice;
  }

  public File getProjectFile()
  {
    return this.projectFile;
  }

  /**
   * Shows a file chooser dialog and tries to read in the selected file as a
   * Jalview project
   */
  @Override
  public void loadState_actionPerformed()
  {
    final String[] suffix = new String[] { "jvp", "jar" };
    final String[] desc = new String[] { "Jalview Project",
        "Jalview Project (old)" };
    JalviewFileChooser chooser = new JalviewFileChooser(
            Cache.getProperty("LAST_DIRECTORY"), suffix, desc,
            "Jalview Project", true, BackupFiles.getEnabled()); // last two booleans: allFiles,
                                            // allowBackupFiles
    chooser.setFileView(new JalviewFileView());
    chooser.setDialogTitle(MessageManager.getString("label.restore_state"));

    int value = chooser.showOpenDialog(this);

    if (value == JalviewFileChooser.APPROVE_OPTION)
    {
      final File selectedFile = chooser.getSelectedFile();
      setProjectFile(selectedFile);
      final String choice = selectedFile.getAbsolutePath();
      Cache.setProperty("LAST_DIRECTORY", selectedFile.getParent());
      new Thread(new Runnable()
      {
        @Override
        public void run()
        {
          setProgressBar(MessageManager.formatMessage(
                  "label.loading_jalview_project", new Object[]
                  { choice }), choice.hashCode());
          try
          {
            new Jalview2XML().loadJalviewAlign(choice);
          } catch (OutOfMemoryError oom)
          {
            new OOMWarning("Whilst loading project from " + choice, oom);
          } catch (Exception ex)
          {
            Cache.log.error(
                    "Problems whilst loading project from " + choice, ex);
            JvOptionPane.showMessageDialog(Desktop.desktop,
                    MessageManager.formatMessage(
                            "label.error_whilst_loading_project_from",
                            new Object[]
                            { choice }),
                    MessageManager.getString("label.couldnt_load_project"),
                    JvOptionPane.WARNING_MESSAGE);
          }
          setProgressBar(null, choice.hashCode());
        }
      }).start();
    }
  }

  @Override
  public void inputSequence_actionPerformed(ActionEvent e)
  {
    new SequenceFetcher(this);
  }

  JPanel progressPanel;

  ArrayList<JPanel> fileLoadingPanels = new ArrayList<>();

  public void startLoading(final String fileName)
  {
    if (fileLoadingCount == 0)
    {
      fileLoadingPanels.add(addProgressPanel(MessageManager
              .formatMessage("label.loading_file", new Object[]
              { fileName })));
    }
    fileLoadingCount++;
  }

  private JPanel addProgressPanel(String string)
  {
    if (progressPanel == null)
    {
      progressPanel = new JPanel(new GridLayout(1, 1));
      totalProgressCount = 0;
      instance.getContentPane().add(progressPanel, BorderLayout.SOUTH);
    }
    JPanel thisprogress = new JPanel(new BorderLayout(10, 5));
    JProgressBar progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);

    thisprogress.add(new JLabel(string), BorderLayout.WEST);

    thisprogress.add(progressBar, BorderLayout.CENTER);
    progressPanel.add(thisprogress);
    ((GridLayout) progressPanel.getLayout()).setRows(
            ((GridLayout) progressPanel.getLayout()).getRows() + 1);
    ++totalProgressCount;
    instance.validate();
    return thisprogress;
  }

  int totalProgressCount = 0;

  private void removeProgressPanel(JPanel progbar)
  {
    if (progressPanel != null)
    {
      synchronized (progressPanel)
      {
        progressPanel.remove(progbar);
        GridLayout gl = (GridLayout) progressPanel.getLayout();
        gl.setRows(gl.getRows() - 1);
        if (--totalProgressCount < 1)
        {
          this.getContentPane().remove(progressPanel);
          progressPanel = null;
        }
      }
    }
    validate();
  }

  public void stopLoading()
  {
    fileLoadingCount--;
    if (fileLoadingCount < 1)
    {
      while (fileLoadingPanels.size() > 0)
      {
        removeProgressPanel(fileLoadingPanels.remove(0));
      }
      fileLoadingPanels.clear();
      fileLoadingCount = 0;
    }
    validate();
  }

  public static int getViewCount(String alignmentId)
  {
    AlignmentViewport[] aps = getViewports(alignmentId);
    return (aps == null) ? 0 : aps.length;
  }

  /**
   * 
   * @param alignmentId
   *          - if null, all sets are returned
   * @return all AlignmentPanels concerning the alignmentId sequence set
   */
  public static AlignmentPanel[] getAlignmentPanels(String alignmentId)
  {
    if (Desktop.desktop == null)
    {
      // no frames created and in headless mode
      // TODO: verify that frames are recoverable when in headless mode
      return null;
    }
    List<AlignmentPanel> aps = new ArrayList<>();
    AlignFrame[] frames = getAlignFrames();
    if (frames == null)
    {
      return null;
    }
    for (AlignFrame af : frames)
    {
      for (AlignmentPanel ap : af.alignPanels)
      {
        if (alignmentId == null
                || alignmentId.equals(ap.av.getSequenceSetId()))
        {
          aps.add(ap);
        }
      }
    }
    if (aps.size() == 0)
    {
      return null;
    }
    AlignmentPanel[] vap = aps.toArray(new AlignmentPanel[aps.size()]);
    return vap;
  }

  /**
   * get all the viewports on an alignment.
   * 
   * @param sequenceSetId
   *          unique alignment id (may be null - all viewports returned in that
   *          case)
   * @return all viewports on the alignment bound to sequenceSetId
   */
  public static AlignmentViewport[] getViewports(String sequenceSetId)
  {
    List<AlignmentViewport> viewp = new ArrayList<>();
    if (desktop != null)
    {
      AlignFrame[] frames = Desktop.getAlignFrames();

      for (AlignFrame afr : frames)
      {
        if (sequenceSetId == null || afr.getViewport().getSequenceSetId()
                .equals(sequenceSetId))
        {
          if (afr.alignPanels != null)
          {
            for (AlignmentPanel ap : afr.alignPanels)
            {
              if (sequenceSetId == null
                      || sequenceSetId.equals(ap.av.getSequenceSetId()))
              {
                viewp.add(ap.av);
              }
            }
          }
          else
          {
            viewp.add(afr.getViewport());
          }
        }
      }
      if (viewp.size() > 0)
      {
        return viewp.toArray(new AlignmentViewport[viewp.size()]);
      }
    }
    return null;
  }

  /**
   * Explode the views in the given frame into separate AlignFrame
   * 
   * @param af
   */
  public static void explodeViews(AlignFrame af)
  {
    int size = af.alignPanels.size();
    if (size < 2)
    {
      return;
    }

    // FIXME: ideally should use UI interface API
    FeatureSettings viewFeatureSettings = (af.featureSettings != null
            && af.featureSettings.isOpen())
            ? af.featureSettings
            : null;
    Rectangle fsBounds = af.getFeatureSettingsGeometry();
    for (int i = 0; i < size; i++)
    {
      AlignmentPanel ap = af.alignPanels.get(i);

      AlignFrame newaf = new AlignFrame(ap);

      // transfer reference for existing feature settings to new alignFrame
      if (ap == af.alignPanel)
      {
        if (viewFeatureSettings != null && viewFeatureSettings.fr.ap == ap)
        {
          newaf.featureSettings = viewFeatureSettings;
        }
        newaf.setFeatureSettingsGeometry(fsBounds);
      }

      /*
       * Restore the view's last exploded frame geometry if known. Multiple
       * views from one exploded frame share and restore the same (frame)
       * position and size.
       */
      Rectangle geometry = ap.av.getExplodedGeometry();
      if (geometry != null)
      {
        newaf.setBounds(geometry);
      }

      ap.av.setGatherViewsHere(false);

      addInternalFrame(newaf, af.getTitle(), AlignFrame.DEFAULT_WIDTH,
              AlignFrame.DEFAULT_HEIGHT);
      // and materialise a new feature settings dialog instance for the new alignframe
      // (closes the old as if 'OK' was pressed)
      if (ap == af.alignPanel && newaf.featureSettings != null
              && newaf.featureSettings.isOpen()
              && af.alignPanel.getAlignViewport().isShowSequenceFeatures())
      {
        newaf.showFeatureSettingsUI();
      }
    }

    af.featureSettings = null;
    af.alignPanels.clear();
    af.closeMenuItem_actionPerformed(true);

  }

  /**
   * Gather expanded views (separate AlignFrame's) with the same sequence set
   * identifier back in to this frame as additional views, and close the
   * expanded views. Note the expanded frames may themselves have multiple
   * views. We take the lot.
   * 
   * @param source
   */
  public void gatherViews(AlignFrame source)
  {
    source.viewport.setGatherViewsHere(true);
    source.viewport.setExplodedGeometry(source.getBounds());
    JInternalFrame[] frames = desktop.getAllFrames();
    String viewId = source.viewport.getSequenceSetId();
    for (int t = 0; t < frames.length; t++)
    {
      if (frames[t] instanceof AlignFrame && frames[t] != source)
      {
        AlignFrame af = (AlignFrame) frames[t];
        boolean gatherThis = false;
        for (int a = 0; a < af.alignPanels.size(); a++)
        {
          AlignmentPanel ap = af.alignPanels.get(a);
          if (viewId.equals(ap.av.getSequenceSetId()))
          {
            gatherThis = true;
            ap.av.setGatherViewsHere(false);
            ap.av.setExplodedGeometry(af.getBounds());
            source.addAlignmentPanel(ap, false);
          }
        }

        if (gatherThis)
        {
          if (af.featureSettings != null && af.featureSettings.isOpen())
          {
            if (source.featureSettings == null)
            {
              // preserve the feature settings geometry for this frame
              source.featureSettings = af.featureSettings;
              source.setFeatureSettingsGeometry(
                      af.getFeatureSettingsGeometry());
            }
            else
            {
              // close it and forget
              af.featureSettings.close();
            }
          }
          af.alignPanels.clear();
          af.closeMenuItem_actionPerformed(true);
        }
      }
    }
    // refresh the feature setting UI for the source frame if it exists
    if (source.featureSettings != null
            && source.featureSettings.isOpen())
    {
      source.showFeatureSettingsUI();
    }

  }

  public JInternalFrame[] getAllFrames()
  {
    return desktop.getAllFrames();
  }

  /**
   * Checks the given url to see if it gives a response indicating that the user
   * should be informed of a new questionnaire.
   * 
   * @param url
   */
  public void checkForQuestionnaire(String url)
  {
    UserQuestionnaireCheck jvq = new UserQuestionnaireCheck(url);
    // javax.swing.SwingUtilities.invokeLater(jvq);
    new Thread(jvq).start();
  }

  public void checkURLLinks()
  {
    // Thread off the URL link checker
    addDialogThread(new Runnable()
    {
      @Override
      public void run()
      {
        if (Cache.getDefault("CHECKURLLINKS", true))
        {
          // check what the actual links are - if it's just the default don't
          // bother with the warning
          List<String> links = Preferences.sequenceUrlLinks
                  .getLinksForMenu();

          // only need to check links if there is one with a
          // SEQUENCE_ID which is not the default EMBL_EBI link
          ListIterator<String> li = links.listIterator();
          boolean check = false;
          List<JLabel> urls = new ArrayList<>();
          while (li.hasNext())
          {
            String link = li.next();
            if (link.contains(jalview.util.UrlConstants.SEQUENCE_ID)
                    && !UrlConstants.isDefaultString(link))
            {
              check = true;
              int barPos = link.indexOf("|");
              String urlMsg = barPos == -1 ? link
                      : link.substring(0, barPos) + ": "
                              + link.substring(barPos + 1);
              urls.add(new JLabel(urlMsg));
            }
          }
          if (!check)
          {
            return;
          }

          // ask user to check in case URL links use old style tokens
          // ($SEQUENCE_ID$ for sequence id _or_ accession id)
          JPanel msgPanel = new JPanel();
          msgPanel.setLayout(new BoxLayout(msgPanel, BoxLayout.PAGE_AXIS));
          msgPanel.add(Box.createVerticalGlue());
          JLabel msg = new JLabel(MessageManager
                  .getString("label.SEQUENCE_ID_for_DB_ACCESSION1"));
          JLabel msg2 = new JLabel(MessageManager
                  .getString("label.SEQUENCE_ID_for_DB_ACCESSION2"));
          msgPanel.add(msg);
          for (JLabel url : urls)
          {
            msgPanel.add(url);
          }
          msgPanel.add(msg2);

          final JCheckBox jcb = new JCheckBox(
                  MessageManager.getString("label.do_not_display_again"));
          jcb.addActionListener(new ActionListener()
          {
            @Override
            public void actionPerformed(ActionEvent e)
            {
              // update Cache settings for "don't show this again"
              boolean showWarningAgain = !jcb.isSelected();
              Cache.setProperty("CHECKURLLINKS",
                      Boolean.valueOf(showWarningAgain).toString());
            }
          });
          msgPanel.add(jcb);

          JvOptionPane.showMessageDialog(Desktop.desktop, msgPanel,
                  MessageManager
                          .getString("label.SEQUENCE_ID_no_longer_used"),
                  JvOptionPane.WARNING_MESSAGE);
        }
      }
    });
  }

  /**
   * Proxy class for JDesktopPane which optionally displays the current memory
   * usage and highlights the desktop area with a red bar if free memory runs
   * low.
   * 
   * @author AMW
   */
  public class MyDesktopPane extends JDesktopPane implements Runnable
  {

    private static final float ONE_MB = 1048576f;

    boolean showMemoryUsage = false;

    Runtime runtime;

    java.text.NumberFormat df;

    float maxMemory, allocatedMemory, freeMemory, totalFreeMemory,
            percentUsage;

    public MyDesktopPane(boolean showMemoryUsage)
    {
      showMemoryUsage(showMemoryUsage);
    }

    public void showMemoryUsage(boolean showMemory)
    {
      this.showMemoryUsage = showMemory;
      if (showMemory)
      {
        Thread worker = new Thread(this);
        worker.start();
      }
      repaint();
    }

    public boolean isShowMemoryUsage()
    {
      return showMemoryUsage;
    }

    @Override
    public void run()
    {
      df = java.text.NumberFormat.getNumberInstance();
      df.setMaximumFractionDigits(2);
      runtime = Runtime.getRuntime();

      while (showMemoryUsage)
      {
        try
        {
          maxMemory = runtime.maxMemory() / ONE_MB;
          allocatedMemory = runtime.totalMemory() / ONE_MB;
          freeMemory = runtime.freeMemory() / ONE_MB;
          totalFreeMemory = freeMemory + (maxMemory - allocatedMemory);

          percentUsage = (totalFreeMemory / maxMemory) * 100;

          // if (percentUsage < 20)
          {
            // border1 = BorderFactory.createMatteBorder(12, 12, 12, 12,
            // Color.red);
            // instance.set.setBorder(border1);
          }
          repaint();
          // sleep after showing usage
          Thread.sleep(3000);
        } catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
    }

    @Override
    public void paintComponent(Graphics g)
    {
      if (showMemoryUsage && g != null && df != null)
      {
        if (percentUsage < 20)
        {
          g.setColor(Color.red);
        }
        FontMetrics fm = g.getFontMetrics();
        if (fm != null)
        {
          g.drawString(MessageManager.formatMessage("label.memory_stats",
                  new Object[]
                  { df.format(totalFreeMemory), df.format(maxMemory),
                      df.format(percentUsage) }),
                  10, getHeight() - fm.getHeight());
        }
      }
    }
  }

  /**
   * Accessor method to quickly get all the AlignmentFrames loaded.
   * 
   * @return an array of AlignFrame, or null if none found
   */
  public static AlignFrame[] getAlignFrames()
  {
    if (Jalview.isHeadlessMode())
    {
      // Desktop.desktop is null in headless mode
      return new AlignFrame[] { Jalview.currentAlignFrame };
    }

    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return null;
    }
    List<AlignFrame> avp = new ArrayList<>();
    // REVERSE ORDER
    for (int i = frames.length - 1; i > -1; i--)
    {
      if (frames[i] instanceof AlignFrame)
      {
        avp.add((AlignFrame) frames[i]);
      }
      else if (frames[i] instanceof SplitFrame)
      {
        /*
         * Also check for a split frame containing an AlignFrame
         */
        GSplitFrame sf = (GSplitFrame) frames[i];
        if (sf.getTopFrame() instanceof AlignFrame)
        {
          avp.add((AlignFrame) sf.getTopFrame());
        }
        if (sf.getBottomFrame() instanceof AlignFrame)
        {
          avp.add((AlignFrame) sf.getBottomFrame());
        }
      }
    }
    if (avp.size() == 0)
    {
      return null;
    }
    AlignFrame afs[] = avp.toArray(new AlignFrame[avp.size()]);
    return afs;
  }

  /**
   * Returns an array of any AppJmol frames in the Desktop (or null if none).
   * 
   * @return
   */
  public GStructureViewer[] getJmols()
  {
    JInternalFrame[] frames = Desktop.desktop.getAllFrames();

    if (frames == null)
    {
      return null;
    }
    List<GStructureViewer> avp = new ArrayList<>();
    // REVERSE ORDER
    for (int i = frames.length - 1; i > -1; i--)
    {
      if (frames[i] instanceof AppJmol)
      {
        GStructureViewer af = (GStructureViewer) frames[i];
        avp.add(af);
      }
    }
    if (avp.size() == 0)
    {
      return null;
    }
    GStructureViewer afs[] = avp.toArray(new GStructureViewer[avp.size()]);
    return afs;
  }

  /**
   * Add Groovy Support to Jalview
   */
  @Override
  public void groovyShell_actionPerformed()
  {
    try
    {
      openGroovyConsole();
    } catch (Exception ex)
    {
      jalview.bin.Cache.log.error("Groovy Shell Creation failed.", ex);
      JvOptionPane.showInternalMessageDialog(Desktop.desktop,

              MessageManager.getString("label.couldnt_create_groovy_shell"),
              MessageManager.getString("label.groovy_support_failed"),
              JvOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Open the Groovy console
   */
  void openGroovyConsole()
  {
    if (groovyConsole == null)
    {
      groovyConsole = new groovy.ui.Console();
      groovyConsole.setVariable("Jalview", this);
      groovyConsole.run();

      /*
       * We allow only one console at a time, so that AlignFrame menu option
       * 'Calculate | Run Groovy script' is unambiguous.
       * Disable 'Groovy Console', and enable 'Run script', when the console is 
       * opened, and the reverse when it is closed
       */
      Window window = (Window) groovyConsole.getFrame();
      window.addWindowListener(new WindowAdapter()
      {
        @Override
        public void windowClosed(WindowEvent e)
        {
          /*
           * rebind CMD-Q from Groovy Console to Jalview Quit
           */
          addQuitHandler();
          enableExecuteGroovy(false);
        }
      });
    }

    /*
     * show Groovy console window (after close and reopen)
     */
    ((Window) groovyConsole.getFrame()).setVisible(true);

    /*
     * if we got this far, enable 'Run Groovy' in AlignFrame menus
     * and disable opening a second console
     */
    enableExecuteGroovy(true);
  }

  /**
   * Bind Ctrl/Cmd-Q to Quit - for reset as Groovy Console takes over this
   * binding when opened
   */
  protected void addQuitHandler()
  {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
                    jalview.util.ShortcutKeyMaskExWrapper.getMenuShortcutKeyMaskEx()),
                    "Quit");
    getRootPane().getActionMap().put("Quit", new AbstractAction()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        quit();
      }
    });
  }

  /**
   * Enable or disable 'Run Groovy script' in AlignFrame calculate menus
   * 
   * @param enabled
   *          true if Groovy console is open
   */
  public void enableExecuteGroovy(boolean enabled)
  {
    /*
     * disable opening a second Groovy console
     * (or re-enable when the console is closed)
     */
    groovyShell.setEnabled(!enabled);

    AlignFrame[] alignFrames = getAlignFrames();
    if (alignFrames != null)
    {
      for (AlignFrame af : alignFrames)
      {
        af.setGroovyEnabled(enabled);
      }
    }
  }

  /**
   * Progress bars managed by the IProgressIndicator method.
   */
  private Hashtable<Long, JPanel> progressBars;

  private Hashtable<Long, IProgressIndicatorHandler> progressBarHandlers;

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#setProgressBar(java.lang.String, long)
   */
  @Override
  public void setProgressBar(String message, long id)
  {
    if (progressBars == null)
    {
      progressBars = new Hashtable<>();
      progressBarHandlers = new Hashtable<>();
    }

    if (progressBars.get(Long.valueOf(id)) != null)
    {
      JPanel panel = progressBars.remove(Long.valueOf(id));
      if (progressBarHandlers.contains(Long.valueOf(id)))
      {
        progressBarHandlers.remove(Long.valueOf(id));
      }
      removeProgressPanel(panel);
    }
    else
    {
      progressBars.put(Long.valueOf(id), addProgressPanel(message));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see jalview.gui.IProgressIndicator#registerHandler(long,
   * jalview.gui.IProgressIndicatorHandler)
   */
  @Override
  public void registerHandler(final long id,
          final IProgressIndicatorHandler handler)
  {
    if (progressBarHandlers == null
            || !progressBars.containsKey(Long.valueOf(id)))
    {
      throw new Error(MessageManager.getString(
              "error.call_setprogressbar_before_registering_handler"));
    }
    progressBarHandlers.put(Long.valueOf(id), handler);
    final JPanel progressPanel = progressBars.get(Long.valueOf(id));
    if (handler.canCancel())
    {
      JButton cancel = new JButton(
              MessageManager.getString("action.cancel"));
      final IProgressIndicator us = this;
      cancel.addActionListener(new ActionListener()
      {

        @Override
        public void actionPerformed(ActionEvent e)
        {
          handler.cancelActivity(id);
          us.setProgressBar(MessageManager
                  .formatMessage("label.cancelled_params", new Object[]
                  { ((JLabel) progressPanel.getComponent(0)).getText() }),
                  id);
        }
      });
      progressPanel.add(cancel, BorderLayout.EAST);
    }
  }

  /**
   * 
   * @return true if any progress bars are still active
   */
  @Override
  public boolean operationInProgress()
  {
    if (progressBars != null && progressBars.size() > 0)
    {
      return true;
    }
    return false;
  }

  /**
   * This will return the first AlignFrame holding the given viewport instance.
   * It will break if there are more than one AlignFrames viewing a particular
   * av.
   * 
   * @param viewport
   * @return alignFrame for viewport
   */
  public static AlignFrame getAlignFrameFor(AlignViewportI viewport)
  {
    if (desktop != null)
    {
      AlignmentPanel[] aps = getAlignmentPanels(
              viewport.getSequenceSetId());
      for (int panel = 0; aps != null && panel < aps.length; panel++)
      {
        if (aps[panel] != null && aps[panel].av == viewport)
        {
          return aps[panel].alignFrame;
        }
      }
    }
    return null;
  }

  public VamsasApplication getVamsasApplication()
  {
    // TODO: JAL-3311 remove remaining code from Jalview relating to VAMSAS
    return null;

  }

  /**
   * flag set if jalview GUI is being operated programmatically
   */
  private boolean inBatchMode = false;

  /**
   * check if jalview GUI is being operated programmatically
   * 
   * @return inBatchMode
   */
  public boolean isInBatchMode()
  {
    return inBatchMode;
  }

  /**
   * set flag if jalview GUI is being operated programmatically
   * 
   * @param inBatchMode
   */
  public void setInBatchMode(boolean inBatchMode)
  {
    this.inBatchMode = inBatchMode;
  }

  public void startServiceDiscovery()
  {
    startServiceDiscovery(false);
  }

  public void startServiceDiscovery(boolean blocking)
  {
    boolean alive = true;
    Thread t0 = null, t1 = null, t2 = null;
    // JAL-940 - JALVIEW 1 services are now being EOLed as of JABA 2.1 release
    if (true)
    {
      // todo: changesupport handlers need to be transferred
      if (discoverer == null)
      {
        discoverer = new jalview.ws.jws1.Discoverer();
        // register PCS handler for desktop.
        discoverer.addPropertyChangeListener(changeSupport);
      }
      // JAL-940 - disabled JWS1 service configuration - always start discoverer
      // until we phase out completely
      (t0 = new Thread(discoverer)).start();
    }

    if (Cache.getDefault("SHOW_JWS2_SERVICES", true))
    {
      t2 = jalview.ws.jws2.Jws2Discoverer.getDiscoverer()
              .startDiscoverer(changeSupport);
    }
    Thread t3 = null;
    {
      // TODO: do rest service discovery
    }
    if (blocking)
    {
      while (alive)
      {
        try
        {
          Thread.sleep(15);
        } catch (Exception e)
        {
        }
        alive = (t1 != null && t1.isAlive()) || (t2 != null && t2.isAlive())
                || (t3 != null && t3.isAlive())
                || (t0 != null && t0.isAlive());
      }
    }
  }

  /**
   * called to check if the service discovery process completed successfully.
   * 
   * @param evt
   */
  protected void JalviewServicesChanged(PropertyChangeEvent evt)
  {
    if (evt.getNewValue() == null || evt.getNewValue() instanceof Vector)
    {
      final String ermsg = jalview.ws.jws2.Jws2Discoverer.getDiscoverer()
              .getErrorMessages();
      if (ermsg != null)
      {
        if (Cache.getDefault("SHOW_WSDISCOVERY_ERRORS", true))
        {
          if (serviceChangedDialog == null)
          {
            // only run if we aren't already displaying one of these.
            addDialogThread(serviceChangedDialog = new Runnable()
            {
              @Override
              public void run()
              {

                /*
                 * JalviewDialog jd =new JalviewDialog() {
                 * 
                 * @Override protected void cancelPressed() { // TODO
                 * Auto-generated method stub
                 * 
                 * }@Override protected void okPressed() { // TODO
                 * Auto-generated method stub
                 * 
                 * }@Override protected void raiseClosed() { // TODO
                 * Auto-generated method stub
                 * 
                 * } }; jd.initDialogFrame(new
                 * JLabel("<html><table width=\"450\"><tr><td>" + ermsg +
                 * "<br/>It may be that you have invalid JABA URLs in your web service preferences,"
                 * + " or mis-configured HTTP proxy settings.<br/>" +
                 * "Check the <em>Connections</em> and <em>Web services</em> tab of the"
                 * +
                 * " Tools->Preferences dialog box to change them.</td></tr></table></html>"
                 * ), true, true, "Web Service Configuration Problem", 450,
                 * 400);
                 * 
                 * jd.waitForInput();
                 */
                JvOptionPane.showConfirmDialog(Desktop.desktop,
                        new JLabel("<html><table width=\"450\"><tr><td>"
                                + ermsg + "</td></tr></table>"
                                + "<p>It may be that you have invalid JABA URLs<br/>in your web service preferences,"
                                + "<br>or as a command-line argument, or mis-configured HTTP proxy settings.</p>"
                                + "<p>Check the <em>Connections</em> and <em>Web services</em> tab<br/>of the"
                                + " Tools->Preferences dialog box to change them.</p></html>"),
                        "Web Service Configuration Problem",
                        JvOptionPane.DEFAULT_OPTION,
                        JvOptionPane.ERROR_MESSAGE);
                serviceChangedDialog = null;

              }
            });
          }
        }
        else
        {
          Cache.log.error(
                  "Errors reported by JABA discovery service. Check web services preferences.\n"
                          + ermsg);
        }
      }
    }
  }

  private Runnable serviceChangedDialog = null;

  /**
   * start a thread to open a URL in the configured browser. Pops up a warning
   * dialog to the user if there is an exception when calling out to the browser
   * to open the URL.
   * 
   * @param url
   */
  public static void showUrl(final String url)
  {
    showUrl(url, Desktop.instance);
  }

  /**
   * Like showUrl but allows progress handler to be specified
   * 
   * @param url
   * @param progress
   *          (null) or object implementing IProgressIndicator
   */
  public static void showUrl(final String url,
          final IProgressIndicator progress)
  {
    new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        try
        {
          if (progress != null)
          {
            progress.setProgressBar(MessageManager
                    .formatMessage("status.opening_params", new Object[]
                    { url }), this.hashCode());
          }
          jalview.util.BrowserLauncher.openURL(url);
        } catch (Exception ex)
        {
          JvOptionPane.showInternalMessageDialog(Desktop.desktop,
                  MessageManager
                          .getString("label.web_browser_not_found_unix"),
                  MessageManager.getString("label.web_browser_not_found"),
                  JvOptionPane.WARNING_MESSAGE);

          ex.printStackTrace();
        }
        if (progress != null)
        {
          progress.setProgressBar(null, this.hashCode());
        }
      }
    }).start();
  }

  public static WsParamSetManager wsparamManager = null;

  public static ParamManager getUserParameterStore()
  {
    if (wsparamManager == null)
    {
      wsparamManager = new WsParamSetManager();
    }
    return wsparamManager;
  }

  /**
   * static hyperlink handler proxy method for use by Jalview's internal windows
   * 
   * @param e
   */
  public static void hyperlinkUpdate(HyperlinkEvent e)
  {
    if (e.getEventType() == EventType.ACTIVATED)
    {
      String url = null;
      try
      {
        url = e.getURL().toString();
        Desktop.showUrl(url);
      } catch (Exception x)
      {
        if (url != null)
        {
          if (Cache.log != null)
          {
            Cache.log.error("Couldn't handle string " + url + " as a URL.");
          }
          else
          {
            System.err.println(
                    "Couldn't handle string " + url + " as a URL.");
          }
        }
        // ignore any exceptions due to dud links.
      }

    }
  }

  /**
   * single thread that handles display of dialogs to user.
   */
  ExecutorService dialogExecutor = Executors.newSingleThreadExecutor();

  /**
   * flag indicating if dialogExecutor should try to acquire a permit
   */
  private volatile boolean dialogPause = true;

  /**
   * pause the queue
   */
  private java.util.concurrent.Semaphore block = new Semaphore(0);

  private static groovy.ui.Console groovyConsole;

  /**
   * add another dialog thread to the queue
   * 
   * @param prompter
   */
  public void addDialogThread(final Runnable prompter)
  {
    dialogExecutor.submit(new Runnable()
    {
      @Override
      public void run()
      {
        if (dialogPause)
        {
          try
          {
            block.acquire();
          } catch (InterruptedException x)
          {
          }
          ;
        }
        if (instance == null)
        {
          return;
        }
        try
        {
          SwingUtilities.invokeAndWait(prompter);
        } catch (Exception q)
        {
          Cache.log.warn("Unexpected Exception in dialog thread.", q);
        }
      }
    });
  }

  public void startDialogQueue()
  {
    // set the flag so we don't pause waiting for another permit and semaphore
    // the current task to begin
    dialogPause = false;
    block.release();
  }

  @Override
  protected void snapShotWindow_actionPerformed(ActionEvent e)
  {
    invalidate();
    File of;
    ImageMaker im = new jalview.util.ImageMaker(
            this, ImageMaker.TYPE.EPS, "View of Desktop", getWidth(),
            getHeight(), of = new File("Jalview_snapshot"
                    + System.currentTimeMillis() + ".eps"),
            "View of desktop", null, 0, false);
    try
    {
      paintAll(im.getGraphics());
      im.writeImage();
    } catch (Exception q)
    {
      Cache.log.error("Couldn't write snapshot to " + of.getAbsolutePath(),
              q);
      return;
    }
    Cache.log.info("Successfully written snapshot to file "
            + of.getAbsolutePath());
  }

  /**
   * Explode the views in the given SplitFrame into separate SplitFrame windows.
   * This respects (remembers) any previous 'exploded geometry' i.e. the size
   * and location last time the view was expanded (if any). However it does not
   * remember the split pane divider location - this is set to match the
   * 'exploding' frame.
   * 
   * @param sf
   */
  public void explodeViews(SplitFrame sf)
  {
    AlignFrame oldTopFrame = (AlignFrame) sf.getTopFrame();
    AlignFrame oldBottomFrame = (AlignFrame) sf.getBottomFrame();
    List<? extends AlignmentViewPanel> topPanels = oldTopFrame
            .getAlignPanels();
    List<? extends AlignmentViewPanel> bottomPanels = oldBottomFrame
            .getAlignPanels();
    int viewCount = topPanels.size();
    if (viewCount < 2)
    {
      return;
    }

    /*
     * Processing in reverse order works, forwards order leaves the first panels
     * not visible. I don't know why!
     */
    for (int i = viewCount - 1; i >= 0; i--)
    {
      /*
       * Make new top and bottom frames. These take over the respective
       * AlignmentPanel objects, including their AlignmentViewports, so the
       * cdna/protein relationships between the viewports is carried over to the
       * new split frames.
       * 
       * explodedGeometry holds the (x, y) position of the previously exploded
       * SplitFrame, and the (width, height) of the AlignFrame component
       */
      AlignmentPanel topPanel = (AlignmentPanel) topPanels.get(i);
      AlignFrame newTopFrame = new AlignFrame(topPanel);
      newTopFrame.setSize(oldTopFrame.getSize());
      newTopFrame.setVisible(true);
      Rectangle geometry = ((AlignViewport) topPanel.getAlignViewport())
              .getExplodedGeometry();
      if (geometry != null)
      {
        newTopFrame.setSize(geometry.getSize());
      }

      AlignmentPanel bottomPanel = (AlignmentPanel) bottomPanels.get(i);
      AlignFrame newBottomFrame = new AlignFrame(bottomPanel);
      newBottomFrame.setSize(oldBottomFrame.getSize());
      newBottomFrame.setVisible(true);
      geometry = ((AlignViewport) bottomPanel.getAlignViewport())
              .getExplodedGeometry();
      if (geometry != null)
      {
        newBottomFrame.setSize(geometry.getSize());
      }

      topPanel.av.setGatherViewsHere(false);
      bottomPanel.av.setGatherViewsHere(false);
      JInternalFrame splitFrame = new SplitFrame(newTopFrame,
              newBottomFrame);
      if (geometry != null)
      {
        splitFrame.setLocation(geometry.getLocation());
      }
      Desktop.addInternalFrame(splitFrame, sf.getTitle(), -1, -1);
    }

    /*
     * Clear references to the panels (now relocated in the new SplitFrames)
     * before closing the old SplitFrame.
     */
    topPanels.clear();
    bottomPanels.clear();
    sf.close();
  }

  /**
   * Gather expanded split frames, sharing the same pairs of sequence set ids,
   * back into the given SplitFrame as additional views. Note that the gathered
   * frames may themselves have multiple views.
   * 
   * @param source
   */
  public void gatherViews(GSplitFrame source)
  {
    /*
     * special handling of explodedGeometry for a view within a SplitFrame: - it
     * holds the (x, y) position of the enclosing SplitFrame, and the (width,
     * height) of the AlignFrame component
     */
    AlignFrame myTopFrame = (AlignFrame) source.getTopFrame();
    AlignFrame myBottomFrame = (AlignFrame) source.getBottomFrame();
    myTopFrame.viewport.setExplodedGeometry(new Rectangle(source.getX(),
            source.getY(), myTopFrame.getWidth(), myTopFrame.getHeight()));
    myBottomFrame.viewport
            .setExplodedGeometry(new Rectangle(source.getX(), source.getY(),
                    myBottomFrame.getWidth(), myBottomFrame.getHeight()));
    myTopFrame.viewport.setGatherViewsHere(true);
    myBottomFrame.viewport.setGatherViewsHere(true);
    String topViewId = myTopFrame.viewport.getSequenceSetId();
    String bottomViewId = myBottomFrame.viewport.getSequenceSetId();

    JInternalFrame[] frames = desktop.getAllFrames();
    for (JInternalFrame frame : frames)
    {
      if (frame instanceof SplitFrame && frame != source)
      {
        SplitFrame sf = (SplitFrame) frame;
        AlignFrame topFrame = (AlignFrame) sf.getTopFrame();
        AlignFrame bottomFrame = (AlignFrame) sf.getBottomFrame();
        boolean gatherThis = false;
        for (int a = 0; a < topFrame.alignPanels.size(); a++)
        {
          AlignmentPanel topPanel = topFrame.alignPanels.get(a);
          AlignmentPanel bottomPanel = bottomFrame.alignPanels.get(a);
          if (topViewId.equals(topPanel.av.getSequenceSetId())
                  && bottomViewId.equals(bottomPanel.av.getSequenceSetId()))
          {
            gatherThis = true;
            topPanel.av.setGatherViewsHere(false);
            bottomPanel.av.setGatherViewsHere(false);
            topPanel.av.setExplodedGeometry(
                    new Rectangle(sf.getLocation(), topFrame.getSize()));
            bottomPanel.av.setExplodedGeometry(
                    new Rectangle(sf.getLocation(), bottomFrame.getSize()));
            myTopFrame.addAlignmentPanel(topPanel, false);
            myBottomFrame.addAlignmentPanel(bottomPanel, false);
          }
        }

        if (gatherThis)
        {
          topFrame.getAlignPanels().clear();
          bottomFrame.getAlignPanels().clear();
          sf.close();
        }
      }
    }

    /*
     * The dust settles...give focus to the tab we did this from.
     */
    myTopFrame.setDisplayedView(myTopFrame.alignPanel);
  }

  public static groovy.ui.Console getGroovyConsole()
  {
    return groovyConsole;
  }

  /**
   * handles the payload of a drag and drop event.
   * 
   * TODO refactor to desktop utilities class
   * 
   * @param files
   *          - Data source strings extracted from the drop event
   * @param protocols
   *          - protocol for each data source extracted from the drop event
   * @param evt
   *          - the drop event
   * @param t
   *          - the payload from the drop event
   * @throws Exception
   */
  public static void transferFromDropTarget(List<String> files,
          List<DataSourceType> protocols, DropTargetDropEvent evt,
          Transferable t) throws Exception
  {

    DataFlavor uriListFlavor = new DataFlavor(
            "text/uri-list;class=java.lang.String"), urlFlavour = null;
    try
    {
      urlFlavour = new DataFlavor(
              "application/x-java-url; class=java.net.URL");
    } catch (ClassNotFoundException cfe)
    {
      Cache.log.debug("Couldn't instantiate the URL dataflavor.", cfe);
    }

    if (urlFlavour != null && t.isDataFlavorSupported(urlFlavour))
    {

      try
      {
        java.net.URL url = (URL) t.getTransferData(urlFlavour);
        // nb: java 8 osx bug https://bugs.openjdk.java.net/browse/JDK-8156099
        // means url may be null.
        if (url != null)
        {
          protocols.add(DataSourceType.URL);
          files.add(url.toString());
          Cache.log.debug("Drop handled as URL dataflavor "
                  + files.get(files.size() - 1));
          return;
        }
        else
        {
          if (Platform.isAMac())
          {
            System.err.println(
                    "Please ignore plist error - occurs due to problem with java 8 on OSX");
          }
          ;
        }
      } catch (Throwable ex)
      {
        Cache.log.debug("URL drop handler failed.", ex);
      }
    }
    if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
    {
      // Works on Windows and MacOSX
      Cache.log.debug("Drop handled as javaFileListFlavor");
      for (Object file : (List) t
              .getTransferData(DataFlavor.javaFileListFlavor))
      {
        files.add(((File) file).toString());
        protocols.add(DataSourceType.FILE);
      }
    }
    else
    {
      // Unix like behaviour
      boolean added = false;
      String data = null;
      if (t.isDataFlavorSupported(uriListFlavor))
      {
        Cache.log.debug("Drop handled as uriListFlavor");
        // This is used by Unix drag system
        data = (String) t.getTransferData(uriListFlavor);
      }
      if (data == null)
      {
        // fallback to text: workaround - on OSX where there's a JVM bug
        Cache.log.debug("standard URIListFlavor failed. Trying text");
        // try text fallback
        DataFlavor textDf = new DataFlavor(
                "text/plain;class=java.lang.String");
        if (t.isDataFlavorSupported(textDf))
        {
          data = (String) t.getTransferData(textDf);
        }

        Cache.log.debug("Plain text drop content returned "
                + (data == null ? "Null - failed" : data));

      }
      if (data != null)
      {
        while (protocols.size() < files.size())
        {
          Cache.log.debug("Adding missing FILE protocol for "
                  + files.get(protocols.size()));
          protocols.add(DataSourceType.FILE);
        }
        for (java.util.StringTokenizer st = new java.util.StringTokenizer(
                data, "\r\n"); st.hasMoreTokens();)
        {
          added = true;
          String s = st.nextToken();
          if (s.startsWith("#"))
          {
            // the line is a comment (as per the RFC 2483)
            continue;
          }
          java.net.URI uri = new java.net.URI(s);
          if (uri.getScheme().toLowerCase().startsWith("http"))
          {
            protocols.add(DataSourceType.URL);
            files.add(uri.toString());
          }
          else
          {
            // otherwise preserve old behaviour: catch all for file objects
            java.io.File file = new java.io.File(uri);
            protocols.add(DataSourceType.FILE);
            files.add(file.toString());
          }
        }
      }

      if (Cache.log.isDebugEnabled())
      {
        if (data == null || !added)
        {

          if (t.getTransferDataFlavors() != null
                  && t.getTransferDataFlavors().length > 0)
          {
            Cache.log.debug(
                    "Couldn't resolve drop data. Here are the supported flavors:");
            for (DataFlavor fl : t.getTransferDataFlavors())
            {
              Cache.log.debug(
                      "Supported transfer dataflavor: " + fl.toString());
              Object df = t.getTransferData(fl);
              if (df != null)
              {
                Cache.log.debug("Retrieves: " + df);
              }
              else
              {
                Cache.log.debug("Retrieved nothing");
              }
            }
          }
          else
          {
            Cache.log.debug("Couldn't resolve dataflavor for drop: "
                    + t.toString());
          }
        }
      }
    }
    if (Platform.isWindows())

    {
      Cache.log.debug("Scanning dropped content for Windows Link Files");

      // resolve any .lnk files in the file drop
      for (int f = 0; f < files.size(); f++)
      {
        String source = files.get(f).toLowerCase();
        if (protocols.get(f).equals(DataSourceType.FILE)
                && (source.endsWith(".lnk") || source.endsWith(".url")
                        || source.endsWith(".site")))
        {
          try
          {
            File lf = new File(files.get(f));
            // process link file to get a URL
            Cache.log.debug("Found potential link file: " + lf);
            WindowsShortcut wscfile = new WindowsShortcut(lf);
            String fullname = wscfile.getRealFilename();
            protocols.set(f, FormatAdapter.checkProtocol(fullname));
            files.set(f, fullname);
            Cache.log.debug("Parsed real filename " + fullname
                    + " to extract protocol: " + protocols.get(f));
          } catch (Exception ex)
          {
            Cache.log.error(
                    "Couldn't parse " + files.get(f) + " as a link file.",
                    ex);
          }
        }
      }
    }
  }

  /**
   * Sets the Preferences property for experimental features to True or False
   * depending on the state of the controlling menu item
   */
  @Override
  protected void showExperimental_actionPerformed(boolean selected)
  {
    Cache.setProperty(EXPERIMENTAL_FEATURES, Boolean.toString(selected));
  }

  /**
   * Answers a (possibly empty) list of any structure viewer frames (currently
   * for either Jmol or Chimera) which are currently open. This may optionally
   * be restricted to viewers of a specified class, or viewers linked to a
   * specified alignment panel.
   * 
   * @param apanel
   *          if not null, only return viewers linked to this panel
   * @param structureViewerClass
   *          if not null, only return viewers of this class
   * @return
   */
  public List<StructureViewerBase> getStructureViewers(
          AlignmentPanel apanel,
          Class<? extends StructureViewerBase> structureViewerClass)
  {
    List<StructureViewerBase> result = new ArrayList<>();
    JInternalFrame[] frames = Desktop.instance.getAllFrames();

    for (JInternalFrame frame : frames)
    {
      if (frame instanceof StructureViewerBase)
      {
        if (structureViewerClass == null
                || structureViewerClass.isInstance(frame))
        {
          if (apanel == null
                  || ((StructureViewerBase) frame).isLinkedWith(apanel))
          {
            result.add((StructureViewerBase) frame);
          }
        }
      }
    }
    return result;
  }
}
