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
//////////////////////////////////////////////////////////////////
package jalview.io;

import jalview.bin.Cache;
import jalview.gui.JvOptionPane;
import jalview.util.MessageManager;
import jalview.util.Platform;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

/**
 * Enhanced file chooser dialog box.
 *
 * NOTE: bug on Windows systems when filechooser opened on directory to view
 * files with colons in title.
 *
 * @author AMW
 *
 */
public class JalviewFileChooser extends JFileChooser
{
  /**
   * backupfilesCheckBox = "Include backup files" checkbox includeBackupfiles =
   * flag set by checkbox
   */
  private JCheckBox backupfilesCheckBox = null;

  protected boolean includeBackupFiles = false;

  /**
   * Factory method to return a file chooser that offers readable alignment file
   * formats
   * 
   * @param directory
   * @param selected
   * @return
   */
  public static JalviewFileChooser forRead(String directory,
          String selected)
  {
    return JalviewFileChooser.forRead(directory, selected, false);
  }

  public static JalviewFileChooser forRead(String directory,
          String selected, boolean allowBackupFiles)
  {
    List<String> extensions = new ArrayList<>();
    List<String> descs = new ArrayList<>();
    for (FileFormatI format : FileFormats.getInstance().getFormats())
    {
      if (format.isReadable())
      {
        extensions.add(format.getExtensions());
        descs.add(format.getName());
      }
    }

    return new JalviewFileChooser(directory,
            extensions.toArray(new String[extensions.size()]),
            descs.toArray(new String[descs.size()]), selected, true,
            allowBackupFiles);
  }

  /**
   * Factory method to return a file chooser that offers writable alignment file
   * formats
   * 
   * @param directory
   * @param selected
   * @return
   */
  public static JalviewFileChooser forWrite(String directory,
          String selected)
  {
    // TODO in Java 8, forRead and forWrite can be a single method
    // with a lambda expression parameter for isReadable/isWritable
    List<String> extensions = new ArrayList<>();
    List<String> descs = new ArrayList<>();
    for (FileFormatI format : FileFormats.getInstance().getFormats())
    {
      if (format.isWritable())
      {
        extensions.add(format.getExtensions());
        descs.add(format.getName());
      }
    }
    return new JalviewFileChooser(directory,
            extensions.toArray(new String[extensions.size()]),
            descs.toArray(new String[descs.size()]), selected, false);
  }

  public JalviewFileChooser(String dir)
  {
    super(safePath(dir));
    setAccessory(new RecentlyOpened());
  }

  public JalviewFileChooser(String dir, String[] suffix, String[] desc,
          String selected)
  {
    this(dir, suffix, desc, selected, true);
  }

  /**
   * Constructor for a single choice of file extension and description
   * 
   * @param extension
   * @param desc
   */
  public JalviewFileChooser(String extension, String desc)
  {
    this(Cache.getProperty("LAST_DIRECTORY"), new String[] { extension },
            new String[]
            { desc }, desc, true);
  }

  JalviewFileChooser(String dir, String[] extensions, String[] descs,
          String selected, boolean allFiles)
  {
    this(dir, extensions, descs, selected, allFiles, false);
  }

  public JalviewFileChooser(String dir, String[] extensions, String[] descs,
          String selected, boolean allFiles, boolean allowBackupFiles)
  {
    super(safePath(dir));
    if (extensions.length == descs.length)
    {
      List<String[]> formats = new ArrayList<>();
      for (int i = 0; i < extensions.length; i++)
      {
        formats.add(new String[] { extensions[i], descs[i] });
      }
      init(formats, selected, allFiles, allowBackupFiles);
    }
    else
    {
      System.err.println("JalviewFileChooser arguments mismatch: "
              + extensions + ", " + descs);
    }
  }

  private static File safePath(String dir)
  {
    if (dir == null)
    {
      return null;
    }

    File f = new File(dir);
    if (f.getName().indexOf(':') > -1)
    {
      return null;
    }
    return f;
  }

  /**
   * 
   * @param formats
   *          a list of {extensions, description} for each file format
   * @param selected
   * @param allFiles
   *          if true, 'any format' option is included
   */
  void init(List<String[]> formats, String selected, boolean allFiles)
  {
    init(formats, selected, allFiles, false);
  }

