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
package jalview.analysis;

import jalview.api.analysis.ScoreModelI;
import jalview.datamodel.AlignmentView;
import jalview.datamodel.BinaryNode;
import jalview.datamodel.CigarArray;
import jalview.datamodel.NodeTransformI;
import jalview.datamodel.SeqCigar;
import jalview.datamodel.Sequence;
import jalview.datamodel.SequenceI;
import jalview.datamodel.SequenceNode;
import jalview.io.NewickFile;
import jalview.schemes.ResidueProperties;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
public class NJTree
{
  Vector<Cluster> cluster;

  SequenceI[] sequence;

  // SequenceData is a string representation of what the user
  // sees. The display may contain hidden columns.
  public AlignmentView seqData = null;

  int[] done;

  int noseqs;

  int noClus;

  float[][] distance;

  int mini;

  int minj;

  float ri;

  float rj;

  Vector<SequenceNode> groups = new Vector<SequenceNode>();

  SequenceNode maxdist;

  SequenceNode top;

  float maxDistValue;

  float maxheight;

  int ycount;

  Vector<SequenceNode> node;

  String type;

  String pwtype;

  Object found = null;

  boolean hasDistances = true; // normal case for jalview trees

  boolean hasBootstrap = false; // normal case for jalview trees

  private boolean hasRootDistance = true;

  /**
   * Create a new NJTree object with leaves associated with sequences in seqs,
   * and original alignment data represented by Cigar strings.
   * 
   * @param seqs
   *          SequenceI[]
   * @param odata
   *          Cigar[]
   * @param treefile
   *          NewickFile
   */
  public NJTree(SequenceI[] seqs, AlignmentView odata, NewickFile treefile)
  {
    this(seqs, treefile);
    if (odata != null)
    {
      seqData = odata;
    }
    /*
     * sequenceString = new String[odata.length]; char gapChar =
     * jalview.util.Comparison.GapChars.charAt(0); for (int i = 0; i <
     * odata.length; i++) { SequenceI oseq_aligned = odata[i].getSeq(gapChar);
     * sequenceString[i] = oseq_aligned.getSequence(); }
     */
  }

  /**
   * Creates a new NJTree object from a tree from an external source
   * 
   * @param seqs
   *          SequenceI which should be associated with leafs of treefile
   * @param treefile
   *          A parsed tree
   */
  public NJTree(SequenceI[] seqs, NewickFile treefile)
  {
    this.sequence = seqs;
    top = treefile.getTree();

    /**
     * There is no dependent alignment to be recovered from an imported tree.
     * 
     * if (sequenceString == null) { sequenceString = new String[seqs.length];
     * for (int i = 0; i < seqs.length; i++) { sequenceString[i] =
     * seqs[i].getSequence(); } }
     */

    hasDistances = treefile.HasDistances();
    hasBootstrap = treefile.HasBootstrap();
    hasRootDistance = treefile.HasRootDistance();

    maxheight = findHeight(top);

    SequenceIdMatcher algnIds = new SequenceIdMatcher(seqs);

    Vector<SequenceNode> leaves = findLeaves(top);

    int i = 0;
    int namesleft = seqs.length;

    SequenceNode j;
    SequenceI nam;
    String realnam;
    Vector<SequenceI> one2many = new Vector<SequenceI>();
    int countOne2Many = 0;
    while (i < leaves.size())
    {
      j = leaves.elementAt(i++);
      realnam = j.getName();
      nam = null;

      if (namesleft > -1)
      {
        nam = algnIds.findIdMatch(realnam);
      }

      if (nam != null)
      {
        j.setElement(nam);
        if (one2many.contains(nam))
        {
          countOne2Many++;
          // if (jalview.bin.Cache.log.isDebugEnabled())
          // jalview.bin.Cache.log.debug("One 2 many relationship for
          // "+nam.getName());
        }
        else
        {
          one2many.addElement(nam);
          namesleft--;
        }
      }
      else
      {
        j.setElement(new Sequence(realnam, "THISISAPLACEHLDER"));
        j.setPlaceholder(true);
      }
    }
    // if (jalview.bin.Cache.log.isDebugEnabled() && countOne2Many>0) {
    // jalview.bin.Cache.log.debug("There were "+countOne2Many+" alignment
    // sequence ids (out of "+one2many.size()+" unique ids) linked to two or
    // more leaves.");
    // }
    // one2many.clear();
  }

