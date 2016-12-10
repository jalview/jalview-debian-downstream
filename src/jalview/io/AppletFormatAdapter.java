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
package jalview.io;

import jalview.api.AlignExportSettingI;
import jalview.api.AlignmentViewPanel;
import jalview.datamodel.Alignment;
import jalview.datamodel.AlignmentAnnotation;
import jalview.datamodel.AlignmentI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.PDBEntry.Type;
import jalview.ext.jmol.JmolParser;
import jalview.structure.StructureImportSettings;
import jalview.util.MessageManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * A low level class for alignment and feature IO with alignment formatting
 * methods used by both applet and application for generating flat alignment
 * files. It also holds the lists of magic format names that the applet and
 * application will allow the user to read or write files with.
 *
 * @author $author$
 * @version $Revision$
 */
public class AppletFormatAdapter
{
  private AlignmentViewPanel viewpanel;

  public static String FILE = "File";

  public static String URL = "URL";

  public static String PASTE = "Paste";

  public static String CLASSLOADER = "ClassLoader";

  /**
   * add jalview-derived non-secondary structure annotation from PDB structure
   */
  boolean annotFromStructure = false;

  /**
   * add secondary structure from PDB data with built-in algorithms
   */
  boolean localSecondaryStruct = false;

  /**
   * process PDB data with web services
   */
  boolean serviceSecondaryStruct = false;

  private AlignFile alignFile = null;

  String inFile;

  /**
   * character used to write newlines
   */
  protected String newline = System.getProperty("line.separator");

  private AlignExportSettingI exportSettings;

  /**
   * List of valid format strings used in the isValidFormat method
   */
  public static final String[] READABLE_FORMATS = new String[] { "BLC",
      "CLUSTAL", "FASTA", "MSF", "PileUp", "PIR", "PFAM", "STH", "PDB",
      "JnetFile", "RNAML", PhylipFile.FILE_DESC, JSONFile.FILE_DESC,
      IdentifyFile.FeaturesFile, "HTML", "mmCIF" };

  /**
   * List of readable format file extensions by application in order
   * corresponding to READABLE_FNAMES
   */
  public static final String[] READABLE_EXTENSIONS = new String[] {
      "fa, fasta, mfa, fastq", "aln", "pfam", "msf", "pir", "blc", "amsa",
      "sto,stk", "xml,rnaml", PhylipFile.FILE_EXT, JSONFile.FILE_EXT,
      ".gff2,gff3", "jar,jvp", HtmlFile.FILE_EXT, "cif" };

  /**
   * List of readable formats by application in order corresponding to
   * READABLE_EXTENSIONS
   */
  public static final String[] READABLE_FNAMES = new String[] { "Fasta",
      "Clustal", "PFAM", "MSF", "PIR", "BLC", "AMSA", "Stockholm", "RNAML",
      PhylipFile.FILE_DESC, JSONFile.FILE_DESC, IdentifyFile.FeaturesFile,
      "Jalview", HtmlFile.FILE_DESC, "mmCIF" };

  /**
   * List of valid format strings for use by callers of the formatSequences
   * method
   */
  public static final String[] WRITEABLE_FORMATS = new String[] { "BLC",
      "CLUSTAL", "FASTA", "MSF", "PileUp", "PIR", "PFAM", "AMSA", "STH",
      PhylipFile.FILE_DESC, JSONFile.FILE_DESC };

  /**
   * List of extensions corresponding to file format types in WRITABLE_FNAMES
   * that are writable by the application.
   */
  public static final String[] WRITABLE_EXTENSIONS = new String[] {
      "fa, fasta, mfa, fastq", "aln", "pfam", "msf", "pir", "blc", "amsa",
      "sto,stk", PhylipFile.FILE_EXT, JSONFile.FILE_EXT, "jvp" };

  /**
   * List of writable formats by the application. Order must correspond with the
   * WRITABLE_EXTENSIONS list of formats.
   */
  public static final String[] WRITABLE_FNAMES = new String[] { "Fasta",
      "Clustal", "PFAM", "MSF", "PIR", "BLC", "AMSA", "STH",
      PhylipFile.FILE_DESC, JSONFile.FILE_DESC, "Jalview" };

