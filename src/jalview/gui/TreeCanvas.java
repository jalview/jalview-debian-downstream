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
package jalview.gui;

import jalview.analysis.Conservation;
import jalview.analysis.TreeModel;
import jalview.api.AlignViewportI;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceGroup;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.schemes.ColourSchemeI;
import jalview.structure.SelectionSource;
import jalview.util.Format;
import jalview.util.MessageManager;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class TreeCanvas extends JPanel implements MouseListener, Runnable,
        Printable, MouseMotionListener, SelectionSource
{
  /** DOCUMENT ME!! */
  public static final String PLACEHOLDER = " * ";

  TreeModel tree;

  JScrollPane scrollPane;

  TreePanel tp;

  private AlignViewport av;

  private AlignmentPanel ap;

  Font font;

  FontMetrics fm;

  boolean fitToWindow = true;

  boolean showDistances = false;

  boolean showBootstrap = false;

  boolean markPlaceholders = false;

  int offx = 20;

  int offy;

  private float threshold;

  String longestName;

  int labelLength = -1;

  Hashtable nameHash = new Hashtable();

  Hashtable nodeHash = new Hashtable();

  SequenceNode highlightNode;

  boolean applyToAllViews = false;

  /**
   * Creates a new TreeCanvas object.
   * 
   * @param av
   *          DOCUMENT ME!
   * @param tree
   *          DOCUMENT ME!
   * @param scroller
   *          DOCUMENT ME!
   * @param label
   *          DOCUMENT ME!
   */
  public TreeCanvas(TreePanel tp, AlignmentPanel ap, JScrollPane scroller)
  {
    this.tp = tp;
    this.av = ap.av;
    this.setAssociatedPanel(ap);
    font = av.getFont();
    scrollPane = scroller;
    addMouseListener(this);
    addMouseMotionListener(this);
    ToolTipManager.sharedInstance().registerComponent(this);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param sequence
   *          DOCUMENT ME!
   */
  public void treeSelectionChanged(SequenceI sequence)
  {
    AlignmentPanel[] aps = getAssociatedPanels();

    for (int a = 0; a < aps.length; a++)
    {
      SequenceGroup selected = aps[a].av.getSelectionGroup();

      if (selected == null)
      {
        selected = new SequenceGroup();
        aps[a].av.setSelectionGroup(selected);
      }

      selected.setEndRes(aps[a].av.getAlignment().getWidth() - 1);
      selected.addOrRemove(sequence, true);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param tree
   *          DOCUMENT ME!
   */
  public void setTree(TreeModel tree)
  {
    this.tree = tree;
    tree.findHeight(tree.getTopNode());

    // Now have to calculate longest name based on the leaves
    Vector<SequenceNode> leaves = tree.findLeaves(tree.getTopNode());
    boolean has_placeholders = false;
    longestName = "";

    for (int i = 0; i < leaves.size(); i++)
    {
      SequenceNode lf = leaves.elementAt(i);

      if (lf.isPlaceholder())
      {
        has_placeholders = true;
      }

      if (longestName.length() < ((Sequence) lf.element()).getName()
              .length())
      {
        longestName = TreeCanvas.PLACEHOLDER
                + ((Sequence) lf.element()).getName();
      }
    }

    setMarkPlaceholders(has_placeholders);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   * @param node
   *          DOCUMENT ME!
   * @param chunk
   *          DOCUMENT ME!
   * @param wscale
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param offx
   *          DOCUMENT ME!
   * @param offy
   *          DOCUMENT ME!
   */
  public void drawNode(Graphics g, SequenceNode node, float chunk,
          double wscale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ((node.left() == null) && (node.right() == null))
    {
      // Drawing leaf node
      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (node.element() instanceof SequenceI)
      {
        SequenceI seq = (SequenceI) node.element();

        if (av.getSequenceColour(seq) == Color.white)
        {
          g.setColor(Color.black);
        }
        else
        {
          g.setColor(av.getSequenceColour(seq).darker());
        }
      }
      else
      {
        g.setColor(Color.black);
      }

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%-.2f").form(node.dist);
      }

      if (showBootstrap && node.bootstrap > -1)
      {
        if (showDistances)
        {
          nodeLabel = nodeLabel + " : ";
        }

        nodeLabel = nodeLabel + String.valueOf(node.bootstrap);
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }

      String name = (markPlaceholders && node.isPlaceholder())
              ? (PLACEHOLDER + node.getName())
              : node.getName();

      int charWidth = fm.stringWidth(name) + 3;
      int charHeight = font.getSize();

      Rectangle rect = new Rectangle(xend + 10, ypos - charHeight / 2,
              charWidth, charHeight);

      nameHash.put(node.element(), rect);

      // Colour selected leaves differently
      SequenceGroup selected = av.getSelectionGroup();

      if ((selected != null)
              && selected.getSequences(null).contains(node.element()))
      {
        g.setColor(Color.gray);

        g.fillRect(xend + 10, ypos - charHeight / 2, charWidth, charHeight);
        g.setColor(Color.white);
      }

      g.drawString(name, xend + 10, ypos + fm.getDescent());
      g.setColor(Color.black);
    }
    else
    {
      drawNode(g, (SequenceNode) node.left(), chunk, wscale, width, offx,
              offy);
      drawNode(g, (SequenceNode) node.right(), chunk, wscale, width, offx,
              offy);

      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;
      int ypos = (int) (node.ycount * chunk) + offy;

      g.setColor(node.color.darker());

      // Draw horizontal line
      g.drawLine(xstart, ypos, xend, ypos);
      if (node == highlightNode)
      {
        g.fillRect(xend - 3, ypos - 3, 6, 6);
      }
      else
      {
        g.fillRect(xend - 2, ypos - 2, 4, 4);
      }

      int ystart = (node.left() == null ? 0
              : (int) (((SequenceNode) node.left()).ycount * chunk)) + offy;
      int yend = (node.right() == null ? 0
              : (int) (((SequenceNode) node.right()).ycount * chunk))
              + offy;

      Rectangle pos = new Rectangle(xend - 2, ypos - 2, 5, 5);
      nodeHash.put(node, pos);

      g.drawLine((int) (height * wscale) + offx, ystart,
              (int) (height * wscale) + offx, yend);

      String nodeLabel = "";

      if (showDistances && (node.dist > 0))
      {
        nodeLabel = new Format("%-.2f").form(node.dist);
      }

      if (showBootstrap && node.bootstrap > -1)
      {
        if (showDistances)
        {
          nodeLabel = nodeLabel + " : ";
        }

        nodeLabel = nodeLabel + String.valueOf(node.bootstrap);
      }

      if (!nodeLabel.equals(""))
      {
        g.drawString(nodeLabel, xstart + 2, ypos - 2);
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param x
   *          DOCUMENT ME!
   * @param y
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Object findElement(int x, int y)
  {
    Enumeration keys = nameHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nameHash.get(ob);

      if ((x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y)
              && (y <= (rect.y + rect.height)))
      {
        return ob;
      }
    }

    keys = nodeHash.keys();

    while (keys.hasMoreElements())
    {
      Object ob = keys.nextElement();
      Rectangle rect = (Rectangle) nodeHash.get(ob);

      if ((x >= rect.x) && (x <= (rect.x + rect.width)) && (y >= rect.y)
              && (y <= (rect.y + rect.height)))
      {
        return ob;
      }
    }

    return null;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pickBox
   *          DOCUMENT ME!
   */
  public void pickNodes(Rectangle pickBox)
  {
    int width = getWidth();
    int height = getHeight();

    SequenceNode top = tree.getTopNode();

    double wscale = ((width * .8) - (offx * 2)) / tree.getMaxHeight();

    if (top.count == 0)
    {
      top.count = ((SequenceNode) top.left()).count
              + ((SequenceNode) top.right()).count;
    }

    float chunk = (float) (height - (offy)) / top.count;

    pickNode(pickBox, top, chunk, wscale, width, offx, offy);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pickBox
   *          DOCUMENT ME!
   * @param node
   *          DOCUMENT ME!
   * @param chunk
   *          DOCUMENT ME!
   * @param wscale
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param offx
   *          DOCUMENT ME!
   * @param offy
   *          DOCUMENT ME!
   */
  public void pickNode(Rectangle pickBox, SequenceNode node, float chunk,
          double wscale, int width, int offx, int offy)
  {
    if (node == null)
    {
      return;
    }

    if ((node.left() == null) && (node.right() == null))
    {
      double height = node.height;
      double dist = node.dist;

      int xstart = (int) ((height - dist) * wscale) + offx;
      int xend = (int) (height * wscale) + offx;

      int ypos = (int) (node.ycount * chunk) + offy;

      if (pickBox.contains(new Point(xend, ypos)))
      {
        if (node.element() instanceof SequenceI)
        {
          SequenceI seq = (SequenceI) node.element();
          SequenceGroup sg = av.getSelectionGroup();

          if (sg != null)
          {
            sg.addOrRemove(seq, true);
          }
        }
      }
    }
    else
    {
      pickNode(pickBox, (SequenceNode) node.left(), chunk, wscale, width,
              offx, offy);
      pickNode(pickBox, (SequenceNode) node.right(), chunk, wscale, width,
              offx, offy);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param node
   *          DOCUMENT ME!
   * @param c
   *          DOCUMENT ME!
   */
  public void setColor(SequenceNode node, Color c)
  {
    if (node == null)
    {
      return;
    }

    node.color = c;
    if (node.element() instanceof SequenceI)
    {
      final SequenceI seq = (SequenceI) node.element();
      AlignmentPanel[] aps = getAssociatedPanels();
      if (aps != null)
      {
        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSequenceColour(seq, c);
        }
      }
    }
    setColor((SequenceNode) node.left(), c);
    setColor((SequenceNode) node.right(), c);
  }

  /**
   * DOCUMENT ME!
   */
  void startPrinting()
  {
    Thread thread = new Thread(this);
    thread.start();
  }

  // put printing in a thread to avoid painting problems
  @Override
  public void run()
  {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    PageFormat defaultPage = printJob.defaultPage();
    PageFormat pf = printJob.pageDialog(defaultPage);

    if (defaultPage == pf)
    {
      /*
       * user cancelled
       */
      return;
    }

    printJob.setPrintable(this, pf);

    if (printJob.printDialog())
    {
      try
      {
        printJob.print();
      } catch (Exception PrintException)
      {
        PrintException.printStackTrace();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param pg
   *          DOCUMENT ME!
   * @param pf
   *          DOCUMENT ME!
   * @param pi
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   * 
   * @throws PrinterException
   *           DOCUMENT ME!
   */
  @Override
  public int print(Graphics pg, PageFormat pf, int pi)
          throws PrinterException
  {
    pg.setFont(font);
    pg.translate((int) pf.getImageableX(), (int) pf.getImageableY());

    int pwidth = (int) pf.getImageableWidth();
    int pheight = (int) pf.getImageableHeight();

    int noPages = getHeight() / pheight;

    if (pi > noPages)
    {
      return Printable.NO_SUCH_PAGE;
    }

    if (pwidth > getWidth())
    {
      pwidth = getWidth();
    }

    if (fitToWindow)
    {
      if (pheight > getHeight())
      {
        pheight = getHeight();
      }

      noPages = 0;
    }
    else
    {
      FontMetrics fm = pg.getFontMetrics(font);
      int height = fm.getHeight() * nameHash.size();
      pg.translate(0, -pi * pheight);
      pg.setClip(0, pi * pheight, pwidth, (pi * pheight) + pheight);

      // translate number of pages,
      // height is screen size as this is the
      // non overlapping text size
      pheight = height;
    }

    draw(pg, pwidth, pheight);

    return Printable.PAGE_EXISTS;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g
   *          DOCUMENT ME!
   */
  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    g.setFont(font);

    if (tree == null)
    {
      g.drawString(
              MessageManager.getString("label.calculating_tree") + "....",
              20, getHeight() / 2);
    }
    else
    {
      fm = g.getFontMetrics(font);

      if (nameHash.size() == 0)
      {
        repaint();
      }

      if (fitToWindow || (!fitToWindow && (scrollPane
              .getHeight() > ((fm.getHeight() * nameHash.size()) + offy))))
      {
        draw(g, scrollPane.getWidth(), scrollPane.getHeight());
        setPreferredSize(null);
      }
      else
      {
        setPreferredSize(new Dimension(scrollPane.getWidth(),
                fm.getHeight() * nameHash.size()));
        draw(g, scrollPane.getWidth(), fm.getHeight() * nameHash.size());
      }

      scrollPane.revalidate();
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param fontSize
   *          DOCUMENT ME!
   */
  @Override
  public void setFont(Font font)
  {
    this.font = font;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param g1
   *          DOCUMENT ME!
   * @param width
   *          DOCUMENT ME!
   * @param height
   *          DOCUMENT ME!
   */
  public void draw(Graphics g1, int width, int height)
  {
    Graphics2D g2 = (Graphics2D) g1;
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(Color.white);
    g2.fillRect(0, 0, width, height);
    g2.setFont(font);

    if (longestName == null || tree == null)
    {
      g2.drawString("Calculating tree.", 20, 20);
    }
    offy = font.getSize() + 10;

    fm = g2.getFontMetrics(font);

    labelLength = fm.stringWidth(longestName) + 20; // 20 allows for scrollbar

    double wscale = (width - labelLength - (offx * 2))
            / tree.getMaxHeight();

    SequenceNode top = tree.getTopNode();

    if (top.count == 0)
    {
      top.count = ((SequenceNode) top.left()).count
              + ((SequenceNode) top.right()).count;
    }

    float chunk = (float) (height - (offy)) / top.count;

    drawNode(g2, tree.getTopNode(), chunk, wscale, width, offx, offy);

    if (threshold != 0)
    {
      if (av.getCurrentTree() == tree)
      {
        g2.setColor(Color.red);
      }
      else
      {
        g2.setColor(Color.gray);
      }

      int x = (int) ((threshold * (getWidth() - labelLength - (2 * offx)))
              + offx);

      g2.drawLine(x, 0, x, getHeight());
    }
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseReleased(MouseEvent e)
  {
    /*
     * isPopupTrigger is set on mouseReleased on Windows
     */
    if (e.isPopupTrigger())
    {
      chooseSubtreeColour();
      e.consume(); // prevent mouseClicked happening
    }
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseEntered(MouseEvent e)
  {
  }

  /**
   * Empty method to satisfy the MouseListener interface
   * 
   * @param e
   */
  @Override
  public void mouseExited(MouseEvent e)
  {
  }

  /**
   * Handles a mouse click on a tree node (clicks elsewhere are handled in
   * mousePressed). Click selects the sub-tree, double-click swaps leaf nodes
   * order, right-click opens a dialogue to choose colour for the sub-tree.
   * 
   * @param e
   */
  @Override
  public void mouseClicked(MouseEvent evt)
  {
    if (highlightNode == null)
    {
      return;
    }

    if (evt.getClickCount() > 1)
    {
      tree.swapNodes(highlightNode);
      tree.reCount(tree.getTopNode());
      tree.findHeight(tree.getTopNode());
    }
    else
    {
      Vector<SequenceNode> leaves = tree.findLeaves(highlightNode);

      for (int i = 0; i < leaves.size(); i++)
      {
        SequenceI seq = (SequenceI) leaves.elementAt(i).element();
        treeSelectionChanged(seq);
      }
      av.sendSelection();
    }

    PaintRefresher.Refresh(tp, av.getSequenceSetId());
    repaint();
  }

  /**
   * Offer the user the option to choose a colour for the highlighted node and
   * its children; this colour is also applied to the corresponding sequence ids
   * in the alignment
   */
  void chooseSubtreeColour()
  {
    Color col = JColorChooser.showDialog(this,
            MessageManager.getString("label.select_subtree_colour"),
            highlightNode.color);
    if (col != null)
    {
      setColor(highlightNode, col);
      PaintRefresher.Refresh(tp, getAssociatedPanel().av.getSequenceSetId());
      repaint();
    }
  }

  @Override
  public void mouseMoved(MouseEvent evt)
  {
    av.setCurrentTree(tree);

    Object ob = findElement(evt.getX(), evt.getY());

    if (ob instanceof SequenceNode)
    {
      highlightNode = (SequenceNode) ob;
      this.setToolTipText(
              "<html>" + MessageManager.getString("label.highlightnode"));
      repaint();

    }
    else
    {
      if (highlightNode != null)
      {
        highlightNode = null;
        setToolTipText(null);
        repaint();
      }
    }
  }

  @Override
  public void mouseDragged(MouseEvent ect)
  {
  }

  /**
   * Handles a mouse press on a sequence name or the tree background canvas
   * (click on a node is handled in mouseClicked). The action is to create
   * groups by partitioning the tree at the mouse position. Colours for the
   * groups (and sequence names) are generated randomly.
   * 
   * @param e
   */
  @Override
  public void mousePressed(MouseEvent e)
  {
    av.setCurrentTree(tree);

    /*
     * isPopupTrigger is set for mousePressed (Mac)
     * or mouseReleased (Windows)
     */
    if (e.isPopupTrigger())
    {
      if (highlightNode != null)
      {
        chooseSubtreeColour();
      }
      return;
    }

    /*
     * defer right-click handling on Windows to
     * mouseClicked; note isRightMouseButton
     * also matches Cmd-click on Mac which should do
     * nothing here
     */
    if (SwingUtilities.isRightMouseButton(e))
    {
      return;
    }

    int x = e.getX();
    int y = e.getY();

    Object ob = findElement(x, y);

    if (ob instanceof SequenceI)
    {
      treeSelectionChanged((Sequence) ob);
      PaintRefresher.Refresh(tp, getAssociatedPanel().av.getSequenceSetId());
      repaint();
      av.sendSelection();
      return;
    }
    else if (!(ob instanceof SequenceNode))
    {
      // Find threshold
      if (tree.getMaxHeight() != 0)
      {
        threshold = (float) (x - offx)
                / (float) (getWidth() - labelLength - (2 * offx));

        List<SequenceNode> groups = tree.groupNodes(threshold);
        setColor(tree.getTopNode(), Color.black);

        AlignmentPanel[] aps = getAssociatedPanels();

        // TODO push calls below into a single AlignViewportI method?
        // see also AlignViewController.deleteGroups
        for (int a = 0; a < aps.length; a++)
        {
          aps[a].av.setSelectionGroup(null);
          aps[a].av.getAlignment().deleteAllGroups();
          aps[a].av.clearSequenceColours();
          if (aps[a].av.getCodingComplement() != null)
          {
            aps[a].av.getCodingComplement().setSelectionGroup(null);
            aps[a].av.getCodingComplement().getAlignment()
                    .deleteAllGroups();
            aps[a].av.getCodingComplement().clearSequenceColours();
          }
          aps[a].av.setUpdateStructures(true);
        }
        colourGroups(groups);

        /*
         * clear partition (don't show vertical line) if
         * it is to the right of all nodes
         */
        if (groups.isEmpty())
        {
          threshold = 0f;
        }
      }

      PaintRefresher.Refresh(tp, getAssociatedPanel().av.getSequenceSetId());
      repaint();
    }

  }

  void colourGroups(List<SequenceNode> groups)
  {
    AlignmentPanel[] aps = getAssociatedPanels();
    for (int i = 0; i < groups.size(); i++)
    {
      Color col = new Color((int) (Math.random() * 255),
              (int) (Math.random() * 255), (int) (Math.random() * 255));
      setColor(groups.get(i), col.brighter());

      Vector<SequenceNode> l = tree.findLeaves(groups.get(i));

      Vector<SequenceI> sequences = new Vector<>();

      for (int j = 0; j < l.size(); j++)
      {
        SequenceI s1 = (SequenceI) l.elementAt(j).element();

        if (!sequences.contains(s1))
        {
          sequences.addElement(s1);
        }
      }

      ColourSchemeI cs = null;
      SequenceGroup _sg = new SequenceGroup(sequences, null, cs, true, true,
              false, 0, av.getAlignment().getWidth() - 1);

      _sg.setName("JTreeGroup:" + _sg.hashCode());
      _sg.setIdColour(col);

      for (int a = 0; a < aps.length; a++)
      {
        SequenceGroup sg = new SequenceGroup(_sg);
        AlignViewport viewport = aps[a].av;

        // Propagate group colours in each view
        if (viewport.getGlobalColourScheme() != null)
        {
          cs = viewport.getGlobalColourScheme().getInstance(viewport, sg);
          sg.setColourScheme(cs);
          sg.getGroupColourScheme().setThreshold(
                  viewport.getResidueShading().getThreshold(),
                  viewport.isIgnoreGapsConsensus());

          if (viewport.getResidueShading().conservationApplied())
          {
            Conservation c = new Conservation("Group",
                    sg.getSequences(null), sg.getStartRes(),
                    sg.getEndRes());
            c.calculate();
            c.verdict(false, viewport.getConsPercGaps());
            sg.cs.setConservation(c);
          }
        }
        // indicate that associated structure views will need an update
        viewport.setUpdateStructures(true);
        // propagate structure view update and sequence group to complement view
        viewport.addSequenceGroup(sg);
      }
    }

    // notify the panel(s) to redo any group specific stuff
    // also updates structure views if necessary
    for (int a = 0; a < aps.length; a++)
    {
      aps[a].updateAnnotation();
      final AlignViewportI codingComplement = aps[a].av
              .getCodingComplement();
      if (codingComplement != null)
      {
        ((AlignViewport) codingComplement).getAlignPanel()
                .updateAnnotation();
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowDistances(boolean state)
  {
    this.showDistances = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setShowBootstrap(boolean state)
  {
    this.showBootstrap = state;
    repaint();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param state
   *          DOCUMENT ME!
   */
  public void setMarkPlaceholders(boolean state)
  {
    this.markPlaceholders = state;
    repaint();
  }

  AlignmentPanel[] getAssociatedPanels()
  {
    if (applyToAllViews)
    {
      return PaintRefresher.getAssociatedPanels(av.getSequenceSetId());
    }
    else
    {
      return new AlignmentPanel[] { getAssociatedPanel() };
    }
  }

  public AlignmentPanel getAssociatedPanel()
  {
    return ap;
  }

  public void setAssociatedPanel(AlignmentPanel ap)
  {
    this.ap = ap;
  }

  public AlignViewport getViewport()
  {
    return av;
  }

  public void setViewport(AlignViewport av)
  {
    this.av = av;
  }

  public float getThreshold()
  {
    return threshold;
  }

  public void setThreshold(float threshold)
  {
    this.threshold = threshold;
  }

  public boolean isApplyToAllViews()
  {
    return this.applyToAllViews;
  }

  public void setApplyToAllViews(boolean applyToAllViews)
  {
    this.applyToAllViews = applyToAllViews;
  }
}