  void init(List<String[]> formats, String selected, boolean allFiles,
          boolean allowBackupFiles)
  {

    JalviewFileFilter chosen = null;

    // SelectAllFilter needs to be set first before adding further
    // file filters to fix bug on Mac OSX
    setAcceptAllFileFilterUsed(allFiles);

    for (String[] format : formats)
    {
      JalviewFileFilter jvf = new JalviewFileFilter(format[0], format[1]);
      if (allowBackupFiles)
      {
        jvf.setParentJFC(this);
      }
      addChoosableFileFilter(jvf);
      if ((selected != null) && selected.equalsIgnoreCase(format[1]))
      {
        chosen = jvf;
      }
    }

    if (chosen != null)
    {
      setFileFilter(chosen);
    }

    if (allowBackupFiles)
    {
      JPanel multi = new JPanel();
      multi.setLayout(new BoxLayout(multi, BoxLayout.PAGE_AXIS));
      if (backupfilesCheckBox == null)
      {
        try {
          includeBackupFiles = Boolean.parseBoolean(
                  Cache.getProperty(BackupFiles.NS + "_FC_INCLUDE"));
        } catch (Exception e)
        {
          includeBackupFiles = false;
        }
        backupfilesCheckBox = new JCheckBox(
                MessageManager.getString("label.include_backup_files"),
                includeBackupFiles);
        backupfilesCheckBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        JalviewFileChooser jfc = this;
        backupfilesCheckBox.addActionListener(new ActionListener()
        {
          @Override
          public void actionPerformed(ActionEvent e)
          {
            includeBackupFiles = backupfilesCheckBox.isSelected();
            Cache.setProperty(BackupFiles.NS + "_FC_INCLUDE",
                    String.valueOf(includeBackupFiles));

            FileFilter f = jfc.getFileFilter();
            // deselect the selected file if it's no longer choosable
            File selectedFile = jfc.getSelectedFile();
            if (selectedFile != null && !f.accept(selectedFile))
            {
              jfc.setSelectedFile(null);
            }
            // fake the OK button changing (to force it to upate)
            String s = jfc.getApproveButtonText();
            jfc.firePropertyChange(
                    APPROVE_BUTTON_TEXT_CHANGED_PROPERTY, null, s);
            // fake the file filter changing (its behaviour actually has)
            jfc.firePropertyChange(FILE_FILTER_CHANGED_PROPERTY, null, f);

            jfc.rescanCurrentDirectory();
            jfc.revalidate();
            jfc.repaint();
          }
        });
      }
      multi.add(new RecentlyOpened());
      multi.add(backupfilesCheckBox);
      setAccessory(multi);
    }
    else
    {
      // set includeBackupFiles=false to avoid other file choosers from picking
      // up backup files (Just In Case)
      includeBackupFiles = false;
      setAccessory(new RecentlyOpened());
    }
  }

  @Override
  public void setFileFilter(javax.swing.filechooser.FileFilter filter)
  {
    super.setFileFilter(filter);

    try
    {
      if (getUI() instanceof BasicFileChooserUI)
      {
        final BasicFileChooserUI fcui = (BasicFileChooserUI) getUI();
        final String name = fcui.getFileName().trim();

        if ((name == null) || (name.length() == 0))
        {
          return;
        }

        EventQueue.invokeLater(new Thread()
        {
          @Override
          public void run()
          {
            String currentName = fcui.getFileName();
            if ((currentName == null) || (currentName.length() == 0))
            {
              fcui.setFileName(name);
            }
          }
        });
      }
    } catch (Exception ex)
    {
      ex.printStackTrace();
      // Some platforms do not have BasicFileChooserUI
    }
  }

  /**
   * Returns the selected file format, or null if none selected
   * 
   * @return
   */
  public FileFormatI getSelectedFormat()
  {
    if (getFileFilter() == null)
    {
      return null;
    }

    /*
     * logic here depends on option description being formatted as 
     * formatName (extension, extension...)
     * or the 'no option selected' value
     * All Files
     * @see JalviewFileFilter.getDescription
     */
    String format = getFileFilter().getDescription();
    int parenPos = format.indexOf("(");
    if (parenPos > 0)
    {
      format = format.substring(0, parenPos).trim();
      try
      {
        return FileFormats.getInstance().forName(format);
      } catch (IllegalArgumentException e)
      {
        System.err.println("Unexpected format: " + format);
      }
    }
    return null;
  }

  File ourselectedFile = null;

  @Override
  public File getSelectedFile()
  {
    File selfile = super.getSelectedFile();
    if (selfile == null && ourselectedFile != null)
    {
      return ourselectedFile;
    }
    return selfile;
  }

  @Override
  public int showSaveDialog(Component parent) throws HeadlessException
  {
    this.setAccessory(null);
    this.setSelectedFile(null);
    return super.showSaveDialog(parent);
  }

