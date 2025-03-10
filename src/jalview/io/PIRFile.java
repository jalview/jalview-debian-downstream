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
package jalview.io;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.util.Comparison;

import java.io.IOException;
import java.util.Vector;

public class PIRFile extends AlignFile
{
  public static boolean useModellerOutput = false;

  Vector words = new Vector(); // Stores the words in a line after splitting

  public PIRFile()
  {
  }

  public PIRFile(String inFile, DataSourceType sourceType)
          throws IOException
  {
    super(inFile, sourceType);
  }

  public PIRFile(FileParse source) throws IOException
  {
    super(source);
  }

  @Override
  public void parse() throws IOException
  {
    StringBuffer sequence;
    String line = null;
    ModellerDescription md;

    while ((line = nextLine()) != null)
    {
      if (line.length() == 0)
      {
        // System.out.println("blank line");
        continue;
      }
      if (line.indexOf("C;") == 0 || line.indexOf("#") == 0)
      {
        continue;
      }
      Sequence newSeq = parseId(line.substring(line.indexOf(";") + 1));

      sequence = new StringBuffer();

      newSeq.setDescription(nextLine()); // this is the title line

      boolean starFound = false;

      while (!starFound)
      {
        line = nextLine();
        sequence.append(line);

        if (line == null)
        {
          break;
        }

        if (line.indexOf("*") > -1)
        {
          starFound = true;
        }
      }

      if (sequence.length() > 0)
      {
        sequence.setLength(sequence.length() - 1);
        newSeq.setSequence(sequence.toString());

        seqs.addElement(newSeq);

        md = new ModellerDescription(newSeq.getDescription());
        md.updateSequenceI(newSeq);
      }
    }
  }

  @Override
  public String print(SequenceI[] s, boolean jvsuffix)
  {
    boolean is_NA = Comparison.isNucleotide(s);
    int len = 72;
    StringBuffer out = new StringBuffer();
    int i = 0;
    ModellerDescription md;

    while ((i < s.length) && (s[i] != null))
    {
      String seq = s[i].getSequenceAsString();
      seq = seq + "*";

      if (is_NA)
      {
        // modeller doesn't really do nucleotides, so we don't do anything fancy
        // Official tags area as follows, for now we'll use P1 and DL
        // Protein (complete) P1
        // Protein (fragment) F1
        // DNA (linear) Dl
        // DNA (circular) DC
        // RNA (linear) RL
        // RNA (circular) RC
        // tRNA N3
        // other functional RNA N1

        out.append(">N1;" + s[i].getName());
        out.append(newline);
        if (s[i].getDescription() == null)
        {
          out.append(s[i].getName() + " "
                  + (s[i].getEnd() - s[i].getStart() + 1));
          out.append(is_NA ? " bases" : " residues");
          out.append(newline);
        }
        else
        {
          out.append(s[i].getDescription());
          out.append(newline);
        }
      }
      else
      {

        if (useModellerOutput)
        {
          out.append(">P1;" + s[i].getName());
          out.append(newline);
          md = new ModellerDescription(s[i]);
          out.append(md.getDescriptionLine());
          out.append(newline);
        }
        else
        {
          out.append(">P1;" + printId(s[i], jvsuffix));
          out.append(newline);
          if (s[i].getDescription() != null)
          {
            out.append(s[i].getDescription());
            out.append(newline);
          }
          else
          {
            out.append(s[i].getName() + " "
                    + (s[i].getEnd() - s[i].getStart() + 1) + " residues");
            out.append(newline);
          }
        }
      }
      int nochunks = (seq.length() / len)
              + (seq.length() % len > 0 ? 1 : 0);

      for (int j = 0; j < nochunks; j++)
      {
        int start = j * len;
        int end = start + len;

        if (end < seq.length())
        {
          out.append(seq.substring(start, end));
          out.append(newline);
        }
        else if (start < seq.length())
        {
          out.append(seq.substring(start));
          out.append(newline);
        }
      }

      i++;
    }

    return out.toString();
  }

}
