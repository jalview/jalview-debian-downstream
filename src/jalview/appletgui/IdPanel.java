/*
 * Jalview - A Sequence Alignment Editor and Viewer (2.10.1)
 * Copyright (C) 2016 The Jalview Authors
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
package jalview.appletgui;

import static jalview.util.UrlConstants.EMBLEBI_STRING;
import static jalview.util.UrlConstants.SRS_STRING;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.util.UrlLink;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.Panel;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;
import java.util.Vector;

public class IdPanel extends Panel implements MouseListener,
        MouseMotionListener
{

  protected IdCanvas idCanvas;

  protected AlignmentViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

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
    {
      // upgrade old SRS link
      int srsPos = links.indexOf(SRS_STRING);
      if (srsPos > -1)
      {
        links.setElementAt(EMBLEBI_STRING, srsPos);
      }
    }
    if (links.size() < 1)
    {
      links = new java.util.Vector();
      links.addElement(EMBLEBI_STRING);
    }
  }

  Tooltip tooltip;

  @Override
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
          if (!Float.isNaN(sf[sl].getScore()) && sf[sl].getScore() != 0f)
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

  @Override
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

  @Override
  public void mouseClicked(MouseEvent e)
  {
    if (e.getClickCount() < 2)
    {
      return;
    }

    // DEFAULT LINK IS FIRST IN THE LINK LIST
    int seq = alignPanel.seqPanel.findSeq(e);
    SequenceI sq = av.getAlignment().getSequenceAt(seq);
    if (sq == null)
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

      if (urlLink.usesDBAccession())
      {
        // this URL requires an accession id, not the name of a sequence
        url = null;
        continue;
      }

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

  @Override
  public void mouseEntered(MouseEvent e)
  {
    if (scrollThread != null)
    {
      scrollThread.running = false;
    }
  }

  @Override
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
            && av.getAlignment().getHeight() > av.getEndSeq())
    {
      scrollThread = new ScrollThread(false);
    }
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() > 1)
    {
      return;
    }

    int y = e.getY();
    if (av.getWrapAlignment())
    {
      y -= 2 * av.getCharHeight();
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
      SequenceFeature sf[] = sq == null ? null : sq.getSequenceFeatures();
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
            || ((!jalview.util.Platform.isControlDown(e) && !e
                    .isShiftDown()) && av.getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);
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
    av.getSelectionGroup().addOrRemove(pickedSeq, true);
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
              av.getAlignment().getSequenceAt(i), i == end);
    }

  }

  @Override
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

  public void highlightSearchResults(List<SequenceI> list)
  {
    idCanvas.setHighlighted(list);

    if (list == null)
    {
      return;
    }

    int index = av.getAlignment().findIndex(list.get(0));

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

    @Override
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
          else if (seq > lastid && seq < av.getAlignment().getHeight())
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