  /**
   * If doing a Save, and an existing file is chosen or entered, prompt for
   * confirmation of overwrite. Proceed if Yes, else leave the file chooser
   * open.
   * 
   * @see https://stackoverflow.com/questions/8581215/jfilechooser-and-checking-for-overwrite
   */
  @Override
  public void approveSelection()
  {
    if (getDialogType() != SAVE_DIALOG)
    {
      super.approveSelection();
      return;
    }

    ourselectedFile = getSelectedFile();

    if (ourselectedFile == null)
    {
      // Workaround for Java 9,10 on OSX - no selected file, but there is a
      // filename typed in
      try
      {
        String filename = ((BasicFileChooserUI) getUI()).getFileName();
        if (filename != null && filename.length() > 0)
        {
          ourselectedFile = new File(getCurrentDirectory(), filename);
        }
      } catch (Throwable x)
      {
        System.err.println(
                "Unexpected exception when trying to get filename.");
        x.printStackTrace();
      }
      // TODO: ENSURE THAT FILES SAVED WITH A ':' IN THE NAME ARE REFUSED AND
      // THE
      // USER PROMPTED FOR A NEW FILENAME
    }
    if (ourselectedFile == null)
    {
      return;
    }

    if (getFileFilter() instanceof JalviewFileFilter)
    {
      JalviewFileFilter jvf = (JalviewFileFilter) getFileFilter();

      if (!jvf.accept(ourselectedFile))
      {
        String withExtension = getSelectedFile().getName() + "."
                + jvf.getAcceptableExtension();
        ourselectedFile = (new File(getCurrentDirectory(), withExtension));
        setSelectedFile(ourselectedFile);
      }
    }

    if (ourselectedFile.exists())
    {
      int confirm = JvOptionPane.showConfirmDialog(this,
              MessageManager.getString("label.overwrite_existing_file"),
              MessageManager.getString("label.file_already_exists"),
              JvOptionPane.YES_NO_OPTION);

      if (confirm != JvOptionPane.YES_OPTION)
      {
        return;
      }
    }

    super.approveSelection();
  }

  void recentListSelectionChanged(Object selection)
  {
    setSelectedFile(null);
    if (selection != null)
    {
      File file = new File((String) selection);
      if (getFileFilter() instanceof JalviewFileFilter)
      {
        JalviewFileFilter jvf = (JalviewFileFilter) this.getFileFilter();

        if (!jvf.accept(file))
        {
          setFileFilter(getChoosableFileFilters()[0]);
        }
      }

      setSelectedFile(file);
    }
  }

  /**
   * A panel to set as the 'accessory' component to the file chooser dialog,
   * holding a list of recently opened files (if any). These are held as a
   * tab-separated list of file paths under key <code>RECENT_FILE</code> in
   * <code>.jalview_properties</code>. A click in the list calls a method in
   * JalviewFileChooser to set the chosen file as the selection.
   */
  class RecentlyOpened extends JPanel
  {
    private static final long serialVersionUID = 1L;

    private JList<String> list;

    RecentlyOpened()
    {
      String historyItems = Cache.getProperty("RECENT_FILE");
      StringTokenizer st;
      Vector<String> recent = new Vector<>();

      if (historyItems != null)
      {
        st = new StringTokenizer(historyItems, "\t");
        while (st.hasMoreTokens())
        {
          recent.addElement(st.nextToken());
        }
      }

      list = new JList<>(recent);

      DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
      dlcr.setHorizontalAlignment(DefaultListCellRenderer.RIGHT);
      list.setCellRenderer(dlcr);

      list.addMouseListener(new MouseAdapter()
      {
        @Override
        public void mousePressed(MouseEvent evt)
        {
          recentListSelectionChanged(list.getSelectedValue());
        }
      });

      this.setBorder(new TitledBorder(
              MessageManager.getString("label.recently_opened")));

      final JScrollPane scroller = new JScrollPane(list);

      SpringLayout layout = new SpringLayout();
      layout.putConstraint(SpringLayout.WEST, scroller, 5,
              SpringLayout.WEST, this);
      layout.putConstraint(SpringLayout.NORTH, scroller, 5,
              SpringLayout.NORTH, this);

      if (Platform.isAMac())
      {
        scroller.setPreferredSize(new Dimension(500, 100));
      }
      else
      {
        scroller.setPreferredSize(new Dimension(130, 200));
      }

      this.add(scroller);

      SwingUtilities.invokeLater(new Runnable()
      {
        @Override
        public void run()
        {
          scroller.getHorizontalScrollBar()
                  .setValue(scroller.getHorizontalScrollBar().getMaximum());
        }
      });
    }
  }
}