  public static String INVALID_CHARACTERS = "Contains invalid characters";

  // TODO: make these messages dynamic
  public static String SUPPORTED_FORMATS = "Formats currently supported are\n"
          + prettyPrint(READABLE_FORMATS);

  public AppletFormatAdapter()
  {
  }

  public AppletFormatAdapter(AlignmentViewPanel viewpanel)
  {
    this.viewpanel = viewpanel;
  }

  public AppletFormatAdapter(AlignmentViewPanel alignPanel,
          AlignExportSettingI settings)
  {
    viewpanel = alignPanel;
    exportSettings = settings;
  }

  /**
   *
   * @param els
   * @return grammatically correct(ish) list consisting of els elements.
   */
  public static String prettyPrint(String[] els)
  {
    StringBuffer list = new StringBuffer();
    for (int i = 0, iSize = els.length - 1; i < iSize; i++)
    {
      list.append(els[i]);
      list.append(", ");
    }
    list.append(" and " + els[els.length - 1] + ".");
    return list.toString();
  }

  public void setNewlineString(String nl)
  {
    newline = nl;
  }

  public String getNewlineString()
  {
    return newline;
  }

  /**
   * check that this format is valid for reading
   *
   * @param format
   *          a format string to be compared with READABLE_FORMATS
   * @return true if format is readable
   */
  public static final boolean isValidFormat(String format)
  {
    return isValidFormat(format, false);
  }

  /**
   * validate format is valid for IO
   *
   * @param format
   *          a format string to be compared with either READABLE_FORMATS or
   *          WRITEABLE_FORMATS
   * @param forwriting
   *          when true, format is checked for containment in WRITEABLE_FORMATS
   * @return true if format is valid
   */
  public static final boolean isValidFormat(String format,
          boolean forwriting)
  {
    if (format == null)
    {
      return false;
    }
    boolean valid = false;
    String[] format_list = (forwriting) ? WRITEABLE_FORMATS
            : READABLE_FORMATS;
    for (String element : format_list)
    {
      if (element.equalsIgnoreCase(format))
      {
        return true;
      }
    }

    return valid;
  }

