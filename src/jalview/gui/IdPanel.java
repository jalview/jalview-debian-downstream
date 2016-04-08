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
package jalview.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import javax.swing.*;

import jalview.datamodel.*;
import jalview.util.UrlLink;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener
{
  protected IdCanvas idCanvas;

  protected AlignViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

  String linkImageURL;

  int offy;

  // int width;
  int lastid = -1;

  boolean mouseDragging = false;

  /**
   * Creates a new IdPanel object.
   * 
   * @param av
   *          DOCUMENT ME!
   * @param parent
   *          DOCUMENT ME!
   */
  public IdPanel(AlignViewport av, AlignmentPanel parent)
  {
    this.av = av;
    alignPanel = parent;
    idCanvas = new IdCanvas(av);
    linkImageURL = getClass().getResource("/images/link.gif").toString();
    setLayout(new BorderLayout());
    add(idCanvas, BorderLayout.CENTER);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseMoved(MouseEvent e)
  {
    SeqPanel sp = alignPanel.seqPanel;
    int seq = Math.max(0, sp.findSeq(e));
    String tmp;
    if (seq > -1 && seq < av.alignment.getHeight())
    {
      SequenceI sequence = av.alignment.getSequenceAt(seq);
      StringBuffer tip = new StringBuffer();
      tip.append("<i>");

      int maxWidth = 0;
      if (sequence.getDescription() != null)
      {
        tmp = sequence.getDescription();
        tip.append("<br>" + tmp);
        maxWidth = Math.max(maxWidth, tmp.length());
      }

      DBRefEntry[] dbrefs = sequence.getDatasetSequence().getDBRef();
      if (av.isShowDbRefs() && dbrefs != null)
      {
        for (int i = 0; i < dbrefs.length; i++)
        {
          tip.append("<br>");
          tmp = dbrefs[i].getSource() + " " + dbrefs[i].getAccessionId();
          tip.append(tmp);
          maxWidth = Math.max(maxWidth, tmp.length());
        }
      }

      // ADD NON POSITIONAL SEQUENCE INFO
      SequenceFeature[] features = sequence.getDatasetSequence()
              .getSequenceFeatures();
      SequenceFeature[] tfeat = new SequenceFeature[1];
      if (av.isShowNpFeats() && features != null)
      {
        for (int i = 0; i < features.length; i++)
        {
          if (features[i].begin == 0 && features[i].end == 0)
          {
            int sz = -tip.length();
            tfeat[0] = features[i];
            sp.appendFeatures(tip, linkImageURL, 0, tfeat,
                    sp.seqCanvas.fr.minmax);
            sz += tip.length();
            maxWidth = Math.max(maxWidth, sz);
          }
        }
      }

      if (maxWidth > 60)
      {
        tip.insert(0, "<table width=350 border=0><tr><td><i>");
        tip.append("</i></td></tr></table>");
      }

      tip.append("</html>");

      setToolTipText("<html>" + sequence.getDisplayId(true) + " "
              + tip.toString());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseDragged(MouseEvent e)
  {
    mouseDragging = true;

    int seq = Math.max(0, alignPanel.seqPanel.findSeq(e));

    if (seq < lastid)
    {
      selectSeqs(lastid - 1, seq);
    }
    else if (seq > lastid)
    {
      selectSeqs(lastid + 1, seq);
    }

    lastid = seq;
    alignPanel.paintAlignment(true);
  }

  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
    if (e.getWheelRotation() > 0)
    {
      alignPanel.scrollUp(false);
    }
    else
    {
      alignPanel.scrollUp(true);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() < 2)
    {
      return;
    }

    java.util.Vector links = Preferences.sequenceURLLinks;
    if (links == null || links.size() < 1)
    {
      return;
    }

    int seq = alignPanel.seqPanel.findSeq(e);
    String url = null;
    int i = 0;
    String id = av.getAlignment().getSequenceAt(seq).getName();
    while (url == null && i < links.size())
    {
      // DEFAULT LINK IS FIRST IN THE LINK LIST
      // BUT IF ITS A REGEX AND DOES NOT MATCH THE NEXT ONE WILL BE TRIED
      url = links.elementAt(i++).toString();
      jalview.util.UrlLink urlLink = null;
      try
      {
        urlLink = new UrlLink(url);
      } catch (Exception foo)
      {
        jalview.bin.Cache.log.error("Exception for URLLink '" + url + "'",
                foo);
        url = null;
        continue;
      }
      ;
      if (!urlLink.isValid())
      {
        jalview.bin.Cache.log.error(urlLink.getInvalidMessage());
        url = null;
        continue;
      }

      String urls[] = urlLink.makeUrls(id, true);
      if (urls == null || urls[0] == null || urls[0].length() < 4)
      {
        url = null;
        continue;
      }
      // just take first URL made from regex
      url = urls[1];
    }
    try
    {
      jalview.util.BrowserLauncher.openURL(url);
    } catch (Exception ex)
    {
      JOptionPane
              .showInternalMessageDialog(
                      Desktop.desktop,
                      "Unixers: Couldn't find default web browser."
                              + "\nAdd the full path to your browser in Preferences.",
                      "Web browser not found", JOptionPane.WARNING_MESSAGE);
      ex.printStackTrace();
    }

  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseEntered(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && (e.getY() < 0) && (av.getStartSeq() > 0))
    {
      scrollThread = new ScrollThread(true);
    }

    if (mouseDragging && (e.getY() >= getHeight())
            && (av.alignment.getHeight() > av.getEndSeq()))
    {
      scrollThread = new ScrollThread(false);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() == 2)
    {
      return;
    }

    int seq = alignPanel.seqPanel.findSeq(e);

    if (javax.swing.SwingUtilities.isRightMouseButton(e))
    {
      Sequence sq = (Sequence) av.getAlignment().getSequenceAt(seq);
      // build a new links menu based on the current links + any non-positional
      // features
      Vector nlinks = new Vector(Preferences.sequenceURLLinks);
      SequenceFeature sf[] = sq==null ? null : sq.getDatasetSequence().getSequenceFeatures();
      for (int sl = 0; sf != null && sl < sf.length; sl++)
      {
        if (sf[sl].begin == sf[sl].end && sf[sl].begin == 0)
        {
          if (sf[sl].links != null && sf[sl].links.size() > 0)
          {
            for (int l = 0, lSize = sf[sl].links.size(); l < lSize; l++)
            {
              nlinks.addElement(sf[sl].links.elementAt(l));
            }
          }
        }
      }

      jalview.gui.PopupMenu pop = new jalview.gui.PopupMenu(alignPanel, sq,
              nlinks, new Vector(Preferences.getGroupURLLinks()));
      pop.show(this, e.getX(), e.getY());

      return;
    }

    if ((av.getSelectionGroup() == null)
            || ((!e.isControlDown() && !e.isShiftDown()) && av
                    .getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.alignment.getWidth() - 1);
    }

    if (e.isShiftDown() && (lastid != -1))
    {
      selectSeqs(lastid, seq);
    }
    else
    {
      selectSeq(seq);
    }
    alignPanel.paintAlignment(true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param seq
   *          DOCUMENT ME!
   */
  void selectSeq(int seq)
  {
    lastid = seq;

    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, true);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   */
  void selectSeqs(int start, int end)
  {
    if (av.getSelectionGroup() == null)
    {
      return;
    }

    if (end >= av.getAlignment().getHeight())
    {
      end = av.getAlignment().getHeight() - 1;
    }

    lastid = start;

    if (end < start)
    {
      int tmp = start;
      start = end;
      end = tmp;
      lastid = end;
    }

    for (int i = start; i <= end; i++)
    {
      av.getSelectionGroup().addSequence(
              av.getAlignment().getSequenceAt(i), true);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  public void mouseReleased(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }

    mouseDragging = false;
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    // always send selection message when mouse is released
    av.sendSelection();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param found
   *          DOCUMENT ME!
   */
  public void highlightSearchResults(java.util.Vector found)
  {
    idCanvas.setHighlighted(found);

    if (found == null)
    {
      return;
    }

    int index = av.alignment.findIndex((SequenceI) found.get(0));

    // do we need to scroll the panel?
    if ((av.getStartSeq() > index) || (av.getEndSeq() < index))
    {
      alignPanel.setScrollValues(av.getStartRes(), index);
    }
  }

  // this class allows scrolling off the bottom of the visible alignment
  class ScrollThread extends Thread
  {
    boolean running = false;

    boolean up = true;

    public ScrollThread(boolean up)
    {
      this.up = up;
      start();
    }

    public void stopScrolling()
    {
      running = false;
    }

    public void run()
    {
      running = true;

      while (running)
      {
        if (alignPanel.scrollUp(up))
        {
          // scroll was ok, so add new sequence to selection
          int seq = av.getStartSeq();

          if (!up)
          {
            seq = av.getEndSeq();
          }

          if (seq < lastid)
          {
            selectSeqs(lastid - 1, seq);
          }
          else if (seq > lastid)
          {
            selectSeqs(lastid + 1, seq);
          }

          lastid = seq;
        }
        else
        {
          running = false;
        }

        alignPanel.paintAlignment(false);

        try
        {
          Thread.sleep(100);
        } catch (Exception ex)
        {
        }
      }
    }
  }
}
