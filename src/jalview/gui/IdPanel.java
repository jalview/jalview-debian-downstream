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
package jalview.gui;

import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceFeature;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.io.SequenceAnnotationReport;
import jalview.util.MessageManager;
import jalview.util.Platform;
import jalview.util.UrlLink;
import jalview.viewmodel.AlignmentViewport;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * This panel hosts alignment sequence ids and responds to mouse clicks on them,
 * as well as highlighting ids matched by a search from the Find menu.
 * 
 * @author $author$
 * @version $Revision$
 */
public class IdPanel extends JPanel implements MouseListener,
        MouseMotionListener, MouseWheelListener
{
  private IdCanvas idCanvas;

  protected AlignmentViewport av;

  protected AlignmentPanel alignPanel;

  ScrollThread scrollThread = null;

  String linkImageURL;

  int offy;

  // int width;
  int lastid = -1;

  boolean mouseDragging = false;

  private final SequenceAnnotationReport seqAnnotReport;

  /**
   * Creates a new IdPanel object.
   * 
   * @param av
   * @param parent
   */
  public IdPanel(AlignViewport av, AlignmentPanel parent)
  {
    this.av = av;
    alignPanel = parent;
    setIdCanvas(new IdCanvas(av));
    linkImageURL = getClass().getResource("/images/link.gif").toString();
    seqAnnotReport = new SequenceAnnotationReport(linkImageURL);
    setLayout(new BorderLayout());
    add(getIdCanvas(), BorderLayout.CENTER);
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * Respond to mouse movement by constructing tooltip text for the sequence id
   * under the mouse.
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
  public void mouseMoved(MouseEvent e)
  {
    SeqPanel sp = alignPanel.getSeqPanel();
    int seq = Math.max(0, sp.findSeq(e));
    if (seq > -1 && seq < av.getAlignment().getHeight())
    {
      SequenceI sequence = av.getAlignment().getSequenceAt(seq);
      StringBuilder tip = new StringBuilder(64);
      seqAnnotReport.createTooltipAnnotationReport(tip, sequence,
              av.isShowDBRefs(), av.isShowNPFeats(),
              sp.seqCanvas.fr.getMinMax());
      setToolTipText(JvSwingUtils.wrapTooltip(true,
              sequence.getDisplayId(true) + " " + tip.toString()));
    }
  }

  /**
   * Responds to a mouse drag by selecting the sequences under the dragged
   * region.
   * 
   * @param e
   */
  @Override
  public void mouseDragged(MouseEvent e)
  {
    mouseDragging = true;

    int seq = Math.max(0, alignPanel.getSeqPanel().findSeq(e));

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

  /**
   * Response to the mouse wheel by scrolling the alignment panel.
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e)
  {
    e.consume();
    if (e.getWheelRotation() > 0)
    {
      if (e.isShiftDown())
      {
        alignPanel.scrollRight(true);
      }
      else
      {
        alignPanel.scrollUp(false);
      }
    }
    else
    {
      if (e.isShiftDown())
      {
        alignPanel.scrollRight(false);
      }
      else
      {
        alignPanel.scrollUp(true);
      }
    }
  }

  /**
   * Handle a mouse click event. Currently only responds to a double-click. The
   * action is to try to open a browser window at a URL that searches for the
   * selected sequence id. The search URL is configured in Preferences |
   * Connections | URL link from Sequence ID. For example:
   * 
   * http://www.ebi.ac.uk/ebisearch/search.ebi?db=allebi&query=$SEQUENCE_ID$
   * 
   * @param e
   */
  @Override
  public void mouseClicked(MouseEvent e)
  {
    /*
     * Ignore single click. Ignore 'left' click followed by 'right' click (user
     * selects a row then its pop-up menu).
     */
    if (e.getClickCount() < 2 || SwingUtilities.isRightMouseButton(e))
    {
      // reinstate isRightMouseButton check to ignore mouse-related popup events
      // note - this does nothing on default MacBookPro force-trackpad config!
      return;
    }

    Vector links = Preferences.sequenceURLLinks;
    if (links == null || links.size() < 1)
    {
      return;
    }

    int seq = alignPanel.getSeqPanel().findSeq(e);
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

      if (urlLink.usesDBAccession())
      {
        // this URL requires an accession id, not the name of a sequence
        url = null;
        continue;
      }

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
      JOptionPane.showInternalMessageDialog(Desktop.desktop,
              MessageManager.getString("label.web_browser_not_found_unix"),
              MessageManager.getString("label.web_browser_not_found"),
              JOptionPane.WARNING_MESSAGE);
      ex.printStackTrace();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param e
   *          DOCUMENT ME!
   */
  @Override
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
  @Override
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
            && (av.getAlignment().getHeight() > av.getEndSeq()))
    {
      scrollThread = new ScrollThread(false);
    }
  }

  /**
   * Respond to a mouse press. Does nothing for (left) double-click as this is
   * handled by mouseClicked().
   * 
   * Right mouse down - construct and show context menu.
   * 
   * Ctrl-down or Shift-down - add to or expand current selection group if there
   * is one.
   * 
   * Mouse down - select this sequence.
   * 
   * @param e
   */
  @Override
  public void mousePressed(MouseEvent e)
  {
    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e))
    {
      return;
    }

    if (e.isPopupTrigger()) // Mac reports this in mousePressed
    {
      showPopupMenu(e);
      return;
    }

    /*
     * defer right-mouse click handling to mouseReleased on Windows
     * (where isPopupTrigger() will answer true)
     * NB isRightMouseButton is also true for Cmd-click on Mac
     */
    if (SwingUtilities.isRightMouseButton(e) && !Platform.isAMac())
    {
      return;
    }

    if ((av.getSelectionGroup() == null)
            || (!jalview.util.Platform.isControlDown(e) && !e.isShiftDown() && av
                    .getSelectionGroup() != null))
    {
      av.setSelectionGroup(new SequenceGroup());
      av.getSelectionGroup().setStartRes(0);
      av.getSelectionGroup().setEndRes(av.getAlignment().getWidth() - 1);
    }

    int seq = alignPanel.getSeqPanel().findSeq(e);
    if (e.isShiftDown() && (lastid != -1))
    {
      selectSeqs(lastid, seq);
    }
    else
    {
      selectSeq(seq);
    }

    av.isSelectionGroupChanged(true);

    alignPanel.paintAlignment(true);
  }

  /**
   * Build and show the popup-menu at the right-click mouse position
   * 
   * @param e
   */
  void showPopupMenu(MouseEvent e)
  {
    int seq2 = alignPanel.getSeqPanel().findSeq(e);
    Sequence sq = (Sequence) av.getAlignment().getSequenceAt(seq2);
    // build a new links menu based on the current links + any non-positional
    // features
    Vector<String> nlinks = new Vector<String>(Preferences.sequenceURLLinks);
    SequenceFeature sfs[] = sq == null ? null : sq.getSequenceFeatures();
    if (sfs != null)
    {
      for (SequenceFeature sf : sfs)
      {
        if (sf.begin == sf.end && sf.begin == 0)
        {
          if (sf.links != null && sf.links.size() > 0)
          {
            for (int l = 0, lSize = sf.links.size(); l < lSize; l++)
            {
              nlinks.addElement(sf.links.elementAt(l));
            }
          }
        }
      }
    }

    PopupMenu pop = new PopupMenu(alignPanel, sq, nlinks,
            Preferences.getGroupURLLinks());
    pop.show(this, e.getX(), e.getY());
  }

  /**
   * Toggle whether the sequence is part of the current selection group.
   * 
   * @param seq
   */
  void selectSeq(int seq)
  {
    lastid = seq;

    SequenceI pickedSeq = av.getAlignment().getSequenceAt(seq);
    av.getSelectionGroup().addOrRemove(pickedSeq, true);
  }

  /**
   * Add contiguous rows of the alignment to the current selection group. Does
   * nothing if there is no selection group.
   * 
   * @param start
   * @param end
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
              av.getAlignment().getSequenceAt(i), i == end);
    }
  }

  /**
   * Respond to mouse released. Refreshes the display and triggers broadcast of
   * the new selection group to any listeners.
   * 
   * @param e
   */
  @Override
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

    if (e.isPopupTrigger()) // Windows reports this in mouseReleased
    {
      showPopupMenu(e);
    }
  }

  /**
   * Highlight sequence ids that match the given list, and if necessary scroll
   * to the start sequence of the list.
   * 
   * @param list
   */
  public void highlightSearchResults(List<SequenceI> list)
  {
    getIdCanvas().setHighlighted(list);

    if (list == null)
    {
      return;
    }

    int index = av.getAlignment().findIndex(list.get(0));

    // do we need to scroll the panel?
    if ((av.getStartSeq() > index) || (av.getEndSeq() < index))
    {
      alignPanel.setScrollValues(av.getStartRes(), index);
    }
  }

  public IdCanvas getIdCanvas()
  {
    return idCanvas;
  }

  public void setIdCanvas(IdCanvas idCanvas)
  {
    this.idCanvas = idCanvas;
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
