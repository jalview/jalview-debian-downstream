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
package jalview.schemes;

import java.awt.*;

/**
 * ColourSchemeProperty Binds names to hardwired colourschemes and tries to deal
 * intelligently with mapping unknown names to user defined colourschemes (that
 * exist or can be created from the string representation of the colourscheme
 * name - either a hex RGB triplet or a named colour under java.awt.color ). The
 * values of the colourscheme constants is important for callers of
 * getColourName(int i), since it can be used to enumerate the set of built in
 * colours. The FIRST_COLOUR and LAST_COLOUR symbols are provided for this.
 * 
 * @author $author$
 * @version $Revision$
 */
public class ColourSchemeProperty
{
  /** Undefined Colourscheme Index */
  public static final int UNDEFINED = -1;

  /** for schemes defined on the fly */
  public static final int USER_DEFINED = 0;

  /** No Colourscheme Index */
  public static final int NONE = 1;

  /** DOCUMENT ME!! */
  public static final int CLUSTAL = 2;

  /** DOCUMENT ME!! */
  public static final int BLOSUM = 3;

  /** DOCUMENT ME!! */
  public static final int PID = 4;

  /** DOCUMENT ME!! */
  public static final int ZAPPO = 5;

  /** DOCUMENT ME!! */
  public static final int TAYLOR = 6;

  /** DOCUMENT ME!! */
  public static final int HYDROPHOBIC = 7;

  /** DOCUMENT ME!! */
  public static final int HELIX = 8;

  /** DOCUMENT ME!! */
  public static final int STRAND = 9;

  /** DOCUMENT ME!! */
  public static final int TURN = 10;

  /** DOCUMENT ME!! */
  public static final int BURIED = 11;

  /** DOCUMENT ME!! */
  public static final int NUCLEOTIDE = 12;

  /**
   * purine/pyrimidine
   */
  public static final int PURINEPYRIMIDINE = 13;

  public static final int COVARIATION = 14;

  /**
   * index of first colourscheme (includes 'None')
   */
  public static final int FIRST_COLOUR = NONE;

  public static final int LAST_COLOUR = NUCLEOTIDE;