  /**
   * Creates a new NJTree object.
   * 
   * @param sequence
   *          DOCUMENT ME!
   * @param type
   *          DOCUMENT ME!
   * @param pwtype
   *          DOCUMENT ME!
   * @param start
   *          DOCUMENT ME!
   * @param end
   *          DOCUMENT ME!
   */
  public NJTree(SequenceI[] sequence, AlignmentView seqData, String type,
          String pwtype, ScoreModelI sm, int start, int end)
  {
    this.sequence = sequence;
    this.node = new Vector<SequenceNode>();
    this.type = type;
    this.pwtype = pwtype;
    if (seqData != null)
    {
      this.seqData = seqData;
    }
    else
    {
      SeqCigar[] seqs = new SeqCigar[sequence.length];
      for (int i = 0; i < sequence.length; i++)
      {
        seqs[i] = new SeqCigar(sequence[i], start, end);
      }
      CigarArray sdata = new CigarArray(seqs);
      sdata.addOperation(CigarArray.M, end - start + 1);
      this.seqData = new AlignmentView(sdata, start);
    }
    // System.err.println("Made seqData");// dbg
    if (!(type.equals("NJ")))
    {
      type = "AV";
    }

    if (sm == null && !(pwtype.equals("PID")))
    {
      if (ResidueProperties.getScoreMatrix(pwtype) == null)
      {
        pwtype = "BLOSUM62";
      }
    }

    int i = 0;

    done = new int[sequence.length];

    while ((i < sequence.length) && (sequence[i] != null))
    {
      done[i] = 0;
      i++;
    }

    noseqs = i++;

    distance = findDistances(sm);
    // System.err.println("Made distances");// dbg
    makeLeaves();
    // System.err.println("Made leaves");// dbg

    noClus = cluster.size();

    cluster();
    // System.err.println("Made clusters");// dbg

  }

  /**
   * Generate a string representation of the Tree
   * 
   * @return Newick File with all tree data available
   */
  @Override
  public String toString()
  {
    jalview.io.NewickFile fout = new jalview.io.NewickFile(getTopNode());

    return fout.print(isHasBootstrap(), isHasDistances(),
            isHasRootDistance()); // output all data available for tree
  }

  /**
   * 
   * used when the alignment associated to a tree has changed.
   * 
   * @param list
   *          Sequence set to be associated with tree nodes
   */
  public void UpdatePlaceHolders(List<SequenceI> list)
  {
    Vector<SequenceNode> leaves = findLeaves(top);

    int sz = leaves.size();
    SequenceIdMatcher seqmatcher = null;
    int i = 0;

    while (i < sz)
    {
      SequenceNode leaf = leaves.elementAt(i++);

      if (list.contains(leaf.element()))
      {
        leaf.setPlaceholder(false);
      }
      else
      {
        if (seqmatcher == null)
        {
          // Only create this the first time we need it
          SequenceI[] seqs = new SequenceI[list.size()];

          for (int j = 0; j < seqs.length; j++)
          {
            seqs[j] = list.get(j);
          }

          seqmatcher = new SequenceIdMatcher(seqs);
        }

        SequenceI nam = seqmatcher.findIdMatch(leaf.getName());

        if (nam != null)
        {
          if (!leaf.isPlaceholder())
          {
            // remapping the node to a new sequenceI - should remove any refs to
            // old one.
            // TODO - make many sequenceI to one leaf mappings possible!
            // (JBPNote)
          }
          leaf.setPlaceholder(false);
          leaf.setElement(nam);
        }
        else
        {
          if (!leaf.isPlaceholder())
          {
            // Construct a new placeholder sequence object for this leaf
            leaf.setElement(new Sequence(leaf.getName(),
                    "THISISAPLACEHLDER"));
          }
          leaf.setPlaceholder(true);

        }
      }
    }
  }

