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
package jalview.ws.rest.params;

import jalview.datamodel.AlignmentI;
import jalview.io.FileFormat;
import jalview.io.FileFormatI;
import jalview.io.FileFormats;
import jalview.io.FormatAdapter;
import jalview.ws.params.OptionI;
import jalview.ws.params.simple.BooleanOption;
import jalview.ws.params.simple.Option;
import jalview.ws.rest.InputType;
import jalview.ws.rest.NoValidInputDataException;
import jalview.ws.rest.RestJob;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;

/**
 * format an alignment for input to rest service.
 * 
 * @author JimP
 * 
 */
public class Alignment extends InputType
{
  public Alignment()
  {
    super(new Class[] { AlignmentI.class });
  }

  FileFormatI format = FileFormat.Fasta;

  molType type;

  boolean jvsuffix = false;

  /**
   * input data as a file upload rather than inline content
   */
  public boolean writeAsFile = false;

  @Override
  public ContentBody formatForInput(RestJob rj)
          throws UnsupportedEncodingException, NoValidInputDataException
  {
    AlignmentI alignment = rj.getAlignmentForInput(token, type);
    if (writeAsFile)
    {
      try
      {
        File fa = File.createTempFile("jvmime", ".fa");
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                new BufferedOutputStream(new FileOutputStream(fa)),
                "UTF-8"));
        pw.append(new FormatAdapter().formatSequences(format, alignment,
                jvsuffix));
        pw.close();
        return new FileBody(fa, "text/plain");
      } catch (Exception ex)
      {
        throw new NoValidInputDataException(
                "Couldn't write out alignment to file.", ex);
      }
    }
    else
    {
      FormatAdapter fa = new FormatAdapter();
      fa.setNewlineString("\r\n");
      return new StringBody(
              (fa.formatSequences(format, alignment, jvsuffix)));
      // ,
      // "text/plain",Charset.forName("UTF-8"));
      // , "text/plain", Charset.forName("UTF-8"));
      // sb.getContentTypeParameters().put("filename", "alignment.fa");
    }
  }

  @Override
  public List<String> getURLEncodedParameter()
  {
    List<String> prms = new ArrayList<String>();
    prms.add("format='" + format.getName() + "'");
    if (type != null)
    {
      prms.add("type='" + type.toString() + "'");
    }
    if (jvsuffix)
    {
      prms.add("jvsuffix");
    }
    if (writeAsFile)
    {
      prms.add("writeasfile");
    }
    return prms;
  }

  @Override
  public String getURLtokenPrefix()
  {
    return "ALIGNMENT";
  }

  @Override
  public boolean configureProperty(String tok, String val,
          StringBuffer warnings)
  {
    if (tok.startsWith("jvsuffix"))
    {
      jvsuffix = true;
      return true;
    }
    if (tok.startsWith("writeasfile"))
    {
      writeAsFile = true;
      return true;
    }

    if (tok.startsWith("format"))
    {
      for (FileFormatI fmt : FileFormats.getInstance().getFormats())
      {
        if (fmt.isWritable() && val.equalsIgnoreCase(fmt.getName()))
        {
          format = fmt;
          return true;
        }
      }
      warnings.append(
              "Invalid alignment format '" + val + "'. Must be one of (");
      for (String fmt : FileFormats.getInstance().getWritableFormats(true))
      {
        warnings.append(" ").append(fmt);
      }
      warnings.append(")\n");
    }
    if (tok.startsWith("type"))
    {
      try
      {
        type = molType.valueOf(val);
        return true;
      } catch (Exception x)
      {
        warnings.append(
                "Invalid molecule type '" + val + "'. Must be one of (");
        for (molType v : molType.values())
        {
          warnings.append(" " + v);
        }
        warnings.append(")\n");
      }
    }
    return false;
  }

  @Override
  public List<OptionI> getOptions()
  {
    List<OptionI> lst = getBaseOptions();
    lst.add(new BooleanOption("jvsuffix",
            "Append jalview style /start-end suffix to ID", false, false,
            jvsuffix, null));
    lst.add(new BooleanOption("writeasfile",
            "Append jalview style /start-end suffix to ID", false, false,
            writeAsFile, null));

    List<String> writable = FileFormats.getInstance()
            .getWritableFormats(true);
    lst.add(new Option("format", "Alignment upload format", true,
            FileFormat.Fasta.toString(), format.getName(), writable, null));
    lst.add(createMolTypeOption("type", "Sequence type", false, type,
            null));

    return lst;
  }

}