  /**
   * Constructs the correct filetype parser for a characterised datasource
   *
   * @param inFile
   *          data/data location
   * @param type
   *          type of datasource
   * @param format
   *          File format of data provided by datasource
   *
   * @return DOCUMENT ME!
   */
  public AlignmentI readFile(String inFile, String type, String format)
          throws java.io.IOException
  {
    // TODO: generalise mapping between format string and io. class instances
    // using Constructor.invoke reflection
    this.inFile = inFile;
    try
    {
      if (format.equals("FASTA"))
      {
        alignFile = new FastaFile(inFile, type);
      }
      else if (format.equals("MSF"))
      {
        alignFile = new MSFfile(inFile, type);
      }
      else if (format.equals("PileUp"))
      {
        alignFile = new PileUpfile(inFile, type);
      }
      else if (format.equals("CLUSTAL"))
      {
        alignFile = new ClustalFile(inFile, type);
      }
      else if (format.equals("BLC"))
      {
        alignFile = new BLCFile(inFile, type);
      }
      else if (format.equals("PIR"))
      {
        alignFile = new PIRFile(inFile, type);
      }
      else if (format.equals("PFAM"))
      {
        alignFile = new PfamFile(inFile, type);
      }
      else if (format.equals("JnetFile"))
      {
        alignFile = new JPredFile(inFile, type);
        ((JPredFile) alignFile).removeNonSequences();
      }
      else if (format.equals("PDB"))
      {
        // TODO obtain config value from preference settings.
        // Set value to 'true' to test PDB processing with Jmol: JAL-1213
        boolean isParseWithJMOL = StructureImportSettings
                .getDefaultPDBFileParser().equalsIgnoreCase(
                        StructureImportSettings.StructureParser.JMOL_PARSER
                                .toString());
        if (isParseWithJMOL)
        {
          StructureImportSettings.addSettings(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct);
          alignFile = new jalview.ext.jmol.JmolParser(inFile, type);
        }
        else
        {
          StructureImportSettings.addSettings(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct);
          StructureImportSettings.setShowSeqFeatures(true);
          alignFile = new MCview.PDBfile(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct, inFile,
                  type);
        }
        ((StructureFile) alignFile).setDbRefType(format);
      }
      else if (format.equalsIgnoreCase("mmCIF"))
      {
        StructureImportSettings.addSettings(annotFromStructure,
                localSecondaryStruct, serviceSecondaryStruct);
        alignFile = new jalview.ext.jmol.JmolParser(inFile, type);
        ((StructureFile) alignFile).setDbRefType(format);
      }
      else if (format.equals("STH"))
      {
        alignFile = new StockholmFile(inFile, type);
      }
      else if (format.equals("SimpleBLAST"))
      {
        alignFile = new SimpleBlastFile(inFile, type);
      }
      else if (format.equals(PhylipFile.FILE_DESC))
      {
        alignFile = new PhylipFile(inFile, type);
      }
      else if (format.equals(JSONFile.FILE_DESC))
      {
        alignFile = new JSONFile(inFile, type);
      }
      else if (format.equals(HtmlFile.FILE_DESC))
      {
        alignFile = new HtmlFile(inFile, type);
      }
      else if (format.equals("RNAML"))
      {
        alignFile = new RnamlFile(inFile, type);
      }
      else if (format.equals(IdentifyFile.FeaturesFile))
      {
        alignFile = new FeaturesFile(true, inFile, type);
      }
      return buildAlignmentFrom(alignFile);
    } catch (Exception e)
    {
      e.printStackTrace();
      System.err.println("Failed to read alignment using the '" + format
              + "' reader.\n" + e);

      if (e.getMessage() != null
              && e.getMessage().startsWith(INVALID_CHARACTERS))
      {
        throw new java.io.IOException(e.getMessage());
      }

      // Finally test if the user has pasted just the sequence, no id
      if (type.equalsIgnoreCase("Paste"))
      {
        try
        {
          // Possible sequence is just residues with no label
          alignFile = new FastaFile(">UNKNOWN\n" + inFile, "Paste");
          return buildAlignmentFrom(alignFile);

        } catch (Exception ex)
        {
          if (ex.toString().startsWith(INVALID_CHARACTERS))
          {
            throw new java.io.IOException(e.getMessage());
          }

          ex.printStackTrace();
        }
      }
      if (format.equalsIgnoreCase("HTML"))
      {
        throw new IOException(e.getMessage());
      }
      // If we get to this stage, the format was not supported
      throw new java.io.IOException(SUPPORTED_FORMATS);
    }
  }

