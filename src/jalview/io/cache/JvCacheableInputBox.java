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
package jalview.io.cache;

import jalview.bin.Cache;
import jalview.util.MessageManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class JvCacheableInputBox<E> extends JComboBox<String>
{

  private static final long serialVersionUID = 5774610435079326695L;

  private static final int LEFT_BOARDER_WIDTH = 16;

  private String cacheKey;

  private AppCache appCache;

  private JPopupMenu popup = new JPopupMenu();

  private JMenuItem menuItemClearCache = new JMenuItem();

  volatile boolean enterWasPressed = false;

  /**
   * @return flag indicating if the most recent keypress was enter
   */
  public boolean wasEnterPressed()
  {
    return enterWasPressed;
  }

  /**
   * Constructor given the key to cached values, and the (approximate) length in
   * characters of the input field
   * 
   * @param newCacheKey
   * @param length
   */
  public JvCacheableInputBox(String newCacheKey, int length)
  {
    super();
    this.cacheKey = newCacheKey;
    setEditable(true);
    addKeyListener(new KeyAdapter()
    {

      @Override
      public void keyTyped(KeyEvent e)
      {
        enterWasPressed = false;
        if (e.getKeyCode() == KeyEvent.VK_ENTER)
        {
          enterWasPressed = true;
        }
        // let event bubble up
      }
    });
    if (length > 0)
    {
      StringBuilder sb = new StringBuilder();
      for (int i = 0; i < length; i++)
      {
        sb.append("X");
      }
      setPrototypeDisplayValue(sb.toString());
    }
    appCache = AppCache.getInstance();
    initCachePopupMenu();
    initCache(newCacheKey);
    updateCache();
  }

  /**
   * Method for initialising cache items for a given cache key and populating the
   * in-memory cache with persisted cache items
   * 
   * @param cacheKey
   */
  private void initCache(String cacheKey)
  {
    // obtain persisted cache items from properties file as a delimited string
    String delimitedCacheStr = Cache.getProperty(cacheKey);
    if (delimitedCacheStr == null || delimitedCacheStr.isEmpty())
    {
      return;
    }
    // convert delimited cache items to a list of strings
    List<String> persistedCacheItems = Arrays
            .asList(delimitedCacheStr.split(AppCache.CACHE_DELIMITER));

    LinkedHashSet<String> foundCacheItems = appCache
            .getAllCachedItemsFor(cacheKey);
    if (foundCacheItems == null)
    {
      foundCacheItems = new LinkedHashSet<>();
    }
    // populate memory cache
    for (String cacheItem : persistedCacheItems)
    {
      foundCacheItems.add(cacheItem);
    }
    appCache.putCache(cacheKey, foundCacheItems);
  }

  /**
   * Initialise this cache's pop-up menu
   */
  private void initCachePopupMenu()
  {
    menuItemClearCache.setFont(new java.awt.Font("Verdana", 0, 12));
    menuItemClearCache
            .setText(MessageManager.getString("action.clear_cached_items"));
    menuItemClearCache.addActionListener(new ActionListener()
    {
      @Override
      public void actionPerformed(ActionEvent e)
      {
        // System.out.println(">>>>> Clear cache items");
        setSelectedItem("");
        appCache.deleteCacheItems(cacheKey);
        updateCache();
      }
    });

    popup.add(menuItemClearCache);
    setComponentPopupMenu(popup);
    add(popup);
  }

  /**
   * Answers true if input text is an integer
   * 
   * @param text
   * @return
   */
  static boolean isInteger(String text)
  {
    try
    {
      Integer.parseInt(text);
      return true;
    } catch (NumberFormatException e)
    {
      return false;
    }
  }

  /**
   * Method called to update the cache with the last user input
   */
  public void updateCache()
  {
    SwingUtilities.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        int cacheLimit = Integer.parseInt(appCache.getCacheLimit(cacheKey));
        String userInput = getUserInput();
        if (userInput != null && !userInput.isEmpty())
        {
          LinkedHashSet<String> foundCache = appCache
                  .getAllCachedItemsFor(cacheKey);
          // remove old cache item so as to place current input at the top of
          // the result
          foundCache.remove(userInput);
          foundCache.add(userInput);
          appCache.putCache(cacheKey, foundCache);
        }

        String lastSearch = userInput;
        if (getItemCount() > 0)
        {
          removeAllItems();
        }
        Set<String> cacheItems = appCache.getAllCachedItemsFor(cacheKey);
        List<String> reversedCacheItems = new ArrayList<>();
        reversedCacheItems.addAll(cacheItems);
        cacheItems = null;
        Collections.reverse(reversedCacheItems);
        if (lastSearch.isEmpty())
        {
          addItem("");
        }

        if (reversedCacheItems != null && !reversedCacheItems.isEmpty())
        {
          LinkedHashSet<String> foundCache = appCache
                  .getAllCachedItemsFor(cacheKey);
          boolean prune = reversedCacheItems.size() > cacheLimit;
          int count = 1;
          boolean limitExceeded = false;
          for (String cacheItem : reversedCacheItems)
          {
            limitExceeded = (count++ > cacheLimit);
            if (prune)
            {
              if (limitExceeded)
              {
                foundCache.remove(cacheItem);
              }
              else
              {
                addItem(cacheItem);
              }
            }
            else
            {
              addItem(cacheItem);
            }
          }
          appCache.putCache(cacheKey, foundCache);
        }
        setSelectedItem(lastSearch.isEmpty() ? "" : lastSearch);
      }
    });
  }

  /**
   * This method should be called to persist the in-memory cache when this
   * components parent frame is closed / exited
   */
  public void persistCache()
  {
    appCache.persistCache(cacheKey);
  }

  /**
   * Method to obtain input text from the cache box
   * 
   * @return
   */
  public String getUserInput()
  {
    return getEditor().getItem() == null ? ""
            : getEditor().getItem().toString().trim();
  }

}
