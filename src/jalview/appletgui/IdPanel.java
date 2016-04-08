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
package jalview.appletgui;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

import jalview.datamodel.*;
import jalview.util.UrlLink;

public class IdPanel extends Panel implements MouseListener,
        MouseMotionListener
{

  protected IdCanvas idCanvas;

  protected AlignViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

  int offy;

  int width;

  int lastid = -1;

  boolean mouseDragging = false;

  java.util.Vector links = new java.util.Vector();

  public IdPanel(AlignViewport av, AlignmentPanel parent)
  {
    this.av = av;
    alignPanel = parent;
    idCanvas = new IdCanvas(av);
    setLayout(new BorderLayout());
    add(idCanvas, BorderLayout.CENTER);
    idCanvas.addMouseListener(this);
    idCanvas.addMouseMotionListener(this);

    String label, url;
    // TODO: add in group link parameter
    if (av.applet != null)
    {
      for (int i = 1; i < 10; i++)
      {
        label = av.applet.getParameter("linkLabel_" + i);
        url = av.applet.getParameter("linkURL_" + i);

        if (label != null && url != null)
        {
          links.addElement(label + "|" + url);
        }

      }
    }
    if (links.size() < 1)
    {
      links = new java.util.Vector();
      links.addElement("SRS|http://srs.ebi.ac.uk/srsbin/cgi-bin/wgetz?-newId+(([uniprot-all:$SEQUENCE_ID$]))+-view+SwissEntry");
    }
  }

  Tooltip tooltip;

  public void mouseMoved(MouseEvent e)
  {
    int seq = alignPanel.seqPanel.findSeq(e);

    SequenceI sequence = av.getAlignment().getSequenceAt(seq);

    // look for non-pos features
    StringBuffer tooltiptext = new StringBuffer();
    if (sequence != null)
    {
      if (sequence.getDescription() != null)
      {
        tooltiptext.append(sequence.getDescription());
        tooltiptext.append("\n");
      }

      SequenceFeature sf[] = sequence.getSequenceFeatures();
      for (int sl = 0; sf != null && sl < sf.length; sl++)
      {
        if (sf[sl].begin == sf[sl].end && sf[sl].begin == 0)
        {
          boolean nl = false;
          if (sf[sl].getFeatureGroup() != null)
          {
            tooltiptext.append(sf[sl].getFeatureGroup());
            nl = true;
          }
          ;
          if (sf[sl].getType() != null)
          {
            tooltiptext.append(" ");
            tooltiptext.append(sf[sl].getType());
            nl = true;
          }
          ;
          if (sf[sl].getDescription() != null)
          {
            tooltiptext.append(" ");
            tooltiptext.append(sf[sl].getDescription());
            nl = true;
          }
          ;
          if (sf[sl].getScore() != Float.NaN && sf[sl].getScore() != 0f)
          {
            tooltiptext.append(" Score = ");
            tooltiptext.append(sf[sl].getScore());
            nl = true;
          }
          ;
          if (sf[sl].getStatus() != null && sf[sl].getStatus().length() > 0)
          {
            tooltiptext.append(" (");
            tooltiptext.append(sf[sl].getStatus());
            tooltiptext.append(")");
            nl = true;
          }
          ;
          if (nl)
          {
            tooltiptext.append("\n");
          }
        }
      }
    }
    if (tooltiptext.length() == 0)
    {
      // nothing to display - so clear tooltip if one is visible
      if (tooltip != null)
      {
        tooltip.setVisible(false);
      }
      tooltip = null;
      tooltiptext = null;
      return;
    }
    if (tooltip == null)
    {
      tooltip = new Tooltip(sequence.getDisplayId(true) + "\n"
              + tooltiptext.toString(), idCanvas);
    }
    else
    {
      tooltip.setTip(sequence.getDisplayId(true) + "\n"
              + tooltiptext.toString());
    }
    tooltiptext = null;
  }

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
    alignPanel.paintAlignment(false);
  }

  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() < 2)
    {
      return;
    }

    // DEFAULT LINK IS FIRST IN THE LINK LIST
    int seq = alignPanel.seqPanel.findSeq(e);
    SequenceI sq = av.getAlignment().getSequenceAt(seq);
    if (sq==null)
    {
      return;
    }
    String id = sq.getName();

    String target = null;
    String url = null;
    int i = 0;
    while (url == null && i < links.size())
    {
      // DEFAULT LINK IS FIRST IN THE LINK LIST
      // BUT IF ITS A REGEX AND DOES NOT MATCH THE NEXT ONE WILL BE TRIED
      url = links.elementAt(i++).toString();
      jalview.util.UrlLink urlLink = null;
      try
      {
        urlLink = new UrlLink(url);
        target = urlLink.getTarget();
      } catch (Exception foo)
      {
        System.err.println("Exception for URLLink '" + url + "'");
        foo.printStackTrace();
        url = null;
        continue;
      }
      ;
      if (!urlLink.isValid())
      {
        System.err.println(urlLink.getInvalidMessage());
        url = null;
        continue;
      }

      String urls[] = urlLink.makeUrls(id, true);
      if (urls == null || urls[0] == null || urls[0].length() < 1)
      {
        url = null;
        continue;
      }
      // just take first URL made from regex
      url = urls[1];
    }
    try
    {

      alignPanel.alignFrame.showURL(url, target);
    } catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }

  public void mouseEntered(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }
  }

  public void mouseExited(MouseEvent e)
  {
    if (av.getWrapAlignment())
    {
      return;
    }

    if (mouseDragging && e.getY() < 0 && av.getStartSeq() > 0)
    {
      scrollThread = new ScrollThread(true);
    }

    if (mouseDragging && e.getY() >= getSize().height
            && av.alignment.getHeight() > av.getEndSeq())
    {
      scrollThread = new ScrollThread(false);
    }
  }

  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() > 1)
    {
      return;
    }

    int y = e.getY();
    if (av.getWrapAlignment())
    {
      y -= 2 * av.charHeight;
    }

    int seq = alignPanel.seqPanel.findSeq(e);

    if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK)
    {
      Sequence sq = (Sequence) av.getAlignment().getSequenceAt(seq);
      
      // build a new links menu based on the current links + any non-positional
      // features
      Vector nlinks = new Vector();
      for (int l = 0, lSize = links.size(); l < lSize; l++)
      {
        nlinks.addElement(links.elementAt(l));
      }
      SequenceFeature sf[] = sq==null ? null:sq.getSequenceFeatures();
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

      APopupMenu popup = new APopupMenu(alignPanel, sq, nlinks);
      this.add(popup);
      popup.show(this, e.getX(), e.getY());
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

    if (e.isShiftDown() && lastid != -1)
    {
      selectSeqs(lastid, seq);
    }
    else
    {
      selectSeq(seq);
    }

    alignPanel.paintAlignment(false);
  }

  void selectSeq(int seq)
  {
    lastid = seq;
    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, false);
  }

  void selectSeqs(int start, int end)
  {

    lastid = start;

    if (end >= av.getAlignment().getHeight())
    {
      end = av.getAlignment().getHeight() - 1;
    }

    if (end < start)
    {
      int tmp = start;
      start = end;
      end = tmp;
      lastid = end;
    }
    if (av.getSelectionGroup() == null)
    {
      av.setSelectionGroup(new SequenceGroup());
    }
    for (int i = start; i <= end; i++)
    {
      av.getSelectionGroup().addSequence(
              av.getAlignment().getSequenceAt(i), false);
    }

  }

  public void mouseReleased(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }

    if (av.getSelectionGroup() != null)
    {
      av.getSelectionGroup().recalcConservation();
    }

    mouseDragging = false;
    PaintRefresher.Refresh(this, av.getSequenceSetId());
    // always send selection message when mouse is released
    av.sendSelection();
  }

  public void highlightSearchResults(java.util.Vector found)
  {
    idCanvas.setHighlighted(found);

    if (found == null)
    {
      return;
    }

    int index = av.alignment.findIndex((SequenceI) found.elementAt(0));

    // do we need to scroll the panel?
    if (av.getStartSeq() > index || av.getEndSeq() < index)
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
          else if (seq > lastid && seq < av.alignment.getHeight())
          {
            selectSeqs(lastid + 1, seq);
          }

          lastid = seq;
        }
        else
        {
          running = false;
        }

        alignPanel.paintAlignment(true);
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