  /**
   * Constructs the correct filetype parser for an already open datasource
   *
   * @param source
   *          an existing datasource
   * @param format
   *          File format of data that will be provided by datasource
   *
   * @return DOCUMENT ME!
   */
  public AlignmentI readFromFile(FileParse source, String format)
          throws java.io.IOException
  {
    // TODO: generalise mapping between format string and io. class instances
    // using Constructor.invoke reflection
    // This is exactly the same as the readFile method except we substitute
    // 'inFile, type' with 'source'
    this.inFile = source.getInFile();
    String type = source.type;
    try
    {
      if (format.equals("FASTA"))
      {
        alignFile = new FastaFile(source);
      }
      else if (format.equals("MSF"))
      {
        alignFile = new MSFfile(source);
      }
      else if (format.equals("PileUp"))
      {
        alignFile = new PileUpfile(source);
      }
      else if (format.equals("CLUSTAL"))
      {
        alignFile = new ClustalFile(source);
      }
      else if (format.equals("BLC"))
      {
        alignFile = new BLCFile(source);
      }
      else if (format.equals("PIR"))
      {
        alignFile = new PIRFile(source);
      }
      else if (format.equals("PFAM"))
      {
        alignFile = new PfamFile(source);
      }
      else if (format.equals("JnetFile"))
      {
        alignFile = new JPredFile(source);
        ((JPredFile) alignFile).removeNonSequences();
      }
      else if (format.equals("PDB"))
      {
        // TODO obtain config value from preference settings
        boolean isParseWithJMOL = false;
        if (isParseWithJMOL)
        {
          StructureImportSettings.addSettings(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct);
          alignFile = new JmolParser(source);
        }
        else
        {
          StructureImportSettings.setShowSeqFeatures(true);
          alignFile = new MCview.PDBfile(annotFromStructure,
                  localSecondaryStruct, serviceSecondaryStruct, source);
        }
        ((StructureFile) alignFile).setDbRefType(Type.PDB);
      }
      else if (format.equalsIgnoreCase("mmCIF"))
      {
        StructureImportSettings.addSettings(annotFromStructure,
                localSecondaryStruct, serviceSecondaryStruct);
        alignFile = new JmolParser(source);
        ((StructureFile) alignFile).setDbRefType(Type.MMCIF);
      }
      else if (format.equals("STH"))
      {
        alignFile = new StockholmFile(source);
      }
      else if (format.equals("RNAML"))
      {
        alignFile = new RnamlFile(source);
      }
      else if (format.equals("SimpleBLAST"))
      {
        alignFile = new SimpleBlastFile(source);
      }
      else if (format.equals(PhylipFile.FILE_DESC))
      {
        alignFile = new PhylipFile(source);
      }
      else if (format.equals(IdentifyFile.FeaturesFile))
      {
        alignFile = new FeaturesFile(inFile, type);
      }
      else if (format.equals(JSONFile.FILE_DESC))
      {
        alignFile = new JSONFile(source);
      }
      else if (format.equals(HtmlFile.FILE_DESC))
      {
        alignFile = new HtmlFile(source);
      }

      return buildAlignmentFrom(alignFile);

    } catch (Exception e)
    {
      e.printStackTrace();
      System.err.println("Failed to read alignment using the '" + format
              + "' reader.\n" + e);

      if (e.getMessage() != null
              && e.getMessage().startsWith(INVALID_CHARACTERS))
      {
        throw new java.io.IOException(e.getMessage());
      }

      // Finally test if the user has pasted just the sequence, no id
      if (type.equalsIgnoreCase("Paste"))
      {
        try
        {
          // Possible sequence is just residues with no label
          alignFile = new FastaFile(">UNKNOWN\n" + inFile, "Paste");
          return buildAlignmentFrom(alignFile);

        } catch (Exception ex)
        {
          if (ex.toString().startsWith(INVALID_CHARACTERS))
          {
            throw new java.io.IOException(e.getMessage());
          }

          ex.printStackTrace();
        }
      }

      // If we get to this stage, the format was not supported
      throw new java.io.IOException(SUPPORTED_FORMATS);
    }
  }

  /**
   * boilerplate method to handle data from an AlignFile and construct a new
   * alignment or import to an existing alignment
   * 
   * @param alignFile2
   * @return AlignmentI instance ready to pass to a UI constructor
   */
  private AlignmentI buildAlignmentFrom(AlignFile alignFile2)
  {
    // Standard boilerplate for creating alignment from parser
    // alignFile.configureForView(viewpanel);

    AlignmentI al = new Alignment(alignFile.getSeqsAsArray());

    alignFile.addAnnotations(al);

    alignFile.addGroups(al);

    return al;
  }

  /**
   * create an alignment flatfile from a Jalview alignment view
   * 
   * @param format
   * @param jvsuffix
   * @param av
   * @param selectedOnly
   * @return flatfile in a string
   */
  public String formatSequences(String format, boolean jvsuffix,
          AlignmentViewPanel ap, boolean selectedOnly)
  {

    AlignmentView selvew = ap.getAlignViewport().getAlignmentView(
            selectedOnly, false);
    AlignmentI aselview = selvew.getVisibleAlignment(ap.getAlignViewport()
            .getGapCharacter());
    List<AlignmentAnnotation> ala = (ap.getAlignViewport()
            .getVisibleAlignmentAnnotation(selectedOnly));
    if (ala != null)
    {
      for (AlignmentAnnotation aa : ala)
      {
        aselview.addAnnotation(aa);
      }
    }
    viewpanel = ap;
    return formatSequences(format, aselview, jvsuffix);
  }