  /**
   * DOCUMENT ME!
   * 
   * @param name
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static int getColourIndexFromName(String name)
  {
    int ret = UNDEFINED;

    if (name.equalsIgnoreCase("Clustal"))
    {
      ret = CLUSTAL;
    }
    else if (name.equalsIgnoreCase("Blosum62"))
    {
      ret = BLOSUM;
    }
    else if (name.equalsIgnoreCase("% Identity"))
    {
      ret = PID;
    }
    else if (name.equalsIgnoreCase("Zappo"))
    {
      ret = ZAPPO;
    }
    else if (name.equalsIgnoreCase("Taylor"))
    {
      ret = TAYLOR;
    }
    else if (name.equalsIgnoreCase("Hydrophobic"))
    {
      ret = HYDROPHOBIC;
    }
    else if (name.equalsIgnoreCase("Helix Propensity"))
    {
      ret = HELIX;
    }
    else if (name.equalsIgnoreCase("Strand Propensity"))
    {
      ret = STRAND;
    }
    else if (name.equalsIgnoreCase("Turn Propensity"))
    {
      ret = TURN;
    }
    else if (name.equalsIgnoreCase("Buried Index"))
    {
      ret = BURIED;
    }
    else if (name.equalsIgnoreCase("Nucleotide"))
    {
      ret = NUCLEOTIDE;
    }
    else if (name.equalsIgnoreCase("User Defined"))
    {
      ret = USER_DEFINED;
    }
    else if (name.equalsIgnoreCase("None"))
    {
      ret = NONE;
    }

    return ret;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param cs
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static String getColourName(ColourSchemeI cs)
  {

    int index = NONE;

    if (cs instanceof ClustalxColourScheme)
    {
      index = CLUSTAL;
    }
    else if (cs instanceof Blosum62ColourScheme)
    {
      index = BLOSUM;
    }
    else if (cs instanceof PIDColourScheme)
    {
      index = PID;
    }
    else if (cs instanceof ZappoColourScheme)
    {
      index = ZAPPO;
    }
    else if (cs instanceof TaylorColourScheme)
    {
      index = TAYLOR;
    }
    else if (cs instanceof HydrophobicColourScheme)
    {
      index = HYDROPHOBIC;
    }
    else if (cs instanceof HelixColourScheme)
    {
      index = HELIX;
    }
    else if (cs instanceof StrandColourScheme)
    {
      index = STRAND;
    }
    else if (cs instanceof TurnColourScheme)
    {
      index = TURN;
    }
    else if (cs instanceof BuriedColourScheme)
    {
      index = BURIED;
    }
    else if (cs instanceof NucleotideColourScheme)
    {
      index = NUCLEOTIDE;
    }
    else if (cs instanceof UserColourScheme)
    {
      if ((((UserColourScheme) cs).getName() != null)
              && (((UserColourScheme) cs).getName().length() > 0))
      {
        return ((UserColourScheme) cs).getName();
      }
      // get default colourscheme name
      index = USER_DEFINED;
    }

    return getColourName(index);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param index
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static String getColourName(int index)
  {
    String ret = null;

    switch (index)
    {
    case CLUSTAL:
      ret = "Clustal";

      break;

    case BLOSUM:
      ret = "Blosum62";

      break;

    case PID:
      ret = "% Identity";

      break;

    case ZAPPO:
      ret = "Zappo";

      break;

    case TAYLOR:
      ret = "Taylor";
      break;

    case HYDROPHOBIC:
      ret = "Hydrophobic";

      break;

    case HELIX:
      ret = "Helix Propensity";

      break;

    case STRAND:
      ret = "Strand Propensity";

      break;

    case TURN:
      ret = "Turn Propensity";

      break;

    case BURIED:
      ret = "Buried Index";

      break;

    case NUCLEOTIDE:
      ret = "Nucleotide";

      break;

    case USER_DEFINED:
      ret = "User Defined";

      break;

    default:
      ret = "None";

      break;
    }

    return ret;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param al
   *          DOCUMENT ME!
   * @param name
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static ColourSchemeI getColour(jalview.datamodel.AlignmentI al,
          String name)
  {
    return getColour(al.getSequences(), al.getWidth(), name);
  }

  /**
   * retrieve or create colourscheme associated with name
   * 
   * @param seqs
   *          sequences to colour
   * @param width
   *          range of sequences to colour
   * @param name
   *          colourscheme name, applet colour parameter specification, or
   *          string to parse as colour for new coloursheme
   * @return Valid Colourscheme
   */
  public static ColourSchemeI getColour(java.util.Vector seqs, int width,
          String name)
  {
    int colindex = getColourIndexFromName(name);
    if (colindex == UNDEFINED)
    {
      if (name.indexOf('=') == -1)
      {
        // try to build a colour from the string directly
        try
        {
          return new UserColourScheme(name);
        } catch (Exception e)
        {
          // System.err.println("Ignoring unknown colourscheme name");
        }
      }
      else
      {
        // try to parse the string as a residue colourscheme
        try
        {
          // fix the launchApp user defined coloursheme transfer bug
          jalview.schemes.UserColourScheme ucs = new jalview.schemes.UserColourScheme(
                  "white");
          ucs.parseAppletParameter(name);

        } catch (Exception e)
        {
          // System.err.println("Ignoring exception when parsing colourscheme as applet-parameter");
        }
      }
    }
    return getColour(seqs, width, getColourIndexFromName(name));
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seqs
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param index
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public static ColourSchemeI getColour(java.util.Vector seqs, int width,
          int index)
  {
    ColourSchemeI cs = null;

    switch (index)
    {
    case CLUSTAL:
      cs = new ClustalxColourScheme(seqs, width);

      break;

    case BLOSUM:
      cs = new Blosum62ColourScheme();

      break;

    case PID:
      cs = new PIDColourScheme();

      break;

    case ZAPPO:
      cs = new ZappoColourScheme();

      break;

    case TAYLOR:
      cs = new TaylorColourScheme();
      break;

    case HYDROPHOBIC:
      cs = new HydrophobicColourScheme();

      break;

    case HELIX:
      cs = new HelixColourScheme();

      break;

    case STRAND:
      cs = new StrandColourScheme();

      break;

    case TURN:
      cs = new TurnColourScheme();

      break;

    case BURIED:
      cs = new BuriedColourScheme();

      break;

    case NUCLEOTIDE:
      cs = new NucleotideColourScheme();

      break;

    case USER_DEFINED:
      Color[] col = new Color[24];
      for (int i = 0; i < 24; i++)
      {
        col[i] = Color.white;
      }
      cs = new UserColourScheme(col);
      break;

    default:
      break;
    }

    return cs;
  }

  public static Color getAWTColorFromName(String name)
  {
    Color col = null;
    name = name.toLowerCase();
    if (name.equals("black"))
    {
      col = Color.black;
    }
    else if (name.equals("blue"))
    {
      col = Color.blue;
    }
    else if (name.equals("cyan"))
    {
      col = Color.cyan;
    }
    else if (name.equals("darkGray"))
    {
      col = Color.darkGray;
    }
    else if (name.equals("gray"))
    {
      col = Color.gray;
    }
    else if (name.equals("green"))
    {
      col = Color.green;
    }
    else if (name.equals("lightGray"))
    {
      col = Color.lightGray;
    }
    else if (name.equals("magenta"))
    {
      col = Color.magenta;
    }
    else if (name.equals("orange"))
    {
      col = Color.orange;
    }
    else if (name.equals("pink"))
    {
      col = Color.pink;
    }
    else if (name.equals("red"))
    {
      col = Color.red;
    }
    else if (name.equals("white"))
    {
      col = Color.white;
    }
    else if (name.equals("yellow"))
    {
      col = Color.yellow;
    }

    return col;
  }
}
