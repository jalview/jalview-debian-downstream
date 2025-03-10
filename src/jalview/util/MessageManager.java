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
package jalview.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author David Roldan Martinez
 * @author Thomas Abeel
 * 
 * 
 */
public class MessageManager
{
  private static ResourceBundle rb;

  private static Logger log = Logger
          .getLogger(MessageManager.class.getCanonicalName());

  private static Locale loc;

  static
  {
    try
    {
      /* Localize Java dialogs */
      loc = Locale.getDefault();
      // Locale.setDefault(loc);
      /* Getting messages for GV */
      log.info("Getting messages for lang: " + loc);
      rb = ResourceBundle.getBundle("lang.Messages", loc);
      if (log.isLoggable(Level.FINEST))
      {
        // this might take a while, so we only do it if it will be shown
        log.finest("Language keys: " + rb.keySet());
      }
    } catch (Exception q)
    {
      log.warning("Exception when initting Locale for i18n messages\n"
              + q.getMessage());
      q.printStackTrace();
    } catch (Error v)
    {
      log.warning("Error when initting Locale for i18n messages\n"
              + v.getMessage());
      v.printStackTrace();
    }

  }

  /**
   * Returns the resource bundle text for the given key, or if not found, the
   * key prefixed by "[missing key]"
   * 
   * @param key
   * @return
   */
  public static String getString(String key)
  {
    String value = "[missing key] " + key;
    try
    {
      value = rb.getString(key);
    } catch (Exception e)
    {
      log.warning("I18N missing: " + loc + "\t" + key);
    }
    return value;
  }

  public static Locale getLocale()
  {
    return loc;
  }

  /**
   * Returns the resource bundle text for the given key, with tokens {@code {0},
   * {1} etc replaced by the supplied parameters. If the key is not found,
   * returns the key and values prefixed by "[missing key]"
   * 
   * @param key
   * 
   * @return
   */
  public static String formatMessage(String key, Object... params)
  {
    try
    {
      return MessageFormat.format(rb.getString(key), params);
    } catch (Exception e)
    {
      log.warning("I18N missing: " + loc + "\t" + key);
    }
    String value = "[missing key] " + key + "";
    for (Object p : params)
    {
      value += " '" + p.toString() + "'";
    }
    return value;
  }

  /**
   * Returns the resource bundle text for the given key, with tokens {@code {0},
   * {1} etc replaced by the supplied parameters. If the key is not found,
   * returns the key and values prefixed by "[missing key]"
   * 
   * @param key
   * 
   * @return
   */
  public static String formatMessage(String key, String[] params)
  {
    return formatMessage(key, (Object[]) params);
  }

  /**
   * Returns resource bundle text given a root and a human-readable(ish) name
   * that when combined might resolve to an i18n string. {@code name} is forced
   * to lower case, with any spaces removed, and concatenated to {@code keyroot}
   * to form a lookup key.
   * <p>
   * If the key doesn't resolve, then {@code name} is returned.
   * <p>
   * Use this for programmatically constructed keys that might have a human
   * readable alternative used in the program (e.g. BLOSUM62 and
   * label.score_blosum62).
   * 
   * @param keyroot
   * @param name
   * @return
   */
  public static String getStringOrReturn(String keyroot, String name)
  {
    String smkey = keyroot + name.toLowerCase().replaceAll(" ", "");
    try
    {
      name = rb.getString(smkey);
    } catch (Exception x)
    {
      log.finest("I18N missing key with root " + keyroot + ": " + loc + "\t"
              + smkey);
    }
    return name;
  }
}