  /**
   * Construct an output class for an alignment in a particular filetype TODO:
   * allow caller to detect errors and warnings encountered when generating
   * output
   *
   * @param format
   *          string name of alignment format
   * @param alignment
   *          the alignment to be written out
   * @param jvsuffix
   *          passed to AlnFile class controls whether /START-END is added to
   *          sequence names
   *
   * @return alignment flat file contents
   */
  public String formatSequences(String format, AlignmentI alignment,
          boolean jvsuffix)
  {
    try
    {
      AlignFile afile = null;
      if (format.equalsIgnoreCase("FASTA"))
      {
        afile = new FastaFile();
      }
      else if (format.equalsIgnoreCase("MSF"))
      {
        afile = new MSFfile();
      }
      else if (format.equalsIgnoreCase("PileUp"))
      {
        afile = new PileUpfile();
      }
      else if (format.equalsIgnoreCase("CLUSTAL"))
      {
        afile = new ClustalFile();
      }
      else if (format.equalsIgnoreCase("BLC"))
      {
        afile = new BLCFile();
      }
      else if (format.equalsIgnoreCase("PIR"))
      {
        afile = new PIRFile();
      }
      else if (format.equalsIgnoreCase("PFAM"))
      {
        afile = new PfamFile();
      }
      else if (format.equalsIgnoreCase("STH"))
      {
        afile = new StockholmFile(alignment);
      }
      else if (format.equalsIgnoreCase("AMSA"))
      {
        afile = new AMSAFile(alignment);
      }
      else if (format.equalsIgnoreCase(PhylipFile.FILE_DESC))
      {
        afile = new PhylipFile();
      }
      else if (format.equalsIgnoreCase(JSONFile.FILE_DESC))
      {
        afile = new JSONFile();
      }
      else if (format.equalsIgnoreCase("RNAML"))
      {
        afile = new RnamlFile();
      }

      else
      {
        throw new Exception(
                MessageManager
                        .getString("error.implementation_error_unknown_file_format_string"));
      }

      afile.setNewlineString(newline);
      afile.addJVSuffix(jvsuffix);
      afile.setExportSettings(exportSettings);
      afile.configureForView(viewpanel);

      // check whether we were given a specific alignment to export, rather than
      // the one in the viewpanel
      if (viewpanel == null || viewpanel.getAlignment() == null
              || viewpanel.getAlignment() != alignment)
      {
        afile.setSeqs(alignment.getSequencesArray());
      }
      else
      {
        afile.setSeqs(viewpanel.getAlignment().getSequencesArray());
      }

      String afileresp = afile.print();
      if (afile.hasWarningMessage())
      {
        System.err.println("Warning raised when writing as " + format
                + " : " + afile.getWarningMessage());
      }
      return afileresp;
    } catch (Exception e)
    {
      System.err.println("Failed to write alignment as a '" + format
              + "' file\n");
      e.printStackTrace();
    }

    return null;
  }

  public static String checkProtocol(String file)
  {
    String protocol = FILE;
    String ft = file.toLowerCase().trim();
    if (ft.indexOf("http:") == 0 || ft.indexOf("https:") == 0
            || ft.indexOf("file:") == 0)
    {
      protocol = URL;
    }
    return protocol;
  }

  public static void main(String[] args)
  {
    int i = 0;
    while (i < args.length)
    {
      File f = new File(args[i]);
      if (f.exists())
      {
        try
        {
          System.out.println("Reading file: " + f);
          AppletFormatAdapter afa = new AppletFormatAdapter();
          Runtime r = Runtime.getRuntime();
          System.gc();
          long memf = -r.totalMemory() + r.freeMemory();
          long t1 = -System.currentTimeMillis();
          AlignmentI al = afa.readFile(args[i], FILE,
                  new IdentifyFile().identify(args[i], FILE));
          t1 += System.currentTimeMillis();
          System.gc();
          memf += r.totalMemory() - r.freeMemory();
          if (al != null)
          {
            System.out.println("Alignment contains " + al.getHeight()
                    + " sequences and " + al.getWidth() + " columns.");
            try
            {
              System.out.println(new AppletFormatAdapter().formatSequences(
                      "FASTA", al, true));
            } catch (Exception e)
            {
              System.err
                      .println("Couln't format the alignment for output as a FASTA file.");
              e.printStackTrace(System.err);
            }
          }
          else
          {
            System.out.println("Couldn't read alignment");
          }
          System.out.println("Read took " + (t1 / 1000.0) + " seconds.");
          System.out
                  .println("Difference between free memory now and before is "
                          + (memf / (1024.0 * 1024.0) * 1.0) + " MB");
        } catch (Exception e)
        {
          System.err.println("Exception when dealing with " + i
                  + "'th argument: " + args[i] + "\n" + e);
        }
      }
      else
      {
        System.err.println("Ignoring argument '" + args[i] + "' (" + i
                + "'th)- not a readable file.");
      }
      i++;
    }
  }

