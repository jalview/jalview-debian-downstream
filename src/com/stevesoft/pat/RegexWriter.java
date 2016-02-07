//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat;

import java.io.IOException;
import java.io.Writer;

import com.stevesoft.pat.wrap.WriterWrap;

/**
 * A basic extension of FilterWriter that uses Transformer to make replacements
 * in data as it is written out. It attempts to transform a string whenever the
 * End-of-Line (EOL) character is written (which is, by default, the carriage
 * return '\n'). Only the transformed portion of the line is written out,
 * allowing the RegexWriter to wait until a complete pattern is present before
 * attempting to write out info. Until a pattern completes, data is stored in a
 * StringBuffer -- which can be accessed through the length() and charAt()
 * methods of this class.
 * <p>
 * Note a subtlety here -- while a Transformer normally matches at higher
 * priority against the pattern added to it first, this will not necessarily be
 * true when a multi-line match is in progress because one of the complete
 * multi-line patterns may not be completely loaded in RegexWriter's buffer. For
 * this reason, the Transformer class is equipped with a way to add a pattern
 * and replacement rule in three pieces -- a beginning (once this matches,
 * nothing else in the Transformer can match until the whole pattern matches),
 * an ending (the whole pattern is a String formed by adding the beginning and
 * ending), and a ReplaceRule.
 * <p>
 * An illustration of this is given in the this <a
 * href="../test/trans.java">example.</a>
 */
public class RegexWriter extends Writer
{
  Replacer repr;

  Writer w;

  WriterWrap ww;

  StringBuffer sb = new StringBuffer();

  PartialBuffer wrap = new PartialBuffer(sb);

  int pos, epos;

  int interval = 128;

  int bufferSize = 2 * 1024;

  public RegexWriter(Transformer t, Writer w)
  {
    this.w = w;
    ww = new WriterWrap(w);
    repr = t.getReplacer();
    repr.setBuffer(new StringBufferLike(ww));
    repr.setSource(wrap);
  }

  public RegexWriter(Regex r, Writer w)
  {
    this.w = w;
    ww = new WriterWrap(w);
    repr = r.getReplacer();
    repr.setBuffer(new StringBufferLike(ww));
    repr.setSource(wrap);
  }

  char EOLchar = '\n';

  /**
   * This method no longer serves any purpose.
   * 
   * @deprecated
   */
  @Deprecated
  public char getEOLchar()
  {
    return EOLchar;
  }

  /**
   * This method no longer serves any purpose.
   * 
   * @deprecated
   */
  @Deprecated
  public void setEOLchar(char c)
  {
    EOLchar = c;
  }

  int max_lines = 2;

  /**
   * This method no longer serves any purpose.
   * 
   * @deprecated
   */
  @Deprecated
  public int getMaxLines()
  {
    return max_lines;
  }

  /**
   * This method no longer serves any purpose.
   * 
   * @deprecated
   */
  @Deprecated
  public void setMaxLines(int ml)
  {
    max_lines = ml;
  }

  void write() throws IOException
  {
    Regex rex = repr.getRegex();
    int eposOld = epos;
    if (rex.matchAt(wrap, epos) && !wrap.overRun)
    {
      while (pos < epos)
      {
        w.write(sb.charAt(pos++));
      }
      int to = rex.matchedTo();
      repr.setPos(to);
      repr.apply(rex, rex.getReplaceRule());
      epos = pos = to;
      if (epos == eposOld && epos < sb.length())
      {
        epos++;
      }
    }
    else if (!wrap.overRun && epos < sb.length())
    {
      epos++;
    }
    while (pos < epos)
    {
      w.write(sb.charAt(pos++));
    }
    if (epos == sb.length())
    {
      sb.setLength(1);
      pos = epos = 1;
    }
    else if (pos > bufferSize)
    {
      for (int i = bufferSize; i < sb.length(); i++)
      {
        sb.setCharAt(i - bufferSize, sb.charAt(i));
      }
      pos -= bufferSize;
      epos -= bufferSize;
      sb.setLength(sb.length() - bufferSize);
    }
  }

  public void write(char[] ca, int b, int n) throws IOException
  {
    int m = b + n;
    for (int i = b; i < m; i++)
    {
      sb.append(ca[i]);
      if (sb.length() % interval == interval - 1)
      {
        wrap.overRun = false;
        while (epos + interval < sb.length() && !wrap.overRun)
        {
          write();
        }
      }
    }
  }

  public void flush() throws IOException
  {
  }

  public void close() throws IOException
  {
    wrap.allowOverRun = false;
    wrap.overRun = false;
    while (epos < sb.length())
    {
      write();
    }
    write();
    w.close();
  }

  /** The current size of the StringBuffer in use by RegexWriter. */
  public int length()
  {
    return sb.length();
  }

  /** The character at location i in the StringBuffer. */
  public char charAt(int i)
  {
    return sb.charAt(i);
  }

  /** Set the interval at which regex patterns are checked. */
  public void setInterval(int i)
  {
    interval = i;
  }

  /** Get the interval at which regex matches are checked. */
  public int getInterval()
  {
    return interval;
  }

  /** Get the buffer size. */
  public int getBufferSize()
  {
    return bufferSize;
  }

  /** Set the buffer size. */
  public void setBufferSize(int i)
  {
    bufferSize = i;
  }
}