  /**
   * rename any nodes according to their associated sequence. This will modify
   * the tree's metadata! (ie the original NewickFile or newly generated
   * BinaryTree's label data)
   */
  public void renameAssociatedNodes()
  {
    applyToNodes(new NodeTransformI()
    {

      @Override
      public void transform(BinaryNode nd)
      {
        Object el = nd.element();
        if (el != null && el instanceof SequenceI)
        {
          nd.setName(((SequenceI) el).getName());
        }
      }
    });
  }

  /**
   * DOCUMENT ME!
   */
  public void cluster()
  {
    while (noClus > 2)
    {
      if (type.equals("NJ"))
      {
        findMinNJDistance();
      }
      else
      {
        findMinDistance();
      }

      Cluster c = joinClusters(mini, minj);

      done[minj] = 1;

      cluster.setElementAt(null, minj);
      cluster.setElementAt(c, mini);

      noClus--;
    }

    boolean onefound = false;

    int one = -1;
    int two = -1;

    for (int i = 0; i < noseqs; i++)
    {
      if (done[i] != 1)
      {
        if (onefound == false)
        {
          two = i;
          onefound = true;
        }
        else
        {
          one = i;
        }
      }
    }

    joinClusters(one, two);
    top = (node.elementAt(one));

    reCount(top);
    findHeight(top);
    findMaxDist(top);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Cluster joinClusters(int i, int j)
  {
    float dist = distance[i][j];

    int noi = cluster.elementAt(i).value.length;
    int noj = cluster.elementAt(j).value.length;

    int[] value = new int[noi + noj];

    for (int ii = 0; ii < noi; ii++)
    {
      value[ii] = cluster.elementAt(i).value[ii];
    }

    for (int ii = noi; ii < (noi + noj); ii++)
    {
      value[ii] = cluster.elementAt(j).value[ii - noi];
    }

    Cluster c = new Cluster(value);

    ri = findr(i, j);
    rj = findr(j, i);

    if (type.equals("NJ"))
    {
      findClusterNJDistance(i, j);
    }
    else
    {
      findClusterDistance(i, j);
    }

    SequenceNode sn = new SequenceNode();

    sn.setLeft((node.elementAt(i)));
    sn.setRight((node.elementAt(j)));

    SequenceNode tmpi = (node.elementAt(i));
    SequenceNode tmpj = (node.elementAt(j));

    if (type.equals("NJ"))
    {
      findNewNJDistances(tmpi, tmpj, dist);
    }
    else
    {
      findNewDistances(tmpi, tmpj, dist);
    }

    tmpi.setParent(sn);
    tmpj.setParent(sn);

    node.setElementAt(sn, i);

    return c;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param tmpi
   *          DOCUMENT ME!
   * @param tmpj
   *          DOCUMENT ME!
   * @param dist
   *          DOCUMENT ME!
   */
  public void findNewNJDistances(SequenceNode tmpi, SequenceNode tmpj,
          float dist)
  {

    tmpi.dist = ((dist + ri) - rj) / 2;
    tmpj.dist = (dist - tmpi.dist);

    if (tmpi.dist < 0)
    {
      tmpi.dist = 0;
    }

    if (tmpj.dist < 0)
    {
      tmpj.dist = 0;
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param tmpi
   *          DOCUMENT ME!
   * @param tmpj
   *          DOCUMENT ME!
   * @param dist
   *          DOCUMENT ME!
   */
  public void findNewDistances(SequenceNode tmpi, SequenceNode tmpj,
          float dist)
  {
    float ih = 0;
    float jh = 0;

    SequenceNode sni = tmpi;
    SequenceNode snj = tmpj;

    while (sni != null)
    {
      ih = ih + sni.dist;
      sni = (SequenceNode) sni.left();
    }

    while (snj != null)
    {
      jh = jh + snj.dist;
      snj = (SequenceNode) snj.left();
    }

    tmpi.dist = ((dist / 2) - ih);
    tmpj.dist = ((dist / 2) - jh);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   */
  public void findClusterDistance(int i, int j)
  {
    int noi = cluster.elementAt(i).value.length;
    int noj = cluster.elementAt(j).value.length;

    // New distances from cluster to others
    float[] newdist = new float[noseqs];

    for (int l = 0; l < noseqs; l++)
    {
      if ((l != i) && (l != j))
      {
        newdist[l] = ((distance[i][l] * noi) + (distance[j][l] * noj))
                / (noi + noj);
      }
      else
      {
        newdist[l] = 0;
      }
    }

    for (int ii = 0; ii < noseqs; ii++)
    {
      distance[i][ii] = newdist[ii];
      distance[ii][i] = newdist[ii];
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   */
  public void findClusterNJDistance(int i, int j)
  {

    // New distances from cluster to others
    float[] newdist = new float[noseqs];

    for (int l = 0; l < noseqs; l++)
    {
      if ((l != i) && (l != j))
      {
        newdist[l] = ((distance[i][l] + distance[j][l]) - distance[i][j]) / 2;
      }
      else
      {
        newdist[l] = 0;
      }
    }

    for (int ii = 0; ii < noseqs; ii++)
    {
      distance[i][ii] = newdist[ii];
      distance[ii][i] = newdist[ii];
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param i
   *          DOCUMENT ME!
   * @param j
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float findr(int i, int j)
  {
    float tmp = 1;

    for (int k = 0; k < noseqs; k++)
    {
      if ((k != i) && (k != j) && (done[k] != 1))
      {
        tmp = tmp + distance[i][k];
      }
    }

    if (noClus > 2)
    {
      tmp = tmp / (noClus - 2);
    }

    return tmp;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float findMinNJDistance()
  {
    float min = 100000;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        if ((done[i] != 1) && (done[j] != 1))
        {
          float tmp = distance[i][j] - (findr(i, j) + findr(j, i));

          if (tmp < min)
          {
            mini = i;
            minj = j;

            min = tmp;
          }
        }
      }
    }

    return min;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float findMinDistance()
  {
    float min = 100000;

    for (int i = 0; i < (noseqs - 1); i++)
    {
      for (int j = i + 1; j < noseqs; j++)
      {
        if ((done[i] != 1) && (done[j] != 1))
        {
          if (distance[i][j] < min)
          {
            mini = i;
            minj = j;

            min = distance[i][j];
          }
        }
      }
    }

    return min;
  }

  /**
   * Calculate a distance matrix given the sequence input data and score model
   * 
   * @return similarity matrix used to compute tree
   */
  public float[][] findDistances(ScoreModelI _pwmatrix)
  {

    float[][] dist = new float[noseqs][noseqs];
    if (_pwmatrix == null)
    {
      // Resolve substitution model
      _pwmatrix = ResidueProperties.getScoreModel(pwtype);
      if (_pwmatrix == null)
      {
        _pwmatrix = ResidueProperties.getScoreMatrix("BLOSUM62");
      }
    }
    dist = _pwmatrix.findDistances(seqData);
    return dist;

  }

  /**
   * DOCUMENT ME!
   */
  public void makeLeaves()
  {
    cluster = new Vector<Cluster>();

    for (int i = 0; i < noseqs; i++)
    {
      SequenceNode sn = new SequenceNode();

      sn.setElement(sequence[i]);
      sn.setName(sequence[i].getName());
      node.addElement(sn);

      int[] value = new int[1];
      value[0] = i;

      Cluster c = new Cluster(value);
      cluster.addElement(c);
    }
  }

  /**
   * Search for leaf nodes below (or at) the given node
   * 
   * @param nd
   *          root node to search from
   * 
   * @return
   */
  public Vector<SequenceNode> findLeaves(SequenceNode nd)
  {
    Vector<SequenceNode> leaves = new Vector<SequenceNode>();
    findLeaves(nd, leaves);
    return leaves;
  }

  /**
   * Search for leaf nodes.
   * 
   * @param nd
   *          root node to search from
   * @param leaves
   *          Vector of leaves to add leaf node objects too.
   * 
   * @return Vector of leaf nodes on binary tree
   */
  Vector<SequenceNode> findLeaves(SequenceNode nd,
          Vector<SequenceNode> leaves)
  {
    if (nd == null)
    {
      return leaves;
    }

    if ((nd.left() == null) && (nd.right() == null)) // Interior node
    // detection
    {
      leaves.addElement(nd);

      return leaves;
    }
    else
    {
      /*
       * TODO: Identify internal nodes... if (node.isSequenceLabel()) {
       * leaves.addElement(node); }
       */
      findLeaves((SequenceNode) nd.left(), leaves);
      findLeaves((SequenceNode) nd.right(), leaves);
    }

    return leaves;
  }

  /**
   * Find the leaf node with a particular ycount
   * 
   * @param nd
   *          initial point on tree to search from
   * @param count
   *          value to search for
   * 
   * @return null or the node with ycound=count
   */
  public Object findLeaf(SequenceNode nd, int count)
  {
    found = _findLeaf(nd, count);

    return found;
  }

  /*
   * #see findLeaf(SequenceNode node, count)
   */
  public Object _findLeaf(SequenceNode nd, int count)
  {
    if (nd == null)
    {
      return null;
    }

    if (nd.ycount == count)
    {
      found = nd.element();

      return found;
    }
    else
    {
      _findLeaf((SequenceNode) nd.left(), count);
      _findLeaf((SequenceNode) nd.right(), count);
    }

    return found;
  }

  /**
   * printNode is mainly for debugging purposes.
   * 
   * @param nd
   *          SequenceNode
   */
  public void printNode(SequenceNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      System.out.println("Leaf = " + ((SequenceI) nd.element()).getName());
      System.out.println("Dist " + nd.dist);
      System.out.println("Boot " + nd.getBootstrap());
    }
    else
    {
      System.out.println("Dist " + nd.dist);
      printNode((SequenceNode) nd.left());
      printNode((SequenceNode) nd.right());
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void findMaxDist(SequenceNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() == null) && (nd.right() == null))
    {
      float dist = nd.dist;

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
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public Vector<SequenceNode> getGroups()
  {
    return groups;
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float getMaxHeight()
  {
    return maxheight;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * @param threshold
   *          DOCUMENT ME!
   */
  public void groupNodes(SequenceNode nd, float threshold)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.height / maxheight) > threshold)
    {
      groups.addElement(nd);
    }
    else
    {
      groupNodes((SequenceNode) nd.left(), threshold);
      groupNodes((SequenceNode) nd.right(), threshold);
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public float findHeight(SequenceNode nd)
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
   * @return DOCUMENT ME!
   */
  public SequenceNode reRoot()
  {
    if (maxdist != null)
    {
      ycount = 0;

      float tmpdist = maxdist.dist;

      // New top
      SequenceNode sn = new SequenceNode();
      sn.setParent(null);

      // New right hand of top
      SequenceNode snr = (SequenceNode) maxdist.parent();
      changeDirection(snr, maxdist);
      System.out.println("Printing reversed tree");
      printN(snr);
      snr.dist = tmpdist / 2;
      maxdist.dist = tmpdist / 2;

      snr.setParent(sn);
      maxdist.setParent(sn);

      sn.setRight(snr);
      sn.setLeft(maxdist);

      top = sn;

      ycount = 0;
      reCount(top);
      findHeight(top);
    }

    return top;
  }

  /**
   * 
   * @return true if original sequence data can be recovered
   */
  public boolean hasOriginalSequenceData()
  {
    return seqData != null;
  }

  /**
   * Returns original alignment data used for calculation - or null where not
   * available.
   * 
   * @return null or cut'n'pasteable alignment
   */
  public String printOriginalSequenceData(char gapChar)
  {
    if (seqData == null)
    {
      return null;
    }

    StringBuffer sb = new StringBuffer();
    String[] seqdatas = seqData.getSequenceStrings(gapChar);
    for (int i = 0; i < seqdatas.length; i++)
    {
      sb.append(new jalview.util.Format("%-" + 15 + "s").form(sequence[i]
              .getName()));
      sb.append(" " + seqdatas[i] + "\n");
    }
    return sb.toString();
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void printN(SequenceNode nd)
  {
    if (nd == null)
    {
      return;
    }

    if ((nd.left() != null) && (nd.right() != null))
    {
      printN((SequenceNode) nd.left());
      printN((SequenceNode) nd.right());
    }
    else
    {
      System.out.println(" name = " + ((SequenceI) nd.element()).getName());
    }

    System.out.println(" dist = " + nd.dist + " " + nd.count + " "
            + nd.height);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void reCount(SequenceNode nd)
  {
    ycount = 0;
    _lycount = 0;
    // _lylimit = this.node.size();
    _reCount(nd);
  }

  private long _lycount = 0, _lylimit = 0;

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void _reCount(SequenceNode nd)
  {
    // if (_lycount<_lylimit)
    // {
    // System.err.println("Warning: depth of _recount greater than number of nodes.");
    // }
    if (nd == null)
    {
      return;
    }
    _lycount++;

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
    _lycount--;
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   */
  public void swapNodes(SequenceNode nd)
  {
    if (nd == null)
    {
      return;
    }

    SequenceNode tmp = (SequenceNode) nd.left();

    nd.setLeft(nd.right());
    nd.setRight(tmp);
  }

  /**
   * DOCUMENT ME!
   * 
   * @param nd
   *          DOCUMENT ME!
   * @param dir
   *          DOCUMENT ME!
   */
  public void changeDirection(SequenceNode nd, SequenceNode dir)
  {
    if (nd == null)
    {
      return;
    }

    if (nd.parent() != top)
    {
      changeDirection((SequenceNode) nd.parent(), nd);

      SequenceNode tmp = (SequenceNode) nd.parent();

      if (dir == nd.left())
      {
        nd.setParent(dir);
        nd.setLeft(tmp);
      }
      else if (dir == nd.right())
      {
        nd.setParent(dir);
        nd.setRight(tmp);
      }
    }
    else
    {
      if (dir == nd.left())
      {
        nd.setParent(nd.left());

        if (top.left() == nd)
        {
          nd.setRight(top.right());
        }
        else
        {
          nd.setRight(top.left());
        }
      }
      else
      {
        nd.setParent(nd.right());

        if (top.left() == nd)
        {
          nd.setLeft(top.right());
        }
        else
        {
          nd.setLeft(top.left());
        }
      }
    }
  }

  /**
   * DOCUMENT ME!
   * 
   * @return DOCUMENT ME!
   */
  public SequenceNode getMaxDist()
  {
    return maxdist;
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
  public boolean isHasDistances()
  {
    return hasDistances;
  }

  /**
   * 
   * @return true if tree has real bootstrap values
   */
  public boolean isHasBootstrap()
  {
    return hasBootstrap;
  }

  public boolean isHasRootDistance()
  {
    return hasRootDistance;
  }

  /**
   * apply the given transform to all the nodes in the tree.
   * 
   * @param nodeTransformI
   */
  public void applyToNodes(NodeTransformI nodeTransformI)
  {
    for (Enumeration<SequenceNode> nodes = node.elements(); nodes
            .hasMoreElements(); nodeTransformI.transform(nodes
            .nextElement()))
    {
      ;
    }
  }
}

/**
 * DOCUMENT ME!
 * 
 * @author $author$
 * @version $Revision$
 */
class Cluster
{
  int[] value;

  /**
   * Creates a new Cluster object.
   * 
   * @param value
   *          DOCUMENT ME!
   */
  public Cluster(int[] value)
  {
    this.value = value;
  }
}
