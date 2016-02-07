/*
 * Jalview - A Sequence Alignment Editor and Viewer (Version 2.9)
 * Copyright (C) 2015 The Jalview Authors
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

import jalview.api.ComplexAlignFile;
import jalview.api.FeaturesDisplayedI;
import jalview.datamodel.ColumnSelection;
import jalview.datamodel.SequenceI;
import jalview.schemes.ColourSchemeI;

import java.io.IOException;
import java.io.StringReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HtmlFile extends AlignFile implements ComplexAlignFile
{
  public static final String FILE_EXT = "html";

  public static final String FILE_DESC = "HTML";

  private ColourSchemeI colourScheme;

  private boolean showSeqFeatures;

  private ColumnSelection columnSelection;

  private SequenceI[] hiddenSequences;

  private FeaturesDisplayedI displayedFeatures;

  public HtmlFile()
  {
    super();
  }

  public HtmlFile(FileParse source) throws IOException
  {
    super(source);
  }

  public HtmlFile(String inFile, String type) throws IOException
  {
    super(inFile, type);
  }

  @Override
  public void parse() throws IOException
  {
    Element content = null;
    Document doc = null;
    try
    {
      StringBuilder htmlData = new StringBuilder();
      String currentLine;
      while ((currentLine = nextLine()) != null)
      {
        htmlData.append(currentLine);
      }
      doc = Jsoup.parse(htmlData.toString());
    } catch (OutOfMemoryError oom)
    {
      errormessage = "Not enough memory to process HTML document";
      throw new IOException(errormessage);
    }

    try
    {
      boolean contentFromDiv = true;
      // search for BioJSON data in div element with id seqData
      content = doc.select("div[id=seqData]").first();
      if (content == null)
      {
        contentFromDiv = false;
        // search for BioJSON data in input element with id seqData
        content = doc.getElementById("seqData");
      }

      if (content == null)
      {
        errormessage = "The html document is not embedded with BioJSON data";
        throw new IOException(errormessage);
      }
      JSONFile jsonFile = new JSONFile().parse(new StringReader(
              contentFromDiv ? content.text() : content.val()));
      this.seqs = jsonFile.getSeqs();
      this.seqGroups = jsonFile.getSeqGroups();
      this.annotations = jsonFile.getAnnotations();
      this.showSeqFeatures = jsonFile.isShowSeqFeatures();
      this.colourScheme = jsonFile.getColourScheme();
      this.hiddenSequences = jsonFile.getHiddenSequences();
      this.columnSelection = jsonFile.getColumnSelection();
      this.displayedFeatures = jsonFile.getDisplayedFeatures();
    } catch (Exception e)
    {
      throw e;
    }
  }

  @Override
  public String print()
  {
    throw new UnsupportedOperationException(
            "Print method of HtmlFile is not supported!");
  }

  public boolean isShowSeqFeatures()
  {
    return showSeqFeatures;
  }

  public void setShowSeqFeatures(boolean showSeqFeatures)
  {
    this.showSeqFeatures = showSeqFeatures;
  }

  public ColourSchemeI getColourScheme()
  {
    return colourScheme;
  }

  public void setColourScheme(ColourSchemeI colourScheme)
  {
    this.colourScheme = colourScheme;
  }

  public ColumnSelection getColumnSelection()
  {
    return columnSelection;
  }

  public void setColumnSelection(ColumnSelection columnSelection)
  {
    this.columnSelection = columnSelection;
  }

  public SequenceI[] getHiddenSequences()
  {
    return hiddenSequences;
  }

  public void setHiddenSequences(SequenceI[] hiddenSequences)
  {
    this.hiddenSequences = hiddenSequences;
  }

  @Override
  public FeaturesDisplayedI getDisplayedFeatures()
  {
    return displayedFeatures;
  }

}
