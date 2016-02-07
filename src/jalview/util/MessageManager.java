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

  private static Logger log = Logger.getLogger(MessageManager.class
          .getCanonicalName());

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

  public static String formatMessage(String key, Object... params)
  {
    return MessageFormat.format(rb.getString(key), params);
  }

  public static String formatMessage(String key, String[] params)
  {
    return MessageFormat.format(rb.getString(key), (Object[]) params);
  }

  /**
   * lookup and return a key given a root and a human-readable(ish) name that
   * when combined might resolve to an i18n string. If the key doesn't resolve,
   * then name is returned.if the key doesn't exist. Use this for
   * programatically constructed keys that have have a human readable
   * alternative used in the program (e.g. BLOSUM62 and label.score_blosum62)
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
      log.finest("I18N missing key with root " + keyroot + ": " + loc
              + "\t" + smkey);
    }
    return name;
  }
}
