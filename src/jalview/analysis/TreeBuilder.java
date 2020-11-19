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
package jalview.analysis;

import jalview.api.analysis.ScoreModelI;
import jalview.api.analysis.SimilarityParamsI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.CigarArray;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.math.MatrixI;
import jalview.viewmodel.AlignmentViewport;

import java.util.BitSet;
import java.util.Vector;

public abstract class TreeBuilder
{
  public static final String AVERAGE_DISTANCE = "AV";

  public static final String NEIGHBOUR_JOINING = "NJ";

  protected Vector<BitSet> clusters;

  protected SequenceI[] sequences;

  public AlignmentView seqData;

  protected BitSet done;

  protected int noseqs;

  int noClus;

  protected MatrixI distances;

  protected int mini;

  protected int minj;

  protected double ri;

  protected double rj;

  SequenceNode maxdist;

  SequenceNode top;

  double maxDistValue;

  double maxheight;

  int ycount;

  Vector<SequenceNode> node;

  private AlignmentView seqStrings;

  /**
   * Constructor
   * 
   * @param av
   * @param sm
   * @param scoreParameters
   */
  public TreeBuilder(AlignmentViewport av, ScoreModelI sm,
          SimilarityParamsI scoreParameters)
  {
    int start, end;
    boolean selview = av.getSelectionGroup() != null
            && av.getSelectionGroup().getSize() > 1;
    seqStrings = av.getAlignmentView(selview);
    if (!selview)
    {
      start = 0;
      end = av.getAlignment().getWidth();
      this.sequences = av.getAlignment().getSequencesArray();
    }
    else
    {
      start = av.getSelectionGroup().getStartRes();
      end = av.getSelectionGroup().getEndRes() + 1;
      this.sequences = av.getSelectionGroup()
              .getSequencesInOrder(av.getAlignment());
    }

    init(seqStrings, start, end);

    computeTree(sm, scoreParameters);
  }