  /**
   * try to discover how to access the given file as a valid datasource that
   * will be identified as the given type.
   *
   * @param file
   * @param format
   * @return protocol that yields the data parsable as the given type
   */
  public static String resolveProtocol(String file, String format)
  {
    return resolveProtocol(file, format, false);
  }

  public static String resolveProtocol(String file, String format,
          boolean debug)
  {
    // TODO: test thoroughly!
    String protocol = null;
    if (debug)
    {
      System.out.println("resolving datasource started with:\n>>file\n"
              + file + ">>endfile");
    }

    // This might throw a security exception in certain browsers
    // Netscape Communicator for instance.
    try
    {
      boolean rtn = false;
      InputStream is = System.getSecurityManager().getClass()
              .getResourceAsStream("/" + file);
      if (is != null)
      {
        rtn = true;
        is.close();
      }
      if (debug)
      {
        System.err.println("Resource '" + file + "' was "
                + (rtn ? "" : "not") + " located by classloader.");
      }
      ;
      if (rtn)
      {
        protocol = AppletFormatAdapter.CLASSLOADER;
      }

    } catch (Exception ex)
    {
      System.err
              .println("Exception checking resources: " + file + " " + ex);
    }

    if (file.indexOf("://") > -1)
    {
      protocol = AppletFormatAdapter.URL;
    }
    else
    {
      // skipping codebase prepend check.
      protocol = AppletFormatAdapter.FILE;
    }
    FileParse fp = null;
    try
    {
      if (debug)
      {
        System.out.println("Trying to get contents of resource as "
                + protocol + ":");
      }
      fp = new FileParse(file, protocol);
      if (!fp.isValid())
      {
        fp = null;
      }
      else
      {
        if (debug)
        {
          System.out.println("Successful.");
        }
      }
    } catch (Exception e)
    {
      if (debug)
      {
        System.err.println("Exception when accessing content: " + e);
      }
      fp = null;
    }
    if (fp == null)
    {
      if (debug)
      {
        System.out.println("Accessing as paste.");
      }
      protocol = AppletFormatAdapter.PASTE;
      fp = null;
      try
      {
        fp = new FileParse(file, protocol);
        if (!fp.isValid())
        {
          fp = null;
        }
      } catch (Exception e)
      {
        System.err.println("Failed to access content as paste!");
        e.printStackTrace();
        fp = null;
      }
    }
    if (fp == null)
    {
      return null;
    }
    if (format == null || format.length() == 0)
    {
      return protocol;
    }
    else
    {
      try
      {
        String idformat = new jalview.io.IdentifyFile().identify(file,
                protocol);
        if (idformat == null)
        {
          if (debug)
          {
            System.out.println("Format not identified. Inaccessible file.");
          }
          return null;
        }
        if (debug)
        {
          System.out.println("Format identified as " + idformat
                  + "and expected as " + format);
        }
        if (idformat.equals(format))
        {
          if (debug)
          {
            System.out.println("Protocol identified as " + protocol);
          }
          return protocol;
        }
        else
        {
          if (debug)
          {
            System.out
                    .println("File deemed not accessible via " + protocol);
          }
          fp.close();
          return null;
        }
      } catch (Exception e)
      {
        if (debug)
        {
          System.err.println("File deemed not accessible via " + protocol);
          e.printStackTrace();
        }
        ;

      }
    }
    return null;
  }

  public AlignFile getAlignFile()
  {
    return alignFile;
  }

  public void setAlignFile(AlignFile alignFile)
  {
    this.alignFile = alignFile;
  }
}