  public SequenceI[] getSequences()
  {
    return sequences;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  double findHeight(SequenceNode nd)
  {
    if (nd == null)
    {
      return maxheight;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      nd.height = ((SequenceNode) nd.parent()).height + nd.dist;

      if (nd.height > maxheight)
      {
        return nd.height;
      }
      else
      {
        return maxheight;
      }
    }
    else
    {
      if (nd.parent() != null)
      {
        nd.height = ((SequenceNode) nd.parent()).height + nd.dist;
      }
      else
      {
        maxheight = 0;
        nd.height = (float) 0.0;
      }

      maxheight = findHeight((SequenceNode) (nd.left()));
      maxheight = findHeight((SequenceNode) (nd.right()));
    }

    return maxheight;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  void reCount(SequenceNode nd)
  {
    ycount = 0;
    // _lycount = 0;
    // _lylimit = this.node.size();
    _reCount(nd);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  void _reCount(SequenceNode nd)
  {
    // if (_lycount<_lylimit)
    // {
    // System.err.println("Warning: depth of _recount greater than number of
    // nodes.");
    // }
    if (nd == null)
    {
      return;
    }
    // _lycount++;

    if ((nd.left() != null) && (nd.right() != null))
    {

      _reCount((SequenceNode) nd.left());
      _reCount((SequenceNode) nd.right());

      SequenceNode l = (SequenceNode) nd.left();
      SequenceNode r = (SequenceNode) nd.right();

      nd.count = l.count + r.count;
      nd.ycount = (l.ycount + r.ycount) / 2;
    }
    else
    {
      nd.count = 1;
      nd.ycount = ycount++;
    }
    // _lycount--;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceNode getTopNode()
  {
    return top;
  }

  /**
   * 
   * @return true if tree has real distances
   */
  public boolean hasDistances()
  {
    return true;
  }

  /**
   * 
   * @return true if tree has real bootstrap values
   */
  public boolean hasBootstrap()
  {
    return false;
  }

  public boolean hasRootDistance()
  {
    return true;
  }

  /**
   * Form clusters by grouping sub-clusters, starting from one sequence per
   * cluster, and finishing when only two clusters remain
   */
  void cluster()
  {
    while (noClus > 2)
    {
      findMinDistance();

      joinClusters(mini, minj);

      noClus--;
    }

    int rightChild = done.nextClearBit(0);
    int leftChild = done.nextClearBit(rightChild + 1);

    joinClusters(leftChild, rightChild);
    top = (node.elementAt(leftChild));

    reCount(top);
    findHeight(top);
    findMaxDist(top);
  }

  /**
   * Returns the minimum distance between two clusters, and also sets the
   * indices of the clusters in fields mini and minj
   * 
   * @return
   */
  protected abstract double findMinDistance();

  /**
   * Calculates the tree using the given score model and parameters, and the
   * configured tree type
   * <p>
   * If the score model computes pairwise distance scores, then these are used
   * directly to derive the tree
   * <p>
   * If the score model computes similarity scores, then the range of the scores
   * is reversed to give a distance measure, and this is used to derive the tree
   * 
   * @param sm
   * @param scoreOptions
   */
  protected void computeTree(ScoreModelI sm, SimilarityParamsI scoreOptions)
  {
    distances = sm.findDistances(seqData, scoreOptions);

    makeLeaves();

    noClus = clusters.size();

    cluster();
  }

  /**
   * Finds the node, at or below the given node, with the maximum distance, and
   * saves the node and the distance value
   * 
   * @param nd
   */
  void findMaxDist(SequenceNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      double dist = nd.dist;

      if (dist > maxDistValue)
      {
        maxdist = nd;
        maxDistValue = dist;
      }
    }
    else
    {
      findMaxDist((SequenceNode) nd.left());
      findMaxDist((SequenceNode) nd.right());
    }
  }

  /**
   * Calculates and returns r, whatever that is
   * 
   * @param i
   * @param j
   * 
   * @return
   */
  protected double findr(int i, int j)
  {
    double tmp = 1;

    for (int k = 0; k < noseqs; k++)
    {
      if ((k != i) && (k != j) && (!done.get(k)))
      {
        tmp = tmp + distances.getValue(i, k);
      }
    }

    if (noClus > 2)
    {
      tmp = tmp / (noClus - 2);
    }

    return tmp;
  }

  protected void init(AlignmentView seqView, int start, int end)
  {
    this.node = new Vector<SequenceNode>();
    if (seqView != null)
    {
      this.seqData = seqView;
    }
    else
    {
      SeqCigar[] seqs = new SeqCigar[sequences.length];
      for (int i = 0; i < sequences.length; i++)
      {
        seqs[i] = new SeqCigar(sequences[i], start, end);
      }
      CigarArray sdata = new CigarArray(seqs);
      sdata.addOperation(CigarArray.M, end - start + 1);
      this.seqData = new AlignmentView(sdata, start);
    }

    /*
     * count the non-null sequences
     */
    noseqs = 0;

    done = new BitSet();

    for (SequenceI seq : sequences)
    {
      if (seq != null)
      {
        noseqs++;
      }
    }
  }

  /**
   * Merges cluster(j) to cluster(i) and recalculates cluster and node distances
   * 
   * @param i
   * @param j
   */
  void joinClusters(final int i, final int j)
  {
    double dist = distances.getValue(i, j);

    ri = findr(i, j);
    rj = findr(j, i);

    findClusterDistance(i, j);

    SequenceNode sn = new SequenceNode();

    sn.setLeft((node.elementAt(i)));
    sn.setRight((node.elementAt(j)));

    SequenceNode tmpi = (node.elementAt(i));
    SequenceNode tmpj = (node.elementAt(j));

    findNewDistances(tmpi, tmpj, dist);

    tmpi.setParent(sn);
    tmpj.setParent(sn);

    node.setElementAt(sn, i);

    /*
     * move the members of cluster(j) to cluster(i)
     * and mark cluster j as out of the game
     */
    clusters.get(i).or(clusters.get(j));
    clusters.get(j).clear();
    done.set(j);
  }

  /*
   * Computes and stores new distances for nodei and nodej, given the previous
   * distance between them
   */
  protected abstract void findNewDistances(SequenceNode nodei,
          SequenceNode nodej, double previousDistance);

  /**
   * Calculates and saves the distance between the combination of cluster(i) and
   * cluster(j) and all other clusters. The form of the calculation depends on
   * the tree clustering method being used.
   * 
   * @param i
   * @param j
   */
  protected abstract void findClusterDistance(int i, int j);

  /**
   * Start by making a cluster for each individual sequence
   */
  void makeLeaves()
  {
    clusters = new Vector<BitSet>();

    for (int i = 0; i < noseqs; i++)
    {
      SequenceNode sn = new SequenceNode();

      sn.setElement(sequences[i]);
      sn.setName(sequences[i].getName());
      node.addElement(sn);
      BitSet bs = new BitSet();
      bs.set(i);
      clusters.addElement(bs);
    }
  }

  public AlignmentView getOriginalData()
  {
    return seqStrings;
  }

}
